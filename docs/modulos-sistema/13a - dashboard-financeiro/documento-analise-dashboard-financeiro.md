# dscproject — Análise de Sistemas
## Módulo Dashboard — USER / ADMIN — Dashboard Financeiro

**Gerado em:** 14/09/2026
**Versão:** 1.0
**Status:** Em análise
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
| 1.0 | 14/09/2026 | Diego dos Santos Cordeiro | Criação do documento. Primeiro dashboard concreto, plugado na mecânica de orquestração do documento `13 - dashboard` como o tipo `FINANCEIRO`. Tela somente leitura com 8 cards sobre dados já existentes (`CONTAS`, `RECEITAS`, `DESPESAS`, `DESPESAS_USUARIO`, `CATEGORIAS`, `CARTOES_CREDITO`, `CONTATOS`): saldo consolidado, saldo por conta, receitas × despesas do mês, despesas por categoria, pagas × pendentes, total rateado por pessoa, limite usado × disponível por cartão e evolução anual/mensal de receitas × despesas. Filtro global de competência (`YearMonth` único, padrão `mes_atual - 1`) aplicado à maioria dos cards, com contrato uniforme de parâmetro mesmo nos cards que o ignoram. Gráficos em ApexCharts, dados embutidos no HTML pelo servidor — sem AJAX. Nova permissão `DASHBOARD_VISUALIZAR`. Não introduz tabela nova nem faz `ALTER TABLE`. |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento é **somente leitura** sobre tabelas já existentes (`CONTAS`, `RECEITAS`, `DESPESAS`, `DESPESAS_USUARIO`, `CATEGORIAS`, `CARTOES_CREDITO`, `CONTATOS`) e **não introduz tabela nova** nem faz `ALTER TABLE`. |
| D04 | Todo card é **dado do próprio usuário autenticado** (escopo row-level). Nenhum card expõe dado financeiro de outro usuário, **inclusive para o ADMIN** ([RN01](#rn01)). |
| D05 | Este documento descreve o tipo `FINANCEIRO` do catálogo `TipoDashboard` — a mecânica de seleção/roteamento entre tipos de dashboard é do documento `13 - dashboard`, não redefinida aqui. |

---

## 1. Introdução

Este documento descreve o **Dashboard Financeiro** do `dscproject-spring-mvc` — o primeiro (e, nesta versão, único) dashboard concreto plugado na mecânica de orquestração do documento `13 - dashboard`, sob o tipo `FINANCEIRO`.

É uma tela **somente leitura**: nenhum card cria, edita ou exclui dado — todos os 8 cards leem dados já existentes, produzidos pelos módulos `06 - manter-conta`, `08 - manter-receita`, `09 - manter-despesa` e `07 - manter-cartao-credito`. Não há tabela nova nem `ALTER TABLE` neste documento.

Todos os cards são escopados **por usuário** (row-level), no mesmo padrão dos demais módulos financeiros — inclusive o ADMIN não tem visão consolidada de dados financeiros de terceiros: administra o próprio dashboard, como qualquer USER.

A maioria dos cards é filtrada por um **filtro global de competência** no topo da página: um valor único (`YearMonth`, não um intervalo início/fim como em Receitas e Despesas), com padrão inicial `mes_atual - 1` (o mesmo padrão dos documentos `08`/`09`). Trocar a competência recarrega a página inteira via GET — **sem AJAX**, seguindo a diretriz do projeto. Por contrato, **todo** card recebe a competência filtrada como parâmetro, mesmo os que não a usam no cálculo (saldo consolidado, saldo por conta e limite de cartão) — isso evita que uma futura mudança de regra num desses cards precise alterar a assinatura do serviço.

**Escopo deste documento:**
- A tela **Dashboard Financeiro**, com o filtro global de competência e os 8 cards da v1 (Seção 7).
- O **cálculo somente leitura** de cada card, a fonte de dados, e o comportamento quando não há dado no período (Seção 9).
- Os **gráficos** (ApexCharts), com dados calculados no servidor e embutidos no HTML — sem endpoint JSON.
- A nova permissão granular `DASHBOARD_VISUALIZAR` (Seção 13).
- O plugue deste módulo na mecânica de orquestração do documento `13` (tipo `FINANCEIRO`).

**Não contempla:**
- Qualquer **escrita** de dado financeiro — cadastro, edição e exclusão de conta, receita, despesa, cartão ou contato continuam nos documentos `06`, `07`, `08`, `09` e `16`.
- O **ciclo de fatura de cartão** (fechamento, vencimento) — documento `11 - manter-fatura-cartao`, hoje "A escrever". O card de limite ([Card 7](#quadro-descritivo-8)) ignora esse ciclo nesta versão (ver [RN12](#rn12) e a referência cruzada ao documento `07`, Observação 11).
- **CRUD de Contatos** — o card de rateio por pessoa ([Card 6](#quadro-descritivo-7)) lê a tabela `CONTATOS` diretamente; não depende do documento `16 - manter-contato` estar implementado.
- A **mecânica de seleção de tipo de dashboard** (catálogo, seletor condicional, roteamento) — documento `13 - dashboard`.

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER), mediante [PERM01](#perm01) (`DASHBOARD_VISUALIZAR`). Cada perfil vê **apenas os próprios dados** ([RN01](#rn01)).

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **Tela somente leitura.** Nenhum dos 8 cards grava dado algum. Todas as tabelas lidas (`CONTAS`, `RECEITAS`, `DESPESAS`, `DESPESAS_USUARIO`, `CATEGORIAS`, `CARTOES_CREDITO`, `CONTATOS`) já existem no Documento 0; este documento não altera nenhuma delas. | [RF01](#rf01)–[RF08](#rf08) |
| 2 | **Escopo row-level em todos os cards.** Cada card filtra pelo `USU_ID` do usuário autenticado, resolvido do contexto de segurança — nunca de parâmetro da requisição. O ADMIN não tem exceção: vê apenas os próprios dados, como qualquer USER. Mesmo modelo dos documentos `06`, `07`, `08` e `09`. | [RN01](#rn01) |
| 3 | **Filtro global de competência é um valor único, não um intervalo.** Ao contrário dos filtros de Receitas ([08](../08%20-%20manter-receita/documento-analise-manter-receita.md)) e Despesas ([09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md)), que filtram por intervalo `competenciaInicio`/`competenciaFim`, o Dashboard usa um único `YearMonth`. Padrão inicial `mes_atual - 1`, mesmo critério da [RN29 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn29). | [RN02](#rn02), [RN03](#rn03) |
| 4 | **Contrato uniforme de competência.** Todo serviço de card recebe a competência filtrada como parâmetro — inclusive o Saldo Consolidado ([Card 1](#quadro-descritivo-2)), o Saldo por Conta ([Card 2](#quadro-descritivo-3)) e o Limite por Cartão ([Card 7](#quadro-descritivo-8)), que **ignoram** esse parâmetro no cálculo atual. O motivo é não quebrar a assinatura do serviço se, no futuro, algum desses cards passar a considerar a competência (por exemplo, saldo projetado até o fim do mês). | [RN04](#rn04), [RN08](#rn08), [RN09](#rn09), [RN12](#rn12) |
| 5 | **Sem AJAX.** Toda troca de filtro (competência global ou o filtro próprio do Card 8) recarrega a página inteira via GET. Nenhum card tem endpoint JSON dedicado; os dados de todos os cards, inclusive os gráficos, são calculados no servidor a cada requisição e embutidos no HTML via Thymeleaf (`th:inline="javascript"`). | [RNF02](#rnf02) |
| 6 | **Valores de despesa são a cota líquida do titular (v1.1).** Os cards 3 (Receitas × Despesas), 4 (Despesas por Categoria) e 5 (Pagas × Pendentes) somam a cota líquida do titular (`DESP_VALOR` menos a soma das fatias transferidas a contatos via rateio, `DESPESAS_USUARIO`), no mesmo critério do totalizador da listagem de Despesas ([RN30 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn30), campo `valorUsuario`). Decisão confirmada nesta versão: evita inflar o gasto pessoal exibido no Dashboard quando parte da despesa foi repassada a um contato, e mantém os números consistentes com a tela de Despesas para a mesma competência. | [RN04](#rn04), [RN05](#rn05), [RN06](#rn06) |
| 7 | **Gráficos em ApexCharts.** O tema Tabler, já usado no projeto, tem seus exemplos de dashboard construídos sobre ApexCharts. Verificar se a dependência já está disponível como webjar no `pom.xml` — hoje não está presente no projeto. **[Requer código]** | [RNF03](#rnf03) |
| 8 | **Limite do cartão é informativo quando nulo.** Quando `CACR_LIMITE` é nulo (campo opcional, [documento 07](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md)), o [Card 7](#quadro-descritivo-8) exibe somente o valor gasto, sem barra nem percentual — não há limite contra o qual calcular "disponível". | [RN11](#rn11) |
| 9 | **Limite disponível ignora o ciclo de fatura.** O cálculo do [Card 7](#quadro-descritivo-8) usa despesas com status de pagamento pendente vinculadas ao cartão, sem considerar dia de fechamento/vencimento — essa é exatamente a conta que o [documento 07 (Observação 11)](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#2-observações) já registrou como pendente e delegada ao Dashboard: "a tela não calcula limite disponível... isso depende da fatura (11) e do Dashboard (13)". O ciclo de fechamento fica para quando o documento `11 - manter-fatura-cartao` existir. | [RN10](#rn10), [RN12](#rn12) |
| 10 | **Card 6 não depende do CRUD de Contatos.** O total rateado por pessoa lê `CONTATOS` e `DESPESAS_USUARIO` diretamente — ambas já existem no Documento 0 ([QUADRO_DESCRITIVO_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29) e [_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11)), consumidas pelo rateio do [documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md). O CRUD de manutenção da agenda de contatos ([documento 16](../16%20-%20manter-contato)) pode estar implementado ou não; o card funciona de qualquer forma. | Documento `09`, Documento `16` |
| 11 | **Card 8 tem filtro próprio, independente do filtro global.** "Evolução: Receitas × Despesas" usa dois seletores de ano (`anoInicio`, `anoFim`), com opções calculadas dinamicamente a partir dos anos com registro de receita ou despesa do usuário — não é um intervalo fixo. Mudar esse filtro também recarrega a página inteira via GET, sem afetar o filtro de competência global dos demais cards. | [RN13](#rn13), [RN14](#rn14) |
| 12 | **Granularidade condicional do Card 8.** Quando `anoInicio == anoFim`, o gráfico é mensal (12 pontos, Jan a Dez daquele ano); quando `anoInicio != anoFim`, o gráfico é anual (1 ponto por ano no intervalo). Mesmo espírito do totalizador condicional por competência única da [RN30 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn30) — a condição aqui é sobre o intervalo de anos, não sobre competência única. | [RN14](#rn14) |
| 13 | **Estados vazios.** Todo card exibe uma mensagem amigável de "sem dados no período" quando não há dado para a competência/intervalo de anos filtrado, em vez de um gráfico ou valor vazio/zerado sem contexto. | [RN15](#rn15) |
| 14 | **Nova permissão granular.** `DASHBOARD_VISUALIZAR` é atômica, sem sufixo `MANTER` — a tela não tem operação de escrita, então uma única permissão de visualização basta. Concedida a ADMIN e USER na carga inicial (Seção 13.1). | [PERM01](#perm01) |
| 15 | **Faixas de cor da barra de limite (v1.1).** O [Card 7](#quadro-descritivo-8) preenche a barra proporcionalmente ao percentual usado do limite, com três faixas: verde (usado < 70%), amarelo (usado entre 70% e 99%) e vermelho (usado ≥ 100%, limite estourado). Quando o gasto ultrapassa o limite, a barra fica 100% preenchida em vermelho e o texto de "Disponível" exibe o valor negativo (ex.: "Disponível: -R$ 120,00"), sem travar nada — apenas alerta visual. | [RN11](#rn11) |
| 16 | **Badge de conta fora do saldo geral (v1.1).** O [Card 2](#quadro-descritivo-3) continua listando **todas** as contas ativas do usuário, independentemente de `CTA_FL_CONSIDERA_SALDO` (diferente do [Card 1](#quadro-descritivo-2), que filtra por essa flag). Para não gerar confusão com o total do Card 1, cada linha de conta com `CTA_FL_CONSIDERA_SALDO = FALSE` exibe um badge "Não entra no saldo geral". | [RN09](#rn09) |
| 17 | **Paginação do Card 6 a partir de 5 contatos (v1.1).** A lista de rateio por pessoa exibe os primeiros itens diretamente; a partir de 5 contatos com rateio na competência filtrada, a lista pagina. | [RN07](#rn07) |
| 18 | **Ano final padrão do Card 8 sem lançamento no ano corrente (v1.1).** Quando o usuário não tem nenhuma receita nem despesa no ano corrente, `anoFim` passa a assumir o ano do lançamento mais recente do usuário (em vez do ano corrente); `anoInicio` continua o menor ano disponível. | [RN13](#rn13) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve exibir o saldo consolidado das contas do usuário autenticado onde "considera no saldo geral" está marcado, independentemente da competência filtrada. | Alta | Em análise |
| <a id="rf02"></a>RF02 | O sistema deve exibir o saldo atual de cada conta ativa do usuário autenticado, independentemente da competência filtrada. | Alta | Em análise |
| <a id="rf03"></a>RF03 | O sistema deve exibir, para a competência filtrada, o comparativo entre o total de receitas e a cota líquida de despesas do usuário autenticado (`DESP_VALOR` descontado o rateio transferido a contatos). | Alta | Analisado |
| <a id="rf04"></a>RF04 | O sistema deve exibir, para a competência filtrada, a cota líquida de despesas do usuário autenticado agrupada por categoria, em gráfico donut. | Alta | Analisado |
| <a id="rf05"></a>RF05 | O sistema deve exibir, para a competência filtrada, a cota líquida de despesas do usuário autenticado agrupada por status de pagamento (pagas, pendentes, não se aplica). | Alta | Analisado |
| <a id="rf06"></a>RF06 | O sistema deve exibir, para a competência filtrada, o total rateado com cada contato do usuário autenticado, somando as fatias de `DESPESAS_USUARIO`. | Média | Em análise |
| <a id="rf07"></a>RF07 | O sistema deve exibir, para cada cartão de crédito ativo do usuário autenticado, o valor usado e o valor disponível de limite numa mesma barra segmentada, independentemente da competência filtrada, colorida por faixa de uso (verde < 70%, amarelo 70–99%, vermelho ≥ 100%). | Alta | Analisado |
| <a id="rf08"></a>RF08 | Quando o limite do cartão não estiver definido, o sistema deve exibir somente o valor usado, sem barra nem percentual. Quando o usado ultrapassar o limite definido, a barra deve ficar 100% preenchida em vermelho e o texto "Disponível" deve exibir o valor negativo. | Média | Analisado |
| <a id="rf09"></a>RF09 | O sistema deve exibir um gráfico de evolução de receitas e despesas do usuário autenticado, com filtro próprio de ano inicial e ano final, cujas opções são calculadas a partir dos anos com lançamento do usuário. | Alta | Em análise |
| <a id="rf10"></a>RF10 | Quando o ano inicial e o ano final do Card 8 forem iguais, o sistema deve exibir a evolução mensal (12 pontos) daquele ano; quando forem diferentes, deve exibir a evolução anual (1 ponto por ano no intervalo). | Alta | Em análise |
| <a id="rf11"></a>RF11 | O sistema deve inicializar o filtro global de competência com o mês anterior ao atual (`mes_atual - 1`). | Média | Em análise |
| <a id="rf12"></a>RF12 | O sistema deve recarregar a página inteira via GET ao trocar o filtro global de competência ou o filtro de anos do Card 8, sem uso de AJAX. | Alta | Em análise |
| <a id="rf13"></a>RF13 | O sistema deve exibir uma mensagem amigável de ausência de dados em cada card que não tiver dado para o período filtrado. | Média | Em análise |
| <a id="rf14"></a>RF14 | O sistema deve restringir todo o conteúdo do Dashboard Financeiro ao usuário autenticado — nenhum usuário visualiza dado financeiro de outro, nem o ADMIN. | Alta | Em análise |
| <a id="rf15"></a>RF15 | O sistema deve exibir um indicador visual ("Não entra no saldo geral") nas contas do Card 2 cuja flag "considera no saldo geral" estiver desmarcada. | Baixa | Analisado |
| <a id="rf16"></a>RF16 | O sistema deve paginar a lista do Card 6 (total rateado por pessoa) quando houver 5 ou mais contatos com rateio na competência filtrada. | Baixa | Analisado |
| <a id="rf17"></a>RF17 | Quando o usuário não tiver nenhum lançamento no ano corrente, o sistema deve inicializar o `anoFim` do Card 8 com o ano do lançamento mais recente do usuário, em vez do ano corrente. | Baixa | Analisado |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | O conteúdo deste dashboard exige [PERM01](#perm01) (`DASHBOARD_VISUALIZAR`). Sem a permissão, o orquestrador ([documento 13](../13%20-%20dashboard)) exibe o estado vazio de conteúdo indisponível ([RN07 do documento 13](../13%20-%20dashboard/documento-analise-dashboard.md#rn07)). Além da permissão, todo cálculo aplica o escopo por `USU_ID` ([RN01](#rn01)). | Teste de acesso com ADMIN, com USER e com um perfil sem `DASHBOARD_VISUALIZAR`. |
| <a id="rnf02"></a>RNF02 | Usabilidade | A interface segue o padrão do projeto (Thymeleaf + Tabler) e é responsiva. Toda troca de filtro recarrega a página inteira via GET — nenhum endpoint JSON ou chamada AJAX. | Revisão visual e inspeção de rede ao trocar os filtros. |
| <a id="rnf03"></a>RNF03 | Técnica | Os gráficos usam ApexCharts, incluído via webjar; verificar e, se necessário, adicionar a dependência no `pom.xml`. | Inspeção do `pom.xml` e da página renderizada. |
| <a id="rnf04"></a>RNF04 | Desempenho | A página completa (8 cards) responde em menos de 2 s para um usuário com volume típico de lançamentos (algumas centenas de despesas/receitas por competência). | Medição em homologação. |
| <a id="rnf05"></a>RNF05 | Privacidade | Nenhum card retorna dado financeiro de um usuário diferente do autenticado, inclusive para o ADMIN. | Teste de acesso cruzado: comparar o conteúdo do Dashboard entre dois usuários com dados distintos. |

---

## 4. Casos de Uso

Diagrama de Casos de Uso: a gerar quando solicitado (ver Seção 18 — Anexos).

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Visualizar Dashboard Financeiro | [PERF01](#perf01), [PERF02](#perf02) | Usuário com [PERM01](#perm01) acessa o Dashboard Financeiro e visualiza os 8 cards da competência padrão (`mes_atual - 1`). ([RF01](#rf01)–[RF08](#rf08), [RF11](#rf11)) |
| <a id="caus02"></a>CAUS02 | Filtrar por Competência | [PERF01](#perf01), [PERF02](#perf02) | Usuário altera o filtro global de competência; a página recarrega e os cards dependentes de competência são recalculados. ([RF12](#rf12)) |
| <a id="caus03"></a>CAUS03 | Consultar Evolução Anual/Mensal | [PERF01](#perf01), [PERF02](#perf02) | Usuário ajusta o ano inicial e/ou final do Card 8; a página recarrega exibindo a granularidade mensal ou anual conforme o intervalo escolhido. ([RF09](#rf09), [RF10](#rf10)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Dashboard (tela servida pelo orquestrador do documento `13` quando o tipo ativo é `FINANCEIRO`; hoje é sempre o caso, por ser o único tipo cadastrado).

**Critérios de Aceitação:**
- O conteúdo só aparece para quem tem [PERM01](#perm01) (`DASHBOARD_VISUALIZAR`).
- Ao acessar a tela, o filtro de competência é inicializado com `mes_atual - 1` e os 8 cards são carregados para essa competência.
- Nenhum card, em nenhuma circunstância, exibe dado financeiro de outro usuário.
- Trocar a competência global recarrega a página inteira via GET.
- O Card 7 (limite por cartão) e os Cards 1 e 2 (saldo) não mudam de valor ao trocar a competência global.
- O Card 8 tem filtro de ano próprio, independente da competência global; sua granularidade (mensal/anual) muda conforme o intervalo de anos escolhido.
- Um card sem dado no período filtrado exibe mensagem amigável, não um gráfico vazio.

---

## 6. Banco de Dados

Toda a estrutura está no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova** e **não faz `ALTER TABLE`** — é somente leitura sobre tabelas já existentes.

| Tabela | Onde | Papel neste dashboard |
|---|---|---|
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | Somente leitura — Cards 1, 2 e escopo de Receitas/Despesas |
| `RECEITAS` | Documento 0 — [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9) | Somente leitura — Cards 3 e 8 |
| `DESPESAS` | Documento 0 — [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) | Somente leitura — Cards 3, 4, 5, 6, 7 e 8 |
| `DESPESAS_USUARIO` | Documento 0 — [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11) | Somente leitura — Card 6 |
| `CATEGORIAS` | Documento 0 — [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) | Somente leitura — Card 4 |
| `CARTOES_CREDITO` | Documento 0 — [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6) | Somente leitura — Card 7 |
| `CONTATOS` | Documento 0 — [QUADRO_DESCRITIVO_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29) | Somente leitura — Card 6 |

> Nenhum `ALTER TABLE` neste documento. Todas as colunas usadas nos cálculos (`CTA_SALDO`, `CTA_FL_CONSIDERA_SALDO`, `RECE_VALOR`, `DESP_VALOR`, `DESP_IND_STATUS_PAGAMENTO`, `DEPU_VALOR`, `CACR_LIMITE`, entre outras) já existem no Documento 0.

### 6.1 Diagrama ER

Não aplicável — nenhuma tabela nova. Ver o DER do Documento 0 para o modelo completo.

### 6.2 Auditoria de Tabelas

Não aplicável — este documento não grava dado algum.

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. Todos os agrupamentos e somatórios são consultas de leitura na camada de serviço (Seção 11).

---

## 7. Protótipos de Interface

Protótipo navegável ainda não gerado nesta versão — solicitar quando necessário (ver Seção 18 — Anexos). Os números em destaque nas telas, quando o protótipo existir, corresponderão aos IDs dos itens do respectivo QUADRO_DESCRITIVO.

### <a id="quadro-descritivo-1"></a>7.1 Tela: Dashboard Financeiro (Estrutura Geral) — QUADRO_DESCRITIVO_1

> OBSERVAÇÕES: Conteúdo delegado pelo orquestrador ([documento 13](../13%20-%20dashboard/documento-analise-dashboard.md)) quando o tipo ativo é `FINANCEIRO`. Restrita a quem tem [PERM01](#perm01). Todos os cards são somente leitura e escopados ao usuário autenticado ([RN01](#rn01)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-1"></a>1 | FILTRO – COMPETÊNCIA GLOBAL | Tipo: Seletor mês/ano<br>Obrigatório: Não<br>Valor default: `mes_atual - 1` | Aplicado aos Cards 3, 4, 5 e 6. Ao alterar, executar [RT01](#rt01). Ver [RN02](#rn02), [RN03](#rn03). |
| <a id="qdd1-2"></a>2 | GRADE DE CARDS | Tipo: Grade responsiva de cards | Contém os 8 cards da v1, cada um descrito no seu próprio QUADRO_DESCRITIVO ([_2](#quadro-descritivo-2) a [_9](#quadro-descritivo-9)). |

### <a id="quadro-descritivo-2"></a>7.2 Card: Saldo Consolidado — QUADRO_DESCRITIVO_2

> OBSERVAÇÕES: Card 1. Fonte: `CONTAS` do usuário autenticado com `CTA_FL_CONSIDERA_SALDO = TRUE`. Ignora a competência global (recebida por contrato — [Observação 4](#2-observações)). Consulta: [C1](#c1).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | VALOR – SALDO CONSOLIDADO | Tipo: Card numérico (destaque)<br>Formato: BRL | Soma de `CTA_SALDO` das contas ativas e não excluídas do usuário com `CTA_FL_CONSIDERA_SALDO = TRUE` ([RN08](#rn08), [C1](#c1)). |
| <a id="qdd2-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido no lugar do valor quando o usuário não tem nenhuma conta com `CTA_FL_CONSIDERA_SALDO = TRUE` ([MSG02](#msg02)). |

### <a id="quadro-descritivo-3"></a>7.3 Card: Saldo por Conta Bancária — QUADRO_DESCRITIVO_3

> OBSERVAÇÕES: Card 2. Fonte: `CONTAS` ativas do usuário autenticado. Ignora a competência global. Consulta: [C2](#c2).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | MINI-TABELA – SALDO POR CONTA | Tipo: Lista/mini-tabela<br>Colunas: Conta, Tipo, Saldo (BRL) | Uma linha por conta ativa e não excluída do usuário, ordenada por descrição ([RN09](#rn09), [C2](#c2)). |
| <a id="qdd3-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando o usuário não tem nenhuma conta ativa ([MSG02](#msg02)). |

### <a id="quadro-descritivo-4"></a>7.4 Card: Receitas × Despesas do Mês — QUADRO_DESCRITIVO_4

> OBSERVAÇÕES: Card 3. Fonte: `RECEITAS` e `DESPESAS` do usuário autenticado na competência filtrada. Comparativo em duas colunas/barras. Consulta: [C3](#c3).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | GRÁFICO – RECEITAS × DESPESAS | Tipo: Gráfico de barras (ApexCharts)<br>Séries: Receitas, Despesas | Soma de `RECE_VALOR` e soma de `DESP_VALOR` do usuário na competência filtrada ([RN04](#rn04), [C3](#c3)). Valores brutos, sem dedução de rateio ([Observação 6](#2-observações)). |
| <a id="qdd4-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando não há receita nem despesa do usuário na competência filtrada ([MSG01](#msg01)). |

### <a id="quadro-descritivo-5"></a>7.5 Card: Despesas por Categoria — QUADRO_DESCRITIVO_5

> OBSERVAÇÕES: Card 4. Fonte: `DESPESAS` do usuário autenticado na competência filtrada, agrupadas por `CATEGORIAS`. Consulta: [C4](#c4).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd5-1"></a>1 | GRÁFICO – DESPESAS POR CATEGORIA | Tipo: Gráfico donut (ApexCharts) | Uma fatia por categoria com despesa na competência filtrada; despesas sem categoria agrupadas em "Sem categoria" ([RN05](#rn05), [C4](#c4)). Cor de cada fatia vem de `CATE_COR` quando definida. |
| <a id="qdd5-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando não há despesa do usuário na competência filtrada ([MSG01](#msg01)). |

### <a id="quadro-descritivo-6"></a>7.6 Card: Pagas × Pendentes no Mês — QUADRO_DESCRITIVO_6

> OBSERVAÇÕES: Card 5. Fonte: `DESPESAS` do usuário autenticado na competência filtrada, agrupadas por `DESP_IND_STATUS_PAGAMENTO`. Consulta: [C5](#c5).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd6-1"></a>1 | GRÁFICO – PAGAS × PENDENTES | Tipo: Gráfico donut ou barras (ApexCharts)<br>Séries: Pagas, Pendentes, Não se aplica | Soma de `DESP_VALOR` do usuário na competência filtrada, agrupada por status ([RN06](#rn06), [C5](#c5)). |
| <a id="qdd6-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando não há despesa do usuário na competência filtrada ([MSG01](#msg01)). |

### <a id="quadro-descritivo-7"></a>7.7 Card: Total Rateado no Mês, por Pessoa — QUADRO_DESCRITIVO_7

> OBSERVAÇÕES: Card 6. Fonte: `DESPESAS_USUARIO` (rateio) das despesas do usuário autenticado na competência filtrada, agrupadas por `CONTATOS`. Não depende do documento `16` estar implementado ([Observação 10](#2-observações)). Consulta: [C6](#c6).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd7-1"></a>1 | GRÁFICO/LISTA – RATEADO POR PESSOA | Tipo: Gráfico de barras ou lista (ApexCharts)<br>Eixo: nome do contato | Soma de `DEPU_VALOR` das linhas de `DESPESAS_USUARIO` cuja despesa é do usuário e está na competência filtrada, agrupada por `CONT_ID`/`CONT_NOME` ([RN07](#rn07), [C6](#c6)). |
| <a id="qdd7-2"></a>2 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando não há rateio registrado na competência filtrada ([MSG04](#msg04)). |

### <a id="quadro-descritivo-8"></a>7.8 Card: Limite Usado × Disponível por Cartão — QUADRO_DESCRITIVO_8

> OBSERVAÇÕES: Card 7. Fonte: `CARTOES_CREDITO` ativos do usuário autenticado e `DESPESAS` pendentes vinculadas a cada cartão. Ignora a competência global e o ciclo de fechamento/vencimento (ver [Observação 9](#2-observações) e [documento 07, Observação 11](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#2-observações)). Consulta: [C7](#c7).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd8-1"></a>1 | BARRA – LIMITE POR CARTÃO | Tipo: Barra de progresso segmentada, uma por cartão<br>Segmentos: Usado, Disponível | Usado = soma de `DESP_VALOR` das despesas com `CACR_ID` do cartão e `DESP_IND_STATUS_PAGAMENTO = 'NAO'`, sem filtro de competência, inclusive despesas de importação de fatura/extrato ([RN10](#rn10), [C7](#c7)). Disponível = `CACR_LIMITE - usado`, quando `CACR_LIMITE` não é nulo. |
| <a id="qdd8-2"></a>2 | VALOR – SOMENTE USADO | Tipo: Card numérico (sem barra)<br>Exibição: condicional | Exibido no lugar de [ID1](#qdd8-1) quando `CACR_LIMITE` é nulo — mostra só o valor usado, sem barra nem percentual ([RN11](#rn11)). |
| <a id="qdd8-3"></a>3 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando o usuário não tem nenhum cartão de crédito ativo ([MSG03](#msg03)). |

### <a id="quadro-descritivo-9"></a>7.9 Card: Evolução Receitas × Despesas — QUADRO_DESCRITIVO_9

> OBSERVAÇÕES: Card 8. Filtro **próprio** (`anoInicio`, `anoFim`), independente da competência global ([Observação 11](#2-observações)). Granularidade condicional ([Observação 12](#2-observações), [RN14](#rn14)). Consultas: [C8](#c8), [C9](#c9), [C10](#c10).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd9-1"></a>1 | FILTRO – ANO INICIAL | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: [SB01](#sb01) | Anos com receita ou despesa do usuário ([C8](#c8)). Ao alterar, executar [RT02](#rt02). |
| <a id="qdd9-2"></a>2 | FILTRO – ANO FINAL | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: [SB01](#sb01) | Anos com receita ou despesa do usuário ([C8](#c8)). Ao alterar, executar [RT02](#rt02). |
| <a id="qdd9-3"></a>3 | GRÁFICO – EVOLUÇÃO MENSAL | Tipo: Gráfico de linhas (ApexCharts)<br>Eixo X: Jan a Dez<br>Exibição: condicional (`anoInicio == anoFim`) | Soma mensal de `RECE_VALOR` e de `DESP_VALOR` do ano selecionado, um ponto por mês ([RN14](#rn14), [C9](#c9)). |
| <a id="qdd9-4"></a>4 | GRÁFICO – EVOLUÇÃO ANUAL | Tipo: Gráfico de linhas (ApexCharts)<br>Eixo X: anos do intervalo<br>Exibição: condicional (`anoInicio != anoFim`) | Soma anual de `RECE_VALOR` e de `DESP_VALOR` de cada ano no intervalo `[anoInicio, anoFim]`, um ponto por ano ([RN14](#rn14), [C10](#c10)). |
| <a id="qdd9-5"></a>5 | ESTADO VAZIO | Tipo: Texto informativo<br>Exibição: condicional | Exibido quando o usuário não tem nenhuma receita nem despesa em nenhum ano ([MSG01](#msg01)); nesse caso, [ID1](#qdd9-1)/[ID2](#qdd9-2) ficam sem opção disponível além do ano corrente. |

### 7.10 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | ANOS DISPONÍVEIS (CARD 8) | Anos distintos com pelo menos uma receita ou despesa do usuário autenticado (`YEAR` de `RECE_COMPETENCIA` ou `DESP_COMPETENCIA`), calculados por [C8](#c8) e ordenados crescentemente. Nunca uma lista fixa de anos. |

### 7.11 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao alterar o filtro de competência global ([ID1](#qdd1-1)), recarregar a página inteira via GET com a nova competência, mantendo o filtro de anos do Card 8 já aplicado, se houver. |
| <a id="rt02"></a>RT02 | Ao alterar o ano inicial ou o ano final do Card 8 ([ID1](#qdd9-1)/[ID2](#qdd9-2)), recarregar a página inteira via GET com os novos anos, mantendo a competência global já aplicada. Se `anoInicio > anoFim` for submetido, o serviço troca os dois valores antes de calcular ([RN13](#rn13)). |
| <a id="rt03"></a>RT03 | Em qualquer card sem dado no período filtrado, exibir o texto do estado vazio correspondente ([ID2](#qdd2-2), [ID2](#qdd3-2), [ID2](#qdd4-2), [ID2](#qdd5-2), [ID2](#qdd6-2), [ID2](#qdd7-2), [ID3](#qdd8-3), [ID5](#qdd9-5)) em vez de um gráfico ou valor vazio. |

---

## 8. Endpoints

Este dashboard não define endpoint próprio — reaproveita [EDP01 do documento 13](../13%20-%20dashboard/documento-analise-dashboard.md#edp01) (`GET /dashboard`), que delega a montagem do conteúdo a este módulo quando o tipo ativo é `FINANCEIRO`. Os parâmetros de querystring específicos deste dashboard, lidos e tratados inteiramente por este módulo, são:

| PARÂMETRO | OBRIGATÓRIO | PADRÃO | DESCRIÇÃO |
|---|---|---|---|
| `competencia` | Não | `mes_atual - 1` | Competência global (`yyyy-MM`) aplicada aos Cards 3, 4, 5 e 6 ([RN02](#rn02), [RN03](#rn03)). |
| `anoInicio` | Não | ano com o lançamento mais antigo do usuário, ou o ano corrente se não houver nenhum | Ano inicial do Card 8 ([RN13](#rn13)). |
| `anoFim` | Não | ano corrente | Ano final do Card 8 ([RN13](#rn13)). |

> Sem retorno JSON — os três parâmetros produzem sempre a página completa renderizada pelo servidor, com os valores e os dados dos gráficos já embutidos no HTML (Thymeleaf, `th:inline="javascript"`).

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | **Escopo por usuário (row-level).** Toda consulta ([C1](#c1)–[C10](#c10)) é filtrada pelo `USU_ID` do usuário autenticado, resolvido do contexto de segurança — nunca de parâmetro da requisição. O ADMIN não tem exceção: vê apenas os próprios dados, como o USER. |
| <a id="rn02"></a>RN02 | **Filtro global de competência.** Valor único (`YearMonth`), aplicado aos Cards 3, 4, 5 e 6. Padrão inicial `mes_atual - 1` ([RN29 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn29) como precedente). Recebido pela querystring `competencia` (`yyyy-MM`); ausente ou inválido, usa o padrão. |
| <a id="rn03"></a>RN03 | **Contrato uniforme de competência.** Os serviços dos Cards 1 ([RN08](#rn08)), 2 ([RN09](#rn09)) e 7 ([RN12](#rn12)) recebem a competência filtrada como parâmetro de método/DTO, mesmo sem usá-la no cálculo — para não quebrar a assinatura caso a regra desses cards passe a considerar competência no futuro. |
| <a id="rn04"></a>RN04 | **Card 3 — Receitas × Despesas do mês.** Soma `RECE_VALOR` de todas as `RECEITAS` do usuário na competência filtrada (`RECE_COMPETENCIA = competencia`) e soma `DESP_VALOR` de todas as `DESPESAS` do usuário na mesma competência (`DESP_COMPETENCIA = competencia`). Valores brutos — não deduz o rateio transferido a contatos ([Observação 6](#2-observações)). Executa [C3](#c3). |
| <a id="rn05"></a>RN05 | **Card 4 — Despesas por categoria.** Agrupa a soma de `DESP_VALOR` das despesas do usuário na competência filtrada por `CATE_ID`; despesas com `CATE_ID` nulo entram no grupo "Sem categoria". Executa [C4](#c4). |
| <a id="rn06"></a>RN06 | **Card 5 — Pagas × pendentes.** Agrupa a soma de `DESP_VALOR` das despesas do usuário na competência filtrada por `DESP_IND_STATUS_PAGAMENTO` (`SIM` = Pagas, `NAO` = Pendentes, `NAO_SE_APLICA` = Não se aplica; nulo é tratado como `NAO_SE_APLICA`). Executa [C5](#c5). |
| <a id="rn07"></a>RN07 | **Card 6 — Total rateado por pessoa.** Soma `DEPU_VALOR` das linhas de `DESPESAS_USUARIO` cuja despesa (`DESP_ID`) pertence ao usuário autenticado e está na competência filtrada, agrupada por `CONT_ID`/`CONT_NOME` (via `CONTATOS`). Executa [C6](#c6). Independe do documento `16` estar implementado ([Observação 10](#2-observações)). |
| <a id="rn08"></a>RN08 | **Card 1 — Saldo consolidado.** Soma `CTA_SALDO` das `CONTAS` ativas e não excluídas do usuário autenticado com `CTA_FL_CONSIDERA_SALDO = TRUE`. Ignora a competência filtrada, recebida por contrato ([RN03](#rn03)). Executa [C1](#c1). |
| <a id="rn09"></a>RN09 | **Card 2 — Saldo por conta bancária.** Lista as `CONTAS` ativas e não excluídas do usuário autenticado com o respectivo `CTA_SALDO`, ordenadas por descrição. Ignora a competência filtrada, recebida por contrato ([RN03](#rn03)). Executa [C2](#c2). |
| <a id="rn10"></a>RN10 | **Card 7 — Cálculo do usado.** Para cada cartão ativo e não excluído do usuário (`CACR_FL_ATIVO = TRUE`), o "usado" é a soma de `DESP_VALOR` das despesas com `CACR_ID` daquele cartão e `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (pendentes), sem filtro de competência e sem distinção de origem (inclui despesas de importação de fatura/extrato do documento `09`). Ignora o ciclo de fechamento/vencimento do cartão ([Observação 9](#2-observações), [documento 07 Observação 11](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#2-observações)). Executa [C7](#c7). |
| <a id="rn11"></a>RN11 | **Card 7 — Disponível e limite nulo.** Quando `CACR_LIMITE` não é nulo, disponível = `CACR_LIMITE - usado` (podendo ser negativo, se o usado ultrapassar o limite — a barra exibe o excedente de forma visualmente destacada, sem travar nada). Quando `CACR_LIMITE` é nulo, exibe somente o valor usado, sem barra nem percentual ([ID2 do QUADRO_DESCRITIVO_8](#qdd8-2)). |
| <a id="rn12"></a>RN12 | **Card 7 — Contrato de competência.** O serviço do Card 7 recebe a competência filtrada por contrato ([RN03](#rn03)), mas não a usa em nenhum cálculo desta versão. |
| <a id="rn13"></a>RN13 | **Card 8 — Filtro de anos.** `anoInicio` e `anoFim` são independentes da competência global. As opções de ambos vêm de [C8](#c8) — anos distintos com pelo menos uma receita ou despesa do usuário. Se `anoInicio` ou `anoFim` não forem informados, `anoFim` assume o ano corrente e `anoInicio` assume o menor ano disponível para o usuário (ou o ano corrente, se não houver nenhum lançamento). Se `anoInicio > anoFim` for submetido, o serviço troca os dois antes de calcular. |
| <a id="rn14"></a>RN14 | **Card 8 — Granularidade condicional.** Quando `anoInicio == anoFim`: gráfico mensal com 12 pontos (Jan a Dez) daquele ano, somando `RECE_VALOR` e `DESP_VALOR` do usuário por mês, incluindo meses sem lançamento como zero ([C9](#c9)). Quando `anoInicio != anoFim`: gráfico anual com 1 ponto por ano no intervalo `[anoInicio, anoFim]`, somando `RECE_VALOR` e `DESP_VALOR` do ano inteiro, incluindo anos sem lançamento como zero ([C10](#c10)). Mesmo espírito do totalizador condicional da [RN30 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn30), aplicado aqui à granularidade em vez de à exibição do card. |
| <a id="rn15"></a>RN15 | **Estados vazios.** Quando não houver dado para o período filtrado (competência global, para os Cards 3/4/5/6; qualquer registro, para os Cards 1/2/7; o intervalo de anos, para o Card 8), o card exibe a mensagem de ausência de dados correspondente em vez do gráfico/valor ([RT03](#rt03)). |
| <a id="rn16"></a>RN16 | **Gráficos sem endpoint próprio.** Todos os dados dos gráficos (Cards 3 a 9) são calculados no servidor a cada requisição de página e embutidos no HTML via Thymeleaf (`th:inline="javascript"`), no formato que a biblioteca ApexCharts espera. Nenhum gráfico é alimentado por chamada AJAX. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Sem dados no período selecionado. |
| <a id="msg02"></a>MSG02 | Nenhuma conta cadastrada. |
| <a id="msg03"></a>MSG03 | Nenhum cartão de crédito ativo cadastrado. |
| <a id="msg04"></a>MSG04 | Nenhum rateio registrado nesta competência. |

---

## 11. Consultas

Todas as consultas recebem o `:usuId` do usuário autenticado e o aplicam no `WHERE` ([RN01](#rn01)).

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Card 1 — saldo consolidado (RN08).<br>`SELECT COALESCE(SUM(c.CTA_SALDO), 0) AS saldoConsolidado`<br>`FROM CONTAS c`<br>`WHERE c.USU_ID = :usuId`<br>`  AND c.CTA_FL_CONSIDERA_SALDO = TRUE`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`  AND c.audit_data_exclusao IS NULL;` |
| <a id="c2"></a>C2 | Card 2 — saldo por conta (RN09).<br>`SELECT c.CTA_ID, c.CTA_DESCRICAO, c.CTA_TIPO, c.CTA_SALDO`<br>`FROM CONTAS c`<br>`WHERE c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`  AND c.audit_data_exclusao IS NULL`<br>`ORDER BY c.CTA_DESCRICAO ASC;` |
| <a id="c3"></a>C3 | Card 3 — receitas × despesas da competência (RN04).<br>`SELECT`<br>`  (SELECT COALESCE(SUM(r.RECE_VALOR), 0) FROM RECEITAS r`<br>`     JOIN CONTAS ct ON ct.CTA_ID = r.CTA_ID`<br>`     WHERE ct.USU_ID = :usuId AND r.RECE_COMPETENCIA = :competencia AND r.audit_data_exclusao IS NULL) AS totalReceitas,`<br>`  (SELECT COALESCE(SUM(d.DESP_VALOR), 0) FROM DESPESAS d`<br>`     LEFT JOIN CONTAS c2           ON c2.CTA_ID  = d.CTA_ID`<br>`     LEFT JOIN CARTOES_CREDITO cc  ON cc.CACR_ID = d.CACR_ID`<br>`     WHERE (c2.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`       AND d.DESP_COMPETENCIA = :competencia AND d.audit_data_exclusao IS NULL) AS totalDespesas;` |
| <a id="c4"></a>C4 | Card 4 — despesas por categoria da competência (RN05).<br>`SELECT COALESCE(cat.CATE_NOME, 'Sem categoria') AS categoria, cat.CATE_COR AS cor,`<br>`       COALESCE(SUM(d.DESP_VALOR), 0) AS total`<br>`FROM DESPESAS d`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`LEFT JOIN CATEGORIAS cat     ON cat.CATE_ID = d.CATE_ID`<br>`WHERE (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL`<br>`GROUP BY cat.CATE_ID, cat.CATE_NOME, cat.CATE_COR`<br>`ORDER BY total DESC;` |
| <a id="c5"></a>C5 | Card 5 — pagas × pendentes da competência (RN06).<br>`SELECT COALESCE(d.DESP_IND_STATUS_PAGAMENTO, 'NAO_SE_APLICA') AS status,`<br>`       COALESCE(SUM(d.DESP_VALOR), 0) AS total`<br>`FROM DESPESAS d`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`WHERE (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL`<br>`GROUP BY status;` |
| <a id="c6"></a>C6 | Card 6 — total rateado por pessoa na competência (RN07).<br>`SELECT ct.CONT_ID, ct.CONT_NOME, COALESCE(SUM(du.DEPU_VALOR), 0) AS total`<br>`FROM DESPESAS_USUARIO du`<br>`JOIN DESPESAS d  ON d.DESP_ID  = du.DESP_ID`<br>`JOIN CONTATOS ct ON ct.CONT_ID = du.CONT_ID`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`WHERE (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL`<br>`  AND du.audit_data_exclusao IS NULL`<br>`GROUP BY ct.CONT_ID, ct.CONT_NOME`<br>`ORDER BY total DESC;` |
| <a id="c7"></a>C7 | Card 7 — limite usado por cartão ativo (RN10, RN11).<br>`SELECT ca.CACR_ID, ca.CACR_DESCRICAO, ca.CACR_LIMITE,`<br>`       COALESCE((SELECT SUM(d.DESP_VALOR) FROM DESPESAS d`<br>`                   WHERE d.CACR_ID = ca.CACR_ID`<br>`                     AND d.DESP_IND_STATUS_PAGAMENTO = 'NAO'`<br>`                     AND d.audit_data_exclusao IS NULL), 0) AS usado`<br>`FROM CARTOES_CREDITO ca`<br>`WHERE ca.USU_ID = :usuId`<br>`  AND ca.CACR_FL_ATIVO = TRUE`<br>`  AND ca.audit_data_exclusao IS NULL`<br>`ORDER BY ca.CACR_DESCRICAO ASC;` |
| <a id="c8"></a>C8 | Card 8 — anos disponíveis para o filtro (RN13, alimenta [SB01](#sb01)).<br>`SELECT DISTINCT ano FROM (`<br>`  SELECT CAST(SUBSTRING(r.RECE_COMPETENCIA, 1, 4) AS UNSIGNED) AS ano`<br>`    FROM RECEITAS r JOIN CONTAS c ON c.CTA_ID = r.CTA_ID`<br>`    WHERE c.USU_ID = :usuId AND r.audit_data_exclusao IS NULL`<br>`  UNION`<br>`  SELECT CAST(SUBSTRING(d.DESP_COMPETENCIA, 1, 4) AS UNSIGNED) AS ano`<br>`    FROM DESPESAS d`<br>`    LEFT JOIN CONTAS c2          ON c2.CTA_ID  = d.CTA_ID`<br>`    LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`    WHERE (c2.USU_ID = :usuId OR cc.USU_ID = :usuId) AND d.audit_data_exclusao IS NULL`<br>`) anos`<br>`ORDER BY ano ASC;` |
| <a id="c9"></a>C9 | Card 8 — evolução mensal, quando `anoInicio == anoFim` (RN14).<br>`SELECT SUBSTRING(r.RECE_COMPETENCIA, 6, 2) AS mes, COALESCE(SUM(r.RECE_VALOR), 0) AS totalReceita`<br>`FROM RECEITAS r JOIN CONTAS c ON c.CTA_ID = r.CTA_ID`<br>`WHERE c.USU_ID = :usuId AND SUBSTRING(r.RECE_COMPETENCIA, 1, 4) = :ano AND r.audit_data_exclusao IS NULL`<br>`GROUP BY mes;`<br>`-- consulta equivalente sobre DESPESAS (mesmo agrupamento por mês) compõe totalDespesa;`<br>`-- o serviço une as duas por mês (01 a 12), preenchendo com zero o mês sem lançamento.` |
| <a id="c10"></a>C10 | Card 8 — evolução anual, quando `anoInicio != anoFim` (RN14).<br>`SELECT SUBSTRING(r.RECE_COMPETENCIA, 1, 4) AS ano, COALESCE(SUM(r.RECE_VALOR), 0) AS totalReceita`<br>`FROM RECEITAS r JOIN CONTAS c ON c.CTA_ID = r.CTA_ID`<br>`WHERE c.USU_ID = :usuId AND SUBSTRING(r.RECE_COMPETENCIA, 1, 4) BETWEEN :anoInicio AND :anoFim AND r.audit_data_exclusao IS NULL`<br>`GROUP BY ano;`<br>`-- consulta equivalente sobre DESPESAS (mesmo agrupamento por ano) compõe totalDespesa;`<br>`-- o serviço une as duas por ano, preenchendo com zero o ano sem lançamento.` |

---

## 12. Parâmetros de Sistema

Nenhum parâmetro novo. O padrão de competência inicial (`mes_atual - 1`) segue fixo, no mesmo critério dos documentos `08`/`09`, sem necessidade de configuração.

---

## 13. Permissões

Uma do módulo **Dashboard** (`PERM_MODULO = 'Dashboard'`). Faz parte do catálogo do código, sincronizado no mesmo padrão do Documento 0 ([QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26)). Convenção domínio-primeiro; vira a autoridade `PERM_DASHBOARD_VISUALIZAR`. Atômica, sem sufixo `MANTER` — a tela não tem operação de escrita.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `DASHBOARD_VISUALIZAR` — visualizar o conteúdo do Dashboard Financeiro. | [PERF01](#perf01), [PERF02](#perf02) |

> A permissão não distingue um usuário do outro — o que restringe cada um aos próprios dados é o escopo por `USU_ID` em cada consulta ([RN01](#rn01)).

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `DASHBOARD_VISUALIZAR` | ✓ | ✓ |

Os dois perfis de sistema recebem a permissão na carga inicial. Um perfil personalizado que não a receba (montado no editor de perfil do documento `02`) não vê conteúdo no Dashboard — o orquestrador ([documento 13](../13%20-%20dashboard/documento-analise-dashboard.md)) exibe o estado vazio de conteúdo indisponível ([RN07 do documento 13](../13%20-%20dashboard/documento-analise-dashboard.md#rn07)).

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. `PERF_FL_SISTEMA = TRUE`. Recebe `DASHBOARD_VISUALIZAR` na carga inicial. Vê apenas os próprios dados financeiros ([RN01](#rn01)). Corresponde a `ROLE_ADMIN`. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum. `PERF_FL_SISTEMA = TRUE`. Recebe `DASHBOARD_VISUALIZAR` na carga inicial. Vê apenas os próprios dados financeiros. Corresponde a `ROLE_USER`. |

---

## 15. Fluxo de Eventos

**Carregar o Dashboard Financeiro:**

```
1. Orquestrador (documento 13) delega a este módulo (tipo ativo = FINANCEIRO).
2. Serviço verifica PERM_DASHBOARD_VISUALIZAR do usuário autenticado.
        │
        └─ Sem a permissão → devolve "sem conteúdo" ao orquestrador (RN07 do documento 13).
3. Resolve os parâmetros: competencia (default mes_atual - 1), anoInicio/anoFim (default: menor
   ano com lançamento / ano corrente).
4. Calcula, em paralelo lógico, os 8 cards:
        Card 1 (C1) ─ saldo consolidado           (ignora competência)
        Card 2 (C2) ─ saldo por conta              (ignora competência)
        Card 3 (C3) ─ receitas x despesas          (usa competência)
        Card 4 (C4) ─ despesas por categoria       (usa competência)
        Card 5 (C5) ─ pagas x pendentes            (usa competência)
        Card 6 (C6) ─ rateio por pessoa            (usa competência)
        Card 7 (C7) ─ limite por cartão            (ignora competência)
        Card 8 (C8, C9 ou C10) ─ evolução          (usa anoInicio/anoFim, granularidade RN14)
5. Cada card sem dado no período exibe o estado vazio correspondente (RN15).
6. Modelo populado; página renderizada por completo, gráficos embutidos via th:inline="javascript".
```

**Trocar a competência global:**

```
1. Usuário altera o filtro de competência (ID1 do QUADRO_DESCRITIVO_1).
2. Navegador faz GET "/dashboard?competencia={nova}" (mantendo anoInicio/anoFim, se já filtrados) (RT01).
3. Sistema repete o fluxo "Carregar o Dashboard Financeiro" a partir do passo 3, com a nova competência.
   Cards 1, 2 e 7 não mudam de valor.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Carregar o dashboard com o filtro padrão

Dado que estou autenticado com um usuário que tem [PERM01](#perm01).
E que hoje é 15/09/2026.
Quando eu acessar o Dashboard.
Então o filtro de competência global deve estar em "08/2026" (`mes_atual - 1`).
E os 8 cards devem ser exibidos com os dados dessa competência.

### 16.1 Saldo consolidado ignora a competência

Dado que possuo duas contas com saldo, ambas com "considera no saldo geral" marcado.
Quando eu trocar a competência global de "08/2026" para "01/2025".
Então o valor do Card "Saldo Consolidado" não deve mudar.

### 16.2 Conta fora do saldo geral não entra no consolidado

Dado que possuo a conta "Carteira" com "considera no saldo geral" desmarcado e saldo de R$ 100,00.
E a conta "Nubank" com "considera no saldo geral" marcado e saldo de R$ 500,00.
Quando eu acessar o Dashboard.
Então o Card "Saldo Consolidado" deve exibir R$ 500,00.

### 16.3 Receitas x despesas do mês

Dado que tenho receitas somando R$ 3.000,00 e despesas somando R$ 1.200,00 na competência "08/2026".
Quando eu filtrar a competência global em "08/2026".
Então o Card "Receitas x Despesas" deve exibir R$ 3.000,00 de receita e R$ 1.200,00 de despesa.

### 16.4 Despesas por categoria com despesa sem categoria

Dado que tenho uma despesa de R$ 200,00 na categoria "Alimentação" e outra de R$ 50,00 sem categoria, ambas na competência filtrada.
Quando eu acessar o Dashboard.
Então o Card "Despesas por Categoria" deve exibir uma fatia "Alimentação" de R$ 200,00 e uma fatia "Sem categoria" de R$ 50,00.

### 16.5 Pagas x pendentes

Dado que tenho duas despesas pagas somando R$ 300,00 e uma pendente de R$ 150,00 na competência filtrada.
Quando eu acessar o Dashboard.
Então o Card "Pagas x Pendentes" deve exibir R$ 300,00 em Pagas e R$ 150,00 em Pendentes.

### 16.6 Total rateado por pessoa

Dado que ratear uma despesa de R$ 100,00 com o contato "Maria" (fatia de R$ 50,00) na competência filtrada.
Quando eu acessar o Dashboard.
Então o Card "Total Rateado por Pessoa" deve exibir "Maria: R$ 50,00".

### 16.7 Limite usado x disponível com limite definido

Dado que possuo o cartão "Nubank" com limite de R$ 1.000,00 e despesas pendentes de R$ 300,00 vinculadas a ele.
Quando eu acessar o Dashboard.
Então o Card "Limite por Cartão" deve exibir a barra do cartão "Nubank" com R$ 300,00 usado e R$ 700,00 disponível.

### 16.8 Limite nulo exibe só o valor usado

Dado que possuo o cartão "Itaú" sem limite definido e despesas pendentes de R$ 150,00 vinculadas a ele.
Quando eu acessar o Dashboard.
Então o Card "Limite por Cartão" deve exibir apenas "R$ 150,00 usado" para o cartão "Itaú", sem barra nem percentual.

### 16.9 Limite do cartão ignora a competência

Dado que o cartão "Nubank" tem R$ 300,00 usados, calculados sobre despesas pendentes sem filtro de competência.
Quando eu trocar a competência global.
Então o valor usado do cartão "Nubank" no Dashboard não deve mudar.

### 16.10 Cartão inativo não aparece no card de limite

Dado que possuo o cartão "Antigo" desativado (`CACR_FL_ATIVO = FALSE`).
Quando eu acessar o Dashboard.
Então o Card "Limite por Cartão" não deve exibir o cartão "Antigo".

### 16.11 Evolução mensal quando os dois anos são iguais

Dado que tenho lançamentos em vários meses de 2026.
Quando eu filtrar o Card "Evolução" com `anoInicio = 2026` e `anoFim = 2026`.
Então o sistema deve exibir o gráfico mensal com 12 pontos (Jan a Dez de 2026).

### 16.12 Evolução anual quando os anos são diferentes

Dado que tenho lançamentos em 2024, 2025 e 2026.
Quando eu filtrar o Card "Evolução" com `anoInicio = 2024` e `anoFim = 2026`.
Então o sistema deve exibir o gráfico anual com 3 pontos, um por ano de 2024 a 2026.

### 16.13 Opções de ano são dinâmicas

Dado que meus lançamentos mais antigos são de 2023 e os mais recentes de 2026.
Quando eu abrir os seletores de ano do Card "Evolução".
Então as opções devem ser exatamente 2023, 2024, 2025 e 2026 — nunca uma lista fixa.

### 16.14 Estado vazio de um card sem dado na competência

Dado que não tenho nenhuma despesa na competência "01/2020".
Quando eu filtrar a competência global em "01/2020".
Então os Cards "Despesas por Categoria" e "Pagas x Pendentes" devem exibir a mensagem de ausência de dados, não um gráfico vazio.

### 16.15 Escopo por usuário em todos os cards

Dado que o usuário A tem contas, despesas e receitas cadastradas, e o usuário B também.
Quando o usuário A acessar o Dashboard.
Então nenhum valor de nenhum card deve incluir dado do usuário B.

### 16.16 ADMIN também só vê os próprios dados

Dado que estou autenticado como ADMIN e existem despesas de outros usuários no sistema.
Quando eu acessar o Dashboard.
Então todos os cards devem refletir apenas os meus próprios dados financeiros.

### 16.17 Bloqueio de acesso sem a permissão

Dado que estou autenticado com um perfil personalizado sem `DASHBOARD_VISUALIZAR`.
Quando eu acessar "/dashboard".
Então a área de conteúdo do Dashboard deve exibir o estado de conteúdo indisponível, sem erro técnico.

---

## 17. Workshop de Análise

Data: 14/09/2026
Convidados: Diego Cordeiro
Participantes: Diego Cordeiro
Descrição: Sessão de brainstorming sobre o primeiro dashboard concreto, plugado na mecânica de orquestração do documento `13`. Levantamento a partir dos QUADRO_DESCRITIVO já existentes no Documento 0 (`CONTAS`, `RECEITAS`, `DESPESAS`, `DESPESAS_USUARIO`, `CATEGORIAS`, `CARTOES_CREDITO`, `CONTATOS`) e das observações em aberto já registradas nos documentos `07` (Observação 11, sobre limite disponível) e `09` (RN30, precedente de granularidade condicional).

**Decisões tomadas:**
- Tela **somente leitura**, 8 cards, sem tabela nova nem `ALTER TABLE`.
- Escopo row-level em todos os cards, inclusive para o ADMIN ([RN01](#rn01)).
- Filtro global de competência (`YearMonth` único, padrão `mes_atual - 1`), aplicado aos Cards 3, 4, 5 e 6; contrato uniforme de parâmetro em todos os cards, mesmo os que o ignoram (1, 2 e 7).
- Sem AJAX: toda troca de filtro recarrega a página inteira via GET; gráficos calculados no servidor e embutidos via `th:inline="javascript"`.
- Biblioteca de gráficos: ApexCharts (via webjar) — verificar/adicionar dependência no `pom.xml`.
- Card 7 (limite por cartão): usado = despesas pendentes vinculadas ao cartão, sem distinguir origem (manual ou importação) e sem considerar ciclo de fatura; disponível só quando há limite definido. Resolve o pendente já registrado no documento `07` (Observação 11).
- Card 6 (rateio por pessoa) não depende do CRUD de Contatos (documento `16`) estar pronto — lê `CONTATOS` e `DESPESAS_USUARIO` diretamente.
- Card 8 (evolução) tem filtro próprio de anos, independente da competência global, com opções dinâmicas e granularidade condicional (mensal quando `anoInicio == anoFim`, anual quando diferentes), no mesmo espírito do precedente da RN30 do documento `09`.
- Nova permissão atômica `DASHBOARD_VISUALIZAR`, concedida a ADMIN e USER.

**A Confirmar:**
- **Valores brutos × líquidos de rateio nos Cards 3, 4 e 5.** Esta versão soma `DESP_VALOR` bruto, sem deduzir a parte transferida a contatos via rateio — diferente do totalizador da listagem de Despesas ([RN30 do documento 09](../09%20-%20manter-despesa/documento-analise-manter-despesa.md#rn30)), que usa a cota líquida do titular (`valorUsuario`). Confirmar se o Dashboard deve adotar o mesmo critério de "cota líquida" para consistência entre as duas telas, ou se o valor bruto é intencional aqui (visão consolidada de todo o gasto, rateado ou não).
- **Cartão com despesas pendentes acima do limite.** [RN11](#rn11) prevê "disponível negativo" sem travar nada — confirmar se a tela deve destacar esse caso de alguma forma além da barra (ex.: cor de alerta, badge "Limite excedido").
- **Card 2 (saldo por conta) inclui contas com "considera no saldo geral" desmarcado?** Esta versão lista **todas** as contas ativas do usuário, independentemente da flag — só o Card 1 (consolidado) filtra por ela. Confirmar se esse é o comportamento desejado.
- **Ordenação e limite de itens no Card 6 (rateio por pessoa).** Esta versão não define um teto de contatos exibidos; confirmar se, com muitos contatos, a tela deve paginar, limitar aos N maiores ou manter a lista completa.
- **Ano corrente sem nenhum lançamento no Card 8.** Esta versão usa o ano corrente como `anoFim` padrão mesmo sem lançamento nele; confirmar se o padrão deveria ser, em vez disso, o ano do lançamento mais recente do usuário.

---

## 18. Anexos

- Documento 0 — Fundação: `../00 - analise-geral/documento-0-fundacao.md` ([QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), [_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), [_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11), [_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3), [_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6), [_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29)).
- Documento `13 - dashboard`: `../13 - dashboard/documento-analise-dashboard.md` (mecânica de orquestração; este documento é o tipo `FINANCEIRO` do catálogo).
- Documento `06 - manter-conta`: `../06 - manter-conta/documento-analise-manter-conta.md` (fonte dos Cards 1 e 2).
- Documento `07 - manter-cartao-credito`: `../07 - manter-cartao-credito/documento-analise-manter-cartao-credito.md` (Observação 11 — pendência de limite disponível, resolvida pelo Card 7 desta versão).
- Documento `08 - manter-receita`: `../08 - manter-receita/documento-analise-manter-receita.md` (fonte do Card 3 e do Card 8).
- Documento `09 - manter-despesa`: `../09 - manter-despesa/documento-analise-manter-despesa.md` (RN30 — precedente da granularidade condicional do Card 8; fonte dos Cards 3 a 8; DESPESAS_USUARIO do rateio, Card 6).
- Documento `16 - manter-contato`: `../16 - manter-contato` (CRUD de `CONTATOS`, consumido só como leitura pelo Card 6; não é dependência de implementação).
