# Pipeline de CI/CD

Plano e manual de uso da esteira de entrega: GitHub Actions → GHCR → VPS Hostinger.
Complementa o [Fluxo de Commits e Merges](../gitflow/git-workflow.md): o GitFlow
continua manual; a pipeline reage aos pushes em `homologacao` e `main`.

```
feature/... ─► release/X.Y.Z ─► homologacao ─────────────────► main
     │               │               │                            │
    ci              ci          entrega (auto)             entrega (aprovação)
                                     │                            │
                     testes ─► imagem :<sha> ─► hml   mesma imagem ─► prod
```

> Mantenha este documento junto com o código: mudou workflow, `deploy/` ou `Dockerfile`,
> atualize a seção correspondente no mesmo commit.

---

## 1. Visão Geral

**O GitHub constrói, testa e publica uma imagem imutável; a VPS só baixa e sobe.**
Nada de `git pull` ou build na VPS.

| Peça | Onde | Faz |
|---|---|---|
| `ci.yml` | `.github/workflows/` | `./mvnw verify` com MySQL 8.4 real. Roda em push de `feature/**`, `release/**` e em PR |
| `entrega.yml` | `.github/workflows/` | Push em `homologacao`/`main` (ou manual): testes → imagem no GHCR → deploy por SSH → smoke test |
| `Dockerfile` | raiz | Empacota o JAR do CI (JRE 21, usuário não-root, healthcheck) |
| `deploy/app/` | → VPS `/opt/dscproject/{hml,prod}/` | `compose.yml` (app + MySQL), `deploy.sh`, `backup-mysql.sh`, `.env.example` |
| `deploy/proxy/` | → VPS `/opt/dscproject/proxy/` | Caddy: TLS automático, único serviço com 80/443 |
| `deploy/systemd/` | → VPS `/etc/systemd/system/` | Timer do backup diário |
| `deploy/vps/preparar-vps.sh` | roda uma vez na VPS | SO atualizado, Docker, usuário `deploy`, hardening SSH, fail2ban |

**Etapas do workflow `entrega`:**

1. `resolver` — define ambiente e tag (SHA do commit) e decide:
   `build` (imagem nova), `promover` (o mesmo conteúdo já tem imagem) ou `nenhuma`.
2. `ci` — build e testes (só quando há `build`).
3. `imagem` — `docker build` + push `ghcr.io/sgtchacald/dscproject:<sha>` e `:tree-<árvore>`,
   com SBOM e proveniência. Em `promover`, só cria a tag `<sha>` apontando para a imagem existente.
4. `deploy` — environment `homologacao` ou `production`; envia `deploy/app/` para a VPS e roda
   `deploy.sh`: dump do banco → `docker compose up --wait` → rollback automático se o healthcheck falhar.
   Em produção também aplica `deploy/proxy/`. Fecha com `GET <APP_URL>/login` pela internet.

**Build once, deploy many.** A imagem leva a tag `tree-<hash da árvore do git>`. O merge
da release na `main` tem o mesmo conteúdo do merge em `homologacao`, então a produção
recebe **a mesma imagem** testada em homologação, sem rebuild. Se o conteúdo diferir
(ex.: `homologacao` tem algo que a `main` não tem), a pipeline refaz testes e imagem.

**Layout na VPS:**

```
/opt/dscproject/
├── proxy/   compose.yml  Caddyfile  .env  atualizar-proxy.sh
├── hml/     compose.yml  deploy.sh  backup-mysql.sh  .env  .tag  deploys.log  backups/
└── prod/    (idem hml)
```

Cada ambiente tem seu próprio MySQL (volume próprio, sem porta publicada, rede interna).

---

## 2. Ambientes

| | Homologação | Produção |
|---|---|---|
| Branch | `homologacao` | `main` |
| Environment GitHub | `homologacao` | `production` (aprovação manual) |
| URL (`vars.APP_URL`) | `https://hml.dscproject.com.br` | `https://dscproject.com.br` (`www` redireciona para o raiz) |
| Perfil Spring | `hml` (`application-hml.properties`) | `prod` (`application-prod.properties`) |
| Pasta na VPS | `/opt/dscproject/hml` | `/opt/dscproject/prod` |
| Banco | `dsc_hml_bd` (MySQL próprio) | `dsc_prod_bd` (MySQL próprio) |
| Memória sugerida (KVM 2, 8 GB) | app 1 GB + MySQL 768 MB | app 1,5 GB + MySQL 1 GB |

Os domínios ficam em variáveis (`APP_URL`, `APP_BASE_URL`, `PROD_DOMINIO`, `HML_DOMINIO`);
trocar de domínio não exige mudar código.

---

## 3. Publicar em Homologação

Siga o passo 4 do [git-workflow](../gitflow/git-workflow.md#4-merge-da-release-para-homologação).
O `git push origin homologacao` basta:

1. Acompanhe em **Actions → entrega** (ou `gh run watch`).
2. Ao final, o resumo do job mostra a tag publicada e a URL.
3. Valide em `https://hml.dscproject.com.br`.

Falhou nos testes: nada muda na VPS. Falhou o healthcheck: o `deploy.sh` volta sozinho para
a versão anterior e o job fica vermelho.

---

## 4. Publicar em Produção

Siga o passo 5 do [git-workflow](../gitflow/git-workflow.md#5-aprovado--tag-de-produção-e-merge-na-main)
(tag `vX.Y.Z` + merge na `main` + push):

1. O workflow `entrega` roda e **para no job `deploy`**, aguardando revisão.
2. Aprove em **Actions → run → Review deployments → production → Approve and deploy**
   (o GitHub também avisa por e-mail).
3. Depois do deploy, confira `https://dscproject.com.br`.

Pedidos na fila: se chegar um push novo na `main` enquanto um deploy aguarda aprovação,
o GitHub mantém só o mais recente pendente.

---

## 5. Rollback

**Automático** — o `deploy.sh` volta para a tag anterior (e para o `compose.yml` anterior)
quando o healthcheck da nova versão falha. Histórico em `deploys.log`.

**Manual pelo GitHub** (versão sobe saudável, mas com defeito):

1. Descubra a tag anterior: `cat /opt/dscproject/prod/deploys.log` na VPS, ou o histórico
   de runs do `entrega` (SHA de 40 caracteres).
2. **Actions → entrega → Run workflow**:
   - *Use workflow from*: `main` para produção, `homologacao` para homologação
     (o environment só aceita a própria branch);
   - `ambiente` e `tag` (SHA completo).
3. Produção pede aprovação de novo.

Via `gh`: `gh workflow run entrega.yml --ref main -f ambiente=production -f tag=<sha>`.

**Direto na VPS** (GitHub fora do ar; a imagem precisa estar no disco — o deploy mantém 14 dias):

```bash
sudo -iu deploy
bash /opt/dscproject/prod/deploy.sh --ambiente prod --tag <sha-anterior>
```

**Banco** — voltar a imagem **não** desfaz alteração de schema. O Hibernate (`ddl-auto=update`)
só acrescenta tabelas/colunas, então a versão anterior costuma funcionar com o schema novo.
Se dados foram estragados, restaure o dump `predeploy-*` (seção 8): perde o que foi gravado
depois dele — decisão humana.

---

## 6. Secrets e Variáveis

Nunca coloque valores neste documento, no código ou em chat.

**GitHub — por environment** (Settings → Environments → `homologacao` / `production`; mesmos nomes nos dois):

| Nome | Tipo | Para quê |
|---|---|---|
| `VPS_SSH_KEY` | secret | Chave privada ed25519 exclusiva do CI **daquele** ambiente (usuário `deploy`) |
| `VPS_KNOWN_HOSTS` | secret | Linha de `ssh-keyscan -t ed25519 <VPS_HOST>` — impede servidor falso (MITM) |
| `VPS_HOST` | var | IP ou hostname da VPS; tem de ser o mesmo usado no `ssh-keyscan` |
| `VPS_USUARIO` | var | Opcional, padrão `deploy` |
| `VPS_DIR` | var | Opcional, padrão `/opt/dscproject` |
| `APP_URL` | var | `https://hml.dscproject.com.br` / `https://dscproject.com.br` (sem barra) — smoke test e link do environment |

O login no GHCR usa o `GITHUB_TOKEN` do próprio job (expira no fim dele); não há PAT.

**VPS — `/opt/dscproject/<hml|prod>/.env`** (modelo: `deploy/app/.env.example`; `chmod 600`, dono `deploy`):

| Nome | Para quê |
|---|---|
| `AMBIENTE` | `hml` ou `prod` — perfil Spring e nome do projeto compose; o `deploy.sh` confere |
| `APP_BASE_URL` | Link do e-mail de recuperação de senha |
| `APP_IMAGEM` | Padrão `ghcr.io/sgtchacald/dscproject` |
| `MYSQL_BANCO`, `MYSQL_USUARIO`, `MYSQL_SENHA` | Banco e usuário da aplicação |
| `MYSQL_ROOT_SENHA` | Root do MySQL (backup/restore) |
| `APP_REMEMBER_ME_KEY` | Assinatura do cookie "Lembrar-me" |
| `GMAIL_APP_EMAIL`, `GMAIL_APP_SENHA` | SMTP (senha de app do Gmail) |
| `APP_ADMIN_*` | ADMIN inicial; `APP_ADMIN_OBRIGATORIO=true` faz o boot falhar sem ADMIN |
| `APP_MEMORIA`, `MYSQL_MEMORIA`, `MYSQL_BUFFER_POOL`, `MYSQL_MAX_CONEXOES` | Limites de recurso |
| `BACKUP_RETENCAO_DIAS` | Dias de retenção dos dumps diários |
| `BACKUP_HEALTHCHECK_URL` | Opcional: URL de ping do Healthchecks.io para o backup diário (seção 7.6) |

Gerar segredo: `openssl rand -base64 36 | tr -d '/+=' | cut -c1-40`. Homologação e produção
usam valores **diferentes**.

**VPS — `/opt/dscproject/proxy/.env`**: `PROD_DOMINIO`, `HML_DOMINIO`, `ACME_EMAIL` (sem segredo).

---

## 7. Preparação Única

Ordem recomendada. Dados da VPS: KVM 2, Ubuntu 24.04, `srv1074476.hstgr.cloud`,
IP `31.97.28.150`, VM ID `1074476`, data center **Brasil (São Paulo)**.

### 7.1 Verificação da VPS (somente leitura)

A Geração 1 já foi removida. Antes de começar, tire um snapshot
(hPanel → VPS → **Snapshots**; ponto de retorno, expira e só existe um) e confirme
que não sobrou nada dela:

```bash
sudo ss -tlnp '( sport = :80 or sport = :443 )'       # tem de sair vazio
sudo ss -tlnp                                         # nada inesperado escutando (8080, 3306...)
command -v docker && docker ps -a && docker volume ls # sem containers/volumes antigos
systemctl list-units --type=service --state=running   # sem nginx, apache2, serviço Java antigo
systemctl list-timers --all | grep -i certbot         # sem renovação do certbot antigo
free -h; df -h
```

Sobrou algo: remova antes de subir o proxy (o Caddy precisa de 80/443 livres).

**Acesso à Binance** a partir da VPS (verificação de ambiente; a Binance bloqueia data centers nos EUA):

```bash
curl -s -o /dev/null -w '%{http_code}
' https://api.binance.com/api/v3/ping
```

`200` = liberado (esperado no data center de São Paulo). `451` ou `403` = região bloqueada:
a integração com a Binance não funcionará dessa VPS.

### 7.2 hPanel

1. **Data center**: Brasil (São Paulo) — conferível em hPanel → **VPS** → `srv1074476` →
   **Visão geral** (*Overview*). A localização é fixa depois de criada a VPS.
2. **Firewall** (VPS → Segurança → Firewall): crie um grupo com *accept* para
   TCP 22, TCP 80, TCP 443 e UDP 443; **só então** ative. Qualquer outra porta
   fica fechada por não ter regra. O firewall do painel é a barreira principal:
   porta publicada pelo Docker ignora o `ufw`.
3. **Backups**: mantenha o backup semanal do hPanel ligado — é a **única cópia fora da VPS** (seção 8).

### 7.3 DNS no registro.br

Em registro.br → *Meus domínios* → `dscproject.com.br` → **DNS** → editar zona
(com os servidores DNS do próprio Registro.br; se o domínio usa outro DNS, faça lá):

| Tipo | Nome | Valor |
|---|---|---|
| A | *(vazio — raiz)* | `31.97.28.150` |
| A | `www` | `31.97.28.150` |
| A | `hml` | `31.97.28.150` |
| CAA *(se o editor oferecer)* | *(vazio)* | `0 issue "letsencrypt.org"` |

Não crie `AAAA` enquanto o IPv6 da VPS não estiver testado (o Let's Encrypt prefere IPv6 e
falha se ele não responder). Confira a propagação antes de subir o proxy:
`nslookup hml.dscproject.com.br 8.8.8.8`. Registros de e-mail (MX/TXT) não mudam.

### 7.4 VPS

1. Usuário pessoal (como root, com a chave pública do seu PC):
   `adduser <voce> && usermod -aG sudo <voce>` e `~<voce>/.ssh/authorized_keys`.
   Teste `ssh <voce>@31.97.28.150` numa janela nova.
2. No seu PC, gere as chaves do CI (uma por ambiente):

   ```bash
   ssh-keygen -t ed25519 -N "" -C ci-dscproject-hml  -f ci-dscproject-hml
   ssh-keygen -t ed25519 -N "" -C ci-dscproject-prod -f ci-dscproject-prod
   ```

3. Envie e rode o preparo (verifica se o Docker já existe antes de instalar):

   ```bash
   scp deploy/vps/preparar-vps.sh ci-dscproject-hml.pub ci-dscproject-prod.pub <voce>@31.97.28.150:~
   ssh <voce>@31.97.28.150
   sudo bash preparar-vps.sh --admin <voce> \
     --chave-hml "$(cat ~/ci-dscproject-hml.pub)" --chave-prod "$(cat ~/ci-dscproject-prod.pub)"
   ```

   Anote a fingerprint impressa no final. **Não feche a sessão** antes de testar um login novo.
4. Copie os arquivos e crie os `.env` (como `deploy`):

   ```bash
   scp -r deploy/proxy deploy/app deploy/systemd <voce>@31.97.28.150:/tmp/dsc
   ssh <voce>@31.97.28.150
   sudo cp /tmp/dsc/systemd/* /etc/systemd/system/ && sudo systemctl daemon-reload
   sudo chown -R deploy:deploy /tmp/dsc && sudo -iu deploy
   cp /tmp/dsc/proxy/{compose.yml,Caddyfile,atualizar-proxy.sh,.env.example} /opt/dscproject/proxy/
   for a in hml prod; do cp /tmp/dsc/app/{compose.yml,deploy.sh,backup-mysql.sh,.env.example} /opt/dscproject/$a/; done
   cd /opt/dscproject
   for d in proxy hml prod; do cp $d/.env.example $d/.env && chmod 600 $d/.env; done
   nano proxy/.env; nano hml/.env; nano prod/.env      # AMBIENTE=hml no hml/.env!
   ```

5. Com o DNS propagado e 80/443 livres (7.1), suba o proxy:
   `bash /opt/dscproject/proxy/atualizar-proxy.sh` e acompanhe a emissão dos certificados em
   `docker logs -f dscproject-proxy-caddy-1`.
6. Backup diário: `sudo systemctl enable --now dscproject-backup@prod.timer dscproject-backup@hml.timer`.
7. Remova `/tmp/dsc`.

### 7.5 GitHub

1. **Settings → Actions → General**: *Workflow permissions* = **Read repository contents**;
   desmarque *Allow GitHub Actions to create and approve pull requests*;
   *Fork pull request workflows*: exigir aprovação para colaboradores externos.
2. **Settings → Environments**:
   - `homologacao`: *Deployment branches* = *Selected branches* → `homologacao`. Sem revisor.
   - `production`: *Deployment branches* → `main`; *Required reviewers* = você;
     **desmarque** *Prevent self-review* (senão você não consegue aprovar o próprio deploy).
3. Secrets e vars de cada environment (seção 6). Pelo `gh`, no seu PC:

   ```bash
   ssh-keyscan -t ed25519 31.97.28.150 > kh && ssh-keygen -lf kh   # igual à fingerprint da 7.4
   for e in homologacao production; do
     gh secret set VPS_KNOWN_HOSTS --env $e < kh
     gh variable set VPS_HOST --env $e --body 31.97.28.150
   done
   gh secret set VPS_SSH_KEY --env homologacao < ci-dscproject-hml
   gh secret set VPS_SSH_KEY --env production  < ci-dscproject-prod
   gh variable set APP_URL --env homologacao --body https://hml.dscproject.com.br
   gh variable set APP_URL --env production  --body https://dscproject.com.br
   rm ci-dscproject-hml ci-dscproject-prod kh      # a chave privada só existe no GitHub
   ```

4. **Settings → Rules → Rulesets**:
   - `branches-protegidas` (alvo: `main`, `homologacao`): *Restrict deletions* e *Block force pushes*.
   - `tags-de-release` (alvo: tags `v*`): *Restrict deletions* e *Restrict updates*.
   - Não ative *Require a pull request* nem *Require status checks*: os merges do GitFlow são
     locais e seriam recusados. A `entrega` já roda os testes antes de gerar a imagem.
     Se um dia os merges passarem a ser por PR, exija o check `build-e-testes`.
5. **Settings → Code security**: Dependabot alerts e security updates, secret scanning com
   push protection e CodeQL (*default setup*).
6. Depois do primeiro run, em **Packages → dscproject**: confira que o pacote está ligado ao
   repositório. Pode ficar privado (o deploy usa o token do job).

### 7.6 Monitoramento Externo

Use um serviço **gratuito fora da VPS** — monitor na própria VPS não percebe a VPS caída.
UptimeRobot ou Better Stack (Uptime), plano gratuito:

| Monitor | Tipo | Verificação | Intervalo |
|---|---|---|---|
| `https://dscproject.com.br/login` | HTTP(s) — *Keyword* | palavra presente na tela de login (ou só status 200) | 5 min |
| `https://hml.dscproject.com.br/login` | HTTP(s) — *Keyword* | idem | 5 min |

- **Alerta**: por e-mail (contato de alerta = seu e-mail), ao cair e ao voltar.
- **Certificado**: ative o aviso de expiração do SSL (UptimeRobot: *SSL errors / expiry reminders*
  no monitor; Better Stack: *Verify SSL* e *SSL expiration* no monitor) com 14 dias de antecedência.
  O Caddy renova sozinho; o alerta pega renovação quebrada.
- Keyword: prefira um texto fixo da tela de login (ex.: o título); troque se a tela mudar.

**Backup diário (opcional) — Healthchecks.io** como *dead man's switch*:

1. Crie uma conta gratuita e um check por ambiente (`dscproject-prod-backup`, `dscproject-hml-backup`),
   *Period* 1 dia, *Grace* 2 horas, alerta por e-mail.
2. Copie a *ping URL* de cada check para `BACKUP_HEALTHCHECK_URL` no `.env` do ambiente.
3. O `backup-mysql.sh diario` envia `/start`, sucesso ou `/fail`. Sem ping no prazo, o
   Healthchecks.io alerta — pega também timer parado e VPS fora do ar.

Vazio = sem ping; o backup funciona igual.

### 7.7 Primeiro Deploy

1. Push em `homologacao` (ou *Re-run* do último `entrega`) → confira `https://hml.dscproject.com.br`,
   faça login com o ADMIN inicial e troque a senha.
2. Merge na `main` → aprove → confira `https://dscproject.com.br`.
3. Esvazie `APP_ADMIN_SENHA` nos `.env` depois da troca de senha.
4. Confira os monitores da 7.6 verdes.

---

## 8. Backup e Restore

| Camada | Quando | Onde | Retenção |
|---|---|---|---|
| `predeploy-*.sql.gz` | antes de cada deploy | `<amb>/backups/` na VPS | últimos 5 |
| `diario-*.sql.gz` | 03:15 (timer systemd) | `<amb>/backups/` na VPS | `BACKUP_RETENCAO_DIAS` (padrão 7) |
| Backup do hPanel | semanal | Hostinger | até 4; restaura a VPS inteira |

> **Risco conhecido (aceito):** os dumps ficam só na VPS. Se a VPS for perdida, os dumps
> vão junto, e o backup semanal da Hostinger passa a ser a única cópia fora dela —
> até 7 dias de dados perdidos no pior caso.

Backup avulso: `sudo -iu deploy bash /opt/dscproject/prod/backup-mysql.sh manual`.

**Restore em produção** (sobrescreve as tabelas do dump; o que foi gravado depois se perde):

```bash
sudo -iu deploy
cd /opt/dscproject/prod
export APP_TAG="$(cat .tag)"
bash backup-mysql.sh manual                        # guarda o estado atual antes
docker compose stop app
gunzip -c backups/<arquivo>.sql.gz \
  | docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -u root'
docker compose start app
```

**Teste de restore (mensal)** — num MySQL descartável, sem rede:

```bash
docker run -d --name restore-teste --network none -e MYSQL_ROOT_PASSWORD=teste mysql:8.4.11
sleep 60
gunzip -c <arquivo>.sql.gz | docker exec -i -e MYSQL_PWD=teste restore-teste mysql -u root
docker exec -e MYSQL_PWD=teste restore-teste mysql -u root -e \
  "select table_name, table_rows from information_schema.tables where table_schema='dsc_prod_bd'"
docker rm -f restore-teste
```

Não copie o banco de produção para homologação sem mascarar dados pessoais.

---

## 9. Troubleshooting

Leia primeiro o **passo que falhou** e a **primeira** mensagem de erro, não a última linha.

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `ci` vermelho em *Build e testes* | teste quebrado | baixe o artifact `relatorios-teste` do run |
| `resolver`: `imagem ... nao existe no GHCR` | tag errada no dispatch | use o SHA completo de um deploy anterior |
| `deploy` parado em *Waiting* | aguardando aprovação de `production` | *Review deployments* |
| `deploy`: *environment protection rules* / branch não permitida | dispatch da branch errada | *Use workflow from* `main` (prod) ou `homologacao` (hml) |
| `Host key verification failed` | `VPS_KNOWN_HOSTS` desatualizado (VPS reinstalada) | refaça o `ssh-keyscan`, confira a fingerprint na VPS |
| `Permission denied (publickey)` | chave errada no environment ou `authorized_keys` | confira `/home/deploy/.ssh/authorized_keys` |
| `o .env ... e de 'hml', nao de 'prod'` | `AMBIENTE` errado no `.env` | corrija o `.env` da pasta |
| `outro deploy deste ambiente em andamento` | deploy manual simultâneo | espere o outro terminar |
| `healthcheck falhou` + rollback | app não subiu (variável faltando, banco) | log impresso no job; `docker compose logs app` na pasta do ambiente |
| `Could not resolve placeholder 'APP_...'` | variável obrigatória ausente no `.env` | preencha e rode o deploy de novo |
| Smoke test falha, container saudável | DNS, firewall do hPanel ou certificado | `docker logs dscproject-proxy-caddy-1`; `curl -v https://<dominio>/login` |
| Caddy sem certificado | DNS não aponta para a VPS, porta 80 fechada ou IPv6 quebrado | confira A/AAAA e o firewall; limite do Let's Encrypt: aguarde |
| `unauthorized` no pull | pacote do GHCR sem acesso do repositório | Package settings → *Manage Actions access* → adicionar `dscproject` |
| Container reiniciando com `OutOfMemoryError` | `APP_MEMORIA` baixo | aumente no `.env` e redeploy |

Comandos úteis na VPS (`sudo -iu deploy`, dentro da pasta do ambiente, com `export APP_TAG="$(cat .tag)"`):
`docker compose ps`, `docker compose logs -f --tail 200 app`, `docker stats --no-stream`, `tail deploys.log`.

---

## 10. Manutenção

- **Versões fixadas**: actions por SHA (comentário com a versão), imagens por tag + digest
  (`Dockerfile`, `deploy/app/compose.yml`, `deploy/proxy/compose.yml`, `ci.yml`). Atualize
  numa feature, pelo fluxo normal. Última versão de uma action:
  `git ls-remote --tags --refs https://github.com/<owner>/<action>`.
- **Chaves do CI**: rotacione uma vez por ano ou quando alguém sair — nova chave no
  `authorized_keys`, novo secret, remove a antiga.
- **Mudou `deploy/proxy/`**: aplicado no próximo deploy de produção.
- **Mudou `deploy/systemd/` ou `deploy/vps/`**: aplicar à mão na VPS (não vão pelo pipeline).

---

## 11. Riscos Conhecidos e Pendências

**Riscos aceitos:**

- Schema mantido pelo Hibernate (`ddl-auto=update`), sem Flyway/Liquibase por enquanto.
  Mitigação: dump antes de cada deploy e só mudanças aditivas (nova coluna/tabela).
- Backup do banco só na VPS (seção 8).
- Chaves de hml e prod no mesmo usuário `deploy` (grupo docker ≈ root): a chave de hml
  também alcança produção. Aceito para 1 pessoa; evoluir para `command=` restrito no `authorized_keys`.

**Pendências:**

| # | Pendência | Padrão adotado até decidir |
|---|---|---|
| 1 | MySQL de dev em 8.0 (fim de suporte em abr/2026) × 8.4 em hml/prod/CI | Migrar `docker-compose.yml` de dev para `mysql:8.4` |
| 2 | Scan de imagem (Trivy) e Dependabot para actions/imagens | Não incluídos; SBOM já é gerado |
| 3 | Limpeza de versões antigas no GHCR | Manual em Packages |
