# dscproject — Análise de Sistemas
## Módulo Despesas — USER / ADMIN — Manter Despesa

**Gerado em:** 08/09/2026  
**Atualizado em:** 15/09/2026  
**Versão:** 2.9  
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
| 1.0 | 08/09/2026 | Diego dos Santos Cordeiro | Criação do documento. CRUD das **despesas do próprio usuário** (tela "Finanças > Despesas") para a geração 2 — sucessor do CRUD REST de `Despesa` da geração 1 (`dsc-backend`), agora sobre a tabela `DESPESAS` ([QUADRO_DESCRITIVO_10 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10)) e a associativa de rateio `DESPESAS_USUARIO` ([QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11)). Cobre os dois pontos de complexidade do módulo: **parcelamento** (série de `DESPESAS` ligadas por `DESP_ID_PARCELA_PAI`, melhoria F) e **rateio entre usuários** (`DESPESAS_USUARIO`, recurso de plano pago via `DESPESA_RATEAR_MULTIUSUARIO`). Introduz a forma de pagamento (`DESP_MEIO_PAGAMENTO`), o vínculo com cartão de crédito (`CACR_ID`), a origem do lançamento (`DESP_ORIGEM`), a baixa de pagamento individual e em lote, e o escopo *row-level* por usuário derivado da conta **ou** do cartão. Remove os enums `DESP_TIPO_TRANSACAO` e `DESP_TIPO_RECEITA_DESPESA` da geração 1 (categoria passa a ser `CATE_ID`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |
| 1.1 | 11/09/2026 | Diego dos Santos Cordeiro | Evolução do Rateio para Agenda de Contatos Privada e Contatos Extra-Sistema: substituição da busca global de usuários pela seleção a partir da agenda de contatos do próprio usuário (`CONTATOS`, [QUADRO_DESCRITIVO_29 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29)). Permite rateio com pessoas fora da plataforma (contatos extra-sistema com dados de acerto/Pix) e com outros usuários da plataforma conectados via convite aceito. Elimina vulnerabilidade de exposição de dados da base global de usuários (LGPD/isolamento) e fecha o item "A Confirmar" da Seção 17. Atualização dos requisitos RF05, RF06, novos RF15/RF16, regras RN15 a RN20, mensagens MSG19/MSG19b, seletor SB06, endpoints EDP03/EDP04/EDP05/EDP09/EDP10/EDP11 e consultas C7/C8. |
| 1.2 | 11/09/2026 | Diego dos Santos Cordeiro | Despesas Recorrentes: inclusão do requisito RF17 e regra de negócio RN25 para cadastro e projeção em lote de despesas fixas periódicas (aluguel, condomínio, assinaturas, internet) com valor integral em cada mês, suporte a exclusão em lote da série periódica pela mãe, mútua exclusão com compra parcelada e réplica de rateio nas ocorrências projetadas. |
| 1.3 | 11/09/2026 | Diego dos Santos Cordeiro | Split de `DESPESAS_MANTER` em `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR` e `DESPESAS_IMPORTAR`, com proibição mandatória de permissões agregadas `MANTER`. Resgate da importação de extrato/fatura de cartão de crédito da geração 1 (`dsc-backend`: Excel Itaú, Bradesco, C6 Bank e arquivos OFX via Apache POI e OFX4J) em modal dedicado na listagem com endpoint `POST /despesas/importar-extrato`. Duplicação de despesas individual e em lote (`POST /despesas/duplicar`), edição inline do valor na célula do grid (`PATCH /despesas/{id}/valor`), filtro padrão inicial em `mes_atual - 1` com aceitação irrestrita de competências passadas, totalizador condicional por competência única (`competenciaInicio == competenciaFim`), preservação de filtros ativos e página no grid após mutações e máscara monetária client-side contínua (`pt-BR`, `R$ 0,00`). |
| 1.4 | 12/09/2026 | Diego dos Santos Cordeiro | Melhorias no grid e inicialização de competência: (1) herança automática da competência ativa no filtro da listagem no modal de nova despesa (garantindo que séries parceladas e despesas recorrentes iniciem na competência selecionada); (2) edição inline da competência diretamente na célula do grid via `PATCH /despesas/{id}/competencia` (novo endpoint EDP15, RF24, RN31, RT20); (3) ordenação determinística do grid com desempate por número de parcela (`nroParcela`), competência e id; (4) ocultação mandatória de despesas excluídas no grid (cláusula `audit_data_exclusao IS NULL` na consulta C1 / EDP02 e filtro client-side). |
| 1.5 | 12/09/2026 | Diego dos Santos Cordeiro | Correção e estabilização da ordenação da coluna Competência: ajuste na RT21 para que a ordenação por competência dentro do mesmo mês realize desempate cronológico por vencimento, lançamento, nome e id (eliminando ordenação indevida por parcela entre despesas não relacionadas), com inversão bidirecional consistente de id e desempate de parcelas restrito à mesma série parcelada. |
| 1.6 | 12/09/2026 | Diego dos Santos Cordeiro | Padronização visual do sistema: explicitação do alinhamento à esquerda para a coluna de Ações no grid de Despesas (QUADRO_DESCRITIVO_1). |
| 1.7 | 12/09/2026 | Diego dos Santos Cordeiro | Inclusão da coluna Número de Ordem no grid de Despesas com suporte a ordenação e reordenação interativa via Drag and Drop com persistência de ordem personalizada (QUADRO_DESCRITIVO_1, RT22), além da padronização visual com destaque de linha ao passar o mouse (hover) em todas as tabelas e grids do sistema. |
| 1.8 | 13/09/2026 | Diego dos Santos Cordeiro | Edição inline da categoria no grid de Despesas: atualização do QUADRO_DESCRITIVO_1 (linha 12), inclusão da regra de tela RT23 e do endpoint EDP16 (PATCH /despesas/{id}/categoria), permitindo alterar ou desvincular a categoria diretamente pela célula do grid com combobox rápido de opções ativas de despesa e feedback toast. |
| 1.9 | 13/09/2026 | Diego dos Santos Cordeiro | Ajuste da atribuição do Número de Ordem no grid de Despesas: a numeração ordinal inicial passa a seguir rigorosamente o identificador único (`id`) em ordem crescente (do menor ID para o maior ID, numerando sequencialmente de 1 a X), com o grid respeitando inicialmente a ordenação pela coluna Número de Ordem (ascendente) e suporte a reordenação manual via Drag and Drop (QUADRO_DESCRITIVO_1, RT22). |
| 2.0 | 13/09/2026 | Diego dos Santos Cordeiro | Permissão de exclusão para despesas importadas do cartão (`origem = IMPORTACAO`), mantendo o bloqueio de exclusão restrito ao Open Finance (`origem = OPEN_FINANCE`) (RN08, RT07, QUADRO_DESCRITIVO_1 item 23, EDP06). Reenumeração automática da coluna Nº Ordem de 1 a X ao atualizar a ordenação via Drag and Drop (RT22, QUADRO_DESCRITIVO_1 item 8a). Edição inline da coluna Forma / Meio diretamente no grid de despesas via `PATCH /despesas/{id}/forma-pagamento` com combobox agrupado por cartões de crédito, contas bancárias e dinheiro (QUADRO_DESCRITIVO_1 itens 13/14, RF25, RN34, RT24, EDP17, MSG30). |
| 2.1 | 13/09/2026 | Diego dos Santos Cordeiro | Permissão para registrar pagamento (individual e em lote) de despesas importadas de cartão de crédito (`origem = IMPORTACAO`). As despesas importadas passam a nascer com `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (Em aberto), permitindo baixa individual via modal ou em lote e atualização via formulário de edição (RF09, RN21, RN23, RN28, RT11, RT12, EDP08, EDP09). |
| 2.2 | 13/09/2026 | Diego dos Santos Cordeiro | Edição inline do nome da despesa diretamente na célula do grid via `PATCH /despesas/{id}/nome` (novo endpoint EDP18, RF26, RN35, RT25, QUADRO_DESCRITIVO_1 item 10, MSG31, MSG32). |
| 2.3 | 13/09/2026 | Diego dos Santos Cordeiro | Pagamento e somatório de despesas no Cartão de Crédito: permissão irrestrita para registrar pagamento (individual e em lote) de qualquer despesa vinculada a cartão de crédito (manual ou importada). Despesas manuais no cartão nascem com `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (Em aberto) ou `SIM` (caso informado pagamento no cadastro/edição), deixando de forçar `NAO_SE_APLICA`. O modal de cadastro/edição passa a exibir e permitir informar status e data de pagamento para compras no cartão. Registros legados com status `NAO_SE_APLICA` são tratados como pendentes (`NAO`), permitindo baixa normal e sendo incluídos no totalizador de despesas pendentes e total geral do grid (D05 itens 11, 13, 28; CAUS04; QUADRO_DESCRITIVO_1 item 17; QUADRO_DESCRITIVO_3 item 16; QUADRO_DESCRITIVO_4 obs; QUADRO_DESCRITIVO_6 obs; RN12; RN21; RN23; RN25; RN26; RN34; EDP07; EDP08). |
| 2.4 | 13/09/2026 | Diego dos Santos Cordeiro | Ativação da funcionalidade de Rateio e Contatos: vinculação da permissão `DESPESA_RATEAR_MULTIUSUARIO` aos perfis `ADMIN` e `USER` na carga inicial, desvinculando-a da obrigatoriedade de plano pago imediato (`concedivelPorPlano = false` no catálogo), permitindo o uso pleno do rateio de despesas, co-participantes da agenda e cadastro rápido de contatos externos na tela de Despesas (D05 itens 19 e 22; RT10; PERM07). Ajuste no clique da ação "Ratear" do grid para reabrir o modal focando e rolando suavemente até a seção de rateio com foco na busca de contato. |
| 2.5 | 14/09/2026 | Diego dos Santos Cordeiro | Rateio com Cota Zerada, Divisão Automática e Pré-visualização de Parcelas: explicitação em RN16 e RN17 de que a soma das fatias dos co-participantes pode igualar 100% do valor da despesa (`somaFatias == DESP_VALOR`), resultando em cota implícita do titular igual a `R$ 0,00` (quando o titular realiza a compra integralmente em nome/benefício de um terceiro). Evolução da RT09, RT10 e do QUADRO_DESCRITIVO_3 com: (1) campo interativo e editável "Minha Cota (Você - Titular)" com máscara monetária e sincronização bidirecional em tempo real com as fatias de rateio; (2) botão de atalho "Zerar minha cota" atribuindo o valor integral aos contatos; (3) botão de ação "100% / Total" na linha de cada co-participante para atribuir o saldo integral instantaneamente; (4) divisão automática equitativa ao adicionar contatos ao rateio e botão dedicado "Dividir igualmente" para redistribuição com um clique; (5) pré-visualização detalhada do valor estimado da parcela quando a compra for parcelada com rateio (decompondo parcela total, parcela do titular e parcela dos contatos, com discriminativo `Nx de R$ Y,YY` nas fatias e em Minha Cota); (6) correção na precedência de eventos de digitação da máscara monetária; (7) badges informativos visuais de rateio integral (cota zerada). |
| 2.6 | 14/09/2026 | Diego dos Santos Cordeiro | Reflexão do Rateio nos Totalizadores e Grid: totalizadores condicionados da competência (Pago, Pendente e Total Geral) passam a somar exclusivamente a cota líquida efetiva do titular (`valorUsuario = valor - somaFatiasContatos`), expurgando as fatias transferidas para terceiros (`valorRateado`) para evitar falsos positivos de gastos pessoais quando 100% ou parte da despesa for repassada a contatos. O card Total Geral passa a exibir subtítulo informativo discriminando valor bruto e total rateado quando houver rateios no mês (`Bruto: R$ X,XX (Rateado: R$ Y,YY)`). No grid, a coluna "Valor" exibe badge estilizado com a cota do titular (`Sua cota: R$ X,XX` ou `Sua cota: R$ 0,00` em destaque suave para rateio integral). Inclusão dos campos `valorUsuario` e `valorRateado` no payload JSON de EDP02 / DespesaGridDTO (QUADRO_DESCRITIVO_1 itens 3a e 14; RT15; RT18; RN30; EDP02). |
| 2.7 | 14/09/2026 | Diego dos Santos Cordeiro | Modais Globais Interativas, Altura Uniforme dos Cards, Saldo de Receita e Duplicação/Edição com Parcelamento/Recorrência: (1) Comportamento global em todas as modais do sistema (`modal-movel.js`), permitindo arraste pelo cabeçalho, redimensionamento fluido pelas bordas e cantos e maximização por duplo clique; (2) Uniformização obrigatória da altura de todos os cards totalizadores da tela de despesas (`h-100`); (3) Inclusão do 4º card totalizador "Receita Restante / Saldo" na competência selecionada no filtro (`Total de Receitas - Total de Despesas cota titular`), consultado via novo endpoint EDP19 (`GET /despesas/receita-competencia`); (4) Opções de geração de compra parcelada e despesa recorrente na duplicação (modal de duplicação QUADRO_DESCRITIVO_7 e EDP12) e conversão de despesa avulsa em parcelada/recorrente na edição (QUADRO_DESCRITIVO_3, RN24, RN25, RN26, RT14, RT15, RT16, EDP10, EDP12, EDP19). |
| 2.8 | 15/09/2026 | Diego dos Santos Cordeiro | Ocultação Condicional dos Botões de Ações em Lote: os botões 'Duplicar' (`btnDuplicarLote`) e 'Registrar pagamento' (`btnPagarLote`) passam a ser ocultados (`display: none`) em vez de desabilitados (`disabled`) quando não houver linhas ou linhas elegíveis selecionadas no grid de despesas, exibindo-se dinamicamente apenas quando houver seleção correspondente (QUADRO_DESCRITIVO_1 itens 6 e 6a; RT12; RT16). |
| 2.9 | 15/09/2026 | Diego dos Santos Cordeiro | Visibilidade Condicional do Rateio e Limpeza Mandatória Sem Vazamento (Zero-Leak): inclusão do switch 'Habilitar rateio' (`#despesaHabilitarRateio`), exibindo a seção de rateio (`#conteudoRateio`) sob demanda apenas quando ativado ou quando o registro possuir rateio existente. Encapsulamento da tabela de co-participantes e do painel 'Minha Cota' no bloco condicional `#blocoItensRateio`, visível unicamente quando houver contatos adicionados (`itensRateio.length > 0`). Limpeza profunda e restauração mandatória do estado virgem (`limparRateio`) ao desmarcar o switch, fechar o modal (`hidden.bs.modal`) ou abrir novo formulário, expurgando resíduos de contatos, cotas, buscas e caches de memória, eliminando contaminação entre registros (QUADRO_DESCRITIVO_3 itens 22 e 23; RT10). |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova** nem altera schema. |
| D04 | `DESPESAS` não tem `USU_ID` próprio ([QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10)); o dono da despesa é o `USU_ID` da conta (`DESP.CTA_ID → CONTAS.USU_ID`) **ou** do cartão (`DESP.CACR_ID → CARTOES_CREDITO.USU_ID`). O escopo por usuário (*row-level*) é sempre resolvido **no serviço**, a partir do contexto de segurança — nunca de um parâmetro da requisição. |
| D05 | A importação de faturas e extratos de cartão de crédito da geração 1 (Excel Itaú, Bradesco, C6 Bank e arquivos OFX via Apache POI e OFX4J) é incorporada neste documento via modal na listagem e endpoint de importação (`POST /despesas/importar-extrato`), gerando registros com `DESP_ORIGEM = 'IMPORTACAO'`, `CACR_ID` preenchido e `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (Em aberto, permitindo baixa de pagamento individual ou em lote pelo usuário). |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Despesa** do `dscproject-spring-mvc` — o cadastro e a manutenção das **despesas do próprio usuário**: saídas de dinheiro previstas ou já pagas (compra à vista, compra parcelada, compra no cartão de crédito, conta de consumo, boleto).

Na **geração 1** (API REST + SPA Angular), isto é o CRUD REST de `Despesa` (`dsc-backend`): `GET /despesas` devolve **apenas as despesas do usuário autenticado** (`buscarTodosPorUsuario()` — resolve o usuário pelo token e consulta por `despesa.instituicaoFinanceiraUsuario.usuario`), com `inserir`, `editar`, `excluir`, geração de parcelamento (`gerarParcelamento`), rateio de gastos entre usuários (`DespesaUsuario`, métodos `inserir`/`editar`/`compartilharDespesas`), baixa de pagamento individual e em lote (`pagarDespesa` / `pagarDespesas`), e a **importação de faturas de cartão de crédito** (`importarDadosCartaoCredito` — suporte a planilhas Excel Itaú, Bradesco, C6 Bank e arquivos OFX).

O **Documento 0** ([QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) e [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11); Observações 8, 9, 12, 15, 16, 18 e 24) travou a reescrita das tabelas `DESPESAS` e `DESPESAS_USUARIO` para a geração 2:

- a categoria deixa de ser enum e passa a ser `CATE_ID` (FK para `CATEGORIAS`, documento `04 - manter-categoria`); os enums `DESP_TIPO_TRANSACAO` (`TipoRegistroFinanceiro`) e `DESP_TIPO_RECEITA_DESPESA` (`CategoriaRegistroFinanceiro`) são **removidos**;
- `DESP_VALOR_PARCELADO` + `DESP_VALOR_TOTAL_A_DIVIDIR` são consolidados em **`DESP_VALOR_TOTAL_COMPRA`** (valor cheio quando parcelada); `DESP_VALOR` passa a ser o valor **desta despesa/parcela**;
- `DESP_ID_PARCELA_PAI` deixa de ser um `Long` solto e passa a ser **FK real** (auto-relacionamento `@ManyToOne Despesa parcelaPai` — melhoria F, Observação 12);
- a competência (`DESP_COMPETENCIA`) vira `CHAR(7)` no formato `yyyy-MM`, com `CHECK`, mapeada em Java como `java.time.YearMonth` via `YearMonthConverter` (Documento 0, Seção 7.4); o *default* `"0000-00"` da geração 1 é removido;
- `DESP_DT_LANCAMENTO` passa a `LocalDate`;
- entram os campos novos **`DESP_MEIO_PAGAMENTO`** (enum `MeioPagamento`: `DINHEIRO` / `DEBITO` / `CREDITO` / `PIX` / `BOLETO` / `TRANSFERENCIA`), **`DESP_FL_PAGAMENTO_FATURA`** (`BOOLEAN`), **`DESP_ORIGEM`** (`MANUAL` / `OPEN_FINANCE` / `IMPORTACAO`, *default* `MANUAL`), **`CACR_ID`** (FK para `CARTOES_CREDITO`, nulo quando à vista/débito/dinheiro) e **`FTCA_ID`** (FK para `FATURAS_CARTAO`, preenchida pelo documento `11`);
- `DEPU_IND_STATUS_PAGAMENTO` de `DESPESAS_USUARIO` passa de `BOOLEAN` para o enum `StatusPagamento` (`SIM` / `NAO` / `NAO_SE_APLICA`), alinhado com `DESPESAS`; entra `DEPU_DT_ACERTO` (data em que a pessoa acertou a parte dela);
- `Despesa` passa a herdar da superclasse `LancamentoFinanceiro` (`@MappedSuperclass`, Documento 0, Seção 7.2), compartilhando valor, competência, data de lançamento, origem, conta e categoria com `Receita` e `TransacaoBancaria`.

Este documento cobre:

- a **tela Despesas** — listar, cadastrar, editar, registrar o pagamento (individual e em lote), duplicar (individual e em lote), editar inline o valor na tabela, importar faturas de cartão de crédito e excluir (exclusão lógica) as despesas do usuário autenticado;
- o **filtro padrão** — inicialização automática pela competência do mês anterior (`mes_atual - 1`), aceitando livremente a seleção de competências passadas sem restrições temporais;
- o **totalizador condicional** — card totalizador por competência única, exibido no topo do grid quando `competenciaInicio == competenciaFim`;
- o **parcelamento** — ao marcar "parcelada" e informar o número de parcelas e o valor total da compra, a tela cria uma **série de registros `DESPESAS`** ligados por `DESP_ID_PARCELA_PAI` ([RN12](#rn12));
- a **recorrência** — cadastro e projeção de despesas periódicas mensais fixas com valor integral em cada ocorrência ([RN25](#rn25));
- a **importação de cartão de crédito** — modal na listagem para importação de faturas e extratos nos formatos Excel Itaú, Bradesco, C6 Bank e OFX via Apache POI e OFX4J ([RN28](#rn28));
- a **duplicação de despesas** — duplicação individual (linha do grid) ou em lote (seleção múltipla) gerando cópias com status em aberto ([RN26](#rn26));
- a **edição inline de valor** — ajuste ágil do valor da despesa na própria célula do grid com requisição `PATCH /despesas/{id}/valor` ([RN27](#rn27));
- o **rateio com a agenda de contatos** — dividir uma despesa com pessoas da sua agenda de contatos ([QUADRO_DESCRITIVO_29 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29)), abrangendo contatos extra-sistema (com campos livres para acerto/Pix) e outros usuários da plataforma conectados via convite aceito, cada um responsável por uma fatia (`DEPU_VALOR`), com controle de acerto ([RN15](#rn15) a [RN20](#rn20)); recurso liberado pela permissão `DESPESA_RATEAR_MULTIUSUARIO`, concedível por plano pago;
- o **escopo por usuário**: um usuário nunca vê nem altera a despesa de outro — toda consulta e todo comando filtram pelo `USU_ID` da conta **ou** do cartão da despesa, no serviço;
- o tratamento das despesas **importadas do Open Finance** (`DESP_ORIGEM = 'OPEN_FINANCE'`), que aparecem nesta tela mas têm conta, cartão, origem e exclusão bloqueadas; as despesas **importadas do cartão** (`DESP_ORIGEM = 'IMPORTACAO'`), por sua vez, podem ser excluídas normalmente.

**Escopo deste documento:**
- Tela de **listagem das despesas do usuário** (grid client-side), com filtro por modal (intervalo de competência, status de pagamento, conta/cartão, categoria, parcelada, texto).
- **Filtro padrão inicial** em `mes_atual - 1` e totalizador condicional exibido quando o filtro engloba competência única.
- **Cadastro e edição** de despesa via modal único, com as seções: dados básicos, forma de pagamento (conta × cartão × dinheiro/`CARTEIRA`), parcelamento e rateio (esta última só quando o plano permite).
- **Importação de faturas/extratos de cartão de crédito** via modal dedicado (Itaú, Bradesco, C6 Bank e OFX), gerando despesas vinculadas ao cartão de crédito.
- **Duplicação de despesas** individual e em lote, recarregando o grid e preservando filtros ativos.
- **Edição inline de valor** diretamente na célula da tabela do grid via chamada AJAX `PATCH`.
- **Parcelamento**: geração da série, regras de arredondamento, e o comportamento de editar e de excluir uma despesa parcelada.
- **Despesas recorrentes**: geração de ocorrências projetadas para despesas fixas mensais.
- **Rateio**: adicionar co-participantes da agenda de contatos com badge de tipo (externo/conectado), atalho para cadastro rápido de contato extra-sistema, definir fatias, marcar o acerto de cada fatia, replicar o rateio nas parcelas, e a trava de plano pago.
- **Baixa de pagamento**: ação "Registrar pagamento" no grid (individual) e em lote, marcando `DESP_IND_STATUS_PAGAMENTO = SIM` e gravando `DESP_DT_PAGAMENTO`.
- **Exclusão lógica** de despesa (e de série parcelada/recorrente).
- Definição das permissões atômicas `DESPESAS_LISTAR`, `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR`, `DESPESAS_IMPORTAR` e `DESPESA_RATEAR_MULTIUSUARIO`, proibição mandatória de `DESPESAS_MANTER` e regra de escopo por usuário.

**Não contempla:**
- CRUD de **Conta** (`CONTAS`) — documento `06 - manter-conta`. Aqui, a conta é apenas escolhida num combobox alimentado pelo endpoint de opções daquele documento ([EDP07 do `06`](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07)).
- CRUD de **Cartão de Crédito** (`CARTOES_CREDITO`) — documento `07 - manter-cartao-credito`. Aqui, o cartão é apenas escolhido num combobox alimentado pelo [EDP07 do `07`](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#edp07).
- CRUD de **Categoria** (`CATEGORIAS`) — documento `04 - manter-categoria`. Aqui, a categoria é escolhida num combobox alimentado pelo [EDP07 do `04`](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07), filtrado por `aplicaA=DESPESA`.
- **Fatura de cartão** (`FATURAS_CARTAO`) — ciclo de vida (`ABERTA` → `FECHADA` → `PAGA`), fechamento, cálculo do total consolidado, pagamento da fatura e o vínculo `DESP.FTCA_ID` são do documento `11 - manter-fatura-cartao`. Aqui, a compra ou importação no cartão grava `CACR_ID`; `FTCA_ID` fica nulo até o documento `11` existir ([RN23](#rn23)).
- **Importação de extrato bancário de conta corrente** (`TransacaoBancaria`) — documento `10 - manter-transacao-bancaria`.
- **Sincronização e conciliação de Open Finance** — a conciliação (documento `16`) pode **criar** uma despesa com `DESP_ORIGEM = OPEN_FINANCE`; esta tela **não** faz sincronização, apenas exibe e edita parcialmente o resultado.
- Cálculo de **saldo por competência, total de gastos por categoria e efeito de `DESP_FL_PAGAMENTO_FATURA` / `DESP_IND_STATUS_PAGAMENTO` no painel** — documento `14 - dashboard-financeiro`. Aqui só se mantém o dado.
- **Visão do co-participante do rateio** (tela "compartilhado comigo", o outro usuário acertando a própria fatia) — fora do escopo da v1.0; na v1.0 só o dono vê e gerencia o rateio ([RN19](#rn19), Seção 17).
- **CRUD completo de Contatos e Gestão de Convites** — documento `17 - manter-contato`. Esta tela apenas consome os contatos ativos do usuário e oferece atalho de cadastro rápido de contato extra-sistema.

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) e [PERF02](#perf02) (USER). A tela é do **próprio usuário** — cada um opera somente sobre as suas despesas ([RN02](#rn02)). Dado financeiro é privado do dono: o ADMIN opera como um USER comum, sem qualquer visão administrativa das despesas de terceiros. As operações são regidas pelas permissões atômicas (`DESPESAS_LISTAR`, `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR`, `DESPESAS_IMPORTAR`); o rateio exige, além do perfil, a permissão `DESPESA_RATEAR_MULTIUSUARIO` — concedida por plano pago ([RN15](#rn15)).

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **`DESPESAS` é dado do próprio usuário, mas o dono é indireto e tem duas âncoras.** A tabela **não tem `USU_ID`** ([QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10)); o dono é o `USU_ID` da conta (`DESP.CTA_ID → CONTAS.USU_ID`) **ou** do cartão (`DESP.CACR_ID → CARTOES_CREDITO.USU_ID`). Toda despesa tem exatamente **uma** das duas ([RN03](#rn03)). A tela vive em "Finanças > Despesas", não em "Administração". | [RN02](#rn02), [RN03](#rn03) |
| 2 | **`DESP.CTA_ID` e `DESP.CACR_ID` são anuláveis no schema, mas a aplicação exige um deles.** O Documento 0 mantém as duas colunas anuláveis (uma despesa é à vista **ou** no cartão). A v1.0 exige, na camada de serviço, que **conta e cartão não sejam ambos nulos nem ambos preenchidos** ([RN03](#rn03)). Sem nenhum dos dois, a despesa ficaria órfã (sem dono, invisível em qualquer grid). **O Documento 0 não é alterado.** | [RN03](#rn03), [RNF02](#rnf02) |
| 2a | **Dinheiro em espécie é modelado como conta do tipo `CARTEIRA`.** Não há despesa "sem conta e sem cartão": para registrar um gasto em dinheiro vivo, o usuário escolhe a forma de pagamento "Dinheiro", que aponta para uma conta do tipo `CARTEIRA` (`TipoConta.CARTEIRA`, documento `06 - manter-conta`) e fixa `DESP_MEIO_PAGAMENTO = DINHEIRO`. Decisão herdada do documento `08 - manter-receita`. | [RN03](#rn03), [RN22](#rn22), documento `06` |
| 3 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `DESPESAS` filtram pelo `USU_ID` da conta **ou** do cartão, resolvido do contexto de segurança — nunca de um parâmetro. Um endpoint com `{id}` de despesa de outro usuário responde como "não encontrada" ([MSG05](#msg05)). Espelha o `findDespesasByUsuarioId(...)` da geração 1. | [RN02](#rn02), [RNF01](#rnf01), [RNF02](#rnf02) |
| 4 | **Mudanças de estrutura vs geração 1.** Saem os enums `DESP_TIPO_TRANSACAO` e `DESP_TIPO_RECEITA_DESPESA` (categoria vira `CATE_ID`). `DESP_VALOR_PARCELADO` + `DESP_VALOR_TOTAL_A_DIVIDIR` viram `DESP_VALOR_TOTAL_COMPRA`. `DESP_ID_PARCELA_PAI` vira FK. `DEPU_IND_STATUS_PAGAMENTO` vira enum. `DESP_DT_LANCAMENTO` passa a `LocalDate`; `DESP_COMPETENCIA` passa a `YearMonth`. Entram `DESP_MEIO_PAGAMENTO`, `DESP_FL_PAGAMENTO_FATURA`, `DESP_ORIGEM`, `CACR_ID`, `FTCA_ID`, `DEPU_DT_ACERTO`. Em `DESPESAS_USUARIO`, a FK `USU_ID` é substituída por `CONT_ID` (FK para `CONTATOS`). **[Requer código]** | Documento 0, [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11), [QUADRO_DESCRITIVO_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29) |
| 5 | **Parcelamento é uma série de `DESPESAS`.** Ao marcar "parcelada", a tela informa o **número de parcelas** e o **valor total da compra**; o serviço cria **N registros `DESPESAS`** — a **1ª parcela é a despesa-mãe** (`DESP_ID_PARCELA_PAI` nula, `DESP_NRO_PARCELA = 1`) e as demais apontam para ela ([RN12](#rn12)). `DESP_VALOR` de cada parcela = total ÷ N, com o resíduo do arredondamento na **última** parcela ([RN12](#rn12), parâmetro `DESPESA_RESIDUO_PARCELA_NA_ULTIMA`). `DESP_VALOR_TOTAL_COMPRA` = total em **todas** as parcelas. Competência e data de vencimento avançam **1 mês** por parcela; a data de lançamento (data da compra) é a mesma em todas. | [RN12](#rn12), [RN13](#rn13), [RN14](#rn14) |
| 6 | **Rateio com contatos da agenda (`DESPESAS_USUARIO` / `CONTATOS`).** A despesa **é do criador** (dono via conta/cartão). O rateio adiciona **contatos da agenda do usuário** (`CONTATOS`, [QUADRO_DESCRITIVO_29 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29)) como responsáveis por uma fatia (`DEPU_VALOR`), com status de acerto (`DEPU_IND_STATUS_PAGAMENTO`) e data de acerto (`DEPU_DT_ACERTO`). Podem ser contatos extra-sistema (pessoas sem conta) ou usuários da plataforma conectados via convite aceito. O par (`DESP_ID`, `CONT_ID`) é único. | [RN15](#rn15) a [RN20](#rn20) |
| 7 | **Rateio é recurso de plano pago.** A permissão `DESPESA_RATEAR_MULTIUSUARIO` tem `PERM_FL_CONCEDIVEL_POR_PLANO = TRUE` (Documento 0, Observação 24 e [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26)). Sem essa **permissão efetiva** (perfil ∪ plano), a seção de rateio do modal **não aparece** e os endpoints de rateio **recusam** o corpo ([MSG16](#msg16)). | [RN15](#rn15), Seção 13.1 |
| 8 | **Privacidade e isolamento do rateio.** O usuário **só pode compartilhar despesas com quem está na sua lista de contatos ativos**. A busca de participantes nunca expõe a base global de usuários do sistema (LGPD/isolamento). Um co-participante não vê a despesa do dono nesta tela; o acerto de cada fatia é registrado **pelo dono** ([RN20](#rn20)). | [RN02](#rn02), [RN19](#rn19), [RN20](#rn20) |
| 9 | **Soma das fatias ≤ `DESP_VALOR`.** A soma das fatias dos co-participantes pode ser **menor ou igual** ao valor da despesa; a diferença é a **cota implícita do dono**. A soma **não pode exceder** `DESP_VALOR` ([RN17](#rn17) → [MSG17](#msg17)). Exigir soma exata é item "A Confirmar" (Seção 17). | [RN17](#rn17) |
| 10 | **Rateio replicado nas parcelas.** Quando a despesa é parcelada, o rateio informado é replicado em **cada** parcela da série (`DEPU_VALOR` proporcional ao valor da parcela), como na geração 1. | [RN18](#rn18) |
| 11 | **Compra no cartão × fatura.** Uma despesa com `CACR_ID` preenchido é uma compra no cartão. O **vínculo com a fatura** (`DESP.FTCA_ID`), o fechamento e o cálculo do total da fatura são do documento `11 - manter-fatura-cartao` (Documento 0, Observação 15). Nesta tela, a despesa só grava `CACR_ID`; `FTCA_ID` fica nulo. Para compra no cartão, o serviço fixa `DESP_MEIO_PAGAMENTO = CREDITO` e define `DESP_IND_STATUS_PAGAMENTO = NAO` (Em aberto) ou `SIM` (quando informado pagamento), permitindo a baixa individual ou em lote ([RN21](#rn21), [RN22](#rn22), [RN23](#rn23)). | [RN23](#rn23), documento `11` |
| 12 | **`DESP_FL_PAGAMENTO_FATURA` — caso de borda.** Marca a despesa que **representa o pagamento de uma fatura** de cartão, quando lançada como despesa em vez de `TransacaoBancaria` (Documento 0, Observação 16). Essa despesa é **excluída** do total de gastos por categoria (regra detalhada no documento `14 - dashboard-financeiro`). A v1.0 **não** oferece esse lançamento pelo fluxo normal da tela; o campo existe para o caminho alternativo e é mencionado aqui sem detalhamento ([RN24](#rn24)). | [RN24](#rn24), documentos `11` e `14` |
| 13 | **Status de pagamento.** `DESP_IND_STATUS_PAGAMENTO` (enum `StatusPagamento`): `NAO` (previsto, ainda não pago — *default* para todas as formas de pagamento, inclusive cartão de crédito), `SIM` (pago — gravado na baixa ou no cadastro/edição, com `DESP_DT_PAGAMENTO`), `NAO_SE_APLICA` (legado de compras de cartão anteriores ou lançamentos meramente informativos; tratado como pendente `NAO` na visualização e permite baixa de pagamento). | [RN21](#rn21), [RN23](#rn23) |
| 14 | **Baixa de pagamento — individual e em lote.** Ação "Registrar pagamento" no grid marca uma despesa como paga (`SIM` + `DESP_DT_PAGAMENTO`). A seleção múltipla no grid permite baixar várias de uma vez ([EDP08](#edp08), espelha `pagarDespesas` da geração 1). Numa série parcelada, a baixa é **por parcela**. | [RN21](#rn21) |
| 15 | **Meio de pagamento — derivação.** `DESP_MEIO_PAGAMENTO` (enum `MeioPagamento`) é **derivado da forma de pagamento escolhida e ajustável**: cartão → `CREDITO` (fixo); conta `CARTEIRA` (Dinheiro) → `DINHEIRO` (fixo); conta corrente/poupança → *default* `DEBITO`, ajustável para `PIX` / `BOLETO` / `TRANSFERENCIA` ([RN22](#rn22)). O campo é opcional no schema. Se deve permanecer derivado ou virar campo livre é item "A Confirmar". | [RN22](#rn22) |
| 16 | **Competência é `YearMonth` e independente.** `DESP_COMPETENCIA` é `CHAR(7)` `yyyy-MM`, com `CHECK` e `YearMonthConverter`. É um campo **próprio**, apenas **pré-preenchido** com o mês da data de lançamento e reajustado enquanto o usuário não a editar ([RT13](#rt13), parâmetro `DESPESA_COMPETENCIA_SEGUE_DATA_LANCAMENTO`). Pode divergir do mês da data de lançamento (ex.: compra de 28/01 que "cai" na fatura de fevereiro → competência `2026-02`). **[Requer código]** | [RN04](#rn04), Documento 0, Seção 7.4 |
| 17 | **Origem do lançamento.** `DESP_ORIGEM` (`MANUAL` / `OPEN_FINANCE` / `IMPORTACAO`, *default* `MANUAL` — Documento 0, Observação 18). Despesas criadas por esta tela são **sempre `MANUAL`**; o serviço fixa o valor e ignora qualquer origem recebida ([RN07](#rn07)). Não há migração da geração 1 (Observação 18a). Despesas geradas por importação de fatura recebem `IMPORTACAO` ([RN28](#rn28)). | [RN07](#rn07), [RN28](#rn28) |
| 18 | **Despesa importada do Open Finance** (`DESP_ORIGEM = 'OPEN_FINANCE'`): aparece nesta tela e tem os campos de negócio editáveis (nome, descrição, valor, categoria, competência, datas, status de pagamento, rateio), mas **conta, cartão e origem ficam bloqueados** e ela **não é excluível** por esta tela ([RN08](#rn08)). Espelha a decisão do documento `08 - manter-receita`. Despesas importadas de fatura/extrato de cartão de crédito (`DESP_ORIGEM = 'IMPORTACAO'`), por sua vez, **podem ser excluídas** normalmente por esta tela. | [RN08](#rn08) |
| 19 | **Datas podem ser futuras.** `DESP_DT_LANCAMENTO`, `DESP_DT_VENCIMENTO` e `DESP_DT_PAGAMENTO` aceitam datas posteriores a hoje — a tela serve para planejar despesas previstas (parcela do mês que vem, boleto agendado). O serviço **não** rejeita data futura. | [RN06](#rn06) |
| 20 | **Grid client-side.** A tela carrega a lista das despesas do usuário e pagina/ordena/filtra no navegador (DataTables). Uma despesa parcelada aparece como **N linhas** (uma por parcela), agrupáveis. O volume por usuário é da ordem de centenas de linhas por ano; a v1.0 não faz paginação server-side (ver Seção 17). | [RNF05](#rnf05) |
| 21 | **Auditoria.** `DESPESAS` e `DESPESAS_USUARIO` são auditadas via Hibernate Envers (`@Audited`), conforme o Documento 0. Criação, edição, geração de parcelamento, recorrência, importação, duplicação, edição inline de valor, rateio, baixa de pagamento, acerto de fatia e exclusão lógica ficam registrados. | [RNF03](#rnf03) |
| 22 | **Sem regra de unicidade.** Não há restrição de nome/valor/competência únicos — é normal ter "Mercado" repetido na mesma competência. Na importação de faturas, a duplicidade é prevenida pela checagem de lançamentos idênticos para o mesmo cartão, competência e data/valor ([RN28](#rn28)). | [RN05](#rn05), [RN28](#rn28) |
| 23 | **Valor positivo.** `DESP_VALOR` é obrigatório e deve ser **maior que zero** ([RN05](#rn05)). Estorno com valor negativo não é escopo da v1.0. | [RN05](#rn05) |
| 24 | **Conta e cartão só do próprio usuário.** O combobox de conta ([EDP07 do `06`](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07)) e o de cartão ([EDP07 do `07`](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#edp07)) já devolvem apenas os registros **ativos do usuário autenticado**. O serviço revalida ([C3](#c3), [C4](#c4)). | [SB01](#sb01), [SB02](#sb02) |
| 25 | **Filtro padrão e competências passadas.** A tela é inicializada filtrando a competência `mes_atual - 1`. O sistema aceita qualquer competência passada sem travas temporais ou limitações retroativas. | [RN29](#rn29), [RT02](#rt02) |
| 26 | **Totalizador condicional por competência única.** O card com o somatório dos valores de despesas da listagem é exibido no topo do grid exclusivamente quando o filtro ativo possui `competenciaInicio == competenciaFim`. | [RN30](#rn30), [RT15](#rt15) |
| 27 | **Importação de fatura de cartão de crédito (v1 resgatada).** A funcionalidade de importação de faturas de cartão de crédito da geração 1 (`dsc-backend`) é resgatada e disponibilizada em modal acionado na listagem. Dá suporte a arquivos Excel do Itaú, Bradesco, C6 Bank e arquivos no formato OFX (via Apache POI e OFX4J). Os lançamentos gerados recebem `DESP_ORIGEM = 'IMPORTACAO'`, `CACR_ID` vinculado e status `NAO` (Em aberto), permitindo baixa de pagamento individual ou em lote. | [RN28](#rn28), [EDP14](#edp14) |
| 28 | **Duplicação de despesas e preservação de filtros.** O usuário pode duplicar uma despesa individualmente (ação de linha) ou selecionar múltiplos registros e duplicá-los em lote. As novas despesas nascem com `DESP_ORIGEM = 'MANUAL'`, status `NAO` e data de pagamento nula (inclusive para compras de cartão de crédito). Ao concluir inserção, edição, exclusão, duplicação ou pagamento, o grid recarrega preservando os filtros ativos e a página atual. | [RN26](#rn26), [RN31](#rn31), [EDP12](#edp12) |
| 29 | **Edição inline de valor no grid.** É permitido editar diretamente o valor de uma despesa na célula correspondente da tabela do grid, via duplo clique ou clique no ícone de lápis da célula, acionando requisição AJAX assíncrona `PATCH /despesas/{id}/valor`. | [RN27](#rn27), [EDP13](#edp13) |
| 30 | **Máscara monetária contínua.** Todos os campos que recebem valores monetários (modal de cadastro/edição, edição inline e filtros) contam obrigatoriamente com máscara em tempo real (`pt-BR`, `R$ 0,00`). | [RNF06](#rnf06), [RT13](#rt13) |
| 31 | **Edição inline de nome no grid.** É permitido editar diretamente o nome de uma despesa na célula correspondente da tabela do grid, via clique na célula, acionando requisição AJAX assíncrona `PATCH /despesas/{id}/nome`. | [RN35](#rn35), [EDP18](#edp18) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as despesas ativas do usuário autenticado, com: competência (com edição inline), nome, categoria, conta ou cartão, valor da despesa/parcela (com edição inline), parcela (ex.: "3/6"), data de lançamento, data de vencimento, status de pagamento, data de pagamento, rateio (indicador) e origem, omitindo registros excluídos logicamente. | Alta | Analisado |
| <a id="rf02"></a>RF02 | O sistema deve permitir filtrar a listagem por texto (nome/descrição), intervalo de competência (com padrão inicial em `mes_atual - 1`), status de pagamento, conta/cartão, categoria e "parcelada", por meio de um modal acionado pelo botão "Filtrar". | Média | Em análise |
| <a id="rf03"></a>RF03 | O sistema deve permitir cadastrar uma nova despesa via modal, com dados básicos (nome, descrição, valor, data de lançamento, data de vencimento, competência, categoria), forma de pagamento (conta à vista / cartão de crédito / dinheiro) e status de pagamento, inicializando o campo Competência com o valor atualmente ativo no filtro de competência da tela (`filtro.competenciaInicio`). | Alta | Analisado |
| <a id="rf04"></a>RF04 | O sistema deve permitir marcar a despesa como **parcelada**, informando o número de parcelas e o valor total da compra, gerando automaticamente a série de parcelas (uma despesa por mês) com a primeira parcela iniciando na competência selecionada/informada. | Alta | Analisado |
| <a id="rf05"></a>RF05 | O sistema deve permitir **ratear** a despesa entre contatos da agenda do usuário (contatos extra-sistema e usuários da plataforma conectados via convite aceito), definindo a fatia de cada um, desde que o usuário tenha a permissão `DESPESA_RATEAR_MULTIUSUARIO`. | Alta | Analisado |
| <a id="rf06"></a>RF06 | O sistema deve permitir registrar o **acerto** de cada fatia do rateio (status e data), pelo dono da despesa. | Média | Analisado |
| <a id="rf07"></a>RF07 | O sistema deve permitir editar uma despesa existente via modal; numa despesa parcelada, a edição afeta apenas a parcela selecionada. | Alta | Analisado |
| <a id="rf08"></a>RF08 | O sistema deve permitir a exclusão lógica de uma despesa (com origem `MANUAL` ou `IMPORTACAO`); ao excluir a despesa-mãe de uma série parcelada ou recorrente, deve excluir toda a série e omiti-la imediatamente do grid de registros. | Alta | Analisado |
| <a id="rf09"></a>RF09 | O sistema deve permitir **registrar o pagamento** de uma despesa por ação dedicada no grid (individual) e de várias despesas em lote (seleção múltipla), gravando a data de pagamento e o status `SIM`. | Alta | Analisado |
| <a id="rf10"></a>RF10 | O sistema deve garantir que cada usuário só liste, consulte, edite, pague, duplique e exclua as **próprias** despesas (as vinculadas às suas contas ou aos seus cartões). | Alta | Analisado |
| <a id="rf11"></a>RF11 | O sistema deve exigir que toda despesa tenha **exatamente uma** âncora de pagamento: uma conta **ou** um cartão do próprio usuário, nunca ambos nem nenhum. | Alta | Analisado |
| <a id="rf12"></a>RF12 | O sistema deve exigir valor maior que zero e competência no formato AAAA-MM. | Alta | Analisado |
| <a id="rf13"></a>RF13 | O sistema deve tratar as despesas importadas do Open Finance (`DESP_ORIGEM = 'OPEN_FINANCE'`) com conta, cartão e origem bloqueados e sem permitir a exclusão por esta tela. | Média | Analisado |
| <a id="rf14"></a>RF14 | O sistema deve ocultar a seção de rateio e recusar operações de rateio para o usuário que não tem a permissão efetiva `DESPESA_RATEAR_MULTIUSUARIO`. | Alta | Analisado |
| <a id="rf15"></a>RF15 | O sistema deve restringir a seleção de co-participantes do rateio exclusivamente aos contatos com status `ATIVO` pertencentes à agenda privada do usuário autenticado, sem expor a base global de usuários do sistema. | Alta | Analisado |
| <a id="rf16"></a>RF16 | O sistema deve disponibilizar atalho no modal de despesa para cadastro rápido de contato extra-sistema (com nome, e-mail/telefone e chave PIX para acerto), inserindo-o automaticamente na fatia de rateio sem sair da tela. | Média | Analisado |
| <a id="rf17"></a>RF17 | O sistema deve permitir marcar a despesa como recorrente (despesa fixa mensal), definindo a quantidade de meses a projetar (mínimo 2, máximo 36, padrão 12 meses), gerando a série de ocorrências com o mesmo valor integral em cada mês a partir da competência selecionada/informada, sendo mutuamente exclusiva com a compra parcelada. | Alta | Analisado |
| <a id="rf18"></a>RF18 | O sistema deve exibir no topo da listagem um card com o valor total consolidado das despesas listadas exclusivamente quando o filtro ativo englobar competência única (`competenciaInicio == competenciaFim`). | Média | Em análise |
| <a id="rf19"></a>RF19 | O sistema deve inicializar a listagem com filtro na competência do mês anterior (`mes_atual - 1`) e aceitar livremente a filtragem e o cadastro em competências passadas sem restrição temporal. | Média | Em análise |
| <a id="rf20"></a>RF20 | O sistema deve disponibilizar modal de importação de faturas e extratos de cartão de crédito nos formatos Excel Itaú, Bradesco, C6 Bank e arquivos OFX (via Apache POI e OFX4J), gerando as despesas correspondentes vinculadas ao cartão com `DESP_ORIGEM = 'IMPORTACAO'`. | Alta | Em análise |
| <a id="rf21"></a>RF21 | O sistema deve permitir duplicar uma despesa individualmente ou em lote (múltiplas selecionadas), gerando novos registros em aberto com `DESP_ORIGEM = 'MANUAL'`. | Média | Em análise |
| <a id="rf22"></a>RF22 | O sistema deve permitir a edição inline do valor da despesa diretamente na célula da tabela do grid, com chamada AJAX `PATCH /despesas/{id}/valor`. | Média | Analisado |
| <a id="rf23"></a>RF23 | O sistema deve recarregar a listagem preservando os filtros ativos e a página atual após qualquer operação de inserção, edição, exclusão, duplicação ou pagamento. | Média | Analisado |
| <a id="rf24"></a>RF24 | O sistema deve permitir a edição inline da competência da despesa diretamente na célula da tabela do grid (seletor mês/ano), com chamada AJAX `PATCH /despesas/{id}/competencia`. | Média | Analisado |
| <a id="rf25"></a>RF25 | O sistema deve permitir a edição inline da forma e meio de pagamento da despesa diretamente na célula da tabela do grid via combobox dinâmico agrupando cartões de crédito, contas bancárias e dinheiro, com chamada AJAX `PATCH /despesas/{id}/forma-pagamento`. | Média | Analisado |
| <a id="rf26"></a>RF26 | O sistema deve permitir a edição inline do nome da despesa diretamente na célula da tabela do grid, com chamada AJAX `PATCH /despesas/{id}/nome`. | Média | Analisado |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada endpoint desta tela exige a autoridade atômica da sua operação (`PERM_DESPESAS_LISTAR`, `PERM_DESPESAS_INSERIR`, `PERM_DESPESAS_EDITAR`, `PERM_DESPESAS_EXCLUIR`, `PERM_DESPESAS_PAGAR`, `PERM_DESPESAS_IMPORTAR` ou `PERM_DESPESA_RATEAR_MULTIUSUARIO` — ver Seção 13 e [RN01](#rn01)). É terminantemente proibido o uso de `PERM_DESPESAS_MANTER`. O `USU_ID` usado no filtro e nas travas vem sempre do contexto de segurança, nunca da requisição. | Teste de acesso com ADMIN, com USER, com perfis que possuem apenas permissões específicas e com tentativa de uso de permissões inexistentes/bloqueadas. |
| <a id="rnf02"></a>RNF02 | Isolamento | Nenhum endpoint que recebe `{id}` retorna, edita, paga, duplica ou exclui a despesa de outro usuário — a resposta é "não encontrada" ([MSG05](#msg05)), sem revelar a existência do registro. O dono é resolvido por `DESP.CTA_ID → CONTAS.USU_ID` **ou** `DESP.CACR_ID → CARTOES_CREDITO.USU_ID`. Um co-participante do rateio **não** ganha acesso à despesa ([RN19](#rn19)). | Teste chamando `buscar/{id}`, `editar/{id}`, `registrar-pagamento/{id}`, `patch/{id}/valor` e `excluir/{id}` com o id de uma despesa de outro usuário, e chamando `listar-dados` com um usuário que é só co-participante. |
| <a id="rnf03"></a>RNF03 | Auditoria | `DESPESAS` e `DESPESAS_USUARIO` têm auditoria completa via Hibernate Envers. Criação, edição, parcelamento, recorrência, importação de fatura, duplicação, edição inline de valor, rateio, acerto de fatia, baixa de pagamento e exclusão lógica são registrados. | Inspeção das tabelas `DESPESAS_aud` e `DESPESAS_USUARIO_aud` após as operações. |
| <a id="rnf04"></a>RNF04 | Integridade | A âncora conta-XOR-cartão ([RN03](#rn03)), o valor positivo ([RN05](#rn05)), o formato da competência ([RN04](#rn04)), o número mínimo de parcelas ([RN12](#rn12)), a soma das fatias do rateio ([RN17](#rn17)) e a aceitação de datas futuras ([RN06](#rn06)) são validados no serviço, não só na tela. | Teste chamando os endpoints diretamente, inclusive com conta e cartão juntos, soma de fatias acima do valor e data no futuro. |
| <a id="rnf05"></a>RNF05 | Desempenho | A listagem ([EDP02](#edp02)) responde em menos de 1 s carregando a lista do usuário uma vez. A geração de uma série de até 72 parcelas ([EDP04](#edp04)) responde em menos de 2 s. A importação de extrato com até 500 linhas processa em menos de 3 s. | Medição em homologação com carga de ~1.000 despesas e importação de arquivo com centenas de linhas. |
| <a id="rnf06"></a>RNF06 | Usabilidade | A interface segue o padrão do projeto (Thymeleaf + Tabler + DataTables + AJAX) e é responsiva. O campo de valor usa máscara monetária contínua em tempo real (`pt-BR`, `R$ 0,00`); a competência usa um seletor mês/ano; a seção de parcelamento mostra o valor da parcela calculado em tempo real; a célula de valor permite edição inline via duplo clique. | Revisão visual do protótipo e validação de interação de tela. |

---

## 4. Casos de Uso

![Casos de Uso - Manter Despesa](images/manter-despesa-casos-uso.png)

Fonte: `prototipo/manter-despesa-casos-uso.drawio` (editável) e `prototipo/_diagrama-casos-uso.html` (render). `ADMIN` ([PERF01](#perf01)) e `USER` ([PERF02](#perf02)) têm o mesmo acesso; cada um opera só sobre as próprias despesas ([RN02](#rn02)). CAUS06 e CAUS07 exigem, além do perfil, a permissão `DESPESA_RATEAR_MULTIUSUARIO` ([RN15](#rn15)). CAUS12 exige `DESPESAS_IMPORTAR`.

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Minhas Despesas | [PERF01](#perf01), [PERF02](#perf02) | O usuário acessa o menu e visualiza a lista das suas despesas, incluindo as parcelas das compras parceladas e totalizador condicional por competência. ([RF01](#rf01), [RF10](#rf10), [RF18](#rf18)) |
| <a id="caus02"></a>CAUS02 | Filtrar Despesas | [PERF01](#perf01), [PERF02](#perf02) | O usuário abre o modal de filtro, informa os critérios (iniciado por padrão em `mes_atual - 1`) e aplica. ([RF02](#rf02), [RF19](#rf19)) |
| <a id="caus03"></a>CAUS03 | Cadastrar Despesa à Vista | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_INSERIR` | O usuário abre o modal, preenche os dados básicos, escolhe a forma de pagamento "Conta à vista" ou "Dinheiro" e confirma. ([RF03](#rf03), [RF11](#rf11), [RF12](#rf12)) |
| <a id="caus04"></a>CAUS04 | Cadastrar Despesa no Cartão | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_INSERIR` | O usuário escolhe a forma de pagamento "Cartão de crédito", seleciona o cartão e confirma; pode informar se já foi paga (com data) ou deixar em aberto (padrão). ([RF03](#rf03), [RN23](#rn23)) |
| <a id="caus05"></a>CAUS05 | Cadastrar Despesa Parcelada | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_INSERIR` | O usuário marca "Parcelada", informa o número de parcelas e o valor total; o sistema gera a série de parcelas. ([RF04](#rf04), [RN12](#rn12)) |
| <a id="caus06"></a>CAUS06 | Ratear Despesa com Contatos | [PERF01](#perf01), [PERF02](#perf02) com `DESPESA_RATEAR_MULTIUSUARIO` | O usuário seleciona contatos da sua agenda privada (extra-sistema ou usuários da plataforma conectados) e define a fatia de cada um; a soma não pode exceder o valor da despesa. ([RF05](#rf05), [RF15](#rf15), [RF16](#rf16), [RN15](#rn15), [RN16](#rn16), [RN17](#rn17)) |
| <a id="caus07"></a>CAUS07 | Registrar Acerto de Fatia | [PERF01](#perf01), [PERF02](#perf02) com `DESPESA_RATEAR_MULTIUSUARIO` | O dono marca a fatia de um co-participante como acertada, gravando a data. ([RF06](#rf06), [RN20](#rn20)) |
| <a id="caus08"></a>CAUS08 | Editar Despesa | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_EDITAR` | O usuário abre o modal de edição, altera os dados e confirma; numa série parcelada, a alteração afeta só a parcela selecionada. ([RF07](#rf07), [RN13](#rn13)) |
| <a id="caus09"></a>CAUS09 | Excluir Despesa ou Série | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_EXCLUIR` | O usuário exclui logicamente uma despesa `MANUAL`; ao excluir a despesa-mãe de uma série, exclui todas as parcelas; a despesa importada do Open Finance não é excluível. ([RF08](#rf08), [RN14](#rn14), [RF13](#rf13)) |
| <a id="caus10"></a>CAUS10 | Registrar Pagamento | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_PAGAR` | O usuário registra o pagamento de uma despesa pela ação do grid, ou de várias em lote pela seleção múltipla. ([RF09](#rf09), [RN21](#rn21)) |
| <a id="caus11"></a>CAUS11 | Duplicar Despesa(s) | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_INSERIR` | O usuário clica no ícone de duplicação de uma linha ou seleciona múltiplas despesas e aciona o botão de duplicação em lote, gerando novas despesas como previstas. ([RF21](#rf21), [RN26](#rn26)) |
| <a id="caus12"></a>CAUS12 | Importar Fatura de Cartão | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_IMPORTAR` | O usuário abre o modal de importação na listagem, seleciona o cartão, competência e anexa o arquivo (Excel Itaú, Bradesco, C6 Bank ou OFX), gerando as despesas correspondentes. ([RF20](#rf20), [RN28](#rn28)) |
| <a id="caus13"></a>CAUS13 | Editar Valor Inline | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_EDITAR` | O usuário efetua duplo clique ou clica no ícone de edição na célula de valor do grid, edita o montante e confirma, persistindo via requisição `PATCH`. ([RF22](#rf22), [RN27](#rn27)) |
| <a id="caus14"></a>CAUS14 | Editar Competência Inline | [PERF01](#perf01), [PERF02](#perf02) com `DESPESAS_EDITAR` | O usuário clica na célula de competência do grid, edita o mês/ano e confirma, persistindo via requisição `PATCH`. ([RF23](#rf23), [RN32](#rn32)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Finanças > Despesas

**Critérios de Aceitação:**
- O menu 'Despesas' é visível apenas para quem tem [PERM01](#perm01) (`DESPESAS_LISTAR`).
- Ao acessar a tela, a listagem das despesas do usuário autenticado é carregada automaticamente com o filtro da competência do mês anterior (`mes_atual - 1`).
- Quando o filtro ativo englobar exatamente uma competência (`competenciaInicio == competenciaFim`), exibe card de totalizador consolidado no topo do grid.
- A listagem nunca traz despesa de outro usuário, nem mesmo aquelas em que o usuário é apenas co-participante do rateio, tampouco registros com exclusão lógica ativa.
- O filtro é aplicado por um modal acionado pelo botão "Filtrar".
- O botão "Nova despesa" e as ações de duplicação só aparecem para quem tem [PERM02](#perm02) (`DESPESAS_INSERIR`).
- O botão "Importar extrato" só aparece para quem tem [PERM06](#perm06) (`DESPESAS_IMPORTAR`).
- A edição modal e as edições inline (valor e competência) nas células só aparecem para quem tem [PERM03](#perm03) (`DESPESAS_EDITAR`).
- O registro de pagamento (individual e em lote) só aparece para quem tem [PERM05](#perm05) (`DESPESAS_PAGAR`).
- A exclusão só aparece para quem tem [PERM04](#perm04) (`DESPESAS_EXCLUIR`); ao ser excluída, a despesa não mais é exibida no grid nem retornada na consulta.
- A ordenação das colunas do grid utiliza desempate determinístico (parcela, competência, vencimento e id) garantindo estabilidade no ordenamento de parcelas e registros com valores idênticos.
- A seção de rateio só aparece para quem tem [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`); a soma das fatias não pode exceder o valor da despesa.
- Após qualquer operação de inserção, edição, exclusão, duplicação ou baixa de pagamento, o grid é recarregado preservando a página e os filtros ativos.
- Chamar qualquer endpoint REST com o `{id}` de despesa pertencente a outro usuário responde como 404 "não encontrada".
- O cadastro e a edição são feitos num modal único, com as seções: dados básicos, forma de pagamento, parcelamento e rateio.
- A forma de pagamento é uma escolha entre "Conta à vista", "Cartão de crédito" e "Dinheiro"; a despesa é salva com **conta ou cartão**, nunca os dois.
- Ao marcar "Parcelada" ou "Recorrente", a competência e vencimento iniciam rigorosamente a partir da competência selecionada/filtrada.
- Ao marcar "Parcelada", os campos "Número de parcelas" e "Valor total da compra" aparecem, e o valor da parcela é exibido calculado.
- Ao gerar uma série parcelada, são criadas N despesas, com competências e vencimentos consecutivos e o valor total dividido, com o resíduo na última parcela.
- Uma despesa importada do Open Finance abre em edição com Conta, Cartão e Origem desabilitados e sem o botão de excluir.

---

## 6. Banco de Dados

Toda a estrutura está no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova** nem executa `ALTER TABLE`.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `DESPESAS` | Documento 0 — [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) | CRUD + parcelamento (auto-relacionamento `DESP_ID_PARCELA_PAI`) + recorrência (`DESP_FL_RECORRENTE`, auto-relacionamento `DESP_ID_RECORRENTE_PAI`) + baixa de pagamento, sempre no escopo do `USU_ID` da conta **ou** do cartão |
| `DESPESAS_USUARIO` | Documento 0 — [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11) | Rateio: uma linha por (`DESP_ID`, `CONT_ID`); fatia, status e data de acerto |
| `CONTATOS` | Documento 0 — [QUADRO_DESCRITIVO_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29) | Somente leitura e cadastro rápido: agenda de contatos privados do dono da despesa (contatos extra-sistema e conexões via convite aceito) |
| `CONTAS` | Documento 0 — [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5) | Somente leitura: âncora de dono (`CONTAS.USU_ID`) e combobox de conta (endpoint do documento `06`) |
| `CARTOES_CREDITO` | Documento 0 — [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6) | Somente leitura: âncora de dono (`CARTOES_CREDITO.USU_ID`) e combobox de cartão (endpoint do documento `07`) |
| `CATEGORIAS` | Documento 0 — [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) | Somente leitura: combobox de categoria (endpoint do documento `04`, `aplicaA=DESPESA`) |
| `FATURAS_CARTAO` | Documento 0 — [QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8) | Contexto: `DESP.FTCA_ID` referencia a fatura em que a compra entrou — preenchido pelo documento `11`, não por esta tela ([RN23](#rn23)) |
| `USUARIOS` | Documento 0 — [QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2) | Somente leitura: dono da despesa (via `CONTAS`/`CARTOES_CREDITO`) |
| `PERMISSOES` | Documento 0 — [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26) | Contexto: `DESPESA_RATEAR_MULTIUSUARIO` com `PERM_FL_CONCEDIVEL_POR_PLANO = TRUE` ([RN15](#rn15)) |

> Nenhum `ALTER TABLE` neste documento. As tabelas `DESPESAS`, `DESPESAS_USUARIO` e `CONTATOS`, as FKs de auto-relacionamento `DESP_ID_PARCELA_PAI` e `DESP_ID_RECORRENTE_PAI`, a FK `CONT_ID` em `DESPESAS_USUARIO`, as FKs `CTA_ID` / `CACR_ID` / `FTCA_ID` / `CATE_ID` (todas anuláveis) e o `UNIQUE (DESP_ID, CONT_ID)` já existem no Documento 0 ([DDL_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [DDL_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11) e [DDL_29](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29)). A obrigatoriedade de conta-XOR-cartão na aplicação ([RN03](#rn03)) é validada no serviço — o schema fica como está.

### 6.1 Diagrama ER

![DER - Manter Despesa](images/manter-despesa-der.png)

Subconjunto do DER do Documento 0: `DESPESAS` (com o auto-relacionamento `DESP_ID_PARCELA_PAI`), `DESPESAS_USUARIO`, `CONTATOS`, `CONTAS`, `CARTOES_CREDITO`, `CATEGORIAS`, `FATURAS_CARTAO` e `USUARIOS`. Este documento não cria tabela nova nem faz `ALTER TABLE`. Fonte: `prototipo/manter-despesa-der.drawio` (editável) e `prototipo/_diagrama-der.html` (render).

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| DESPESAS | DESPESAS_aud | Competência, nome, descrição, valor, valor total da compra, datas (lançamento, vencimento, pagamento), flags de parcelada, recorrente e de pagamento de fatura, número e quantidade de parcelas, parcela-pai, recorrência-pai, meio de pagamento, status de pagamento, origem, conta, cartão, fatura e categoria. Registra criação, edição, parcelamento, recorrência, baixa de pagamento e exclusão lógica |
| DESPESAS_USUARIO | DESPESAS_USUARIO_aud | Valor da fatia, status de pagamento, data de acerto, despesa e contato. Registra inclusão no rateio, alteração de fatia, acerto e remoção |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. A resolução do dono pela conta ou pelo cartão, a validação de conta-XOR-cartão, a geração da série de parcelas, o cálculo das fatias do rateio, a baixa de pagamento e a exclusão lógica ficam na camada de serviço.

### 6.4 Carga Inicial

Nenhuma para `DESPESAS` / `DESPESAS_USUARIO` (Documento 0, Seção 6.4 — sem *seed*). A permissão `DESPESA_RATEAR_MULTIUSUARIO` entra na carga inicial de `PERMISSOES` pelo catálogo do código, com `PERM_FL_CONCEDIVEL_POR_PLANO = TRUE` e **sem vínculo a perfil** ([RN15](#rn15), Seção 13.1).

---

## 7. Protótipos de Interface

Protótipo navegável: `prototipo/manter-despesa-prototipo.html`. Wireframes editáveis: `prototipo/manter-despesa-prototipo.drawio` (5 páginas, 7.1 a 7.5). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright). Os números em destaque nas telas correspondem aos IDs dos itens do respectivo QUADRO_DESCRITIVO.

### <a id="quadro-descritivo-1"></a>7.1 Tela: Despesas (Listagem) — QUADRO_DESCRITIVO_1

![Despesas - Listagem](images/md-tela-1.png)

> OBSERVAÇÕES: Tela acessada via 'Finanças > Despesas'. Restrita a quem tem [PERM01](#perm01) (`DESPESAS_LISTAR`). Grid client-side, carregado apenas com as despesas do usuário autenticado ([EDP02](#edp02) → [C1](#c1)), iniciado com filtro na competência `mes_atual - 1`. Cada parcela de uma compra parcelada é uma linha. O filtro é acionado por um modal (botão "Filtrar"). A seleção múltipla habilita a baixa em lote ([PERM05](#perm05)) e a duplicação em lote ([PERM02](#perm02)). A coluna Valor permite edição inline rápida para quem tem [PERM03](#perm03).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/despesas/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Finanças > Despesas | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Despesas | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Cadastre e acompanhe as suas despesas, parcelamentos, divisões de conta e faturas importadas. | — |
| <a id="qdd1-3a"></a>3a | CARD TOTALIZADOR | Tipo: Card informativo (destaque)<br>Exibição: condicional | Exibe 4 cards consolidadores em BRL em grade uniforme de 4 colunas (`col-sm-6 col-lg-3` com `h-100` obrigatório): "Pago" (verde), "Pendente" (amarelo), "Total Geral" (azul) e "Receita Restante / Saldo" (teal/ciano), exclusivamente quando `competenciaInicio == competenciaFim` ([RN30](#rn30), [RT15](#rt15)). Os cards de despesa somam a **cota efetiva do titular** (`valorUsuario`), expurgando os valores rateados com contatos (`valorRateado`) para evitar falsos positivos de gastos. O card "Total Geral" exibe subtítulo discriminando bruto e rateado quando houver rateio (`Bruto: R$ X.XXX,XX (Rateado: R$ Y.YYY,YY)`). O 4º card exibe o somatório do que resta de receita para o usuário na competência (`Total de Receitas - Total Geral de Despesas da cota do titular`), consultado via [EDP19](#edp19), com destaque verde/azul se positivo ou vermelho se negativo, e subtítulo com a receita bruta do mês (`Receitas: R$ X,XX`). Caso o filtro englobe mais de uma competência, todos os cards permanecem ocultos. |
| <a id="qdd1-4"></a>4 | BOTÃO FILTRAR | Tipo: Botão<br>Texto: Filtrar<br>Ícone: filter | Ao clicar, executar [RT01](#rt01). |
| <a id="qdd1-5"></a>5 | BOTÃO NOVA DESPESA | Tipo: Botão (primário)<br>Texto: Nova despesa<br>Ícone: plus | Visível a quem tem [PERM02](#perm02) (`DESPESAS_INSERIR`). Ao clicar, executar [RT04](#rt04). |
| <a id="qdd1-6"></a>6 | BOTÃO REGISTRAR PAGAMENTO EM LOTE | Tipo: Botão<br>Texto: Registrar pagamento<br>Ícone: cash | Visível a quem tem [PERM05](#perm05) (`DESPESAS_PAGAR`) quando houver linhas com pagamento pendente selecionadas ([ID8](#qdd1-8)); oculto quando não houver linhas elegíveis selecionadas ([RT12](#rt12)). Ao clicar, executar [RT12](#rt12). |
| <a id="qdd1-6a"></a>6a | BOTÃO DUPLICAR EM LOTE | Tipo: Botão<br>Texto: Duplicar selecionadas<br>Ícone: copy | Visível a quem tem [PERM02](#perm02) (`DESPESAS_INSERIR`) quando houver linhas selecionadas ([ID8](#qdd1-8)); oculto quando não houver linhas selecionadas ([RT16](#rt16)). Ao clicar, executar [RT16](#rt16). |
| <a id="qdd1-6b"></a>6b | BOTÃO IMPORTAR EXTRATO | Tipo: Botão<br>Texto: Importar extrato<br>Ícone: upload | Visível a quem tem [PERM06](#perm06) (`DESPESAS_IMPORTAR`). Ao clicar, executar [RT17](#rt17). |
| <a id="qdd1-7"></a>7 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side)<br>Colunas: [ID8a](#qdd1-8a)…[ID21](#qdd1-21)<br>Itens por página: 10, 25, 50<br>Ordenação padrão: Competência decrescente, depois Data de vencimento crescente<br>Endpoint: [EDP02](#edp02) | Carrega a lista completa das despesas do usuário uma vez. Filtra em memória conforme [RT02](#rt02). Preserva filtros ativos e página após mutações ([RT19](#rt19)). Destaca visualmente a linha sob o cursor do mouse (hover) em conformidade com o padrão global do sistema. |
| <a id="qdd1-8"></a>8 | SELEÇÃO DE LINHA | Tipo: Checkbox por linha + "selecionar todas" | Visível a quem tem [PERM02](#perm02) ou [PERM05](#perm05). Alimenta baixa em lote ([RT12](#rt12)) e duplicação em lote ([RT16](#rt16)). |
| <a id="qdd1-8a"></a>8a | NÚMERO DE ORDEM | Tipo: Coluna (`#` / `Nº Ordem`) com alça de arrasto (`drag handle`)<br>Ordenação: Sim (numérica) | Exibe o número sequencial da linha (1, 2, 3... X) derivado inicialmente do identificador único (`id`) em ordem crescente (o menor ID recebe 1 e o maior ID recebe X). O grid respeita inicialmente a ordenação por esta coluna de forma ascendente. Permite ordenação explícita crescente/decrescente pelo cabeçalho da coluna e reordenação interativa das linhas via Drag and Drop (arrastar e soltar), reenumerando automaticamente os números ordinais de todas as linhas exibidas de 1 a X e persistindo a ordem personalizada em armazenamento local ([RT22](#rt22)). |
| <a id="qdd1-9"></a>9 | COMPETÊNCIA | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).competencia formatada como "MM/AAAA". |
| <a id="qdd1-10"></a>10 | NOME | Tipo: Coluna (com edição inline)<br>Ordenação: Sim | Exibe [C1](#c1).nome. Ao clicar na célula, permite edição inline rápida via [RT25](#rt25) (visível a quem tem [PERM03](#perm03)). |
| <a id="qdd1-11"></a>11 | PARCELA | Tipo: Coluna<br>Ordenação: Não | Exibe "{nroParcela}/{qtdParcelas}" quando [C1](#c1).parcelada; vazio caso contrário. |
| <a id="qdd1-12"></a>12 | CATEGORIA | Tipo: Coluna (badge, com edição inline)<br>Ordenação: Sim | Exibe [C1](#c1).categoriaNome; vazio quando sem categoria. Ao clicar na célula, permite seleção e alteração inline da categoria via combobox rápido ([RT23](#rt23), visível a quem tem [PERM03](#perm03)). |
| <a id="qdd1-13"></a>13 | FORMA / MEIO | Tipo: Coluna (badge, com edição inline)<br>Ordenação: Sim | Exibe a forma (Cartão, Conta ou Dinheiro) e o meio de pagamento ([C1](#c1).cartaoDescricao / [C1](#c1).contaDescricao e [C1](#c1).meioPagamento). Ao clicar na célula, permite seleção e alteração inline da forma e meio via combobox agrupado de cartões de crédito, contas bancárias e dinheiro ([RT24](#rt24), visível a quem tem [PERM03](#perm03)). |
| <a id="qdd1-14"></a>14 | VALOR | Tipo: Coluna (moeda, alinhada à direita, com edição inline)<br>Ordenação: Sim | Exibe [C1](#c1).valor (valor desta despesa/parcela) formatado em BRL. Quando houver rateio ([C1](#c1).temRateio), exibe badge estilizado logo abaixo com a cota líquida do titular: `Sua cota: R$ X,XX` (ou `Sua cota: R$ 0,00` em destaque roxo suave para rateio integral). Ao dar duplo clique ou clicar no ícone de lápis da célula, permite edição inline rápida via [RT18](#rt18) (visível com [PERM03](#perm03)). |
| <a id="qdd1-15"></a>15 | DATA DE LANÇAMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe [C1](#c1).dataLancamento. |
| <a id="qdd1-16"></a>16 | DATA DE VENCIMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe [C1](#c1).dataVencimento; vazio quando nulo. |
| <a id="qdd1-17"></a>17 | STATUS DE PAGAMENTO | Tipo: Coluna (badge)<br>Ordenação: Sim | "Pago" (verde) quando [C1](#c1).statusPagamento = `SIM`; "Em aberto" (amarelo) quando `NAO` (ou quando `NAO_SE_APLICA`, tratado como pendente na visualização); "N/A" (cinza) para outros casos não aplicáveis. "Excluída" quando há `audit_data_exclusao`. |
| <a id="qdd1-18"></a>18 | DATA DE PAGAMENTO | Tipo: Coluna (data)<br>Ordenação: Sim | Exibe [C1](#c1).dataPagamento; vazio quando não pago. |
| <a id="qdd1-19"></a>19 | RATEIO | Tipo: Coluna (ícone)<br>Ordenação: Não | Ícone "users" quando [C1](#c1).temRateio; tooltip "Dividida com N pessoa(s)". Visível a quem tem [PERM07](#perm07). |
| <a id="qdd1-20"></a>20 | ORIGEM | Tipo: Coluna (badge)<br>Ordenação: Sim | "Manual" / "Open Finance" / "Importação", de [C1](#c1).origem. |
| <a id="qdd1-21"></a>21 | AÇÃO | Tipo: Coluna (alinhada à esquerda) | Visível a quem tem ao menos uma permissão de mutação. Ícones [ID22](#qdd1-22). |
| <a id="qdd1-22"></a>22 | ÍCONES DE AÇÃO | Tipo: Ícones<br>Editar (ícone: edit, tooltip: Editar despesa)<br>Duplicar (ícone: copy, tooltip: Duplicar despesa)<br>Registrar pagamento (ícone: cash-banknote, tooltip: Registrar pagamento)<br>Ratear (ícone: users, tooltip: Dividir despesa)<br>Excluir (ícone: trash, tooltip: Excluir despesa) | Editar → [RT05](#rt05) (visível com [PERM03](#perm03)). Duplicar → [RT16](#rt16) (visível com [PERM02](#perm02)). Registrar pagamento → [RT11](#rt11) (visível com [PERM05](#perm05); oculto quando status = 'SIM' ou despesa excluída). Ratear → [RT10](#rt10) (visível com [PERM07](#perm07)). Excluir → [RT07](#rt07) (visível com [PERM04](#perm04); oculto quando já excluída ou origem = "OPEN_FINANCE" ([RN08](#rn08))). |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Filtrar Despesas — QUADRO_DESCRITIVO_2

![Modal Filtrar Despesas](images/md-tela-2.png)

> OBSERVAÇÕES: Todos os campos são opcionais. A tela inicializa com Competência inicial e final pré-preenchidas com `mes_atual - 1` ([RN29](#rn29)). O filtro é aplicado em memória sobre a lista já carregada ([RT02](#rt02)). Aceita competências passadas sem restrições temporais.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Filtrar Despesas | — |
| <a id="qdd2-2"></a>2 | FILTRO – BUSCA | Tipo: Input Text<br>Obrigatório: Não<br>Placeholder: Nome ou descrição<br>Tooltip: Filtre por parte do nome ou da descrição. | Filtro parcial e sem acento sobre nome e descrição. |
| <a id="qdd2-3"></a>3 | FILTRO – COMPETÊNCIA INICIAL | Tipo: Seletor mês/ano<br>Obrigatório: Não<br>Valor default: `mes_atual - 1` | Filtra [C1](#c1).competencia ≥ valor. Ver [SB05](#sb05) e [RN29](#rn29). |
| <a id="qdd2-4"></a>4 | FILTRO – COMPETÊNCIA FINAL | Tipo: Seletor mês/ano<br>Obrigatório: Não<br>Valor default: `mes_atual - 1` | Filtra [C1](#c1).competencia ≤ valor. Ver [SB05](#sb05) e [RN29](#rn29). |
| <a id="qdd2-5"></a>5 | FILTRO – STATUS DE PAGAMENTO | Tipo: Combobox<br>Obrigatório: Não<br>Valor default: Todos<br>Domínio: Todos / Em aberto / Pago / Não se aplica / Excluída | Filtra por [C1](#c1).statusPagamento e pela presença de `audit_data_exclusao`. Ver [SB04](#sb04). |
| <a id="qdd2-6"></a>6 | FILTRO – FORMA DE PAGAMENTO | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todas<br>Domínio: "Todas" + contas do usuário + cartões do usuário | Filtra por [C1](#c1).contaId / [C1](#c1).cartaoId. Ver [SB01](#sb01) e [SB02](#sb02). |
| <a id="qdd2-7"></a>7 | FILTRO – CATEGORIA | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todas as categorias<br>Domínio: "Todas" + categorias de despesa | Filtra por [C1](#c1).categoriaId. Ver [SB03](#sb03). |
| <a id="qdd2-8"></a>8 | FILTRO – PARCELADA | Tipo: Combobox<br>Obrigatório: Não<br>Valor default: Todas<br>Domínio: Todas / Sim / Não | Filtra por [C1](#c1).parcelada. |
| <a id="qdd2-9"></a>9 | BOTÃO APLICAR | Tipo: Botão<br>Texto: Aplicar | Ao clicar, executar [RT02](#rt02). |
| <a id="qdd2-10"></a>10 | BOTÃO LIMPAR | Tipo: Botão<br>Texto: Limpar | Ao clicar, executar [RT03](#rt03). |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Cadastro / Edição de Despesa — QUADRO_DESCRITIVO_3

![Modal Cadastro / Edição de Despesa](images/md-tela-3.png)

> OBSERVAÇÕES: Modal único de cadastro (restrito a [PERM02](#perm02)) e edição (restrito a [PERM03](#perm03)). Organizado em quatro seções: **Dados básicos**, **Forma de pagamento**, **Parcelamento** e **Rateio**. A seção Parcelamento fica oculta no modo edição de uma parcela existente ([RT09](#rt09)). A seção Rateio só aparece para quem tem [PERM07](#perm07) ([RT10](#rt10)). Ao editar uma despesa importada do Open Finance, os campos Forma de pagamento e Origem ficam desabilitados e o aviso ([ID24](#qdd3-24)) é exibido ([RN08](#rn08)). Todos os campos monetários possuem máscara contínua em tempo real (`pt-BR`, `R$ 0,00`).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Nova despesa / Editar despesa | Varia conforme o modo. No modo edição de parcela, mostra "Editar despesa — parcela {n}/{qtd}". |
| <a id="qdd3-2"></a>2 | SEÇÃO – DADOS BÁSICOS | Tipo: Título de seção | — |
| <a id="qdd3-3"></a>3 | CAMPO – NOME | Tipo: Input Text<br>Tamanho: 100<br>Obrigatório: Sim | Grava `DESP_NOME`. Ex.: "MERCADO", "NOTEBOOK". |
| <a id="qdd3-4"></a>4 | CAMPO – DESCRIÇÃO | Tipo: Textarea<br>Tamanho: 512<br>Obrigatório: Não | Grava `DESP_DESCRICAO`. |
| <a id="qdd3-5"></a>5 | CAMPO – VALOR | Tipo: Input monetário com máscara contínua<br>Obrigatório: Sim | Máscara `pt-BR`, `R$ 0,00`. Quando não parcelada, grava `DESP_VALOR`. Quando parcelada, o rótulo muda para "Valor total da compra" e alimenta `DESP_VALOR_TOTAL_COMPRA` ([RT09](#rt09)). Deve ser maior que zero ([RN05](#rn05)). |
| <a id="qdd3-6"></a>6 | CAMPO – DATA DE LANÇAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Valor default: hoje | Grava `DESP_DT_LANCAMENTO`. Ao mudar, pode reajustar a Competência ([RT13](#rt13)). |
| <a id="qdd3-7"></a>7 | CAMPO – DATA DE VENCIMENTO | Tipo: Input Date<br>Obrigatório: Não | Grava `DESP_DT_VENCIMENTO`. Numa série parcelada, é a data da 1ª parcela e avança 1 mês por parcela ([RN12](#rn12)). |
| <a id="qdd3-8"></a>8 | CAMPO – COMPETÊNCIA | Tipo: Seletor mês/ano<br>Obrigatório: Sim<br>Valor default: mês da data de lançamento | Grava `DESP_COMPETENCIA` (`yyyy-MM`). Aceita qualquer competência passada ([RN29](#rn29)). Ver [SB05](#sb05) e [RN04](#rn04). |
| <a id="qdd3-9"></a>9 | CAMPO – CATEGORIA | Tipo: Combobox<br>Obrigatório: Não<br>Domínio: categorias de despesa ativas | Grava `CATE_ID`. Ver [SB03](#sb03). |
| <a id="qdd3-10"></a>10 | SEÇÃO – FORMA DE PAGAMENTO | Tipo: Título de seção | — |
| <a id="qdd3-11"></a>11 | CAMPO – FORMA DE PAGAMENTO | Tipo: Radio / segmented<br>Obrigatório: Sim<br>Domínio: Conta à vista / Cartão de crédito / Dinheiro | Não persiste diretamente; define qual campo abaixo aparece e o `DESP_MEIO_PAGAMENTO` derivado ([RT08](#rt08), [RN22](#rn22)). **Desabilitado** ao editar despesa com origem ≠ MANUAL ([RN08](#rn08)). |
| <a id="qdd3-12"></a>12 | CAMPO – CONTA | Tipo: Combobox<br>Obrigatório: Sim quando Forma = "Conta à vista"<br>Domínio: contas ativas do usuário | Grava `CTA_ID`. Ver [SB01](#sb01) e [RN03](#rn03). Oculto nas outras formas. |
| <a id="qdd3-13"></a>13 | CAMPO – CARTÃO | Tipo: Combobox<br>Obrigatório: Sim quando Forma = "Cartão de crédito"<br>Domínio: cartões ativos do usuário | Grava `CACR_ID`. Ver [SB02](#sb02) e [RN03](#rn03). Oculto nas outras formas. |
| <a id="qdd3-14"></a>14 | CAMPO – CONTA CARTEIRA | Tipo: Combobox<br>Obrigatório: Sim quando Forma = "Dinheiro"<br>Domínio: contas ativas do usuário do tipo `CARTEIRA` | Grava `CTA_ID`. Fixa `DESP_MEIO_PAGAMENTO = DINHEIRO` ([RN22](#rn22)). Se o usuário não tem conta `CARTEIRA`, exibir [MSG18](#msg18). |
| <a id="qdd3-15"></a>15 | CAMPO – MEIO DE PAGAMENTO | Tipo: Combobox<br>Obrigatório: Não<br>Domínio: Débito / PIX / Boleto / Transferência<br>Exibição: só quando Forma = "Conta à vista" | Grava `DESP_MEIO_PAGAMENTO`. Default `DEBITO`. Fixo em `CREDITO` (cartão) ou `DINHEIRO` (dinheiro), sem exibir o campo ([RN22](#rn22)). |
| <a id="qdd3-16"></a>16 | CAMPO – STATUS DE PAGAMENTO | Tipo: Toggle "Já paguei"<br>Valor default: Não<br>Exibição: Visível para todas as formas de pagamento | Quando "Sim", grava `DESP_IND_STATUS_PAGAMENTO = SIM` e exibe [ID17](#qdd3-17). Quando "Não", grava `NAO` (Em aberto) inclusive para compras no cartão ([RN23](#rn23)). |
| <a id="qdd3-17"></a>17 | CAMPO – DATA DE PAGAMENTO | Tipo: Input Date<br>Obrigatório: Sim quando "Já paguei" = Sim<br>Valor default: hoje<br>Exibição: só quando "Já paguei" = Sim | Grava `DESP_DT_PAGAMENTO`. Ver [RN21](#rn21). |
| <a id="qdd3-18"></a>18 | SEÇÃO – PARCELAMENTO | Tipo: Título de seção<br>Exibição: oculta no modo edição de parcela ([RT09](#rt09)) | — |
| <a id="qdd3-19"></a>19 | CAMPO – PARCELADA | Tipo: Toggle (Sim/Não)<br>Valor default: Não | Grava `DESP_FL_PARCELADA`. Ao ligar, exibe [ID20](#qdd3-20) e [ID21](#qdd3-21) e muda o rótulo de [ID5](#qdd3-5) ([RT09](#rt09)). |
| <a id="qdd3-20"></a>20 | CAMPO – NÚMERO DE PARCELAS | Tipo: Input numérico<br>Obrigatório: Sim quando Parcelada = Sim<br>Mínimo: 2<br>Máximo: parâmetro `DESPESA_PARCELAS_MAXIMO` | Grava `DESP_QTD_PARCELAS`. Ver [RN12](#rn12). |
| <a id="qdd3-21"></a>21 | VALOR DA PARCELA (CALCULADO) | Tipo: Texto (somente leitura) | Exibe "valor total ÷ nº de parcelas", em BRL, com nota "a última parcela pode diferir alguns centavos" ([RN12](#rn12)). |
| <a id="qdd3-22"></a>22 | SEÇÃO – RATEIO / HABILITAR RATEIO | Tipo: Switch / Toggle (`#despesaHabilitarRateio`)<br>Valor default: Desmarcado | Exibido apenas para quem tem [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`). Por padrão vem desmarcado e com o bloco de rateio (`#conteudoRateio`) oculto em novas despesas e edições sem rateio prévio. Ao marcar, exibe o container do rateio e foca na busca de contato. Ao desmarcar ou fechar o modal, executa limpeza mandatória de todos os dados de rateio ([RT10](#rt10)). |
| <a id="qdd3-23"></a>23 | BLOCO DE ITENS DO RATEIO E MINHA COTA | Tipo: Container condicional (`#blocoItensRateio`)<br>Componentes: Tabela de Co-participantes, Painel Minha Cota e Botão Dividir Igualmente<br>Exibição: só quando houver ao menos 1 contato | Só é exibido quando há pelo menos um contato adicionado ao rateio (`itensRateio.length > 0`). Contém a tabela editável (Contato via [SB06](#sb06), Valor da fatia com atalho 100%, Status de acerto, Ações), o botão "Dividir igualmente" e o controle interativo "Minha Cota (Você - Titular)" com campo monetário editável bidirecional, botão "Zerar minha cota" (cota do titular = `R$ 0,00`) e sincronização em tempo real ([RT10](#rt10), [RN17](#rn17)). Com zero contatos, permanece oculto, exibindo apenas o campo de busca de contato e atalho de novo contato rápido. |
| <a id="qdd3-24"></a>24 | AVISO – DESPESA IMPORTADA | Tipo: Texto informativo | Exibido no modo edição quando [C1](#c1).origem ≠ "MANUAL": "Esta despesa foi importada do Open Finance ou de extrato de cartão. A forma de pagamento e a origem não podem ser alteradas, e ela não pode ser excluída por esta tela." |
| <a id="qdd3-25"></a>25 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar<br>Endpoint: [EDP04](#edp04) (criação) ou [EDP05](#edp05) (edição) | Ao clicar, executar [RT06](#rt06). |
| <a id="qdd3-26"></a>26 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-4"></a>7.4 Modal: Registrar Pagamento — QUADRO_DESCRITIVO_4

![Modal Registrar Pagamento](images/md-tela-4.png)

> OBSERVAÇÕES: Acionado pelo ícone "Registrar pagamento" do grid ([ID23](#qdd1-23)), restrito a [PERM05](#perm05) (`DESPESAS_PAGAR`). Marca `DESP_IND_STATUS_PAGAMENTO = SIM` e grava `DESP_DT_PAGAMENTO`. Oculto para despesas já pagas (`SIM`). Disponível para despesas em aberto (`NAO`) e despesas de cartão com status legado (`NAO_SE_APLICA`).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Registrar pagamento — {nome da despesa} | — |
| <a id="qdd4-2"></a>2 | VALOR DA DESPESA | Tipo: Texto (somente leitura) | Exibe [C1](#c1).valor formatado em BRL; numa parcela, mostra "parcela {n}/{qtd}". |
| <a id="qdd4-3"></a>3 | CAMPO – DATA DE PAGAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Valor default: hoje ou a data de vencimento, o que for mais recente | Grava `DESP_DT_PAGAMENTO`. |
| <a id="qdd4-4"></a>4 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Registrar<br>Endpoint: [EDP07](#edp07) | Ao clicar, executar [RT11](#rt11). |
| <a id="qdd4-5"></a>5 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-5"></a>7.5 Modal: Confirmar Pagamento em Lote — QUADRO_DESCRITIVO_5

![Modal Confirmar Pagamento em Lote](images/md-tela-5.png)

> OBSERVAÇÕES: Acionado pelo botão "Registrar pagamento" da barra ([ID6](#qdd1-6)) quando há linhas selecionadas, restrito a [PERM05](#perm05) (`DESPESAS_PAGAR`). Espelha `pagarDespesas` da geração 1.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd5-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Registrar pagamento em lote | — |
| <a id="qdd5-2"></a>2 | RESUMO DA SELEÇÃO | Tipo: Texto (somente leitura) | "N despesas selecionadas — total {soma} em BRL". Lista as despesas selecionadas com nome, competência e valor. |
| <a id="qdd5-3"></a>3 | CAMPO – DATA DE PAGAMENTO | Tipo: Input Date<br>Obrigatório: Sim<br>Valor default: hoje | Aplicada a todas as despesas selecionadas. |
| <a id="qdd5-4"></a>4 | BOTÃO CONFIRMAR | Tipo: Botão (primário)<br>Texto: Registrar<br>Endpoint: [EDP08](#edp08) | Ao clicar, executar [RT12](#rt12). |
| <a id="qdd5-5"></a>5 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-6"></a>7.6 Modal: Importar Fatura / Extrato de Cartão de Crédito — QUADRO_DESCRITIVO_6

> OBSERVAÇÕES: Acionado pelo botão "Importar extrato" da barra ([ID6b](#qdd1-6b)), restrito a [PERM06](#perm06) (`DESPESAS_IMPORTAR`). Resgate da funcionalidade de importação de cartão de crédito da geração 1 (`dsc-backend`: Excel Itaú, Bradesco, C6 Bank e arquivos OFX via Apache POI e OFX4J). Cria despesas com `DESP_ORIGEM = 'IMPORTACAO'`, `CACR_ID` vinculado e status `NAO` (Em aberto).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd6-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Importar Fatura de Cartão de Crédito | — |
| <a id="qdd6-2"></a>2 | CAMPO – CARTÃO DE CRÉDITO | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: cartões ativos do usuário | Seleciona o cartão de crédito de destino das despesas importadas via [SB02](#sb02). |
| <a id="qdd6-3"></a>3 | CAMPO – COMPETÊNCIA | Tipo: Seletor mês/ano<br>Obrigatório: Sim<br>Valor default: mês corrente | Grava a competência (`yyyy-MM`) em todas as despesas importadas do arquivo. Aceita competências passadas. |
| <a id="qdd6-4"></a>4 | CAMPO – DATA DE VENCIMENTO | Tipo: Input Date<br>Obrigatório: Não | Data de vencimento da fatura atribuída aos lançamentos importados. |
| <a id="qdd6-5"></a>5 | CAMPO – INSTITUIÇÃO / FORMATO | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: Itaú (Excel) / Bradesco (Excel) / C6 Bank (Excel/CSV) / OFX genérico | Define o parser a ser aplicado sobre o arquivo via [SB07](#sb07). |
| <a id="qdd6-6"></a>6 | CAMPO – ARQUIVO | Tipo: Input File<br>Obrigatório: Sim<br>Extensões aceitas: .xls, .xlsx, .csv, .ofx, .qfx | Arquivo de extrato ou fatura baixado do Internet Banking. |
| <a id="qdd6-7"></a>7 | BOTÃO IMPORTAR | Tipo: Botão (primário)<br>Texto: Importar Fatura<br>Ícone: upload<br>Endpoint: [EDP14](#edp14) | Ao clicar, executar [RT17](#rt17). |
| <a id="qdd6-8"></a>8 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem importar. |

### <a id="quadro-descritivo-7"></a>7.7 Modal: Confirmar Duplicação de Despesa(s) — QUADRO_DESCRITIVO_7

> OBSERVAÇÕES: Acionado pelo ícone de duplicação na linha ([ID23](#qdd1-23)) ou pelo botão "Duplicar selecionadas" ([ID6a](#qdd1-6a)), restrito a [PERM02](#perm02) (`DESPESAS_INSERIR`). Cria réplicas das despesas como previstas (`NAO`), inclusive para compras vinculadas a cartão de crédito.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd7-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Duplicar Despesa(s) | — |
| <a id="qdd7-2"></a>2 | RESUMO DA SELEÇÃO | Tipo: Texto (somente leitura) | "1 despesa selecionada" ou "N despesas selecionadas — total {soma} em BRL". |
| <a id="qdd7-3"></a>3 | CAMPO – COMPETÊNCIA DESTINO | Tipo: Seletor mês/ano<br>Obrigatório: Sim<br>Valor default: competência da despesa de origem ou competência corrente | Define a competência em que a nova despesa (ou início da série) será criada. |
| <a id="qdd7-3a"></a>3a | CAMPO – TIPO DE DUPLICAÇÃO | Tipo: Radio Group<br>Opções: Despesa avulsa (padrão) / Compra parcelada / Despesa recorrente<br>Obrigatório: Sim<br>Default: Despesa avulsa | Permite transformar a despesa em série parcelada ou recorrente ao duplicar ([RT16](#rt16), [RN26](#rn26)). |
| <a id="qdd7-3b"></a>3b | CAMPO – QUANTIDADE DE PARCELAS | Tipo: Input Number (min: 2, max: 72)<br>Obrigatório: Se parcelada<br>Default: 2 | Quantidade de parcelas a serem geradas a partir da competência de destino. |
| <a id="qdd7-3c"></a>3c | CAMPO – QUANTIDADE DE MESES RECORRÊNCIA | Tipo: Input Number (min: 2, max: 36)<br>Obrigatório: Se recorrente<br>Default: 12 | Quantidade de meses a serem projetados a partir da competência de destino. |
| <a id="qdd7-3d"></a>3d | CAMPO – BASE DO VALOR (PARCELADA) | Tipo: Radio Group<br>Opções: Valor original é o valor da parcela / Valor original é o valor total da compra<br>Default: Parcela | Define como calcular o montante das parcelas. Exibe pré-visualização calculada em tempo real. |
| <a id="qdd7-4"></a>4 | BOTÃO CONFIRMAR | Tipo: Botão (primário)<br>Texto: Duplicar<br>Endpoint: [EDP12](#edp12) | Ao clicar, executar [RT16](#rt16). |
| <a id="qdd7-5"></a>5 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem duplicar. |

### 7.8 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | CONTA | Itens carregados de `CONTAS` via o endpoint de opções do documento `06 - manter-conta` ([EDP07 daquele documento](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07) — `GET /contas/opcoes`), que já devolve apenas as contas **ativas do usuário autenticado**. No modal, sem opção vazia; na forma "Dinheiro" ([ID14](#qdd3-14)), a lista é recortada em memória para o tipo `CARTEIRA`. No filtro do grid ([ID6](#qdd2-6)), a opção "Todas" é adicional. Nunca `<option>` fixo no HTML. |
| <a id="sb02"></a>SB02 | CARTÃO | Itens carregados de `CARTOES_CREDITO` via o endpoint de opções do documento `07 - manter-cartao-credito` ([EDP07 daquele documento](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#edp07) — `GET /cartoes/opcoes`), que devolve os cartões **ativos do usuário autenticado**. Usado no modal de despesa e no modal de importação de fatura. Nunca `<option>` fixo no HTML. |
| <a id="sb03"></a>SB03 | CATEGORIA | Itens carregados de `CATEGORIAS` via o endpoint de opções do documento `04 - manter-categoria` ([EDP07 daquele documento](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07) — `GET /categorias/opcoes?aplicaA=DESPESA`), que devolve as categorias ativas com `CATE_APLICA_A` em `DESPESA` ou `AMBOS`, ordenadas por nome. No modal, opcional (opção "Sem categoria"). No filtro ([ID7](#qdd2-7)), "Todas as categorias" é adicional. Nunca `<option>` fixo no HTML. |
| <a id="sb04"></a>SB04 | FILTRO STATUS DE PAGAMENTO | Domínio fixo do próprio filtro: Todos, Em aberto, Pago, Não se aplica, Excluída. Não é entidade. |
| <a id="sb05"></a>SB05 | COMPETÊNCIA | Campo mês/ano (`YearMonth`), renderizado como seletor mês/ano ou `<input type="month">`. Aceita competências passadas sem restrições. Não é entidade. |
| <a id="sb06"></a>SB06 | CONTATO DO RATEIO | Campo de texto com filtragem em tempo real que consome [EDP10](#edp10) (`GET /despesas/contatos-rateio?termo=`), devolvendo contatos **ativos** (`CONT_STATUS = 'ATIVO'`) pertencentes à agenda privada do usuário autenticado (id, nome, email, tipo `EXTERNO` ou `SISTEMA`, telefone, chavePix). Só disponível para quem tem [PERM07](#perm07). Inclui botão de atalho inline "+ Novo Contato Externo" acionando modal de cadastro rápido ([EDP11](#edp11)). Não é `<select>` estático. |
| <a id="sb07"></a>SB07 | FORMATO DE IMPORTAÇÃO DE FATURA | Domínio fixo do seletor de parser: Itaú (Excel XLS/XLSX), Bradesco (Excel XLS), C6 Bank (Excel XLS/CSV), OFX (Extrato OFX/QFX genérico). |

### 7.9 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao clicar em "Filtrar" ([ID4](#qdd1-4)), abrir o modal de filtro ([QUADRO_DESCRITIVO_2](#quadro-descritivo-2)) com os valores atualmente aplicados. |
| <a id="rt02"></a>RT02 | Ao clicar em "Aplicar" ([ID9](#qdd2-9)), filtrar **em memória** a lista já carregada: busca parcial e sem acento sobre nome/descrição, competência dentro do intervalo, e correspondência exata de status de pagamento, forma de pagamento, categoria e parcelada. Fechar o modal. Atualizar a exibição do Card Totalizador conforme [RT15](#rt15). Se nada restar, exibir [MSG08](#msg08) na área do grid. |
| <a id="rt03"></a>RT03 | Ao clicar em "Limpar" ([ID10](#qdd2-10)), voltar todos os campos do filtro para vazio (competência inicial e final para `mes_atual - 1`, status e parcelada para "Todos") e reaplicar conforme [RT02](#rt02). |
| <a id="rt04"></a>RT04 | Ao clicar em "Nova despesa" ([ID5](#qdd1-5)) — visível só com [PERM02](#perm02) (`DESPESAS_INSERIR`) —, abrir o modal ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)) em modo criação: campos vazios, Data de lançamento em hoje, Competência inicializada com o valor da competência atualmente ativa no filtro da tela (`filtro.competenciaInicio`, ou mês corrente se o filtro não definir competência), Forma de pagamento em "Conta à vista", "Já paguei" em "Não", Parcelada em "Não", seção Rateio vazia. Carregar os comboboxes conforme [RT14](#rt14). Toda série parcelada ([RN12](#rn12)) ou despesa recorrente ([RN25](#rn25)) gerada a partir do modal inicia obrigatoriamente na competência definida no formulário. |
| <a id="rt05"></a>RT05 | Ao clicar no ícone Editar ([ID23](#qdd1-23)) — visível só com [PERM03](#perm03) (`DESPESAS_EDITAR`) —, chamar [EDP03](#edp03) com o id e abrir o modal em modo edição, com os campos preenchidos. Se a despesa é uma parcela de série (`DESP_ID_PARCELA_PAI` não nula ou tem parcelas-filhas), ocultar a seção Parcelamento e mostrar o título com "parcela {n}/{qtd}" ([RN13](#rn13)). Se [C1](#c1).origem ≠ "MANUAL", desabilitar Forma de pagamento e Origem e exibir o aviso [ID24](#qdd3-24) ([RN08](#rn08)). Se há rateio, carregar as linhas. |
| <a id="rt06"></a>RT06 | Ao clicar em "Salvar" ([ID25](#qdd3-25)): validar Nome, Valor, Data de lançamento, Competência obrigatórios ([MSG02](#msg02)); Valor maior que zero ([MSG03](#msg03)); formato da Competência ([MSG11](#msg11)); conforme a Forma de pagamento, Conta **ou** Cartão obrigatório ([MSG12](#msg12)); se Parcelada, Número de parcelas ≥ 2 ([MSG13](#msg13)); se há rateio, soma das fatias ≤ Valor ([MSG17](#msg17)) e cada contato participante válido e ativo na agenda ([MSG19](#msg19)); se "Já paguei" = Sim, Data de pagamento preenchida ([MSG09](#msg09)). Em criação, chamar [EDP04](#edp04); em edição, [EDP05](#edp05). Em sucesso, exibir [MSG01](#msg01) (criação, ou [MSG14](#msg14) quando gerou série) ou [MSG04](#msg04) (edição), fechar o modal e recarregar o grid preservando filtros ativos ([RT19](#rt19)). |
| <a id="rt07"></a>RT07 | Ao clicar no ícone Excluir ([ID22](#qdd1-22)) — visível só com [PERM04](#perm04) (`DESPESAS_EXCLUIR`) e oculto quando [C1](#c1).origem = "OPEN_FINANCE" —: se a despesa é a **mãe** de uma série parcelada ou recorrente, exibir a confirmação [MSG15](#msg15) (informando quantas ocorrências); se é uma parcela isolada ou despesa avulsa, exibir [MSG06](#msg06). Ao confirmar, chamar [EDP06](#edp06). Despesas importadas do cartão (`origem = IMPORTACAO`) e manuais são excluídas normalmente; apenas despesa originada do Open Finance é bloqueada com [MSG16b](#msg16b). Em sucesso, exibir [MSG07](#msg07), remover imediatamente o item excluído da visualização do grid e recarregar os dados preservando filtros ativos ([RT19](#rt19)). |
| <a id="rt08"></a>RT08 | Ao mudar a Forma de pagamento ([ID11](#qdd3-11)): "Conta à vista" → exibir Conta ([ID12](#qdd3-12)) e Meio de pagamento ([ID15](#qdd3-15), default `DEBITO`), ocultar Cartão e Conta Carteira, exibir o toggle "Já paguei"; "Cartão de crédito" → exibir Cartão ([ID13](#qdd3-13)), ocultar os demais, fixar meio `CREDITO`, ocultar o toggle "Já paguei" ([RN23](#rn23)); "Dinheiro" → exibir Conta Carteira ([ID14](#qdd3-14)), fixar meio `DINHEIRO`, exibir o toggle "Já paguei" com default "Sim". Limpar os campos das formas não escolhidas ([RN03](#rn03), [RN22](#rn22)). |
| <a id="rt09"></a>RT09 | Ao ligar o toggle Parcelada ([ID19](#qdd3-19)): mudar o rótulo do campo Valor ([ID5](#qdd3-5)) para "Valor total da compra", exibir Número de parcelas ([ID20](#qdd3-20)) e o Valor da parcela calculado ([ID21](#qdd3-21)), recalculado a cada mudança de valor, de número de parcelas ou de composição de rateio. Quando houver rateio configurado em compra parcelada, a pré-visualização do valor estimado da parcela detalha a decomposição mensal entre o titular e os co-participantes (ex: `R$ 100,00` e subtítulo `Sua parcela: R$ 50,00 | Contatos: R$ 50,00`, ou `Sua parcela: R$ 0,00` caso a cota do titular seja zerada). Ao desligar, reverter. No modo edição de uma parcela existente, a seção Parcelamento fica **oculta** — não se reparcelam parcelas ([RN13](#rn13)). |
| <a id="rt10"></a>RT10 | **Visibilidade Condicional e Limpeza de Rateio (Zero-Leak):** A seção de Rateio ([ID22](#qdd3-22)) só é renderizada para usuários com [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`) e adota controle de visibilidade progressiva em dois níveis: (1) **Switch "Habilitar rateio"** (`#despesaHabilitarRateio`): por padrão desmarcado e com o container de rateio (`#conteudoRateio`) oculto ao abrir o modal de nova despesa ou edição de despesa sem rateio anterior. Ao marcar, exibe o formulário de rateio com foco no campo de busca de contatos. Ao abrir edição de despesa que já possui rateio ou ao clicar no botão "Ratear" do grid ([ID23](#qdd1-23)), o switch é automaticamente marcado e o container exibido com rolagem suave até a seção. (2) **Bloco Condicional de Itens** (`#blocoItensRateio`, [ID23](#qdd3-23)): a tabela de co-participantes, o botão "Dividir igualmente" e o painel "Minha Cota (Você - Titular)" só são exibidos quando há pelo menos um contato adicionado (`itensRateio.length > 0`). Enquanto não houver contatos, esse bloco permanece oculto, exibindo apenas a busca de contatos e o atalho de novo contato rápido. (3) **Limpeza Mandatória Sem Vazamento (Zero-Leak):** ao desmarcar o switch, ao clicar em "Cancelar", ao fechar o modal (evento `hidden.bs.modal` via backdrop, tecla Esc ou botão fechar) ou ao inicializar novo formulário (`abrirNovo` ou `abrirEdicao`), todos os dados de rateio em memória (`itensRateio = []`), valores de busca, datalists, caches de contatos, painel de Minha Cota, badges e alertas são completamente purgados, garantindo que nenhum dado residual contamine registros abertos posteriormente. Se o usuário desmarcar o switch e salvar a despesa, quaisquer rateios prévios são descartados/excluídos. Quando habilitado com contatos, o rateio disponibiliza: campo editável "Minha Cota" com máscara monetária contínua (`pt-BR`, `R$ 0,00`); botão de atalho "Zerar minha cota" (cota do titular = `R$ 0,00`); botão de atalho "100% / Total" na linha de cada co-participante; divisão automática equitativa ao adicionar contatos; botão dedicado "Dividir igualmente"; discriminativo de parcelas nas fatias e em Minha Cota (`Nx de R$ Y,YY`) para compras parceladas ([RN18](#rn18)); e sincronização bidirecional em tempo real. Se a soma das fatias exceder o Valor ([MSG17](#msg17)), o sistema exibe alerta em vermelho e bloqueia o salvamento. |
| <a id="rt11"></a>RT11 | Ao clicar no ícone Registrar pagamento ([ID23](#qdd1-23)) — visível só com [PERM05](#perm05) (`DESPESAS_PAGAR`) e oculto quando [C1](#c1).statusPagamento ≠ `NAO` —, abrir o modal ([QUADRO_DESCRITIVO_4](#quadro-descritivo-4)) com o Valor ([ID2](#qdd4-2)) preenchido e a Data de pagamento ([ID3](#qdd4-3)) iniciada em hoje. Ao clicar em "Registrar": validar a data preenchida ([MSG02](#msg02)) e chamar [EDP07](#edp07). Em sucesso, exibir [MSG10](#msg10), fechar o modal e recarregar o grid preservando filtros ativos ([RT19](#rt19)). |
| <a id="rt12"></a>RT12 | O botão "Registrar pagamento" da barra ([ID6](#qdd1-6)) — visível a quem tem [PERM05](#perm05) — é exibido dinamicamente apenas quando houver ao menos uma linha selecionada com status pendente (`NAO` ou `NAO_SE_APLICA`), permanecendo oculto (`display: none`) caso contrário. Ao clicar com linhas selecionadas ([ID8](#qdd1-8)), abrir o modal ([QUADRO_DESCRITIVO_5](#quadro-descritivo-5)) com o resumo da seleção. Ao confirmar, validar a data ([MSG02](#msg02)) e chamar [EDP08](#edp08) com a lista de ids e a data. Em sucesso, exibir [MSG10b](#msg10b), limpar a seleção, ocultar o botão e recarregar o grid preservando filtros ativos ([RT19](#rt19)). |
| <a id="rt13"></a>RT13 | Aplicar máscara monetária contínua em tempo real (`pt-BR`, `R$ 0,00`) ao campo Valor ([ID5](#qdd3-5)), nos inputs de edição inline ([RT18](#rt18)) e formatar a coluna Valor do grid ([ID15](#qdd1-15)) em BRL. Ao alterar a Data de lançamento ([ID6](#qdd3-6)), e enquanto o usuário não tiver editado a Competência ([ID8](#qdd3-8)) manualmente, atualizar a Competência para o mês da nova data — comportamento controlado pelo parâmetro `DESPESA_COMPETENCIA_SEGUE_DATA_LANCAMENTO` ([Seção 12](#12-parâmetros-de-sistema)). |
| <a id="rt14"></a>RT14 | Ao abrir o modal de despesa ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)), carregar o combobox Conta pelo endpoint de opções do documento `06` ([SB01](#sb01)), o combobox Cartão pelo endpoint do documento `07` ([SB02](#sb02)) e o combobox Categoria pelo endpoint do documento `04` com `aplicaA=DESPESA` ([SB03](#sb03)). Nunca renderizar `<option>` fixo no HTML. |
| <a id="rt15"></a>RT15 | **Card Totalizador por Competência Única e Altura Uniforme:** O conjunto de cards ([ID3a](#qdd1-3a)) deve ser visível se e somente se o filtro ativo possuir `competenciaInicio == competenciaFim`. Caso o filtro englobe um intervalo com competências distintas ou não haja filtro de competência definido, os cards devem permanecer ocultos ([RN30](#rn30)). São exibidos 4 cards alinhados em grade (`col-sm-6 col-lg-3` com `h-100` obrigatório em todos os cards): Pago, Pendente, Total Geral e Receita Restante / Saldo. O valor exibido nos cards de despesa corresponde à soma da **cota líquida do titular** (`valorUsuario`), deduzindo fatias transferidas a co-participantes (`valorRateado`), eliminando falsos positivos. Quando houver despesas com rateio na competência, o card Total Geral exibe subtítulo discriminando o valor bruto e o total rateado (`Bruto: R$ X,XX (Rateado: R$ Y,YY)`). O 4º card exibe a receita restante do usuário (`Total de Receitas da competência - Total de Despesas da cota do titular`), consultado de forma assíncrona via [EDP19](#edp19), exibindo valor verde/azul se positivo ou vermelho se negativo, e subtítulo com a receita do mês. |
| <a id="rt16"></a>RT16 | **Duplicação de Despesa(s) com Opção Parcelada ou Recorrente:** O botão "Duplicar selecionadas" da barra ([ID6a](#qdd1-6a)) — visível a quem tem [PERM02](#perm02) — é exibido dinamicamente apenas quando houver ao menos uma linha selecionada ([ID8](#qdd1-8)), permanecendo oculto (`display: none`) caso contrário. Ao clicar no ícone Duplicar da linha ([ID23](#qdd1-23)) ou no botão "Duplicar selecionadas" da barra, abrir o modal ([QUADRO_DESCRITIVO_7](#quadro-descritivo-7)). O usuário informa a competência de destino e seleciona o tipo de duplicação: Avulsa (padrão), Compra parcelada (com quantidade de parcelas e base de valor) ou Despesa recorrente (com quantidade de meses). Ao confirmar, enviar requisição `POST /despesas/duplicar` ([EDP12](#edp12)) com os parâmetros correspondentes. Em sucesso, exibir [MSG22](#msg22), desmarcar as seleções, ocultar o botão e recarregar o grid preservando os filtros ativos ([RT19](#rt19)). |
| <a id="rt17"></a>RT17 | **Importação de Fatura de Cartão de Crédito:** Ao clicar no botão "Importar extrato" ([ID6b](#qdd1-6b)), abrir o modal ([QUADRO_DESCRITIVO_6](#quadro-descritivo-6)). O usuário seleciona o Cartão ([SB02](#sb02)), a Competência, a Data de Vencimento (opcional), o Formato ([SB07](#sb07)) e seleciona o arquivo. Ao clicar em "Importar Fatura", enviar requisição multipart `POST /despesas/importar-extrato` ([EDP14](#edp14)). Em sucesso, exibir [MSG23](#msg23), fechar o modal e recarregar o grid preservando filtros ativos ([RT19](#rt19)). Em erro de arquivo ou formato, exibir [MSG24](#msg24). |
| <a id="rt18"></a>RT18 | **Edição Inline de Valor no Grid:** Ao dar duplo clique ou clicar no ícone de edição na célula da coluna Valor ([ID15](#qdd1-15)) em uma linha ativa (visível a quem tem [PERM03](#perm03) `DESPESAS_EDITAR`), a célula é substituída por um input monetário com foco automático e máscara `R$ 0,00`. Ao teclar `Enter` ou perder o foco (`blur`), se o valor foi modificado e é maior que zero, enviar requisição AJAX `PATCH /despesas/{id}/valor` ([EDP13](#edp13)) com payload `{ "valor": novoValor }`. Em sucesso, exibir notificação toast com [MSG25](#msg25), atualizar a célula formatada com o valor e o badge de cota do titular caso haja rateio e recalcular o card totalizador se visível ([RT15](#rt15)). Se teclar `Esc`, cancelar a edição restaurando o valor anterior sem requisição ao servidor. |
| <a id="rt19"></a>RT19 | **Preservação de Filtros Ativos e Paginação:** Após a execução de qualquer comando de mutação (criação, edição, exclusão, baixa de pagamento, duplicação ou importação), o DataTables deve recarregar os dados via AJAX mantendo a página atual, ordenação e os filtros vigentes no momento da operação sem resetar a navegação do usuário. |
| <a id="rt20"></a>RT20 | **Edição Inline de Competência no Grid:** Ao clicar na célula da coluna Competência ([ID14](#qdd1-14)) em uma linha ativa (visível a quem tem [PERM03](#perm03) `DESPESAS_EDITAR`), a célula é substituída por um input de mês (`<input type="month">`) com foco automático. Ao teclar `Enter` ou perder o foco (`blur`), se a competência foi modificada e é válida (`yyyy-MM`), enviar requisição AJAX `PATCH /despesas/{id}/competencia` ([EDP15](#edp15)). Em sucesso, exibir notificação toast com [MSG27](#msg27), atualizar a célula formatada (`MM/yyyy`) e atualizar o grid e o totalizador conforme os filtros ativos. Se teclar `Esc`, cancelar a edição restaurando a competência anterior sem requisição ao servidor. |
| <a id="rt21"></a>RT21 | **Ordenação Estável e Determinística:** A ordenação das colunas do grid deve aplicar critérios determinísticos acompanhando a direção (`asc`/`desc`). Para a coluna **Competência**, a ordenação principal avalia o mês/ano (`yyyy-MM`) e, havendo empate (como no filtro padrão de competência única), desempata cronologicamente pelo vencimento (`dataVencimento`), pela data de lançamento (`dataLancamento`), pelo nome da despesa (`nome`), pelo número da parcela (`nroParcela`, caso pertençam à mesma série/compra) e pelo identificador único (`id`), todos respeitando a direção `asc`/`desc`. Para ordenação por outras colunas (como `nome` ou `valor`), o desempate por `nroParcela` aplica-se exclusivamente a parcelas da mesma série parcelada (`idParcelaPai` idêntico ou mesmo `nome`), seguido de `competencia`, `dataVencimento`, `nome` e `id`. O cabeçalho deve exibir indicadores visuais claros da coluna e direção ordenadas (`ph-caret-up` e `ph-caret-down`). |
| <a id="rt22"></a>RT22 | **Reordenação por Drag and Drop e Coluna Número de Ordem:** O grid de Despesas disponibiliza a coluna **Nº Ordem** ([ID8a](#qdd1-8a)), contendo um identificador ordinal (1..X) e uma alça de arrasto (`ph-dots-six-vertical`). Ao carregar os dados, a numeração sequencial é atribuída inicialmente com base no identificador único (`id`) em ordem crescente (do menor ID para o maior ID, numerando sequencialmente de 1 a X) ou na ordem personalizada salva no navegador (`localStorage`). O grid respeita inicialmente a ordenação pela coluna **Nº Ordem** em modo ascendente. O usuário pode arrastar qualquer linha para uma nova posição (Drag and Drop nativo HTML5). Ao soltar a linha: (1) a nova sequência de itens da listagem exibida é consolidada; (2) os números ordinais de todas as linhas exibidas são recalculados e reenumerados automaticamente de forma sequencial (1..X); (3) a ordem resultante é persistida no `localStorage`; (4) a ordenação do grid é fixada na coluna `ordem` ascendente; (5) a visualização da tabela é re-renderizada imediatamente preservando seleções e estado visual. Ao clicar no cabeçalho "Nº Ordem", a ordenação numérica é alternada entre crescente e decrescente. |
| <a id="rt23"></a>RT23 | **Edição Inline de Categoria no Grid:** Ao clicar na célula da coluna Categoria ([ID12](#qdd1-12)) em uma linha ativa (visível a quem tem [PERM03](#perm03) `DESPESAS_EDITAR`), a célula é substituída por um combobox (`<select>`) com foco automático, carregado com as opções de categorias ativas de despesa (`aplicaA=DESPESA` ou `AMBOS`, obtidas do endpoint de opções do documento 04) e a opção inicial "Sem categoria". Ao selecionar uma opção ou perder o foco (`blur`), se a categoria foi modificada, enviar requisição AJAX `PATCH /despesas/{id}/categoria` ([EDP16](#edp16)) com parâmetro `categoriaId` (ou vazio para desvincular). Em sucesso, exibir notificação toast com [MSG28](#msg28), atualizar a célula formatada com o badge da categoria correspondente e atualizar a listagem caso haja filtro por categoria ativo. Se teclar `Esc`, cancelar a edição restaurando a categoria anterior sem requisição ao servidor. |
| <a id="rt24"></a>RT24 | **Edição Inline de Forma e Meio no Grid:** Ao clicar na célula da coluna Forma / Meio ([ID13](#qdd1-13)) em uma linha ativa (visível a quem tem [PERM03](#perm03) `DESPESAS_EDITAR` e despesa não originada de `OPEN_FINANCE`), a célula é substituída por um combobox (`<select>`) com foco automático agrupado por `<optgroup>` com as opções ativas do usuário: (1) **Cartões de Crédito** (lista de cartões ativos, meio `CREDITO`); (2) **Contas Bancárias** (lista de contas bancárias ativas associadas a opções de meio de pagamento como `Débito`, `PIX`, `Boleto` e `Transferência`); (3) **Dinheiro** (conta carteira ativa com meio `DINHEIRO`). A opção atual da despesa vem pré-selecionada. Ao mudar a seleção ou perder o foco (`blur`), se a forma/meio foi modificada, enviar requisição AJAX `PATCH /despesas/{id}/forma-pagamento` ([EDP17](#edp17)) com parâmetros `formaPagamento`, `cartaoId`, `contaId` e `meioPagamento`. Em sucesso, exibir notificação toast com [MSG30](#msg30), atualizar os badges e dados da despesa e re-renderizar a visualização. Se teclar `Esc`, cancelar a edição restaurando a exibição anterior sem requisição ao servidor. |
| <a id="rt25"></a>RT25 | **Edição Inline de Nome no Grid:** Ao clicar na célula da coluna Nome ([ID10](#qdd1-10)) em uma linha ativa (visível a quem tem [PERM03](#perm03) `DESPESAS_EDITAR`), a célula é substituída por um input de texto (`<input type="text">`, máximo 100 caracteres) com foco automático. Ao teclar `Enter` ou perder o foco (`blur`), se o nome foi modificado e não está vazio, enviar requisição AJAX `PATCH /despesas/{id}/nome` ([EDP18](#edp18)) com parâmetro `nome`. Em sucesso, exibir notificação toast com [MSG31](#msg31), atualizar os dados da despesa na memória e re-renderizar a célula. Se o nome estiver em branco, exibir notificação toast com [MSG32](#msg32) e restaurar o nome anterior. Se teclar `Esc`, cancelar a edição restaurando a exibição anterior sem requisição ao servidor. |

---

## 8. Endpoints

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) (`DESPESAS_LISTAR`) | /despesas/listar | N |
| Retorna a página da listagem de despesas (Thymeleaf). O grid é carregado por [EDP02](#edp02). | | | | |
| <a id="edp02"></a>EDP02 | GET | [PERM01](#perm01) (`DESPESAS_LISTAR`) | /despesas/listar-dados | N |
| Lista das despesas do usuário autenticado para o grid, em JSON. Executa [C1](#c1) com o `USU_ID` do contexto de segurança ([RN02](#rn02)). Campos por linha: id, competencia, nome, descricao, valor, valorTotalCompra, valorUsuario (cota líquida do titular), valorRateado (total rateado com contatos), parcelada, nroParcela, qtdParcelas, idParcelaPai, meioPagamento, statusPagamento, dataLancamento, dataVencimento, dataPagamento, origem, contaId, contaDescricao, cartaoId, cartaoDescricao, categoriaId, categoriaNome, temRateio (boolean), qtdCoParticipantes, excluido (boolean). Sem paginação (client-side). | | | | |
| <a id="edp03"></a>EDP03 | GET | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/buscar/{id} | N |
| Retorna uma despesa do usuário autenticado para edição. Executa [RN02](#rn02) (via [C2](#c2)) — se não for do usuário, 404 ([MSG05](#msg05)). Campos: os de [EDP02](#edp02) mais o rateio (lista de {contatoId, contatoNome, contatoEmail, contatoTipo, contatoChavePix, valor, statusPagamento, dataAcerto}, via [C7](#c7)). | | | | |
| <a id="edp04"></a>EDP04 | POST | [PERM02](#perm02) (`DESPESAS_INSERIR`) | /despesas/inserir | N |
| Cria uma despesa para o usuário autenticado. Executa, nesta ordem: [RN03](#rn03) (via [C3](#c3) e/ou [C4](#c4) — conta XOR cartão, ativos e do usuário), [RN04](#rn04) (competência), [RN05](#rn05) (valor > 0), [RN06](#rn06) (datas), [C5](#c5) (categoria, quando informada), [RN22](#rn22) (deriva `DESP_MEIO_PAGAMENTO`), [RN23](#rn23) (compra no cartão → `NAO` ou `SIM` se informado pagamento), [RN07](#rn07) (fixa `DESP_ORIGEM = MANUAL`); se rateado, [RN15](#rn15) (permissão efetiva), [C8](#c8) (contatos participantes válidos e ativos na agenda), [RN17](#rn17) (soma das fatias); persiste a despesa-mãe; se parcelada, [RN12](#rn12) (gera as demais parcelas); se recorrente, [RN25](#rn25) (gera as ocorrências projetadas); se rateado, [RN18](#rn18) (replica o rateio em cada parcela/ocorrência). Dados: competencia, nome, descricao, valor, valorTotalCompra, dataLancamento, dataVencimento, dataPagamento, formaPagamento, contaId, cartaoId, meioPagamento, statusPagamento, categoriaId, parcelada, qtdParcelas, recorrente, qtdMesesRecorrencia, rateio[] (contatoId, valor, statusPagamento, dataAcerto). Retorno: 200 ([MSG01](#msg01) / [MSG14](#msg14)) ou 422 ([MSG02](#msg02) / [MSG03](#msg03) / [MSG09](#msg09) / [MSG11](#msg11) / [MSG12](#msg12) / [MSG13](#msg13) / [MSG16](#msg16) / [MSG17](#msg17) / [MSG18](#msg18) / [MSG19](#msg19)). | | | | |
| <a id="edp05"></a>EDP05 | PUT | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/editar/{id} | N |
| Edita **uma** despesa do usuário autenticado (uma parcela isolada, quando série). Executa, nesta ordem: [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário), [RN08](#rn08) (se origem ≠ MANUAL, ignora forma de pagamento e origem), [RN13](#rn13) (não altera número/valor total de parcelas de uma série; esses campos são ignorados), [RN03](#rn03) (via [C3](#c3)/[C4](#c4)), [RN04](#rn04), [RN05](#rn05), [RN06](#rn06), [C5](#c5) (categoria), [RN22](#rn22); se rateio no corpo, [RN15](#rn15), [C8](#c8), [RN17](#rn17); persiste. Dados: os de [EDP04](#edp04) exceto `parcelada`/`qtdParcelas`/`valorTotalCompra`/`recorrente`. Retorno: 200 ([MSG04](#msg04)) ou 422 (mesmas de [EDP04](#edp04)). | | | | |
| <a id="edp06"></a>EDP06 | DELETE | [PERM04](#perm04) (`DESPESAS_EXCLUIR`) | /despesas/excluir/{id} | N |
| Exclusão lógica da despesa do usuário autenticado. Executa [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário), [RN08](#rn08) (recusa somente se origem = OPEN_FINANCE → [MSG16b](#msg16b)); permite MANUAL e IMPORTACAO), [RN14](#rn14) (série parcelada) e [RN25](#rn25) (série recorrente): se o `{id}` é a **despesa-mãe** de uma série parcelada ou recorrente, exclui logicamente **todas** as ocorrências (via [C6](#c6)) e as `DESPESAS_USUARIO` de cada uma; se é ocorrência filha isolada ou despesa avulsa, exclui só ela. Preenche `audit_data_exclusao` / `audit_excluido_por`. Retorno: 200 ([MSG07](#msg07)) ou 422 ([MSG16b](#msg16b)). | | | | |
| <a id="edp07"></a>EDP07 | PUT | [PERM05](#perm05) (`DESPESAS_PAGAR`) | /despesas/registrar-pagamento/{id} | N |
| Registra o pagamento de **uma** despesa do usuário autenticado. Executa [RN02](#rn02) (via [C2](#c2) — 404 se não for do usuário) e [RN21](#rn21). Dados: dataPagamento. Grava `DESP_IND_STATUS_PAGAMENTO = SIM` e `DESP_DT_PAGAMENTO = dataPagamento` (inclusive para despesas vinculadas a cartão de crédito ou com status legado `NAO_SE_APLICA`). O Envers registra a revisão. Retorno: 200 ([MSG10](#msg10)) ou 422 ([MSG02](#msg02)). | | | | |
| <a id="edp08"></a>EDP08 | POST | [PERM05](#perm05) (`DESPESAS_PAGAR`) | /despesas/registrar-pagamento-lote | N |
| Registra o pagamento de **várias** despesas do usuário autenticado (espelha `pagarDespesas` da geração 1). Executa [C9](#c9) (todas as despesas do lote são do usuário; qualquer id fora do escopo → 404 sem baixar nenhuma) e, por despesa, [RN21](#rn21). Dados: idDespesaList[], dataPagamento. Retorno: 200 ([MSG10b](#msg10b)) ou 422. | | | | |
| <a id="edp09"></a>EDP09 | PUT | [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`) | /despesas/{id}/rateio-acerto | N |
| Registra ou desfaz o **acerto** de uma fatia do rateio (pelo dono). Executa [RN02](#rn02) (via [C2](#c2) — 404), [RN15](#rn15) (permissão efetiva) e [RN20](#rn20). Dados: contatoId, acertado (boolean), dataAcerto. Grava `DEPU_IND_STATUS_PAGAMENTO` (`SIM`/`NAO`) e `DEPU_DT_ACERTO` na linha `(DESP_ID = {id}, CONT_ID = contatoId)`. Retorno: 200 ([MSG21](#msg21)) ou 422 ([MSG16](#msg16) / [MSG19](#msg19)). | | | | |
| <a id="edp10"></a>EDP10 | GET | [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`) | /despesas/contatos-rateio?termo= | N |
| Busca contatos **ativos** (`CONT_STATUS = 'ATIVO'`) pertencentes à agenda privada do usuário autenticado (`USU_ID_DONO = :usuIdLogado`), por parte do nome, e-mail ou telefone (mínimo 3 caracteres). Executa [RN15](#rn15), [RN19](#rn19) e [C10](#c10). Campos: id, nome, email, telefone, tipo (`EXTERNO` ou `SISTEMA`), chavePix. Máx. 20 itens. Alimenta [SB06](#sb06). | | | | |
| <a id="edp11"></a>EDP11 | POST | [PERM07](#perm07) (`DESPESA_RATEAR_MULTIUSUARIO`) | /despesas/contatos-rapido | N |
| Cadastro rápido inline de contato extra-sistema (`CONT_TIPO = 'EXTERNO'`) acionado diretamente pelo modal de rateio de despesa ([RF16](#rf16), [RT10](#rt10)). Executa [RN15](#rn15). Grava em `CONTATOS` com `USU_ID_DONO = :usuIdLogado`, `CONT_TIPO = 'EXTERNO'`, `CONT_STATUS = 'ATIVO'`. Dados: nome (obrigatório), email (opcional), telefone (opcional), chavePix (opcional). Retorno: 200 (JSON do contato criado com id, [MSG19b](#msg19b)) ou 422 ([MSG02](#msg02)). | | | | |
| <a id="edp12"></a>EDP12 | POST | [PERM02](#perm02) (`DESPESAS_INSERIR`) | /despesas/duplicar | N |
| Duplica uma ou mais despesas para a competência informada ([RF21](#rf21), [RN26](#rn26), [RT16](#rt16)). Executa [C9](#c9) (validação de escopo de todas as despesas informadas). Dados: `despesaIds[]`, `competenciaDestino`, `tipo` (`AVULSA`, `PARCELADA`, `RECORRENTE`), `qtdParcelas`, `valorTotal`, `qtdMesesRecorrencia`. Cria cópias em aberto com `DESP_ORIGEM = 'MANUAL'`, projetando séries quando parcelada ou recorrente. Retorno: 200 ([MSG22](#msg22)) ou 422 ([MSG26](#msg26)). | | | | |
| <a id="edp13"></a>EDP13 | PATCH | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/{id}/valor | N |
| Atualiza inline o valor de uma despesa diretamente a partir da célula do grid ([RF22](#rf22), [RN27](#rn27), [RT18](#rt18)). Executa [RN02](#rn02) (escopo por usuário) e [RN05](#rn05) (valor maior que zero). Atualiza `DESP_VALOR` e audita via Envers. Retorno: 200 (JSON com id e novo valor, [MSG25](#msg25)) ou 422 ([MSG03](#msg03)). | | | | |
| <a id="edp14"></a>EDP14 | POST | [PERM06](#perm06) (`DESPESAS_IMPORTAR`) | /despesas/importar-extrato | N |
| Importa faturas ou extratos de cartão de crédito da geração 1 nos formatos Excel Itaú, Bradesco, C6 Bank e arquivos OFX via Apache POI e OFX4J ([RF20](#rf20), [RN28](#rn28), [RT17](#rt17)). Executa [C4](#c4) (validação do cartão de crédito do usuário autenticado). Dados multipart: `arquivo`, `cartaoId`, `competencia`, `formato`, `dtVencimento` (opcional). Gera as despesas com `DESP_ORIGEM = 'IMPORTACAO'`, `CACR_ID` vinculado e status `NAO`. Retorno: 200 (JSON com contagem de despesas importadas, [MSG23](#msg23)) ou 422 ([MSG24](#msg24)). | | | | |
| <a id="edp15"></a>EDP15 | PATCH | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/{id}/competencia | N |
| Atualiza inline a competência de uma despesa diretamente a partir da célula do grid ([RF23](#rf23), [RN32](#rn32), [RT20](#rt20)). Executa [RN02](#rn02) (escopo por usuário) e [RN04](#rn04) (formato de competência yyyy-MM). Atualiza `DESP_COMPETENCIA` e audita via Envers. Retorno: 200 (JSON com id e nova competência, [MSG27](#msg27)) ou 422 ([MSG11](#msg11)). | | | | |
| <a id="edp16"></a>EDP16 | PATCH | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/{id}/categoria | N |
| Atualiza inline a categoria de uma despesa diretamente a partir da célula do grid ([RF23](#rf23), [RN33](#rn33), [RT23](#rt23)). Executa [RN02](#rn02) (escopo por usuário) e validação da categoria informada ([C5](#c5) — ativa e aplicável a despesa, ou nula para desvincular). Atualiza `CATE_ID` e audita via Envers. Retorno: 200 (JSON com id, categoriaId e categoriaNome, [MSG28](#msg28)) ou 422 ([MSG29](#msg29)). | | | | |
| <a id="edp17"></a>EDP17 | PATCH | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/{id}/forma-pagamento | N |
| Atualiza inline a forma e o meio de pagamento de uma despesa diretamente a partir da célula do grid ([RF25](#rf25), [RN34](#rn34), [RT24](#rt24)). Executa [RN02](#rn02) (escopo por usuário), recusa despesa de origem `OPEN_FINANCE` e valida o cartão ou conta informados. Atualiza `CACR_ID`, `CTA_ID`, `DESP_MEIO_PAGAMENTO`, status de pagamento e audita via Envers. Retorno: 200 (JSON com id, formaPagamento, cartaoId, cartaoDescricao, contaId, contaDescricao, meioPagamento, statusPagamento, [MSG30](#msg30)) ou 422 ([MSG12](#msg12) / [MSG16](#msg16)). | | | | |
| <a id="edp18"></a>EDP18 | PATCH | [PERM03](#perm03) (`DESPESAS_EDITAR`) | /despesas/{id}/nome | N |
| Atualiza inline o nome de uma despesa diretamente a partir da célula do grid ([RF26](#rf26), [RN35](#rn35), [RT25](#rt25)). Executa [RN02](#rn02) (escopo por usuário) e validação de nome obrigatório (não nulo e não em branco, máx 100 caracteres). Atualiza `DESP_NOME` e audita via Envers. Retorno: 200 (JSON com id e novo nome, [MSG31](#msg31)) ou 422 ([MSG32](#msg32)). | | | | |
| <a id="edp19"></a>EDP19 | GET | [PERM01](#perm01) (`DESPESAS_VISUALIZAR`) | /despesas/receita-competencia?competencia= | N |
| Consulta o somatório das receitas ativas do usuário autenticado na competência informada (`yyyy-MM`) para cálculo do saldo restante ([RN30](#rn30), [RT15](#rt15)). Dados: `competencia`. Retorno: 200 (JSON `{ "competencia": "yyyy-MM", "totalReceita": 0.00 }`). | | | | |

> Os comboboxes de conta, cartão e categoria são alimentados pelos endpoints de opções dos documentos `06`, `07` e `04` respectivamente ([EDP07 do `06`](../06%20-%20manter-conta/documento-analise-manter-conta.md#edp07), [EDP07 do `07`](../07%20-%20manter-cartao-credito/documento-analise-manter-cartao-credito.md#edp07), [EDP07 do `04`](../04%20-%20manter-categoria/documento-analise-manter-categoria.md#edp07)). Este documento não os redefine.

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | Cada endpoint exige a autoridade atômica da sua operação: [EDP01](#edp01) / [EDP02](#edp02) → `PERM_DESPESAS_LISTAR`; [EDP04](#edp04) / [EDP12](#edp12) → `PERM_DESPESAS_INSERIR`; [EDP03](#edp03) / [EDP05](#edp05) / [EDP13](#edp13) / [EDP15](#edp15) / [EDP16](#edp16) / [EDP17](#edp17) / [EDP18](#edp18) → `PERM_DESPESAS_EDITAR`; [EDP06](#edp06) → `PERM_DESPESAS_EXCLUIR`; [EDP07](#edp07) / [EDP08](#edp08) → `PERM_DESPESAS_PAGAR`; [EDP14](#edp14) → `PERM_DESPESAS_IMPORTAR`; [EDP09](#edp09) / [EDP10](#edp10) / [EDP11](#edp11) → `PERM_DESPESA_RATEAR_MULTIUSUARIO`. É **terminantemente proibido** o uso de permissões agregadas com sufixo `MANTER` (`DESPESAS_MANTER`). As autoridades são resolvidas pelo `getAuthorities()` do `Usuario` como **permissão efetiva = permissões do perfil ∪ permissões do plano** (Documento 0, Observação 24). A permissão **habilita a tela / o recurso**; o recorte por dono é a [RN02](#rn02). |
| <a id="rn02"></a>RN02 | **Escopo por usuário (*row-level*).** Toda consulta e todo comando de `DESPESAS` são restritos às despesas cuja conta (`DESP.CTA_ID → CONTAS.USU_ID`) **ou** cujo cartão (`DESP.CACR_ID → CARTOES_CREDITO.USU_ID`) pertence ao usuário autenticado, obtido do contexto de segurança — nunca de um parâmetro. Em [EDP03](#edp03), [EDP05](#edp05), [EDP06](#edp06), [EDP07](#edp07), [EDP08](#edp08), [EDP09](#edp09), [EDP12](#edp12), [EDP13](#edp13), [EDP15](#edp15), [EDP16](#edp16), [EDP17](#edp17) e [EDP18](#edp18), se a despesa do `{id}` não for do usuário, o serviço responde **404** com [MSG05](#msg05), sem distinguir "não existe" de "é de outro usuário". Executa [C2](#c2). Aparecer como co-participante do rateio **não** dá acesso à despesa ([RN19](#rn19)). Espelha o `findDespesasByUsuarioId(...)` da geração 1. |
| <a id="rn03"></a>RN03 | **Âncora de pagamento: conta XOR cartão.** Toda despesa tem **exatamente uma** de `CTA_ID` ou `CACR_ID` — nunca ambas, nunca nenhuma (embora o schema permita as duas anuláveis — [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10)). A conta informada deve existir, estar ativa (`CTA_FL_ATIVO = TRUE`) e pertencer ao usuário (executa [C3](#c3)); o cartão, idem (`CACR_FL_ATIVO = TRUE`, executa [C4](#c4)). Conta/cartão inválidos → [MSG12](#msg12); conta e cartão juntos (ou nenhum) → [MSG12b](#msg12b). Dinheiro em espécie usa uma conta do tipo `CARTEIRA` ([Observação 2a](#2-observações)). |
| <a id="rn04"></a>RN04 | `DESP_COMPETENCIA` é obrigatória, `CHAR(7)` no formato `yyyy-MM` (validada pelo `CHECK` no banco e pelo `YearMonthConverter` no mapeamento — Documento 0, Seção 7.4). Fora do formato → [MSG11](#msg11). É um campo **próprio e independente**: a tela pré-preenche com o mês da data de lançamento ([RT13](#rt13)), mas o usuário ajusta livremente e ela pode divergir do mês da data de lançamento. Não há validação que force a igualdade. Aceita competências passadas irrestritas ([RN29](#rn29)). **[Requer código]** |
| <a id="rn05"></a>RN05 | `DESP_VALOR` é obrigatório, `DECIMAL(15,2)`, e deve ser **maior que zero** — ausente, zero ou negativo → [MSG03](#msg03). Numa despesa parcelada, o campo informado pela tela é o **valor total da compra** (`DESP_VALOR_TOTAL_COMPRA`), também maior que zero; `DESP_VALOR` de cada parcela é derivado ([RN12](#rn12)). Não há regra de unicidade. |
| <a id="rn06"></a>RN06 | **Datas.** `DESP_DT_LANCAMENTO` é obrigatória; `DESP_DT_VENCIMENTO` e `DESP_DT_PAGAMENTO` são opcionais. Todas aceitam datas **posteriores a hoje** — a tela serve para planejar despesas previstas. O serviço não rejeita data futura; a única validação de data é o formato. |
| <a id="rn07"></a>RN07 | `DESP_ORIGEM` das despesas criadas por esta tela é **sempre `MANUAL`** — o serviço fixa o valor em [EDP04](#edp04) e em [EDP12](#edp12), ignorando qualquer origem no corpo. `OPEN_FINANCE` só chega pela conciliação (documento `16`); `IMPORTACAO`, pela rotina de importação de faturas ([EDP14](#edp14), [RN28](#rn28)). Não há migração da geração 1 (Documento 0, Observação 18a). |
| <a id="rn08"></a>RN08 | **Despesa Open Finance vs Importada**: para despesas com `DESP_ORIGEM = 'OPEN_FINANCE'`, na edição ([EDP05](#edp05)), `CTA_ID`, `CACR_ID` e `DESP_ORIGEM` **não** são alteráveis — valores divergentes no corpo são ignorados. Na exclusão ([EDP06](#edp06)), a operação é **recusada** → [MSG16b](#msg16b), porque a transação de *staging* de Open Finance guarda um vínculo lógico com a despesa gerada (Documento 0, Observações 19 e 21). Já para despesas importadas do cartão (`DESP_ORIGEM = 'IMPORTACAO'`), a exclusão é **permitida** normalmente ([EDP06](#edp06)), removendo a despesa logicamente. Os demais campos de negócio **são** editáveis. |
| <a id="rn09"></a>RN09 | Exclusão de despesa: é sempre **lógica** (`audit_data_exclusao` / `audit_excluido_por`) e **não checa uso** por FK externa — nada no domínio referencia `DESPESAS` além de `DESPESAS_USUARIO` (rateio, excluído junto — [RN14](#rn14)) e, quando o documento `11` existir, `FATURAS_CARTAO` via `DESP.FTCA_ID` (o efeito da exclusão de uma compra sobre a fatura é tratado lá). A única restrição desta tela é a da [RN08](#rn08). |
| <a id="rn10"></a>RN10 | `DESPESAS` e `DESPESAS_USUARIO` são auditadas via Hibernate Envers (`@Audited`). Cada criação, edição, geração de parcelamento, recorrência, duplicação, edição inline de valor, edição inline de competência, importação de fatura, rateio, acerto de fatia, baixa de pagamento e exclusão lógica gera uma revisão com autor e data. |
| <a id="rn11"></a>RN11 | **Mudanças de estrutura vs geração 1 (requer código):** removidos os enums `DESP_TIPO_TRANSACAO` (`TipoRegistroFinanceiro`) e `DESP_TIPO_RECEITA_DESPESA` (`CategoriaRegistroFinanceiro`); categoria vira `@ManyToOne Categoria` (`CATE_ID`). `DESP_VALOR_PARCELADO` + `DESP_VALOR_TOTAL_A_DIVIDIR` → `DESP_VALOR_TOTAL_COMPRA`. `idParcelaPai` `Long` solto → `@ManyToOne Despesa parcelaPai` (`DESP_ID_PARCELA_PAI`, FK). `DespesaUsuario.statusPagamento` `boolean` → `StatusPagamento` (enum). `dtLancamento` → `LocalDate`; `competencia` `String "0000-00"` → `YearMonth` (via `YearMonthConverter`). Entram `meioPagamento` (`MeioPagamento`), `flPagamentoFatura` (`boolean`), `origem` (`OrigemLancamento`), `cartao` (`@ManyToOne CartaoCredito`, `CACR_ID`), `fatura` (`@ManyToOne FaturaCartao`, `FTCA_ID`), `DespesaUsuario.dataAcerto` (`LocalDate`). `Despesa` passa a herdar de `LancamentoFinanceiro` (Documento 0, Seção 7.2). A importação de dados de cartão de crédito da geração 1 (`importarDadosCartaoCredito*`) é resgatada e portada para a geração 2 via Apache POI e OFX4J ([RN28](#rn28)). **[Requer código]** |
| <a id="rn12"></a>RN12 | **Parcelamento — geração da série.** Quando `parcelada = true`, [EDP04](#edp04) recebe o **valor total da compra** (`T`) e o **número de parcelas** (`N`, inteiro ≥ 2 e ≤ `DESPESA_PARCELAS_MAXIMO`; fora disso → [MSG13](#msg13)). O serviço: (1) calcula a parcela base `P = arredonda_para_baixo(T / N, 2 casas)`; (2) grava a **despesa-mãe** como a **1ª parcela** — `DESP_NRO_PARCELA = 1`, `DESP_QTD_PARCELAS = N`, `DESP_FL_PARCELADA = TRUE`, `DESP_ID_PARCELA_PAI = NULL`, `DESP_VALOR = P`, `DESP_VALOR_TOTAL_COMPRA = T`; (3) cria as parcelas `2..N` copiando a mãe, com `DESP_ID_PARCELA_PAI = {id da mãe}`, `DESP_NRO_PARCELA = i`, `DESP_VALOR_TOTAL_COMPRA = T`, competência e data de vencimento avançando **1 mês** por parcela (a data de lançamento é a mesma da compra em todas); (4) a **última** parcela (`i = N`) recebe `DESP_VALOR = T − P × (N−1)` para fechar exatamente o total (parâmetro `DESPESA_RESIDUO_PARCELA_NA_ULTIMA` — se `false`, o resíduo vai na primeira). Cada parcela nasce com `DESP_IND_STATUS_PAGAMENTO = NAO` (inclusive se for compra no cartão). Espelha o `gerarParcelamento` da geração 1, corrigindo a divisão do valor. |
| <a id="rn13"></a>RN13 | **Parcelamento — edição.** [EDP05](#edp05) edita **uma parcela isoladamente** — nome, descrição, valor, categoria, datas, status de pagamento e rateio daquela parcela. **Não** reprocessa a série: `DESP_QTD_PARCELAS`, `DESP_VALOR_TOTAL_COMPRA` e `DESP_FL_PARCELADA` são imutáveis pela edição e valores no corpo são ignorados. Para mudar o número de parcelas ou o valor total, o usuário exclui a série ([RN14](#rn14)) e cria de novo. O comportamento alternativo (reprocessar a série ao editar a mãe) é item "A Confirmar" (Seção 17). |
| <a id="rn14"></a>RN14 | **Parcelamento — exclusão.** [EDP06](#edp06) sobre a **despesa-mãe** (`DESP_NRO_PARCELA = 1` e com parcelas-filhas) exclui logicamente **toda a série** — a mãe e todas as parcelas com `DESP_ID_PARCELA_PAI = {id da mãe}` (via [C6](#c6)), mais as `DESPESAS_USUARIO` de cada uma. [EDP06](#edp06) sobre uma **parcela-filha** exclui **só aquela parcela**, deixando a série incompleta (a tela avisa com [MSG06](#msg06)). Excluir a série pela mãe é o caminho recomendado; a possibilidade de "reajustar as parcelas restantes" ao apagar uma do meio é item "A Confirmar" (Seção 17). |
| <a id="rn15"></a>RN15 | **Rateio é recurso de plano pago.** As operações de rateio ([EDP09](#edp09), [EDP10](#edp10) e o bloco `rateio[]` de [EDP04](#edp04)/[EDP05](#edp05)) exigem a **permissão efetiva** `DESPESA_RATEAR_MULTIUSUARIO` (`PERM_FL_CONCEDIVEL_POR_PLANO = TRUE` — Documento 0, Observação 24). Sem ela: a seção de rateio do modal não é renderizada ([RT10](#rt10)), o ícone "Ratear" do grid não aparece, e qualquer `rateio[]` no corpo de [EDP04](#edp04)/[EDP05](#edp05) é **recusado** → [MSG16](#msg16). Na carga inicial a permissão **não** é vinculada a nenhum perfil (Seção 13.1) — chega só pelo plano. |
| <a id="rn16"></a>RN16 | **Rateio — a despesa é do criador e participantes são contatos.** O rateio não transfere a propriedade: a despesa continua sendo do usuário dono da conta/cartão ([RN02](#rn02)). Os co-participantes são **contatos** da agenda privada do criador (`CONTATOS`, [C8](#c8)), sejam contatos extra-sistema (`CONT_TIPO = 'EXTERNO'`) ou usuários conectados via convite (`CONT_TIPO = 'SISTEMA'`), devendo estar ativos (`CONT_STATUS = 'ATIVO'`). São vinculados como linhas em `DESPESAS_USUARIO` com uma fatia (`DEPU_VALOR > 0`). O par (`DESP_ID`, `CONT_ID`) é único (Documento 0, [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11)); o próprio criador não precisa figurar como linha explícita ([RN17](#rn17)). É expressamente suportado o cenário em que o titular arca financeiramente com o pagamento da despesa perante a instituição, mas transfere 100% da responsabilidade/fatia para um ou mais contatos (sua cota líquida na despesa é igual a `R$ 0,00`). |
| <a id="rn17"></a>RN17 | **Rateio — soma das fatias e cota do titular.** A soma dos `DEPU_VALOR` das linhas de co-participantes deve ser **≤ `DESP_VALOR`** da despesa (na parcelada, ≤ `DESP_VALOR` de cada parcela). Se a soma exceder → [MSG17](#msg17). A diferença (`DESP_VALOR` − soma) é a **cota implícita do dono** e não precisa de linha própria no banco de dados. Quando a soma das fatias dos co-participantes for exatamente igual a `DESP_VALOR`, a cota do dono é `R$ 0,00` (zerada), sendo válida e aceita pelo sistema em criação e edição. |
| <a id="rn18"></a>RN18 | **Rateio — replicação nas parcelas.** Quando a despesa é parcelada, o rateio informado é replicado em **cada** parcela da série: para cada co-participante, cria-se uma `DESPESAS_USUARIO` por parcela com `DEPU_VALOR` proporcional ao valor daquela parcela (mesma proporção da fatia sobre o total). O acerto ([RN20](#rn20)) é por (parcela, contato). Espelha a geração 1. |
| <a id="rn19"></a>RN19 | **Rateio — privacidade da rede e isolamento de contatos.** A busca de participantes de rateio ([EDP10](#edp10), [C10](#c10)) é restrita exclusivamente aos contatos cadastrados na agenda privada do usuário autenticado (`CONTATOS.USU_ID_DONO = :usuIdLogado`). É proibida qualquer busca global ou exposição da base de usuários da plataforma (`USUARIOS`). Além disso, figurar como co-participante em uma despesa (mesmo sendo contato do tipo `SISTEMA`) não dá acesso à despesa nesta tela — [EDP02](#edp02) e [C1](#c1) filtram estritamente pela conta/cartão do dono da despesa. A visão compartilhada ("Dividido comigo") fica para evolução futura do módulo `17 - manter-contato`. |
| <a id="rn20"></a>RN20 | **Rateio — acerto da fatia.** O **dono** registra, por [EDP09](#edp09) ou pelo modal ([RT10](#rt10)), que um contato participante acertou a parte dele: grava `DEPU_IND_STATUS_PAGAMENTO = SIM` e `DEPU_DT_ACERTO` na linha `(DESP_ID, CONT_ID)`. Desfazer volta para `NAO` e limpa a data. Não altera o `DESP_IND_STATUS_PAGAMENTO` da despesa (que é sobre o pagamento ao credor, não sobre o acerto entre as pessoas). |
| <a id="rn21"></a>RN21 | **Baixa de pagamento.** [EDP07](#edp07) (individual) e [EDP08](#edp08) (lote) marcam `DESP_IND_STATUS_PAGAMENTO = SIM` e gravam `DESP_DT_PAGAMENTO` (data informada; no lote, a mesma para todas). Aplica-se a despesas com status `NAO` e a despesas de cartão (manuais ou importadas, inclusive com status legado `NAO_SE_APLICA`) — despesa já `SIM` é ignorada (idempotente). Numa série parcelada, a baixa é **por parcela** — não há "pagar a série toda" na v1.0. Espelha `pagarDespesa` / `pagarDespesas` da geração 1. |
| <a id="rn22"></a>RN22 | **Meio de pagamento — derivação.** O serviço deriva `DESP_MEIO_PAGAMENTO` da forma de pagamento: "Cartão de crédito" → `CREDITO`; "Dinheiro" (conta `CARTEIRA`) → `DINHEIRO`; "Conta à vista" → o valor informado no campo Meio de pagamento ([ID15](#qdd3-15)), *default* `DEBITO`, entre `DEBITO` / `PIX` / `BOLETO` / `TRANSFERENCIA`. Nas duas primeiras, qualquer valor divergente no corpo é ignorado. Se `DESP_MEIO_PAGAMENTO` deve permanecer derivado ou virar campo totalmente livre é item "A Confirmar" (Seção 17). |
| <a id="rn23"></a>RN23 | **Compra no cartão — fronteira com a fatura.** Quando `CACR_ID` está preenchido, o serviço fixa `DESP_MEIO_PAGAMENTO = CREDITO`. Para despesas manuais ou importadas no cartão, o status padrão inicial é `NAO` (Em aberto) ou `SIM` (caso o usuário informe que já pagou na inclusão/edição), permitindo o registro de pagamento (individual e em lote) normalmente ([RN21](#rn21), [RT11](#rt11)). Registros legados com `NAO_SE_APLICA` são tratados como pendentes e também podem receber baixa. O **vínculo com a fatura** (`DESP.FTCA_ID`), o fechamento e o cálculo do total da fatura são do documento `11 - manter-fatura-cartao` (Documento 0, Observação 15) — nesta tela `FTCA_ID` fica nulo. O pagamento da fatura em si é uma `TransacaoBancaria` com `TRBA_FL_PAGAMENTO_FATURA` (documento `10`, Documento 0, Observação 16). |
| <a id="rn24"></a>RN24 | **`DESP_FL_PAGAMENTO_FATURA` — caso de borda.** É o caminho alternativo de registrar o pagamento de uma fatura **como despesa** em vez de transação (Documento 0, Observação 16). A v1.0 **não** oferece esse lançamento pelo fluxo da tela; toda despesa criada aqui nasce com `DESP_FL_PAGAMENTO_FATURA = FALSE`. O tratamento dessas despesas no total por categoria é do documento `14 - dashboard-financeiro`. |
| <a id="rn25"></a>RN25 | **Despesa recorrente — geração, replicação e exclusão.** Quando `recorrente = true`, o endpoint de inserção ([EDP04](#edp04)) recebe a flag de recorrência e a quantidade de meses a projetar (`qtdMesesRecorrencia`, inteiro entre 2 e 36, *default* 12). (1) **Mútua exclusão:** `recorrente` e `parcelada` não podem ser simultaneamente verdadeiras — rejeita a requisição se ambas forem informadas. (2) **Valor integral:** diferente da parcelada (onde o total é dividido pelas parcelas), cada ocorrência da despesa recorrente recebe o valor integral informado (`DESP_VALOR`). (3) **Geração da série:** a primeira ocorrência é a despesa-mãe (`DESP_FL_RECORRENTE = TRUE`, `DESP_ID_RECORRENTE_PAI = NULL`), herdando o status de pagamento informado pelo usuário. As ocorrências 2..N são geradas com `DESP_FL_RECORRENTE = TRUE`, `DESP_ID_RECORRENTE_PAI = {id da mãe}`, `DESP_IND_STATUS_PAGAMENTO = NAO` (inclusive para compras de cartão de crédito), avançando 1 mês na competência e na data de vencimento a cada mês. (4) **Replicação de rateio:** caso haja rateio configurado, ele é replicado com os mesmos valores integrais em todas as N ocorrências. (5) **Exclusão:** a exclusão da despesa-mãe recorrente ([EDP06](#edp06)) exclui logicamente toda a série periódica e os respectivos rateios; a exclusão de uma ocorrência filha exclui apenas a ocorrência selecionada. |
| <a id="rn26"></a>RN26 | **Duplicação de despesas (individual e em lote) com suporte a parcelamento e recorrência.** O usuário pode duplicar uma ou mais despesas selecionadas para uma competência destino através de [EDP12](#edp12). Cada nova despesa criada nasce com: `DESP_ORIGEM = 'MANUAL'`, `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (inclusive se vinculada a cartão de crédito) e `DESP_DT_PAGAMENTO = NULL`. O endpoint suporta três modalidades de duplicação: (1) **Avulsa** (padrão): cria uma réplica pontual na competência destino com `parcelada = false` e `recorrente = false`; (2) **Parcelada**: define `qtdParcelas` (2..72), criando a parcela 1 na competência destino e as parcelas 2..N nos meses consecutivos com vínculo à parcela-mãe (`DESP_ID_PARCELA_PAI`), dividindo o montante ou replicando o valor base conforme informado e replicando os rateios proporcionalmente; (3) **Recorrente**: define `qtdMesesRecorrencia` (2..36), criando a despesa-mãe na competência destino e projetando as despesas filhas recorrentes nos meses consecutivos com vínculo `DESP_ID_RECORRENTE_PAI` e réplica integral dos rateios. Todas as despesas duplicadas devem pertencer ao usuário autenticado (validado por [C9](#c9)). |
| <a id="rn27"></a>RN27 | **Edição inline de valor (`PATCH /despesas/{id}/valor`).** O valor de uma despesa pode ser ajustado diretamente na célula da tabela do grid por usuário com `DESPESAS_EDITAR`. O endpoint [EDP13](#edp13) valida o escopo por usuário ([RN02](#rn02)) e valor estritamente maior que zero ([RN05](#rn05)). Caso a despesa pertença a uma série parcelada, a alteração afeta apenas o valor da parcela editada, mantendo as demais inalteradas ([RN13](#rn13)). A alteração gera nova revisão no Hibernate Envers ([RN10](#rn10)). |
| <a id="rn28"></a>RN28 | **Importação de fatura de cartão de crédito.** A importação de faturas e extratos de cartão de crédito ([EDP14](#edp14)) resgata os parsers da geração 1 (`dsc-backend`):<br>1. **Itaú:** leitura de planilha Excel (`.xls` e `.xlsx`), parseando colunas de Data, Lançamento/Descrição e Valor.<br>2. **Bradesco:** leitura de planilha Excel (`.xls` e `.xlsx`), parseando colunas de Data, Histórico e Valor.<br>3. **C6 Bank:** leitura de planilha Excel ou arquivo CSV, parseando colunas de Data, Descrição e Valor em BRL.<br>4. **OFX:** leitura de arquivos padronizados (`.ofx` e `.qfx`) via OFX4J, extraindo transações de cartão de crédito.<br>Cada transação importada gera uma despesa com `CACR_ID = :cartaoId`, `CTA_ID = NULL`, `DESP_ORIGEM = 'IMPORTACAO'`, `DESP_MEIO_PAGAMENTO = 'CREDITO'`, `DESP_IND_STATUS_PAGAMENTO = 'NAO'` (Em aberto, permitindo baixa de pagamento individual ou em lote pelo usuário), `DESP_COMPETENCIA = :competencia` informada e vencimento atribuído à fatura. Transações duplicadas no mesmo cartão, mesma competência, mesma data e mesmo valor são descartadas ou sinalizadas. |
| <a id="rn29"></a>RN29 | **Filtro padrão inicial e competências passadas.** A tela de listagem de despesas inicializa filtrando a competência `mes_atual - 1` (`YearMonth.now().minusMonths(1)`). É garantida a aceitação irrestrita de competências passadas, tanto nos filtros do grid quanto no cadastro e importação de despesas retroativas, sem bloqueios de tempo ou travas retroativas. |
| <a id="rn30"></a>RN30 | **Totalizador condicional por competência única e somatório de receita restante.** O bloco totalizador no topo do grid ([ID3a](#qdd1-3a)) é exibido exclusivamente quando o filtro ativo delimitar competência única (`competenciaInicio == competenciaFim`). Quando a consulta contemplar intervalo aberto ou competências distintas, o bloco deve permanecer oculto para evitar interpretações equivocadas de consolidação temporal. O bloco é composto por 4 cards com altura estritamente uniforme (`h-100`): (1) **Pago**, (2) **Pendente**, (3) **Total Geral** e (4) **Receita Restante / Saldo**. Os totalizadores de despesa consolidam a soma da **cota líquida do titular** (`valorUsuario`), deduzindo os valores transferidos a terceiros (`valorRateado`), de modo que despesas rateadas integralmente (cota zerada) não inflam os gastos pessoais do usuário. Havendo rateio no período, o totalizador geral informa o montante bruto e o total rateado. O 4º card calcula e exibe o saldo de receita que resta para o usuário no mês (`Receita Total da competência - Despesa Total cota do titular`), consultado via [EDP19](#edp19), informando a receita total do mês em subtítulo. |
| <a id="rn31"></a>RN31 | **Preservação de filtros e paginação no grid.** Ao concluir qualquer operação de inserção, edição, exclusão, duplicação, pagamento ou importação, o DataTables recarrega os dados preservando a página atual de paginação, ordenação e todos os filtros aplicados pelo usuário no momento da ação ([RT19](#rt19)). |
| <a id="rn32"></a>RN32 | **Edição inline de competência (`PATCH /despesas/{id}/competencia`).** A competência de uma despesa pode ser ajustada diretamente na célula da tabela do grid por usuário com `DESPESAS_EDITAR` ([RF23](#rf23), [RT20](#rt20)). O endpoint [EDP15](#edp15) valida o escopo por usuário ([RN02](#rn02)) e formato válido `yyyy-MM` ([RN04](#rn04)). Caso a despesa pertença a uma série parcelada ou recorrente, a alteração afeta apenas a competência do registro editado, mantendo os demais inalterados ([RN13](#rn13)). A alteração gera nova revisão no Hibernate Envers ([RN10](#rn10)). |
| <a id="rn33"></a>RN33 | **Edição inline de categoria (`PATCH /despesas/{id}/categoria`).** A categoria de uma despesa pode ser ajustada diretamente na célula da tabela do grid por usuário com `DESPESAS_EDITAR` ([RF23](#rf23), [RT23](#rt23)). O endpoint [EDP16](#edp16) valida o escopo por usuário ([RN02](#rn02)). Se informada, a categoria deve existir, estar ativa e possuir `aplicaA = DESPESA` ou `AMBOS` ([C5](#c5)); caso contrário, retorna 422 com [MSG29](#msg29). Se `categoriaId` for nulo ou vazio, a categoria é desvinculada (`CATE_ID = NULL`). Caso a despesa pertença a uma série parcelada ou recorrente, a alteração afeta apenas a categoria do registro editado, mantendo os demais inalterados ([RN13](#rn13)). A alteração gera nova revisão no Hibernate Envers ([RN10](#rn10)). |
| <a id="rn34"></a>RN34 | **Edição inline de forma e meio de pagamento (`PATCH /despesas/{id}/forma-pagamento`).** A forma e o meio de pagamento de uma despesa podem ser alterados diretamente pela célula do grid por usuário com `DESPESAS_EDITAR` ([RF25](#rf25), [RT24](#rt24)). O endpoint [EDP17](#edp17) valida: (1) escopo por usuário e existência da despesa ([RN02](#rn02)); (2) despesa não pode ter origem `OPEN_FINANCE` (recusa com [MSG16b](#msg16b)); (3) se forma for `CARTAO`, exige `cartaoId` de cartão de crédito ativo do usuário autenticado ([C4](#c4)), atribuindo o cartão, `meioPagamento = CREDITO`, `statusPagamento` ajustado para `NAO` se antes era `NAO_SE_APLICA` ou nulo (mantendo `SIM`/`NAO` caso existentes), e `conta = null`; (4) se forma for `CONTA`, exige `contaId` de conta bancária ativa do usuário ([C3](#c3)), atribuindo a conta, `cartao = null` e meio de pagamento informado (padrão `DEBITO` caso nulo), com `statusPagamento` ajustado para `NAO` se antes era `NAO_SE_APLICA`; (5) se forma for `DINHEIRO`, exige `contaId` de conta carteira ativa do usuário, atribuindo `meioPagamento = DINHEIRO`, `cartao = null` e `statusPagamento` ajustado para `NAO` se antes era `NAO_SE_APLICA`. A alteração gera nova revisão no Hibernate Envers ([RN10](#rn10)) e retorna os dados atualizados acompanhados de [MSG30](#msg30). |
| <a id="rn35"></a>RN35 | **Edição inline de nome (`PATCH /despesas/{id}/nome`).** O nome de uma despesa pode ser ajustado diretamente na célula da tabela do grid por usuário com `DESPESAS_EDITAR` ([RF26](#rf26), [RT25](#rt25)). O endpoint [EDP18](#edp18) valida o escopo por usuário ([RN02](#rn02)) e obrigatoriedade de preenchimento do nome (não nulo e não em branco, tamanho máximo de 100 caracteres; caso contrário, retorna 422 com [MSG32](#msg32)). Caso a despesa pertença a uma série parcelada ou recorrente, a alteração afeta apenas o registro editado, mantendo os demais inalterados ([RN13](#rn13)). A alteração gera nova revisão no Hibernate Envers ([RN10](#rn10)) e retorna o identificador e o nome atualizado acompanhados de [MSG31](#msg31). |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Despesa cadastrada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Informe um valor maior que zero. |
| <a id="msg04"></a>MSG04 | Despesa atualizada com sucesso. |
| <a id="msg05"></a>MSG05 | Despesa não encontrada. |
| <a id="msg06"></a>MSG06 | Confirma a exclusão da despesa "{nome}"? |
| <a id="msg07"></a>MSG07 | Despesa excluída com sucesso. |
| <a id="msg08"></a>MSG08 | Nenhuma despesa encontrada com os filtros informados. |
| <a id="msg09"></a>MSG09 | Informe a data de pagamento para marcar a despesa como paga. |
| <a id="msg10"></a>MSG10 | Pagamento registrado com sucesso. |
| <a id="msg10b"></a>MSG10b | Pagamento de {n} despesas registrado com sucesso. |
| <a id="msg11"></a>MSG11 | A competência deve estar no formato AAAA-MM. |
| <a id="msg12"></a>MSG12 | A {conta/cartão} selecionado não está disponível. Escolha uma opção ativa. |
| <a id="msg12b"></a>MSG12b | Escolha uma forma de pagamento: a despesa deve ter uma conta ou um cartão, não os dois. |
| <a id="msg13"></a>MSG13 | O número de parcelas deve ser um inteiro entre 2 e {máximo}. |
| <a id="msg14"></a>MSG14 | Compra parcelada cadastrada: {n} parcelas geradas. |
| <a id="msg15"></a>MSG15 | Esta é a primeira parcela de uma compra em {qtd}x. Excluir vai apagar todas as {qtd} parcelas. Confirma? |
| <a id="msg16"></a>MSG16 | Dividir despesas entre usuários é um recurso do seu plano. Faça o upgrade para habilitar. |
| <a id="msg16b"></a>MSG16b | Esta despesa foi importada do Open Finance e não pode ser excluída por esta tela. |
| <a id="msg17"></a>MSG17 | A soma das fatias ({soma}) não pode ser maior que o valor da despesa ({valor}). |
| <a id="msg18"></a>MSG18 | Você ainda não tem uma conta do tipo Carteira. Crie uma em "Minhas Contas" para lançar despesas em dinheiro. |
| <a id="msg19"></a>MSG19 | Um dos contatos do rateio não foi encontrado, não pertence à sua agenda ou não está ativo. |
| <a id="msg19b"></a>MSG19b | Contato cadastrado com sucesso. |
| <a id="msg20"></a>MSG20 | Esta despesa é uma compra no cartão — o pagamento é controlado pela fatura, não aqui. |
| <a id="msg21"></a>MSG21 | Acerto da fatia registrado com sucesso. |
| <a id="msg22"></a>MSG22 | Despesa(s) duplicada(s) com sucesso. |
| <a id="msg23"></a>MSG23 | Fatura importada com sucesso: {n} despesas criadas. |
| <a id="msg24"></a>MSG24 | Arquivo de fatura inválido ou formato não suportado. |
| <a id="msg25"></a>MSG25 | Valor da despesa atualizado com sucesso. |
| <a id="msg26"></a>MSG26 | Selecione ao menos uma despesa para duplicar. |
| <a id="msg27"></a>MSG27 | Competência da despesa atualizada com sucesso. |
| <a id="msg28"></a>MSG28 | Categoria da despesa atualizada com sucesso. |
| <a id="msg29"></a>MSG29 | A categoria selecionada não está disponível. |
| <a id="msg30"></a>MSG30 | Forma de pagamento da despesa atualizada com sucesso. |
| <a id="msg31"></a>MSG31 | Nome da despesa atualizado com sucesso. |
| <a id="msg32"></a>MSG32 | O nome da despesa é obrigatório. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem das despesas do usuário autenticado para o grid ([EDP02](#edp02)), com conta ou cartão, categoria e o resumo do rateio.<br>`SELECT d.DESP_ID, d.DESP_COMPETENCIA, d.DESP_NOME, d.DESP_DESCRICAO, d.DESP_VALOR,`<br>`       d.DESP_VALOR_TOTAL_COMPRA, d.DESP_FL_PARCELADA, d.DESP_NRO_PARCELA, d.DESP_QTD_PARCELAS,`<br>`       d.DESP_ID_PARCELA_PAI, d.DESP_MEIO_PAGAMENTO, d.DESP_IND_STATUS_PAGAMENTO,`<br>`       d.DESP_DT_LANCAMENTO, d.DESP_DT_VENCIMENTO, d.DESP_DT_PAGAMENTO, d.DESP_ORIGEM,`<br>`       c.CTA_ID, c.CTA_DESCRICAO, cc.CACR_ID, cc.CACR_DESCRICAO,`<br>`       cat.CATE_ID, cat.CATE_NOME,`<br>`       (SELECT COUNT(*) FROM DESPESAS_USUARIO du WHERE du.DESP_ID = d.DESP_ID AND du.audit_data_exclusao IS NULL) AS qtd_co_participantes,`<br>`       (d.audit_data_exclusao IS NOT NULL) AS excluido`<br>`FROM DESPESAS d`<br>`LEFT JOIN CONTAS c            ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc  ON cc.CACR_ID = d.CACR_ID`<br>`LEFT JOIN CATEGORIAS cat      ON cat.CATE_ID = d.CATE_ID`<br>`WHERE (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.audit_data_exclusao IS NULL`<br>`ORDER BY d.DESP_COMPETENCIA DESC, d.DESP_DT_VENCIMENTO ASC;` |
| <a id="c2"></a>C2 | Verifica se a despesa pertence ao usuário autenticado ([RN02](#rn02)) — usada antes de editar, pagar, ratear, acertar ou excluir.<br>`SELECT COUNT(*) FROM DESPESAS d`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`WHERE d.DESP_ID = :despId`<br>`  AND (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.audit_data_exclusao IS NULL;` |
| <a id="c3"></a>C3 | Verifica se a conta informada existe, está ativa e é do usuário autenticado ([RN03](#rn03)).<br>`SELECT COUNT(*) FROM CONTAS c`<br>`WHERE c.CTA_ID = :ctaId AND c.USU_ID = :usuId`<br>`  AND c.CTA_FL_ATIVO = TRUE AND c.audit_data_exclusao IS NULL;` |
| <a id="c4"></a>C4 | Verifica se o cartão informado existe, está ativo e é do usuário autenticado ([RN03](#rn03)).<br>`SELECT COUNT(*) FROM CARTOES_CREDITO cc`<br>`WHERE cc.CACR_ID = :cacrId AND cc.USU_ID = :usuId`<br>`  AND cc.CACR_FL_ATIVO = TRUE AND cc.audit_data_exclusao IS NULL;` |
| <a id="c5"></a>C5 | Verifica se a categoria informada está ativa e se aplica a despesa.<br>`SELECT COUNT(*) FROM CATEGORIAS cat`<br>`WHERE cat.CATE_ID = :cateId AND cat.CATE_FL_ATIVO = TRUE`<br>`  AND cat.CATE_APLICA_A IN ('DESPESA', 'AMBOS')`<br>`  AND cat.audit_data_exclusao IS NULL;` |
| <a id="c6"></a>C6 | Busca todas as parcelas de uma série a partir da despesa-mãe ([RN14](#rn14)).<br>`SELECT d.DESP_ID FROM DESPESAS d`<br>`WHERE (d.DESP_ID = :maeId OR d.DESP_ID_PARCELA_PAI = :maeId)`<br>`  AND d.audit_data_exclusao IS NULL;` |
| <a id="c7"></a>C7 | Busca o rateio de uma despesa ([EDP03](#edp03)).<br>`SELECT du.CONT_ID, ct.CONT_NOME, ct.CONT_EMAIL, ct.CONT_TIPO, ct.CONT_CHAVE_PIX, du.DEPU_VALOR, du.DEPU_IND_STATUS_PAGAMENTO, du.DEPU_DT_ACERTO`<br>`FROM DESPESAS_USUARIO du`<br>`JOIN CONTATOS ct ON ct.CONT_ID = du.CONT_ID`<br>`WHERE du.DESP_ID = :despId AND du.audit_data_exclusao IS NULL`<br>`ORDER BY ct.CONT_NOME;` |
| <a id="c8"></a>C8 | Verifica se os contatos informados no rateio existem, pertencem à agenda do usuário autenticado e estão ativos ([RN16](#rn16)).<br>`SELECT COUNT(*) FROM CONTATOS ct`<br>`WHERE ct.CONT_ID IN (:contIds) AND ct.USU_ID_DONO = :usuIdLogado`<br>`  AND ct.CONT_STATUS = 'ATIVO' AND ct.audit_data_exclusao IS NULL;` |
| <a id="c9"></a>C9 | Verifica se todas as despesas de um lote de baixa ou duplicação são do usuário autenticado ([EDP08](#edp08), [EDP12](#edp12), [RN26](#rn26)).<br>`SELECT COUNT(*) FROM DESPESAS d`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`WHERE d.DESP_ID IN (:despIds)`<br>`  AND (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.audit_data_exclusao IS NULL;` |
| <a id="c10"></a>C10 | Busca contatos ativos da agenda do usuário autenticado para o rateio, por parte do nome, e-mail ou telefone ([EDP10](#edp10), [RN19](#rn19)).<br>`SELECT ct.CONT_ID, ct.CONT_NOME, ct.CONT_EMAIL, ct.CONT_TELEFONE, ct.CONT_TIPO, ct.CONT_CHAVE_PIX FROM CONTATOS ct`<br>`WHERE ct.USU_ID_DONO = :usuIdLogado AND ct.CONT_STATUS = 'ATIVO' AND ct.audit_data_exclusao IS NULL`<br>`  AND (ct.CONT_NOME LIKE :termo OR ct.CONT_EMAIL LIKE :termo OR ct.CONT_TELEFONE LIKE :termo)`<br>`ORDER BY ct.CONT_NOME LIMIT 20;` |
| <a id="c11"></a>C11 | Totaliza despesas de uma competência única ([RN30](#rn30), [EDP02](#edp02)).<br>`SELECT COALESCE(SUM(d.DESP_VALOR), 0) FROM DESPESAS d`<br>`LEFT JOIN CONTAS c           ON c.CTA_ID  = d.CTA_ID`<br>`LEFT JOIN CARTOES_CREDITO cc ON cc.CACR_ID = d.CACR_ID`<br>`WHERE (c.USU_ID = :usuId OR cc.USU_ID = :usuId)`<br>`  AND d.DESP_COMPETENCIA = :competencia`<br>`  AND d.audit_data_exclusao IS NULL;` |

> Os nomes de coluna de `CONTATOS` (`CONT_NOME`, `CONT_EMAIL`, `CONT_TELEFONE`, `CONT_TIPO`, `CONT_STATUS`, `CONT_CHAVE_PIX`) seguem o [QUADRO_DESCRITIVO_29 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-29).

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| DESPESA_COMPETENCIA_SEGUE_DATA_LANCAMENTO | true | Se `true`, a Competência do modal ([ID8](#qdd3-8)) é pré-preenchida e reajustada pelo mês da Data de lançamento enquanto o usuário não a editar à mão ([RT13](#rt13)). Se `false`, começa no mês corrente e nunca é reajustada. |
| DESPESA_PARCELAS_MAXIMO | 72 | Número máximo de parcelas aceito por [EDP04](#edp04) ([RN12](#rn12) / [MSG13](#msg13)). |
| DESPESA_RESIDUO_PARCELA_NA_ULTIMA | true | Se `true`, o resíduo do arredondamento da divisão do valor total vai na **última** parcela; se `false`, na primeira ([RN12](#rn12)). |
| DESPESA_COMPRA_CARTAO_STATUS_NAO_SE_APLICA | false | Se `true`, a compra no cartão (`CACR_ID` preenchido) nasce com `DESP_IND_STATUS_PAGAMENTO = NAO_SE_APLICA`. Se `false` (padrão v2.3), nasce `NAO` (ou `SIM` se informado pagamento no cadastro) ([RN23](#rn23)). |
| DESPESA_RATEIO_SOMA_DEVE_FECHAR | false | Se `true`, a soma das fatias do rateio deve ser **exatamente** igual ao valor da despesa; se `false`, pode ser menor (o resto é a cota do dono — [RN17](#rn17)). |

---

## 13. Permissões

Sete permissões relacionadas ao módulo **Despesas** (`PERM_MODULO = 'Despesas'`). Fazem parte do catálogo do código e da carga inicial (Documento 0, [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26) e Observação 28). Convenção domínio-primeiro; cada uma vira a autoridade `PERM_{CÓDIGO}`. É **terminantemente proibido** o uso de permissão agregadora com sufixo `MANTER` (`DESPESAS_MANTER`).

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `DESPESAS_LISTAR` — abrir a tela Despesas, listar e filtrar as próprias despesas. Controla a visibilidade do menu 'Despesas'. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm02"></a>PERM02 | `DESPESAS_INSERIR` — cadastrar novas despesas à vista, parceladas, recorrentes e duplicar despesas existentes. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm03"></a>PERM03 | `DESPESAS_EDITAR` — editar dados cadastrais de despesas existentes e atualizar valor inline no grid. | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm04"></a>PERM04 | `DESPESAS_EXCLUIR` — excluir logicamente despesas manuais do próprio usuário (individuais, parceladas ou séries recorrentes). | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm05"></a>PERM05 | `DESPESAS_PAGAR` — registrar e reverter o pagamento de despesas em aberto (individual ou em lote). | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm06"></a>PERM06 | `DESPESAS_IMPORTAR` — importar faturas e extratos de cartão de crédito (Excel Itaú, Bradesco, C6 Bank e arquivos OFX). | [PERF01](#perf01), [PERF02](#perf02) |
| <a id="perm07"></a>PERM07 | `DESPESA_RATEAR_MULTIUSUARIO` — dividir uma despesa com contatos da agenda privada (adicionar co-participantes externos ou usuários conectados do sistema, definir fatias, registrar acertos, buscar contatos para o rateio). `PERM_FL_CONCEDIVEL_POR_PLANO = FALSE` (v2.4) — vinculada aos perfis `ADMIN` e `USER` na carga inicial, permitindo o uso nativo do rateio de despesas e agenda de contatos sem bloqueio por plano pago. | [PERF01](#perf01), [PERF02](#perf02) |

> Todas as sete permissões do módulo vêm da carga inicial e são concedidas a **ADMIN e USER** — a tela é do próprio usuário. O que cada usuário enxerga e altera é limitado às suas despesas pela [RN02](#rn02), não pela permissão. A permissão `DESPESA_RATEAR_MULTIUSUARIO` é habilitada por padrão para ambos os perfis a partir da v2.4.

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER | Concedível por plano |
|---|:-:|:-:|:-:|
| `DESPESAS_LISTAR` | ✓ | ✓ | · |
| `DESPESAS_INSERIR` | ✓ | ✓ | · |
| `DESPESAS_EDITAR` | ✓ | ✓ | · |
| `DESPESAS_EXCLUIR` | ✓ | ✓ | · |
| `DESPESAS_PAGAR` | ✓ | ✓ | · |
| `DESPESAS_IMPORTAR` | ✓ | ✓ | · |
| `DESPESA_RATEAR_MULTIUSUARIO` | ✓ | ✓ | · |

`ADMIN` e `USER` recebem as sete permissões operacionais na carga inicial: cada usuário gerencia as suas próprias despesas. O `ADMIN` **não** tem visão administrativa das despesas de outros usuários — dado financeiro é privado do dono, e o escopo por `USU_ID` da [RN02](#rn02) vale igual para os dois perfis. A partir da versão 2.4, `DESPESA_RATEAR_MULTIUSUARIO` é vinculada na inicialização do sistema para `ADMIN` e `USER`, disponibilizando o rateio e a agenda de contatos para todos os usuários cadastrados.

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. `PERF_FL_SISTEMA = TRUE`. Recebe todas as permissões na carga inicial, inclusive as seis atômicas de Despesas (Seção 13.1); **não** recebe `DESPESA_RATEAR_MULTIUSUARIO` por perfil. Opera apenas sobre as próprias despesas ([RN02](#rn02)). Corresponde a `ROLE_ADMIN`. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum. `PERF_FL_SISTEMA = TRUE`. Recebe `DESPESAS_LISTAR`, `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR` e `DESPESAS_IMPORTAR` na carga inicial e gerencia as próprias despesas. `DESPESA_RATEAR_MULTIUSUARIO` só pela permissão efetiva do plano. Corresponde a `ROLE_USER`. |

---

## 15. Fluxo de Eventos

**Cadastrar uma compra parcelada em 6x:**

```
1. Usuário clica em "Nova despesa" → modal QUADRO_DESCRITIVO_3 em modo criação.
        Data de lançamento ← hoje    Competência ← mês corrente
        Forma de pagamento ← "Cartão de crédito".
2. Usuário informa nome "Notebook", seleciona o cartão, liga "Parcelada",
   informa Número de parcelas = 6 e Valor total da compra = 3.000,00.
        A tela mostra "Valor da parcela: 500,00".
3. Usuário clica em "Salvar" → EDP04.
        │
        ├─ Cartão inativo / de outro usuário (RN03 / C4)     → MSG12.
        ├─ Número de parcelas < 2 ou > 72 (RN12)             → MSG13.
        ├─ Competência fora do formato (RN04)                → MSG11.
        └─ OK →
             • grava a despesa-mãe: parcela 1/6, competência atual,
               DESP_VALOR = 500,00, DESP_VALOR_TOTAL_COMPRA = 3.000,00,
               DESP_ID_PARCELA_PAI = NULL, meio CREDITO, status NAO;
             • gera as parcelas 2..6, DESP_ID_PARCELA_PAI = id da mãe,
               competência e vencimento +1 mês por parcela;
             • parcela 6 recebe DESP_VALOR = 3.000,00 − 500,00 × 5 = 500,00
               (aqui divide exato; com 3.001,00 a última seria 501,00);
             • fixa DESP_ORIGEM = MANUAL, audita (Envers), retorna MSG14
               ("6 parcelas geradas"), recarrega o grid com as 6 linhas.
```

**Ratear uma despesa 50/50 e registrar o acerto do contato:**

```
1. Usuário com DESPESA_RATEAR_MULTIUSUARIO edita a despesa "Jantar" (R$ 200,00)
   → modal, seção Rateio visível (RT10).
2. Usuário adiciona o contato "Maria" (SB06 → EDP10, ou cria via "+ Novo Contato Externo" → EDP11), fatia 100,00.
        A tela mostra "Minha cota: 100,00".
3. Usuário clica em "Salvar" → EDP05.
        │
        ├─ Sem a permissão efetiva de rateio (RN15)          → MSG16.
        ├─ Maria inativa ou não pertence à agenda (C8)       → MSG19.
        ├─ Soma das fatias 100,00 > valor 200,00? não        → segue.
        │  (se fosse 250,00 > 200,00 → MSG17)
        └─ OK → cria DESPESAS_USUARIO (DESP_ID, CONT_ID de Maria),
                 DEPU_VALOR = 100,00, DEPU_IND_STATUS_PAGAMENTO = NAO,
                 audita, retorna MSG04, recarrega o grid (ícone de rateio na linha).
4. Maria transfere os 100,00. Usuário abre a despesa (ícone "Ratear"),
   marca a fatia de Maria como acertada, data de hoje → EDP09.
        └─ OK → grava DEPU_IND_STATUS_PAGAMENTO = SIM e DEPU_DT_ACERTO,
                 audita, retorna MSG21.
   (Maria não vê essa despesa em lugar nenhum na v1.0 — RN19.)
```

**Registrar pagamento de várias despesas em lote:**

```
1. Usuário marca 4 despesas "Em aberto" no grid (ID8) → habilita o botão da barra (ID6).
2. Usuário clica em "Registrar pagamento" → modal QUADRO_DESCRITIVO_5,
   resumo "4 despesas — total 780,00", data ← hoje.
3. Usuário confirma → EDP08.
        │
        ├─ Alguma despesa do lote é de outro usuário (C9)    → MSG05 (404), nada é baixado.
        ├─ Despesas com status NAO ou NAO_SE_APLICA legado   → baixadas para SIM com dataPagamento informada (RN21).
        └─ OK → grava DESP_IND_STATUS_PAGAMENTO = SIM e DESP_DT_PAGAMENTO
                 nas despesas elegíveis, audita, retorna MSG10b, recarrega o grid.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Listar minhas despesas

Dado que estou autenticado com um usuário que tem a permissão [PERM01](#perm01).
E que tenho oito despesas vinculadas às minhas contas e aos meus cartões.
Quando eu acessar o menu "Finanças > Despesas".
Então o sistema deve exibir o grid com as minhas oito despesas, ordenadas por competência decrescente, mostrando a categoria, a conta ou o cartão, o valor, as datas, o status de pagamento e a origem.

### 16.1 Bloquear acesso de usuário sem a permissão

Dado que estou autenticado com um usuário de um perfil que não tem [PERM01](#perm01).
Quando eu tentar acessar "/despesas/listar" ou chamar "/despesas/listar-dados".
Então o sistema deve negar o acesso (HTTP 403).

### 16.2 A listagem traz só as despesas do próprio usuário

Dado que o usuário A tem cinco despesas e o usuário B tem três despesas.
Quando o usuário A abrir a tela Despesas.
Então o grid deve mostrar apenas as cinco despesas do usuário A.

### 16.3 Co-participante do rateio não vê a despesa

Dado que o usuário A criou a despesa "Jantar" e adicionou o contato "Maria" (mesmo sendo contato do tipo SISTEMA vinculado a outro usuário) ao rateio.
Quando o usuário daquele contato abrir a tela Despesas.
Então o grid desse outro usuário não deve mostrar a despesa "Jantar" ([RN19](#rn19)).

### 16.4 Acesso cruzado a buscar/editar/pagar/excluir é negado

Dado que estou autenticado como usuário A.
E que a despesa com id 90 é do usuário B.
Quando eu chamar "/despesas/buscar/90", "/despesas/editar/90", "/despesas/registrar-pagamento/90" ou "/despesas/excluir/90".
Então o sistema deve responder 404 com [MSG05](#msg05) e não alterar a despesa do usuário B.

### 16.5 Cadastrar despesa à vista em conta

Dado que estou na tela Despesas com [PERM01](#perm01) e [PERM02](#perm02) e clico em "Nova despesa".
Quando eu informar o nome "Mercado", o valor "250,00", a data de lançamento "05/09/2026", a forma de pagamento "Conta à vista", a conta "Nubank Conta Corrente", o meio "Débito", deixar "Já paguei" em "Sim" com data "05/09/2026" e clicar em "Salvar".
Então o sistema deve criar a despesa com `CTA_ID` da conta, `CACR_ID` nulo, `DESP_MEIO_PAGAMENTO = DEBITO`, `DESP_IND_STATUS_PAGAMENTO = SIM`, `DESP_ORIGEM = MANUAL`, competência "2026-09", exibir [MSG01](#msg01) e recarregar o grid.

### 16.6 Cadastrar despesa em dinheiro usa conta CARTEIRA

Dado que eu tenho uma conta do tipo `CARTEIRA` chamada "Carteira".
Quando eu cadastrar uma despesa com a forma de pagamento "Dinheiro", escolher a conta "Carteira" e salvar.
Então o sistema deve gravar `CTA_ID` da conta Carteira, `DESP_MEIO_PAGAMENTO = DINHEIRO` e `CACR_ID` nulo.

### 16.7 Cadastrar despesa no cartão nasce em aberto e permite pagamento

Dado que estou cadastrando uma despesa.
Quando eu escolher a forma de pagamento "Cartão de crédito", selecionar o cartão "Nubank Ultravioleta" e salvar.
Então o sistema deve gravar `CACR_ID` do cartão, `CTA_ID` nulo, `DESP_MEIO_PAGAMENTO = CREDITO` e `DESP_IND_STATUS_PAGAMENTO = NAO`, e a ação "Registrar pagamento" deve estar disponível para essa linha.

### 16.8 Conta e cartão juntos são recusados

Dado que estou chamando "/despesas/inserir" diretamente.
Quando eu enviar `contaId` e `cartaoId` preenchidos ao mesmo tempo (ou os dois nulos).
Então o sistema deve recusar com [MSG12b](#msg12b).

### 16.9 Valor deve ser maior que zero

Dado que estou cadastrando uma despesa.
Quando eu informar o valor "0,00" e clicar em "Salvar".
Então o sistema deve impedir e exibir [MSG03](#msg03).

### 16.10 Competência pré-preenchida pela data de lançamento

Dado que o parâmetro `DESPESA_COMPETENCIA_SEGUE_DATA_LANCAMENTO` está em "true" e abri o modal de nova despesa.
E que ainda não editei o campo Competência.
Quando eu informar a data de lançamento "10/12/2026".
Então o campo Competência deve passar a "12/2026".

### 16.11 Competência diferente do mês da data de lançamento

Dado que cadastrei uma despesa com data de lançamento "28/01/2026".
Quando eu ajustar a Competência para "02/2026" e salvar.
Então o sistema deve gravar `DESP_COMPETENCIA = '2026-02'` e `DESP_DT_LANCAMENTO = 2026-01-28`, sem forçar a igualdade.

### 16.12 Lançar despesa prevista com data futura

Dado que hoje é 08/09/2026 e estou cadastrando uma despesa.
Quando eu informar a data de vencimento "10/10/2026", deixar "Já paguei" em "Não" e salvar.
Então o sistema deve aceitar o cadastro com a data futura e status de pagamento "Em aberto".

### 16.13 Criar compra parcelada em 6x

Dado que estou cadastrando a despesa "Notebook" no cartão.
Quando eu ligar "Parcelada", informar 6 parcelas, valor total "3.000,00" e salvar.
Então o sistema deve criar 6 despesas: a 1ª como despesa-mãe (`DESP_NRO_PARCELA = 1`, `DESP_ID_PARCELA_PAI` nula) e as demais apontando para ela, todas com `DESP_VALOR = 500,00` e `DESP_VALOR_TOTAL_COMPRA = 3.000,00`, competências de 6 meses consecutivos a partir da competência informada, e exibir [MSG14](#msg14).

### 16.14 Resíduo do arredondamento vai na última parcela

Dado que estou criando uma compra parcelada de valor total "100,00" em 3 parcelas, com `DESPESA_RESIDUO_PARCELA_NA_ULTIMA` em "true".
Quando o sistema gerar a série.
Então as parcelas 1 e 2 devem ter `DESP_VALOR = 33,33` e a parcela 3 deve ter `DESP_VALOR = 33,34`, somando exatamente 100,00.

### 16.15 Editar uma parcela afeta só ela

Dado que tenho uma compra em 6x e a parcela 3/6 tem valor "500,00".
Quando eu editar a parcela 3/6, mudar o valor para "480,00" e salvar.
Então o sistema deve alterar apenas a parcela 3/6, mantendo as outras 5 parcelas e o `DESP_VALOR_TOTAL_COMPRA` inalterados.

### 16.16 Excluir a série pela despesa-mãe

Dado que tenho uma compra em 6x.
Quando eu clicar em excluir na parcela 1/6 (a mãe) e confirmar [MSG15](#msg15).
Então o sistema deve fazer a exclusão lógica das 6 parcelas e das `DESPESAS_USUARIO` de cada uma, e exibir [MSG07](#msg07).

### 16.17 Excluir uma parcela do meio remove só ela

Dado que tenho uma compra em 6x.
Quando eu excluir a parcela 4/6 e confirmar [MSG06](#msg06).
Então o sistema deve excluir logicamente apenas a parcela 4/6, deixando a série com 5 parcelas.

### 16.18 Registrar pagamento individual

Dado que a minha despesa "Boleto de luz" está "Em aberto".
Quando eu clicar no ícone "Registrar pagamento", confirmar a data "12/09/2026" e clicar em "Registrar".
Então o sistema deve gravar `DESP_IND_STATUS_PAGAMENTO = SIM` e `DESP_DT_PAGAMENTO = 2026-09-12`, exibir [MSG10](#msg10) e recarregar o grid com o status "Pago".

### 16.19 Registrar pagamento em lote

Dado que tenho quatro despesas "Em aberto" selecionadas no grid.
Quando eu clicar em "Registrar pagamento" na barra, confirmar a data "12/09/2026" e confirmar.
Então o sistema deve marcar as quatro como pagas com a mesma data e exibir [MSG10b](#msg10b).

### 16.20 Registrar pagamento de despesa no cartão
 
Dado que a minha despesa "Compra no cartão" está em aberto ou possui status legado.
Quando eu clicar no ícone "Registrar pagamento" ou chamar "/despesas/registrar-pagamento/{id}" diretamente.
Então o sistema deve gravar `DESP_IND_STATUS_PAGAMENTO = SIM` e `DESP_DT_PAGAMENTO` com a data informada.

### 16.21 Ratear uma despesa 50/50

Dado que estou editando a despesa "Jantar" (R$ 200,00) e tenho a permissão [PERM07](#perm07).
Quando eu adicionar o contato "Maria" com a fatia "100,00" e salvar.
Então o sistema deve criar uma `DESPESAS_USUARIO` com `CONT_ID` do contato, `DEPU_VALOR = 100,00` e `DEPU_IND_STATUS_PAGAMENTO = NAO`, e o grid deve mostrar o ícone de rateio na linha.

### 16.22 A soma das fatias não pode exceder o valor

Dado que estou rateando a despesa "Jantar" (R$ 200,00).
Quando eu informar uma fatia de "250,00" para um co-participante e salvar.
Então o sistema deve impedir e exibir [MSG17](#msg17).

### 16.23 Marcar a fatia do co-participante como acertada

Dado que a despesa "Jantar" tem a fatia do contato "Maria" com `DEPU_IND_STATUS_PAGAMENTO = NAO`.
Quando eu, como dono, marcar a fatia de Maria como acertada com data "10/09/2026" (via [EDP09](#edp09) informando `contatoId`).
Então o sistema deve gravar `DEPU_IND_STATUS_PAGAMENTO = SIM` e `DEPU_DT_ACERTO = 2026-09-10`, e exibir [MSG21](#msg21).

### 16.24 Rateio replicado nas parcelas

Dado que estou criando uma compra parcelada em 3x de valor total "300,00" e adiciono o contato "Maria" com metade da despesa.
Quando o sistema gerar a série.
Então cada uma das 3 parcelas (valor 100,00) deve ter uma `DESPESAS_USUARIO` do contato Maria com `DEPU_VALOR = 50,00`.

### 16.25 Rateio indisponível sem a permissão de plano

Dado que estou autenticado com um usuário que **não** tem a permissão efetiva [PERM07](#perm07).
Quando eu abrir o modal de despesa.
Então a seção de rateio não deve aparecer, o ícone "Ratear" do grid não deve aparecer, e se eu enviar `rateio[]` no corpo de "/despesas/inserir", o sistema deve recusar com [MSG16](#msg16).

### 16.26 Despesa do Open Finance: edição parcial, sem exclusão

Dado que a minha despesa "Compra PIX" tem `DESP_ORIGEM = OPEN_FINANCE`.
Quando eu abri-la em edição.
Então os campos Forma de pagamento e Origem devem estar desabilitados e o ícone de excluir não deve aparecer.
E se eu alterar a categoria e o nome e salvar, o sistema deve gravar os novos valores mantendo a forma de pagamento e a origem.
E se a exclusão for chamada diretamente, o sistema deve responder [MSG16b](#msg16b).

### 16.27 ADMIN também só vê as próprias despesas

Dado que estou autenticado como ADMIN.
E que existem despesas de outros usuários.
Quando eu abrir a tela Despesas.
Então o grid deve mostrar apenas as minhas despesas.

### 16.28 Auditoria da despesa

Dado que eu registro o pagamento de uma despesa e salvo.
Quando eu consultar a auditoria de `DESPESAS`.
Então deve haver o registro de quem alterou e quando, com o valor anterior e o novo de `DESP_IND_STATUS_PAGAMENTO` e `DESP_DT_PAGAMENTO`.

### 16.29 Filtrar por competência e status de pagamento

Dado que tenho despesas nas competências de 07/2026 a 10/2026, em aberto e pagas.
Quando eu filtrar por competência inicial "08/2026", competência final "09/2026" e status "Em aberto".
Então o grid deve mostrar apenas as despesas em aberto com competência entre 08/2026 e 09/2026.

### 16.30 Cadastro rápido de contato extra-sistema no modal de rateio

Dado que estou editando o rateio de uma despesa no modal e a pessoa não está na minha agenda de contatos.
Quando eu clicar em "+ Novo Contato Externo", preencher Nome "João da Silva", Chave Pix "joao@pix.com" e salvar via [EDP11](#edp11).
Então o sistema deve cadastrar o contato em `CONTATOS` com `CONT_TIPO = 'EXTERNO'`, `CONT_STATUS = 'ATIVO'`, exibir [MSG19b](#msg19b) e inseri-lo como linha na tabela de rateio da despesa.

### 16.31 Busca de rateio restrita exclusivamente à agenda privada do usuário

Dado que existem outros usuários cadastrados na plataforma que não fazem parte da minha agenda de contatos.
Quando eu buscar por um termo no campo de rateio ([EDP10](#edp10)).
Então o sistema deve retornar somente contatos da minha própria agenda (`CONTATOS.USU_ID_DONO`), garantindo a privacidade e sem expor a base global de usuários do sistema ([RN19](#rn19)).

### 16.32 Criação e exclusão de despesa recorrente

Dado que estou autenticado e preencho uma despesa fixa "Aluguel" no valor de R$ 1.500,00 com competência "09/2026".
Quando eu marcar a opção "Despesa recorrente", informar 12 meses e salvar.
Então o sistema deve criar 12 despesas mensais consecutivas de R$ 1.500,00 cada (de 09/2026 a 08/2027), vinculando as ocorrências 2 a 12 à primeira ocorrência como despesa-mãe (`DESP_ID_RECORRENTE_PAI`).
E quando eu excluir a primeira ocorrência (despesa-mãe), o sistema deve excluir logicamente toda a série recorrente projetada.

### 16.33 Duplicação individual e em lote de despesas para competência destino

Dado que estou autenticado com a permissão [PERM02](#perm02) e selecionei três despesas no grid.
Quando eu clicar no botão "Duplicar selecionadas", informar a competência de destino "11/2026" no modal e confirmar a duplicação via [EDP12](#edp12).
Então o sistema deve criar três novas despesas duplicando os dados cadastrais (nome, valor, categoria, conta/cartão e meio de pagamento), definindo `DESP_COMPETENCIA = '2026-11'`, `DESP_ORIGEM = 'MANUAL'`, status de pagamento em aberto (ou não se aplica para cartão), `DESP_DT_PAGAMENTO = NULL`, `DESP_FL_PARCELADA = FALSE` e `DESP_FL_RECORRENTE = FALSE`, exibir [MSG22](#msg22) e recarregar o grid preservando a página e filtros.

### 16.34 Edição inline de valor da despesa no grid via PATCH

Dado que estou autenticado com a permissão [PERM03](#perm03) e visualizo uma despesa no valor de R$ 150,00 no grid.
Quando eu der duplo clique na célula de valor da despesa, digitar o novo valor "180,50", confirmar via [EDP13](#edp13) e a operação for concluída.
Então o sistema deve atualizar `DESP_VALOR = 180.50`, registrar uma nova revisão no Hibernate Envers ([RN10](#rn10)), exibir [MSG25](#msg25) e atualizar a exibição do grid sem recarregar a página inteira.

### 16.35 Importação de fatura de cartão de crédito (Itaú, Bradesco, C6, OFX)

Dado que possuo a permissão [PERM06](#perm06) e um cartão de crédito cadastrado e ativo.
Quando eu acionar a opção "Importar fatura", selecionar o cartão "Itaú Mastercard", a competência "10/2026", o arquivo de fatura em formato Excel (.xlsx) e submeter via [EDP14](#edp14).
Então o sistema deve processar as linhas do extrato utilizando o parser específico do Itaú, criar as despesas com `CACR_ID` correspondente, `DESP_ORIGEM = 'IMPORTACAO'`, `DESP_MEIO_PAGAMENTO = 'CREDITO'`, `DESP_IND_STATUS_PAGAMENTO = 'NAO'`, `DESP_COMPETENCIA = '2026-10'`, exibir [MSG23](#msg23) com o quantitativo de despesas importadas e atualizar o grid.

### 16.36 Totalizador por competência condicional (apenas quando competenciaInicio == competenciaFim)

Dado que estou na tela de Despesas com filtros de competência disponíveis.
Quando eu filtrar pela competência única "09/2026" (`competenciaInicio = '2026-09'` e `competenciaFim = '2026-09'`).
Então o card totalizador no topo do grid ([ID3a](#qdd1-3a)) deve ser exibido com a soma exata de todas as despesas da competência calculada por [C11](#c11).
E quando eu alterar o filtro para o intervalo de "08/2026" a "09/2026" (`competenciaInicio != competenciaFim`), o card totalizador deve ser automaticamente ocultado ([RN30](#rn30)).

### 16.37 Inicialização com filtro padrão no mês anterior (mes_atual - 1) e competências passadas irrestritas

Dado que hoje estamos no mês de setembro de 2026 (`2026-09`).
Quando eu acessar a tela de Despesas pela primeira vez na sessão sem informar parâmetros na URL.
Então os campos de filtro de competência inicial e final devem ser automaticamente inicializados com "08/2026" (`mes_atual - 1`), e o grid deve listar apenas as despesas dessa competência.
E quando eu selecionar uma competência passada remota (ex.: "01/2020"), o sistema deve aceitar e consultar sem emitir erros ou restrições temporais ([RN29](#rn29)).

### 16.38 Edição inline do nome da despesa no grid

Dado que estou autenticado com a permissão [PERM03](#perm03) e visualizo uma despesa no grid.
Quando eu clicar na célula da coluna Nome ([ID10](#qdd1-10)), digitar o novo nome "Aluguel Ajustado" e confirmar via Enter ([RT25](#rt25)).
Então o sistema deve chamar [EDP18](#edp18), atualizar `DESP_NOME = 'Aluguel Ajustado'`, registrar nova revisão no Hibernate Envers ([RN10](#rn10)), exibir [MSG31](#msg31) e atualizar a visualização no grid sem recarregar a página.

---

## 17. Workshop de Análise

Data: —
Convidados: Diego Cordeiro
Participantes: Diego Cordeiro
Descrição: Levantamento a partir do Documento 0 ([QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10) e [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11); Observações 8, 9, 12, 15, 16, 18, 18a, 19, 21, 24 e 28), do CRUD REST de `Despesa` da geração 1 (`dsc-backend` — `domain/Despesa.java`, `domain/DespesaUsuario.java`, `controller/DespesaController.java`, `services/DespesaService.java`, `enums/StatusPagamento.java`; métodos `gerarParcelamento`, `inserir`/`editar` com `usuariosResponsaveis`, `pagarDespesa`/`pagarDespesas`, `compartilharDespesas`, `importarDadosCartaoCredito*`) e do padrão de tela dos documentos `08 - manter-receita` (par direto — escopo *row-level*, competência independente, datas futuras, origem, edição parcial de Open Finance, dinheiro = `CARTEIRA`), `06 - manter-conta` e `07 - manter-cartao-credito` (comboboxes e endpoints de opções).

**Decisões tomadas:**
- A tela **Despesas** vive em "Finanças > Despesas" e é acessível a `ADMIN` e `USER`; cada um opera só sobre as próprias despesas ([RN02](#rn02)).
- `DESPESAS` **não tem `USU_ID`**; o dono é o `USU_ID` da conta (`DESP.CTA_ID → CONTAS.USU_ID`) **ou** do cartão (`DESP.CACR_ID → CARTOES_CREDITO.USU_ID`). O escopo *row-level* é resolvido no serviço, a partir do contexto de segurança — nunca de um parâmetro. Acesso cruzado por `{id}` responde 404 ([MSG05](#msg05)). **O Documento 0 não é alterado** e `DESPESAS` não ganha `USU_ID`.
- **Toda despesa tem exatamente uma âncora de pagamento** — conta **ou** cartão, do próprio usuário, ativos ([RN03](#rn03)). Conta e cartão continuam anuláveis no schema; a regra conta-XOR-cartão fica na camada de serviço.
- **Dinheiro em espécie = conta do tipo `CARTEIRA`**, com `DESP_MEIO_PAGAMENTO = DINHEIRO` (decisão herdada do documento `08`).
- **Permissões atômicas (eliminação definitiva de `DESPESAS_MANTER`):** split em `DESPESAS_LISTAR`, `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR` e `DESPESAS_IMPORTAR` concedidas a `ADMIN` e `USER` na carga inicial; `DESPESA_RATEAR_MULTIUSUARIO` com `PERM_FL_CONCEDIVEL_POR_PLANO = TRUE`, concedida exclusivamente por plano pago (Documento 0, Observação 24).
- **O `ADMIN` não tem visão administrativa de dado financeiro de terceiros.** Opera como um `USER` sobre as próprias despesas.
- **Competência — campo próprio e independente:** `YearMonth`, apenas pré-preenchida com o mês da data de lançamento ([RT13](#rt13) / parâmetro `DESPESA_COMPETENCIA_SEGUE_DATA_LANCAMENTO`). Pode divergir.
- **Datas podem ser futuras** ([RN06](#rn06)).
- **Origem:** despesas criadas por esta tela são sempre `MANUAL` ([RN07](#rn07)). `OPEN_FINANCE` só chega pela conciliação; `IMPORTACAO` chega pela rotina de importação de faturas ([EDP14](#edp14), [RN28](#rn28)). Sem migração da geração 1 (Observação 18a).
- **Despesa importada do Open Finance** — edição parcial (campos de negócio editáveis; forma de pagamento e origem bloqueadas; sem exclusão) ([RN08](#rn08) / [MSG16b](#msg16b)).
- **Parcelamento** ([RN12](#rn12) a [RN14](#rn14)): a tela informa **número de parcelas** e **valor total da compra**; o serviço cria N registros `DESPESAS`. A **1ª parcela é a despesa-mãe** (`DESP_ID_PARCELA_PAI` nula), as demais apontam para ela (melhoria F — FK real). `DESP_VALOR` = total ÷ N com resíduo na última parcela; `DESP_VALOR_TOTAL_COMPRA` = total em todas; competência e vencimento avançam 1 mês por parcela; data de lançamento igual em todas. **Editar** uma parcela afeta só ela; mudar N ou o total exige excluir e recriar. **Excluir** a mãe apaga a série toda; excluir uma filha apaga só ela.
- **Rateio e contatos** ([RN15](#rn15) a [RN20](#rn20)): recurso de plano pago. A despesa continua sendo do criador; co-participantes são **contatos da agenda privada do usuário** (`CONTATOS.CONT_ID`, sejam extra-sistema cadastrados diretamente ou usuários do sistema conectados via convite) com uma fatia (`DEPU_VALOR`). Soma das fatias ≤ `DESP_VALOR` (o resto é a cota do dono). Replicado em todas as parcelas. **Privacidade e segurança:** a busca de participantes é restrita exclusivamente à agenda do próprio usuário ([RN19](#rn19), eliminando enumeração de usuários da plataforma); co-participantes não veem a despesa na tela de despesas do dono. Suporte a cadastro rápido inline de contato extra-sistema ([EDP11](#edp11)). O **acerto** de cada fatia é registrado pelo dono (`DEPU_IND_STATUS_PAGAMENTO` + `DEPU_DT_ACERTO` na chave `(DESP_ID, CONT_ID)`).
- **Baixa de pagamento** ([RN21](#rn21)): ação individual no grid ([EDP07](#edp07)) e em lote ([EDP08](#edp08), espelha `pagarDespesas`). Marca `SIM` + `DESP_DT_PAGAMENTO`. Por parcela numa série.
- **Meio de pagamento** ([RN22](#rn22)): derivado da forma de pagamento e ajustável só na forma "Conta à vista".
- **Compra no cartão** ([RN23](#rn23)): grava `CACR_ID`, meio `CREDITO`, status `NAO` ou `SIM` (se informado pagamento). Permite baixa normal e inclusão no somatório de pendentes/geral do grid. O vínculo com a fatura (`FTCA_ID`), o fechamento e o pagamento da fatura são do documento `11`.
- **`DESP_FL_PAGAMENTO_FATURA`** ([RN24](#rn24)): caso de borda — a v1.0 não o oferece pelo fluxo da tela; toda despesa nasce com `FALSE`.
- **Importação de fatura e extrato de cartão de crédito:** resgate dos parsers da geração 1 (`dsc-backend`: Excel Itaú, Bradesco, C6 Bank e arquivos OFX via Apache POI e OFX4J) em modal dedicado ([EDP14](#edp14), [RN28](#rn28)).
- **Duplicação de despesas:** recurso individual e em lote permitindo projetar despesas para competência futura informada ([EDP12](#edp12), [RN26](#rn26)).
- **Edição inline de valor:** alteração direta de valor na célula do grid com duplo clique via [EDP13](#edp13) (`PATCH /despesas/{id}/valor`) e auditoria Hibernate Envers ([RN27](#rn27)).
- **Filtro padrão e competências passadas:** listagem inicia filtrando `mes_atual - 1` e aceita qualquer competência histórica sem restrições ([RN29](#rn29)).
- **Totalizador condicional por competência única:** exibido somente quando `competenciaInicio == competenciaFim` ([RN30](#rn30)).
- **Preservação de estado do grid:** filtros, ordenação e página mantidos após qualquer ação no grid ([RN31](#rn31)).

**A Confirmar:**
- **Despesa-mãe: 1ª parcela × registro separado.** A v1.0 usa a **1ª parcela como mãe** (`DESP_ID_PARCELA_PAI` nula), espelhando a geração 1. Confirmar se um registro-mãe sintético separado (que não entra no grid nem no total, só agrega a série) seria mais limpo para relatórios e para o comportamento de exclusão/edição.
- **Editar a série inteira.** A v1.0 só edita parcela a parcela ([RN13](#rn13)). Confirmar se deve existir um "editar toda a série" (ex.: mudar a categoria de todas as parcelas de uma vez, ou reprocessar valores ao alterar o total na mãe).
- **Excluir uma parcela do meio.** A v1.0 apaga só a parcela e deixa a série incompleta ([RN14](#rn14)). Confirmar se deve reajustar as parcelas restantes (renumerar, redistribuir o valor) ou bloquear a exclusão de parcela isolada, permitindo só "excluir a série".
- **Privacidade do co-participante do rateio.** A v1.0 não mostra a despesa ao co-participante ([RN19](#rn19)). Confirmar o modelo da versão futura: o co-participante vê só a fatia dele (valor, nome da despesa, competência, quem dividiu) numa tela "compartilhado comigo"? Ele pode marcar o próprio acerto, ou só o dono? Ele vê o valor total e as fatias das outras pessoas?
- **Soma das fatias = total × parcial.** A v1.0 permite soma ≤ valor, com a diferença como cota do dono ([RN17](#rn17), parâmetro `DESPESA_RATEIO_SOMA_DEVE_FECHAR = false`). Confirmar se o padrão deve ser exigir soma exata.
- **Rateio sem plano pago: seção some × aviso de upgrade.** A v1.0 **oculta** a seção ([RN15](#rn15)). Confirmar se deve aparecer um aviso/CTA discreto de upgrade ([MSG16](#msg16)) no lugar da seção, ou nada.
- **Arredondamento da última parcela.** A v1.0 joga o resíduo na **última** parcela (parâmetro `DESPESA_RESIDUO_PARCELA_NA_ULTIMA`). Confirmar se a preferência do produto é última, primeira, ou distribuir o centavo entre as primeiras parcelas.
- **`DESP_MEIO_PAGAMENTO` derivado × campo livre.** A v1.0 deriva da forma de pagamento e só deixa escolher na forma "Conta à vista" ([RN22](#rn22)). Confirmar se o usuário deveria poder informar o meio livremente em qualquer forma (ex.: PIX pago com o saldo de uma conta corrente já é coberto; e um "vale-refeição"?).
- **`NAO_SE_APLICA` de status de pagamento fora da compra no cartão.** A v1.0 usa `NAO_SE_APLICA` só para compra no cartão ([RN23](#rn23)). Confirmar se há outros casos (lançamento informativo, ajuste, estorno) que devem nascer `NAO_SE_APLICA` e se a tela deve oferecer essa escolha.
- **Volume e paginação.** Se o número de despesas por usuário crescer muito (compras em 72x multiplicam as linhas), o grid client-side deve passar a recortar por competência no servidor ([RNF05](#rnf05))?
- **Agrupamento visual da série no grid.** Confirmar se as N parcelas devem aparecer como N linhas soltas (v1.0), como uma linha-resumo expansível, ou com filtro "mostrar/ocultar parcelas futuras".

---

## 18. Anexos

- **Protótipo e diagramas (v1.0):** gerados. Casos de uso (`prototipo/manter-despesa-casos-uso.drawio` + `images/manter-despesa-casos-uso.png`), DER do subconjunto (`prototipo/manter-despesa-der.drawio` + `images/manter-despesa-der.png`), wireframes das cinco telas/modais (`prototipo/manter-despesa-prototipo.drawio` + `images/md-tela-1..5.png`) e protótipo navegável (`prototipo/manter-despesa-prototipo.html`). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright); diagramas `.drawio` por `prototipo/gen-diagramas.py`.
- Documento 0 — Fundação: `../00 - analise-geral/documento-0-fundacao.md` ([QUADRO_DESCRITIVO_2](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-2), [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3), [QUADRO_DESCRITIVO_5](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-5), [QUADRO_DESCRITIVO_6](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-6), [QUADRO_DESCRITIVO_8](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-8), [QUADRO_DESCRITIVO_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [QUADRO_DESCRITIVO_11](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-11), [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26); Seções 7.2, 7.3 e 7.4; Observações 8, 9, 12, 15, 16, 18, 24 e 28).
- Documento `04 - manter-categoria`: `../04 - manter-categoria/documento-analise-manter-categoria.md` — endpoint de opções de categoria (`GET /categorias/opcoes?aplicaA=DESPESA`).
- Documento `06 - manter-conta`: `../06 - manter-conta/documento-analise-manter-conta.md` — endpoint de opções de conta (`GET /contas/opcoes`), conta `CARTEIRA`, padrão de escopo *row-level*.
- Documento `07 - manter-cartao-credito`: `../07 - manter-cartao-credito/documento-analise-manter-cartao-credito.md` — endpoint de opções de cartão (`GET /cartoes/opcoes`).
- Documento `08 - manter-receita`: `../08 - manter-receita/documento-analise-manter-receita.md` — par direto (forma, voz, decisões espelhadas).
- Documentos `11 - manter-fatura-cartao`, `14 - dashboard-financeiro`, `16 - open-finance-conciliacao` (a escrever) — fatura de cartão e `DESP.FTCA_ID`, total por categoria e `DESP_FL_PAGAMENTO_FATURA`, despesas de origem `OPEN_FINANCE`.
- **Importação de extrato e fatura de cartão:** rotinas resgatadas da geração 1 (`dsc-backend` — `importarDadosCartaoCreditoExcelItau`, `importarDadosCartaoCreditoExcelBradesco`, `importarDadosCartaoCreditoExcelC6Bank`, `importarDadosCartaoCreditoOfx`) e integradas ao módulo via Apache POI e OFX4J.
- Código de referência geração 1: `dsc-backend` (`domain/Despesa.java`, `domain/DespesaUsuario.java`, `controller/DespesaController.java`, `services/DespesaService.java`, `enums/StatusPagamento.java`, `enums/TipoRegistroFinanceiro.java`, `enums/CategoriaRegistroFinanceiro.java`).
