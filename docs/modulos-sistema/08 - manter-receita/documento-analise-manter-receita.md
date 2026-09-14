# dscproject — Análise de Sistemas
## Módulo Receitas — USER / ADMIN — Manter Receita

**Gerado em:** 08/09/2026
**Atualizado em:** 12/09/2026
**Versão:** 1.3
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
| 1.0 | 08/09/2026 | Diego dos Santos Cordeiro | Criação do documento. CRUD das **receitas do próprio usuário** (tela "Finanças > Receitas") para a geração 2 — sucessor do CRUD REST de `Receita` da geração 1 (`dsc-backend`), agora sobre a tabela `RECEITAS` ([QUADRO_DESCRITIVO_9 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9)). Introduz o conceito **prevista × recebida** (`RECE_FL_RECEBIDO` + `RECE_DT_RECEBIMENTO`), a competência como `YearMonth`, a origem do lançamento (`RECE_ORIGEM`) e o escopo *row-level* por usuário, derivado da conta. Remove os enums `RECE_TIPO_TRANSACAO` e `RECE_TIPO_RECEITA_DESPESA` da geração 1 (categoria passa a ser `CATE_ID`). Este documento **referencia** o QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova** |
| 1.1 | 11/09/2026 | Diego dos Santos Cordeiro | Desmembramento de `RECEITAS_MANTER` em `RECEITAS_INSERIR`, `RECEITAS_EDITAR`, `RECEITAS_EXCLUIR` e `RECEITAS_REGISTRAR_RECEBIMENTO`, proibição expressa de `MANTER`, definição de filtro inicial padrão em `mes_atual - 1`, aceitação irrestrita de competências passadas, totalizador condicional por competência única (`competenciaInicio == competenciaFim`), duplicação individual e em lote com preservação dos filtros ativos após operações e diretriz de máscara monetária client-side em tempo real (`pt-BR`, `R$ 0,00`). |
| 1.2 | 12/09/2026 | Diego dos Santos Cordeiro | Ordenação estável e determinística no grid: adição da regra de tela RT17 documentando indicadores visuais de ordenação ativa (`ph-caret-up` e `ph-caret-down`) nos cabeçalhos e desempate determinístico bidirecional por data de lançamento, data de recebimento, nome e id único quando a competência for idêntica (como no filtro padrão de competência única) ou quando outras colunas possuírem valores iguais. |
| 1.3 | 12/09/2026 | Diego dos Santos Cordeiro | Padronização visual do sistema: explicitação do alinhamento à esquerda para a coluna de Ações no grid de Receitas (QUADRO_DESCRITIVO_1). |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |
| D04 | `RECEITAS` não tem `USU_ID` próprio ([QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9)); o dono da receita é o `USU_ID` da conta (`RECE.CTA_ID → CONTAS.USU_ID`). O escopo por usuário (*row-level*) é sempre resolvido **no serviço**, a partir do contexto de segurança — nunca de um parâmetro da requisição. |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Receita** do `dscproject-spring-mvc` — o cadastro e a manutenção das **receitas do próprio usuário**: entradas de dinheiro previstas ou já recebidas (salário, renda extra, rendimentos, reembolsos etc.).

Na **geração 1** (API REST + SPA Angular), isto é o CRUD REST de `Receita` (`dsc-backend`): `GET /receitas` devolve **apenas as receitas do usuário autenticado** (`buscarTodosPorUsuario()` — resolve o usuário pelo token e consulta por `receita.instituicaoFinanceiraUsuario.usuario`), com `inserir`, `editar` e `excluir`. A entidade guarda competência (`String`, *default* `"0000-00"`), nome, descrição (obrigatória), valor, data de lançamento (`Instant`), o tipo de registro (enum `TipoRegistroFinanceiro`), a categoria (enum `CategoriaRegistroFinanceiro`) e a conta do usuário (`INFU_ID`). Não há distinção entre receita prevista e recebida, nem origem do lançamento.

O **Documento 0** ([QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), Observações 9, 13, 18 e 28) travou a reescrita da tabela `RECEITAS` para a geração 2:

- a categoria deixa de ser enum e passa a ser `CATE_ID` (FK para `CATEGORIAS`, do documento `04 - manter-categoria`); o enum `RECE_TIPO_TRANSACAO` (`TipoRegistroFinanceiro`) é **removido**;
- `RECE_DESCRICAO` passa a ser **opcional**;
- `RECE_DT_LANCAMENTO` passa de `Instant` para `LocalDate`;
- a competência (`RECE_COMPETENCIA`) vira `CHAR(7)` no formato `yyyy-MM`, com `CHECK`, mapeada em Java como `java.time.YearMonth` via `YearMonthConverter` (Documento 0, Seção 7.4);
- entram os campos novos **`RECE_FL_RECEBIDO`** (`BOOLEAN`, *default* `FALSE` — distingue receita **prevista** de **recebida**), **`RECE_DT_RECEBIMENTO`** (`DATE`, opcional) e **`RECE_ORIGEM`** (`VARCHAR(20)`, domínio `MANUAL` / `OPEN_FINANCE` / `IMPORTACAO`, *default* `MANUAL`);
- `Receita` passa a herdar da superclasse `LancamentoFinanceiro` (`@MappedSuperclass`, Documento 0, Seção 7.2), compartilhando valor, competência, data de lançamento, origem, conta e categoria com `Despesa` e `TransacaoBancaria`.

Este documento cobre:

- a **tela Receitas** — listar, cadastrar, editar, registrar o recebimento, duplicar (individual e em lote) e excluir (exclusão lógica) as receitas do usuário autenticado;
- o **escopo por usuário**: um usuário nunca vê nem altera a receita de outro — toda consulta e todo comando filtram pelo `USU_ID` da conta da receita, no serviço;
- o tratamento das receitas **importadas do Open Finance** (`RECE_ORIGEM != MANUAL`), que aparecem nesta tela mas têm conta, origem e exclusão bloqueadas.

**Escopo deste documento:**
- Tela de **listagem das receitas do usuário** (grid client-side), com filtro inicial padrão pela competência de `mes_atual - 1` e filtro avançado por modal (competência, situação, conta, categoria, texto).
- Totalizador condicional por competência (exibido apenas quando `competenciaInicio == competenciaFim`).
- Aceitação irrestrita de competências passadas.
- **Cadastro e edição** de receita via modal único com máscara monetária client-side em tempo real (`pt-BR`, `R$ 0,00`): nome, descrição, valor, data de lançamento, competência, conta, categoria, "recebido" e data de recebimento.
- **Registro de recebimento** por ação dedicada no grid (marca `RECE_FL_RECEBIDO` e grava `RECE_DT_RECEBIMENTO`), além do toggle equivalente no modal cadastral.
- **Duplicação de receitas** individualmente e em lote.
- **Preservação de filtros ativos** e estado da listagem após mutações.
- **Exclusão lógica** de receita — sem trava de "em uso" (a receita é folha: nada no domínio aponta para ela).
- Definição das permissões atômicas `RECEITAS_LISTAR`, `RECEITAS_INSERIR`, `RECEITAS_EDITAR`, `RECEITAS_EXCLUIR`, `RECEITAS_REGISTRAR_RECEBIMENTO` e da regra de escopo por usuário, com proibição expressa de `MANTER`.

**Não contempla:**
- CRUD de **Conta** (`CONTAS`) — documento `06 - manter-conta`. Aqui, a conta é apenas escolhida num combobox alimentado pelo endpoint de opções daquele documento.
- CRUD de **Categoria** (`CATEGORIAS`) — documento `04 - manter-categoria`. Aqui, a categoria é apenas escolhida num combobox alimentado pelo endpoint daquele documento, filtrado por `aplicaA=RECEITA`.
- CRUD de Despesa e de Transação Bancária — documentos `09` e `10`.
- **Sincronização e conciliação de Open Finance** — a conciliação (documento `15`) pode **criar** uma receita com `RECE_ORIGEM = OPEN_FINANCE`; esta tela **não** faz sincronização, apenas exibe e edita parcialmente o resultado.
- Cálculo de **saldo consolidado e efeito de `RECE_FL_RECEBIDO` no painel geral** — documento `13 - dashboard`. Aqui só se mantém o dado e o totalizador da competência filtrada.
- Receita **recorrente** (geração automática de ocorrências mês a mês) — fora do escopo da v1.1 (ver Seção 17).

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER). A tela é do **próprio usuário** — cada um opera somente sobre as suas receitas ([RN02](#rn02)). Dado financeiro é privado do dono: o ADMIN opera como um USER comum, sem qualquer visão administrativa das receitas de terceiros.

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **`RECEITAS` é dado do próprio usuário, mas o dono é indireto.** A tabela **não tem `USU_ID`** ([QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9)); o dono da receita é o `USU_ID` da conta vinculada (`RECE.CTA_ID → CONTAS.USU_ID`). A tela vive em "Finanças > Receitas", não em "Administração". | [RN02](#rn02), [RN03](#rn03) |
| 2 | **`RECE.CTA_ID` é `NULL` no schema, mas obrigatório na aplicação.** Como a conta é a única âncora de dono e de escopo, a v1.0/v1.1 **exige uma conta em toda receita** ([RN03](#rn03)) — inclusive nas importadas do Open Finance, que a conciliação (documento `15`) deve preencher. Sem conta, a receita ficaria órfã (sem dono, invisível em qualquer grid). O Documento 0 **não é alterado**: `RECE.CTA_ID` continua anulável no schema e a obrigatoriedade fica na camada de serviço. | [RN03](#rn03), [RNF02](#rnf02) |
| 2a | **Dinheiro em espécie é modelado como conta do tipo `CARTEIRA`.** Não há lançamento "sem conta": para registrar uma entrada em dinheiro vivo, o usuário usa uma conta do tipo `CARTEIRA` (`TipoConta.CARTEIRA`, já previsto no documento `06 - manter-conta`). Isso preserva o dono e a rastreabilidade da receita pela conta, sem `USU_ID` em `RECEITAS`. | [RN03](#rn03), documento `06 - manter-conta` |
| 3 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `RECEITAS` filtram pelo `USU_ID` da conta, resolvido do contexto de segurança — nunca de um parâmetro. Um endpoint com `{id}` de receita de outro usuário responde como "não encontrada" ([MSG05](#msg05)). Espelha o `buscarReceitaPorUsuario()` da geração 1 (que já filtrava por `instituicaoFinanceiraUsuario.usuario`). | [RN02](#rn02), [RNF01](#rnf01), [RNF02](#rnf02) |
| 4 | **Mudanças de estrutura vs geração 1.** `RECE_TIPO_TRANSACAO` (enum `TipoRegistroFinanceiro`) e `RECE_TIPO_RECEITA_DESPESA` (categoria como enum) **saem**; a categoria passa a ser `CATE_ID` (FK, *nullable*). `RECE_DESCRICAO` passa de obrigatória a opcional. `RECE_DT_LANCAMENTO` passa de `Instant` para `LocalDate`. Entram `RECE_FL_RECEBIDO`, `RECE_DT_RECEBIMENTO` e `RECE_ORIGEM`. **[Requer código]** | Documento 0, [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9) |
| 5 | **Prevista × recebida.** `RECE_FL_RECEBIDO` (*default* `FALSE`) distingue a receita **prevista** (planejada, ainda não caiu) da **recebida** (dinheiro já entrou). Ao marcar como recebida, `RECE_DT_RECEBIMENTO` passa a ser exigida; ao desmarcar, a data é limpa ([RN06](#rn06)). O efeito disso no saldo e no Dashboard é do documento `13`. | [RN06](#rn06) |
| 6 | **Competência é `YearMonth`.** `RECE_COMPETENCIA` é `CHAR(7)` `yyyy-MM`, com `CHECK` de formato no banco e `YearMonthConverter` no mapeamento (Documento 0, Seção 7.4). O *default* `"0000-00"` da geração 1 foi removido — a competência é obrigatória. A tela usa um campo mês/ano. **[Requer código]** | [RN04](#rn04), Documento 0, Seção 7.4 |
| 7 | **Competência × data de lançamento (decidido).** A competência é um **campo próprio e independente**, apenas **pré-preenchido** com o mês da data de lançamento por conveniência e reajustado enquanto o usuário não a editar à mão ([RT11](#rt11)). Ela pode divergir do mês da data de lançamento — caso clássico: salário de dezembro pago em 5 de janeiro fica com competência `2025-12` e data de lançamento `2026-01-05`. O pré-preenchimento é parametrizável ([Seção 12](#12-parâmetros-de-sistema)). | [RN04](#rn04), [RT11](#rt11) |
| 8 | **Origem do lançamento.** `RECE_ORIGEM` (`MANUAL` / `OPEN_FINANCE` / `IMPORTACAO`, *default* `MANUAL` — Documento 0, Observação 18). Receitas criadas por esta tela são **sempre `MANUAL`**; o serviço fixa o valor e ignora qualquer origem recebida ([RN07](#rn07)). | [RN07](#rn07) |
| 9 | **Receita importada do Open Finance** (`RECE_ORIGEM != MANUAL`): aparece nesta tela e tem os campos de negócio editáveis (nome, descrição, categoria, competência, recebido), mas **conta e origem ficam bloqueadas** e ela **não é excluível** por esta tela ([RN08](#rn08)) — a conciliação grava um vínculo lógico da transação de *staging* com a receita gerada (Documento 0, Observações 19 e 21), e excluí-la aqui deixaria esse vínculo pendente. | [RN08](#rn08), Documento 0, Observações 19 e 21 |
| 10 | **Sem trava de "em uso".** A receita é uma folha do domínio — nenhuma tabela tem FK para `RECEITAS` (o vínculo do *staging* de Open Finance é **lógico**, não FK — Documento 0, Observação 21). A exclusão lógica não precisa checar uso; a única restrição é a da receita `OPEN_FINANCE` ([RN08](#rn08)). | [RN09](#rn09) |
| 11 | **Sem "receita de sistema".** `RECEITAS` não tem *flag* de sistema nem carga inicial. A tabela nasce vazia; as receitas surgem do cadastro manual nesta tela ou do *backfill* / conciliação do Open Finance (documentos `14`/`15`). | Documento 0, Seção 6.4 |
| 12 | **Conta e categoria vêm de outros documentos.** O combobox de conta é o endpoint de opções de `CONTAS` do documento `06 - manter-conta` ([EDP07 daquele documento](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07) — `GET /contas/opcoes`, contas **ativas** do usuário). O combobox de categoria é o endpoint de opções de `CATEGORIAS` do documento `04 - manter-categoria` ([EDP07 daquele documento](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07) — `GET /categorias/opcoes?aplicaA=RECEITA`, categorias `RECEITA` + `AMBOS`). Este documento não define esses endpoints. | [SB01](#sb01), [SB02](#sb02) |
| 13 | **Grid client-side.** A tela carrega a lista das receitas do usuário e pagina/ordena/filtra no navegador (DataTables). O volume por usuário é da ordem de dezenas a poucas centenas de linhas por ano; a v1.0 não faz paginação server-side. Se o volume crescer, o filtro de competência pode passar a recortar server-side (ver Seção 17). | [RNF05](#rnf05) |
| 14 | **Auditoria.** `RECEITAS` é auditada via Hibernate Envers (`@Audited`), conforme o Documento 0. Criação, edição, registro de recebimento, duplicação e exclusão lógica ficam registrados (quem, quando, o quê). | [RNF03](#rnf03) |
| 15 | **Sem regra de unicidade.** Não há restrição de nome/valor/competência únicos — é normal ter "Salário" repetido todo mês, ou duas receitas iguais na mesma competência. | [RN05](#rn05) |
| 16 | **Valor positivo.** `RECE_VALOR` é obrigatório e deve ser **maior que zero** ([RN05](#rn05)). Estorno/ajuste com valor negativo não é escopo da v1.1 (ver Seção 17). | [RN05](#rn05) |
| 17 | **Filtro padrão e competências passadas.** A tela inicializa o filtro de listagem por padrão com `mes_atual - 1`. O sistema aceita qualquer competência passada sem travas ou restrições retroativas. | [RN13](#rn13), [RT13](#rt13) |
| 18 | **Totalizador por competência única.** O card com o totalizador financeiro consolidado das receitas filtradas é exibido apenas quando o filtro ativo limitar a consulta a uma competência única (`competenciaInicio == competenciaFim`). Quando o intervalo abranger competências distintas ou não houver filtro de competência, o card totalizador permanece oculto para evitar distorções cognitivas. | [RN14](#rn14), [RT14](#rt14) |
| 19 | **Duplicação de receitas e preservação de filtros.** O usuário pode duplicar uma receita específica ou selecionar múltiplos registros e duplicá-los em lote. As cópias nascem como `RECE_ORIGEM = 'MANUAL'`, `RECE_FL_RECEBIDO = FALSE`, `RECE_DT_RECEBIMENTO = NULL`. Ao concluir inserção, edição, exclusão ou duplicação, o grid recarrega preservando os filtros ativos e a página atual. | [RN15](#rn15), [RN16](#rn16), [RT15](#rt15) |
| 20 | **Máscara monetária em tempo real.** Todos os campos de valor de receita adotam máscara monetária client-side contínua no padrão `pt-BR` (`R$ 0,00`). | [RNF06](#rnf06), [RT11](#rt11) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as receitas do usuário autenticado, com: competência, nome, categoria, conta, valor, data de lançamento, situação (prevista/recebida), data de recebimento e origem. | Alta | Em análise |
| <a id="rf02"></a>RF02 | O sistema deve permitir filtrar a listagem por texto (nome/descrição), intervalo de competência, situação, conta e categoria, por meio de um modal acionado pelo botão "Filtrar". | Média | Em análise |
| <a id="rf03"></a>RF03 | O sistema deve permitir cadastrar uma nova receita via modal, com os campos: nome, descrição, valor, data de lançamento, competência, conta, categoria, "recebido" e data de recebimento. | Alta | Em análise |
| <a id="rf04"></a>RF04 | O sistema deve permitir editar uma receita existente via modal. | Alta | Em análise |
| <a id="rf05"></a>RF05 | O sistema deve permitir registrar o recebimento de uma receita prevista por uma ação dedicada, gravando a data de recebimento e marcando-a como recebida. | Alta | Em análise |
| <a id="rf06"></a>RF06 | O sistema deve permitir a exclusão lógica de uma receita, sem checar uso (a receita é folha do domínio). | Alta | Em análise |
| <a id="rf07"></a>RF07 | O sistema deve garantir que cada usuário só liste, consulte, edite, registre o recebimento, duplique e exclua as **próprias** receitas (as vinculadas às suas contas). | Alta | Em análise |
| <a id="rf08"></a>RF08 | O sistema deve exigir uma conta em toda receita e aceitar apenas contas ativas do próprio usuário. | Alta | Em análise |
| <a id="rf09"></a>RF09 | O sistema deve exigir valor maior que zero e competência no formato AAAA-MM. | Alta | Em análise |
| <a id="rf10"></a>RF10 | O sistema deve tratar as receitas importadas do Open Finance (`RECE_ORIGEM != MANUAL`) com conta e origem bloqueadas e sem permitir a exclusão por esta tela. | Média | Em análise |
| <a id="rf11"></a>RF11 | O sistema deve permitir duplicar uma receita individualmente a partir de ação na linha do grid. | Média | Em análise |
| <a id="rf12"></a>RF12 | O sistema deve permitir duplicar receitas em lote a partir da seleção de múltiplos registros no grid. | Média | Em análise |
| <a id="rf13"></a>RF13 | O sistema deve exibir totalizador financeiro das receitas filtradas condicionalmente quando a competência inicial for igual à competência final (`competenciaInicio == competenciaFim`). | Média | Em análise |
| <a id="rf14"></a>RF14 | O sistema deve inicializar o filtro da listagem por padrão com a competência de `mes_atual - 1`, permitindo a consulta a quaisquer competências passadas sem restrições temporais. | Média | Em análise |
| <a id="rf15"></a>RF15 | O sistema deve preservar todos os filtros e paginação ativos no grid após operações de cadastro, edição, exclusão, recebimento e duplicação. | Média | Em análise |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada endpoint desta tela exige a autoridade atômica da sua operação (`PERM_RECEITAS_LISTAR`, `PERM_RECEITAS_INSERIR`, `PERM_RECEITAS_EDITAR`, `PERM_RECEITAS_EXCLUIR`, `PERM_RECEITAS_REGISTRAR_RECEBIMENTO` — ver Seção 13 e [RN01](#rn01)). É vedado o uso de permissão agregadora `MANTER`. O `USU_ID` usado no filtro e nas travas vem sempre do contexto de segurança, nunca da requisição. | Teste de acesso com ADMIN, com USER e com perfis granulares sem as permissões específicas. |
| <a id="rnf02"></a>RNF02 | Isolamento | Nenhum endpoint que recebe `{id}` retorna, edita, registra o recebimento ou exclui a receita de outro usuário — a resposta é "não encontrada" ([MSG05](#msg05)), sem revelar a existência do registro. O dono é resolvido por `RECE.CTA_ID → CONTAS.USU_ID`. | Teste chamando `buscar/{id}`, `editar/{id}`, `marcar-recebida/{id}` e `excluir/{id}` com o id de uma receita de outro usuário. |
| <a id="rnf03"></a>RNF03 | Auditoria | `RECEITAS` tem auditoria completa via Hibernate Envers. Criação, edição, registro de recebimento, duplicação e exclusão lógica são registrados. | Inspeção da tabela `RECEITAS_aud` após as operações. |
| <a id="rnf04"></a>RNF04 | Integridade | A conta obrigatória e do próprio usuário ([RN03](#rn03)), o valor positivo ([RN05](#rn05)), o formato da competência ([RN04](#rn04)), a coerência recebido × data de recebimento ([RN06](#rn06)) e a aceitação de datas futuras ([RN12](#rn12)) são validados no serviço, não só na tela. | Teste chamando os endpoints diretamente, inclusive com data de lançamento no futuro. |
| <a id="rnf05"></a>RNF05 | Desempenho | A listagem ([EDP02](#edp02)) responde em menos de 1 s carregando a lista do usuário uma vez. | Medição em homologação com uma carga de ~500 receitas. |
| <a id="rnf06"></a>RNF06 | Usabilidade | A interface segue o padrão do projeto (Thymeleaf + Tabler + DataTables + AJAX) e é responsiva. O campo de valor usa máscara monetária em tempo real (`pt-BR`, `R$ 0,00`); a competência usa um seletor mês/ano. | Revisão visual do protótipo e teste interativo de digitação. |

---

## 4. Casos de Uso

[Inserir o diagrama de casos de uso — `prototipo/manter-receita-casos-uso.drawio` + `images/manter-receita-casos-uso.png` — quando gerado.]

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Minhas Receitas | [PERF01](#perf01), [PERF02](#perf02) | O usuário acessa o menu e visualiza a lista das suas receitas. ([RF01](#rf01), [RF07](#rf07)) |
| <a id="caus02"></a>CAUS02 | Filtrar Receitas | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de filtro, informa os critérios e aplica. ([RF02](#rf02)) |
| <a id="caus03"></a>CAUS03 | Cadastrar Receita | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de cadastro, preenche os dados e confirma; pode já marcá-la como recebida. ([RF03](#rf03), [RF08](#rf08), [RF09](#rf09)) |
| <a id="caus04"></a>CAUS04 | Editar Receita | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de edição, altera os dados e confirma; numa receita importada do Open Finance, conta e origem ficam bloqueadas. ([RF04](#rf04), [RF10](#rf10)) |
| <a id="caus05"></a>CAUS05 | Registrar Recebimento | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre a ação "Registrar recebimento" de uma receita prevista, confirma a data e a receita passa a recebida. ([RF05](#rf05)) |
| <a id="caus06"></a>CAUS06 | Excluir Receita | [PERF01](#perf01), [PERF02](#perf02) | O usuário exclui logicamente uma receita `MANUAL`; a receita importada do Open Finance não é excluível por esta tela. ([RF06](#rf06), [RF10](#rf10)) |
| <a id="caus07"></a>CAUS07 | Duplicar Receita(s) | [PERF01](#perf01), [PERF02](#perf02) | O usuário duplica uma receita individualmente ou várias receitas em lote gerando novos lançamentos previstos. ([RF11](#rf11), [RF12](#rf12)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Finanças > Receitas

**Critérios de Aceitação:**
- O menu 'Receitas' é visível apenas para quem tem [PERM01](#perm01).
- Ao acessar a tela, a listagem das receitas do usuário autenticado é carregada automaticamente, filtrada por padrão com a competência `mes_atual - 1`.
- A listagem nunca traz receita de outro usuário.
- O card totalizador financeiro é exibido se e somente se `competenciaInicio == competenciaFim`.
- O filtro é aplicado por um modal acionado pelo botão "Filtrar" e aceita qualquer competência passada.
- O cadastro e a edição de receita são feitos num modal único com máscara monetária em tempo real (`pt-BR`, `R$ 0,00`).
- O usuário pode duplicar receitas individualmente ou em lote, gerando registros como previstos.
- As mutações (cadastro, edição, exclusão, recebimento e duplicação) preservam todos os filtros ativos e a página do grid.
- O campo Conta só oferece contas ativas do próprio usuário; a receita não é salva sem conta.
- Ao marcar uma receita como recebida, a data de recebimento é exigida; ao desmarcar, a data é limpa.
- Registrar o recebimento por uma ação do grid grava a data e marca a receita como recebida.
- Uma receita importada do Open Finance abre em edição com Conta e Origem desabilitadas e sem o botão de excluir.
- A exclusão é lógica e não checa uso.
- Chamar `buscar/{id}`, `editar/{id}`, `marcar-recebida/{id}` ou `excluir/{id}` com o id de uma receita de outro usuário responde "não encontrada".

---

## 6. Banco de Dados

Toda a estrutura está no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova**.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `RECEITAS` | Documento 0 — [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9) | CRUD + registro de recebimento + duplicação, sempre no escopo do `USU_ID` da conta |
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | Somente leitura: âncora de dono (`CONTAS.USU_ID`) e combobox de conta (endpoint do documento `06`) |
| `CATEGORIAS` | Documento 0 — [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) | Somente leitura: combobox de categoria (endpoint do documento `04`, `aplicaA=RECEITA`) |
| `USUARIOS` | Documento 0 — [QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2) | Somente leitura: dono da receita, via `CONTAS.USU_ID` |
| `OPFI_TRANSACOES` | Documento 0 — [QUADRO_DESCRITIVO_20](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-20) | Contexto: guarda o vínculo **lógico** com a receita gerada na conciliação (documento `15`); não é FK. Base da restrição da [RN08](#rn08) |

> Nenhum `ALTER TABLE` neste documento. A tabela `RECEITAS`, a FK `CTA_ID` (anulável) e a FK `CATE_ID` (anulável) já existem no Documento 0 ([DDL_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9)). A obrigatoriedade de `CTA_ID` na aplicação ([RN03](#rn03)) é validada no serviço — o schema do Documento 0 fica como está (coluna anulável); dinheiro em espécie é conta do tipo `CARTEIRA` ([Observação 2a](#2-observações)).

### 6.1 Diagrama ER

[Inserir `images/manter-receita-der.png` quando gerado — subconjunto do DER do Documento 0: `RECEITAS`, `CONTAS`, `CATEGORIAS`, `USUARIOS` e o vínculo lógico com `OPFI_TRANSACOES`. Fonte: `prototipo/manter-receita-der.drawio`.]

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| RECEITAS | RECEITAS_aud | Competência, nome, descrição, valor, data de lançamento, data de recebimento, flag de recebido, origem, conta e categoria. Registra criação, edição, registro de recebimento, duplicação e exclusão lógica |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. A resolução do dono pela conta, a validação de conta/categoria, a coerência recebido × data de recebimento, a duplicação e a exclusão lógica ficam na camada de serviço.

### 6.4 Carga Inicial

Nenhuma. `RECEITAS` nasce vazia (Documento 0, Seção 6.4 — grupo 7, sem *seed*). As receitas surgem do cadastro manual nesta tela ou do *backfill* / conciliação do Open Finance (documentos `14`/`15`).

---

## 7. Protótipos de Interface

Protótipo navegável e wireframes: `prototipo/manter-receita-prototipo.html` e `prototipo/manter-receita-prototipo.drawio` (a gerar). Os números em destaque nas telas correspondem aos IDs dos itens do respectivo QUADRO_DESCRITIVO.

### <a id="quadro-descritivo-1"></a>7.1 Tela: Receitas (Listagem) — QUADRO_DESCRITIVO_1

[Inserir `images/mr-tela-1.png` quando gerado.]

> OBSERVAÇÕES: Tela acessada via 'Finanças > Receitas'. Restrita a quem tem [PERM01](#perm01). Grid client-side, carregado apenas com as receitas do usuário autenticado ([EDP02](#edp02) → [C1](#c1)), inicializado por padrão com a competência `mes_atual - 1` ([RN13](#rn13)). O card totalizador só é renderizado quando `competenciaInicio == competenciaFim` ([RN14](#rn14)). O filtro avançado é acionado por modal (botão "Filtrar").

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/receitas/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Finanças > Receitas | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Receitas | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Cadastre e acompanhe as suas receitas previstas e recebidas. | — |
| <a id="qdd1-3a"></a>3a | CARD TOTALIZADOR | Tipo: Card numérico<br>Texto: Total da Competência: R$ {total}<br>Exibição condicional: `competenciaInicio == competenciaFim` | Exibe o somatório total das receitas da competência filtrada ([RN14](#rn14)). Oculto quando intervalo for múltiplo ou ausente. |
| <a id="qdd1-4"></a>4 | BOTÃO FILTRAR | Tipo: Botão<br>Texto: Filtrar<br>Ícone: filter | Ao clicar, executar [RT01](#rt01). |
| <a id="qdd1-5"></a>5 | BOTÃO NOVA RECEITA | Tipo: Botão (primário)<br>Texto: Nova receita<br>Ícone: plus | Visível a quem tem [PERM02](#perm02) (`RECEITAS_INSERIR`). Ao clicar, executar [RT04](#rt04). |
| <a id="qdd1-5a"></a>5a | BOTÃO DUPLICAR SELECIONADAS | Tipo: Botão (secundário)<br>Texto: Duplicar selecionadas<br>Ícone: copy | Visível a quem tem [PERM02](#perm02) (`RECEITAS_INSERIR`). Desabilitado quando nenhuma linha selecionada. Ao clicar, executar [RT15](#rt15). |
| <a id="qdd1-6"></a>6 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side)<br>Colunas: [ID6a](#qdd1-6a)…[ID17](#qdd1-17)<br>Itens por página: 10, 25, 50<br>Ordenação padrão: Competência decrescente, depois Data de lançamento decrescente<br>Endpoint: [EDP02](#edp02) | Carrega a lista das receitas do usuário autenticado. Filtra em memória conforme [RT02](#rt02). Preserva estado conforme [RT16](#rt16). |
| <a id="qdd1-6a"></a>6a | SELEÇÃO (CHECKBOX) | Tipo: Coluna (checkbox)<br>Header: Selecionar todos | Permite marcar registros para duplicação em lote ([RT15](#rt15)). |
| <a id="qdd1-7"></a>7 | COMPETÊNCIA | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).competencia formatada como "MM/AAAA". |
| <a id="qdd1-8"></a>8 | NOME | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).nome. |
| <a id="qdd1-9"></a>9 | CATEGORIA | Tipo: Coluna (badge)<br>Ordenação: Sim | Exibe [C1](#c1).categoriaNome; vazio quando a receita não tem categoria. |
| <a id="qdd1-10"></a>10 | CONTA | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).contaDescricao. |
| <a id="qdd1-11"></a>11 | VALOR | Tipo: Coluna (moeda, alinhada à direita)<br>Ordenação: Sim | Exibe [C1](#c1).valor formatado em BRL (`pt-BR`). |
| <a id="qdd1-12"></a>12 | DATA DE LANÇAMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe [C1](#c1).dataLancamento. |
| <a id="qdd1-13"></a>13 | SITUAÇÃO | Tipo: Coluna (badge)<br>Ordenação: Sim | "Recebida" (verde) quando [C1](#c1).recebido; "Prevista" (cinza) caso contrário. "Excluída" quando há `audit_data_exclusao`. |
| <a id="qdd1-14"></a>14 | DATA DE RECEBIMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe [C1](#c1).dataRecebimento; vazio quando prevista. |
| <a id="qdd1-15"></a>15 | ORIGEM | Tipo: Coluna (badge)<br>Ordenação: Sim | "Manual" / "Open Finance" / "Importação", de [C1](#c1).origem. |
| <a id="qdd1-16"></a>16 | AÇÃO | Tipo: Coluna (alinhada à esquerda) | Visível a usuários com permissões de gestão. Ícones [ID17](#qdd1-17). |
| <a id="qdd1-17"></a>17 | ÍCONES DE AÇÃO | Tipo: Ícones<br>Editar (ícone: edit, tooltip: Editar receita)<br>Registrar recebimento (ícone: cash-banknote, tooltip: Registrar recebimento)<br>Duplicar (ícone: copy, tooltip: Duplicar receita)<br>Excluir (ícone: trash, tooltip: Excluir receita) | Editar → [RT05](#rt05) (exige [PERM03](#perm03) `RECEITAS_EDITAR`). Registrar recebimento → [RT08](#rt08) (exige [PERM05](#perm05) `RECEITAS_REGISTRAR_RECEBIMENTO`; oculto quando já recebida). Duplicar → [RT15](#rt15) (exige [PERM02](#perm02) `RECEITAS_INSERIR`). Excluir → [RT07](#rt07) (exige [PERM04](#perm04) `RECEITAS_EXCLUIR`; oculto quando excluída ou origem ≠ "MANUAL"). |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Filtrar Receitas — QUADRO_DESCRITIVO_2

[Inserir `images/mr-tela-2.png` quando gerado.]

> OBSERVAÇÕES: Todos os campos são opcionais. A tela inicializa o filtro padrão com a competência de `mes_atual - 1` ([RN13](#rn13)). São aceitas quaisquer competências passadas sem restrição. O filtro é aplicado em memória sobre a lista já carregada ([RT02](#rt02)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Filtrar Receitas | — |
| <a id="qdd2-2"></a>2 | FILTRO – BUSCA | Tipo: Input Text<br>Obrigatório: Não<br>Placeholder: Nome ou descrição<br>Tooltip: Filtre por parte do nome ou da descrição. | Filtro parcial e sem acento sobre nome e descrição. |
| <a id="qdd2-3"></a>3 | FILTRO – COMPETÊNCIA INICIAL | Tipo: Seletor mês/ano<br>Obrigatório: Não<br>Valor padrão inicial: `mes_atual - 1` | Filtra [C1](#c1).competencia ≥ valor. Aceita competências passadas irrestritamente ([RN13](#rn13)). Ver [SB04](#sb04). |
| <a id="qdd2-4"></a>4 | FILTRO – COMPETÊNCIA FINAL | Tipo: Seletor mês/ano<br>Obrigatório: Não<br>Valor padrão inicial: `mes_atual - 1` | Filtra [C1](#c1).competencia ≤ valor. Aceita competências passadas irrestritamente ([RN13](#rn13)). Ver [SB04](#sb04). |
| <a id="qdd2-5"></a>5 | FILTRO – SITUAÇÃO | Tipo: Combobox<br>Obrigatório: Não<br>Valor default: Todas<br>Domínio: Todas / Prevista / Recebida / Excluída | Filtra por [C1](#c1).recebido e pela presença de `audit_data_exclusao`. Ver [SB03](#sb03). |
| <a id="qdd2-6"></a>6 | FILTRO – CONTA | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todas as contas<br>Domínio: "Todas" + contas do usuário | Filtra por [C1](#c1).contaId. Ver [SB01](#sb01). |
| <a id="qdd2-7"></a>7 | FILTRO – CATEGORIA | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todas as categorias<br>Domínio: "Todas" + categorias de receita | Filtra por [C1](#c1).categoriaId. Ver [SB02](#sb02). |
| <a id="qdd2-8"></a>8 | BOTÃO APLICAR | Tipo: Botão<br>Texto: Aplicar | Ao clicar, executar [RT02](#rt02). |
| <a id="qdd2-9"></a>9 | BOTÃO LIMPAR | Tipo: Botão<br>Texto: Limpar | Ao clicar, executar [RT03](#rt03). |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Cadastro / Edição de Receita — QUADRO_DESCRITIVO_3

[Inserir `images/mr-tela-3.png` quando gerado.]

> OBSERVAÇÕES: Modal único de cadastro e edição. O cadastro exige [PERM02](#perm02) (`RECEITAS_INSERIR`) e a edição exige [PERM03](#perm03) (`RECEITAS_EDITAR`). No formulário, os campos Competência ([ID6](#qdd3-6)), Conta ([ID7](#qdd3-7)) e Categoria ([ID8](#qdd3-8)) são apresentados no topo como os primeiros a serem preenchidos. O campo Valor ([ID4](#qdd3-4)) possui máscara monetária client-side contínua (`pt-BR`, `R$ 0,00`). A Data de recebimento ([ID10](#qdd3-10)) só aparece e só é obrigatória quando Recebido ([ID9](#qdd3-9)) está em "Sim" ([RT12](#rt12)). Ao editar uma receita importada do Open Finance, os campos Conta ([ID7](#qdd3-7)) e Origem ficam desabilitados e o aviso ([ID11](#qdd3-11)) é exibido ([RN08](#rn08)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Nova receita / Editar receita | Varia conforme o modo. |
| <a id="qdd3-6"></a>6 | CAMPO – COMPETÊNCIA | Tipo: Seletor mês/ano<br>Obrigatório: Sim<br>Valor default: mês da data de lançamento | Grava `RECE_COMPETENCIA` (`yyyy-MM`). Aceita qualquer competência passada. Ver [SB04](#sb04) e [RN04](#rn04). |
| <a id="qdd3-7"></a>7 | CAMPO – CONTA | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: contas ativas do usuário | Grava `CTA_ID`. Ver [SB01](#sb01) e [RN03](#rn03). **Desabilitado** ao editar receita com origem ≠ MANUAL ([RN08](#rn08)). |
| <a id="qdd3-8"></a>8 | CAMPO – CATEGORIA | Tipo: Combobox<br>Obrigatório: Não<br>Domínio: categorias de receita ativas | Grava `CATE_ID`. Ver [SB02](#sb02). |
| <a id="qdd3-2"></a>2 | CAMPO – NOME | Tipo: Input Text<br>Tamanho: 100<br>Obrigatório: Sim | Grava `RECE_NOME`. Ex.: "SALÁRIO", "FREELA SITE". |
| <a id="qdd3-3"></a>3 | CAMPO – DESCRIÇÃO | Tipo: Textarea<br>Tamanho: 512<br>Obrigatório: Não | Grava `RECE_DESCRICAO`. |
| <a id="qdd3-4"></a>4 | CAMPO – VALOR | Tipo: Input monetário (BRL)<br>Máscara: Moeda client-side em tempo real (`pt-BR`, `R$ 0,00`)<br>Obrigatório: Sim | Grava `RECE_VALOR`. Deve ser maior que zero ([RN05](#rn05)). |
| <a id="qdd3-5"></a>5 | CAMPO – DATA DE LANÇAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Valor default: hoje | Grava `RECE_DT_LANCAMENTO`. Ao mudar, pode reajustar a Competência ([RT11](#rt11)). |
| <a id="qdd3-9"></a>9 | CAMPO – RECEBIDO | Tipo: Toggle (Sim/Não)<br>Valor default: Não | Grava `RECE_FL_RECEBIDO`. Ao alterar, executar [RT12](#rt12). |
| <a id="qdd3-10"></a>10 | CAMPO – DATA DE RECEBIMENTO | Tipo: Input Date<br>Obrigatório: Sim quando Recebido = Sim<br>Valor default: hoje<br>Exibição: só quando Recebido = Sim | Grava `RECE_DT_RECEBIMENTO`. Ver [RN06](#rn06). |
| <a id="qdd3-11"></a>11 | AVISO – RECEITA IMPORTADA | Tipo: Texto informativo | Exibido no modo edição quando [C1](#c1).origem ≠ "MANUAL": "Esta receita foi importada do Open Finance. A conta e a origem não podem ser alteradas, e ela não pode ser excluída por esta tela." |
| <a id="qdd3-12"></a>12 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar<br>Endpoint: [EDP04](#edp04) (criação - [PERM02](#perm02)) ou [EDP05](#edp05) (edição - [PERM03](#perm03)) | Ao clicar, executar [RT06](#rt06). |
| <a id="qdd3-13"></a>13 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-4"></a>7.4 Modal: Registrar Recebimento — QUADRO_DESCRITIVO_4

[Inserir `images/mr-tela-4.png` quando gerado.]

> OBSERVAÇÕES: Acionado pelo ícone "Registrar recebimento" do grid ([ID17](#qdd1-17)), restrito a [PERM05](#perm05) (`RECEITAS_REGISTRAR_RECEBIMENTO`). Marca `RECE_FL_RECEBIDO = TRUE` e grava `RECE_DT_RECEBIMENTO`. Fica oculto para receitas já recebidas.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Registrar recebimento — {nome da receita} | — |
| <a id="qdd4-2"></a>2 | VALOR DA RECEITA | Tipo: Texto (somente leitura) | Exibe [C1](#c1).valor formatado em BRL. |
| <a id="qdd4-3"></a>3 | CAMPO – DATA DE RECEBIMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Valor default: hoje | Grava `RECE_DT_RECEBIMENTO`. |
| <a id="qdd4-4"></a>4 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Registrar<br>Endpoint: [EDP07](#edp07) (exige [PERM05](#perm05)) | Ao clicar, executar [RT09](#rt09). |
| <a id="qdd4-5"></a>5 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### 7.5 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | CONTA | Itens carregados de `CONTAS` via o endpoint de opções do documento `06 - manter-conta` ([EDP07 daquele documento](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07) — `GET /contas/opcoes`), que já devolve apenas as contas **ativas do usuário autenticado**, ordenadas por descrição. No modal de receita ([ID7](#qdd3-7)), sem opção vazia. No filtro do grid ([ID6](#qdd2-6)), a opção "Todas as contas" é adicional. Nunca `<option>` fixo no HTML. |
| <a id="sb02"></a>SB02 | CATEGORIA | Itens carregados de `CATEGORIAS` via o endpoint de opções do documento `04 - manter-categoria` ([EDP07 daquele documento](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07) — `GET /categorias/opcoes?aplicaA=RECEITA`), que devolve as categorias ativas com `CATE_APLICA_A` em `RECEITA` ou `AMBOS`, ordenadas por nome. No modal, a categoria é opcional (opção "Sem categoria"). No filtro do grid ([ID7](#qdd2-7)), a opção "Todas as categorias" é adicional. Nunca `<option>` fixo no HTML. |
| <a id="sb03"></a>SB03 | FILTRO SITUAÇÃO | Domínio fixo do próprio filtro: Todas, Prevista, Recebida, Excluída. Não é entidade. |
| <a id="sb04"></a>SB04 | COMPETÊNCIA | Campo mês/ano (`YearMonth`), renderizado como seletor mês/ano ou `<input type="month">`. Aceita competências passadas sem restrições. Não é entidade. |

### 7.6 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao clicar em "Filtrar" ([ID4](#qdd1-4)), abrir o modal de filtro ([QUADRO_DESCRITIVO_2](#quadro-descritivo-2)) com os valores atualmente aplicados. |
| <a id="rt02"></a>RT02 | Ao clicar em "Aplicar" ([ID8](#qdd2-8)), filtrar **em memória** a lista já carregada: busca parcial e sem acento sobre nome/descrição, competência dentro do intervalo informado, e correspondência exata de situação, conta e categoria. Atualizar o card totalizador ([ID3a](#qdd1-3a)) conforme [RT14](#rt14). Fechar o modal. Se nada restar, exibir [MSG08](#msg08) na área do grid. |
| <a id="rt03"></a>RT03 | Ao clicar em "Limpar" ([ID9](#qdd2-9)), voltar todos os campos do filtro para vazio, exceto as competências que voltam ao padrão `mes_atual - 1`, Situação para "Todas", e reaplicar conforme [RT02](#rt02). |
| <a id="rt04"></a>RT04 | Ao clicar em "Nova receita" ([ID5](#qdd1-5)) — visível só com [PERM02](#perm02) (`RECEITAS_INSERIR`) —, abrir o modal ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)) em modo criação: Nome/Descrição/Valor vazios, Data de lançamento em hoje, Competência no mês corrente, Recebido em "Não" (sem o campo Data de recebimento), sem o aviso de receita importada. Carregar os comboboxes Conta e Categoria conforme [RT10](#rt10). |
| <a id="rt05"></a>RT05 | Ao clicar no ícone Editar ([ID17](#qdd1-17)) — visível só com [PERM03](#perm03) (`RECEITAS_EDITAR`) —, chamar [EDP03](#edp03) com o id e abrir o modal em modo edição, com os campos preenchidos. Se [C1](#c1).origem ≠ "MANUAL", desabilitar Conta e Origem e exibir o aviso [ID11](#qdd3-11) ([RN08](#rn08)). Se [C1](#c1).recebido, exibir o campo Data de recebimento preenchido. |
| <a id="rt06"></a>RT06 | Ao clicar em "Salvar" ([ID12](#qdd3-12)): validar Nome, Valor, Data de lançamento, Competência e Conta obrigatórios ([MSG02](#msg02)); validar Valor maior que zero ([MSG03](#msg03)); validar o formato da Competência ([MSG11](#msg11)); se Recebido = "Sim", validar Data de recebimento preenchida ([MSG09](#msg09)). Em criação, chamar [EDP04](#edp04) (exige [PERM02](#perm02)); em edição, [EDP05](#edp05) (exige [PERM03](#perm03)). Em sucesso, exibir [MSG01](#msg01) (criação) ou [MSG04](#msg04) (edição), fechar o modal e recarregar o grid via [EDP02](#edp02) preservando todos os filtros e a página ativa ([RT16](#rt16)). Conta indisponível → [MSG13](#msg13). |
| <a id="rt07"></a>RT07 | Ao clicar no ícone Excluir ([ID17](#qdd1-17)) — visível só com [PERM04](#perm04) (`RECEITAS_EXCLUIR`) e oculto quando [C1](#c1).origem ≠ "MANUAL" —, exibir a confirmação [MSG06](#msg06). Ao confirmar, chamar [EDP06](#edp06). Receita importada do Open Finance → [MSG12](#msg12). Em sucesso, exibir [MSG07](#msg07) e recarregar o grid preservando os filtros ativos ([RT16](#rt16)). |
| <a id="rt08"></a>RT08 | Ao clicar no ícone Registrar recebimento ([ID17](#qdd1-17)) — visível só com [PERM05](#perm05) (`RECEITAS_REGISTRAR_RECEBIMENTO`) e oculto quando [C1](#c1).recebido —, abrir o modal ([QUADRO_DESCRITIVO_4](#quadro-descritivo-4)) com o Valor da receita ([ID2](#qdd4-2)) preenchido e a Data de recebimento ([ID3](#qdd4-3)) iniciada em hoje. |
| <a id="rt09"></a>RT09 | Ao clicar em "Registrar" ([ID4](#qdd4-4)): validar Data de recebimento preenchida ([MSG02](#msg02)) e chamar [EDP07](#edp07) com a data (exige [PERM05](#perm05)). Em sucesso, exibir [MSG10](#msg10), fechar o modal e recarregar o grid via [EDP02](#edp02) preservando os filtros ativos ([RT16](#rt16)). |
| <a id="rt10"></a>RT10 | Ao abrir o modal de receita ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)), carregar o combobox Conta pelo endpoint de opções do documento `06` ([SB01](#sb01)) e o combobox Categoria pelo endpoint de opções do documento `04` com `aplicaA=RECEITA` ([SB02](#sb02)). Nunca renderizar `<option>` fixo no HTML. |
| <a id="rt11"></a>RT11 | Aplicar máscara monetária contínua em tempo real (`pt-BR`, `R$ 0,00`) ao campo Valor ([ID4](#qdd3-4)) e formatar a coluna Valor do grid ([ID11](#qdd1-11)) em BRL. Ao alterar a Data de lançamento ([ID5](#qdd3-5)), e enquanto o usuário não tiver editado a Competência ([ID6](#qdd3-6)) manualmente, atualizar a Competência para o mês da nova data — comportamento controlado pelo parâmetro `RECEITA_COMPETENCIA_SEGUE_DATA_LANCAMENTO` ([Seção 12](#12-parâmetros-de-sistema)). |
| <a id="rt12"></a>RT12 | Ao alternar o toggle Recebido ([ID9](#qdd3-9)): quando passar para "Sim", exibir o campo Data de recebimento ([ID10](#qdd3-10)) e iniciá-lo em hoje; quando passar para "Não", ocultar e limpar o campo ([RN06](#rn06)). |
| <a id="rt13"></a>RT13 | Na carga inicial da tela, aplicar o filtro inicial com competência inicial e final configuradas para `mes_atual - 1`. Permitir livremente a navegação e seleção de competências passadas sem restrições. |
| <a id="rt14"></a>RT14 | O card totalizador ([ID3a](#qdd1-3a)) deve ser exibido **exclusivamente** quando o filtro de competência inicial for idêntico ao de competência final (`competenciaInicio == competenciaFim`). Havendo intervalo múltiplo ou ausência de competência, o card é ocultado. O totalizador exibe a soma de todas as receitas ativas listadas para a competência. |
| <a id="rt15"></a>RT15 | Ao acionar a duplicação (ícone da linha [ID17](#qdd1-17) ou botão "Duplicar selecionadas" [ID5a](#qdd1-5a) com 1+ itens selecionados via checkbox [ID6a](#qdd1-6a)): solicitar confirmação; ao confirmar, enviar os IDs para [EDP08](#edp08) (exige [PERM02](#perm02) `RECEITAS_INSERIR`). Em sucesso, exibir [MSG15](#msg15), desmarcar as seleções e recarregar o grid preservando a página e filtros ativos ([RT16](#rt16)). |
| <a id="rt16"></a>RT16 | Todas as operações de mutação (cadastro, edição, exclusão, recebimento e duplicação) devem atualizar o grid de dados mantendo os filtros em memória ativos e preservando a página atual de paginação do DataTables, garantindo continuidade ao fluxo de trabalho do usuário. |
| <a id="rt17"></a>RT17 | **Ordenação Estável e Determinística no Grid:** Todas as colunas ordenáveis (`th.sortable`) devem possuir indicadores visuais de ordenação ativa (`ph-caret-up` para ascendente e `ph-caret-down` para descendente). A ordenação por Competência deve ordenar cronologicamente por `yyyy-MM` e, em caso de empate (como na visualização padrão de competência única), aplicar desempate determinístico respeitando a direção (`asc`/`desc`): pela data de lançamento (`dataLancamento`), pela data de recebimento (`dataRecebimento`), pelo nome da receita (`nome`) e pelo identificador único (`id`). Para ordenação por outras colunas, havendo empate, aplicar desempate por competência, data de lançamento, nome e identificador. |

---

## 8. Endpoints

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) (`RECEITAS_LISTAR`) | /receitas/listar | N |
| Retorna a página da listagem de receitas (Thymeleaf). O grid é carregado por [EDP02](#edp02). | | | | |
| <a id="edp02"></a>EDP02 | GET | [PERM01](#perm01) (`RECEITAS_LISTAR`) | /receitas/listar-dados | N |
| Lista das receitas do usuário autenticado para o grid, em JSON. Executa [C1](#c1) com o `USU_ID` do contexto de segurança ([RN02](#rn02)). Campos: id, competencia, nome, descricao, valor, dataLancamento, dataRecebimento, recebido (boolean), origem, contaId, contaDescricao, categoriaId, categoriaNome, excluido (boolean). Sem paginação (client-side). | | | | |
| <a id="edp03"></a>EDP03 | GET | [PERM03](#perm03) (`RECEITAS_EDITAR`) | /receitas/buscar/{id} | N |
| Retorna uma receita do usuário autenticado para edição. Executa [RN02](#rn02) (via [C2](#c2)) — se a receita não for do usuário, responde 404 ([MSG05](#msg05)). Campos: id, competencia, nome, descricao, valor, dataLancamento, dataRecebimento, recebido, origem, contaId, categoriaId. | | | | |
| <a id="edp04"></a>EDP04 | POST | [PERM02](#perm02) (`RECEITAS_INSERIR`) | /receitas/inserir | N |
| Cria uma receita para o usuário autenticado. Executa, nesta ordem: [RN03](#rn03) (via [C3](#c3) — conta obrigatória, ativa e do usuário), [RN04](#rn04) (competência), [RN05](#rn05) (valor > 0), [RN06](#rn06) (coerência recebido × data), [C4](#c4) (categoria, quando informada), [RN07](#rn07) (fixa `RECE_ORIGEM = MANUAL`) e persiste. Dados: competencia, nome, descricao, valor, dataLancamento, contaId, categoriaId, recebido, dataRecebimento. Retorno: 200 ([MSG01](#msg01)) ou 422 ([MSG02](#msg02) / [MSG03](#msg03) / [MSG09](#msg09) / [MSG11](#msg11) / [MSG13](#msg13) / [MSG14](#msg14)). | | | | |
| <a id="edp05"></a>EDP05 | PUT | [PERM03](#perm03) (`RECEITAS_EDITAR`) | /receitas/editar/{id} | N |
| Edita uma receita do usuário autenticado. Executa, nesta ordem: [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário), [RN08](#rn08) (se `RECE_ORIGEM` ≠ `MANUAL`, ignora `contaId` e qualquer mudança de origem), [RN03](#rn03) (via [C3](#c3)), [RN04](#rn04), [RN05](#rn05), [RN06](#rn06), [C4](#c4) (categoria) e persiste. Dados: competencia, nome, descricao, valor, dataLancamento, contaId, categoriaId, recebido, dataRecebimento. Retorno: 200 ([MSG04](#msg04)) ou 422 ([MSG02](#msg02) / [MSG03](#msg03) / [MSG09](#msg09) / [MSG11](#msg11) / [MSG13](#msg13) / [MSG14](#msg14)). | | | | |
| <a id="edp06"></a>EDP06 | DELETE | [PERM04](#perm04) (`RECEITAS_EXCLUIR`) | /receitas/excluir/{id} | N |
| Exclusão lógica da receita do usuário autenticado. Executa [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário) e [RN08](#rn08) (recusa se `RECE_ORIGEM` ≠ `MANUAL` → [MSG12](#msg12)). Não há checagem de uso ([RN09](#rn09)). Preenche `audit_data_exclusao` / `audit_excluido_por`. Retorno: 200 ([MSG07](#msg07)) ou 422 ([MSG12](#msg12)). | | | | |
| <a id="edp07"></a>EDP07 | PUT | [PERM05](#perm05) (`RECEITAS_REGISTRAR_RECEBIMENTO`) | /receitas/marcar-recebida/{id} | N |
| Registra o recebimento de uma receita do usuário autenticado. Executa [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário) e [RN06](#rn06). Dados: dataRecebimento. Grava `RECE_FL_RECEBIDO = TRUE` e `RECE_DT_RECEBIMENTO = dataRecebimento`. O Hibernate Envers registra a revisão. Retorno: 200 ([MSG10](#msg10)) ou 422 ([MSG02](#msg02)). | | | | |
| <a id="edp08"></a>EDP08 | POST | [PERM02](#perm02) (`RECEITAS_INSERIR`) | /receitas/duplicar | N |
| Duplica uma ou mais receitas do usuário autenticado ([RN15](#rn15)). Dados: lista de `ids`, `competenciaAlvo` (opcional). Valida a posse dos registros ([RN02](#rn02)). Gera novos registros com `RECE_ORIGEM = 'MANUAL'`, `RECE_FL_RECEBIDO = FALSE`, `RECE_DT_RECEBIMENTO = NULL`. Retorno: 200 ([MSG15](#msg15)) ou 422 ([MSG16](#msg16)). | | | | |

> O combobox de conta do modal e do filtro é alimentado pelo endpoint de opções de `CONTAS` definido no documento `06 - manter-conta` ([EDP07 daquele documento](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07)); o de categoria, pelo endpoint de opções de `CATEGORIAS` do documento `04 - manter-categoria` ([EDP07 daquele documento](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07)). Este documento não define esses endpoints.

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | Cada endpoint exige a autoridade da sua operação específica: [EDP01](#edp01) / [EDP02](#edp02) → `PERM_RECEITAS_LISTAR`; [EDP04](#edp04) / [EDP08](#edp08) → `PERM_RECEITAS_INSERIR`; [EDP03](#edp03) / [EDP05](#edp05) → `PERM_RECEITAS_EDITAR`; [EDP06](#edp06) → `PERM_RECEITAS_EXCLUIR`; [EDP07](#edp07) → `PERM_RECEITAS_REGISTRAR_RECEBIMENTO`. É terminantemente proibido o uso de permissão agregadora com sufixo `MANTER`. As autoridades são resolvidas pelo `getAuthorities()` do `Usuario` a partir do perfil e das permissões vinculadas em `PERFIL_PERMISSAO`. |
| <a id="rn02"></a>RN02 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `RECEITAS` são restritos às receitas cuja conta (`RECE.CTA_ID → CONTAS.USU_ID`) pertence ao usuário autenticado, obtido do contexto de segurança — nunca de um parâmetro da requisição. Em [EDP03](#edp03), [EDP05](#edp05), [EDP06](#edp06), [EDP07](#edp07) e [EDP08](#edp08), se alguma receita não for do usuário autenticado, o serviço responde **404** com [MSG05](#msg05), sem distinguir "não existe" de "é de outro usuário". Executa [C2](#c2). Espelha o `buscarReceitaPorUsuario()` da geração 1, que já filtrava por `instituicaoFinanceiraUsuario.usuario`. |
| <a id="rn03"></a>RN03 | `CTA_ID` é **obrigatório na aplicação** (embora anulável no schema — [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9)). Na criação ([EDP04](#edp04)) e na edição ([EDP05](#edp05)), a conta informada deve existir, estar ativa (`CTA_FL_ATIVO = TRUE`) e pertencer ao usuário autenticado — executa [C3](#c3); caso contrário → [MSG13](#msg13). A conta é a única âncora de dono da receita ([RN02](#rn02)). Não há receita sem conta: uma entrada em dinheiro vivo é lançada numa conta do tipo `CARTEIRA` (documento `06 - manter-conta`). |
| <a id="rn04"></a>RN04 | `RECE_COMPETENCIA` é obrigatória, `CHAR(7)` no formato `yyyy-MM` (validada pelo `CHECK` no banco e pelo `YearMonthConverter` no mapeamento — Documento 0, Seção 7.4). Valor fora do formato → [MSG11](#msg11). A competência é um campo **próprio e independente**: a tela a pré-preenche com o mês da data de lançamento ([RT11](#rt11)), mas o usuário a ajusta livremente e ela pode divergir do mês da data de lançamento (ex.: salário de competência `2025-12` pago em `2026-01-05`). Não há validação que force a competência a igualar o mês da data de lançamento. **[Requer código]** |
| <a id="rn05"></a>RN05 | `RECE_VALOR` é obrigatório, `DECIMAL(15,2)`, e deve ser **maior que zero** — valor ausente, zero ou negativo → [MSG03](#msg03). Não há regra de unicidade (nome/valor/competência podem repetir). |
| <a id="rn06"></a>RN06 | **Prevista × recebida.** `RECE_FL_RECEBIDO` (*default* `FALSE`). Quando `TRUE`, `RECE_DT_RECEBIMENTO` é **obrigatória** — ausente → [MSG09](#msg09). Quando `FALSE`, o serviço **limpa** `RECE_DT_RECEBIMENTO`. O comportamento (exigir data ao marcar; limpar ao desmarcar) é parametrizável ([Seção 12](#12-parâmetros-de-sistema)). O efeito de `RECE_FL_RECEBIDO` no saldo por competência e no Dashboard é do documento `13`. |
| <a id="rn07"></a>RN07 | `RECE_ORIGEM` das receitas criadas por esta tela é **sempre `MANUAL`** — o serviço fixa o valor em [EDP04](#edp04) e [EDP08](#edp08), ignorando qualquer origem recebida no corpo. `OPEN_FINANCE` e `IMPORTACAO` só chegam pela conciliação (documento `15`) ou por rotina de importação. |
| <a id="rn08"></a>RN08 | **Receita importada** (`RECE_ORIGEM` ≠ `MANUAL`): na edição ([EDP05](#edp05)), `CTA_ID` e `RECE_ORIGEM` **não** são alteráveis — qualquer valor divergente é ignorado. Na exclusão ([EDP06](#edp06)), a operação é **recusada** → [MSG12](#msg12), porque a transação de *staging* de Open Finance guarda um vínculo lógico com a receita gerada (Documento 0, Observações 19 e 21) e excluí-la aqui deixaria esse vínculo pendente — a gestão dessas receitas é dos documentos `14`/`15`. Os demais campos de negócio (nome, descrição, valor, competência, categoria, recebido, data de recebimento) **são** editáveis. |
| <a id="rn09"></a>RN09 | Exclusão de receita ([EDP06](#edp06)): é sempre **lógica** (preenche `audit_data_exclusao` / `audit_excluido_por`) e **não checa uso** — nenhuma tabela do domínio tem FK para `RECEITAS` (o vínculo do *staging* de Open Finance é lógico, não físico — Documento 0, Observação 21). A única restrição é a da [RN08](#rn08). |
| <a id="rn10"></a>RN10 | `RECEITAS` é auditada via Hibernate Envers (`@Audited`). Cada criação, edição, registro de recebimento, duplicação e exclusão lógica gera uma revisão com autor e data. |
| <a id="rn11"></a>RN11 | **Mudanças de estrutura vs geração 1 (requer código):** os enums `RECE_TIPO_TRANSACAO` (`TipoRegistroFinanceiro`) e `RECE_TIPO_RECEITA_DESPESA` (`CategoriaRegistroFinanceiro`) são removidos da entidade `Receita`; a categoria passa a ser `@ManyToOne Categoria` (`CATE_ID`). `RECE_DESCRICAO` deixa de ser obrigatória. `dataLancamento` passa de `Instant` para `LocalDate`. `competencia` passa de `String` (`"0000-00"`) para `YearMonth` (via `YearMonthConverter`). Entram `recebido` (`boolean`), `dataRecebimento` (`LocalDate`) e `origem` (`OrigemLancamento`). `Receita` passa a herdar de `LancamentoFinanceiro` (Documento 0, Seção 7.2). **[Requer código]** |
| <a id="rn12"></a>RN12 | **Datas podem ser futuras.** `RECE_DT_LANCAMENTO` e `RECE_DT_RECEBIMENTO` aceitam datas posteriores a hoje — a tela serve para planejar receitas previstas para os meses seguintes (salário do mês que vem, recebimento agendado). O serviço **não** rejeita data futura; a única validação de data é o formato ([RN04](#rn04) para a competência). |
| <a id="rn13"></a>RN13 | **Filtro padrão e competências passadas.** A tela inicializa o filtro da listagem com `mes_atual - 1` na competência inicial e final. O sistema aceita qualquer competência passada sem travas, alertas impeditivos ou restrições retroativas. |
| <a id="rn14"></a>RN14 | **Totalizador por competência única.** O card totalizador exibe o somatório das receitas se e somente se o filtro ativo de competência inicial for estritamente igual ao de competência final (`competenciaInicio == competenciaFim`). Havendo intervalo de múltiplos meses ou sem filtro de competência, o card permanece invisível. |
| <a id="rn15"></a>RN15 | **Duplicação de receitas.** A duplicação individual ou em lote ([EDP08](#edp08)) cria novas receitas copiando nome, descrição, valor, conta e categoria dos registros selecionados. As cópias são geradas obrigatoriamente com `RECE_ORIGEM = 'MANUAL'`, `RECE_FL_RECEBIDO = FALSE` (situação "Prevista") e `RECE_DT_RECEBIMENTO = NULL`. A competência dos novos registros será a `competenciaAlvo` especificada ou a competência da receita de origem. |
| <a id="rn16"></a>RN16 | **Preservação de filtros ativos.** Após qualquer operação cadastral ou de mutação (inclusão, edição, exclusão, recebimento, duplicação), a listagem deve ser atualizada preservando os filtros aplicados e o posicionamento da página no DataTables. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Receita cadastrada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Informe um valor maior que zero. |
| <a id="msg04"></a>MSG04 | Receita atualizada com sucesso. |
| <a id="msg05"></a>MSG05 | Receita não encontrada. |
| <a id="msg06"></a>MSG06 | Confirma a exclusão da receita "{nome}"? |
| <a id="msg07"></a>MSG07 | Receita excluída com sucesso. |
| <a id="msg08"></a>MSG08 | Nenhuma receita encontrada com os filtros informados. |
| <a id="msg09"></a>MSG09 | Informe a data de recebimento para marcar a receita como recebida. |
| <a id="msg10"></a>MSG10 | Recebimento registrado com sucesso. |
| <a id="msg11"></a>MSG11 | A competência deve estar no formato AAAA-MM. |
| <a id="msg12"></a>MSG12 | Esta receita foi importada do Open Finance e não pode ser excluída por esta tela. |
| <a id="msg13"></a>MSG13 | A conta selecionada não está disponível. Escolha uma conta ativa. |
| <a id="msg14"></a>MSG14 | A categoria selecionada não está disponível. |
| <a id="msg15"></a>MSG15 | Receita(s) duplicada(s) com sucesso. |
| <a id="msg16"></a>MSG16 | Selecione ao menos uma receita para duplicar. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem das receitas do usuário autenticado para o grid, com a conta e a categoria ([EDP02](#edp02)).<br>`SELECT r.RECE_ID, r.RECE_COMPETENCIA, r.RECE_NOME, r.RECE_DESCRICAO, r.RECE_VALOR,`<br>`       r.RECE_DT_LANCAMENTO, r.RECE_DT_RECEBIMENTO, r.RECE_FL_RECEBIDO, r.RECE_ORIGEM,`<br>`       c.CTA_ID, c.CTA_DESCRICAO,`<br>`       cat.CATE_ID, cat.CATE_NOME,`<br>`       (r.audit_data_exclusao IS NOT NULL) AS excluido`<br>`FROM RECEITAS r`<br>`JOIN CONTAS c        ON c.CTA_ID = r.CTA_ID`<br>`LEFT JOIN CATEGORIAS cat ON cat.CATE_ID = r.CATE_ID`<br>`WHERE c.USU_ID = :usuId`<br>`ORDER BY r.RECE_COMPETENCIA DESC, r.RECE_DT_LANCAMENTO DESC;` |
| <a id="c2"></a>C2 | Verifica se a receita pertence ao usuário autenticado ([RN02](#rn02)) — usada antes de editar, registrar o recebimento ou excluir.<br>`SELECT COUNT(*) FROM RECEITAS r`<br>`JOIN CONTAS c ON c.CTA_ID = r.CTA_ID`<br>`WHERE r.RECE_ID = :receId`<br>`  AND c.USU_ID = :usuId`<br>`  AND r.audit_data_exclusao IS NULL;` |
| <a id="c3"></a>C3 | Verifica se a conta informada existe, está ativa e é do usuário autenticado ([RN03](#rn03)).<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.CTA_ID = :ctaId`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`  AND c.audit_data_exclusao IS NULL;` |
| <a id="c4"></a>C4 | Verifica se a categoria informada está ativa e se aplica a receita (validação de categoria → [MSG14](#msg14)).<br>`SELECT COUNT(*) FROM CATEGORIAS cat`<br>`WHERE cat.CATE_ID = :cateId`<br>`  AND cat.CATE_FL_ATIVO = TRUE`<br>`  AND cat.CATE_APLICA_A IN ('RECEITA', 'AMBOS')`<br>`  AND cat.audit_data_exclusao IS NULL;` |

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| RECEITA_COMPETENCIA_SEGUE_DATA_LANCAMENTO | true | Se `true`, a Competência do modal ([ID6](#qdd3-6)) é pré-preenchida e reajustada pelo mês da Data de lançamento enquanto o usuário não a editar à mão ([RT11](#rt11)). Se `false`, a Competência começa no mês corrente e nunca é reajustada automaticamente. |
| RECEITA_RECEBIMENTO_EXIGE_DATA | true | Se `true`, marcar `RECE_FL_RECEBIDO = TRUE` exige `RECE_DT_RECEBIMENTO` ([RN06](#rn06) / [MSG09](#msg09)). Se `false`, a data de recebimento fica opcional. |
| RECEITA_DESMARCAR_RECEBIDA_LIMPA_DATA | true | Se `true`, ao voltar `RECE_FL_RECEBIDO` para `FALSE` o serviço limpa `RECE_DT_RECEBIMENTO` ([RN06](#rn06)). Se `false`, a data é mantida como histórico. |

---

## 13. Permissões

Cinco permissões do módulo **Receitas** (`PERM_MODULO = 'Receitas'`). Fazem parte do catálogo do código e da carga inicial (Documento 0, [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26) e Observação 28). Convenção domínio-primeiro; cada uma vira a autoridade `PERM_{CÓDIGO}`. É proibido o uso de permissão agregadora com sufixo `MANTER`.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `RECEITAS_LISTAR` — abrir a tela Receitas, listar e filtrar as próprias receitas. Controla a visibilidade do menu 'Receitas'. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm02"></a>PERM02 | `RECEITAS_INSERIR` — cadastrar novas receitas e duplicar receitas para si próprio. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm03"></a>PERM03 | `RECEITAS_EDITAR` — alterar dados de receitas existentes do próprio usuário. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm04"></a>PERM04 | `RECEITAS_EXCLUIR` — excluir logicamente receitas manuais do próprio usuário. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm05"></a>PERM05 | `RECEITAS_REGISTRAR_RECEBIMENTO` — registrar o recebimento de receitas previstas. | [PERF01](#perf01), [PERF02](#perf02) |

> Estas permissões e seus vínculos vêm de carga inicial e são concedidas a **ADMIN e USER** — a tela é do próprio usuário. O que cada usuário enxerga e altera é limitado às suas receitas pela [RN02](#rn02), não pela permissão.

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `RECEITAS_LISTAR` | ✓ | ✓ |
| `RECEITAS_INSERIR` | ✓ | ✓ |
| `RECEITAS_EDITAR` | ✓ | ✓ |
| `RECEITAS_EXCLUIR` | ✓ | ✓ |
| `RECEITAS_REGISTRAR_RECEBIMENTO` | ✓ | ✓ |

Tanto `ADMIN` quanto `USER` recebem as cinco permissões na carga inicial: cada usuário gerencia as suas próprias receitas. O `ADMIN` **não** tem visão administrativa das receitas de outros usuários — dado financeiro é privado do dono, e o escopo por `USU_ID` da [RN02](#rn02) vale igual para os dois perfis.

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. `PERF_FL_SISTEMA = TRUE`. Recebe todas as permissões na carga inicial, inclusive as cinco de Receitas (Seção 13.1). Opera apenas sobre as próprias receitas ([RN02](#rn02)). Corresponde a `ROLE_ADMIN`. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum. `PERF_FL_SISTEMA = TRUE`. Recebe `RECEITAS_LISTAR`, `RECEITAS_INSERIR`, `RECEITAS_EDITAR`, `RECEITAS_EXCLUIR` e `RECEITAS_REGISTRAR_RECEBIMENTO` na carga inicial e gerencia as próprias receitas. Corresponde a `ROLE_USER`. |

---

## 15. Fluxo de Eventos

**Cadastrar uma receita prevista e depois registrar o recebimento:**

```
1. Usuário clica em "Nova receita" → modal QUADRO_DESCRITIVO_3 em modo criação.
        Data de lançamento ← hoje    Competência ← mês corrente    Recebido ← "Não".
2. Usuário informa nome, valor, conta e (opcional) categoria, mantém Recebido em "Não",
   clica em "Salvar" → EDP04.
        │
        ├─ Valor ausente / zero / negativo (RN05)     → MSG03.
        ├─ Conta ausente / inativa / de outro usuário
        │  (RN03 / C3)                                → MSG13.
        ├─ Competência fora do formato (RN04)          → MSG11.
        └─ OK → fixa RECE_ORIGEM = MANUAL, persiste, audita (Envers),
                 retorna MSG01 e o grid é recarregado. Situação: "Prevista".
3. Mais tarde, o dinheiro entra. Usuário clica no ícone "Registrar recebimento"
   da linha → modal QUADRO_DESCRITIVO_4, Data de recebimento ← hoje.
4. Usuário confirma a data, clica em "Registrar" → EDP07.
        │
        ├─ Receita de outro usuário (RN02 / C2)        → MSG05 (404).
        ├─ Data de recebimento vazia (RT09)            → MSG02.
        └─ OK → grava RECE_FL_RECEBIDO = TRUE e RECE_DT_RECEBIMENTO,
                 audita (Envers), retorna MSG10, recarrega o grid.
                 Situação: "Recebida".
```

**Excluir uma receita:**

```
1. Usuário clica no ícone Excluir de uma linha do grid.
        (O ícone não aparece quando a receita veio do Open Finance — RN08.)
2. Sistema exibe a confirmação MSG06.
3. Usuário confirma → chama EDP06.
        │
        ├─ Receita de outro usuário (RN02 / C2)        → MSG05 (404), nada muda.
        ├─ Receita RECE_ORIGEM ≠ MANUAL (RN08)         → MSG12, nada muda.
        └─ OK → preenche audit_data_exclusao / audit_excluido_por,
                 audita (Envers), retorna MSG07 e o grid é recarregado.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Listar minhas receitas

Dado que estou autenticado com um usuário que tem a permissão [PERM01](#perm01).
E que tenho cinco receitas vinculadas às minhas contas.
Quando eu acessar o menu "Finanças > Receitas".
Então o sistema deve exibir o grid com as minhas cinco receitas, ordenadas por competência decrescente, mostrando a categoria, a conta, o valor, a data de lançamento, a situação e a origem.

### 16.1 Bloquear acesso de usuário sem a permissão

Dado que estou autenticado com um usuário de um perfil que não tem [PERM01](#perm01).
Quando eu tentar acessar "/receitas/listar" ou chamar "/receitas/listar-dados".
Então o sistema deve negar o acesso (HTTP 403).

### 16.2 A listagem traz só as receitas do próprio usuário

Dado que o usuário A tem três receitas e o usuário B tem duas receitas.
Quando o usuário A abrir a tela Receitas.
Então o grid deve mostrar apenas as três receitas do usuário A.

### 16.3 Acesso cruzado a buscar/{id} é negado

Dado que estou autenticado como usuário A.
E que existe a receita com id 70 vinculada a uma conta do usuário B.
Quando eu chamar "/receitas/buscar/70".
Então o sistema deve responder 404 com [MSG05](#msg05), sem revelar que a receita existe.

### 16.4 Acesso cruzado a editar/{id}, marcar-recebida/{id} e excluir/{id} é negado

Dado que estou autenticado como usuário A.
E que a receita com id 70 é do usuário B.
Quando eu chamar "/receitas/editar/70", "/receitas/marcar-recebida/70" ou "/receitas/excluir/70".
Então o sistema deve responder 404 com [MSG05](#msg05) e não alterar a receita do usuário B.

### 16.5 Cadastrar receita prevista

Dado que estou na tela Receitas com [PERM01](#perm01) e [PERM02](#perm02) e clico em "Nova receita".
Quando eu informar o nome "Salário", o valor "5.000,00", a data de lançamento "05/09/2026", a conta "Nubank Conta Corrente", a categoria "Salário", deixar Recebido em "Não" e clicar em "Salvar".
Então o sistema deve criar a receita com `RECE_ORIGEM = MANUAL`, competência "2026-09", situação "Prevista", exibir [MSG01](#msg01) e recarregar o grid.

### 16.6 Valor deve ser maior que zero

Dado que estou cadastrando uma receita.
Quando eu informar o valor "0,00" e clicar em "Salvar".
Então o sistema deve impedir e exibir [MSG03](#msg03).

### 16.7 Competência pré-preenchida pela data de lançamento

Dado que o parâmetro RECEITA_COMPETENCIA_SEGUE_DATA_LANCAMENTO está em "true" e abri o modal de nova receita.
E que ainda não editei o campo Competência.
Quando eu informar a data de lançamento "10/12/2026".
Então o campo Competência deve passar a "12/2026".

### 16.8 Marcar como recebida exige a data de recebimento

Dado que estou editando uma receita prevista.
Quando eu ligar o toggle "Recebido" e clicar em "Salvar" sem informar a data de recebimento.
Então o sistema deve impedir e exibir [MSG09](#msg09).

### 16.9 Desmarcar recebida limpa a data

Dado que a minha receita "Freela" está recebida, com data de recebimento "20/08/2026".
E que o parâmetro RECEITA_DESMARCAR_RECEBIDA_LIMPA_DATA está em "true".
Quando eu editar a receita, desligar o toggle "Recebido" e salvar.
Então o sistema deve gravar `RECE_FL_RECEBIDO = FALSE` e `RECE_DT_RECEBIMENTO` nulo.

### 16.10 Registrar recebimento pela ação do grid

Dado que a minha receita "Salário" está prevista.
Quando eu clicar no ícone "Registrar recebimento", confirmar a data "05/10/2026" e clicar em "Registrar".
Então o sistema deve gravar `RECE_FL_RECEBIDO = TRUE` e `RECE_DT_RECEBIMENTO = 2026-10-05`, exibir [MSG10](#msg10) e recarregar o grid com a situação "Recebida".

### 16.11 Conta obrigatória

Dado que estou cadastrando uma receita.
Quando eu tentar salvar sem escolher uma conta.
Então o sistema deve impedir e exibir [MSG02](#msg02) no campo Conta.

### 16.12 Combobox de conta traz só as contas ativas do próprio usuário

Dado que eu tenho as contas "Nubank" (ativa) e "Conta Antiga" (inativa), e o usuário B tem a conta "Itaú" (ativa).
Quando o modal de nova receita carregar o combobox de conta para o meu usuário.
Então deve aparecer "Nubank", e não "Conta Antiga" nem "Itaú".

### 16.13 Excluir receita manual

Dado que a minha receita "Reembolso" tem `RECE_ORIGEM = MANUAL`.
Quando eu excluí-la e confirmar.
Então o sistema deve fazer a exclusão lógica, exibir [MSG07](#msg07) e recarregar o grid, sem checar uso.

### 16.14 Receita do Open Finance não é excluível pela tela

Dado que a minha receita "PIX recebido" tem `RECE_ORIGEM = OPEN_FINANCE`.
Quando eu abri-la em edição.
Então o ícone de excluir não deve aparecer e os campos Conta e Origem devem estar desabilitados.
E se a exclusão for chamada diretamente, o sistema deve responder [MSG12](#msg12).

### 16.15 Editar campos de negócio de uma receita do Open Finance

Dado que a minha receita "PIX recebido" tem `RECE_ORIGEM = OPEN_FINANCE`.
Quando eu alterar a categoria e o nome e salvar.
Então o sistema deve gravar a nova categoria e o novo nome, mantendo a conta e a origem inalteradas.

### 16.16 Filtrar por competência e situação

Dado que tenho receitas nas competências de 07/2026 a 10/2026, previstas e recebidas.
Quando eu filtrar por competência inicial "08/2026", competência final "09/2026" e situação "Recebida".
Então o grid deve mostrar apenas as receitas recebidas com competência entre 08/2026 e 09/2026.

### 16.17 Auditoria da receita

Dado que eu registro o recebimento de uma receita e salvo.
Quando eu consultar a auditoria de `RECEITAS`.
Então deve haver o registro de quem alterou e quando, com o valor anterior e o novo de `RECE_FL_RECEBIDO` e `RECE_DT_RECEBIMENTO`.

### 16.18 ADMIN também só vê as próprias receitas

Dado que estou autenticado como ADMIN.
E que existem receitas de outros usuários.
Quando eu abrir a tela Receitas.
Então o grid deve mostrar apenas as minhas receitas, não as dos outros usuários.

### 16.19 Competência diferente do mês da data de lançamento

Dado que estou cadastrando uma receita e o campo Competência foi pré-preenchido com "01/2026".
Quando eu ajustar a Competência para "12/2025", manter a data de lançamento "05/01/2026" e salvar.
Então o sistema deve gravar `RECE_COMPETENCIA = '2025-12'` e `RECE_DT_LANCAMENTO = 2026-01-05`, sem forçar a competência a igualar o mês da data de lançamento.

### 16.20 Lançar receita prevista com data futura

Dado que hoje é 08/09/2026 e estou cadastrando uma receita.
Quando eu informar a data de lançamento "01/10/2026", a competência "10/2026", deixar Recebido em "Não" e salvar.
Então o sistema deve aceitar o cadastro com a data futura e situação "Prevista".

### 16.21 Filtro padrão inicial e totalizador por competência única

Dado que estou autenticado e acesso a tela "Finanças > Receitas" no mês 09/2026.
Quando a página for carregada inicialmente.
Então os campos de competência inicial e final devem estar pré-selecionados com "08/2026" (`mes_atual - 1`).
E o card totalizador deve ser exibido com a soma das receitas ativas da competência "08/2026".

### 16.22 Totalizador oculto em intervalo de múltiplas competências

Dado que estou na tela de receitas.
Quando eu abrir o modal de filtro e selecionar competência inicial "05/2026" e competência final "08/2026".
E aplicar o filtro.
Então o grid deve listar as receitas do intervalo e o card totalizador da competência deve ficar oculto.

### 16.23 Duplicar receita individualmente

Dado que possuo a receita "Freela Design" na competência "08/2026" no valor de "800,00".
Quando eu clicar na ação "Duplicar" da linha correspondente e confirmar.
Então o sistema deve criar uma nova receita com o mesmo nome, valor, conta e categoria, com situação "Prevista", data de recebimento nula, origem "MANUAL", exibir [MSG15](#msg15) e recarregar o grid preservando a página e filtros.

### 16.24 Duplicar receitas em lote

Dado que possuo 3 receitas listadas no grid.
Quando eu selecionar as 3 receitas por meio dos checkboxes e clicar no botão "Duplicar selecionadas".
E confirmar a operação.
Então o sistema deve criar 3 novas receitas como "Previstas", exibir [MSG15](#msg15) e recarregar o grid mantendo os filtros ativos.

### 16.25 Preservação de filtros após edição

Dado que filtrei as receitas por busca textual "Consultoria" na página 2 do grid.
Quando eu editar o valor de uma das receitas e clicar em "Salvar".
Então o sistema deve salvar a alteração, recarregar os dados do grid mantendo o filtro "Consultoria" e permanecer na página 2.

---

## 17. Workshop de Análise

Data: —
Convidados: Diego Cordeiro
Participantes: Diego Cordeiro
Descrição: Levantamento a partir do Documento 0 ([QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9); Observações 9, 13, 18, 19, 21 e 28), do CRUD REST de `Receita` da geração 1 (`dsc-backend` — `domain/Receita.java`, `controller/ReceitaController.java`, `services/ReceitaService.java`) e do padrão de tela dos documentos `06 - manter-conta` (escopo *row-level* por usuário, forma e voz) e `04 - manter-categoria`.

**Decisões tomadas:**
- A tela **Receitas** vive em "Finanças > Receitas" e é acessível a `ADMIN` e `USER`; cada um opera só sobre as próprias receitas ([RN02](#rn02)).
- `RECEITAS` **não tem `USU_ID`**; o dono é o `USU_ID` da conta (`RECE.CTA_ID → CONTAS.USU_ID`). O escopo por usuário (*row-level*) é resolvido no serviço, a partir do contexto de segurança — nunca de um parâmetro. Acesso cruzado por `{id}` responde 404 ([MSG05](#msg05)), sem revelar a existência do registro.
- **Toda receita exige uma conta na aplicação** ([RN03](#rn03)), ainda que `RECE.CTA_ID` seja anulável no schema — a conta é a única âncora de dono e de escopo, como já era na geração 1 (`INFU_ID` sempre preenchido na prática). A conta deve estar ativa e pertencer ao usuário. **O Documento 0 não é alterado** e `RECEITAS` não ganha `USU_ID`.
- **Dinheiro em espécie = conta do tipo `CARTEIRA`.** Não existe "lançamento sem conta": para registrar uma entrada em dinheiro vivo ou receita informal, o usuário usa uma conta `CARTEIRA` (`TipoConta.CARTEIRA`, já previsto no documento `06 - manter-conta`), o que mantém dono e rastreabilidade pela conta. Descarta a alternativa de acrescentar `USU_ID` ao `LancamentoFinanceiro`.
- Permissões atômicas `RECEITAS_LISTAR`, `RECEITAS_INSERIR`, `RECEITAS_EDITAR`, `RECEITAS_EXCLUIR` e `RECEITAS_REGISTRAR_RECEBIMENTO` (domínio-primeiro, módulo "Receitas") concedidas a `ADMIN` e `USER` na carga inicial. Proibição expressa de permissões agregadoras com sufixo `MANTER`.
- **O `ADMIN` não tem visão administrativa de dado financeiro de terceiros — nem nesta versão nem no roadmap.** Opera exatamente como um `USER`, sobre as próprias receitas. Não haverá permissão do tipo `RECEITAS_ADMINISTRAR_TODAS`.
- **Filtro inicial padrão e competências passadas:** a tela abre filtrando por padrão `mes_atual - 1` e aceita qualquer competência passada sem travas ou avisos impeditivos ([RN13](#rn13)).
- **Totalizador por competência única:** exibido exclusivamente quando `competenciaInicio == competenciaFim`, consolidando o total de receitas ativas daquele mês ([RN14](#rn14)).
- **Duplicação de receitas:** individual e em lote, clonando os dados e gerando novos registros manuais com situação "Prevista" ([RN15](#rn15)).
- **Preservação de filtros e paginação:** recarga suave do grid após mutações mantendo busca, filtros e página atual ([RN16](#rn16)).
- **Máscara monetária contínua:** campos de valor formatados em tempo real com máscara client-side (`pt-BR`, `R$ 0,00`).
- **Prevista × recebida:** `RECE_FL_RECEBIDO` (*default* `FALSE`) mais `RECE_DT_RECEBIMENTO`. Ao marcar como recebida, a data de recebimento é exigida (*default* hoje); ao desmarcar, a data é limpa ([RN06](#rn06)). Há uma ação dedicada no grid ("Registrar recebimento", [EDP07](#edp07)) além do toggle no modal.
- **Origem:** receitas criadas por esta tela são sempre `MANUAL` — o serviço fixa o valor ([RN07](#rn07)). `OPEN_FINANCE` / `IMPORTACAO` só chegam pela conciliação (documento `15`).
- **Receita importada do Open Finance** (`RECE_ORIGEM` ≠ `MANUAL`) — **edição parcial**: editável nos campos de negócio (nome, descrição, valor, competência, categoria, recebido, data de recebimento), com **conta e origem bloqueadas** e **sem exclusão** por esta tela ([RN08](#rn08) / [MSG12](#msg12)) — o vínculo lógico com a transação de *staging* (Documento 0, Observações 19 e 21) pertence aos documentos `14`/`15`.
- **Competência — campo próprio e independente:** `YearMonth` (via `YearMonthConverter`), apenas pré-preenchido com o mês da data de lançamento por conveniência e reajustado enquanto o usuário não o editar ([RT11](#rt11) / parâmetro `RECEITA_COMPETENCIA_SEGUE_DATA_LANCAMENTO`). Pode divergir do mês da data de lançamento (ex.: salário de `2025-12` pago em `2026-01-05`). Não deriva da data.
- **Datas podem ser futuras** ([RN12](#rn12)): `RECE_DT_LANCAMENTO` e `RECE_DT_RECEBIMENTO` aceitam data posterior a hoje, para planejar receitas previstas dos meses seguintes. Sem validação que bloqueie data futura.
- **Valor:** obrigatório e maior que zero ([RN05](#rn05)).
- **Exclusão lógica**, sem trava de "em uso" — a receita é folha do domínio, nada tem FK para ela ([RN09](#rn09)).
- **Categoria opcional**, combobox de `CATEGORIAS` com `aplicaA=RECEITA` (documento `04`); **conta**, combobox de `CONTAS` ativas do usuário (documento `06`). Este documento não redefine esses endpoints.
- Grid client-side; sem carga inicial (a tabela nasce vazia).
- Enums `RECE_TIPO_TRANSACAO` e `RECE_TIPO_RECEITA_DESPESA` da geração 1 removidos; `RECE_DESCRICAO` passa a opcional; `RECE_DT_LANCAMENTO` passa a `LocalDate` ([RN11](#rn11)).

**A Confirmar:**
- **Conta `CARTEIRA` semeada ou manual (decisão do documento `06 - manter-conta`):** o documento `06` deve **semear automaticamente** uma conta "Carteira / Dinheiro em espécie" para cada usuário na criação da conta, ou o usuário cria essa conta manualmente quando precisar lançar dinheiro vivo? Sinalizado aqui apenas como dependência — a decisão é do documento `06`.
- **Desmarcar "recebida":** a v1.0 limpa `RECE_DT_RECEBIMENTO` (parâmetro `RECEITA_DESMARCAR_RECEBIDA_LIMPA_DATA`). Confirmar se a data deve ser mantida como histórico quando a receita volta a "prevista".
- **Receita recorrente (salário todo mês):** a tela deve gerar as ocorrências futuras a partir de um modelo, ou toda receita é sempre um lançamento avulso? **Fora do escopo da v1.0** — registrado aqui para um módulo/versão futura.
- **Valor negativo / estorno:** a v1.0 exige valor > 0. Confirmar se há caso de uso para estorno de receita (valor negativo) ou se isso deve ser tratado como uma despesa.
- **Filtro de competência:** o filtro por intervalo (competência inicial / final) atende, ou o padrão deve ser um seletor de competência única com navegação mês a mês (como o Dashboard)? Confirmar também se a tela deve abrir já filtrada pela competência corrente.
- **Volume e paginação:** se o número de receitas por usuário crescer muito, o grid client-side deve passar a recortar por competência no servidor ([RNF05](#rnf05))?

---

## 18. Anexos

- **Protótipo e diagramas (v1.0):** gerados. Casos de uso (`prototipo/manter-receita-casos-uso.drawio` + `images/manter-receita-casos-uso.png`), DER do subconjunto (`prototipo/manter-receita-der.drawio` + `images/manter-receita-der.png`), wireframes das quatro telas/modais (`prototipo/manter-receita-prototipo.drawio` + `images/mr-tela-1..4.png`) e protótipo navegável (`prototipo/manter-receita-prototipo.html`). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright); diagramas `.drawio` por `prototipo/gen-diagramas.py`.
- Documento 0 — Fundação: `../00 - analise-geral/documento-0-fundacao.md` ([QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2), [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3), [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), [QUADRO_DESCRITIVO_20](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-20), [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26); Seções 7.2 e 7.4).
- Documento `04 - manter-categoria`: `../04 - manter-categoria/documento-analise-manter-categoria.md` — fonte do endpoint de opções de categoria (`GET /categorias/opcoes?aplicaA=RECEITA`).
- Documento `06 - manter-conta`: `../06 - manter-conta/documento-analise-manter-conta.md` — fonte do endpoint de opções de conta (`GET /contas/opcoes`), padrão de escopo *row-level* por usuário, forma e voz.
- Documentos `13 - dashboard`, `14 - open-finance-conectar-conta` e `15 - open-finance-conciliacao` (a escrever) — efeito de `RECE_FL_RECEBIDO` no saldo e gestão das receitas de origem `OPEN_FINANCE`.
- Código de referência geração 1: `dsc-backend` (`domain/Receita.java`, `controller/ReceitaController.java`, `services/ReceitaService.java`, `enums/TipoRegistroFinanceiro.java`, `enums/CategoriaRegistroFinanceiro.java`).
