# dscproject — Análise de Sistemas
## Módulo Contas — USER / ADMIN — Manter Conta

**Gerado em:** 08/09/2026
**Versão:** 1.2
**Status:** Desenvolvido
**Projeto:** `dscproject-spring-mvc` (geração 2)

---

## Informações sobre o Documento

| Órgão | dscproject | Setor | Pessoal |
|---|---|---|---|
| Plataforma | dscproject-spring-mvc | Disciplina | Documentação |
| Área Cliente | Diego Cordeiro | Versão do Modelo | 2 (padrão híbrido) |

## Revisões e Aprovações

| Responsável por | Nome | Data de Execução |
|---|---|---|
| Revisão | Diego dos Santos Cordeiro | — |

## Histórico de Versões

| Versão | Data | Analista Responsável | Descrição da Alteração |
|---|---|---|---|
| 1.0 | 08/09/2026 | Diego dos Santos Cordeiro | Criação do documento. CRUD das **contas do usuário** (bancárias, poupança, investimento e carteira) para a geração 2 — sucessor do CRUD de `InstituicaoFinanceiraUsuario` da geração 1, agora sobre a tabela `CONTAS` ([QUADRO_DESCRITIVO_5 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5)). Introduz o enum `TipoConta`, o tipo de conta, moeda, saldo manual com ajuste dedicado, e o escopo *row-level* por `USU_ID`. Este documento **referencia** o QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova** |
| 1.1 | 11/09/2026 | Diego dos Santos Cordeiro | Desmembramento de `CONTAS_MANTER` em `CONTAS_INSERIR`, `CONTAS_EDITAR`, `CONTAS_EXCLUIR`, `CONTAS_DESATIVAR` e `CONTAS_AJUSTAR_SALDO`, em conformidade com as diretrizes de governança RBAC granular e Observação 28 do Documento 0. Reforço de diretriz de máscara monetária client-side em tempo real (`pt-BR`). |
| 1.2 | 12/09/2026 | Diego dos Santos Cordeiro | Padronização visual do sistema: explicitação do alinhamento à esquerda para a coluna de Ações no grid de Contas (QUADRO_DESCRITIVO_1). |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |
| D04 | `CONTAS` é dado do próprio usuário (tem `USU_ID`). O escopo por usuário (*row-level*) é sempre resolvido **no serviço**, a partir do contexto de segurança — nunca de um parâmetro da requisição. |
| D05 | Todo item de menu introduzido por um módulo (nav-link ou dropdown-item da sidebar) carrega um ícone SVG do icon set do projeto, no tamanho padrão (`viewBox="0 0 24 24"`). Nenhum item de menu fica sem ícone. |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Conta** do `dscproject-spring-mvc` — o cadastro e a manutenção das **contas do próprio usuário**: contas correntes, poupanças, contas de investimento e carteiras (dinheiro em espécie).

Na **geração 1** (API REST + SPA Angular), isto é o CRUD de `InstituicaoFinanceiraUsuario` (`dsc-backend`): `GET /instituicoes-financeiras-usuario` devolve **apenas as contas do usuário autenticado** (`buscarTodosPorUsuario()` — resolve o usuário pelo token), com `inserir`, `editar` e `excluir`. A tabela guarda agência, número da conta, nome e telefone do gerente e a instituição. Não há tipo de conta, saldo, moeda nem situação.

O **Documento 0** (Observação 11) travou a decisão de renomear `INSTITUICOES_FINANCEIRAS_USUARIO` para **`CONTAS`** e acrescentar: tipo de conta (enum `TipoConta`), moeda, saldo corrente, carimbo da última sincronização de saldo, situação (ativa/inativa) e a marca "considera no saldo geral". Semanticamente a tabela sempre foi "a conta do usuário numa instituição".

Este documento cobre:

- a **tela Minhas Contas** — listar, cadastrar, editar, ajustar saldo, desativar e excluir (exclusão lógica) as contas do usuário autenticado;
- o **escopo por usuário**: um usuário nunca vê nem altera a conta de outro — toda consulta e todo comando filtram por `USU_ID` no serviço;
- o **contrato de consulta** que as telas de lançamento (Receita, Despesa, Transação Bancária, Cartão de Crédito) usam para carregar as contas ativas do usuário no combobox de conta.

**Escopo deste documento:**
- Tela de **listagem das contas do usuário** (grid client-side), com filtro por modal.
- **Cadastro e edição** de conta via modal único: descrição, tipo, instituição, agência, número, moeda, saldo inicial, dados do gerente e a marca "considera no saldo geral".
- **Ajuste de saldo** por ação dedicada no grid (não edição livre do saldo no modal cadastral).
- **Exclusão lógica** de conta, com a trava "conta em uso" (transações, receitas, despesas, investimentos ou cartão vinculados) — nesse caso, apenas a desativação.
- **Desativação / reativação** de conta (`CTA_FL_ATIVO`).
- **Fornecimento das contas ativas do usuário** para os comboboxes das telas de lançamento.
- Definição das permissões atômicas `CONTAS_LISTAR`, `CONTAS_INSERIR`, `CONTAS_EDITAR`, `CONTAS_EXCLUIR`, `CONTAS_DESATIVAR` e `CONTAS_AJUSTAR_SALDO` e da regra de escopo por usuário.

**Não contempla:**
- CRUD de Receita, Despesa, Transação Bancária, Cartão de Crédito e Investimento, que **consomem** a conta — documentos `07` a `12`.
- CRUD de **Instituição Financeira** (`INSTITUICOES_FINANCEIRAS`) — documento `05 - manter-instituicao-financeira`. Aqui, a instituição é apenas escolhida num combobox alimentado pelo endpoint daquele documento.
- Conexão de contas via Open Finance, consentimento e sincronização de saldo/transações (que preenche `CTA_SALDO_SINCRONIZADO_EM`) — documentos `14` e `15`.
- Cálculo do **saldo geral consolidado** e o efeito de `CTA_FL_CONSIDERA_SALDO` no painel — documento `13 - dashboard`.

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER). A tela é do **próprio usuário** — cada um opera somente sobre as suas contas ([RN02](#rn02)). Dado financeiro é privado do dono: o ADMIN opera como um USER comum, sem qualquer visão administrativa das contas de terceiros.

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **`CONTAS` é dado do próprio usuário, não catálogo administrativo.** A tabela tem `USU_ID` obrigatório ([QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), item 14). Cada usuário cria e gerencia as **suas** contas; a tela vive em "Finanças > Minhas Contas", não em "Administração". | [RN02](#rn02) |
| 2 | **Renomeação da geração 1.** `INSTITUICOES_FINANCEIRAS_USUARIO` → `CONTAS` (Documento 0, Observação 11). `INFU_AGENCIA` → `CTA_AGENCIA`, `INFU_CONTA` → `CTA_NUMERO`, `INFU_NOM_GERENTE` → `CTA_NOME_GERENTE`, `INFU_TEL_GERENTE` → `CTA_TEL_GERENTE`. Novos: `CTA_DESCRICAO`, `CTA_TIPO`, `CTA_MOEDA`, `CTA_SALDO`, `CTA_SALDO_SINCRONIZADO_EM`, `CTA_FL_ATIVO`, `CTA_FL_CONSIDERA_SALDO`. **[Requer código]** | Documento 0, [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) |
| 3 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `CONTAS` filtram por `USU_ID` do usuário autenticado, resolvido do contexto de segurança — nunca de um parâmetro. Espelha o `buscarTodosPorUsuario()` da geração 1. Um endpoint com `{id}` de conta de outro usuário responde como "não encontrada" ([MSG05](#msg05)). | [RN02](#rn02), [RNF01](#rnf01), [RNF02](#rnf02) |
| 4 | **`TipoConta` é enum novo.** Não existe na geração 1. Domínio: `CORRENTE`, `POUPANCA`, `INVESTIMENTO`, `CARTEIRA` (`CARTEIRA` = dinheiro em espécie, sem instituição bancária real — ver Seção 17 sobre a obrigatoriedade da instituição nesse caso). **[Requer código]** | [RN04](#rn04) |
| 5 | **O saldo é manual nesta tela.** `CTA_SALDO` é informado no cadastro (campo "Saldo inicial") e alterado depois por uma **ação dedicada "Ajustar saldo"** ([RT08](#rt08) / [EDP08](#edp08)), não pela edição cadastral. A sincronização via Open Finance, que preenche `CTA_SALDO_SINCRONIZADO_EM`, é dos documentos `14`/`15`. | [RN10](#rn10) |
| 6 | **Instituição imutável após a criação** (decisão da v1.0). O campo Instituição fica desabilitado no modo edição, porque as transações, receitas e despesas já vinculadas à conta carregam a instituição de forma implícita — trocá-la deixaria o histórico incoerente. Ver a decisão em aberto na Seção 17. | [RN06](#rn06) |
| 7 | **Exclusão em uso.** Excluir uma conta referenciada por qualquer transação bancária, receita, despesa, investimento ou cartão de crédito (não excluído) é **bloqueado** ([RN07](#rn07)); a alternativa é **desativar**. Mesma regra do "categoria em uso" do documento `04` (RN06). `TRANSACOES_BANCARIAS.CTA_ID` é `NOT NULL` — uma conta com transação **nunca** é excluível, independentemente do parâmetro [Seção 12](#12-parâmetros-de-sistema). | [RN07](#rn07) |
| 8 | **Desativação ≠ exclusão.** Uma conta inativa (`CTA_FL_ATIVO = FALSE`) some dos comboboxes de **novos** lançamentos ([EDP07](#edp07)), mas continua válida nos lançamentos que já a usam e nos relatórios. Reativar é apenas voltar `CTA_FL_ATIVO = TRUE`. | [RN08](#rn08) |
| 9 | **`CTA_FL_CONSIDERA_SALDO`** controla se o `CTA_SALDO` da conta entra no "saldo geral consolidado" do Dashboard (documento `13`). Default `TRUE`. Aqui só se mantém o flag; o efeito no painel é do documento `13`. | [RN09](#rn09) |
| 10 | **Instituição vem do documento `05`.** O combobox de instituição é alimentado pelo endpoint de opções de `INSTITUICOES_FINANCEIRAS` do documento `05 - manter-instituicao-financeira` — análogo ao [EDP07 do documento `04`](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07). Contrato assumido: `GET /instituicoes-financeiras/opcoes`, devolvendo id, nome, tipo e situação. | [SB02](#sb02) |
| 11 | **Grid client-side.** A tela carrega a lista completa das contas do usuário uma vez e pagina/ordena/filtra no navegador (DataTables). Um usuário tem poucas contas; paginação server-side seria complexidade sem ganho. | [RNF05](#rnf05) |
| 12 | **Auditoria.** `CONTAS` é auditada via Hibernate Envers (`@Audited`), conforme o Documento 0. Criação, edição, ajuste de saldo, desativação e exclusão lógica ficam registrados (quem, quando, o quê). | [RNF03](#rnf03) |
| 13 | **Sem carga inicial.** `CONTAS` nasce vazia. As contas surgem do uso — cadastro manual nesta tela ou *backfill* do Open Finance (documentos `14`/`15`). | Documento 0, Seção 6.4 |
| 14 | **Moeda diferente de BRL.** A v1.0 apenas armazena `CTA_MOEDA` (ISO 4217, default `BRL`). Não há conversão; o tratamento de contas não-BRL no saldo consolidado é decisão do documento `13` (ver Seção 17). | [RN05](#rn05) |
| 15 | **Contas conectadas ao Open Finance** (`CTA_SALDO_SINCRONIZADO_EM` não nulo) aparecem nesta tela, mas a gestão do vínculo e a sincronização são dos documentos `14`/`15`. Se esta tela deve tratar essas contas como somente-leitura é ponto em aberto na Seção 17. | [RN11](#rn11) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as contas do usuário autenticado, com: descrição, tipo, instituição, agência/número, saldo, "considera no saldo geral", número de lançamentos que a usam e situação (ativa/inativa/excluída). | Alta | Em análise |
| <a id="rf02"></a>RF02 | O sistema deve permitir filtrar a listagem por texto (descrição/agência/número), tipo, instituição e situação, por meio de um modal acionado pelo botão "Filtrar". | Média | Em análise |
| <a id="rf03"></a>RF03 | O sistema deve permitir cadastrar uma nova conta via modal, com os campos: descrição, tipo, instituição, agência, número, moeda, saldo inicial, nome e telefone do gerente e "considera no saldo geral". | Alta | Em análise |
| <a id="rf04"></a>RF04 | O sistema deve permitir editar uma conta existente via modal; a instituição e o saldo **não** são alteráveis na edição cadastral. | Alta | Em análise |
| <a id="rf05"></a>RF05 | O sistema deve impedir que o mesmo usuário tenha duas contas com a mesma descrição. | Média | Em análise |
| <a id="rf06"></a>RF06 | O sistema deve impedir a exclusão de uma conta que tenha lançamentos vinculados (transação, receita, despesa, investimento ou cartão), permitindo, nesse caso, apenas a desativação. | Alta | Em análise |
| <a id="rf07"></a>RF07 | O sistema deve permitir ajustar o saldo de uma conta por uma ação dedicada, separada da edição cadastral, registrando o ajuste. | Alta | Em análise |
| <a id="rf08"></a>RF08 | O sistema deve fornecer às telas de lançamento (Receita, Despesa, Transação Bancária, Cartão de Crédito) a lista das contas **ativas** do usuário autenticado, para o combobox de conta. | Alta | Em análise |
| <a id="rf09"></a>RF09 | O sistema deve garantir que cada usuário só liste, consulte, edite, ajuste o saldo e exclua as **próprias** contas. | Alta | Em análise |
| <a id="rf10"></a>RF10 | O sistema deve permitir ativar e desativar uma conta, sem afetar os lançamentos que já a usam. | Alta | Em análise |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada endpoint desta tela exige a autoridade da sua operação (`PERM_CONTAS_LISTAR`, `PERM_CONTAS_INSERIR`, `PERM_CONTAS_EDITAR`, `PERM_CONTAS_EXCLUIR`, `PERM_CONTAS_DESATIVAR`, `PERM_CONTAS_AJUSTAR_SALDO` — ver Seção 13 e [RN01](#rn01)). O `USU_ID` usado no filtro e nas travas vem sempre do contexto de segurança, nunca da requisição. | Teste de acesso com ADMIN, com USER e com um perfil sem nenhuma das permissões. |
| <a id="rnf02"></a>RNF02 | Isolamento | Nenhum endpoint que recebe `{id}` retorna, edita, ajusta o saldo ou exclui a conta de outro usuário — a resposta é "não encontrada" ([MSG05](#msg05)), sem revelar a existência do registro. | Teste chamando `buscar/{id}`, `editar/{id}`, `ajustar-saldo/{id}` e `excluir/{id}` com o id de uma conta de outro usuário. |
| <a id="rnf03"></a>RNF03 | Auditoria | `CONTAS` tem auditoria completa via Hibernate Envers. Criação, edição, ajuste de saldo, desativação e exclusão lógica são registrados. | Inspeção da tabela `CONTAS_aud` após operações de CRUD e de ajuste de saldo. |
| <a id="rnf04"></a>RNF04 | Integridade | A descrição única por usuário ([RN03](#rn03)) e as travas de exclusão ([RN07](#rn07)) são validadas no serviço, não só na tela. | Teste chamando os endpoints diretamente. |
| <a id="rnf05"></a>RNF05 | Desempenho | A listagem ([EDP02](#edp02)) responde em menos de 1 s carregando a lista completa uma vez. O combobox de contas ([EDP07](#edp07)) pode ser cacheado por usuário e invalidado nas gravações desta tela. | Medição em homologação. |
| <a id="rnf06"></a>RNF06 | Usabilidade | A interface segue o padrão do projeto (Thymeleaf + Tabler + DataTables + AJAX) e é responsiva. Os campos monetários (saldo inicial e ajuste de saldo) possuem máscara de moeda client-side em tempo real no padrão `pt-BR` (`R$ 0,00`). | Revisão visual e teste de digitação nos inputs monetários. |

---

## 4. Casos de Uso

![Casos de Uso - Manter Conta](images/manter-conta-casos-uso.png)

Fonte: `prototipo/manter-conta-casos-uso.drawio` (editável) e `prototipo/_diagrama-casos-uso.html` (render). `ADMIN` ([PERF01](#perf01)) e `USER` ([PERF02](#perf02)) têm o mesmo acesso; cada um opera só sobre as próprias contas ([RN02](#rn02)).

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Minhas Contas | [PERF01](#perf01), [PERF02](#perf02) | O usuário acessa o menu e visualiza a lista das suas contas. ([RF01](#rf01), [RF09](#rf09)) |
| <a id="caus02"></a>CAUS02 | Filtrar Contas | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de filtro, informa os critérios e aplica. ([RF02](#rf02)) |
| <a id="caus03"></a>CAUS03 | Cadastrar Conta | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de cadastro, preenche os dados e confirma. ([RF03](#rf03), [RF05](#rf05)) |
| <a id="caus04"></a>CAUS04 | Editar Conta | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de edição, altera os dados e confirma; instituição e saldo ficam bloqueados. ([RF04](#rf04)) |
| <a id="caus05"></a>CAUS05 | Ajustar Saldo da Conta | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre a ação "Ajustar saldo", informa o novo saldo e uma observação e confirma. ([RF07](#rf07)) |
| <a id="caus06"></a>CAUS06 | Excluir ou Desativar Conta | [PERF01](#perf01), [PERF02](#perf02) | O usuário exclui logicamente uma conta sem uso, ou a desativa quando ela tem lançamentos vinculados. ([RF06](#rf06), [RF10](#rf10)) |
| <a id="caus07"></a>CAUS07 | Selecionar Conta num Lançamento | [PERF01](#perf01), [PERF02](#perf02) | O usuário, ao cadastrar uma receita/despesa/transação/cartão, escolhe a conta num combobox alimentado pelas suas contas ativas. ([RF08](#rf08)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Finanças > Minhas Contas
- O item de menu **"Finanças > Minhas Contas"** entra na sidebar **com um ícone SVG do icon set** (Lucide/Tabler) no tamanho padrão (`viewBox="0 0 24 24"`) — sugestão: `wallet` ou `building-bank`. Nenhum item de menu fica sem ícone (diretriz D05).

**Critérios de Aceitação:**
- O menu 'Minhas Contas' é visível apenas para quem tem [PERM01](#perm01).
- Ao acessar a tela, a listagem das contas do usuário autenticado é carregada automaticamente.
- A listagem nunca traz conta de outro usuário.
- O filtro é aplicado por um modal acionado pelo botão "Filtrar".
- O cadastro e a edição de conta são feitos num modal único.
- Na edição, os campos Instituição e Saldo ficam desabilitados; o saldo só muda pela ação "Ajustar saldo".
- Não é possível excluir uma conta com lançamentos vinculados; nesse caso, a tela oferece a desativação.
- Uma conta inativa não aparece nos comboboxes de novos lançamentos, mas os lançamentos que já a usam permanecem inalterados.
- As telas de lançamento recebem apenas as contas ativas do próprio usuário.
- Chamar `buscar/{id}`, `editar/{id}`, `ajustar-saldo/{id}` ou `excluir/{id}` com o id de uma conta de outro usuário responde "não encontrada".

---

## 6. Banco de Dados

Toda a estrutura está no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova**.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | CRUD + ajuste de saldo + desativação, sempre no escopo do `USU_ID` autenticado |
| `INSTITUICOES_FINANCEIRAS` | Documento 0 — [QUADRO_DESCRITIVO_4](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-4) | Somente leitura (combobox de instituição — endpoint do documento `05`) |
| `TRANSACOES_BANCARIAS` | Documento 0 — [QUADRO_DESCRITIVO_7](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7) | Somente leitura (contagem de uso — [C4](#c4)); `CTA_ID` é FK **NOT NULL** |
| `RECEITAS` / `DESPESAS` | Documento 0 — [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), [_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) | Somente leitura (contagem de uso — [C4](#c4)); `CTA_ID` é FK **nullable** |
| `INVESTIMENTOS` | Documento 0 — [QUADRO_DESCRITIVO_12](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-12) | Somente leitura (contagem de uso — [C4](#c4)); `CTA_ID` é FK **nullable** |
| `CARTOES_CREDITO` | Documento 0 — [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6) | Somente leitura (contagem de uso — [C4](#c4)); `CTA_ID` (conta de débito da fatura) é FK **nullable** |
| `USUARIOS` | Documento 0 — [QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2) | Somente leitura (dono da conta — `CONTAS.USU_ID`) |

> Nenhum `ALTER TABLE` neste documento. A tabela `CONTAS`, a FK `USU_ID` e todas as FKs `CTA_ID` nas tabelas de lançamento já existem no Documento 0 ([DDL_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) e correlatos). A descrição única por usuário ([RN03](#rn03)) é validada no serviço; se deve virar índice parcial no banco é ponto da Seção 17.

### 6.1 Diagrama ER

![DER - Manter Conta](images/manter-conta-der.png)

Subconjunto do DER do Documento 0: `CONTAS` ([QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5)), `INSTITUICOES_FINANCEIRAS` ([QUADRO_DESCRITIVO_4](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-4)), `USUARIOS` ([QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2)) e as FKs `CTA_ID` em `TRANSACOES_BANCARIAS` (NOT NULL), `RECEITAS`, `DESPESAS`, `INVESTIMENTOS` e `CARTOES_CREDITO` (nullable). Este documento não cria tabela nova nem faz `ALTER TABLE`. Fonte: `prototipo/manter-conta-der.drawio` (editável) e `prototipo/_diagrama-der.html` (render).

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| CONTAS | CONTAS_aud | Descrição, tipo, instituição, agência, número, moeda, saldo, carimbo de sincronização, dados do gerente, flag de ativo, flag de considera saldo. Registra criação, edição, ajuste de saldo, desativação/reativação e exclusão lógica |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. A validação de escopo por usuário, a checagem de descrição duplicada, a contagem de uso e o ajuste de saldo ficam na camada de serviço.

### 6.4 Carga Inicial

Nenhuma. `CONTAS` nasce vazia (Documento 0, Seção 6.4 — grupo 3, sem *seed*). As contas surgem do cadastro manual nesta tela ou do *backfill* do Open Finance (documentos `14`/`15`).

---

## 7. Protótipos de Interface

Protótipo navegável: `prototipo/manter-conta-prototipo.html`. Wireframes editáveis: `prototipo/manter-conta-prototipo.drawio` (4 páginas, 7.1 a 7.4). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright). Os números em destaque nas telas correspondem aos IDs dos itens do respectivo QUADRO_DESCRITIVO.

### <a id="quadro-descritivo-1"></a>7.1 Tela: Minhas Contas (Listagem) — QUADRO_DESCRITIVO_1

![Minhas Contas - Listagem](images/mc-tela-1.png)

> OBSERVAÇÕES: Tela acessada via 'Finanças > Minhas Contas'. Restrita a quem tem [PERM01](#perm01). Grid client-side, carregado apenas com as contas do usuário autenticado ([EDP02](#edp02) → [C1](#c1)). O filtro é acionado por um modal (botão "Filtrar").

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/contas/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Finanças > Minhas Contas | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Minhas Contas | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Cadastre e gerencie as suas contas bancárias, carteiras e contas de investimento. | — |
| <a id="qdd1-4"></a>4 | BOTÃO FILTRAR | Tipo: Botão<br>Texto: Filtrar<br>Ícone: filter | Ao clicar, executar [RT01](#rt01). |
| <a id="qdd1-5"></a>5 | BOTÃO NOVA CONTA | Tipo: Botão (primário)<br>Texto: Nova conta<br>Ícone: plus | Visível a quem tem [PERM02](#perm02). Ao clicar, executar [RT04](#rt04). |
| <a id="qdd1-6"></a>6 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side)<br>Colunas: [ID7](#qdd1-7)…[ID15](#qdd1-15)<br>Itens por página: 10, 25, 50<br>Ordenação padrão: Descrição crescente<br>Endpoint: [EDP02](#edp02) | Carrega a lista completa das contas do usuário uma vez. Filtra em memória conforme [RT02](#rt02). |
| <a id="qdd1-7"></a>7 | DESCRIÇÃO | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).descricao. |
| <a id="qdd1-8"></a>8 | TIPO | Tipo: Coluna (badge)<br>Ordenação: Sim | Corrente / Poupança / Investimento / Carteira, de [C1](#c1).tipo. |
| <a id="qdd1-9"></a>9 | INSTITUIÇÃO | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).instituicaoNome. |
| <a id="qdd1-10"></a>10 | AGÊNCIA / NÚMERO | Tipo: Coluna<br>Ordenação: Não | Exibe [C1](#c1).agencia e [C1](#c1).numero concatenados ("Ag. 0001 / C/C 12345-6"); vazio quando ambos nulos. |
| <a id="qdd1-11"></a>11 | SALDO | Tipo: Coluna (moeda, alinhada à direita)<br>Ordenação: Sim | Exibe [C1](#c1).saldo formatado com [C1](#c1).moeda. Valor negativo em vermelho. |
| <a id="qdd1-12"></a>12 | CONSIDERA NO SALDO GERAL | Tipo: Coluna (badge)<br>Ordenação: Sim | "Sim" / "Não", de [C1](#c1).consideraSaldo. |
| <a id="qdd1-13"></a>13 | Nº DE LANÇAMENTOS | Tipo: Coluna (número)<br>Ordenação: Sim | Exibe [C1](#c1).qtdUso — soma de transações, receitas, despesas, investimentos e cartões não excluídos que usam a conta. |
| <a id="qdd1-14"></a>14 | SITUAÇÃO | Tipo: Coluna (badge)<br>Ordenação: Sim | "Ativa" (verde) quando `CTA_FL_ATIVO` e sem `audit_data_exclusao`; "Inativa" (cinza) quando `CTA_FL_ATIVO = FALSE`; "Excluída" quando há `audit_data_exclusao`. |
| <a id="qdd1-15"></a>15 | AÇÃO | Tipo: Coluna (alinhada à esquerda) | Visível a quem tem [PERM03](#perm03), [PERM04](#perm04) ou [PERM06](#perm06). Ícones [ID16](#qdd1-16). |
| <a id="qdd1-16"></a>16 | ÍCONES DE AÇÃO | Tipo: Ícones<br>Editar (ícone: edit, tooltip: Editar conta)<br>Ajustar saldo (ícone: adjustments-dollar, tooltip: Ajustar saldo)<br>Excluir (ícone: trash, tooltip: Excluir conta) | Editar (com [PERM03](#perm03)) → [RT05](#rt05). Ajustar saldo (com [PERM06](#perm06)) → [RT08](#rt08). Excluir (com [PERM04](#perm04)) → [RT07](#rt07); ambos ocultos quando a conta já está excluída. |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Filtrar Contas — QUADRO_DESCRITIVO_2

![Modal Filtrar Contas](images/mc-tela-2.png)

> OBSERVAÇÕES: Todos os campos são opcionais. O filtro é aplicado em memória sobre a lista já carregada ([RT02](#rt02)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Filtrar Contas | — |
| <a id="qdd2-2"></a>2 | FILTRO – BUSCA | Tipo: Input Text<br>Obrigatório: Não<br>Placeholder: Descrição, agência ou número<br>Tooltip: Filtre por parte da descrição, da agência ou do número. | Filtro parcial e sem acento sobre descrição, agência e número. |
| <a id="qdd2-3"></a>3 | FILTRO – TIPO | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todos<br>Domínio: Todos / Corrente / Poupança / Investimento / Carteira | Filtra por [C1](#c1).tipo. Ver [SB04](#sb04). |
| <a id="qdd2-4"></a>4 | FILTRO – INSTITUIÇÃO | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todas as instituições<br>Domínio: "Todas" + instituições | Filtra por [C1](#c1).instituicaoId. Ver [SB05](#sb05). |
| <a id="qdd2-5"></a>5 | FILTRO – SITUAÇÃO | Tipo: Combobox<br>Obrigatório: Não<br>Valor default: Ativa<br>Domínio: Ativa / Inativa / Excluída / Todas | Filtra por `CTA_FL_ATIVO` e pela presença de `audit_data_exclusao`. Ver [SB06](#sb06). |
| <a id="qdd2-6"></a>6 | BOTÃO APLICAR | Tipo: Botão<br>Texto: Aplicar | Ao clicar, executar [RT02](#rt02). |
| <a id="qdd2-7"></a>7 | BOTÃO LIMPAR | Tipo: Botão<br>Texto: Limpar | Ao clicar, executar [RT03](#rt03). |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Cadastro / Edição de Conta — QUADRO_DESCRITIVO_3

![Modal Cadastro / Edição de Conta](images/mc-tela-3.png)

A imagem mostra o **modo edição** de uma conta em uso — com o toggle Ativa ([ID12](#qdd3-12)) e o aviso de conta em uso ([ID13](#qdd3-13)) visíveis. No modo criação, o campo Saldo inicial ([ID8](#qdd3-8)) aparece no lugar desses dois.

> OBSERVAÇÕES: Modal único de cadastro (exige [PERM02](#perm02)) e edição (exige [PERM03](#perm03) e [PERM05](#perm05)). No modo edição, os campos Instituição ([RN06](#rn06)) e Saldo inicial ([RN10](#rn10)) ficam desabilitados, e aparecem os campos Ativa ([ID12](#qdd3-12)) e o aviso de conta em uso ([ID13](#qdd3-13)). O saldo só é alterado pela ação "Ajustar saldo" ([QUADRO_DESCRITIVO_4](#quadro-descritivo-4)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Nova conta / Editar conta | Varia conforme o modo. |
| <a id="qdd3-2"></a>2 | CAMPO – DESCRIÇÃO | Tipo: Input Text<br>Tamanho: 100<br>Obrigatório: Sim | Grava `CTA_DESCRICAO`. Única entre as contas não excluídas do usuário ([RN03](#rn03)). Ex.: "NUBANK CONTA CORRENTE". |
| <a id="qdd3-3"></a>3 | CAMPO – TIPO DE CONTA | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: Corrente / Poupança / Investimento / Carteira | Grava `CTA_TIPO`. Ver [SB01](#sb01) e [RN04](#rn04). |
| <a id="qdd3-4"></a>4 | CAMPO – INSTITUIÇÃO | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: instituições ativas | Grava `INFI_ID`. Ver [SB02](#sb02). **Desabilitado** no modo edição ([RN06](#rn06)). |
| <a id="qdd3-5"></a>5 | CAMPO – AGÊNCIA | Tipo: Input Text<br>Tamanho: 30<br>Obrigatório: Não | Grava `CTA_AGENCIA`. |
| <a id="qdd3-6"></a>6 | CAMPO – NÚMERO | Tipo: Input Text<br>Tamanho: 30<br>Obrigatório: Não | Grava `CTA_NUMERO`. |
| <a id="qdd3-7"></a>7 | CAMPO – MOEDA | Tipo: Combobox<br>Obrigatório: Sim<br>Valor default: BRL | Grava `CTA_MOEDA` (ISO 4217). Ver [SB03](#sb03) e [RN05](#rn05). |
| <a id="qdd3-8"></a>8 | CAMPO – SALDO INICIAL | Tipo: Input monetário<br>Obrigatório: Sim<br>Valor default: 0,00<br>Exibição: só no modo criação | Grava `CTA_SALDO` na criação. Máscara monetária em tempo real `pt-BR` (`R$ 0,00`). No modo edição não aparece — usar "Ajustar saldo" ([RT08](#rt08)). Ver [RN10](#rn10). |
| <a id="qdd3-9"></a>9 | CAMPO – NOME DO GERENTE | Tipo: Input Text<br>Tamanho: 100<br>Obrigatório: Não | Grava `CTA_NOME_GERENTE`. |
| <a id="qdd3-10"></a>10 | CAMPO – TELEFONE DO GERENTE | Tipo: Input Text<br>Tamanho: 20<br>Obrigatório: Não | Grava `CTA_TEL_GERENTE`. |
| <a id="qdd3-11"></a>11 | CAMPO – CONSIDERA NO SALDO GERAL | Tipo: Toggle (Sim/Não)<br>Valor default: Sim | Grava `CTA_FL_CONSIDERA_SALDO`. Ver [RN09](#rn09). |
| <a id="qdd3-12"></a>12 | CAMPO – ATIVA | Tipo: Toggle (Sim/Não)<br>Valor default: Sim<br>Exibição: só no modo edição | Grava `CTA_FL_ATIVO`. Exige [PERM05](#perm05) para alteração. Ver [RN08](#rn08). |
| <a id="qdd3-13"></a>13 | AVISO – CONTA EM USO | Tipo: Texto informativo | Exibido no modo edição quando [C1](#c1).qtdUso > 0: "Esta conta é usada por {n} lançamento(s). Ela não pode ser excluída; você pode desativá-la." |
| <a id="qdd3-14"></a>14 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar<br>Endpoint: [EDP04](#edp04) (criação) ou [EDP05](#edp05) (edição) | Ao clicar, executar [RT06](#rt06). |
| <a id="qdd3-15"></a>15 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-4"></a>7.4 Modal: Ajustar Saldo — QUADRO_DESCRITIVO_4

![Modal Ajustar Saldo](images/mc-tela-4.png)

> OBSERVAÇÕES: Acionado pelo ícone "Ajustar saldo" do grid ([ID16](#qdd1-16)), restrito a [PERM06](#perm06). Substitui o `CTA_SALDO` da conta pelo valor informado; o Hibernate Envers registra o antes e o depois. Não altera `CTA_SALDO_SINCRONIZADO_EM` (o saldo permanece manual).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Ajustar saldo — {descrição da conta} | — |
| <a id="qdd4-2"></a>2 | SALDO ATUAL | Tipo: Texto (somente leitura) | Exibe [C1](#c1).saldo formatado com a moeda da conta. |
| <a id="qdd4-3"></a>3 | CAMPO – NOVO SALDO | Tipo: Input monetário<br>Obrigatório: Sim | Grava `CTA_SALDO`. Aceita valor negativo. Máscara monetária em tempo real `pt-BR` (`R$ 0,00`). |
| <a id="qdd4-4"></a>4 | CAMPO – OBSERVAÇÃO | Tipo: Textarea<br>Tamanho: 255<br>Obrigatório: Não | Nota livre do ajuste, gravada no comentário da revisão do Envers (ver Seção 17 sobre histórico dedicado). |
| <a id="qdd4-5"></a>5 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar ajuste<br>Endpoint: [EDP08](#edp08) | Ao clicar, executar [RT09](#rt09). |
| <a id="qdd4-6"></a>6 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### 7.5 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | TIPO DE CONTA | Domínio fixo do enum `TipoConta` (Corrente, Poupança, Investimento, Carteira). Renderizado como combobox; não é entidade. |
| <a id="sb02"></a>SB02 | INSTITUIÇÃO | Itens carregados de `INSTITUICOES_FINANCEIRAS` (Documento 0, [QUADRO_DESCRITIVO_4](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-4)) via o endpoint de opções do documento `05 - manter-instituicao-financeira` (`GET /instituicoes-financeiras/opcoes`), ordenados por nome. No modal de conta, só instituições ativas. No filtro do grid ([ID4](#qdd2-4)), a opção "Todas as instituições" é adicional. Nunca `<option>` fixo no HTML. |
| <a id="sb03"></a>SB03 | MOEDA | Lista curada de moedas ISO 4217 mantida no front (BRL, USD, EUR, GBP, …), com BRL como default. Não é entidade. Conjunto aceito a confirmar (Seção 17). |
| <a id="sb04"></a>SB04 | FILTRO TIPO | Domínio fixo do próprio filtro: Todos, Corrente, Poupança, Investimento, Carteira. |
| <a id="sb05"></a>SB05 | FILTRO INSTITUIÇÃO | Como [SB02](#sb02), com a opção "Todas" adicional e sem o filtro de ativa (o grid pode conter contas de instituições hoje inativas). |
| <a id="sb06"></a>SB06 | FILTRO SITUAÇÃO | Domínio fixo do próprio filtro: Ativa, Inativa, Excluída, Todas. |

### 7.6 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao clicar em "Filtrar" ([ID4](#qdd1-4)), abrir o modal de filtro ([QUADRO_DESCRITIVO_2](#quadro-descritivo-2)) com os valores atualmente aplicados. |
| <a id="rt02"></a>RT02 | Ao clicar em "Aplicar" ([ID6](#qdd2-6)), filtrar **em memória** a lista já carregada: busca parcial e sem acento sobre descrição/agência/número, e correspondência exata de tipo, instituição e situação. Fechar o modal. Se nada restar, exibir [MSG09](#msg09) na área do grid. |
| <a id="rt03"></a>RT03 | Ao clicar em "Limpar" ([ID7](#qdd2-7)), voltar Busca, Tipo e Instituição para vazio, Situação para "Ativa", e reaplicar conforme [RT02](#rt02). |
| <a id="rt04"></a>RT04 | Ao clicar em "Nova conta" ([ID5](#qdd1-5)) — visível só com [PERM02](#perm02) —, abrir o modal ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)) em modo criação: campos vazios, Moeda em "BRL", Considera no saldo geral em "Sim", Saldo inicial "0,00", sem os campos Ativa e aviso de uso. Carregar o combobox Instituição conforme [RT10](#rt10). |
| <a id="rt05"></a>RT05 | Ao clicar no ícone Editar ([ID16](#qdd1-16)) — visível só com [PERM03](#perm03) —, chamar [EDP03](#edp03) com o id e abrir o modal em modo edição, com os campos preenchidos. Instituição e Saldo inicial ficam desabilitados ([RN06](#rn06), [RN10](#rn10)). Se [C1](#c1).qtdUso > 0, exibir o aviso [ID13](#qdd3-13). |
| <a id="rt06"></a>RT06 | Ao clicar em "Salvar" ([ID14](#qdd3-14)): validar Descrição, Tipo, Instituição e Moeda obrigatórios ([MSG02](#msg02)); na criação, validar Saldo inicial preenchido. Em criação (exige [PERM02](#perm02)), chamar [EDP04](#edp04); em edição (exige [PERM03](#perm03)), chamar [EDP05](#edp05). Em sucesso, exibir [MSG01](#msg01) (criação) ou [MSG04](#msg04) (edição), fechar o modal e recarregar o grid via [EDP02](#edp02). Descrição já usada pelo usuário → [MSG03](#msg03). Tentativa de trocar a instituição na edição ([RN06](#rn06)) → [MSG11](#msg11). |
| <a id="rt07"></a>RT07 | Ao clicar no ícone Excluir ([ID16](#qdd1-16)) — visível só com [PERM04](#perm04) —, exibir a confirmação [MSG07](#msg07). Ao confirmar, chamar [EDP06](#edp06). Conta em uso → [MSG06](#msg06), com a oferta de desativar (ao aceitar com [PERM05](#perm05), chamar [EDP05](#edp05) apenas com `CTA_FL_ATIVO = FALSE`). Em sucesso da exclusão, exibir [MSG08](#msg08) e recarregar o grid. |
| <a id="rt08"></a>RT08 | Ao clicar no ícone Ajustar saldo ([ID16](#qdd1-16)) — visível só com [PERM06](#perm06) —, abrir o modal ([QUADRO_DESCRITIVO_4](#quadro-descritivo-4)) com o Saldo atual ([ID2](#qdd4-2)) preenchido de [C1](#c1).saldo e o campo Novo saldo iniciado com esse mesmo valor. |
| <a id="rt09"></a>RT09 | Ao clicar em "Salvar ajuste" ([ID5](#qdd4-5)) — visível só com [PERM06](#perm06) —: validar Novo saldo preenchido ([MSG02](#msg02)) e chamar [EDP08](#edp08) com o novo valor e a observação. Em sucesso, exibir [MSG10](#msg10), fechar o modal e recarregar o grid via [EDP02](#edp02). |
| <a id="rt10"></a>RT10 | Ao abrir o modal de conta ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)), carregar o combobox Instituição pelo endpoint de opções do documento `05` ([SB02](#sb02)) e o combobox Moeda pela lista curada do front ([SB03](#sb03)). Nunca renderizar `<option>` fixo no HTML. |
| <a id="rt11"></a>RT11 | Aplicar máscara monetária client-side em tempo real (`pt-BR`, `R$ 0,00`) aos campos de saldo ([ID8](#qdd3-8), [ID3](#qdd4-3)) conforme a moeda selecionada, aceitando valores negativos, e formatar a coluna Saldo do grid ([ID11](#qdd1-11)) da mesma forma, com valores negativos em vermelho. |

---

## 8. Endpoints

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) | /contas/listar | N |
| Retorna a página da listagem de contas (Thymeleaf). O grid é carregado por [EDP02](#edp02). | | | | |
| <a id="edp02"></a>EDP02 | GET | [PERM01](#perm01) | /contas/listar-dados | N |
| Lista das contas do usuário autenticado para o grid, em JSON. Executa [C1](#c1) com `USU_ID` do contexto de segurança ([RN02](#rn02)). Campos: id, descricao, tipo, instituicaoId, instituicaoNome, agencia, numero, moeda, saldo, saldoSincronizadoEm, consideraSaldo (boolean), qtdUso, ativo (boolean), excluido (boolean). Sem paginação (client-side). | | | | |
| <a id="edp03"></a>EDP03 | GET | [PERM03](#perm03) | /contas/buscar/{id} | N |
| Retorna uma conta do usuário autenticado para edição. Executa [RN02](#rn02) — se a conta não for do usuário, responde 404 ([MSG05](#msg05)). Campos: id, descricao, tipo, instituicaoId, agencia, numero, moeda, saldo, consideraSaldo, ativo, qtdUso. | | | | |
| <a id="edp04"></a>EDP04 | POST | [PERM02](#perm02) | /contas/inserir | N |
| Cria uma conta para o usuário autenticado. Executa [RN02](#rn02) (fixa `USU_ID` do contexto), [RN03](#rn03) (descrição única por usuário, via [C2](#c2)), [RN04](#rn04), [RN05](#rn05), [RN06](#rn06) (instituição ativa). Dados: descricao, tipo, instituicaoId, agencia, numero, moeda, saldoInicial, nomeGerente, telefoneGerente, consideraSaldo. `CTA_FL_ATIVO = TRUE` e `CTA_SALDO_SINCRONIZADO_EM = NULL` fixos; `CTA_SALDO` recebe `saldoInicial`. Invalida o cache do combobox de contas ([RNF05](#rnf05)). Retorno: 200 ([MSG01](#msg01)) ou 422 ([MSG02](#msg02)/[MSG03](#msg03)). | | | | |
| <a id="edp05"></a>EDP05 | PUT | [PERM03](#perm03) / [PERM05](#perm05) | /contas/editar/{id} | N |
| Edita uma conta do usuário autenticado. Executa [RN02](#rn02) (404 se não for do usuário), [RN03](#rn03), [RN06](#rn06) (ignora qualquer mudança de `INFI_ID`; se a intenção explícita for trocar a instituição → [MSG11](#msg11)), [RN08](#rn08) (desativação), [RN10](#rn10) (ignora qualquer `CTA_SALDO` no corpo). Dados: descricao, tipo, agencia, numero, moeda, nomeGerente, telefoneGerente, consideraSaldo, ativo. Exige [PERM03](#perm03) para dados cadastrais e [PERM05](#perm05) para alteração de situação (ativo). Invalida o cache do combobox de contas. Retorno: 200 ([MSG04](#msg04)) ou 422 ([MSG02](#msg02)/[MSG03](#msg03)/[MSG11](#msg11)). | | | | |
| <a id="edp06"></a>EDP06 | DELETE | [PERM04](#perm04) | /contas/excluir/{id} | N |
| Exclusão lógica da conta do usuário autenticado. Executa [RN02](#rn02) (404 se não for do usuário) e [RN07](#rn07) (recusa se em uso, via [C4](#c4) → [MSG06](#msg06)). Preenche `audit_data_exclusao` / `audit_excluido_por`. Invalida o cache do combobox de contas. Retorno: 200 ([MSG08](#msg08)) ou 422. | | | | |
| <a id="edp07"></a>EDP07 | GET | [PERM01](#perm01) | /contas/opcoes | N |
| Retorna as contas **ativas** do usuário autenticado para o combobox das telas de lançamento. Executa [C3](#c3). Campos: id, descricao, tipo, moeda, instituicaoNome. Consumido pelas telas de Receita, Despesa, Transação Bancária e Cartão de Crédito. Pode ser cacheado por usuário e é invalidado por [EDP04](#edp04)/[EDP05](#edp05)/[EDP06](#edp06)/[EDP08](#edp08). | | | | |
| <a id="edp08"></a>EDP08 | PUT | [PERM06](#perm06) | /contas/ajustar-saldo/{id} | N |
| Ajusta o saldo de uma conta do usuário autenticado. Executa [RN02](#rn02) (404 se não for do usuário) e [RN10](#rn10). Dados: novoSaldo, observacao. Grava `CTA_SALDO = novoSaldo`; **não** altera `CTA_SALDO_SINCRONIZADO_EM`. O Hibernate Envers registra a revisão (saldo anterior e novo); `observacao` vai no comentário da revisão. Invalida o cache do combobox de contas. Retorno: 200 ([MSG10](#msg10)) ou 422 ([MSG02](#msg02)). | | | | |

> O combobox de instituição do modal de conta e do filtro é alimentado pelo endpoint de opções de `INSTITUICOES_FINANCEIRAS` definido no documento `05 - manter-instituicao-financeira` (contrato assumido: `GET /instituicoes-financeiras/opcoes`) — este documento não define esse endpoint.

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | Cada endpoint exige a autoridade da sua operação: [EDP01](#edp01)/[EDP02](#edp02)/[EDP07](#edp07) → `PERM_CONTAS_LISTAR`; [EDP04](#edp04) → `PERM_CONTAS_INSERIR`; [EDP03](#edp03)/[EDP05](#edp05) → `PERM_CONTAS_EDITAR` (a alteração da situação ativa/inativa em [EDP05](#edp05) exige `PERM_CONTAS_DESATIVAR`); [EDP06](#edp06) → `PERM_CONTAS_EXCLUIR`; [EDP08](#edp08) → `PERM_CONTAS_AJUSTAR_SALDO`. As autoridades são resolvidas pelo `getAuthorities()` do `Usuario` a partir do perfil e das permissões vinculadas em `PERFIL_PERMISSAO`. É expressamente proibida a criação de permissões genéricas com sufixo `MANTER`. A permissão **habilita a tela**; o recorte por dono é a [RN02](#rn02). |
| <a id="rn02"></a>RN02 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `CONTAS` são restritos às contas cujo `USU_ID` é o do usuário autenticado, obtido do contexto de segurança — nunca de um parâmetro da requisição. Na criação ([EDP04](#edp04)), o serviço **fixa** `USU_ID` do contexto e ignora qualquer valor recebido. Em [EDP03](#edp03), [EDP05](#edp05), [EDP06](#edp06) e [EDP08](#edp08), se a conta do `{id}` não pertencer ao usuário autenticado, o serviço responde **404** com [MSG05](#msg05), sem distinguir "não existe" de "é de outro usuário". Espelha o `buscarTodosPorUsuario()` da geração 1. |
| <a id="rn03"></a>RN03 | `CTA_DESCRICAO` é obrigatória e única entre as contas **não excluídas do mesmo usuário** (comparação sem diferenciar maiúsculas/minúsculas). Ao criar ([EDP04](#edp04)) ou editar ([EDP05](#edp05)), se a descrição já pertencer a **outra** conta do usuário, impedir e retornar [MSG03](#msg03). Executa [C2](#c2). Contas de usuários diferentes podem ter a mesma descrição. |
| <a id="rn04"></a>RN04 | `CTA_TIPO` é obrigatório e deve ser `CORRENTE`, `POUPANCA`, `INVESTIMENTO` ou `CARTEIRA` (enum `TipoConta`). |
| <a id="rn05"></a>RN05 | `CTA_MOEDA` é obrigatória, `CHAR(3)` no padrão ISO 4217, default `BRL`. A v1.0 apenas armazena a moeda; não há conversão de valores. O tratamento de contas não-BRL no saldo consolidado é do documento `13` (ver Seção 17). |
| <a id="rn06"></a>RN06 | `INFI_ID` é obrigatória. Na criação ([EDP04](#edp04)), a instituição deve estar ativa (`INFI_FL_ATIVO = TRUE`) — instituição inativa ou inexistente → [MSG02](#msg02) no campo Instituição. Na edição ([EDP05](#edp05)), `INFI_ID` **não** é alterável: qualquer valor divergente enviado é ignorado; se a intenção explícita for trocar a instituição, retornar [MSG11](#msg11). Ver a decisão em aberto na Seção 17. |
| <a id="rn07"></a>RN07 | Exclusão de conta ([EDP06](#edp06)): recusar se existir **qualquer** lançamento não excluído apontando para a conta — transação bancária (`TRANSACOES_BANCARIAS.CTA_ID`), receita (`RECEITAS.CTA_ID`), despesa (`DESPESAS.CTA_ID`), investimento (`INVESTIMENTOS.CTA_ID`) ou cartão de crédito (`CARTOES_CREDITO.CTA_ID`, conta de débito da fatura) — executa [C4](#c4) — e retornar [MSG06](#msg06). A alternativa oferecida é a desativação ([RN08](#rn08)). O comportamento das FKs **nullable** é controlado pelo parâmetro `CONTA_EXCLUSAO_BLOQUEIA_EM_USO` ([Seção 12](#12-parâmetros-de-sistema)): quando `false`, a exclusão é permitida e os lançamentos com FK nullable ficam com `CTA_ID` nulo. Como `TRANSACOES_BANCARIAS.CTA_ID` é `NOT NULL`, uma conta com transação **nunca** é excluível, independentemente do parâmetro. Ver a decisão em aberto na Seção 17. |
| <a id="rn08"></a>RN08 | Conta inativa (`CTA_FL_ATIVO = FALSE`): não é devolvida por [EDP07](#edp07) e não pode ser escolhida em novos lançamentos. Os lançamentos que já a referenciam permanecem inalterados, e ela continua contando nos relatórios. Reativar é apenas voltar `CTA_FL_ATIVO = TRUE` por [EDP05](#edp05). |
| <a id="rn09"></a>RN09 | `CTA_FL_CONSIDERA_SALDO` (default `TRUE`) indica se o `CTA_SALDO` da conta entra no "saldo geral consolidado" do Dashboard. Esta tela apenas mantém o flag; o cálculo do saldo consolidado e o efeito exato do flag são do documento `13`. |
| <a id="rn10"></a>RN10 | O `CTA_SALDO` é definido no cadastro ([EDP04](#edp04), campo "Saldo inicial", default `0,00`). A edição cadastral ([EDP05](#edp05)) **ignora** qualquer `CTA_SALDO` no corpo. A alteração posterior do saldo é feita só por [EDP08](#edp08) (ação "Ajustar saldo"), que grava o novo valor e deixa o registro no Hibernate Envers, sem tocar em `CTA_SALDO_SINCRONIZADO_EM`. Aceita valor negativo (conta no cheque especial, fatura em aberto etc.). |
| <a id="rn11"></a>RN11 | Contas com `CTA_SALDO_SINCRONIZADO_EM` não nulo têm o saldo mantido pela sincronização de Open Finance (documentos `14`/`15`). A v1.0 permite o ajuste manual e a edição cadastral dessas contas nesta tela; se a gestão deve ser exclusiva dos documentos `14`/`15` (tela somente-leitura aqui) é ponto em aberto na Seção 17. |
| <a id="rn12"></a>RN12 | `TipoConta` é um enum novo (não existe na geração 1). O tipo `CARTEIRA` representa dinheiro em espécie; a obrigatoriedade de `INFI_ID` também para esse tipo segue o Documento 0 ([QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), item 13) — ver Seção 17. **[Requer código]** |
| <a id="rn13"></a>RN13 | `CONTAS` é auditada via Hibernate Envers (`@Audited`). Cada criação, edição, ajuste de saldo, desativação/reativação e exclusão lógica gera uma revisão com autor e data. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Conta cadastrada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Você já tem uma conta com esta descrição. |
| <a id="msg04"></a>MSG04 | Conta atualizada com sucesso. |
| <a id="msg05"></a>MSG05 | Conta não encontrada. |
| <a id="msg06"></a>MSG06 | Esta conta tem lançamentos vinculados e não pode ser excluída. Desative-a para ocultá-la de novos lançamentos. |
| <a id="msg07"></a>MSG07 | Confirma a exclusão da conta "{descricao}"? |
| <a id="msg08"></a>MSG08 | Conta excluída com sucesso. |
| <a id="msg09"></a>MSG09 | Nenhuma conta encontrada com os filtros informados. |
| <a id="msg10"></a>MSG10 | Saldo ajustado com sucesso. |
| <a id="msg11"></a>MSG11 | A instituição não pode ser alterada depois que a conta é criada. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem das contas do usuário autenticado para o grid, com a instituição e a contagem de uso ([EDP02](#edp02)).<br>`SELECT c.CTA_ID, c.CTA_DESCRICAO, c.CTA_TIPO, c.CTA_AGENCIA, c.CTA_NUMERO,`<br>`       c.CTA_MOEDA, c.CTA_SALDO, c.CTA_SALDO_SINCRONIZADO_EM,`<br>`       c.CTA_FL_ATIVO, c.CTA_FL_CONSIDERA_SALDO,`<br>`       i.INFI_ID, i.INFI_NOME,`<br>`       (c.audit_data_exclusao IS NOT NULL) AS excluido,`<br>`       ( (SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t WHERE t.CTA_ID = c.CTA_ID AND t.audit_data_exclusao IS NULL)`<br>`       + (SELECT COUNT(*) FROM RECEITAS r WHERE r.CTA_ID = c.CTA_ID AND r.audit_data_exclusao IS NULL)`<br>`       + (SELECT COUNT(*) FROM DESPESAS d WHERE d.CTA_ID = c.CTA_ID AND d.audit_data_exclusao IS NULL)`<br>`       + (SELECT COUNT(*) FROM INVESTIMENTOS v WHERE v.CTA_ID = c.CTA_ID AND v.audit_data_exclusao IS NULL)`<br>`       + (SELECT COUNT(*) FROM CARTOES_CREDITO k WHERE k.CTA_ID = c.CTA_ID AND k.audit_data_exclusao IS NULL) ) AS qtd_uso`<br>`FROM CONTAS c`<br>`JOIN INSTITUICOES_FINANCEIRAS i ON i.INFI_ID = c.INFI_ID`<br>`WHERE c.USU_ID = :usuId`<br>`ORDER BY c.CTA_DESCRICAO ASC;` |
| <a id="c2"></a>C2 | Verifica descrição de conta duplicada para o usuário ([RN03](#rn03)).<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.audit_data_exclusao IS NULL`<br>`  AND c.USU_ID = :usuId`<br>`  AND UPPER(c.CTA_DESCRICAO) = UPPER(:descricao)`<br>`  AND (:idAtual IS NULL OR c.CTA_ID <> :idAtual);` |
| <a id="c3"></a>C3 | Contas ativas do usuário para o combobox das telas de lançamento ([EDP07](#edp07)).<br>`SELECT c.CTA_ID, c.CTA_DESCRICAO, c.CTA_TIPO, c.CTA_MOEDA, i.INFI_NOME`<br>`FROM CONTAS c`<br>`JOIN INSTITUICOES_FINANCEIRAS i ON i.INFI_ID = c.INFI_ID`<br>`WHERE c.audit_data_exclusao IS NULL`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`ORDER BY c.CTA_DESCRICAO ASC;` |
| <a id="c4"></a>C4 | Conta os lançamentos não excluídos que usam a conta ([RN07](#rn07)).<br>`SELECT`<br>`   (SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t WHERE t.CTA_ID = :ctaId AND t.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM RECEITAS r WHERE r.CTA_ID = :ctaId AND r.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM DESPESAS d WHERE d.CTA_ID = :ctaId AND d.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM INVESTIMENTOS v WHERE v.CTA_ID = :ctaId AND v.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM CARTOES_CREDITO k WHERE k.CTA_ID = :ctaId AND k.audit_data_exclusao IS NULL) AS qtd_uso;` |
| <a id="c5"></a>C5 | Verifica se a conta pertence ao usuário autenticado ([RN02](#rn02)) — usada antes de editar, ajustar saldo ou excluir.<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.CTA_ID = :ctaId`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.audit_data_exclusao IS NULL;` |

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| CONTA_EXCLUSAO_BLOQUEIA_EM_USO | true | Se `true`, [RN07](#rn07) impede excluir uma conta referenciada por lançamentos (só desativar). Se `false`, a exclusão é permitida e os lançamentos com FK **nullable** ficam com `CTA_ID` nulo. Não afeta `TRANSACOES_BANCARIAS` (FK `NOT NULL`), que sempre bloqueia. |
| CONTA_MOEDA_PADRAO | BRL | Moeda pré-selecionada no cadastro de nova conta ([ID7](#qdd3-7)). |
| CONTA_COMBOBOX_CACHE | true | Se `true`, a lista de [EDP07](#edp07) é cacheada por usuário e invalidada nas gravações de [EDP04](#edp04)/[EDP05](#edp05)/[EDP06](#edp06)/[EDP08](#edp08). |

---

## 13. Permissões

Seis permissões atômicas do módulo **Contas** (`PERM_MODULO = 'Contas'`). Fazem parte do catálogo do código e da carga inicial (Documento 0, [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26)). Convenção domínio-primeiro; cada uma vira a autoridade `PERM_{CÓDIGO}`.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `CONTAS_LISTAR` — abrir a tela Minhas Contas, listar e filtrar as próprias contas, e obter a lista de contas ativas para os comboboxes de lançamento. Controla a visibilidade do menu 'Minhas Contas'. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm02"></a>PERM02 | `CONTAS_INSERIR` — cadastrar nova conta do próprio usuário. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm03"></a>PERM03 | `CONTAS_EDITAR` — editar dados cadastrais das próprias contas. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm04"></a>PERM04 | `CONTAS_EXCLUIR` — realizar exclusão lógica de conta própria sem movimentações impeditivas. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm05"></a>PERM05 | `CONTAS_DESATIVAR` — desativar ou reativar conta própria (`CTA_FL_ATIVO`). | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm06"></a>PERM06 | `CONTAS_AJUSTAR_SALDO` — realizar ajuste manual de saldo da própria conta. | [PERF01](#perf01), [PERF02](#perf02) |

> Estas permissões e seus vínculos vêm de carga inicial e são concedidas a **ADMIN e USER** — a tela é do próprio usuário. O que cada usuário enxerga e altera é limitado às suas contas pela [RN02](#rn02), não pela permissão. É expressamente proibida a criação de permissões genéricas com sufixo `MANTER`.

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `CONTAS_LISTAR` | ✓ | ✓ |
| `CONTAS_INSERIR` | ✓ | ✓ |
| `CONTAS_EDITAR` | ✓ | ✓ |
| `CONTAS_EXCLUIR` | ✓ | ✓ |
| `CONTAS_DESATIVAR` | ✓ | ✓ |
| `CONTAS_AJUSTAR_SALDO` | ✓ | ✓ |

Tanto `ADMIN` quanto `USER` recebem as 6 permissões atômicas na carga inicial: cada usuário gerencia as suas próprias contas. O `ADMIN` **não** tem visão administrativa das contas de outros usuários — dado financeiro é privado do dono, e o escopo por `USU_ID` da [RN02](#rn02) vale igual para os dois perfis.

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. `PERF_FL_SISTEMA = TRUE`. Recebe todas as permissões na carga inicial, inclusive as 6 deste módulo (Seção 13.1). Opera apenas sobre as próprias contas ([RN02](#rn02)). Corresponde a `ROLE_ADMIN`. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum. `PERF_FL_SISTEMA = TRUE`. Recebe as 6 permissões deste módulo na carga inicial e gerencia as próprias contas. Corresponde a `ROLE_USER`. |

---

## 15. Fluxo de Eventos

**Excluir ou desativar uma conta:**

```
1. Usuário clica no ícone Excluir de uma linha do grid.
2. Sistema exibe a confirmação MSG07.
3. Usuário confirma → chama EDP06.
        │
        ├─ Conta de outro usuário (RN02)             → MSG05 (404), nada muda.
        ├─ Conta em uso (RN07 / C4) e o parâmetro
        │  bloqueia a exclusão                       → MSG06 + oferta de desativar.
        │        └─ Usuário aceita desativar → EDP05 com CTA_FL_ATIVO = FALSE
        │                                             → MSG04, grid recarrega.
        └─ OK → preenche audit_data_exclusao / audit_excluido_por,
                 audita (Envers), invalida o cache do combobox,
                 retorna MSG08 e o grid é recarregado.
```

**Ajustar o saldo de uma conta:**

```
1. Usuário clica no ícone Ajustar saldo → modal QUADRO_DESCRITIVO_4.
        Saldo atual ← C1.saldo    Novo saldo iniciado com o mesmo valor.
2. Usuário informa o novo saldo e uma observação, clica em "Salvar ajuste" → EDP08.
        │
        ├─ Conta de outro usuário (RN02)   → MSG05 (404).
        ├─ Novo saldo vazio (RT09)          → MSG02.
        └─ OK → grava CTA_SALDO, mantém CTA_SALDO_SINCRONIZADO_EM,
                 registra a revisão no Envers com a observação,
                 retorna MSG10, recarrega o grid.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Listar minhas contas

Dado que estou autenticado com um usuário que tem a permissão [PERM01](#perm01).
E que tenho três contas cadastradas.
Quando eu acessar o menu "Finanças > Minhas Contas".
Então o sistema deve exibir o grid com as minhas três contas, ordenadas por descrição, mostrando o tipo, a instituição, o saldo, "considera no saldo geral", o número de lançamentos e a situação.

### 16.1 Bloquear acesso de usuário sem a permissão

Dado que estou autenticado com um usuário de um perfil que não tem [PERM01](#perm01).
Quando eu tentar acessar "/contas/listar" ou chamar "/contas/listar-dados".
Então o sistema deve negar o acesso (HTTP 403).

### 16.2 A listagem traz só as contas do próprio usuário

Dado que o usuário A tem duas contas e o usuário B tem uma conta.
Quando o usuário A abrir a tela Minhas Contas.
Então o grid deve mostrar apenas as duas contas do usuário A.

### 16.3 Acesso cruzado a buscar/{id} é negado

Dado que estou autenticado como usuário A.
E que existe a conta com id 50 pertencente ao usuário B.
Quando eu chamar "/contas/buscar/50".
Então o sistema deve responder 404 com [MSG05](#msg05), sem revelar que a conta existe.

### 16.4 Acesso cruzado a editar/{id} e ajustar-saldo/{id} é negado

Dado que estou autenticado como usuário A.
E que a conta com id 50 pertence ao usuário B.
Quando eu chamar "/contas/editar/50" ou "/contas/ajustar-saldo/50".
Então o sistema deve responder 404 com [MSG05](#msg05) e não alterar a conta do usuário B.

### 16.5 Cadastrar conta

Dado que estou na tela Minhas Contas com [PERM01](#perm01) e [PERM02](#perm02) e clico em "Nova conta".
Quando eu informar a descrição "Nubank Conta Corrente", o tipo "Corrente", a instituição "Nubank", a moeda "BRL" e o saldo inicial "1.500,00" e clicar em "Salvar".
Então o sistema deve criar a conta ativa, vinculada ao meu usuário, com saldo 1.500,00, exibir [MSG01](#msg01) e recarregar o grid.

### 16.6 Descrição duplicada para o mesmo usuário

Dado que eu já tenho a conta "Carteira".
Quando eu tentar criar outra conta minha com a descrição "carteira".
Então o sistema deve impedir e exibir [MSG03](#msg03).

### 16.7 Mesma descrição para usuários diferentes é permitida

Dado que o usuário A tem a conta "Carteira".
Quando o usuário B criar uma conta com a descrição "Carteira".
Então o sistema deve aceitar o cadastro.

### 16.8 Instituição imutável após a criação

Dado que abro uma conta minha em edição.
Então o campo Instituição deve estar desabilitado.
E ao salvar, a instituição da conta deve permanecer a mesma.

### 16.9 Não excluir conta com lançamentos

Dado que a minha conta "Nubank Conta Corrente" tem 3 despesas vinculadas.
E que o parâmetro CONTA_EXCLUSAO_BLOQUEIA_EM_USO está em "true".
Quando eu tentar excluí-la.
Então o sistema deve impedir, exibir [MSG06](#msg06) e oferecer a desativação.

### 16.10 Desativar conta em uso

Dado que a minha conta "Nubank Conta Corrente" tem lançamentos vinculados.
Quando eu desativá-la.
Então as despesas que a usam permanecem com a conta "Nubank Conta Corrente".
E a conta não deve mais aparecer no combobox de uma nova despesa.

### 16.11 Excluir conta sem uso

Dado que a minha conta "Conta Antiga" não tem nenhum lançamento.
Quando eu excluí-la e confirmar.
Então o sistema deve fazer a exclusão lógica, exibir [MSG08](#msg08) e recarregar o grid.

### 16.12 Combobox de lançamento traz só as contas ativas do usuário

Dado que eu tenho as contas "Nubank" (ativa) e "Conta Antiga" (inativa), e o usuário B tem a conta "Itaú" (ativa).
Quando a tela de nova Despesa carregar o combobox de conta para o meu usuário.
Então deve aparecer "Nubank", e não "Conta Antiga" nem "Itaú".

### 16.13 Ajustar saldo

Dado que tenho a permissão [PERM06](#perm06) e a minha conta "Nubank" está com saldo 1.500,00.
Quando eu abrir "Ajustar saldo", informar o novo saldo "1.234,56" e a observação "Conferência do extrato" e salvar.
Então o sistema deve gravar o saldo 1.234,56, manter `CTA_SALDO_SINCRONIZADO_EM` nulo, exibir [MSG10](#msg10) e recarregar o grid.

### 16.14 Conta fora do saldo geral

Dado que eu cadastro a conta "Reserva de emergência" com "considera no saldo geral" em "Não".
Quando eu consultar a listagem.
Então a conta deve aparecer com o marcador "Não" na coluna "Considera no saldo geral".
E o efeito no saldo consolidado é validado no documento `13`.

### 16.15 Auditoria da conta

Dado que eu ajusto o saldo de uma conta e salvo.
Quando eu consultar a auditoria de `CONTAS`.
Então deve haver o registro de quem ajustou e quando, com o saldo anterior e o novo.

### 16.16 ADMIN também só vê as próprias contas

Dado que estou autenticado como ADMIN.
E que existem contas de outros usuários.
Quando eu abrir a tela Minhas Contas.
Então o grid deve mostrar apenas as minhas contas, não as dos outros usuários.

---

## 17. Workshop de Análise

Data: —
Convidados: Diego Cordeiro
Participantes: Diego Cordeiro
Descrição: Levantamento a partir do Documento 0 (Observação 11; [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5)), do CRUD de `InstituicaoFinanceiraUsuario` da geração 1 (`dsc-backend`) e do padrão de tela de catálogo do documento `04 - manter-categoria`.

**Decisões tomadas:**
- `CONTAS` é dado do próprio usuário (tem `USU_ID`). A tela vive em "Finanças > Minhas Contas" e é acessível a `ADMIN` e `USER`; cada um opera só sobre as próprias contas ([RN02](#rn02)).
- O escopo por usuário (*row-level*) é resolvido no serviço, a partir do contexto de segurança — nunca de um parâmetro. Acesso cruzado por `{id}` responde 404 ([MSG05](#msg05)), sem revelar a existência do registro.
- Permissões atômicas por operação (`CONTAS_LISTAR`, `CONTAS_INSERIR`, `CONTAS_EDITAR`, `CONTAS_EXCLUIR`, `CONTAS_DESATIVAR`, `CONTAS_AJUSTAR_SALDO`), eliminando permissões genéricas com sufixo `MANTER` conforme diretrizes de governança RBAC granular e Observação 28 do Documento 0. Concedidas a `ADMIN` e `USER` na carga inicial — a permissão habilita a tela/operação; o recorte por dono é a regra de negócio.
- **O `ADMIN` não tem visão administrativa de dados financeiros de outros usuários — nem nesta versão nem no roadmap.** Dado financeiro é privado do dono; o `ADMIN` opera exatamente como um `USER`, sobre as próprias contas, e usa os próprios dados financeiros para os próprios lançamentos. Não haverá permissão do tipo `CONTAS_ADMINISTRAR_TODAS`.
- `TipoConta` é enum novo: `CORRENTE`, `POUPANCA`, `INVESTIMENTO`, `CARTEIRA`.
- Saldo: `CTA_SALDO` é definido no cadastro ("Saldo inicial") e alterado depois só pela ação dedicada "Ajustar saldo" ([EDP08](#edp08)) — a edição cadastral não mexe no saldo. Inputs monetários de saldo possuem obrigatoriamente máscara em tempo real no padrão `pt-BR` (`R$ 0,00`).
- Instituição imutável após a criação da conta ([RN06](#rn06)).
- O combobox de instituição é alimentado pelo endpoint de opções do documento `05 - manter-instituicao-financeira`.
- Exclusão lógica; trava "conta em uso" espelha a RN06 do documento `04`, parametrizável por `CONTA_EXCLUSAO_BLOQUEIA_EM_USO`. Conta com transação bancária nunca é excluível (FK `NOT NULL`).
- Grid client-side; sem carga inicial (a tabela nasce vazia).

**A Confirmar:**
- **Ajuste de saldo:** o registro do ajuste no comentário da revisão do Envers ([ID4](#qdd4-4) do modal) é suficiente, ou é preciso uma tabela de histórico de ajustes dedicada (data, valor anterior, valor novo, observação, autor)?
- **`CTA_FL_CONSIDERA_SALDO`:** confirmar com o documento `13` o efeito exato no "saldo geral consolidado" e se o default `TRUE` é o desejado.
- **Instituição imutável (adotado):** deve ser relaxado para permitir a troca enquanto a conta ainda não tem nenhum lançamento?
- **Moeda ≠ BRL:** a v1.0 só armazena `CTA_MOEDA`. Confirmar se o Dashboard fará conversão, se contas não-BRL ficam fora do saldo consolidado, ou se a moeda deve sair do escopo e ficar fixa em `BRL` nesta versão. Confirmar também o conjunto de moedas do combobox ([SB03](#sb03)).
- **Contas conectadas ao Open Finance** (`CTA_SALDO_SINCRONIZADO_EM` não nulo): esta tela permite editar/ajustar saldo/excluir essas contas, ou elas ficam somente-leitura aqui e são geridas nos documentos `14`/`15`?
- **Descrição única por usuário (adotado, validação de serviço):** vira um índice parcial no banco (`ALTER` no Documento 0), ou fica só na camada de serviço?
- **`CARTEIRA` e instituição:** faz sentido exigir `INFI_ID` para uma carteira de dinheiro em espécie? Se não, seria preciso tornar `INFI_ID` nullable no Documento 0 ou criar uma instituição "sistema" do tipo "Carteira". **Ficou mais urgente:** os documentos `08` (receita) e `09` (despesa) decidiram que **dinheiro em espécie é sempre uma conta do tipo `CARTEIRA`** (conta continua obrigatória em todo lançamento), então esta tela precisa permitir criar uma `CARTEIRA` sem fricção.
- **Conta `CARTEIRA` semeada automaticamente?** Como todo lançamento em espécie precisa de uma, o sistema deve criar uma conta "Carteira / Dinheiro em espécie" por usuário na carga inicial / no primeiro uso, ou o usuário cria manualmente quando precisar?
- **Agência / número:** máscara e obrigatoriedade condicionadas ao tipo de conta (ex.: `CORRENTE`/`POUPANCA` exigem agência e número; `CARTEIRA` não tem)?

---

## 18. Anexos

- **Protótipo e diagramas (v1.0):** gerados. Casos de uso (`prototipo/manter-conta-casos-uso.drawio` + `images/manter-conta-casos-uso.png`), DER do subconjunto (`prototipo/manter-conta-der.drawio` + `images/manter-conta-der.png`), wireframes das quatro telas/modais (`prototipo/manter-conta-prototipo.drawio` + `images/mc-tela-1..4.png`) e protótipo navegável (`prototipo/manter-conta-prototipo.html`). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright); diagramas `.drawio` por `prototipo/gen-diagramas.py`.
- Documento 0 — Fundação: `../00 - analise-geral/documento-0-fundacao.md` ([QUADRO_DESCRITIVO_4](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-4), [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6), [QUADRO_DESCRITIVO_7](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7), [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [QUADRO_DESCRITIVO_12](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-12)).
- Documento `04 - manter-categoria`: `../04 - manter-categoria/documento-analise-manter-categoria.md` (padrão de tela, trava "em uso" espelhada aqui na conta, forma e voz).
- Documento `05 - manter-instituicao-financeira`: `../05 - manter-instituicao-financeira/` (a escrever) — fonte do endpoint de opções de instituição.
- Código de referência geração 1: `dsc-backend` (`domain/InstituicaoFinanceiraUsuario.java`, `controller/InstituicaoFinanceiraUsuarioController.java`, `services/InstituicaoFinanceiraUsuarioService.java`).
