# dscproject — Análise de Sistemas
## Módulo Transações Bancárias — USER / ADMIN — Manter Transação Bancária

**Gerado em:** 15/09/2026  
**Atualizado em:** 17/09/2026  
**Versão:** 1.0  
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
| 1.0 | 15/09/2026 | Diego dos Santos Cordeiro | Criação do documento. CRUD das **transações bancárias do próprio usuário** (tela "Finanças > Transações Bancárias") para a geração 2 — sucessor do CRUD REST de `TransacaoBancaria` da geração 1 (`dsc-backend`), agora sobre a tabela `TRANSACOES_BANCARIAS` ([QUADRO_DESCRITIVO_7 do Documento 0](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7)). Introduz a separação explícita da natureza do movimento (`TRBA_NATUREZA_MOVIMENTO` com enum `NaturezaMovimento`: `CREDITO` / `DEBITO`), a competência como `YearMonth`, a identificação de quitação de fatura (`TRBA_FL_PAGAMENTO_FATURA`), a origem do lançamento (`TRBA_ORIGEM`), a deduplicação de importação por `TRBA_ID_EXTERNO` e o resgate da importação de arquivos bancários OFX via modal dedicado. Escopo *row-level* por usuário, derivado da conta vinculada (`CTA_ID → CONTAS.USU_ID`). Este documento **referencia** o QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |
| D04 | `TRANSACOES_BANCARIAS` não tem `USU_ID` próprio ([QUADRO_DESCRITIVO_7](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7)); o dono da transação é o `USU_ID` da conta vinculada (`TRBA.CTA_ID → CONTAS.USU_ID`). O escopo por usuário (*row-level*) é sempre resolvido **no serviço**, a partir do contexto de segurança — nunca de um parâmetro da requisição. |
| D05 | A importação de extrato bancário no padrão OFX da geração 1 (`dsc-backend`: `AggregateUnmarshaller` da biblioteca ofx4j) é incorporada neste documento via modal na listagem e endpoint de importação (`POST /transacoes-bancarias/importar-ofx`), gerando registros com `TRBA_ORIGEM = 'IMPORTACAO'`, preenchendo `TRBA_ID_EXTERNO` com o identificador da transação bancária (`fitid`) para assegurar idempotência e impedir duplicidades na mesma conta. |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Transação Bancária** do `dscproject-spring-mvc` — o registro e o acompanhamento de entradas (créditos) e saídas (débitos) ocorridos diretamente nas **contas bancárias do próprio usuário** (conta corrente, poupança, conta de pagamento ou carteira).

Na **geração 1** (API REST + SPA Angular), isto é o CRUD REST de `TransacaoBancaria` (`dsc-backend`): `GET /transacoes-bancarias` devolve **apenas as transações do usuário autenticado** (`buscarTodosPorUsuario()` — resolve o usuário pelo token JWT e filtra por `instituicaoFinanceiraUsuario.usuario`), com `inserir`, `editar`, `excluir` e `importarDadosBancariosOfx`. A entidade guardava descrição, valor, data de lançamento (`java.util.Date`), `ofxTransacaoId`, o tipo de registro (enum `TipoRegistroFinanceiro` com problema de duplicidade de código `'D'`), a categoria (enum `CategoriaRegistroFinanceiro`) e a conta (`INFU_ID`). Não havia suporte a competência formal, nem separação formal de natureza de fluxo, nem rastreabilidade de pagamentos de fatura.

O **Documento 0** ([QUADRO_DESCRITIVO_7](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7), Observações 8, 9, 13, 16, 18 e 28) travou a reescrita da tabela `TRANSACOES_BANCARIAS` para a geração 2:

- o enum defeituoso da geração 1 é substituído por **`TRBA_NATUREZA_MOVIMENTO`** (`VARCHAR(10)`, domínio `CREDITO` / `DEBITO` com `CHECK`), mapeado no enum Java `NaturezaMovimento`;
- a categoria deixa de ser enum e passa a ser `CATE_ID` (FK opcional para `CATEGORIAS`, do documento `2.01 - manter-categoria`);
- `TRBA_DT_LANCAMENTO` passa de `java.util.Date` para `LocalDate`;
- a competência (`TRBA_COMPETENCIA`) entra como `CHAR(7)` no formato `yyyy-MM`, com `CHECK` de formato, mapeada em Java como `java.time.YearMonth` via `YearMonthConverter` (Documento 0, Seção 7.4);
- entra o campo **`TRBA_FL_PAGAMENTO_FATURA`** (`BOOLEAN`, *default* `FALSE`), que sinaliza que o débito em conta foi gerado para pagar uma fatura de cartão de crédito (evitando dupla contagem de gastos em relatórios consolidados);
- entra o campo **`TRBA_ID_EXTERNO`** (`VARCHAR(120)`, único), que substitui o antigo `TRBA_OFX_TRANSACAO_ID`, garantindo a deduplicação tanto de importações OFX quanto de conciliações via Open Finance;
- entra o campo **`TRBA_ORIGEM`** (`VARCHAR(20)`, domínio `MANUAL` / `OPEN_FINANCE` / `IMPORTACAO`, *default* `MANUAL`);
- `TransacaoBancaria` passa a herdar da superclasse `LancamentoFinanceiro` (`@MappedSuperclass`, Documento 0, Seção 7.2), compartilhando atributos com `Receita` e `Despesa`.

Este documento cobre:

- a **tela Transações Bancárias** — listar, filtrar, cadastrar, editar, excluir logicamente e duplicar transações da conta;
- a **importação de extratos bancários em formato OFX** (Open Financial Exchange), com associação à conta de destino e prevenção de transações repetidas via `TRBA_ID_EXTERNO`;
- os **totalizadores de competência** (total de créditos, total de débitos e saldo líquido movimentado);
- a rastreabilidade e proteção contra exclusão indevida de transações atreladas à quitação de faturas de cartão de crédito (`FATURAS_CARTAO.TRBA_ID_PAGAMENTO`);
- o **escopo por usuário**: um usuário nunca visualiza nem opera sobre transações de contas pertencentes a outro usuário.

**Escopo deste documento:**
- Tela de **listagem das transações bancárias do usuário** (grid client-side), com filtro padrão inicial pela competência de `mes_atual - 1` e modal de filtros avançados (conta, competência, natureza, categoria, busca textual).
- **Cards totalizadores** condicionais por competência única (Total de Créditos, Total de Débitos e Saldo do Período).
- **Cadastro e edição** de transação via modal único com máscara monetária contínua (`pt-BR`, `R$ 0,00`).
- **Modal de importação de arquivo OFX**: upload de arquivo, leitura estruturada das transações bancárias e gravação com prevenção de duplicidade.
- **Duplicação de transações** individualmente e em lote.
- **Exclusão lógica** com trava de segurança quando a transação for a quitação de uma fatura de cartão fechada/paga.
- Definição das permissões atômicas `TRANSACOES_LISTAR`, `TRANSACOES_INSERIR`, `TRANSACOES_EDITAR`, `TRANSACOES_EXCLUIR` e `TRANSACOES_IMPORTAR`, com proibição mandatória de `TRANSACOES_MANTER`.

**Não contempla:**
- CRUD de **Conta** (`CONTAS`) — documento `2.03 - manter-conta`. A conta bancária é selecionada via combobox alimentado por aquele módulo.
- CRUD de **Categoria** (`CATEGORIAS`) — documento `2.01 - manter-categoria`. A categoria é selecionada via combobox alimentado por aquele módulo.
- CRUD de **Faturas de Cartão** (`FATURAS_CARTAO`) — documento `2.08 - manter-fatura-cartao`. A fatura gera a transação de pagamento no momento de sua baixa, mas o ciclo da fatura pertence àquele documento.
- **Sincronização e conciliação do Open Finance** — documentos `2.09` e `2.10`. A conciliação cria transações com `TRBA_ORIGEM = 'OPEN_FINANCE'`, mas o motor de conciliação fica naqueles módulos.
- Exibição de cards de visão patrimonial global — documento `1.02 - dashboard-financeiro`.

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER). A tela é estritamente do **próprio usuário** — cada um opera apenas sobre transações de suas próprias contas ([RN02](#rn02)).

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **Dono indireto pela conta.** A tabela `TRANSACOES_BANCARIAS` não tem `USU_ID` ([QUADRO_DESCRITIVO_7](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7)); o dono da movimentação é o titular da conta (`TRBA.CTA_ID → CONTAS.USU_ID`). | [RN02](#rn02), [RN03](#rn03) |
| 2 | **`TRBA.CTA_ID` obrigatório na aplicação.** Toda transação pertence necessariamente a uma conta ativa do usuário logado. Contas do tipo `CARTEIRA` também são aceitas para movimentações manuais de espécie. | [RN03](#rn03), [RNF02](#rnf02) |
| 3 | **Escopo *row-level* rigoroso.** Consultas e mutações resolvem o `USU_ID` a partir da sessão/autenticação. Requisição com identificador de transação alheia responde 404 ([MSG05](#msg05)), sem vazar dados. | [RN02](#rn02), [RNF01](#rnf01) |
| 4 | **Natureza do movimento (`CREDITO` e `DEBITO`).** Substitui o antigo enum duplicado. Crédito representa entrada financeira na conta; débito representa saída financeira. O valor em banco (`TRBA_VALOR`) é sempre positivo e a coluna de natureza define a polaridade contábil. | [RN05](#rn05), [RNF04](#rnf04) |
| 5 | **Competência e Data de Lançamento independentes.** `TRBA_COMPETENCIA` é `CHAR(7)` no formato `yyyy-MM` com validação de formato. A data de lançamento (`TRBA_DT_LANCAMENTO`) é `LocalDate`. A competência inicializa sugerida a partir do mês da data, mas o usuário pode ajustá-la livremente. | [RN04](#rn04), [RT11](#rt11) |
| 6 | **Transações geradas por Pagamento de Fatura.** Quando uma fatura de cartão de crédito é paga (documento `2.08`), uma transação bancária de débito é gerada com `TRBA_FL_PAGAMENTO_FATURA = TRUE`. Essa transação não pode ser alterada em sua conta/valor nem excluída diretamente por esta tela enquanto a fatura estiver vinculada, para não desbalancear a quitação do cartão. | [RN08](#rn08), [MSG12](#msg12) |
| 7 | **Deduplicação de importação por `TRBA_ID_EXTERNO`.** Na importação de arquivos OFX, o código da transação bancária (`<FITID>`) é gravado em `TRBA_ID_EXTERNO`. Se um arquivo contendo uma transação com o mesmo identificador externo for importado novamente para a mesma conta, o registro duplicado é ignorado, garantindo idempotência. | [RN10](#rn10), [MSG17](#msg17) |
| 8 | **Transações oriundas de Open Finance.** Movimentações vindas do Open Finance (`TRBA_ORIGEM = 'OPEN_FINANCE'`) têm conta e origem imutáveis e exclusão bloqueada por esta tela, pois mantêm vínculo lógico com `OPFI_TRANSACOES`. | [RN09](#rn09), [MSG13](#msg13) |
| 9 | **Filtro padrão e flexibilidade temporal.** A listagem inicializa filtrando pela competência `mes_atual - 1`. Todas as competências passadas são aceitas irrestritamente sem bloqueios. | [RN13](#rn13), [RT13](#rt13) |
| 10 | **Totalizadores de Competência Única.** Os cards de somatório (Créditos, Débitos e Saldo Líquido) são exibidos apenas quando o filtro de competência inicial for estritamente igual ao final (`competenciaInicio == competenciaFim`). | [RN14](#rn14), [RT14](#rt14) |
| 11 | **Máscara monetária contínua.** Todos os inputs de valores utilizam máscara client-side em tempo real no padrão `pt-BR` (`R$ 0,00`). | [RNF06](#rnf06), [RT11](#rt11) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as transações bancárias do usuário autenticado, com: competência, descrição, conta, categoria, natureza (crédito/débito), valor, data de lançamento, identificação de pagamento de fatura e origem. | Alta | Analisado |
| <a id="rf02"></a>RF02 | O sistema deve permitir filtrar a listagem por texto (descrição), intervalo de competência, conta bancária, natureza do movimento e categoria, através de modal acionado pelo botão "Filtrar". | Média | Analisado |
| <a id="rf03"></a>RF03 | O sistema deve permitir cadastrar uma nova transação bancária via modal, com: descrição, valor, natureza (`CREDITO`/`DEBITO`), data de lançamento, competência, conta e categoria (opcional). | Alta | Analisado |
| <a id="rf04"></a>RF04 | O sistema deve permitir editar uma transação existente via modal, respeitando as travas de registros importados ou atrelados a pagamento de fatura. | Alta | Analisado |
| <a id="rf05"></a>RF05 | O sistema deve permitir a exclusão lógica de uma transação bancária manual, bloqueando exclusão se for quitação de fatura ou importada de Open Finance. | Alta | Analisado |
| <a id="rf06"></a>RF06 | O sistema deve permitir importar extratos bancários em formato OFX, associando as linhas à conta bancária selecionada e deduplicando pelo ID externo. | Alta | Analisado |
| <a id="rf07"></a>RF07 | O sistema deve permitir duplicar transações bancárias individualmente ou em lote, gerando novos lançamentos manuais. | Média | Analisado |
| <a id="rf08"></a>RF08 | O sistema deve garantir que o usuário acesse e altere apenas as transações das contas pertencentes ao seu próprio usuário autenticado. | Alta | Analisado |
| <a id="rf09"></a>RF09 | O sistema deve exigir uma conta bancária ativa em toda transação inserida ou editada manualmente. | Alta | Analisado |
| <a id="rf10"></a>RF10 | O sistema deve validar valor estritamente maior que zero e competência no formato `AAAA-MM`. | Alta | Analisado |
| <a id="rf11"></a>RF11 | O sistema deve exibir os cards totalizadores de Crédito, Débito e Saldo Líquido quando a listagem estiver filtrada por uma única competência (`competenciaInicio == competenciaFim`). | Média | Analisado |
| <a id="rf12"></a>RF12 | O sistema deve inicializar o filtro da tela com o mês anterior (`mes_atual - 1`), permitindo navegação livre por competências passadas. | Média | Analisado |
| <a id="rf13"></a>RF13 | O sistema deve preservar os filtros e a página de navegação ativos no grid após operações de inclusão, edição, exclusão, importação ou duplicação. | Média | Analisado |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada endpoint exige autoridade atômica específica (`PERM_TRANSACOES_LISTAR`, `PERM_TRANSACOES_INSERIR`, `PERM_TRANSACOES_EDITAR`, `PERM_TRANSACOES_EXCLUIR`, `PERM_TRANSACOES_IMPORTAR`). É expressamente proibido o uso de permissão com sufixo `MANTER`. | Testes de acesso com perfis ADMIN, USER e sem permissão. |
| <a id="rnf02"></a>RNF02 | Isolamento | Nenhuma consulta ou comando com `{id}` pode retornar, alterar ou excluir transação de conta de outro usuário — deve responder HTTP 404 ([MSG05](#msg05)). | Teste automatizado chamando endpoints com IDs de outro usuário. |
| <a id="rnf03"></a>RNF03 | Auditoria | A tabela `TRANSACOES_BANCARIAS` deve ser auditada via Hibernate Envers (`@Audited`), registrando inclusão, alteração, exclusão lógica e importação. | Consulta à tabela `TRANSACOES_BANCARIAS_aud`. |
| <a id="rnf04"></a>RNF04 | Integridade | Validações de obrigatoriedade de conta, valor maior que zero, formato de competência e natureza válida devem ocorrer no serviço antes da persistência. | Testes unitários no `TransacaoBancariaService`. |
| <a id="rnf05"></a>RNF05 | Desempenho | O processamento de um arquivo OFX com até 500 transações deve ser concluído e persistido em menos de 3 segundos. | Teste de carga com arquivo OFX padrão de bancos brasileiros. |
| <a id="rnf06"></a>RNF06 | Usabilidade | Interface construída com Thymeleaf + Tabler UI + Phosphor Icons + DataTables, com máscara monetária contínua em tempo real (`pt-BR`, `R$ 0,00`). | Validação visual no navegador. |

---

## 4. Casos de Uso

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Transações Bancárias | [PERF01](#perf01), [PERF02](#perf02) | O usuário visualiza o extrato consolidado de suas transações bancárias. ([RF01](#rf01), [RF08](#rf08)) |
| <a id="caus02"></a>CAUS02 | Filtrar Transações | [PERF01](#perf01), [PERF02](#perf02) | O usuário aplica filtros por período, conta, natureza e categoria. ([RF02](#rf02)) |
| <a id="caus03"></a>CAUS03 | Cadastrar Transação Bancária | [PERF01](#perf01), [PERF02](#perf02) | O usuário insere um crédito ou débito manual em sua conta. ([RF03](#rf03), [RF09](#rf09), [RF10](#rf10)) |
| <a id="caus04"></a>CAUS04 | Editar Transação Bancária | [PERF01](#perf01), [PERF02](#perf02) | O usuário altera dados de uma transação existente. ([RF04](#rf04)) |
| <a id="caus05"></a>CAUS05 | Excluir Transação Bancária | [PERF01](#perf01), [PERF02](#perf02) | O usuário realiza exclusão lógica de um lançamento manual. ([RF05](#rf05)) |
| <a id="caus06"></a>CAUS06 | Importar Extrato OFX | [PERF01](#perf01), [PERF02](#perf02) | O usuário envia arquivo OFX para carga de movimentações na conta. ([RF06](#rf06)) |
| <a id="caus07"></a>CAUS07 | Duplicar Transações | [PERF01](#perf01), [PERF02](#perf02) | O usuário duplica uma ou várias transações para novas competências. ([RF07](#rf07)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Finanças > Transações Bancárias

**Critérios de Aceitação:**
- O menu 'Transações Bancárias' é visível exclusivamente para usuários com [PERM01](#perm01).
- Ao acessar a página, o extrato é carregado automaticamente com o filtro da competência `mes_atual - 1`.
- Apenas transações pertencentes a contas do próprio usuário autenticado são exibidas.
- Os cards de Total de Créditos, Total de Débitos e Saldo do Período são calculados e exibidos quando `competenciaInicio == competenciaFim`.
- No cadastro e edição, a conta deve ser uma conta bancária ativa do usuário.
- O campo valor aplica máscara contínua `pt-BR` e não permite valores menores ou iguais a zero.
- Transações com `TRBA_FL_PAGAMENTO_FATURA = TRUE` exibem badge identificador no grid e não podem ser excluídas diretamente pela tela.
- Na importação de arquivo OFX, transações que possuam `TRBA_ID_EXTERNO` já existente na conta informada são descartadas silenciosamente para prevenir duplicidades.
- Exclusões são lógicas via preenchimento de `audit_data_exclusao`.
- Tentativas de acessar registros de terceiros retornam HTTP 404 com [MSG05](#msg05).

---

## 6. Banco de Dados

A estrutura está integralmente definida no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova nem altera schema**.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `TRANSACOES_BANCARIAS` | Documento 0 — [QUADRO_DESCRITIVO_7](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7) | Tabela principal do CRUD, importações e movimentações |
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | Somente leitura: vínculo de dono (`CTA_ID → CONTAS.USU_ID`) e combobox de contas ativas |
| `CATEGORIAS` | Documento 0 — [QUADRO_DESCRITIVO_3](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) | Somente leitura: combobox de categorização |
| `FATURAS_CARTAO` | Documento 0 — [QUADRO_DESCRITIVO_8](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8) | Contexto: faturas que apontam para a transação via `TRBA_ID_PAGAMENTO` |
| `OPFI_TRANSACOES` | Documento 0 — [QUADRO_DESCRITIVO_20](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-20) | Contexto: rastreabilidade de conciliação de lançamentos Open Finance |

### 6.1 Diagrama ER

O relacionamento segue o modelo do Documento 0: `CONTAS (1) ──< (N) TRANSACOES_BANCARIAS (N) >── (0..1) CATEGORIAS`, com auto-relacionamento de auditoria e chave estrangeira de quitação a partir de `FATURAS_CARTAO.TRBA_ID_PAGAMENTO`.

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| `TRANSACOES_BANCARIAS` | `TRANSACOES_BANCARIAS_aud` | Descrição, valor, competência, data de lançamento, natureza, flag pagamento fatura, ID externo, origem, conta, categoria e dados de soft delete. |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. As regras de conciliação, importação OFX, validação de natureza e proteção de fatura residem na camada de serviço Spring.

---

## 7. Protótipos de Interface

### <a id="quadro-descritivo-1"></a>7.1 Tela: Transações Bancárias (Listagem) — QUADRO_DESCRITIVO_1

> OBSERVAÇÕES: Tela acessada via 'Finanças > Transações Bancárias'. Restrita a [PERM01](#perm01). Grid DataTables com filtros em memória, inicializado com a competência `mes_atual - 1`. Cards totalizadores condicionais no topo.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/transacoes-bancarias/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Finanças > Transações Bancárias | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Transações Bancárias | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Acompanhe e concilie entradas e saídas em suas contas bancárias. | — |
| <a id="qdd1-3a"></a>3a | CARD TOTAL CRÉDITOS | Tipo: Card numérico<br>Texto: Entradas: R$ {totalCreditos}<br>Cor: Verde | Exibido quando `competenciaInicio == competenciaFim` ([RN14](#rn14)). |
| <a id="qdd1-3b"></a>3b | CARD TOTAL DÉBITOS | Tipo: Card numérico<br>Texto: Saídas: R$ {totalDebitos}<br>Cor: Vermelho | Exibido quando `competenciaInicio == competenciaFim` ([RN14](#rn14)). |
| <a id="qdd1-3c"></a>3c | CARD SALDO LÍQUIDO | Tipo: Card numérico<br>Texto: Saldo Movimentado: R$ {saldoLiquido}<br>Cor: Azul / Neutro | Exibido quando `competenciaInicio == competenciaFim` ([RN14](#rn14)). |
| <a id="qdd1-4"></a>4 | BOTÃO FILTRAR | Tipo: Botão<br>Texto: Filtrar<br>Ícone: filter | Ao clicar, executar [RT01](#rt01). |
| <a id="qdd1-5"></a>5 | BOTÃO NOVA TRANSAÇÃO | Tipo: Botão (primário)<br>Texto: Nova transação<br>Ícone: plus | Visível com [PERM02](#perm02). Ao clicar, executar [RT04](#rt04). |
| <a id="qdd1-5a"></a>5a | BOTÃO IMPORTAR OFX | Tipo: Botão (secundário)<br>Texto: Importar OFX<br>Ícone: upload-simple | Visível com [PERM05](#perm05). Ao clicar, executar [RT08](#rt08). |
| <a id="qdd1-5b"></a>5b | BOTÃO DUPLICAR SELECIONADAS | Tipo: Botão (secundário)<br>Texto: Duplicar selecionadas<br>Ícone: copy | Visível com [PERM02](#perm02). Desabilitado sem seleção. Ao clicar, executar [RT15](#rt15). |
| <a id="qdd1-6"></a>6 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side)<br>Endpoint: [EDP02](#edp02) | Carrega transações do usuário via [C1](#c1). Filtros em memória conforme [RT02](#rt02). |
| <a id="qdd1-6a"></a>6a | SELEÇÃO (CHECKBOX) | Tipo: Coluna (checkbox) | Permite selecionar linhas para ações em lote. |
| <a id="qdd1-7"></a>7 | COMPETÊNCIA | Tipo: Coluna<br>Ordenação: Sim | Exibe `TRBA_COMPETENCIA` ("MM/AAAA"). |
| <a id="qdd1-8"></a>8 | DATA | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe `TRBA_DT_LANCAMENTO` ("DD/MM/AAAA"). |
| <a id="qdd1-9"></a>9 | DESCRIÇÃO | Tipo: Coluna<br>Ordenação: Sim | Exibe `TRBA_DESCRICAO`. Se `TRBA_FL_PAGAMENTO_FATURA`, adiciona badge "Pagamento de Fatura". |
| <a id="qdd1-10"></a>10 | CONTA | Tipo: Coluna<br>Ordenação: Sim | Exibe `CTA_DESCRICAO`. |
| <a id="qdd1-11"></a>11 | CATEGORIA | Tipo: Coluna (badge)<br>Ordenação: Sim | Exibe `CATE_NOME` ou traço quando sem categoria. |
| <a id="qdd1-12"></a>12 | NATUREZA | Tipo: Coluna (badge)<br>Ordenação: Sim | "Crédito" (badge verde) ou "Débito" (badge vermelho). |
| <a id="qdd1-13"></a>13 | VALOR | Tipo: Coluna (moeda, alinhada à direita)<br>Ordenação: Sim | Valor monetário formatado (`pt-BR`). |
| <a id="qdd1-14"></a>14 | ORIGEM | Tipo: Coluna (badge)<br>Ordenação: Sim | "Manual" / "Open Finance" / "Importação". |
| <a id="qdd1-15"></a>15 | AÇÕES | Tipo: Coluna (alinhada à esquerda) | Ações [ID16](#qdd1-16). |
| <a id="qdd1-16"></a>16 | ÍCONES DE AÇÃO | Tipo: Ícones<br>Editar (ícone: edit)<br>Duplicar (ícone: copy)<br>Excluir (ícone: trash) | Editar → [RT05](#rt05) ([PERM03](#perm03)). Duplicar → [RT15](#rt15) ([PERM02](#perm02)). Excluir → [RT07](#rt07) ([PERM04](#perm04); oculto quando origem ≠ MANUAL ou pagamento de fatura). |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Filtrar Transações — QUADRO_DESCRITIVO_2

> OBSERVAÇÕES: Todos os campos opcionais. Inicializa com competência padrão em `mes_atual - 1`.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Filtrar Transações Bancárias | — |
| <a id="qdd2-2"></a>2 | BUSCA TEXTUAL | Tipo: Input Text<br>Placeholder: Descrição | Busca parcial sem acento. |
| <a id="qdd2-3"></a>3 | COMPETÊNCIA INICIAL | Tipo: Seletor mês/ano | Padrão inicial: `mes_atual - 1`. Ver [SB04](#sb04). |
| <a id="qdd2-4"></a>4 | COMPETÊNCIA FINAL | Tipo: Seletor mês/ano | Padrão inicial: `mes_atual - 1`. Ver [SB04](#sb04). |
| <a id="qdd2-5"></a>5 | CONTA BANCÁRIA | Tipo: Combobox<br>Opções: "Todas" + contas do usuário | Ver [SB01](#sb01). |
| <a id="qdd2-6"></a>6 | NATUREZA | Tipo: Combobox<br>Opções: Todas / Créditos / Débitos | Ver [SB03](#sb03). |
| <a id="qdd2-7"></a>7 | CATEGORIA | Tipo: Combobox<br>Opções: "Todas" + categorias ativas | Ver [SB02](#sb02). |
| <a id="qdd2-8"></a>8 | BOTÃO APLICAR | Tipo: Botão (primário)<br>Texto: Aplicar | Ao clicar, executar [RT02](#rt02). |
| <a id="qdd2-9"></a>9 | BOTÃO LIMPAR | Tipo: Botão<br>Texto: Limpar | Ao clicar, executar [RT03](#rt03). |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Cadastro / Edição de Transação — QUADRO_DESCRITIVO_3

> OBSERVAÇÕES: Modal único para inclusão ([PERM02](#perm02)) e edição ([PERM03](#perm03)). Máscara monetária contínua no valor. Em edição de transações importadas ou de fatura, travas de campos são aplicadas ([RN08](#rn08), [RN09](#rn09)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Nova transação / Editar transação | Conforme o modo. |
| <a id="qdd3-2"></a>2 | COMPETÊNCIA | Tipo: Seletor mês/ano<br>Obrigatório: Sim | Formato `yyyy-MM`. Grava `TRBA_COMPETENCIA`. |
| <a id="qdd3-3"></a>3 | CONTA BANCÁRIA | Tipo: Combobox<br>Obrigatório: Sim | Grava `CTA_ID`. Ver [SB01](#sb01). Bloqueado se importada ou de fatura. |
| <a id="qdd3-4"></a>4 | NATUREZA | Tipo: Radio/Select (Crédito/Débito)<br>Obrigatório: Sim<br>Default: Débito | Grava `TRBA_NATUREZA_MOVIMENTO`. |
| <a id="qdd3-5"></a>5 | CATEGORIA | Tipo: Combobox<br>Obrigatório: Não | Grava `CATE_ID`. Ver [SB02](#sb02). |
| <a id="qdd3-6"></a>6 | DESCRIÇÃO | Tipo: Input Text<br>Tamanho: 512<br>Obrigatório: Sim | Grava `TRBA_DESCRICAO`. |
| <a id="qdd3-7"></a>7 | VALOR | Tipo: Input monetário<br>Máscara: `pt-BR`, `R$ 0,00`<br>Obrigatório: Sim | Grava `TRBA_VALOR`. Deve ser maior que zero ([RN05](#rn05)). |
| <a id="qdd3-8"></a>8 | DATA DE LANÇAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Default: hoje | Grava `TRBA_DT_LANCAMENTO`. |
| <a id="qdd3-9"></a>9 | AVISO TRAVA FATURA/IMPORTADA | Tipo: Texto informativo | Exibido quando `TRBA_FL_PAGAMENTO_FATURA = TRUE` ou origem ≠ MANUAL. |
| <a id="qdd3-10"></a>10 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar | Ao clicar, executar [RT06](#rt06). |
| <a id="qdd3-11"></a>11 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-4"></a>7.4 Modal: Importar Extrato Bancário (OFX) — QUADRO_DESCRITIVO_4

> OBSERVAÇÕES: Permite carregar movimentações a partir de arquivo `.ofx`. Exige [PERM05](#perm05) (`TRANSACOES_IMPORTAR`).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Importar Extrato Bancário (OFX) | — |
| <a id="qdd4-2"></a>2 | CONTA DE DESTINO | Tipo: Combobox<br>Obrigatório: Sim | Contas ativas do usuário ([SB01](#sb01)). |
| <a id="qdd4-3"></a>3 | COMPETÊNCIA PADRÃO | Tipo: Seletor mês/ano<br>Obrigatório: Sim<br>Default: `mes_atual - 1` | Competência atribuída às transações importadas. |
| <a id="qdd4-4"></a>4 | ARQUIVO OFX | Tipo: Input File<br>Aceita: `.ofx`<br>Obrigatório: Sim | Arquivo de extrato gerado pelo internet banking. |
| <a id="qdd4-5"></a>5 | BOTÃO IMPORTAR | Tipo: Botão (primário)<br>Texto: Processar Arquivo<br>Endpoint: [EDP07](#edp07) | Ao clicar, executar [RT09](#rt09). |
| <a id="qdd4-6"></a>6 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem importar. |

### 7.5 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | CONTA BANCÁRIA | Carregado do endpoint `GET /contas/opcoes` do documento `2.03 - manter-conta`. Apenas contas ativas do usuário logado. |
| <a id="sb02"></a>SB02 | CATEGORIA | Carregado de `GET /categorias/opcoes` do documento `2.01 - manter-categoria`. Todas as categorias ativas aplicáveis a receitas, despesas ou ambos. |
| <a id="sb03"></a>SB03 | NATUREZA | Domínio estático: "Todas", "Crédito" e "Débito". |
| <a id="sb04"></a>SB04 | COMPETÊNCIA | Seletor mês/ano (`YearMonth`, `yyyy-MM`). Aceita qualquer mês passado livremente. |

### 7.6 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao clicar em "Filtrar" ([ID4](#qdd1-4)), abrir o modal de filtro ([QUADRO_DESCRITIVO_2](#quadro-descritivo-2)) com os valores ativos. |
| <a id="rt02"></a>RT02 | Ao clicar em "Aplicar" ([ID8](#qdd2-8)), filtrar em memória o grid DataTables pelos parâmetros informados e atualizar os cards totalizadores ([RT14](#rt14)). Se vazio, exibir [MSG08](#msg08). |
| <a id="rt03"></a>RT03 | Ao clicar em "Limpar" ([ID9](#qdd2-9)), restaurar filtros com competência inicial e final em `mes_atual - 1`, conta em "Todas", natureza em "Todas" e reaplicar. |
| <a id="rt04"></a>RT04 | Ao clicar em "Nova transação" ([ID5](#qdd1-5)) — visível com [PERM02](#perm02) —, abrir [QUADRO_DESCRITIVO_3](#quadro-descritivo-3) em modo criação, com data em hoje, competência no mês da listagem e campos limpos. |
| <a id="rt05"></a>RT05 | Ao clicar em Editar ([ID16](#qdd1-16)) — visível com [PERM03](#perm03) —, buscar dados via [EDP03](#edp03) e abrir modal de edição. Se `TRBA_FL_PAGAMENTO_FATURA = TRUE` ou origem ≠ MANUAL, desabilitar campos de Conta, Natureza e Valor, exibindo o aviso informativo ([ID9](#qdd3-9)). |
| <a id="rt06"></a>RT06 | Ao clicar em "Salvar" ([ID10](#qdd3-10)): validar campos obrigatórios ([MSG02](#msg02)), valor > 0 ([MSG03](#msg03)) e formato de competência ([MSG11](#msg11)). Chamar [EDP04](#edp04) (criação) ou [EDP05](#edp05) (edição). Em sucesso, exibir [MSG01](#msg01) ou [MSG04](#msg04), fechar modal e recarregar grid preservando filtros ([RT16](#rt16)). |
| <a id="rt07"></a>RT07 | Ao clicar em Excluir ([ID16](#qdd1-16)) — visível com [PERM04](#perm04) e oculto para transações de fatura ou Open Finance —, solicitar confirmação ([MSG06](#msg06)). Ao confirmar, chamar [EDP06](#edp06). Em sucesso, exibir [MSG07](#msg07) e recarregar grid ([RT16](#rt16)). |
| <a id="rt08"></a>RT08 | Ao clicar em "Importar OFX" ([ID5a](#qdd1-5a)) — visível com [PERM05](#perm05) —, abrir modal [QUADRO_DESCRITIVO_4](#quadro-descritivo-4). |
| <a id="rt09"></a>RT09 | Ao clicar em "Processar Arquivo" ([ID5](#qdd4-5)): validar conta e arquivo informados ([MSG02](#msg02)). Enviar multipart para [EDP07](#edp07). Em sucesso, exibir mensagem de resumo com transações importadas e duplicadas ignoradas ([MSG17](#msg17)), fechar modal e recarregar grid ([RT16](#rt16)). |
| <a id="rt10"></a>RT10 | Comboboxes de conta ([SB01](#sb01)) e categoria ([SB02](#sb02)) são carregados dinamicamente via endpoints de opções dos módulos 06 e 04, nunca fixos no HTML. |
| <a id="rt11"></a>RT11 | O campo valor adota máscara contínua `pt-BR` (`R$ 0,00`). Ao alterar a data de lançamento no cadastro, sugerir a competência equivalente caso ainda não editada manualmente. |
| <a id="rt12"></a>RT12 | Linhas do grid com `TRBA_FL_PAGAMENTO_FATURA = TRUE` renderizam badge "Pagamento de Fatura" na descrição e suprimem o botão de exclusão. |
| <a id="rt13"></a>RT13 | Na inicialização da tela, configurar o filtro de competência inicial e final como `mes_atual - 1`, permitindo seleção de qualquer competência anterior sem alertas bloqueantes. |
| <a id="rt14"></a>RT14 | Renderizar os cards totalizadores ([ID3a](#qdd1-3a), [ID3b](#qdd1-3b), [ID3c](#qdd1-3c)) exclusivamente quando `competenciaInicio == competenciaFim`. O totalizador de crédito soma transações com natureza `CREDITO`; o de débito soma `DEBITO`; o saldo líquido é a diferença entre créditos e débitos. |
| <a id="rt15"></a>RT15 | Ao acionar a duplicação (linha [ID16](#qdd1-16) ou lote [ID5b](#qdd1-5b)): solicitar confirmação; ao confirmar, enviar IDs para [EDP08](#edp08). Em sucesso, exibir [MSG15](#msg15) e recarregar grid ([RT16](#rt16)). |
| <a id="rt16"></a>RT16 | Todas as mutações preservam a página ativa do DataTables e os filtros em memória após a atualização dos dados. |

---

## 8. Endpoints

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) (`TRANSACOES_LISTAR`) | /transacoes-bancarias/listar | N |
| Retorna a página da listagem de transações bancárias (Thymeleaf). O grid é alimentado via [EDP02](#edp02). | | | | |
| <a id="edp02"></a>EDP02 | GET | [PERM01](#perm01) (`TRANSACOES_LISTAR`) | /transacoes-bancarias/listar-dados | N |
| Lista as transações bancárias das contas do usuário autenticado para o grid em JSON. Executa [C1](#c1). Dados: id, competencia, dtLancamento, descricao, valor, naturezaMovimento, flPagamentoFatura, idExterno, origem, contaId, contaDescricao, categoriaId, categoriaNome. | | | | |
| <a id="edp03"></a>EDP03 | GET | [PERM03](#perm03) (`TRANSACOES_EDITAR`) | /transacoes-bancarias/buscar/{id} | N |
| Retorna os dados de uma transação específica para edição. Executa [RN02](#rn02) e [C2](#c2); responde 404 ([MSG05](#msg05)) se não pertencer ao usuário. | | | | |
| <a id="edp04"></a>EDP04 | POST | [PERM02](#perm02) (`TRANSACOES_INSERIR`) | /transacoes-bancarias/inserir | N |
| Cadastra uma nova transação manual. Validações: [RN03](#rn03) (conta ativa do usuário via [C3](#c3)), [RN04](#rn04) (competência), [RN05](#rn05) (valor > 0 e natureza válida). Fixa `TRBA_ORIGEM = 'MANUAL'` e `TRBA_FL_PAGAMENTO_FATURA = FALSE`. Retorno: 200 ([MSG01](#msg01)) ou 422. | | | | |
| <a id="edp05"></a>EDP05 | PUT | [PERM03](#perm03) (`TRANSACOES_EDITAR`) | /transacoes-bancarias/editar/{id} | N |
| Altera uma transação existente. Valida [RN02](#rn02). Se vinculada a fatura ([RN08](#rn08)) ou Open Finance ([RN09](#rn09)), impede alteração de conta, valor e natureza. Retorno: 200 ([MSG04](#msg04)) ou 422. | | | | |
| <a id="edp06"></a>EDP06 | DELETE | [PERM04](#perm04) (`TRANSACOES_EXCLUIR`) | /transacoes-bancarias/excluir/{id} | N |
| Exclusão lógica da transação. Valida [RN02](#rn02). Se `TRBA_FL_PAGAMENTO_FATURA = TRUE`, bloqueia com [MSG12](#msg12); se `TRBA_ORIGEM = 'OPEN_FINANCE'`, bloqueia com [MSG13](#msg13). Preenche auditoria de exclusão. Retorno: 200 ([MSG07](#msg07)) ou 422. | | | | |
| <a id="edp07"></a>EDP07 | POST | [PERM05](#perm05) (`TRANSACOES_IMPORTAR`) | /transacoes-bancarias/importar-ofx | N |
| Processa arquivo multipart `.ofx` para uma conta específica. Realiza parse via AggregateUnmarshaller, valida integridade bancária, ignora transações cujo `fitid` coincida com `TRBA_ID_EXTERNO` existente na conta ([RN10](#rn10)), persiste as novas com `TRBA_ORIGEM = 'IMPORTACAO'`. Retorno: 200 com resumo de importados/duplicados ([MSG17](#msg17)) ou 422 ([MSG18](#msg18)). | | | | |
| <a id="edp08"></a>EDP08 | POST | [PERM02](#perm02) (`TRANSACOES_INSERIR`) | /transacoes-bancarias/duplicar | N |
| Duplica uma ou mais transações selecionadas ([RN15](#rn15)). Cria novos registros com `TRBA_ORIGEM = 'MANUAL'`, `TRBA_FL_PAGAMENTO_FATURA = FALSE` e `TRBA_ID_EXTERNO = NULL`. Retorno: 200 ([MSG15](#msg15)) ou 422. | | | | |

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | **Autoridades Atômicas:** Cada operação exige sua permissão específica (`TRANSACOES_LISTAR`, `TRANSACOES_INSERIR`, `TRANSACOES_EDITAR`, `TRANSACOES_EXCLUIR`, `TRANSACOES_IMPORTAR`). É proibida qualquer autoridade agrupada `MANTER`. |
| <a id="rn02"></a>RN02 | **Escopo Row-Level por Titular da Conta:** Todas as leituras e gravações filtram pelo `USU_ID` vinculado à conta da transação (`TRBA.CTA_ID → CONTAS.USU_ID`), extraído da autenticação. Tentativas de acessar registros de outros respondem 404 ([MSG05](#msg05)). |
| <a id="rn03"></a>RN03 | **Validação da Conta Vinculada:** Na criação e edição, a conta informada deve existir, estar ativa (`CTA_FL_ATIVO = TRUE`) e pertencer ao usuário autenticado (executa [C3](#c3)). Conta inválida ou de terceiro → [MSG14](#msg14). |
| <a id="rn04"></a>RN04 | **Competência `YearMonth`:** `TRBA_COMPETENCIA` é obrigatória, `CHAR(7)` no formato `yyyy-MM`. Campo independente da data de lançamento, permitindo conciliação retroativa ou competência contábil divergente do dia efetivo do débito/crédito. Formato inválido → [MSG11](#msg11). |
| <a id="rn05"></a>RN05 | **Valor Positivo e Natureza:** `TRBA_VALOR` deve ser estritamente maior que zero (`DECIMAL(15,2)`). O sinal financeiro do movimento é governado exclusivamente por `TRBA_NATUREZA_MOVIMENTO` (`CREDITO` ou `DEBITO`). Valor não positivo → [MSG03](#msg03). |
| <a id="rn06"></a>RN06 | **Categorização Opcional:** `CATE_ID` é opcional na transação. Quando preenchida, deve apontar para uma categoria ativa ([C4](#c4)). Se inativa → [MSG16](#msg16). |
| <a id="rn07"></a>RN07 | **Origem Manual em Tela:** Transações criadas por formulário ou duplicação nascem sempre com `TRBA_ORIGEM = 'MANUAL'` e `TRBA_ID_EXTERNO = NULL`. |
| <a id="rn08"></a>RN08 | **Proteção de Transação Vinculada a Pagamento de Fatura:** Se `TRBA_FL_PAGAMENTO_FATURA = TRUE` (débito originado pela baixa de uma fatura de cartão no documento `2.08`), a exclusão é terminantemente **bloqueada** por esta tela ([MSG12](#msg12)) e a alteração fica restrita à descrição e categoria. Para desfazer a transação, o usuário deve reabrir a fatura de cartão correspondente. |
| <a id="rn09"></a>RN09 | **Proteção de Transação Open Finance:** Transações com `TRBA_ORIGEM = 'OPEN_FINANCE'` têm conta, valor, natureza e origem bloqueados para alteração e não podem ser excluídas ([MSG13](#msg13)), pois possuem amarração com `OPFI_TRANSACOES`. |
| <a id="rn10"></a>RN10 | **Deduplicação e Idempotência na Importação OFX:** Na importação de arquivos bancários OFX ([EDP07](#edp07)), cada lançamento possui um código único de transação (`fitid`). O sistema verifica via [C5](#c5) se já existe transação com este `TRBA_ID_EXTERNO` na conta selecionada. Em caso afirmativo, o lançamento é ignorado, registrando na resposta o total de ignorados sem falhar o lote ([MSG17](#msg17)). |
| <a id="rn11"></a>RN11 | **Exclusão Lógica e Rastreabilidade:** A exclusão preenche `audit_data_exclusao` e `audit_excluido_por`. Não ocorre `DELETE` físico. |
| <a id="rn12"></a>RN12 | **Datas Futuras Permitidas:** `TRBA_DT_LANCAMENTO` aceita datas futuras para fins de planejamento e conciliação de agendamentos bancários. |
| <a id="rn13"></a>RN13 | **Filtro Padrão de Competência:** Inicializa por padrão em `mes_atual - 1` na tela, aceitando quaisquer competências passadas sem restrições. |
| <a id="rn14"></a>RN14 | **Condição dos Cards Totalizadores:** Os cartões com somatório de créditos, débitos e saldo movimentado são exibidos exclusivamente quando `competenciaInicio == competenciaFim`. |
| <a id="rn15"></a>RN15 | **Duplicação de Transações:** A duplicação individual ou em lote ([EDP08](#edp08)) gera novos registros manuais preservando conta, natureza, valor, descrição e categoria, atribuindo a competência alvo selecionada ou a do registro de origem. |
| <a id="rn16"></a>RN16 | **Preservação de Estado:** Todas as operações cadastrais ou de importação recarregam a listagem preservando a página do DataTables e os filtros ativos. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Transação bancária cadastrada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Informe um valor maior que zero. |
| <a id="msg04"></a>MSG04 | Transação bancária atualizada com sucesso. |
| <a id="msg05"></a>MSG05 | Transação bancária não encontrada. |
| <a id="msg06"></a>MSG06 | Confirma a exclusão da transação "{descricao}"? |
| <a id="msg07"></a>MSG07 | Transação bancária excluída com sucesso. |
| <a id="msg08"></a>MSG08 | Nenhuma transação bancária encontrada com os filtros informados. |
| <a id="msg09"></a>MSG09 | Selecione uma natureza de movimento válida (Crédito ou Débito). |
| <a id="msg10"></a>MSG10 | Selecione ao menos uma transação bancária para duplicar. |
| <a id="msg11"></a>MSG11 | A competência deve estar no formato AAAA-MM. |
| <a id="msg12"></a>MSG12 | Esta transação refere-se ao pagamento de uma fatura de cartão e não pode ser excluída diretamente. Reabra a fatura no módulo correspondente para cancelar o pagamento. |
| <a id="msg13"></a>MSG13 | Esta transação foi importada via Open Finance e não pode ser excluída por esta tela. |
| <a id="msg14"></a>MSG14 | A conta selecionada não está disponível. Escolha uma conta ativa. |
| <a id="msg15"></a>MSG15 | Transação(ões) duplicada(s) com sucesso. |
| <a id="msg16"></a>MSG16 | A categoria selecionada não está disponível. |
| <a id="msg17"></a>MSG17 | Extrato importado com sucesso: {novas} transações incluídas e {duplicadas} transações já existentes ignoradas. |
| <a id="msg18"></a>MSG18 | Não foi possível ler o arquivo OFX informado. Certifique-se de que é um extrato bancário válido. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem de transações bancárias das contas do usuário autenticado ([EDP02](#edp02)):<br>`SELECT t.TRBA_ID, t.TRBA_COMPETENCIA, t.TRBA_DT_LANCAMENTO, t.TRBA_DESCRICAO,`<br>`       t.TRBA_VALOR, t.TRBA_NATUREZA_MOVIMENTO, t.TRBA_FL_PAGAMENTO_FATURA,`<br>`       t.TRBA_ID_EXTERNO, t.TRBA_ORIGEM,`<br>`       c.CTA_ID, c.CTA_DESCRICAO,`<br>`       cat.CATE_ID, cat.CATE_NOME`<br>`FROM TRANSACOES_BANCARIAS t`<br>`JOIN CONTAS c ON c.CTA_ID = t.CTA_ID`<br>`LEFT JOIN CATEGORIAS cat ON cat.CATE_ID = t.CATE_ID`<br>`WHERE c.USU_ID = :usuId`<br>`  AND t.audit_data_exclusao IS NULL`<br>`ORDER BY t.TRBA_COMPETENCIA DESC, t.TRBA_DT_LANCAMENTO DESC, t.TRBA_ID DESC;` |
| <a id="c2"></a>C2 | Validação de posse da transação pelo usuário autenticado ([RN02](#rn02)):<br>`SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t`<br>`JOIN CONTAS c ON c.CTA_ID = t.CTA_ID`<br>`WHERE t.TRBA_ID = :trbaId`<br>`  AND c.USU_ID = :usuId`<br>`  AND t.audit_data_exclusao IS NULL;` |
| <a id="c3"></a>C3 | Validação de existência e situação ativa da conta bancária do usuário ([RN03](#rn03)):<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.CTA_ID = :ctaId`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`  AND c.audit_data_exclusao IS NULL;` |
| <a id="c4"></a>C4 | Validação de categoria ativa ([RN06](#rn06)):<br>`SELECT COUNT(*) FROM CATEGORIAS cat`<br>`WHERE cat.CATE_ID = :cateId`<br>`  AND cat.CATE_FL_ATIVO = TRUE`<br>`  AND cat.audit_data_exclusao IS NULL;` |
| <a id="c5"></a>C5 | Verificação de existência de transação com ID externo na conta para deduplicação OFX ([RN10](#rn10)):<br>`SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t`<br>`WHERE t.CTA_ID = :ctaId`<br>`  AND t.TRBA_ID_EXTERNO = :idExterno`<br>`  AND t.audit_data_exclusao IS NULL;` |

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| `TRANSACAO_COMPETENCIA_SEGUE_DATA_LANCAMENTO` | true | Se `true`, a competência do modal de cadastro é sugerida automaticamente pelo mês da data de lançamento enquanto não alterada manualmente pelo usuário. |
| `TRANSACAO_IGNORAR_DUPLICADAS_OFX` | true | Se `true`, linhas de arquivo OFX com ID externo já existente são ignoradas silenciosamente sem interromper o lote. Se `false`, o upload é rejeitado ao encontrar o primeiro duplicado. |

---

## 13. Permissões

Cinco permissões atômicas do módulo **Transações Bancárias** (`PERM_MODULO = 'Transações Bancárias'`), sem permissão agrupada `MANTER`.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `TRANSACOES_LISTAR` — abrir tela e listar transações das próprias contas. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm02"></a>PERM02 | `TRANSACOES_INSERIR` — cadastrar manualmente novas transações e duplicar lançamentos. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm03"></a>PERM03 | `TRANSACOES_EDITAR` — editar lançamentos de transações existentes. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm04"></a>PERM04 | `TRANSACOES_EXCLUIR` — realizar exclusão lógica de transações manuais. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm05"></a>PERM05 | `TRANSACOES_IMPORTAR` — enviar e processar arquivos de extrato bancário OFX. | [PERF01](#perf01), [PERF02](#perf02) |

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `TRANSACOES_LISTAR` | ✓ | ✓ |
| `TRANSACOES_INSERIR` | ✓ | ✓ |
| `TRANSACOES_EDITAR` | ✓ | ✓ |
| `TRANSACOES_EXCLUIR` | ✓ | ✓ |
| `TRANSACOES_IMPORTAR` | ✓ | ✓ |

O perfil ADMIN não tem privilégio de consultar movimentações financeiras de terceiros; a barreira *row-level* ([RN02](#rn02)) aplica-se identicamente a ambos os perfis.

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. Possui todas as permissões concedidas por padrão na carga inicial, operando exclusivamente sobre suas próprias contas financeiras ([RN02](#rn02)). |
| <a id="perf02"></a>PERF02 | USER | Usuário comum do sistema. Gerencia suas próprias transações e contas bancárias com autorizações atômicas. |

---

## 15. Fluxo de Eventos

**1. Importação de Extrato Bancário OFX:**
```
1. Usuário clica em "Importar OFX" no grid de transações.
2. Sistema exibe modal QUADRO_DESCRITIVO_4.
3. Usuário seleciona Conta Bancária, Competência Padrão e faz upload de extrato.ofx.
4. Usuário clica em "Processar Arquivo" → dispara chamada ao EDP07.
5. Serviço analisa o envelope OFX via unmarshaller:
   ├─ Conta inválida / de outro usuário (RN02 / C3) → erro MSG14.
   ├─ Arquivo corrompido / não OFX                 → erro MSG18.
   └─ Para cada transação no extrato:
      ├─ Se TRBA_ID_EXTERNO já existe na conta (C5) → contabiliza como duplicada ignorada.
      └─ Se inédita → persiste TransacaoBancaria com TRBA_ORIGEM = 'IMPORTACAO'.
6. Sistema retorna MSG17 informando total importado e ignorado.
7. Modal fecha e listagem de transações é recarregada preservando filtros.
```

**2. Bloqueio de Exclusão de Pagamento de Fatura:**
```
1. Usuário clica no botão excluir de uma transação bancária.
2. Se TRBA_FL_PAGAMENTO_FATURA = TRUE:
   └─ Sistema intercepta chamada (ou o botão já se encontra oculto no grid) e retorna MSG12.
3. Transação permanece intacta no banco de dados.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Listar apenas transações das próprias contas
Dado que o usuário "usuario1" possui 10 transações registradas em sua conta corrente.  
E o usuário "usuario2" possui 5 transações em outra conta.  
Quando o usuário "usuario1" acessar a tela "Finanças > Transações Bancárias".  
Então o sistema deve exibir no grid exatamente as 10 transações do usuário "usuario1".  
E nenhuma transação do usuário "usuario2" deve ser exibida.

### 16.1 Impedir acesso cruzado via busca por ID
Dado que existe uma transação com ID 55 pertencente a uma conta do "usuario2".  
Quando o "usuario1" requisitar `GET /transacoes-bancarias/buscar/55`.  
Então o sistema deve responder com status HTTP 404 e mensagem [MSG05](#msg05).

### 16.2 Deduplicação na importação OFX
Dado que o usuário importa um arquivo OFX contendo uma transação com `fitid = "TX-998822"`.  
E a transação é gravada na conta corrente do usuário com `TRBA_ID_EXTERNO = "TX-998822"`.  
Quando o usuário importar novamente o mesmo arquivo OFX para a mesma conta.  
Então o sistema não deve criar uma transação duplicada.  
E deve exibir na mensagem de retorno que 1 transação duplicada foi ignorada ([MSG17](#msg17)).

### 16.3 Trava de exclusão de débito de fatura
Dado que uma transação de débito no valor de R$ 1.500,00 possui `TRBA_FL_PAGAMENTO_FATURA = TRUE`.  
Quando o usuário acionar `DELETE /transacoes-bancarias/excluir/{id}` para esta transação.  
Então o sistema deve recusar a exclusão e exibir a mensagem [MSG12](#msg12).

---

## 17. Workshop de Análise / Itens a Confirmar

| Item | Descrição | Situação |
|---|---|---|
| W01 | Integração com categorização automática por inteligência artificial ou regras de regex a partir do texto do OFX. | Futuro (módulo de automações financeiras). |
| W02 | Suporte a conciliação manual mútua entre uma transação bancária e uma receita/despesa avulsa pré-cadastrada. | A Confirmar para v1.2. |

---

## 18. Anexos

- [Documento 0 — Fundação](../../00%20-%20analise-geral/documento-0-fundacao.md) — Definição de `TRANSACOES_BANCARIAS` ([QUADRO_DESCRITIVO_7](../../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7)) e DDL MySQL correspondente ([DDL_7](../../00%20-%20analise-geral/documento-0-fundacao.md#ddl_7)).
- Documento `2.03 - manter-conta` — Especificação do CRUD de contas do usuário e endpoint `GET /contas/opcoes`.
- Documento `2.08 - manter-fatura-cartao` — Especificação do ciclo de vida da fatura de cartão e geração de pagamento.
