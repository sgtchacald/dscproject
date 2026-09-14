# Documentos de Análise — dscproject-spring-mvc

Padrão 72B (documento de análise híbrido, 17 seções). Uma pasta por módulo/tela,
nomeada `NN - nome-com-hifen` para ordenar sozinha.

## Ciclo de vida do documento

Cada documento carrega um campo **`Status`** no cabeçalho (logo abaixo de
`Versão`); a coluna **Situação** desta tabela repete o mesmo valor. Progressão:

| Status | Significado | Quem marca |
|---|---|---|
| `A escrever` | Pasta/tema previstos, documento ainda não redigido | analista-requisitos / usuário |
| `Em análise` | Documento em redação — seções incompletas ou decisões de escopo em aberto | analista-requisitos |
| `Analisado` | Corpo das 17 seções fechado e versionado; pronto para desenvolvimento. Itens menores podem seguir listados em "A Confirmar" na Seção 17 sem bloquear | analista-requisitos |
| `Desenvolvido` | **Todos** os requisitos funcionais (RF) e não funcionais (RNF) do documento implementados e cobertos por teste; branch de feature commitada no repo-alvo | **desenvolvedor-java** |
| `Homologado` | Tela publicada em homologação, testada e aprovada pelo usuário | **usuário** (ou Claude, a pedido do usuário) |

### Diretrizes

- **D-CICLO-01** — O `desenvolvedor-java` só move o Status para `Desenvolvido`
  quando **todos** os RF (Seção 3.1) e RNF (Seção 3.2) do documento estiverem
  implementados e com teste passando. Cobertura parcial mantém `Analisado`; a
  pendência vai para o relatório de execução.
- **D-CICLO-02** — `Desenvolvido → Homologado` é decisão do usuário, depois de
  validar a tela no ambiente de homologação. O usuário marca ou pede ao Claude que
  marque.
- **D-CICLO-03** — Ao mudar o Status, atualizar **os dois lugares**: o cabeçalho do
  documento e a coluna Situação deste README. Mudança de Status é metadado — não
  gera nova versão nem linha no Histórico de Versões, a menos que venha junto de
  alteração de conteúdo.

## Documentos

| Pasta | Conteúdo | Situação |
|---|---|---|
| `00 - analise-geral` | Documento 0 — Fundação: tabelas (RBAC + domínio financeiro + Open Finance + parâmetros globais + redes sociais de usuário), DDL MySQL, mapeamento JPA, DER e diagrama de classes. Base transversal de todos os demais. v1.7: inclusão da associativa `USUARIOS_REDES_SOCIAIS` (QUADRO_DESCRITIVO_30) e split atômico de permissões do sistema (Observação 28). | `Analisado` · v1.7 |
| `01 - manter-usuario` | CRUD de Usuário (mescla gerações 1 e 2) + RBAC + recuperação de senha por token + redes sociais de usuário (`USUARIOS_REDES_SOCIAIS`). Tela nova `USUARIOS_RECUPERACAO_SENHA`. v1.4: nova aba "Redes Sociais" em `/minha-conta`, reflexo dinâmico de ícones no cabeçalho e rodapé do admin. Protótipo navegável + wireframes na Seção 7 | `Homologado` · v1.4 |
| `02 - manter-perfil-permissao` | Tela de RBAC: gerenciar perfis e vincular permissões. Catálogo de permissões vem do código (sincronizador). Travas anti-lockout. v1.3: eliminação de permissões agrupadas com sufixo `MANTER` e migração completa para catálogo granular de operações atômicas (`_LISTAR`, `_INSERIR`, `_EDITAR`, `_EXCLUIR`, `_VINCULAR_PERMISSAO`). | `Homologado` · v1.3 |
| `03 - manter-parametro-global` | Tela **só de edição** dos parâmetros globais do sistema (`PARAMETROS_GLOBAIS`, prefixo `PAGL_`). Os parâmetros são semeados por um **loader no código** (mesmo padrão do catálogo de permissões) — a tela não cria nem exclui. v1.1: `tipo` e `valor` são editados **inline no grid** (a célula vira o editor do tipo da linha em tempo de execução); a confirmação de cada alteração pede o `motivo` num modal enxuto ("Confirmar Alteração") — não há tela/modal de edição. Histórico de revisões (Envers) no escopo. Engenharia reversa do `portal-lgpd-api`. Nova tabela no Documento 0: `PARAMETROS_GLOBAIS` (QUADRO_DESCRITIVO_28, v1.4) | `Desenvolvido` · v1.1 |
| `04 - manter-categoria` | CRUD de Categoria + tela do mapa `CATEGORIAS_PROVEDOR`. Categoria de sistema (carga inicial do enum da geração 1) não excluível e de código imutável (`CATE_FL_SISTEMA`). Permissões granulares por operação (`CATEGORIAS_LISTAR`/`_INSERIR`/`_EDITAR`/`_EXCLUIR` e `CATEGORIAS_PROVEDOR_LISTAR`/`_INSERIR`/`_EDITAR`/`_EXCLUIR`). CRUD server-side sem AJAX. | `Desenvolvido` · v1.1 |
| `05 - manter-instituicao-financeira` | CRUD de Instituição Financeira (catálogo global de bancos/corretoras, ADMIN-only, dá tela ao `InstituicaoFinanceiraController` REST da geração 1 + `INFI_FL_ATIVO`) + tela do mapa `OPFI_INSTITUICAO_PROVEDOR`. Instituição de carga inicial protegida por `INFI_FL_SISTEMA` (padrão de `CATE_FL_SISTEMA`); trava de exclusão por uso em `CONTAS`/`INVESTIMENTOS`/`OPFI_CONEXOES`. v1.1: permissões atômicas granulares (`INSTITUICOES_LISTAR`, `INSTITUICOES_INSERIR`, `INSTITUICOES_EDITAR`, `INSTITUICOES_EXCLUIR`, `INSTITUICOES_PROVEDOR_LISTAR`, `INSTITUICOES_PROVEDOR_INSERIR`, `INSTITUICOES_PROVEDOR_EDITAR`, `INSTITUICOES_PROVEDOR_EXCLUIR`) sem sufixo `MANTER`. | `Desenvolvido` · v1.1 |
| `06 - manter-conta` | CRUD das contas **do usuário** (tela "Minhas Contas") sobre `CONTAS` (prefixo `CTA_`, ex-`INSTITUICOES_FINANCEIRAS_USUARIO` da geração 1): descrição, tipo (enum `TipoConta` novo), instituição (combobox do `05`), agência, número, moeda, saldo inicial, "considera no saldo geral", situação. Escopo *row-level* por `USU_ID` no serviço — **inclusive para o ADMIN**, sem visão administrativa de dados financeiros de outros; acesso cruzado por id responde 404. Saldo manual com ação dedicada "Ajustar saldo" (Envers registra). Exclusão lógica com trava de uso (oferta de desativar). Endpoint de contas ativas alimenta `07`/`08`/`09`/`10`. v1.1: permissões granulares atômicas (`CONTAS_LISTAR`, `CONTAS_INSERIR`, `CONTAS_EDITAR`, `CONTAS_EXCLUIR`, `CONTAS_DESATIVAR`, `CONTAS_ATIVAR`, `CONTAS_AJUSTAR_SALDO`). | `Desenvolvido` · v1.1 |
| `07 - manter-cartao-credito` | CRUD dos cartões de crédito **do usuário** (tela "Meus Cartões"): descrição, bandeira, final do cartão, limite, dia de fechamento/vencimento, conta de débito opcional, situação. Escopo por `USU_ID` no serviço — **inclusive para o ADMIN**; acesso cruzado por id responde 404. Sem "cartão de sistema"; exclusão lógica com trava por faturas/despesas vinculadas (oferta de desativar). Depende do `06` (combobox de conta de débito) e alimenta o `09` (cartões ativos); fatura de cartão fica no `11`. v1.1: permissões granulares atômicas (`CARTOES_LISTAR`, `CARTOES_INSERIR`, `CARTOES_EDITAR`, `CARTOES_EXCLUIR`, `CARTOES_DESATIVAR`, `CARTOES_ATIVAR`). | `Desenvolvido` · v1.1 |
| `08 - manter-receita` | CRUD das receitas **do usuário** (tela "Finanças > Receitas") sobre `RECEITAS` (prefixo `RECE_`): nome, descrição, valor com máscara contínua `pt-BR`, data de lançamento, competência (`YearMonth`), conta (combobox do `06`), categoria opcional (combobox do `04`, `aplicaA=RECEITA`). Introduz **prevista × recebida** (`RECE_FL_RECEBIDO` + `RECE_DT_RECEBIMENTO`, com ação dedicada "Registrar recebimento") e `RECE_ORIGEM`. Escopo *row-level* por usuário derivado da conta (`RECE.CTA_ID → CONTAS.USU_ID`) — **inclusive para o ADMIN**; acesso cruzado por id responde 404. Conta obrigatória na aplicação. Receita `OPEN_FINANCE` editável nos campos de negócio, sem conta/origem e não excluível pela tela. Sem trava de uso (receita é folha); exclusão lógica. v1.1: split atômico de permissões (`RECEITAS_LISTAR`, `RECEITAS_INSERIR`, `RECEITAS_EDITAR`, `RECEITAS_EXCLUIR`, `RECEITAS_REGISTRAR_RECEBIMENTO`), filtro inicial `mes_atual - 1`, competências passadas irrestritas, totalizador condicional por competência única, duplicação e preservação de filtros no grid. | `Desenvolvido` · v1.1 |
| `09 - manter-despesa` | CRUD das despesas **do usuário** (tela "Finanças > Despesas") sobre `DESPESAS` (prefixo `DESP_`) e a associativa `DESPESAS_USUARIO` (`DEPU_`). Escopo *row-level* pelo `USU_ID` da conta **ou** do cartão (conta XOR cartão obrigatório; dinheiro = conta `CARTEIRA`); ADMIN sem visão de terceiros. Parcelamento (série ligada por `DESP_ID_PARCELA_PAI` — melhoria F) e rateio com a agenda de contatos (`DESPESAS_USUARIO`, recurso de plano pago via `DESPESA_RATEAR_MULTIUSUARIO`). Baixa individual e em lote. Compra no cartão grava `CACR_ID`. v1.3: permissões atômicas granulares (`DESPESAS_LISTAR`, `DESPESAS_INSERIR`, `DESPESAS_EDITAR`, `DESPESAS_EXCLUIR`, `DESPESAS_PAGAR`, `DESPESAS_IMPORTAR`, `DESPESA_RATEAR_MULTIUSUARIO`), resgate da importação de cartão v1 (Excel Itaú, Bradesco, C6 e OFX via Apache POI e OFX4J), duplicação individual e em lote, edição inline de valor (`PATCH`), máscara monetária em tempo real, filtro `mes_atual - 1`, totalizador condicional e preservação de filtros no grid. | `Analisado` · v1.3 |
| `10 - manter-transacao-bancaria` | CRUD de Transação Bancária | `A escrever` |
| `11 - manter-fatura-cartao` | Fatura de Cartão — ciclo de vida, pagamento | `A escrever` |
| `12 - manter-investimento` | CRUD de Investimento | `A escrever` |
| `13 - dashboard` | Totais por competência, saldo consolidado, gráficos | `A escrever` |
| `14 - open-finance-conectar-conta` | Provedor, credencial cifrada, widget de conexão, consentimento | `A escrever` |
| `15 - open-finance-conciliacao` | Conciliação das transações de staging com o domínio | `A escrever` |
| `16 - manter-contato` | CRUD de Contatos e Conexões (tela "Minha Rede > Contatos") sobre `CONTATOS` (prefixo `CONT_`): agenda de contatos do usuário dividida entre contatos extra-sistema (nome, e-mail, telefone, chave Pix para acertos) e conexões com outros usuários da plataforma via convite (ciclo de vida pendente, ativo, recusado, bloqueado). Base para o rateio de despesas (`09`) e futuros compartilhamentos | `A escrever` |

## Convenções

- Cada documento segue o template `72b-template-doc-analise.md`.
- Identificadores: `EDP_NNNNN` (endpoints), `RN_NNNNN` (regras de negócio), `MSG_NNNNN`
  (mensagens), `C1`/`C2` (consultas), `RT01` (regras de tela), `QUADRO_DESCRITIVO_N`.
- Referência cruzada é `ação + link + pontuação` — não repetir no ponto da citação o
  que o destino já diz.
- O documento 0 é a fonte única da estrutura de dados; os documentos de tela
  referenciam os `QUADRO_DESCRITIVO` dele, não redefinem tabelas.
- Diagramas: `.drawio` (fonte) + `images/*.png` (render, via `flatpak run com.jgraph.drawio.desktop -x -f png`).
