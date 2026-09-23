# Fluxo de Commits e Merges

Guia passo a passo para o fluxo de desenvolvimento, release e deploy em produção.
Todos os passos são executados manualmente com `git`.

```
feature/... ──► release/X.Y.Z ──► homologacao ──► (aprovação) ──► tag vX.Y.Z + merge main
```

---

## 1. Trabalho Normal (Feature Branch)

Crie a branch a partir da `main` atualizada:

```bash
git checkout main
git pull origin main
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

Commite em Conventional Commits PT-BR (um commit por assunto) e suba a branch:

```bash
git add <arquivos>
git commit -m "feat(modulo): minha alteração"
git push -u origin feature/nome_funcionalidade-breve-descricao-DDMMAAAAHHMM
```

---

## 2. Calcular a Versão

Parta da **última tag** e dos commits desde ela:

```bash
git tag --sort=-v:refname | head -5
git log vX.Y.Z..HEAD --oneline
```

| Commits desde a última tag | Bump |
|---|---|
| `BREAKING CHANGE` ou `tipo!:` | MAJOR (`2.0.0`) |
| algum `feat:` | MINOR (`1.12.0`) |
| só `fix:` / `perf:` | PATCH (`1.11.2`) |
| só `docs:` / `test:` / `chore:` / `refactor:` | PATCH conservador |

> A release anterior precisa estar com a tag criada. Sem ela, a conta parte de uma
> versão antiga e repete um número que já foi para produção.

---

## 3. Criar a Release

Se a `main` avançou depois que a feature saiu dela, traga a `main` para a feature
antes (evita conflito na linha `<version>` do `pom.xml`):

```bash
git checkout feature/...
git merge --no-ff origin/main -m "chore: Merge main em feature/..."
git push origin feature/...
```

Crie a release a partir da feature e atualize a versão **só no `pom.xml`**
(`app.version` no `application.properties` lê `@project.version@`):

```bash
git checkout -b release/X.Y.Z
# pom.xml: <version>X.Y.Z</version>
git add pom.xml
git commit -m "chore: Atualizando o número de versão para X.Y.Z"
git push -u origin release/X.Y.Z
```

---

## 4. Merge da Release para Homologação

```bash
git checkout homologacao
git pull origin homologacao
git merge --no-ff release/X.Y.Z -m "chore: Merge release/X.Y.Z em homologacao"
git push origin homologacao
```

> Faça o deploy para homologação e aguarde a aprovação.

### Novos commits com a release ainda pendente

Commite na mesma feature e incorpore na release existente (sem nova versão):

```bash
git checkout release/X.Y.Z
git merge --no-ff feature/... -m "chore: Incorporando commits de feature/... em release/X.Y.Z"
git push origin release/X.Y.Z
# repetir o passo 4
```

---

## 5. Aprovado — Tag de Produção e Merge na Main

```bash
git checkout release/X.Y.Z
git tag -a vX.Y.Z -m "Release X.Y.Z"
git push origin vX.Y.Z

git checkout main
git pull origin main
git merge --no-ff release/X.Y.Z -m "chore: Merge release/X.Y.Z em main"
git push origin main
```

> Faça o deploy em produção a partir da `main` (ou da tag `vX.Y.Z`).

---

## 6. Limpeza

Com a tag criada e a release na `main`, as branches `release/X.Y.Z` e `feature/...`
podem ser apagadas (local e remota) — a tag guarda o ponto exato da release.
Confira antes que não há commit fora da `main`:

```bash
git rev-list --count origin/main..origin/release/X.Y.Z   # tem de dar 0
git branch -d release/X.Y.Z feature/...
git push origin --delete release/X.Y.Z feature/...
git fetch --prune origin
```

Se precisar da branch de novo: `git checkout -b release/X.Y.Z vX.Y.Z`.
**Nunca apague as tags.**
