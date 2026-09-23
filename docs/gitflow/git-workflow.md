# Fluxo de Commits e Merges

Guia passo a passo para o fluxo de desenvolvimento, release e deploy em produção.

---

## 1. Trabalho Normal (Feature Branch)

Crie a branch a partir da `main` seguindo a convenção de nomenclatura:

```bash
git checkout main
git checkout -b feature/nome_funcionalidade-breve-descricao-DDMMAAAAHHMM
```

As partes do nome são separadas por hífen (`-`), nunca por `+`. Ex.: `feature/open-finance-conectar-conta-230920261751`.

**Convenção do timestamp no nome da branch:**

| Variável | Significado |
|----------|-------------|
| `DD`     | Dia         |
| `MM`     | Mês         |
| `AAAA`   | Ano         |
| `HH`     | Hora        |
| `MM`     | Minuto      |

Faça seus commits e suba a branch normalmente:

```bash
git add .
git commit -m "feat: minha alteração"
git push origin feature/minha-feature
```

---

## 2. Criar a Release

A partir da branch da tarefa, crie a branch de release com o próximo número de versão após a última release:

```bash
git checkout -b release/1.0.0
```

### Atualizar a versão nos arquivos do projeto

**Back-end — `pom.xml`:**
```xml
<version>1.0.0-SNAPSHOT</version>
```

**Back-end — `lgpd.portal.utils.AppVersao.java`:**
```java
public static String VERSAO = "1.0.0";
```

**Front-end — `package.json`:**
```json
"version": "1.0.0"
```

Commite e suba a branch de release:

```bash
git add .
git commit -m "chore: Atualizando o número de versão para 1.0.0"
git push origin release/1.0.0
```

---

## 3. Merge da Release para Homologação

```bash
git checkout homologacao
git merge release/1.0.0
git push origin homologacao
```

> Faça o deploy para homologação e aguarde a aprovação.

---

## 4. Aprovado — Criar Tag de Produção

Com a release aprovada em homologação, crie a tag de produção a partir da branch de release:

```bash
git checkout release/1.0.0
```

Verifique a última tag criada:

```bash
git tag --sort=-v:refname | head -5
```

Crie a nova tag:

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

---

## 5. Merge da Release para Main

```bash
git checkout main
git merge release/1.0.0
git push origin main
```

---

## Resumo do Fluxo

```
feature/... ──► release/x.x.x ──► homologacao ──► (aprovação) ──► tag vx.x.x + merge main
```

---

## Automação via Script (Recomendado)

O script divide o fluxo em duas fases independentes, executadas em momentos diferentes.

### Fase 1 — Criar e enviar a release para homologação

Execute a partir da branch de feature:

```bash
./scripts/gitflow.sh
```

O script:
1. Verifica que você está em uma branch de feature (bloqueia `main`, `homologacao` e `release/*`)
2. Calcula a próxima versão pelos Conventional Commits:
   - `feat:` → MINOR | `fix:` / `perf:` → PATCH | `BREAKING CHANGE` → MAJOR
   - Primeira release (sem tags anteriores): sempre `1.0.0`
3. Cria a branch `release/X.Y.Z` a partir da feature, atualiza o `pom.xml` e commita
4. Faz push da release e merge em `homologacao`
5. Encerra exibindo o comando para a fase 2

Ao final, faça o deploy em homologação e aguarde a aprovação.

#### Re-execução com novos commits (release pendente)

Se a release ainda não foi aprovada e você fez novos commits na feature, execute o script novamente da mesma branch. Ele detecta que `release/X.Y.Z` já existe e incorpora os novos commits sem criar uma nova release:

```bash
# novos commits feitos na feature branch
./scripts/gitflow.sh
# → mergeia os commits novos na release existente e reenvia para homologacao
```

> **Atenção:** enquanto a tag `vX.Y.Z` não for criada (fase 2 pendente), qualquer outra feature branch que rodar `./scripts/gitflow.sh` também será incorporada à mesma release, pois a versão calculada será idêntica.

---

### Fase 2 — Aprovar: criar tag e mergear em main

Após a aprovação em homologação, execute de qualquer branch:

```bash
./scripts/gitflow.sh aprovar X.Y.Z
```

O script:
1. Verifica que `release/X.Y.Z` existe no remoto e está mergeada em `homologacao`
2. Cria a tag anotada `vX.Y.Z` e faz push
3. Mergeia `release/X.Y.Z` em `main` e faz push
4. Pergunta se deve deletar a branch de release (local e remota)

---

### Resumo dos comandos

| Momento | Comando |
|---|---|
| Após concluir a feature | `./scripts/gitflow.sh` |
| Novos commits na feature (release pendente) | `./scripts/gitflow.sh` (mesma branch) |
| Após aprovação em homologação | `./scripts/gitflow.sh aprovar X.Y.Z` |

**Pré-requisito:** A branch `homologacao` deve existir no repositório remoto.
