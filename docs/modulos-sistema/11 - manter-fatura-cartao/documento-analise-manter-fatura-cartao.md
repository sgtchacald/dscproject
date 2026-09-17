# dscproject — Análise de Sistemas
## Módulo Faturas de Cartão — USER / ADMIN — Manter Fatura de Cartão

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
| 1.0 | 15/09/2026 | Diego dos Santos Cordeiro | Criação do documento. Gestão do ciclo de vida das **faturas mensais dos cartões de crédito do usuário** (tela "Finanças > Faturas de Cartão") para a geração 2, implementando a tabela `FATURAS_CARTAO` ([QUADRO_DESCRITIVO_8 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8)). Modela a progressão de estados (`ABERTA` → `FECHADA` → `PAGA` / `PAGA_PARCIAL`), o cálculo dinâmico do valor parcial em aberto a partir das compras em `DESPESAS`, o fechamento com congelamento do valor total e encargos, a reabertura de faturas fechadas e a quitação com geração automática ou vínculo de débito em conta (`TRANSACOES_BANCARIAS` com `TRBA_FL_PAGAMENTO_FATURA = TRUE`). Escopo *row-level* por usuário, derivado do cartão (`CACR_ID → CARTOES_CREDITO.USU_ID`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova** nem altera schema. |
| D04 | `FATURAS_CARTAO` não tem `USU_ID` próprio ([QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8)); o dono da fatura é o titular do cartão vinculado (`FTCA.CACR_ID → CARTOES_CREDITO.USU_ID`). O escopo por usuário (*row-level*) é sempre resolvido **no serviço**, a partir do contexto de segurança — nunca de um parâmetro da requisição. |
| D05 | Uma fatura representa um ciclo mensal de cartão único (`CACR_ID + FTCA_COMPETENCIA` únicos no banco). Enquanto `ABERTA`, o valor exibido reflete dinamicamente a soma das compras lançadas em `DESPESAS` para o cartão na competência; quando `FECHADA`, o valor é congelado em `FTCA_VALOR_TOTAL` com eventual acréscimo de encargos (`FTCA_VALOR_ENCARGOS`). |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Fatura de Cartão** do `dscproject-spring-mvc` — a gestão do ciclo de vida, consolidação e pagamento das **faturas mensais dos cartões de crédito do próprio usuário**.

Na **geração 1** (API REST + SPA Angular), cartões de crédito e faturas **não existiam como entidades próprias**. As compras no cartão eram lançadas como despesas avulsas com identificação informal ou importadas via extrato de cartão sem fechamento formal de ciclo.

O **Documento 0** ([QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8), Observações 15, 16, 18 e 28) introduziu a tabela `FATURAS_CARTAO` como uma entidade de controle fino do ciclo de crédito:

- cada fatura é estritamente vinculada a um cartão (`CACR_ID`) e a uma competência (`FTCA_COMPETENCIA`, no formato `yyyy-MM`), possuindo restrição de unicidade `uq_faturas_cartao_ciclo (CACR_ID, FTCA_COMPETENCIA)`;
- o ciclo de vida é regido pelo enum `StatusFatura`:
  - **`ABERTA`:** fatura em andamento no mês corrente ou futuro. O valor não é estático: é calculado dinamicamente somando todas as `DESPESAS` associadas ao cartão naquela competência. `FTCA_DT_FECHAMENTO` é nula;
  - **`FECHADA`:** ciclo de compras encerrado na data de corte (`FTCA_DT_FECHAMENTO`). O valor total de compras é congelado em `FTCA_VALOR_TOTAL`, somando eventuais encargos (`FTCA_VALOR_ENCARGOS`), juros, IOF ou anuidade. Novas despesas lançadas com data posterior caem na fatura seguinte;
  - **`PAGA`:** valor quitado integralmente (`FTCA_VALOR_PAGO >= FTCA_VALOR_TOTAL`). Aponta para a transação bancária de débito que liquidou a fatura (`TRBA_ID_PAGAMENTO`);
  - **`PAGA_PARCIAL`:** valor pago inferior ao total fechado (`0 < FTCA_VALOR_PAGO < FTCA_VALOR_TOTAL`), deixando o saldo remanescente sujeito a juros de rotativo para o próximo ciclo;
- a liquidação da fatura gera ou associa uma `TransacaoBancaria` de débito na conta bancária selecionada (sugerindo a conta padrão de débito configurada no cartão de crédito), marcando-a com `TRBA_FL_PAGAMENTO_FATURA = TRUE` para não distorcer relatórios de despesas por categoria;
- suporte a **reabertura de fatura**: uma fatura fechada pode ser reaberta para inclusão ou retificação de compras, desde que ainda não tenha sido paga.

Este documento cobre:

- a **tela Faturas de Cartão** — seleção do cartão, visualização das faturas por competência, status e indicadores;
- o **detalhamento das compras** que compõem cada fatura (integração visual com `DESPESAS`);
- a operação de **Fechamento de Fatura** com conferência de compras e inclusão de encargos;
- a operação de **Reabertura de Fatura**;
- a operação de **Registrar Pagamento** (quitação total ou parcial via débito em conta bancária);
- o **escopo por usuário**: o usuário gerencia exclusivamente faturas dos cartões de sua titularidade.

**Escopo deste documento:**
- Visualização de faturas por cartão e navegação entre competências (`mes_atual - 1`, mês corrente e histórico).
- Cálculo dinâmico de compras em `DESPESAS` para faturas com status `ABERTA`.
- Modal de **detalhamento de lançamentos**: listagem das despesas incluídas na fatura com data, descrição, categoria, valor total e cota do titular.
- Modal de **fechamento de fatura**: congelamento do valor de compras, definição de data de fechamento e inserção de encargos/mínimo.
- Modal de **registro de pagamento**: escolha da conta bancária de débito (com sugestão da conta padrão do cartão), data do pagamento, valor pago (total ou parcial) e criação do débito em `TRANSACOES_BANCARIAS`.
- Ação de **reabertura de fatura** (retorna de `FECHADA` para `ABERTA`).
- Definição das permissões atômicas `FATURAS_LISTAR`, `FATURAS_FECHAR`, `FATURAS_REABRIR`, `FATURAS_PAGAR` e `FATURAS_EDITAR`.

**Não contempla:**
- CRUD de **Cartão de Crédito** (`CARTOES_CREDITO`) — documento `07 - manter-cartao-credito`.
- CRUD de **Despesas** (`DESPESAS`) — documento `09 - manter-despesa`. As despesas no cartão são criadas e editadas naquele módulo.
- CRUD de **Contas** (`CONTAS`) — documento `06 - manter-conta`.
- Sincronização direta de faturas do provedor Open Finance — documento `15`.

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER). A tela é estritamente do **próprio usuário** — cada um opera apenas sobre faturas dos seus próprios cartões ([RN02](#rn02)).

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **Dono indireto pelo cartão.** A tabela `FATURAS_CARTAO` não possui `USU_ID` próprio ([QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8)); o dono é o titular do cartão vinculado (`FTCA.CACR_ID → CARTOES_CREDITO.USU_ID`). | [RN02](#rn02), [RN03](#rn03) |
| 2 | **Unicidade de fatura por ciclo mensal.** Não pode existir mais de uma fatura para o mesmo cartão na mesma competência (`UNIQUE (CACR_ID, FTCA_COMPETENCIA)`). | [RN04](#rn04), [RNF04](#rnf04) |
| 3 | **Cálculo dinâmico enquanto ABERTA.** Enquanto o status for `ABERTA`, o valor total não fica gravado em `FTCA_VALOR_TOTAL` (permanece `NULL` no banco). A tela soma dinamicamente as despesas do cartão na competência via [C3](#c3). | [RN05](#rn05), [RT02](#rt02) |
| 4 | **Congelamento do valor no fechamento.** Ao executar a ação "Fechar Fatura", o sistema soma as compras ativas do cartão, soma os encargos informados (`FTCA_VALOR_ENCARGOS`) e grava o resultado autoritativo em `FTCA_VALOR_TOTAL`, preenchendo `FTCA_DT_FECHAMENTO` e mudando o status para `FECHADA`. | [RN06](#rn06), [EDP04](#edp04) |
| 5 | **Reabertura permitida se não paga.** Uma fatura com status `FECHADA` pode ser reaberta ([EDP05](#edp05)) desde que `FTCA_STATUS != 'PAGA'` e `FTCA_STATUS != 'PAGA_PARCIAL'`. Ao reabrir, `FTCA_STATUS` volta para `ABERTA`, `FTCA_VALOR_TOTAL` e `FTCA_DT_FECHAMENTO` voltam a ser `NULL`. | [RN07](#rn07), [EDP05](#edp05) |
| 6 | **Pagamento gera Transação Bancária.** Ao registrar a quitação da fatura ([EDP06](#edp06)), o sistema debita o valor na conta bancária informada, gerando um registro em `TRANSACOES_BANCARIAS` com `TRBA_NATUREZA_MOVIMENTO = 'DEBITO'`, `TRBA_FL_PAGAMENTO_FATURA = TRUE` e vinculando `FTCA.TRBA_ID_PAGAMENTO`. | [RN08](#rn08), [RN09](#rn09) |
| 7 | **Pagamento parcial e saldo rotativo.** Se o valor pago for inferior ao valor total fechado, a fatura passa para `PAGA_PARCIAL`. O saldo devedor remanescente (`FTCA_VALOR_TOTAL - FTCA_VALOR_PAGO`) fica disponível para ser lançado como encargo/rotativo na fatura da competência seguinte. | [RN10](#rn10), [MSG10](#msg10) |
| 8 | **Data de Vencimento padrão.** Ao instanciar ou sugerir uma nova fatura, a data de vencimento (`FTCA_DT_VENCIMENTO`) é calculada a partir do dia de vencimento cadastrado no cartão de crédito (`CARTOES_CREDITO.CACR_DIA_VENCIMENTO`) aplicado ao mês/ano da competência. | [RN11](#rn11), [RT11](#rt11) |
| 9 | **Soft Delete e integridade de histórico.** Faturas que já possuam pagamento registrado não podem ser excluídas sem antes realizar o estorno/cancelamento do pagamento associado. | [RN12](#rn12), [MSG12](#msg12) |
| 10 | **Máscara monetária contínua.** Todos os inputs de valores monetários (encargos, valor pago, total) seguem o padrão `pt-BR` (`R$ 0,00`). | [RNF06](#rnf06) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as faturas dos cartões de crédito do usuário autenticado, com filtro por cartão e competência. | Alta | Analisado |
| <a id="rf02"></a>RF02 | O sistema deve exibir o resumo da fatura: cartão, competência, data de fechamento, data de vencimento, total de compras, encargos, total geral, valor pago e status. | Alta | Analisado |
| <a id="rf03"></a>RF03 | O sistema deve calcular dinamicamente o total de compras das despesas ativas vinculadas ao cartão na competência enquanto a fatura estiver `ABERTA`. | Alta | Analisado |
| <a id="rf04"></a>RF04 | O sistema deve permitir visualizar o detalhamento de todas as compras/despesas individuais que compõem a fatura via modal dedicado. | Alta | Analisado |
| <a id="rf05"></a>RF05 | O sistema deve permitir fechar a fatura, congelando o total de compras, data de fechamento e permitindo informar encargos (juros/IOF) e valor mínimo. | Alta | Analisado |
| <a id="rf06"></a>RF06 | O sistema deve permitir reabrir uma fatura fechada que ainda não tenha recebido pagamentos. | Alta | Analisado |
| <a id="rf07"></a>RF07 | O sistema deve permitir registrar pagamento total ou parcial de uma fatura fechada, debitando o valor em conta bancária e gerando transação de pagamento. | Alta | Analisado |
| <a id="rf08"></a>RF08 | O sistema deve sugerir automaticamente a conta bancária padrão de débito configurada no cartão ao abrir o modal de pagamento. | Média | Analisado |
| <a id="rf09"></a>RF09 | O sistema deve atualizar o status para `PAGA` quando o valor pago igualar ou superar o total da fatura, ou `PAGA_PARCIAL` se o valor pago for menor. | Alta | Analisado |
| <a id="rf10"></a>RF10 | O sistema deve garantir que o usuário acesse e altere apenas faturas dos cartões pertencentes ao seu próprio usuário autenticado. | Alta | Analisado |
| <a id="rf11"></a>RF11 | O sistema deve calcular a data de vencimento padrão com base no dia de vencimento configurado no cartão de crédito. | Média | Analisado |
| <a id="rf12"></a>RF12 | O sistema deve impedir fechamento de fatura sem data de fechamento ou com data de fechamento posterior à data de vencimento. | Alta | Analisado |
| <a id="rf13"></a>RF13 | O sistema deve impedir exclusão ou reabertura de faturas com status `PAGA` ou `PAGA_PARCIAL` sem o devido cancelamento do pagamento. | Alta | Analisado |
| <a id="rf14"></a>RF14 | O sistema deve preservar o cartão selecionado e a navegação no grid após mutações. | Média | Analisado |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada operação exige autoridade atômica específica (`PERM_FATURAS_LISTAR`, `PERM_FATURAS_FECHAR`, `PERM_FATURAS_REABRIR`, `PERM_FATURAS_PAGAR`, `PERM_FATURAS_EDITAR`), com proibição expressa de `FATURAS_MANTER`. | Testes de acesso com perfis ADMIN, USER e sem permissão. |
| <a id="rnf02"></a>RNF02 | Isolamento | Nenhuma consulta com `{id}` pode retornar ou manipular fatura de cartão de outro usuário — deve responder HTTP 404 ([MSG05](#msg05)). | Teste automatizado chamando endpoints com IDs de outro usuário. |
| <a id="rnf03"></a>RNF03 | Auditoria | A tabela `FATURAS_CARTAO` é auditada via Hibernate Envers (`@Audited`), registrando fechamento, reabertura, pagamento e alterações de encargos. | Verificação na tabela `FATURAS_CARTAO_aud`. |
| <a id="rnf04"></a>RNF04 | Integridade | A restrição de chave única `(CACR_ID, FTCA_COMPETENCIA)` é garantida no banco e validada na camada de serviço. | Teste de inserção de fatura duplicada para o mesmo ciclo. |
| <a id="rnf05"></a>RNF05 | Consistência | O registro de pagamento e a criação da `TransacaoBancaria` de débito devem ocorrer sob a mesma transação `@Transactional`. | Teste de rollback simulando falha na geração do débito. |
| <a id="rnf06"></a>RNF06 | Usabilidade | Interface Tabler UI com indicadores visuais de status (badge verde para PAGA, amarelo para FECHADA, cinza para ABERTA, azul para PAGA_PARCIAL). | Validação visual no navegador. |

---

## 4. Casos de Uso

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Faturas de Cartão | [PERF01](#perf01), [PERF02](#perf02) | O usuário visualiza as faturas mensais de seus cartões de crédito. ([RF01](#rf01), [RF02](#rf02)) |
| <a id="caus02"></a>CAUS02 | Visualizar Compras da Fatura | [PERF01](#perf01), [PERF02](#perf02) | O usuário consulta a listagem de despesas vinculadas ao ciclo da fatura. ([RF04](#rf04)) |
| <a id="caus03"></a>CAUS03 | Fechar Fatura | [PERF01](#perf01), [PERF02](#perf02) | O usuário encerra o ciclo de compras, consolida o valor e aplica encargos. ([RF05](#rf05), [RF12](#rf12)) |
| <a id="caus04"></a>CAUS04 | Reabrir Fatura | [PERF01](#perf01), [PERF02](#perf02) | O usuário reverte uma fatura fechada para o estado aberta para ajustes. ([RF06](#rf06)) |
| <a id="caus05"></a>CAUS05 | Registrar Pagamento de Fatura | [PERF01](#perf01), [PERF02](#perf02) | O usuário liquida a fatura total ou parcialmente gerando débito em conta. ([RF07](#rf07), [RF08](#rf08), [RF09](#rf09)) |
| <a id="caus06"></a>CAUS06 | Ajustar Encargos da Fatura | [PERF01](#perf01), [PERF02](#perf02) | O usuário edita os valores de encargos e valor mínimo da fatura fechada. ([RF05](#rf05)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Finanças > Faturas de Cartão

**Critérios de Aceitação:**
- O menu 'Faturas de Cartão' é visível exclusivamente para usuários com [PERM01](#perm01).
- Ao acessar a tela, o usuário seleciona um dos seus cartões ativos; caso possua apenas um cartão, ele é pré-selecionado automaticamente.
- A listagem exibe as faturas organizadas cronologicamente por competência decrescente.
- Para faturas com status `ABERTA`:
  - O valor exibido soma em tempo real todas as despesas ativas do cartão na competência;
  - O botão de ação principal é "Fechar Fatura" ([RT04](#rt04));
  - O botão "Registrar Pagamento" permanece desabilitado até que a fatura seja fechada.
- Para faturas com status `FECHADA`:
  - O valor total congelado é exibido, somado aos encargos;
  - Ações disponíveis: "Ver Compras", "Registrar Pagamento" ([RT06](#rt06)) e "Reabrir Fatura" ([RT05](#rt05)).
- Para faturas com status `PAGA` ou `PAGA_PARCIAL`:
  - O valor pago e a data de liquidação são exibidos com badge de quitação;
  - O botão "Reabrir" fica oculto e protegido contra acionamento.
- Ao confirmar o pagamento de uma fatura:
  - Um débito correspondente em `TRANSACOES_BANCARIAS` é criado com flag de pagamento de fatura ativada;
  - A chave estrangeira `TRBA_ID_PAGAMENTO` da fatura é atualizada;
  - O status da fatura é atualizado para `PAGA` (ou `PAGA_PARCIAL`).

---

## 6. Banco de Dados

A estrutura está integralmente definida no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova nem altera schema**.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `FATURAS_CARTAO` | Documento 0 — [QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8) | Tabela principal do ciclo de vida de faturas |
| `CARTOES_CREDITO` | Documento 0 — [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6) | Âncora de titularidade (`CACR_ID → CARTOES_CREDITO.USU_ID`) e dados do cartão (vencimento, fechamento, limite) |
| `DESPESAS` | Documento 0 — [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) | Compras no cartão (`DESP.CACR_ID = CACR_ID`) que compõem o valor da fatura na competência |
| `TRANSACOES_BANCARIAS` | Documento 0 — [QUADRO_DESCRITIVO_7](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7) | Débito em conta bancária gerado pela quitação da fatura (`TRBA_ID_PAGAMENTO`) |
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | Conta bancária de onde saem os fundos para liquidação da fatura |

### 6.1 Diagrama ER

O relacionamento do ciclo é: `CARTOES_CREDITO (1) ──< (N) FATURAS_CARTAO (0..1) >── (1) TRANSACOES_BANCARIAS (1) >── (1) CONTAS`. As despesas do cartão relacionam-se por: `CARTOES_CREDITO (1) ──< (N) DESPESAS`.

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| `FATURAS_CARTAO` | `FATURAS_CARTAO_aud` | Competência, data de fechamento, data de vencimento, valor total, valor mínimo, encargos, valor pago, status, origem, cartão e ID da transação de pagamento. |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. As transições de ciclo de vida e conciliações são executadas pelo serviço Spring sob controle transacional.

---

## 7. Protótipos de Interface

### <a id="quadro-descritivo-1"></a>7.1 Tela: Faturas de Cartão (Listagem) — QUADRO_DESCRITIVO_1

> OBSERVAÇÕES: Tela acessada via 'Finanças > Faturas de Cartão'. Restrita a [PERM01](#perm01). Apresenta seletor de cartão no topo e tabela com as faturas por competência.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/faturas-cartao/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Finanças > Faturas de Cartão | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Faturas de Cartão | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Acompanhe, feche e registre o pagamento das faturas dos seus cartões. | — |
| <a id="qdd1-4"></a>4 | SELETOR DE CARTÃO | Tipo: Combobox<br>Opções: Cartões ativos do usuário | Ver [SB01](#sb01). Ao trocar, recarrega o grid ([RT01](#rt01)). |
| <a id="qdd1-4a"></a>4.1 | BUSCA POR COMPETÊNCIA | Tipo: Input mês (`type="month"`) com botão de limpar<br>Placeholder: AAAA-MM | Filtro de competência posicionado ao lado do seletor de cartões ([RT15](#rt15)). |
| <a id="qdd1-5"></a>5 | CARD LIMITE DISPONÍVEL | Tipo: Card numérico<br>Texto: Limite Usado / Total | Mostra consumo consolidado do cartão selecionado. |
| <a id="qdd1-6"></a>6 | GRID DE FATURAS | Tipo: Grid (DataTables, client-side)<br>Endpoint: [EDP02](#edp02) | Lista faturas do cartão selecionado via [C1](#c1). |
| <a id="qdd1-7"></a>7 | COMPETÊNCIA | Tipo: Coluna<br>Ordenação: Sim | Exibe `FTCA_COMPETENCIA` ("MM/AAAA"). |
| <a id="qdd1-8"></a>8 | VENCIMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe `FTCA_DT_VENCIMENTO` ("DD/MM/AAAA"). |
| <a id="qdd1-9"></a>9 | FECHAMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe `FTCA_DT_FECHAMENTO` ou traço quando aberta. |
| <a id="qdd1-10"></a>10 | STATUS | Tipo: Coluna (badge)<br>Ordenação: Sim | Aberta (cinza), Fechada (amarelo), Paga (verde), Paga Parcial (azul). |
| <a id="qdd1-11"></a>11 | COMPRAS | Tipo: Coluna (moeda)<br>Ordenação: Sim | Total de compras em despesas no cartão na competência. |
| <a id="qdd1-12"></a>12 | ENCARGOS | Tipo: Coluna (moeda)<br>Ordenação: Sim | `FTCA_VALOR_ENCARGOS` (juros, multas, anuidade). |
| <a id="qdd1-13"></a>13 | TOTAL FATURA | Tipo: Coluna (moeda em destaque)<br>Ordenação: Sim | Valor final a pagar da fatura. |
| <a id="qdd1-14"></a>14 | VALOR PAGO | Tipo: Coluna (moeda)<br>Ordenação: Sim | `FTCA_VALOR_PAGO` registrado na quitação. |
| <a id="qdd1-15"></a>15 | AÇÕES | Tipo: Coluna (alinhada à esquerda) | Ações [ID16](#qdd1-16). |
| <a id="qdd1-16"></a>16 | BOTÕES DE AÇÃO | Tipo: Botões/Ícones<br>Ver compras (ícone: eye)<br>Fechar fatura (ícone: lock)<br>Pagar (ícone: credit-card)<br>Reabrir (ícone: lock-open) | Ver compras → [RT03](#rt03). Fechar fatura → [RT04](#rt04) ([PERM02](#perm02)). Pagar → [RT06](#rt06) ([PERM04](#perm04)). Reabrir → [RT05](#rt05) ([PERM03](#perm03)). |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Detalhes das Compras da Fatura — QUADRO_DESCRITIVO_2

> OBSERVAÇÕES: Exibe todas as compras (`DESPESAS`) atreladas àquele cartão na competência consultada.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Compras da Fatura — {Cartão} ({Competência}) | — |
| <a id="qdd2-2"></a>2 | GRID DE COMPRAS | Tipo: Grid de visualização | Carrega despesas via [C2](#c2). |
| <a id="qdd2-3"></a>3 | DATA | Tipo: Coluna | Data de lançamento da despesa. |
| <a id="qdd2-4"></a>4 | DESCRIÇÃO | Tipo: Coluna | Nome/descrição da despesa e indicador de parcela (`Nx`). |
| <a id="qdd2-5"></a>5 | CATEGORIA | Tipo: Coluna (badge) | Categoria da despesa. |
| <a id="qdd2-6"></a>6 | VALOR TOTAL | Tipo: Coluna (moeda) | Valor bruto da compra. |
| <a id="qdd2-7"></a>7 | SUA COTA | Tipo: Coluna (moeda) | Cota líquida do titular após eventuais rateios com contatos. |
| <a id="qdd2-8"></a>8 | TOTAL DA FATURA | Tipo: Texto em destaque | Somatório total das compras da fatura. |
| <a id="qdd2-9"></a>9 | BOTÃO FECHAR | Tipo: Botão | Fecha a modal. |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Fechar Fatura — QUADRO_DESCRITIVO_3

> OBSERVAÇÕES: Encerra o ciclo e congela o valor da fatura. Exige [PERM02](#perm02) (`FATURAS_FECHAR`).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Fechar Fatura — {Cartão} ({Competência}) | — |
| <a id="qdd3-2"></a>2 | DATA DE FECHAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Default: data atual | Grava `FTCA_DT_FECHAMENTO`. |
| <a id="qdd3-3"></a>3 | DATA DE VENCIMENTO | Tipo: Input Date<br>Obrigatório: Sim | Grava `FTCA_DT_VENCIMENTO`. Sugere o dia de vencimento do cartão. |
| <a id="qdd3-4"></a>4 | TOTAL EM COMPRAS | Tipo: Input monetário (somente leitura) | Soma dinâmica de compras na competência. |
| <a id="qdd3-5"></a>5 | ENCARGOS / JUROS / ANUIDADE | Tipo: Input monetário<br>Máscara: `pt-BR`<br>Default: R$ 0,00 | Grava `FTCA_VALOR_ENCARGOS`. |
| <a id="qdd3-6"></a>6 | VALOR TOTAL DA FATURA | Tipo: Input monetário (calculado)<br>Fórmula: Compras + Encargos | Grava `FTCA_VALOR_TOTAL`. |
| <a id="qdd3-7"></a>7 | PAGAMENTO MÍNIMO | Tipo: Input monetário<br>Obrigatório: Não | Grava `FTCA_VALOR_MINIMO`. |
| <a id="qdd3-8"></a>8 | BOTÃO CONFIRMAR FECHAMENTO | Tipo: Botão (primário)<br>Texto: Confirmar Fechamento | Ao clicar, executar [RT04](#rt04). |
| <a id="qdd3-9"></a>9 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem alterar status. |

### <a id="quadro-descritivo-4"></a>7.4 Modal: Registrar Pagamento de Fatura — QUADRO_DESCRITIVO_4

> OBSERVAÇÕES: Registra a quitação e gera transação de débito bancário. Exige [PERM04](#perm04) (`FATURAS_PAGAR`).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Registrar Pagamento da Fatura — {Cartão} ({Competência}) | — |
| <a id="qdd4-2"></a>2 | TOTAL DA FATURA | Tipo: Texto em destaque | Exibe `FTCA_VALOR_TOTAL`. |
| <a id="qdd4-3"></a>3 | DATA DO PAGAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Default: data atual | Data do débito na conta bancária. |
| <a id="qdd4-4"></a>4 | CONTA DE DÉBITO | Tipo: Combobox<br>Obrigatório: Sim | Contas ativas do usuário. Sugere a conta padrão do cartão ([SB02](#sb02)). |
| <a id="qdd4-5"></a>5 | VALOR A PAGAR | Tipo: Input monetário<br>Máscara: `pt-BR`<br>Default: Valor total da fatura | Permite pagamento integral ou parcial ([RN10](#rn10)). |
| <a id="qdd4-6"></a>6 | BOTÃO CONFIRMAR PAGAMENTO | Tipo: Botão (primário)<br>Texto: Confirmar Pagamento<br>Endpoint: [EDP06](#edp06) | Ao clicar, executar [RT07](#rt07). |
| <a id="qdd4-7"></a>7 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem liquidar. |

### 7.5 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | CARTÃO DE CRÉDITO | Lista dos cartões de crédito ativos pertencentes ao usuário autenticado (`GET /cartoes/opcoes` do documento `07`). |
| <a id="sb02"></a>SB02 | CONTA DE DÉBITO | Lista das contas bancárias ativas pertencentes ao usuário autenticado (`GET /contas/opcoes` do documento `06`). |
| <a id="sb03"></a>SB03 | COMPETÊNCIA | Seletor mês/ano (`YearMonth`, `yyyy-MM`). |
| <a id="sb04"></a>SB04 | STATUS FATURA | Domínio: Aberta (`ABERTA`), Fechada (`FECHADA`), Paga (`PAGA`), Paga Parcial (`PAGA_PARCIAL`). |

### 7.6 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao selecionar um cartão de crédito no seletor ([ID4](#qdd1-4)), recarregar o grid de faturas via [EDP02](#edp02) para o cartão escolhido, aplicando ordenação inicial por competência ascendente ([RT15](#rt15)). |
| <a id="rt02"></a>RT02 | Para faturas com status `ABERTA`, renderizar o valor total dinâmico baseado no total de compras em despesas, exibindo badge indicativo cinza "Aberta". |
| <a id="rt03"></a>RT03 | Ao clicar em "Ver compras" ([ID16](#qdd1-16)), abrir o modal [QUADRO_DESCRITIVO_2](#quadro-descritivo-2) consultando as despesas via [EDP03](#edp03). |
| <a id="rt04"></a>RT04 | Ao clicar em "Fechar fatura" ([ID16](#qdd1-16)) — visível apenas para faturas abertas e com [PERM02](#perm02) —, abrir o modal [QUADRO_DESCRITIVO_3](#quadro-descritivo-3). Ao submeter, chamar [EDP04](#edp04) com data de fechamento, data de vencimento e encargos. Em sucesso, exibir [MSG01](#msg01) e recarregar o grid ([RT14](#rt14)). |
| <a id="rt05"></a>RT05 | Ao clicar em "Reabrir fatura" ([ID16](#qdd1-16)) — visível apenas para faturas fechadas e sem pagamento —, solicitar confirmação ([MSG06](#msg06)). Ao confirmar, chamar [EDP05](#edp05). Em sucesso, exibir [MSG07](#msg07) e recarregar o grid ([RT14](#rt14)). |
| <a id="rt06"></a>RT06 | Ao clicar em "Pagar" ([ID16](#qdd1-16)) — visível apenas para faturas fechadas ou parcialmente pagas e com [PERM04](#perm04) —, abrir modal [QUADRO_DESCRITIVO_4](#quadro-descritivo-4) pré-preenchendo a conta de débito padrão do cartão e o valor total devido. |
| <a id="rt07"></a>RT07 | Ao confirmar pagamento no modal: validar conta e valor maior que zero ([MSG03](#msg03)). Chamar [EDP06](#edp06). Em sucesso, exibir [MSG08](#msg08), fechar o modal e recarregar o grid ([RT14](#rt14)). |
| <a id="rt08"></a>RT08 | Desabilitar o botão de registrar pagamento para faturas que se encontrem com status `ABERTA`. |
| <a id="rt09"></a>RT09 | Ocultar a ação de reabrir fatura para registros com status `PAGA` ou `PAGA_PARCIAL`. |
| <a id="rt10"></a>RT10 | No modal de fechamento, calcular o valor total em tempo real no formulário sempre que o usuário digitar encargos: `Total = Compras + Encargos`. |
| <a id="rt11"></a>RT11 | A data de vencimento é sugerida calculando o dia de vencimento do cartão sobre o mês/ano da fatura. |
| <a id="rt12"></a>RT12 | Aplicar formatação monetária `pt-BR` em todas as colunas e inputs de valores. |
| <a id="rt13"></a>RT13 | Faturas importadas de Open Finance (`FTCA_ORIGEM = 'OPEN_FINANCE'`) exibem badge indicativo e não permitem reabertura. |
| <a id="rt14"></a>RT14 | Manter o cartão de crédito ativo selecionado e a página de navegação após qualquer mutação de fatura. |
| <a id="rt15"></a>RT15 | A ordenação inicial e padrão do grid de faturas deve ser da menor competência para a maior competência (`ASC`). O campo de busca por competência ([ID4.1](#qdd1-4a)) permite filtrar instantaneamente as faturas exibidas no client-side; o botão de limpar redefine o filtro mantendo todos os registros visíveis. |

---

## 8. Endpoints

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) (`FATURAS_LISTAR`) | /faturas-cartao/listar | N |
| Retorna a página Thymeleaf de visualização das faturas de cartão de crédito. | | | | |
| <a id="edp02"></a>EDP02 | GET | [PERM01](#perm01) (`FATURAS_LISTAR`) | /faturas-cartao/dados?cartaoId={id} | N |
| Retorna em JSON a lista de faturas do cartão informado pertencente ao usuário autenticado. Executa [C1](#c1). Dados: id, competencia, dtFechamento, dtVencimento, valorTotal, valorEncargos, valorPago, status, origem, totalCompras. | | | | |
| <a id="edp03"></a>EDP03 | GET | [PERM01](#perm01) (`FATURAS_LISTAR`) | /faturas-cartao/{id}/compras | N |
| Retorna a listagem detalhada de todas as compras de despesas do cartão associadas à fatura na competência. Executa [C2](#c2). | | | | |
| <a id="edp04"></a>EDP04 | POST | [PERM02](#perm02) (`FATURAS_FECHAR`) | /faturas-cartao/{id}/fechar | N |
| Executa o fechamento formal da fatura. Validações: [RN02](#rn02) (posse do cartão), [RN06](#rn06) (consolida total compras + encargos, grava `FTCA_DT_FECHAMENTO` e muda status para `FECHADA`). Payload: dtFechamento, dtVencimento, valorEncargos, valorMinimo. Retorno: 200 ([MSG01](#msg01)) ou 422. | | | | |
| <a id="edp05"></a>EDP05 | POST | [PERM03](#perm03) (`FATURAS_REABRIR`) | /faturas-cartao/{id}/reabrir | N |
| Reabre uma fatura fechada que não tenha sido paga ([RN07](#rn07)). Retorna o status para `ABERTA`, limpa `FTCA_VALOR_TOTAL` e `FTCA_DT_FECHAMENTO`. Retorno: 200 ([MSG07](#msg07)) ou 422 ([MSG11](#msg11)). | | | | |
| <a id="edp06"></a>EDP06 | POST | [PERM04](#perm04) (`FATURAS_PAGAR`) | /faturas-cartao/{id}/pagar | N |
| Registra quitação total ou parcial da fatura ([RN08](#rn08), [RN10](#rn10)). Sob `@Transactional`, cria débito em `TRANSACOES_BANCARIAS` (`TRBA_FL_PAGAMENTO_FATURA = TRUE`), atualiza `FTCA_VALOR_PAGO`, grava `TRBA_ID_PAGAMENTO` e move status para `PAGA` ou `PAGA_PARCIAL`. Payload: contaId, dataPagamento, valorPago. Retorno: 200 ([MSG08](#msg08)) ou 422. | | | | |
| <a id="edp07"></a>EDP07 | PUT | [PERM05](#perm05) (`FATURAS_EDITAR`) | /faturas-cartao/{id}/encargos | N |
| Atualiza encargos ou valor mínimo de uma fatura fechada antes da quitação total. Retorno: 200 ([MSG04](#msg04)) ou 422. | | | | |

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | **Autoridades Atômicas:** Cada operação exige permissão específica (`FATURAS_LISTAR`, `FATURAS_FECHAR`, `FATURAS_REABRIR`, `FATURAS_PAGAR`, `FATURAS_EDITAR`), vedando terminantemente o uso de `MANTER`. |
| <a id="rn02"></a>RN02 | **Escopo Row-Level por Titular do Cartão:** Todas as consultas e operações filtram pelo `USU_ID` do cartão de crédito (`FTCA.CACR_ID → CARTOES_CREDITO.USU_ID`), extraído da autenticação. Tentativas de acesso a faturas de cartões de outros usuários respondem HTTP 404 ([MSG05](#msg05)). |
| <a id="rn03"></a>RN03 | **Cartão Ativo Obrigatório:** Toda fatura pertence a um cartão de crédito ativo do usuário autenticado. |
| <a id="rn04"></a>RN04 | **Unicidade de Ciclo Mensal:** É proibido duplicar faturas para o mesmo cartão na mesma competência (`CACR_ID` e `FTCA_COMPETENCIA` são únicos). Tentativa duplicada → [MSG09](#msg09). |
| <a id="rn05"></a>RN05 | **Total Dinâmico de Fatura Aberta:** Enquanto a fatura mantiver o status `ABERTA`, `FTCA_VALOR_TOTAL` permanece nulo no banco e a aplicação calcula o valor somando as despesas ativas do cartão no mês via [C3](#c3). |
| <a id="rn06"></a>RN06 | **Fechamento de Fatura:** O fechamento consolida o total de compras apurado, soma os encargos (`FTCA_VALOR_ENCARGOS`), valida a data de fechamento (menor ou igual ao vencimento) e grava `FTCA_VALOR_TOTAL` com status `FECHADA`. |
| <a id="rn07"></a>RN07 | **Regra de Reabertura de Fatura:** Apenas faturas com status `FECHADA` e com `FTCA_VALOR_PAGO == 0.00` podem ser reabertas. Faturas pagas ou parcialmente pagas não podem ser reabertas diretamente ([MSG11](#msg11)). |
| <a id="rn08"></a>RN08 | **Geração de Débito Bancário na Quitação:** A liquidação da fatura gera automaticamente uma `TransacaoBancaria` de débito na conta informada, atribuindo `TRBA_FL_PAGAMENTO_FATURA = TRUE`, `TRBA_ORIGEM = 'MANUAL'` e registrando a chave gerada em `FTCA_ID.TRBA_ID_PAGAMENTO`. |
| <a id="rn09"></a>RN09 | **Isolamento de Gastos por Categoria:** Transações com `TRBA_FL_PAGAMENTO_FATURA = TRUE` são sinalizadas para exclusão dos relatórios de gastos por categoria para não duplicar valores já contabilizados pelas despesas no cartão. |
| <a id="rn10"></a>RN10 | **Quitação Integral vs Parcial:** Se `valorPago >= FTCA_VALOR_TOTAL`, o status é atualizado para `PAGA`. Se `0 < valorPago < FTCA_VALOR_TOTAL`, o status é atualizado para `PAGA_PARCIAL`. |
| <a id="rn11"></a>RN11 | **Data de Vencimento Padrão:** A data de vencimento padrão é derivada do dia de vencimento cadastrado no cartão de crédito aplicado à competência da fatura. |
| <a id="rn12"></a>RN12 | **Trava de Exclusão de Fatura Paga:** Faturas com pagamentos registrados não podem sofrer exclusão lógica sem a anulação prévia do pagamento vinculado. |
| <a id="rn13"></a>RN13 | **Transações em Conta Ativa:** A conta indicada para pagamento da fatura deve pertencer ao usuário autenticado e estar ativa (`CTA_FL_ATIVO = TRUE`). |
| <a id="rn14"></a>RN14 | **Datas Futuras de Vencimento:** Faturas futuras podem ser consultadas e projetadas para acompanhamento de limites e planejamento. |
| <a id="rn15"></a>RN15 | **Preservação de Contexto:** Mutações no estado da fatura preservam o cartão selecionado e os filtros de navegação ativos. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Fatura fechada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Informe um valor de pagamento maior que zero. |
| <a id="msg04"></a>MSG04 | Encargos da fatura atualizados com sucesso. |
| <a id="msg05"></a>MSG05 | Fatura de cartão não encontrada. |
| <a id="msg06"></a>MSG06 | Confirma a reabertura da fatura de competência {competencia}? Novas compras voltarão a incidir no cálculo aberto. |
| <a id="msg07"></a>MSG07 | Fatura reaberta com sucesso. |
| <a id="msg08"></a>MSG08 | Pagamento da fatura registrado com sucesso. |
| <a id="msg09"></a>MSG09 | Já existe uma fatura cadastrada para este cartão nesta competência. |
| <a id="msg10"></a>MSG10 | Pagamento parcial registrado. O saldo restante de R$ {saldo} entrará no próximo ciclo. |
| <a id="msg11"></a>MSG11 | Esta fatura já possui pagamentos registrados e não pode ser reaberta. |
| <a id="msg12"></a>MSG12 | Faturas pagas ou parcialmente pagas não podem ser excluídas. |
| <a id="msg13"></a>MSG13 | A data de fechamento não pode ser posterior à data de vencimento da fatura. |
| <a id="msg14"></a>MSG14 | A conta selecionada para pagamento não está disponível. Escolha uma conta ativa. |
| <a id="msg15"></a>MSG15 | Fatura já se encontra fechada. |
| <a id="msg16"></a>MSG16 | Não é possível registrar pagamento para uma fatura com status ABERTA. Feche a fatura antes de realizar o pagamento. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem das faturas de um cartão do usuário autenticado ([EDP02](#edp02)):<br>`SELECT f.FTCA_ID, f.FTCA_COMPETENCIA, f.FTCA_DT_FECHAMENTO, f.FTCA_DT_VENCIMENTO,`<br>`       f.FTCA_VALOR_TOTAL, f.FTCA_VALOR_MINIMO, f.FTCA_VALOR_ENCARGOS, f.FTCA_VALOR_PAGO,`<br>`       f.FTCA_STATUS, f.FTCA_ORIGEM, f.CACR_ID, f.TRBA_ID_PAGAMENTO`<br>`FROM FATURAS_CARTAO f`<br>`JOIN CARTOES_CREDITO c ON c.CACR_ID = f.CACR_ID`<br>`WHERE c.CACR_ID = :cartaoId`<br>`  AND c.USU_ID = :usuId`<br>`  AND f.audit_data_exclusao IS NULL`<br>`ORDER BY f.FTCA_COMPETENCIA ASC;` |
| <a id="c2"></a>C2 | Listagem das despesas/compras de uma fatura do cartão na competência ([EDP03](#edp03)):<br>`SELECT d.DESP_ID, d.DESP_COMPETENCIA, d.DESP_DT_VENCIMENTO, d.DESP_NOME,`<br>`       d.DESP_VALOR, cat.CATE_NOME, d.DESP_NRO_PARCELA, d.DESP_TOTAL_PARCELAS`<br>`FROM DESPESAS d`<br>`LEFT JOIN CATEGORIAS cat ON cat.CATE_ID = d.CATE_ID`<br>`WHERE d.CACR_ID = :cartaoId`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL`<br>`ORDER BY d.DESP_DT_VENCIMENTO ASC, d.DESP_ID ASC;` |
| <a id="c3"></a>C3 | Soma dinâmica de compras em despesas para fatura aberta ([RN05](#rn05)):<br>`SELECT COALESCE(SUM(d.DESP_VALOR), 0.00)`<br>`FROM DESPESAS d`<br>`WHERE d.CACR_ID = :cartaoId`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL;` |
| <a id="c4"></a>C4 | Validação de posse do cartão de crédito pelo usuário autenticado ([RN02](#rn02)):<br>`SELECT COUNT(*) FROM CARTOES_CREDITO c`<br>`WHERE c.CACR_ID = :cartaoId`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.audit_data_exclusao IS NULL;` |
| <a id="c5"></a>C5 | Validação de conta bancária ativa para liquidação ([RN13](#rn13)):<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.CTA_ID = :ctaId`<br>`  AND c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE`<br>`  AND c.audit_data_exclusao IS NULL;` |

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| `FATURA_SUGERIR_CONTA_DEBITO_PADRAO` | true | Se `true`, o modal de quitação da fatura pré-seleciona a conta de débito padrão cadastrada no cartão (`CACR_ID_DEBITO_PADRAO`). |
| `FATURA_PERMITIR_PAGAMENTO_PARCIAL` | true | Se `true`, permite que o usuário realize pagamentos parciais, marcando a fatura como `PAGA_PARCIAL`. |

---

## 13. Permissões

Cinco permissões atômicas do módulo **Faturas de Cartão** (`PERM_MODULO = 'Faturas de Cartão'`), sem permissão agregada `MANTER`.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `FATURAS_LISTAR` — consultar e listar faturas dos próprios cartões. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm02"></a>PERM02 | `FATURAS_FECHAR` — realizar o fechamento formal de faturas em aberto. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm03"></a>PERM03 | `FATURAS_REABRIR` — reabrir faturas fechadas para ajuste de compras. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm04"></a>PERM04 | `FATURAS_PAGAR` — registrar liquidação total ou parcial de faturas fechadas. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm05"></a>PERM05 | `FATURAS_EDITAR` — retificar encargos e valores mínimos de faturas fechadas. | [PERF01](#perf01), [PERF02](#perf02) |

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `FATURAS_LISTAR` | ✓ | ✓ |
| `FATURAS_FECHAR` | ✓ | ✓ |
| `FATURAS_REABRIR` | ✓ | ✓ |
| `FATURAS_PAGAR` | ✓ | ✓ |
| `FATURAS_EDITAR` | ✓ | ✓ |

O ADMIN administra apenas os próprios cartões e faturas ([RN02](#rn02)).

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. Possui todas as permissões de fatura para seus próprios cartões de crédito. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum do sistema. Gerencia as faturas de seus próprios cartões com autorizações atômicas. |

---

## 15. Fluxo de Eventos

**1. Ciclo de Fechamento e Pagamento de Fatura:**
```
1. Usuário seleciona o cartão de crédito e visualiza a fatura com status ABERTA.
2. Na data de corte, o usuário clica em "Fechar Fatura" ([RT04](#rt04)).
3. Sistema abre modal QUADRO_DESCRITIVO_3 com compras consolidadas.
4. Usuário informa eventuais encargos e confirma → dispara EDP04.
5. Fatura é atualizada para status FECHADA e valor total é congelado.
6. Na data de vencimento, o usuário clica em "Pagar" ([RT06](#rt06)).
7. Sistema abre modal QUADRO_DESCRITIVO_4 sugerindo a conta de débito padrão do cartão.
8. Usuário confirma pagamento integral → dispara EDP06.
9. Sob transação única:
   ├─ Gera TransacaoBancaria (TRBA_NATUREZA_MOVIMENTO='DEBITO', TRBA_FL_PAGAMENTO_FATURA=TRUE).
   └─ Atualiza FATURAS_CARTAO: status 'PAGA', valorPago = valorTotal, vincula TRBA_ID_PAGAMENTO.
10. Sistema emite MSG08 e atualiza a interface.
```

**2. Reabertura de Fatura:**
```
1. Usuário localiza uma fatura com status FECHADA que ainda não foi paga.
2. Usuário clica no botão "Reabrir" ([RT05](#rt05)) e confirma o modal MSG06.
3. Serviço valida se FTCA_VALOR_PAGO == 0.00 (RN07).
4. Fatura retorna ao status ABERTA, limpando FTCA_VALOR_TOTAL e FTCA_DT_FECHAMENTO.
5. Sistema emite MSG07 e recarrega a tela com recálculo dinâmico ativo.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Total dinâmico de compras em fatura aberta
Dado que o cartão "Nubank" possui duas compras em despesas na competência "2026-10": uma de R$ 100,00 e outra de R$ 250,00.  
E a fatura da competência "2026-10" encontra-se com status `ABERTA`.  
Quando o usuário visualizar a listagem de faturas do cartão "Nubank".  
Então o sistema deve exibir dinamicamente o total de compras como "R$ 350,00".

### 16.1 Fechar fatura congelando encargos
Dado que a fatura do cartão "Nubank" da competência "2026-10" possui R$ 350,00 em compras.  
Quando o usuário acionar o fechamento informando encargos de R$ 15,00 e confirmar.  
Então o sistema deve gravar a fatura com status `FECHADA`.  
E o valor total congelado deve ser exatamente R$ 365,00.

### 16.2 Registrar pagamento com criação de transação bancária
Dado que a fatura "2026-10" está fechada com valor de R$ 365,00.  
Quando o usuário registrar o pagamento integral selecionando a conta corrente "Banco do Brasil".  
Então o sistema deve criar uma transação bancária de débito no valor de R$ 365,00 na conta "Banco do Brasil" com `TRBA_FL_PAGAMENTO_FATURA = TRUE`.  
E a fatura deve ter seu status alterado para `PAGA` com `TRBA_ID_PAGAMENTO` vinculado.

### 16.3 Bloquear reabertura de fatura quitada
Dado que a fatura "2026-10" possui status `PAGA`.  
Quando o usuário tentar acionar o endpoint de reabertura `POST /faturas-cartao/{id}/reabrir`.  
Então o sistema deve recusar a operação e exibir a mensagem [MSG11](#msg11).

---

## 17. Workshop de Análise / Itens a Confirmar

| Item | Descrição | Situação |
|---|---|---|
| W01 | Suporte a emissão de boleto bancário de fatura ou geração de QR Code Pix copia e cola diretamente pela tela. | Futuro (integração PSP). |
| W02 | Projeção e rolagem automática de juros de fatura parcial para o próximo ciclo de fechamento. | A Confirmar para v1.1. |

---

## 18. Anexos

- [Documento 0 — Fundação](../00%20-%20analise-geral/documento-0-fundacao.md) — Definição de `FATURAS_CARTAO` ([QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8)) e DDL MySQL correspondente ([DDL_8](../00%20-%20analise-geral/documento-0-fundacao.md#ddl_8)).
- Documento `07 - manter-cartao-credito` — Especificação do CRUD de cartões de crédito e endpoint `GET /cartoes/opcoes`.
- Documento `10 - manter-transacao-bancaria` — Especificação de transações bancárias e conciliação de pagamentos de fatura.
