#!/usr/bin/env bash
# Dump logico do MySQL de UM ambiente para <ambiente>/backups/ (so na VPS).
# No tipo "diario", se BACKUP_HEALTHCHECK_URL estiver no .env, avisa o Healthchecks.io
# (inicio, sucesso ou falha) — dead man's switch: sem ping no prazo, alerta.
#
#   bash /opt/dscproject/prod/backup-mysql.sh diario      # timer systemd, 1x por dia
#   bash /opt/dscproject/prod/backup-mysql.sh predeploy   # chamado pelo deploy.sh
set -euo pipefail

TIPO="${1:-diario}"
[[ "$TIPO" =~ ^(diario|predeploy|manual)$ ]] || { echo "uso: $0 diario|predeploy|manual" >&2; exit 2; }

ALVO="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[[ "$(basename "$ALVO")" == ".recebido" ]] && ALVO="$(dirname "$ALVO")"
cd "$ALVO"

ler_env() { grep -E "^$1=" .env 2>/dev/null | tail -n1 | cut -d= -f2- | tr -d '"'"'"'\r' || true; }
RETENCAO="$(ler_env BACKUP_RETENCAO_DIAS)"; RETENCAO="${RETENCAO:-7}"
HC_URL="$(ler_env BACKUP_HEALTHCHECK_URL)"
AMBIENTE="$(ler_env AMBIENTE)"

# O compose interpola APP_TAG mesmo para "exec"; o valor nao importa aqui
export APP_TAG="${APP_TAG:-$(cat .tag 2>/dev/null || echo 0000000000000000000000000000000000000000)}"

# Ping so no backup diario; falha do ping nunca derruba o backup
ping_hc() {
  [[ -n "$HC_URL" && "$TIPO" == "diario" ]] || return 0
  curl -fsS -m 10 --retry 3 -o /dev/null "$HC_URL$1" || true
}
ao_sair() { local rc=$?; if [[ $rc -ne 0 ]]; then ping_hc "/fail"; fi; }
trap ao_sair EXIT
ping_hc "/start"

mkdir -p backups && chmod 700 backups
arquivo="backups/${TIPO}-${AMBIENTE:-db}-$(date -u +%Y%m%dT%H%M%SZ).sql.gz"

docker compose --project-directory "$ALVO" -f compose.yml exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -u root --single-transaction --quick \
     --routines --triggers --events --set-gtid-purged=OFF --databases "$MYSQL_DATABASE"' \
  | gzip -6 > "$arquivo.tmp"
gzip -t "$arquivo.tmp"
mv "$arquivo.tmp" "$arquivo"
chmod 600 "$arquivo"
echo "backup: $ALVO/$arquivo ($(du -h "$arquivo" | cut -f1))"

# Retencao local
find backups -maxdepth 1 -name "diario-*.sql.gz" -mtime +"$RETENCAO" -delete
ls -1t backups/predeploy-*.sql.gz 2>/dev/null | tail -n +6 | xargs -r rm -f || true
ls -1t backups/manual-*.sql.gz 2>/dev/null | tail -n +6 | xargs -r rm -f || true

ping_hc ""
