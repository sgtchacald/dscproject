#!/usr/bin/env bash
# Aplica compose.yml/Caddyfile do proxy. Chamado pelo deploy de PRODUCAO (so a main
# mexe no roteamento) e na preparacao da VPS. Valida antes de trocar; recarrega sem
# derrubar conexoes quando so o Caddyfile mudou.
#   bash /opt/dscproject/proxy/.recebido/atualizar-proxy.sh   (CI)
#   bash /opt/dscproject/proxy/atualizar-proxy.sh             (manual)
set -euo pipefail

ORIGEM="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ "$(basename "$ORIGEM")" == ".recebido" ]]; then ALVO="$(dirname "$ORIGEM")"; else ALVO="$ORIGEM"; fi
cd "$ALVO"

exec 9>"$ALVO/.proxy.lock"
flock -w 120 9

[[ -f .env ]] || { echo "falta $ALVO/.env (copie de .env.example)" >&2; exit 1; }
docker network inspect dscproject-edge >/dev/null 2>&1 || docker network create dscproject-edge >/dev/null

if [[ "$ORIGEM" != "$ALVO" ]]; then
  docker compose --project-directory "$ALVO" -f "$ORIGEM/compose.yml" config --quiet
  # caddy validate com as mesmas variaveis do .env
  docker run --rm --env-file .env -v "$ORIGEM/Caddyfile:/etc/caddy/Caddyfile:ro" \
    "$(docker compose --project-directory "$ALVO" -f "$ORIGEM/compose.yml" config --images | head -n1)" \
    caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile >/dev/null
  mudou_caddyfile=1; cmp -s "$ORIGEM/Caddyfile" Caddyfile 2>/dev/null && mudou_caddyfile=0
  install -m 644 "$ORIGEM/compose.yml" compose.yml
  # cat mantem o inode: bind mount de arquivo unico nao enxerga arquivo substituido
  cat "$ORIGEM/Caddyfile" > Caddyfile
  install -m 755 "$ORIGEM/atualizar-proxy.sh" atualizar-proxy.sh
  install -m 644 "$ORIGEM/.env.example" .env.example
else
  mudou_caddyfile=1
fi

# up recria o container so se compose/imagem mudaram; senao basta o reload
docker compose -f compose.yml up -d --wait --wait-timeout 60
if [[ "$mudou_caddyfile" == 1 ]]; then
  docker compose -f compose.yml exec -T caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
fi
echo "proxy ok"
