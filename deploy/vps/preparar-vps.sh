#!/usr/bin/env bash
# Preparacao UNICA da VPS (Ubuntu 24.04) para o dscproject. Idempotente: pode rodar de novo.
# NAO mexe no firewall do hPanel, na Geracao 1 nem no DNS — ver docs/pipeline/pipeline-ci-cd.md.
#
# Rode como root (ou sudo), numa sessao SSH que voce NAO vai fechar ate testar outra:
#   sudo bash preparar-vps.sh --admin <seu-usuario> \
#        --chave-hml "ssh-ed25519 AAAA... ci-dscproject-hml" \
#        --chave-prod "ssh-ed25519 AAAA... ci-dscproject-prod"
#
# O que faz: atualiza o SO, unattended-upgrades, fuso, fail2ban, Docker (so se faltar),
# usuario "deploy" com as chaves do CI, /opt/dscproject e hardening do SSH.
set -euo pipefail

ADMIN="" CHAVE_HML="" CHAVE_PROD="" BASE=/opt/dscproject
while [[ $# -gt 0 ]]; do
  case "$1" in
    --admin) ADMIN="${2:-}"; shift 2 ;;
    --chave-hml) CHAVE_HML="${2:-}"; shift 2 ;;
    --chave-prod) CHAVE_PROD="${2:-}"; shift 2 ;;
    *) echo "argumento desconhecido: $1" >&2; exit 2 ;;
  esac
done

passo() { printf '\n== %s\n' "$*"; }
[[ $EUID -eq 0 ]] || { echo "rode com sudo/root" >&2; exit 1; }
[[ -n "$ADMIN" && -n "$CHAVE_HML" && -n "$CHAVE_PROD" ]] || { echo "informe --admin, --chave-hml e --chave-prod" >&2; exit 2; }
for c in "$CHAVE_HML" "$CHAVE_PROD"; do
  [[ "$c" =~ ^ssh-ed25519\ [A-Za-z0-9+/=]+(\ .*)?$ ]] || { echo "chave publica invalida (esperado ssh-ed25519 ...)" >&2; exit 2; }
done
. /etc/os-release
[[ "$ID" == "ubuntu" ]] || { echo "script feito para Ubuntu (encontrado: $ID)" >&2; exit 1; }

passo "Usuario administrador '$ADMIN'"
id "$ADMIN" >/dev/null 2>&1 || { echo "crie antes: adduser $ADMIN && usermod -aG sudo $ADMIN" >&2; exit 1; }
id -nG "$ADMIN" | grep -qw sudo || { echo "$ADMIN precisa estar no grupo sudo" >&2; exit 1; }
admin_home="$(getent passwd "$ADMIN" | cut -d: -f6)"
[[ -s "$admin_home/.ssh/authorized_keys" ]] || { echo "$ADMIN sem chave em $admin_home/.ssh/authorized_keys: o hardening trancaria voce para fora" >&2; exit 1; }

passo "Atualizacoes do sistema"
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get -y -q full-upgrade
apt-get -y -q install ca-certificates curl gnupg unattended-upgrades fail2ban
dpkg-reconfigure -f noninteractive unattended-upgrades
timedatectl set-timezone America/Sao_Paulo
timedatectl set-ntp true || true

passo "Docker"
if command -v docker >/dev/null 2>&1; then
  echo "Docker ja instalado: $(docker --version)"
  docker compose version || { echo "falta o plugin compose: apt-get install docker-compose-plugin (repo oficial Docker)" >&2; exit 1; }
  if dpkg-query -W -f='${Status}' docker.io 2>/dev/null | grep -q "install ok installed"; then
    echo "AVISO: Docker veio do pacote docker.io do Ubuntu. Funciona, mas o recomendado e o repositorio oficial da Docker."
  fi
else
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -q
  apt-get -y -q install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  systemctl enable --now docker
fi

passo "Usuario deploy (chaves do CI)"
id deploy >/dev/null 2>&1 || useradd --create-home --shell /bin/bash deploy
passwd -l deploy >/dev/null
# grupo docker = root na pratica; e o preco do deploy por docker compose
usermod -aG docker deploy
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
{
  echo "restrict $CHAVE_HML"
  echo "restrict $CHAVE_PROD"
} > /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys

passo "Diretorios em $BASE"
install -d -m 750 -o deploy -g deploy "$BASE" "$BASE/proxy" "$BASE/hml" "$BASE/prod"
docker network inspect dscproject-edge >/dev/null 2>&1 || docker network create dscproject-edge >/dev/null

passo "Hardening do SSH"
# 00- porque no sshd vale o PRIMEIRO valor lido (50-cloud-init.conf pode liberar senha)
cat > /etc/ssh/sshd_config.d/00-hardening.conf <<EOF
PermitRootLogin no
PasswordAuthentication no
KbdInteractiveAuthentication no
PubkeyAuthentication yes
AllowUsers $ADMIN deploy
MaxAuthTries 3
LoginGraceTime 30
X11Forwarding no
EOF
sshd -t
systemctl reload ssh
sshd -T | grep -Ei '^(permitrootlogin|passwordauthentication|kbdinteractiveauthentication|allowusers) '

passo "fail2ban (sshd)"
cat > /etc/fail2ban/jail.d/sshd.local <<'EOF'
[sshd]
enabled = true
backend = systemd
maxretry = 5
findtime = 10m
bantime = 1h
EOF
systemctl enable --now fail2ban
systemctl restart fail2ban

passo "Pronto"
cat <<EOF
- Teste AGORA, numa nova janela: ssh $ADMIN@<ip>   (nao feche esta sessao antes)
- Fingerprint para conferir o secret VPS_KNOWN_HOSTS:
$(ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub)
- Proximos passos: .env de proxy/hml/prod, timers de backup, firewall do hPanel.
EOF
