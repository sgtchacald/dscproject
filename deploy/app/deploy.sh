#!/usr/bin/env bash
# Sobe a imagem <tag> de UM ambiente (hml|prod) na VPS, com backup antes e rollback
# automatico se o healthcheck falhar.
#
# Pelo CI:  tar dos arquivos -> /opt/dscproject/<amb>/.recebido/  e
#           bash /opt/dscproject/<amb>/.recebido/deploy.sh --ambiente <amb> --tag <sha> [--ghcr-usuario <u>]
#           (com --ghcr-usuario, o token do GHCR chega pela stdin)
# Manual (rollback, GitHub fora do ar):
#           bash /opt/dscproject/<amb>/deploy.sh --ambiente <amb> --tag <sha-anterior>
set -euo pipefail

uso() { echo "uso: $0 --ambiente hml|prod --tag <sha40> [--ghcr-usuario <usuario>]" >&2; exit 2; }

AMBIENTE="" TAG="" GHCR_USUARIO=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --ambiente) AMBIENTE="${2:-}"; shift 2 ;;
    --tag) TAG="${2:-}"; shift 2 ;;
    --ghcr-usuario) GHCR_USUARIO="${2:-}"; shift 2 ;;
    *) uso ;;
  esac
done
[[ "$AMBIENTE" =~ ^(hml|prod)$ ]] || uso
[[ "$TAG" =~ ^[0-9a-f]{40}$ ]] || { echo "tag invalida: '$TAG' (esperado SHA de 40 caracteres)" >&2; exit 2; }

ORIGEM="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ "$(basename "$ORIGEM")" == ".recebido" ]]; then ALVO="$(dirname "$ORIGEM")"; else ALVO="$ORIGEM"; fi
cd "$ALVO"

log() { printf '[%s] [%s] %s\n' "$(date '+%F %T')" "$AMBIENTE" "$*"; }

exec 9>"$ALVO/.deploy.lock"
flock -n 9 || { log "outro deploy deste ambiente em andamento"; exit 1; }

[[ -f .env ]] || { log "falta $ALVO/.env (copie de .env.example)"; exit 1; }
env_ambiente="$(grep -E '^AMBIENTE=' .env | tail -n1 | cut -d= -f2- | tr -d '"'"'"' \r' || true)"
[[ "$env_ambiente" == "$AMBIENTE" ]] || { log "o .env de $ALVO e de '$env_ambiente', nao de '$AMBIENTE'"; exit 1; }

# Instala os arquivos recebidos do CI; guarda o compose anterior para o rollback
if [[ "$ORIGEM" != "$ALVO" ]]; then
  [[ -f compose.yml ]] && cp -p compose.yml compose.anterior.yml
  install -m 644 "$ORIGEM/compose.yml" compose.yml
  install -m 755 "$ORIGEM/deploy.sh" deploy.sh
  install -m 755 "$ORIGEM/backup-mysql.sh" backup-mysql.sh
  install -m 644 "$ORIGEM/.env.example" .env.example
fi

docker network inspect dscproject-edge >/dev/null 2>&1 || docker network create dscproject-edge >/dev/null

# Login no GHCR isolado neste deploy (token do job, expira no fim dele)
if [[ -n "$GHCR_USUARIO" ]]; then
  DOCKER_CONFIG="$(mktemp -d)"; export DOCKER_CONFIG
  trap 'rm -rf "$DOCKER_CONFIG"' EXIT
  docker login ghcr.io -u "$GHCR_USUARIO" --password-stdin >/dev/null
fi

ANTERIOR="$(cat .tag 2>/dev/null || true)"

compose() { docker compose --project-directory "$ALVO" "$@"; }

sobe() {
  local tag="$1" arquivo="$2"
  APP_TAG="$tag" compose -f "$arquivo" pull --quiet app
  APP_TAG="$tag" compose -f "$arquivo" up -d --remove-orphans --wait --wait-timeout 300
}

log "deploy ${ANTERIOR:-<primeiro>} -> $TAG"
APP_TAG="$TAG" compose -f compose.yml config --quiet
APP_TAG="$TAG" compose -f compose.yml pull --quiet app

# Hibernate (ddl-auto=update) altera o schema na subida: dump antes
if [[ -n "$(APP_TAG="${ANTERIOR:-$TAG}" compose -f compose.yml ps --status running -q db 2>/dev/null)" ]]; then
  bash "$ALVO/backup-mysql.sh" predeploy
fi

if sobe "$TAG" compose.yml; then
  echo "$TAG" > .tag
  printf '%s %s %s\n' "$(date -u +%FT%TZ)" "$TAG" "ok" >> deploys.log
  # Mantem imagens recentes deste repositorio para rollback; nao toca em outras imagens da VPS
  docker image prune -af \
    --filter "label=org.opencontainers.image.source=https://github.com/sgtchacald/dscproject" \
    --filter "until=336h" >/dev/null || true
  log "ok: $TAG no ar"
  exit 0
fi

log "healthcheck falhou para $TAG; ultimas linhas do log da app:"
APP_TAG="$TAG" compose -f compose.yml logs --tail 80 app || true
printf '%s %s %s\n' "$(date -u +%FT%TZ)" "$TAG" "falhou" >> deploys.log

if [[ -n "$ANTERIOR" ]]; then
  arquivo=compose.yml; [[ -f compose.anterior.yml ]] && arquivo=compose.anterior.yml
  log "rollback para $ANTERIOR ($arquivo)"
  if sobe "$ANTERIOR" "$arquivo"; then
    [[ "$arquivo" == compose.anterior.yml ]] && cp -p compose.anterior.yml compose.yml
    log "rollback concluido: $ANTERIOR no ar"
  else
    log "ROLLBACK FALHOU: ambiente fora do ar, intervencao manual"
  fi
fi
exit 1
