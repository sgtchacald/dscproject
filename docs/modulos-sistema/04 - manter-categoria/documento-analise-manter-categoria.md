# dscproject — Análise de Sistemas
## Módulo Categorias — ADMIN — Manter Categoria

**Gerado em:** 07/09/2026
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
| 1.0 | 07/09/2026 | Diego dos Santos Cordeiro | Criação do documento. CRUD administrativo de Categoria (substitui o enum `CategoriaRegistroFinanceiro` da geração 1) e a tela do mapa `CATEGORIAS_PROVEDOR` (rótulo de categoria de um provedor de Open Finance → categoria do sistema), usada na conciliação automática. A estrutura das duas tabelas é a do Documento 0 ([QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) e [QUADRO_DESCRITIVO_15](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-15)) — este documento não introduz tabela nova |
| 1.1 | 09/09/2026 | Diego dos Santos Cordeiro | (1) As permissões guarda-chuva `CATEGORIAS_MANTER` e `CATEGORIAS_PROVEDOR_MANTER` são quebradas em uma permissão por operação: `CATEGORIAS_LISTAR` / `CATEGORIAS_INSERIR` / `CATEGORIAS_EDITAR` / `CATEGORIAS_EXCLUIR` e `CATEGORIAS_PROVEDOR_LISTAR` / `CATEGORIAS_PROVEDOR_INSERIR` / `CATEGORIAS_PROVEDOR_EDITAR` / `CATEGORIAS_PROVEDOR_EXCLUIR` (convenção domínio-primeiro, espelhando o documento `01 - manter-usuario`). Desativar/reativar categoria não é permissão própria — é gravar `CATE_FL_ATIVO` por `CATEGORIAS_EDITAR`. Nova matriz 13.1 com as oito permissões; Seções 1, 3.2, 5, 7, 8, 9, 13, 14 e 17 realinhadas. (2) As duas telas passam a ser CRUD sem AJAX: grid renderizado no servidor, modal de cadastro/edição preenchido pelos dados da linha do grid (`data-*` / `<template>`), filtro por GET, gravação por POST → redirect → mensagem flash. Removidos os endpoints JSON `/listar-dados`, `/buscar/{id}` e `/provedores-opcoes`; [EDP05](#edp05) (combobox das telas de lançamento) permanece em JSON. Seções 1, 2, 3.2, 5, 7, 8, 11, 12, 15 e 16 ajustadas |
| 1.2 | 12/09/2026 | Diego dos Santos Cordeiro | Padronização visual do sistema: explicitação do alinhamento à esquerda para a coluna de Ações nos grids de Categorias (QUADRO_DESCRITIVO_1) e Categorias por Provedor (QUADRO_DESCRITIVO_4). |

---

## Diretrizes para Elaboração do Documento

| Nº | DIRETRIZ |
|---|---|
| D01 | As responsabilidades de camada são documentadas como **Regra de Tela (RT)** e **Regra de Negócio (RN)** — nunca "o backend deve" / "o frontend deve". |
| D02 | O termo `endpoint` é aceito na Seção 8. Fora dela, "chamada ao serviço". |
| D03 | A estrutura de dados é a do Documento 0 (`00 - analise-geral`). Este documento **referencia** os QUADRO_DESCRITIVO do Documento 0 e **não introduz tabela nova**. |

---

## 1. Introdução

Este documento descreve a funcionalidade **Manter Categoria** do `dscproject-spring-mvc`, junto com a tela auxiliar **Categorias por Provedor**.

Na geração 1 (API REST + SPA Angular), a categoria de um lançamento é o enum `CategoriaRegistroFinanceiro` (`SALARIO`, `ALIMENTACAO`, `CARTAO_DE_CREDITO`, …) — uma lista fixa no código, sem tela. O Documento 0 (Observação 14) travou a decisão de transformar esse enum na tabela **`CATEGORIAS`**, editável por tela, e de guardar o mapa "categoria do provedor → categoria do sistema" na tabela **`CATEGORIAS_PROVEDOR`**, consultada na conciliação automática das transações de Open Finance.

Este documento cobre:

- a **tela administrativa de Categorias** — listar, cadastrar, editar, desativar e excluir (exclusão lógica), com a proteção das categorias de carga inicial (`CATE_FL_SISTEMA = TRUE`);
- a **tela administrativa de Categorias por Provedor** — cadastrar e manter os vínculos rótulo externo + provedor → categoria;
- o **contrato de consulta** que as telas de lançamento (Receita, Despesa, Transação Bancária) usam para carregar as categorias no combobox.

**Escopo deste documento:**
- Tela de **listagem de categorias** (grid renderizado no servidor), restrita a quem tem [PERM01](#perm01), com filtro por modal (GET).
- **Cadastro e edição** de categoria via modal único (código, nome, aplica-se a, cor, ícone, situação).
- **Exclusão lógica** de categoria, com as travas: categoria de sistema não é excluível; categoria em uso por lançamentos não é excluível (pode ser desativada).
- **Desativação** de categoria (`CATE_FL_ATIVO = FALSE`) — some dos comboboxes de novos lançamentos sem afetar os lançamentos que já a usam.
- Tela de **listagem e manutenção** dos vínculos `CATEGORIAS_PROVEDOR`.
- **Fornecimento das categorias ativas** (filtradas por aplica-se a) para os comboboxes das telas de lançamento.
- Definição das permissões que estas telas usam — uma por operação (`LISTAR` / `INSERIR` / `EDITAR` / `EXCLUIR` por módulo). O RBAC completo é do Documento 0 e a tela que edita perfil × permissão é o documento `02 - manter-perfil-permissao`.

**Não contempla:**
- CRUD de Receita, Despesa e Transação Bancária, que **consomem** a categoria — documentos `08`, `09` e `10`.
- O job de sincronização de Open Finance e o passo de conciliação em si — documentos `14` e `15`. Aqui, apenas o mapa que a conciliação automática consulta.
- Cadastro de provedores de Open Finance (`OPFI_PROVEDORES`) — vem de carga inicial; a tela de Categorias por Provedor apenas o referencia num combobox.
- Categoria **por usuário**: `CATEGORIAS` não tem dono no Documento 0 — as categorias são globais (ver Seção 17).

**Perfis com acesso:** [PERF01](#perf01) (ADMIN) para as duas telas administrativas. O combobox de categorias das telas de lançamento ([EDP05](#edp05)) é disponível a qualquer usuário autenticado ([PERF01](#perf01) e [PERF02](#perf02)).

---

## 2. Observações

| Nº | OBSERVAÇÃO | REFERÊNCIA / IMPACTO |
|---|---|---|
| 1 | **A categoria deixa de ser enum.** `CategoriaRegistroFinanceiro` da geração 1 vira a tabela `CATEGORIAS` ([QUADRO_DESCRITIVO_3 do Documento 0](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3)). `Receita`, `Despesa` e `TransacaoBancaria` passam a ter `CATE_ID` (FK, **nullable**) no lugar da coluna de enum. **[Requer código]** | [RN02](#rn02) |
| 2 | **Categorias são globais, geridas só por ADMIN.** `CATEGORIAS` não tem `USU_ID` no Documento 0 — não há categoria "de um usuário". Todo usuário vê o mesmo catálogo. Um eventual modelo de categoria própria por usuário exigiria coluna nova no Documento 0 e está fora do escopo (ver Seção 17). | [PERM01](#perm01)–[PERM04](#perm04) |
| 3 | **Categoria de sistema** (`CATE_FL_SISTEMA = TRUE` — as de carga inicial, Seção 6.4): o código (`CATE_CODIGO`) **não é editável** e a categoria **não é excluível**. Nome, aplica-se a, cor, ícone e situação (ativa/inativa) **são** editáveis. Mesma regra do "perfil de sistema" do documento `02 - manter-perfil-permissao`. | [RN04](#rn04), [RN05](#rn05) |
| 4 | **Exclusão em uso.** Excluir uma categoria referenciada por algum lançamento (Receita, Despesa ou Transação Bancária não excluído) é **bloqueado** ([RN06](#rn06)); a alternativa é **desativar** a categoria. O comportamento é parametrizável ([Seção 12](#12-parâmetros-de-sistema)) — ver a decisão em aberto na Seção 17. | [RN06](#rn06) |
| 5 | **Desativação ≠ exclusão.** Uma categoria inativa (`CATE_FL_ATIVO = FALSE`) some dos comboboxes de **novos** lançamentos ([EDP05](#edp05)) e não pode ser escolhida em novos vínculos de provedor, mas continua válida nos lançamentos que já a usam, nos relatórios e na conciliação. | [RN09](#rn09) |
| 6 | **`CATE_APLICA_A`** (`RECEITA` / `DESPESA` / `AMBOS`) restringe em quais telas de lançamento a categoria aparece: a tela de Receita só mostra `RECEITA` e `AMBOS`; a de Despesa, `DESPESA` e `AMBOS`; a de Transação Bancária, todas. É um campo de negócio, não de auditoria. | [RN03](#rn03) |
| 7 | **Mapa de provedor em tela própria.** `CATEGORIAS_PROVEDOR` é mantido numa **tela administrativa dedicada**, não numa aba do modal de categoria (ver Seção 17). Como `OPFI_PROVEDORES` vem de carga inicial, esta tela funciona antes das telas de Open Finance (`14`/`15`). | [QUADRO_DESCRITIVO_4](#quadro-descritivo-4) |
| 8 | **Uso do mapa na conciliação automática.** Quando uma transação de *staging* chega com `OFTR_CATEGORIA_EXTERNA` preenchida, o passo de conciliação (documento `15`) casa esse rótulo + o provedor da conexão com uma linha de `CATEGORIAS_PROVEDOR` e resolve o `CATE_ID`. Sem correspondência, a transação fica sem categoria para a conciliação manual. Este documento só mantém o mapa; a regra de casamento é detalhada no documento `15`. | Documento 0, Observação 19 |
| 9 | **Pagamento de fatura de cartão** entra como `TransacaoBancaria` com `TRBA_FL_PAGAMENTO_FATURA = TRUE` e é **excluído** do total de gastos por categoria (Documento 0, Observação 16). Não há categoria específica para isso — a regra de exclusão é do documento do Dashboard. A categoria `CARTAO_DE_CREDITO` da carga inicial continua existindo para classificar compras, não o pagamento da fatura. | Documento 0, Observação 16 |
| 10 | **Carga inicial.** As 19 categorias equivalentes ao enum da geração 1 nascem com `CATE_FL_SISTEMA = TRUE` e `CATE_FL_ATIVO = TRUE`, via o `V1__init.sql` (Documento 0, Seção 6.4, grupo 2). A lista está na Seção 6.4 deste documento. | [RNF06](#rnf06) |
| 11 | **Grid renderizado no servidor.** O Thymeleaf renderiza o `<tbody>` das duas telas com a lista completa ([EDP01](#edp01), [EDP06](#edp06)); o DataTables inicializa sobre a tabela estática e cuida de paginação, ordenação e busca no cliente. O catálogo tem dezenas de linhas; paginação server-side seria complexidade sem ganho. O filtro do modal recarrega a lista pelo servidor (GET). | [RNF04](#rnf04) |
| 12 | **Auditoria.** `CATEGORIAS` e `CATEGORIAS_PROVEDOR` são auditadas via Hibernate Envers (`@Audited`), conforme o Documento 0. Criação, edição, desativação e exclusão lógica ficam registradas (quem, quando, o quê). | [RNF02](#rnf02) |
| 13 | **Normalização do código.** `CATE_CODIGO` é gravado em MAIÚSCULAS, sem acento, com espaços trocados por `_` (ex.: "Cartão de Crédito" → `CARTAO_DE_CREDITO`). A tela reflete a normalização em tempo real; o serviço aplica de novo antes de persistir. | [RN02](#rn02), [RT08](#rt08) |
| 14 | **Cor e ícone.** `CATE_COR` (`#RRGGBB`) alimenta os gráficos do Dashboard; `CATE_ICONE` guarda o nome de um ícone da biblioteca do projeto (Tabler). Ambos opcionais. O conjunto de ícones aceitos é decisão de front — ver Seção 17. | Documento 0, [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) |
| 15 | **Telas sem AJAX.** Premissa do projeto para CRUD simples: página renderizada no servidor, POST → redirect → GET com mensagem flash. As duas telas seguem isso: o grid vem pronto do Thymeleaf ([EDP01](#edp01), [EDP06](#edp06)); o modal de cadastro/edição abre com os dados da própria linha do grid (atributos `data-*` / `<template>`), sem `fetch` nem endpoint `/buscar/{id}`; o modal de filtro submete um GET e o servidor re-filtra a listagem. AJAX permanece apenas no combobox de categorias das telas de lançamento ([EDP05](#edp05)), consumido por outros documentos. | [RNF05](#rnf05) |

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | DESCRIÇÃO | PRIORIDADE | SITUAÇÃO |
|---|---|---|---|
| <a id="rf01"></a>RF01 | O sistema deve listar as categorias cadastradas, com: código, nome, aplica-se a, cor, ícone, quantidade de lançamentos que a usam, tipo (sistema/comum) e situação (ativa/inativa/excluída). | Alta | Em análise |
| <a id="rf02"></a>RF02 | O sistema deve permitir filtrar a listagem de categorias por texto (código/nome), aplica-se a, tipo e situação, por meio de um modal acionado pelo botão "Filtrar". | Média | Em análise |
| <a id="rf03"></a>RF03 | O sistema deve permitir cadastrar uma nova categoria via modal, com os campos: código, nome, aplica-se a, cor, ícone e situação (ativa). | Alta | Em análise |
| <a id="rf04"></a>RF04 | O sistema deve permitir editar uma categoria existente; o código só é editável em categorias que não são de sistema. | Alta | Em análise |
| <a id="rf05"></a>RF05 | O sistema deve impedir o cadastro ou a alteração de uma categoria com um código já em uso por outra categoria. | Alta | Em análise |
| <a id="rf06"></a>RF06 | O sistema deve impedir a exclusão de uma categoria de sistema e de uma categoria em uso por lançamentos, permitindo, nesses casos, apenas a desativação. | Alta | Em análise |
| <a id="rf07"></a>RF07 | O sistema deve fornecer às telas de lançamento (Receita, Despesa, Transação Bancária) a lista de categorias ativas, filtrada por aplica-se a, para o combobox de categoria. | Alta | Em análise |
| <a id="rf08"></a>RF08 | O sistema deve listar os vínculos categoria × provedor de Open Finance, com: rótulo externo, provedor e categoria do sistema. | Média | Em análise |
| <a id="rf09"></a>RF09 | O sistema deve permitir cadastrar e editar um vínculo categoria × provedor (rótulo externo + provedor → categoria). | Média | Em análise |
| <a id="rf10"></a>RF10 | O sistema deve impedir o cadastro de dois vínculos com o mesmo rótulo externo no mesmo provedor. | Média | Em análise |
| <a id="rf11"></a>RF11 | O sistema deve permitir a exclusão lógica de um vínculo categoria × provedor. | Média | Em análise |
| <a id="rf12"></a>RF12 | O sistema deve, no passo de conciliação automática das transações de Open Finance, consultar o mapa categoria × provedor para resolver a categoria de uma transação importada (regra detalhada no documento `15`). | Média | Em análise |

### 3.2 Requisitos Não Funcionais

| ID | CATEGORIA | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO |
|---|---|---|---|
| <a id="rnf01"></a>RNF01 | Segurança | Cada endpoint das telas administrativas exige a autoridade da sua operação (`PERM_CATEGORIAS_LISTAR`, `PERM_CATEGORIAS_INSERIR`, `PERM_CATEGORIAS_EDITAR`, `PERM_CATEGORIAS_EXCLUIR`, `PERM_CATEGORIAS_PROVEDOR_LISTAR`, `PERM_CATEGORIAS_PROVEDOR_INSERIR`, `PERM_CATEGORIAS_PROVEDOR_EDITAR`, `PERM_CATEGORIAS_PROVEDOR_EXCLUIR` — ver Seção 13 e [RN01](#rn01)). O combobox de categorias ([EDP05](#edp05)) exige apenas usuário autenticado. | Teste de acesso com ADMIN, com USER e com um perfil que tenha só parte das permissões. |
| <a id="rnf02"></a>RNF02 | Auditoria | `CATEGORIAS` e `CATEGORIAS_PROVEDOR` têm auditoria completa via Hibernate Envers. | Inspeção das tabelas `_aud` após operações de CRUD. |
| <a id="rnf03"></a>RNF03 | Integridade | `CATE_CODIGO` é único no banco (`uq_categorias_codigo`); o par provedor + rótulo externo é único (`uq_categorias_provedor_rotulo`). As travas de exclusão ([RN05](#rn05), [RN06](#rn06)) e as unicidades ([RN02](#rn02), [RN07](#rn07)) são validadas no serviço, não só na tela. | Teste chamando o endpoint diretamente. |
| <a id="rnf04"></a>RNF04 | Desempenho | As listagens ([EDP01](#edp01), [EDP06](#edp06)) renderizam a lista completa em menos de 1 s. O combobox de categorias ([EDP05](#edp05)) pode ser cacheado e invalidado nas gravações desta tela. | Medição em homologação. |
| <a id="rnf05"></a>RNF05 | Usabilidade | A interface segue o padrão do projeto — Thymeleaf + Tabler + DataTables, renderização server-side (sem AJAX) — e é responsiva. O modal de categoria tem seletor de cor e seletor de ícone. | Revisão visual do protótipo. |
| <a id="rnf06"></a>RNF06 | Consistência da carga inicial | As 19 categorias equivalentes ao enum da geração 1 são criadas pelo `V1__init.sql` com `CATE_FL_SISTEMA = TRUE`. | Revisão do script Flyway contra a Seção 6.4. |

---

## 4. Casos de Uso

![Casos de Uso - Manter Categoria](images/manter-categoria-casos-uso.png)

Fonte: `prototipo/manter-categoria-casos-uso.drawio` (editável) e `prototipo/_diagrama-casos-uso.html` (render).

| CÓDIGO | NOME | ATOR PRINCIPAL | DESCRIÇÃO |
|---|---|---|---|
| <a id="caus01"></a>CAUS01 | Listar Categorias | [PERF01](#perf01) | ADMIN acessa o menu e visualiza a lista de categorias. ([RF01](#rf01)) |
| <a id="caus02"></a>CAUS02 | Filtrar Categorias | [PERF01](#perf01) | ADMIN abre o modal de filtro, informa os critérios e aplica. ([RF02](#rf02)) |
| <a id="caus03"></a>CAUS03 | Cadastrar Categoria | [PERF01](#perf01) | ADMIN abre o modal de cadastro, preenche os dados e confirma. ([RF03](#rf03), [RF05](#rf05)) |
| <a id="caus04"></a>CAUS04 | Editar Categoria | [PERF01](#perf01) | ADMIN abre o modal de edição, altera os dados e confirma; o código de categoria de sistema fica bloqueado. ([RF04](#rf04), [RF05](#rf05)) |
| <a id="caus05"></a>CAUS05 | Excluir ou Desativar Categoria | [PERF01](#perf01) | ADMIN exclui logicamente uma categoria comum sem uso, ou a desativa quando ela é de sistema ou está em uso. ([RF06](#rf06)) |
| <a id="caus06"></a>CAUS06 | Selecionar Categoria num Lançamento | [PERF01](#perf01), [PERF02](#perf02) | Usuário autenticado, ao cadastrar uma receita/despesa/transação, escolhe a categoria num combobox alimentado pelas categorias ativas. ([RF07](#rf07)) |
| <a id="caus07"></a>CAUS07 | Listar Vínculos Categoria × Provedor | [PERF01](#perf01) | ADMIN acessa o menu e visualiza o mapa categoria × provedor. ([RF08](#rf08)) |
| <a id="caus08"></a>CAUS08 | Manter Vínculo Categoria × Provedor | [PERF01](#perf01) | ADMIN cadastra ou edita um vínculo rótulo externo + provedor → categoria. ([RF09](#rf09), [RF10](#rf10)) |
| <a id="caus09"></a>CAUS09 | Excluir Vínculo Categoria × Provedor | [PERF01](#perf01) | ADMIN exclui logicamente um vínculo. ([RF11](#rf11)) |
| <a id="caus10"></a>CAUS10 | Conciliar Transação por Categoria do Provedor | Sistema (job de conciliação) | O passo de conciliação automática consulta o mapa para resolver a categoria de uma transação importada. Contexto — regra no documento `15`. ([RF12](#rf12)) |

---

## 5. Localização / Critérios de Aceitação

**Caminho de Navegação:**
- Menu principal > Administração > Categorias
- Menu principal > Administração > Categorias por Provedor

**Critérios de Aceitação:**
- O menu 'Categorias' e o menu 'Categorias por Provedor' são visíveis apenas para quem tem, respectivamente, [PERM01](#perm01) e [PERM05](#perm05).
- Ao acessar cada tela, a listagem já vem renderizada pelo servidor.
- O filtro de categorias é acionado por um modal (botão "Filtrar") que, ao aplicar, submete um GET e recarrega a listagem filtrada pelo servidor; a busca do DataTables continua atuando sobre a tabela renderizada.
- O cadastro e a edição de categoria são feitos num modal único; na edição, o modal é preenchido com os dados da própria linha do grid, sem chamada ao servidor.
- O campo Código fica desabilitado ao editar uma categoria de sistema.
- Não é possível excluir uma categoria de sistema nem uma categoria em uso por lançamentos; nesses casos, a tela oferece a desativação.
- Uma categoria inativa não aparece nos comboboxes de novos lançamentos, mas os lançamentos que já a usam permanecem inalterados.
- Não é possível cadastrar dois vínculos com o mesmo rótulo externo no mesmo provedor.
- As telas de lançamento recebem apenas as categorias ativas, já filtradas por aplica-se a.

---

## 6. Banco de Dados

Toda a estrutura está no **Documento 0** (`00 - analise-geral`). Este documento **não introduz tabela nova**.

| Tabela | Onde | Papel nesta tela |
|---|---|---|
| `CATEGORIAS` | Documento 0 — [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) | CRUD + desativação |
| `CATEGORIAS_PROVEDOR` | Documento 0 — [QUADRO_DESCRITIVO_15](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-15) | CRUD do vínculo |
| `OPFI_PROVEDORES` | Documento 0 — [QUADRO_DESCRITIVO_13](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-13) | Somente leitura (combobox de provedor no vínculo) |
| `RECEITAS` / `DESPESAS` / `TRANSACOES_BANCARIAS` | Documento 0 — [QUADRO_DESCRITIVO_9](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-9), [_10](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-10), [_7](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-7) | Somente leitura (contagem de uso de uma categoria — [C5](#c5)); `CATE_ID` é FK **nullable** |

> Nenhum `ALTER TABLE` neste documento. A FK `CATE_ID` já existe em `RECEITAS`, `DESPESAS` e `TRANSACOES_BANCARIAS` (Documento 0), e a coluna é anulável — a base do comportamento discutido em [RN06](#rn06).

### 6.1 Diagrama ER

![DER - Manter Categoria](images/manter-categoria-der.png)

Subconjunto do DER do Documento 0: `CATEGORIAS`, `CATEGORIAS_PROVEDOR`, `OPFI_PROVEDORES` e as FKs `CATE_ID` (nullable) em `RECEITAS`/`DESPESAS`/`TRANSACOES_BANCARIAS`. Fonte: `prototipo/manter-categoria-der.drawio` (editável) e `prototipo/_diagrama-der.html` (render).

### 6.2 Auditoria de Tabelas

| TABELA PRINCIPAL | TABELA DE AUDITORIA | CAMPOS AUDITADOS |
|---|---|---|
| CATEGORIAS | CATEGORIAS_aud | Código, nome, aplica-se a, cor, ícone, flag de ativo, flag de sistema. Registra criação, edição, desativação e exclusão lógica |
| CATEGORIAS_PROVEDOR | CATEGORIAS_PROVEDOR_aud | Rótulo externo, categoria e provedor do vínculo. Registra criação e exclusão lógica |

### 6.3 Procedures / Views / Triggers / Functions

Nenhuma. A normalização do código, a contagem de uso e o casamento da conciliação ficam na camada de serviço.

### 6.4 Carga Inicial das Categorias de Sistema

As 19 categorias abaixo equivalem ao enum `CategoriaRegistroFinanceiro` da geração 1 (`dsc-backend`, `enums/CategoriaRegistroFinanceiro.java`). São inseridas pelo `V1__init.sql` (Documento 0, Seção 6.4, grupo 2) com `CATE_FL_SISTEMA = TRUE`, `CATE_FL_ATIVO = TRUE`, `CATE_COR` e `CATE_ICONE` nulos (ajustáveis por tela depois).

| CATE_CODIGO | CATE_NOME | CATE_APLICA_A |
|---|---|---|
| SALARIO | Salário | RECEITA |
| SALARIO_DECIMO_TERCEIRO | 13º Salário | RECEITA |
| EXTRA | Renda Extra | RECEITA |
| FERIAS | Férias | RECEITA |
| INVESTIMENTO | Investimento | RECEITA |
| MORADIA | Moradia | DESPESA |
| ALIMENTACAO | Alimentação | DESPESA |
| LAZER | Lazer | DESPESA |
| VESTUARIO | Vestuário | DESPESA |
| TRANSPORTE | Transporte | DESPESA |
| CARRO | Carro | DESPESA |
| SAUDE | Saúde | DESPESA |
| EDUCACAO | Educação | DESPESA |
| SERVICOS | Serviços | DESPESA |
| EMPRESTIMOS | Empréstimos | DESPESA |
| CARTAO_DE_CREDITO | Cartão de Crédito | DESPESA |
| TAXAS_EMPRESA | Taxas PJ | DESPESA |
| EMPRESA | Empresa | DESPESA |
| OUTRO | Outro | AMBOS |

> O agrupamento receita/despesa segue os comentários do enum da geração 1. `INVESTIMENTO` (como `RECEITA`) e `OUTRO` (como `AMBOS`) são pontos a confirmar — ver Seção 17.

---

## 7. Protótipos de Interface

Protótipo navegável: `prototipo/manter-categoria-prototipo.html`. Wireframes editáveis: `prototipo/manter-categoria-prototipo.drawio` (5 páginas, 7.1 a 7.5). PNGs regeráveis por `prototipo/render-pngs.py`. Os números em destaque nas telas correspondem aos IDs dos itens do respectivo QUADRO_DESCRITIVO.

### <a id="quadro-descritivo-1"></a>7.1 Tela: Categorias (Listagem) — QUADRO_DESCRITIVO_1

![Categorias - Listagem](images/mc-tela-1.png)

> OBSERVAÇÕES: Tela acessada via 'Administração > Categorias'. Restrita a quem tem [PERM01](#perm01). Grid renderizado no servidor. O filtro é acionado por um modal (botão "Filtrar") e recarrega a listagem por GET.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd1-0"></a>0 | LINK | Caminho: "/categorias/listar" | — |
| <a id="qdd1-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Administração > Categorias | — |
| <a id="qdd1-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Categorias | — |
| <a id="qdd1-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Gerencie as categorias de receitas e despesas do sistema. | — |
| <a id="qdd1-4"></a>4 | BOTÃO FILTRAR | Tipo: Botão<br>Texto: Filtrar<br>Ícone: filter | Ao clicar, executar [RT01](#rt01). |
| <a id="qdd1-5"></a>5 | BOTÃO NOVA CATEGORIA | Tipo: Botão (primário)<br>Texto: Nova categoria<br>Ícone: plus | Visível a quem tem [PERM02](#perm02). Ao clicar, executar [RT04](#rt04). |
| <a id="qdd1-6"></a>6 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side sobre tabela renderizada no servidor)<br>Colunas: [ID7](#qdd1-7)…[ID14](#qdd1-14)<br>Itens por página: 10, 25, 50<br>Ordenação padrão: Nome crescente<br>Endpoint: [EDP01](#edp01) | O `<tbody>` é renderizado pelo servidor com o resultado de [C1](#c1). O DataTables cuida de paginação, ordenação e busca no cliente. O filtro do modal recarrega a página por GET ([RT02](#rt02)). |
| <a id="qdd1-7"></a>7 | CÓDIGO | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).codigo. |
| <a id="qdd1-8"></a>8 | NOME | Tipo: Coluna<br>Ordenação: Sim | Exibe [C1](#c1).nome. Precedido do ícone ([C1](#c1).icone) e de um marcador na cor ([C1](#c1).cor), quando houver. |
| <a id="qdd1-9"></a>9 | APLICA-SE A | Tipo: Coluna (badge)<br>Ordenação: Sim | Receita / Despesa / Ambos, de [C1](#c1).aplicaA. |
| <a id="qdd1-10"></a>10 | Nº DE LANÇAMENTOS | Tipo: Coluna (número)<br>Ordenação: Sim | Exibe [C1](#c1).qtdUso — soma de receitas, despesas e transações não excluídas que usam a categoria. |
| <a id="qdd1-11"></a>11 | TIPO | Tipo: Coluna (badge)<br>Ordenação: Sim | "Sistema" quando `CATE_FL_SISTEMA`; "Comum" caso contrário. |
| <a id="qdd1-12"></a>12 | SITUAÇÃO | Tipo: Coluna (badge)<br>Ordenação: Sim | "Ativa" (verde) quando `CATE_FL_ATIVO` e sem `audit_data_exclusao`; "Inativa" (cinza) quando `CATE_FL_ATIVO = FALSE`; "Excluída" quando há `audit_data_exclusao`. |
| <a id="qdd1-13"></a>13 | AÇÃO | Tipo: Coluna (alinhada à esquerda) | Visível a quem tem [PERM03](#perm03) ou [PERM04](#perm04). Ícones [ID14](#qdd1-14). |
| <a id="qdd1-14"></a>14 | ÍCONES DE AÇÃO | Tipo: Ícones<br>Editar (ícone: edit, tooltip: Editar categoria)<br>Excluir (ícone: trash, tooltip: Excluir categoria) | Editar → [RT05](#rt05), visível com [PERM03](#perm03). Excluir → [RT07](#rt07), visível com [PERM04](#perm04); oculto quando a categoria já está excluída. |

### <a id="quadro-descritivo-2"></a>7.2 Modal: Filtrar Categorias — QUADRO_DESCRITIVO_2

![Modal Filtrar Categorias](images/mc-tela-2.png)

> OBSERVAÇÕES: Todos os campos são opcionais. Ao aplicar, o modal submete um GET e o servidor re-renderiza a listagem filtrada ([RT02](#rt02)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd2-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Filtrar Categorias | — |
| <a id="qdd2-2"></a>2 | FILTRO – BUSCA | Tipo: Input Text<br>Obrigatório: Não<br>Placeholder: Código ou nome<br>Tooltip: Filtre por parte do código ou do nome. | Filtro parcial e sem acento sobre código e nome. |
| <a id="qdd2-3"></a>3 | FILTRO – APLICA-SE A | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todos<br>Domínio: Todos / Receita / Despesa / Ambos | Filtra por [C1](#c1).aplicaA. Ver [SB02](#sb02). |
| <a id="qdd2-4"></a>4 | FILTRO – TIPO | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todos<br>Domínio: Todos / Sistema / Comum | Filtra por `CATE_FL_SISTEMA`. Ver [SB03](#sb03). |
| <a id="qdd2-5"></a>5 | FILTRO – SITUAÇÃO | Tipo: Combobox<br>Obrigatório: Não<br>Valor default: Ativa<br>Domínio: Ativa / Inativa / Excluída / Todas | Filtra por `CATE_FL_ATIVO` e pela presença de `audit_data_exclusao`. Ver [SB04](#sb04). |
| <a id="qdd2-6"></a>6 | BOTÃO APLICAR | Tipo: Botão<br>Texto: Aplicar | Ao clicar, executar [RT02](#rt02). |
| <a id="qdd2-7"></a>7 | BOTÃO LIMPAR | Tipo: Botão<br>Texto: Limpar | Ao clicar, executar [RT03](#rt03). |

### <a id="quadro-descritivo-3"></a>7.3 Modal: Cadastro / Edição de Categoria — QUADRO_DESCRITIVO_3

![Modal Cadastro / Edição de Categoria](images/mc-tela-3.png)

> OBSERVAÇÕES: Modal único de cadastro e edição. O botão Nova categoria exige [PERM02](#perm02); o ícone Editar exige [PERM03](#perm03). Na edição, o modal é preenchido com os dados da própria linha do grid (atributos `data-*`), sem chamada ao servidor. Ao editar uma categoria de sistema, o campo Código fica desabilitado ([RN04](#rn04)) e não há como alterar o tipo. O campo Situação (ativa) só aparece na edição.

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd3-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Nova categoria / Editar categoria | Varia conforme o modo. |
| <a id="qdd3-2"></a>2 | CAMPO – CÓDIGO | Tipo: Input Text<br>Tamanho: 40<br>Obrigatório: Sim | Grava `CATE_CODIGO`. Normalizado ([RT08](#rt08)). Único ([RN02](#rn02)). **Desabilitado** ao editar categoria de sistema ([RN04](#rn04)). |
| <a id="qdd3-3"></a>3 | CAMPO – NOME | Tipo: Input Text<br>Tamanho: 100<br>Obrigatório: Sim | Grava `CATE_NOME`. Nome de exibição. |
| <a id="qdd3-4"></a>4 | CAMPO – APLICA-SE A | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: Receita / Despesa / Ambos | Grava `CATE_APLICA_A`. Ver [SB01](#sb01) e [RN03](#rn03). |
| <a id="qdd3-5"></a>5 | CAMPO – COR | Tipo: Seletor de cor (input color)<br>Obrigatório: Não | Grava `CATE_COR` (`#RRGGBB`). Usada nos gráficos do Dashboard. |
| <a id="qdd3-6"></a>6 | CAMPO – ÍCONE | Tipo: Seletor de ícone<br>Obrigatório: Não | Grava `CATE_ICONE` (nome do ícone Tabler). Ver [SB05](#sb05). |
| <a id="qdd3-7"></a>7 | CAMPO – ATIVA | Tipo: Toggle (Sim/Não)<br>Valor default: Sim<br>Exibição: só no modo edição | Grava `CATE_FL_ATIVO`. Ver [RN09](#rn09). |
| <a id="qdd3-8"></a>8 | AVISO – CATEGORIA EM USO | Tipo: Texto informativo | Exibido no modo edição quando [C1](#c1).qtdUso > 0: "Esta categoria é usada por {n} lançamento(s). Ela não pode ser excluída; você pode desativá-la." |
| <a id="qdd3-9"></a>9 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar<br>Endpoint: [EDP02](#edp02) (criação) ou [EDP03](#edp03) (edição) | Ao clicar, executar [RT06](#rt06). |
| <a id="qdd3-10"></a>10 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### <a id="quadro-descritivo-4"></a>7.4 Tela: Categorias por Provedor (Listagem) — QUADRO_DESCRITIVO_4

![Categorias por Provedor - Listagem](images/mc-tela-4.png)

> OBSERVAÇÕES: Tela acessada via 'Administração > Categorias por Provedor'. Restrita a quem tem [PERM05](#perm05). Grid renderizado no servidor. Mantém o mapa que a conciliação automática de Open Finance consulta ([Observação 8](#2-observações)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd4-0"></a>0 | LINK | Caminho: "/categorias-provedor/listar" | — |
| <a id="qdd4-1"></a>1 | BREADCRUMB | Tipo: Texto<br>Texto: Administração > Categorias por Provedor | — |
| <a id="qdd4-2"></a>2 | TÍTULO DA TELA | Tipo: Texto<br>Texto: Categorias por Provedor | — |
| <a id="qdd4-3"></a>3 | DESCRIÇÃO | Tipo: Texto<br>Texto: Associe os rótulos de categoria de cada provedor de Open Finance a uma categoria do sistema. | — |
| <a id="qdd4-4"></a>4 | FILTRO – PROVEDOR | Tipo: Combobox<br>Obrigatório: Não<br>Placeholder: Todos os provedores<br>Domínio: "Todos" + provedores de `OPFI_PROVEDORES` | Ao selecionar, submeter um GET e recarregar a listagem filtrada pelo servidor ([RT02](#rt02)). Ver [SB06](#sb06). |
| <a id="qdd4-5"></a>5 | BOTÃO NOVO VÍNCULO | Tipo: Botão (primário)<br>Texto: Novo vínculo<br>Ícone: plus | Visível a quem tem [PERM06](#perm06). Ao clicar, executar [RT10](#rt10). |
| <a id="qdd4-6"></a>6 | GRID DE LISTAGEM | Tipo: Grid (DataTables, client-side sobre tabela renderizada no servidor)<br>Colunas: [ID7](#qdd4-7)…[ID10](#qdd4-10)<br>Ordenação padrão: Provedor, depois Rótulo externo<br>Endpoint: [EDP06](#edp06) | O `<tbody>` é renderizado pelo servidor com o resultado de [C3](#c3). O filtro de provedor recarrega a página por GET ([RT02](#rt02)). |
| <a id="qdd4-7"></a>7 | PROVEDOR | Tipo: Coluna<br>Ordenação: Sim | Exibe [C3](#c3).provedorNome. |
| <a id="qdd4-8"></a>8 | RÓTULO EXTERNO | Tipo: Coluna<br>Ordenação: Sim | Exibe [C3](#c3).rotuloExterno — a categoria como o provedor a nomeia. |
| <a id="qdd4-9"></a>9 | CATEGORIA DO SISTEMA | Tipo: Coluna (badge)<br>Ordenação: Sim | Exibe [C3](#c3).categoriaNome. |
| <a id="qdd4-10"></a>10 | AÇÃO | Tipo: Coluna (alinhada à esquerda) | Ícone Editar → [RT11](#rt11), visível com [PERM07](#perm07). Ícone Excluir → [RT13](#rt13), visível com [PERM08](#perm08). |

### <a id="quadro-descritivo-5"></a>7.5 Modal: Cadastro / Edição de Vínculo Categoria × Provedor — QUADRO_DESCRITIVO_5

![Modal Cadastro / Edição de Vínculo Categoria × Provedor](images/mc-tela-5.png)

> OBSERVAÇÕES: Modal único de cadastro e edição. O botão Novo vínculo exige [PERM06](#perm06); o ícone Editar exige [PERM07](#perm07). Os comboboxes de Provedor e Categoria são renderizados pelo servidor na carga da página. Na edição, o modal é preenchido com os dados da própria linha do grid (atributos `data-*`), sem chamada ao servidor. O par provedor + rótulo externo é único ([RN07](#rn07)).

| ID | NOME | PROPRIEDADES | OBSERVAÇÕES |
|---|---|---|---|
| <a id="qdd5-1"></a>1 | TÍTULO DO MODAL | Tipo: Texto<br>Texto: Novo vínculo / Editar vínculo | Varia conforme o modo. |
| <a id="qdd5-2"></a>2 | CAMPO – PROVEDOR | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: provedores ativos de `OPFI_PROVEDORES` | Grava `OFPV_ID`. Ver [SB06](#sb06). |
| <a id="qdd5-3"></a>3 | CAMPO – RÓTULO EXTERNO | Tipo: Input Text<br>Tamanho: 120<br>Obrigatório: Sim | Grava `CAPR_ROTULO_EXTERNO` — o texto exato da categoria como o provedor a devolve. Único no provedor ([RN07](#rn07)). |
| <a id="qdd5-4"></a>4 | CAMPO – CATEGORIA DO SISTEMA | Tipo: Combobox<br>Obrigatório: Sim<br>Domínio: categorias ativas | Grava `CATE_ID`. Ver [SB07](#sb07) e [RN10](#rn10). |
| <a id="qdd5-5"></a>5 | BOTÃO SALVAR | Tipo: Botão (primário)<br>Texto: Salvar<br>Endpoint: [EDP07](#edp07) (criação) ou [EDP08](#edp08) (edição) | Ao clicar, executar [RT12](#rt12). |
| <a id="qdd5-6"></a>6 | BOTÃO CANCELAR | Tipo: Botão<br>Texto: Cancelar | Fecha sem salvar. |

### 7.6 Suggestion Boxes

| ID | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="sb01"></a>SB01 | APLICA-SE A | Domínio fixo do enum de negócio (Receita, Despesa, Ambos). Renderizado como combobox; não é entidade. |
| <a id="sb02"></a>SB02 | FILTRO APLICA-SE A | Domínio fixo do próprio filtro: Todos, Receita, Despesa, Ambos. |
| <a id="sb03"></a>SB03 | FILTRO TIPO | Domínio fixo do próprio filtro: Todos, Sistema, Comum. |
| <a id="sb04"></a>SB04 | FILTRO SITUAÇÃO | Domínio fixo do próprio filtro: Ativa, Inativa, Excluída, Todas. |
| <a id="sb05"></a>SB05 | ÍCONE | Lista de ícones da biblioteca do projeto (Tabler), carregada de um recurso estático de front. Não é entidade. Conjunto aceito a confirmar (Seção 17). |
| <a id="sb06"></a>SB06 | PROVEDOR | Itens de `OPFI_PROVEDORES` (Documento 0, [QUADRO_DESCRITIVO_13](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-13)), obtidos por [C7](#c7) e renderizados pelo servidor na carga da página ([EDP06](#edp06)), ordenados por nome. No filtro do grid ([ID4](#qdd4-4)), a opção "Todos os provedores" é adicional. No modal de vínculo, só provedores ativos. Nunca `<option>` fixo no HTML. |
| <a id="sb07"></a>SB07 | CATEGORIA DO SISTEMA | Itens de `CATEGORIAS` (todas as ativas, sem filtro de aplica-se a), obtidos por [C2](#c2) e renderizados pelo servidor na carga da página ([EDP06](#edp06)), ordenados por nome. Nunca `<option>` fixo no HTML. |

### 7.7 Regras de Tela

| ID | DESCRIÇÃO |
|---|---|
| <a id="rt01"></a>RT01 | Ao clicar em "Filtrar" ([ID4](#qdd1-4)), abrir o modal de filtro ([QUADRO_DESCRITIVO_2](#quadro-descritivo-2)) com os valores da query string atual. |
| <a id="rt02"></a>RT02 | Ao clicar em "Aplicar" ([ID6](#qdd2-6)), submeter um GET para `/categorias/listar` com os campos preenchidos como query string (`busca`, `aplicaA`, `tipo`, `situacao`); o servidor re-renderiza a listagem já filtrada por [C1](#c1). A busca do DataTables continua atuando sobre a tabela renderizada. Se o servidor não retornar linhas, exibir [MSG09](#msg09) na área do grid. O filtro de provedor da tela de Categorias por Provedor ([ID4](#qdd4-4)) segue o mesmo desenho, com GET para `/categorias-provedor/listar?provedorId=`. |
| <a id="rt03"></a>RT03 | Ao clicar em "Limpar" ([ID7](#qdd2-7)), voltar Busca, Aplica-se a e Tipo para vazio e Situação para "Ativa", e submeter o GET conforme [RT02](#rt02). |
| <a id="rt04"></a>RT04 | Ao clicar em "Nova categoria" ([ID5](#qdd1-5)) — visível só com [PERM02](#perm02) —, abrir o modal ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)) em modo criação: campos vazios, Aplica-se a sem seleção, sem o campo Ativa. |
| <a id="rt05"></a>RT05 | Ao clicar no ícone Editar ([ID14](#qdd1-14)) — visível só com [PERM03](#perm03) —, abrir o modal ([QUADRO_DESCRITIVO_3](#quadro-descritivo-3)) em modo edição preenchido com os dados da própria linha do grid (atributos `data-*`), sem chamada ao servidor. Se a categoria for de sistema, o campo Código fica desabilitado ([RN04](#rn04)). Se [C1](#c1).qtdUso > 0, exibir o aviso [ID8](#qdd3-8). |
| <a id="rt06"></a>RT06 | Ao clicar em "Salvar" ([ID9](#qdd3-9)): validar Código, Nome e Aplica-se a obrigatórios ([MSG02](#msg02)) e aplicar [RT08](#rt08). Em criação, submeter POST para [EDP02](#edp02); em edição, POST com `_method=PUT` para [EDP03](#edp03). Em erro de validação ou de negócio, o servidor re-renderiza a página com o modal reaberto e as mensagens no HTML ([MSG02](#msg02), [MSG03](#msg03) ou [MSG05](#msg05)). Em sucesso, redirect para [EDP01](#edp01) com mensagem flash: [MSG01](#msg01) (criação) ou [MSG04](#msg04) (edição). |
| <a id="rt07"></a>RT07 | Ao clicar no ícone Excluir ([ID14](#qdd1-14)) — visível só com [PERM04](#perm04) —, exibir a confirmação [MSG07](#msg07). Ao confirmar, submeter POST com `_method=DELETE` para [EDP04](#edp04). Categoria de sistema → redirect para [EDP01](#edp01) com flash [MSG05](#msg05); categoria em uso → redirect para [EDP01](#edp01) com flash [MSG06](#msg06), que oferece a ação Desativar (POST com `_method=PUT` para [EDP03](#edp03) apenas com `CATE_FL_ATIVO = FALSE`). Em sucesso da exclusão, redirect para [EDP01](#edp01) com flash [MSG08](#msg08). |
| <a id="rt08"></a>RT08 | Ao digitar no campo Código ([ID2](#qdd3-2)), normalizar em tempo real: maiúsculas, remoção de acento, espaços e hífens trocados por `_`, e remoção dos demais caracteres não alfanuméricos. |
| <a id="rt09"></a>RT09 | Ao selecionar uma cor ([ID5](#qdd3-5)) ou um ícone ([ID6](#qdd3-6)), exibir a pré-visualização do par cor + ícone ao lado do campo Nome. |
| <a id="rt10"></a>RT10 | Ao clicar em "Novo vínculo" ([ID5](#qdd4-5)) — visível só com [PERM06](#perm06) —, abrir o modal ([QUADRO_DESCRITIVO_5](#quadro-descritivo-5)) em modo criação: os comboboxes de Provedor ([SB06](#sb06)) e Categoria ([SB07](#sb07)) já vêm renderizados pelo servidor; campos vazios. |
| <a id="rt11"></a>RT11 | Ao clicar no ícone Editar ([ID10](#qdd4-10)) — visível só com [PERM07](#perm07) —, abrir o modal ([QUADRO_DESCRITIVO_5](#quadro-descritivo-5)) em modo edição preenchido com os dados da própria linha do grid (atributos `data-*`), sem chamada ao servidor. |
| <a id="rt12"></a>RT12 | Ao clicar em "Salvar" ([ID5](#qdd5-5)): validar Provedor, Rótulo externo e Categoria obrigatórios ([MSG02](#msg02)). Em criação, submeter POST para [EDP07](#edp07); em edição, POST com `_method=PUT` para [EDP08](#edp08). Em erro, o servidor re-renderiza a listagem com o modal reaberto e as mensagens no HTML ([MSG02](#msg02) ou [MSG11](#msg11)). Em sucesso, redirect para [EDP06](#edp06) com mensagem flash: [MSG10](#msg10) (criação) ou [MSG12](#msg12) (edição). |
| <a id="rt13"></a>RT13 | Ao clicar no ícone Excluir ([ID10](#qdd4-10)) — visível só com [PERM08](#perm08) —, exibir a confirmação [MSG13](#msg13). Ao confirmar, submeter POST com `_method=DELETE` para [EDP09](#edp09). Em sucesso, redirect para [EDP06](#edp06) com flash [MSG14](#msg14). |

---

## 8. Endpoints

As duas telas são renderizadas no servidor (ver [Observação 15](#2-observações)): a página já traz o grid preenchido pelo Thymeleaf e o DataTables inicializa sobre a tabela estática. Não há endpoint JSON de listagem, de busca por id nem retorno `{sucesso:true}`/422 — cadastro, edição e exclusão respondem com `redirect` + mensagem flash em caso de sucesso e re-renderizam o HTML com os erros de campo/negócio em caso de falha. O único endpoint JSON é [EDP05](#edp05), consumido por outros documentos.

| CÓDIGO | HTTP | PERMISSÃO | PATH | FINALIZADO? |
|---|---|---|---|---|
| <a id="edp01"></a>EDP01 | GET | [PERM01](#perm01) | /categorias/listar | N |
| Página da listagem de categorias (Thymeleaf). Executa [C1](#c1) com os filtros opcionais da query string (`busca`, `aplicaA`, `tipo`, `situacao`) e coloca a lista no `Model`; o `<tbody>` é renderizado pelo servidor. Sem paginação server-side (o DataTables pagina no cliente). | | | | |
| <a id="edp02"></a>EDP02 | POST | [PERM02](#perm02) | /categorias/inserir | N |
| Cria uma categoria. Executa [RN02](#rn02), [RN03](#rn03). Dados (form): codigo, nome, aplicaA, cor, icone. `CATE_FL_ATIVO = TRUE` e `CATE_FL_SISTEMA = FALSE` fixos. Invalida o cache do combobox de categorias ([RNF04](#rnf04)). Sucesso: `redirect:/categorias/listar` + flash [MSG01](#msg01). Erro: re-renderiza a página com o modal reaberto e os erros no HTML ([MSG02](#msg02), [MSG03](#msg03)). | | | | |
| <a id="edp03"></a>EDP03 | POST (`_method=PUT`) | [PERM03](#perm03) | /categorias/editar/{id} | N |
| Edita uma categoria. Executa [RN02](#rn02), [RN03](#rn03), [RN04](#rn04), [RN09](#rn09). Dados (form): codigo, nome, aplicaA, cor, icone, ativo. Invalida o cache do combobox de categorias ([RNF04](#rnf04)). Sucesso: `redirect:/categorias/listar` + flash [MSG04](#msg04). Erro: re-renderiza a página com o modal reaberto e os erros no HTML ([MSG02](#msg02), [MSG03](#msg03), [MSG05](#msg05)). | | | | |
| <a id="edp04"></a>EDP04 | POST (`_method=DELETE`) | [PERM04](#perm04) | /categorias/excluir/{id} | N |
| Exclusão lógica da categoria. Executa [RN05](#rn05), [RN06](#rn06). Preenche `audit_data_exclusao` / `audit_excluido_por`. Invalida o cache do combobox de categorias ([RNF04](#rnf04)). Sucesso: `redirect:/categorias/listar` + flash [MSG08](#msg08). Bloqueio: `redirect:/categorias/listar` + flash [MSG05](#msg05) (sistema) ou [MSG06](#msg06) (em uso). | | | | |
| <a id="edp05"></a>EDP05 | GET | Autenticado | /categorias/opcoes?aplicaA= | N |
| Retorna as categorias **ativas** para o combobox das telas de lançamento, em JSON. Executa [C2](#c2). O parâmetro `aplicaA` (RECEITA / DESPESA) é opcional: quando informado, devolve as categorias com esse valor mais as `AMBOS`; quando omitido, todas as ativas. Campos: id, codigo, nome, cor, icone. Consumido pelas telas de Receita, Despesa e Transação Bancária. Pode ser cacheado e invalidado nas gravações desta tela. | | | | |
| <a id="edp06"></a>EDP06 | GET | [PERM05](#perm05) | /categorias-provedor/listar | N |
| Página da listagem de vínculos categoria × provedor (Thymeleaf). Executa [C3](#c3), filtrando pelo `provedorId` opcional da query string, e coloca a lista no `Model`; o `<tbody>` é renderizado pelo servidor. A página também carrega as opções de provedor ([C7](#c7)) e de categoria ativa ([C2](#c2)) para os comboboxes do filtro e do modal. Sem paginação server-side. | | | | |
| <a id="edp07"></a>EDP07 | POST | [PERM06](#perm06) | /categorias-provedor/inserir | N |
| Cria um vínculo. Executa [RN07](#rn07), [RN10](#rn10). Dados (form): rotuloExterno, provedorId, categoriaId. Sucesso: `redirect:/categorias-provedor/listar` + flash [MSG10](#msg10). Erro: re-renderiza a página com o modal reaberto e os erros no HTML ([MSG02](#msg02), [MSG11](#msg11)). | | | | |
| <a id="edp08"></a>EDP08 | POST (`_method=PUT`) | [PERM07](#perm07) | /categorias-provedor/editar/{id} | N |
| Edita um vínculo. Executa [RN07](#rn07), [RN10](#rn10). Dados (form): rotuloExterno, provedorId, categoriaId. Sucesso: `redirect:/categorias-provedor/listar` + flash [MSG12](#msg12). Erro: re-renderiza a página com o modal reaberto e os erros no HTML ([MSG02](#msg02), [MSG11](#msg11)). | | | | |
| <a id="edp09"></a>EDP09 | POST (`_method=DELETE`) | [PERM08](#perm08) | /categorias-provedor/excluir/{id} | N |
| Exclusão lógica do vínculo. Executa [RN08](#rn08). Preenche `audit_data_exclusao` / `audit_excluido_por`. Sucesso: `redirect:/categorias-provedor/listar` + flash [MSG14](#msg14). | | | | |

---

## 9. Regras de Negócio

| ID | DESCRIÇÃO |
|---|---|
| <a id="rn01"></a>RN01 | Cada endpoint exige a autoridade da sua operação: [EDP01](#edp01) → `PERM_CATEGORIAS_LISTAR`; [EDP02](#edp02) → `PERM_CATEGORIAS_INSERIR`; [EDP03](#edp03) → `PERM_CATEGORIAS_EDITAR`; [EDP04](#edp04) → `PERM_CATEGORIAS_EXCLUIR`; [EDP06](#edp06) → `PERM_CATEGORIAS_PROVEDOR_LISTAR`; [EDP07](#edp07) → `PERM_CATEGORIAS_PROVEDOR_INSERIR`; [EDP08](#edp08) → `PERM_CATEGORIAS_PROVEDOR_EDITAR`; [EDP09](#edp09) → `PERM_CATEGORIAS_PROVEDOR_EXCLUIR`. [EDP05](#edp05) exige apenas usuário autenticado. As autoridades são resolvidas pelo `getAuthorities()` do `Usuario` a partir do perfil e das permissões vinculadas em `PERFIL_PERMISSAO`. `CATEGORIAS_INSERIR`, `CATEGORIAS_EDITAR` e `CATEGORIAS_EXCLUIR` pressupõem `CATEGORIAS_LISTAR` (sem a listagem não há tela nem menu); o mesmo entre as permissões de Categorias por Provedor. Ainda assim cada permissão é concedida à parte e aparece isolada na matriz da Seção 13.1. |
| <a id="rn02"></a>RN02 | `CATE_CODIGO` é único entre categorias não excluídas. Antes de comparar, o serviço normaliza o valor recebido: maiúsculas, sem acento, espaços e hífens trocados por `_`, demais caracteres não alfanuméricos removidos. Ao criar ([EDP02](#edp02)) ou editar ([EDP03](#edp03)), se o código normalizado já pertencer a **outra** categoria, impedir e retornar [MSG03](#msg03). Executa [C4](#c4). |
| <a id="rn03"></a>RN03 | `CATE_APLICA_A` é obrigatório e deve ser `RECEITA`, `DESPESA` ou `AMBOS`. Ele determina em quais telas de lançamento a categoria aparece ([EDP05](#edp05)): `RECEITA` e `AMBOS` na tela de Receita; `DESPESA` e `AMBOS` na de Despesa; todas na de Transação Bancária. |
| <a id="rn04"></a>RN04 | Categoria de sistema (`CATE_FL_SISTEMA = TRUE`): `CATE_CODIGO` e `CATE_FL_SISTEMA` **não** são alteráveis por [EDP03](#edp03) — qualquer valor divergente enviado é ignorado; se a intenção explícita for mudar o código, retornar [MSG05](#msg05). Nome, aplica-se a, cor, ícone e situação (ativa/inativa) **são** editáveis. Mesma regra do "perfil de sistema" (documento `02 - manter-perfil-permissao`, RN02). |
| <a id="rn05"></a>RN05 | Exclusão de categoria ([EDP04](#edp04)): recusar se `CATE_FL_SISTEMA = TRUE` e retornar [MSG05](#msg05). A categoria de sistema só pode ser desativada. |
| <a id="rn06"></a>RN06 | Exclusão de categoria ([EDP04](#edp04)): recusar se existir **qualquer** lançamento não excluído (`RECEITAS`, `DESPESAS` ou `TRANSACOES_BANCARIAS`) com `CATE_ID` apontando para a categoria — executa [C5](#c5) — e retornar [MSG06](#msg06). A alternativa oferecida é a desativação ([RN09](#rn09)). O comportamento é controlado pelo parâmetro `CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO` (Seção 12): quando `false`, a exclusão é permitida e os lançamentos afetados ficam com `CATE_ID` nulo. Ver a decisão em aberto na Seção 17. |
| <a id="rn07"></a>RN07 | O par `OFPV_ID` + `CAPR_ROTULO_EXTERNO` é único entre vínculos não excluídos (constraint `uq_categorias_provedor_rotulo`). Ao criar ([EDP07](#edp07)) ou editar ([EDP08](#edp08)), se já existir outro vínculo com o mesmo rótulo no mesmo provedor, impedir e retornar [MSG11](#msg11). Executa [C6](#c6). |
| <a id="rn08"></a>RN08 | Exclusão de vínculo categoria × provedor ([EDP09](#edp09)) é sempre lógica (`audit_data_exclusao` / `audit_excluido_por`). Nenhuma trava adicional — o vínculo não é referenciado por FK de outra tabela. |
| <a id="rn09"></a>RN09 | Categoria inativa (`CATE_FL_ATIVO = FALSE`): não é devolvida por [EDP05](#edp05) e não pode ser escolhida em novos vínculos de provedor ([RN10](#rn10)). Os lançamentos que já a referenciam permanecem inalterados, e ela continua contando nos relatórios e na conciliação. Reativar é apenas voltar `CATE_FL_ATIVO = TRUE` por [EDP03](#edp03). |
| <a id="rn10"></a>RN10 | Um vínculo categoria × provedor ([EDP07](#edp07)/[EDP08](#edp08)) só aceita `CATE_ID` de categoria ativa e não excluída. Categoria inexistente, inativa ou excluída → [MSG02](#msg02) no campo Categoria. |
| <a id="rn11"></a>RN11 | Na conciliação automática (documento `15`), ao processar uma transação de *staging* com `OFTR_CATEGORIA_EXTERNA` preenchida, o sistema busca em `CATEGORIAS_PROVEDOR` a linha cujo `OFPV_ID` é o provedor da conexão e cujo `CAPR_ROTULO_EXTERNO` casa com `OFTR_CATEGORIA_EXTERNA`, e usa o `CATE_ID` dela no lançamento gerado. Sem correspondência, o lançamento é criado sem categoria, para a conciliação manual resolver. Esta regra é **consumidora** do mapa; a manutenção do mapa é o escopo deste documento. |

---

## 10. Mensagens de Sistema

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="msg01"></a>MSG01 | Categoria cadastrada com sucesso. |
| <a id="msg02"></a>MSG02 | O campo {campo} é obrigatório. |
| <a id="msg03"></a>MSG03 | Já existe uma categoria com este código. |
| <a id="msg04"></a>MSG04 | Categoria atualizada com sucesso. |
| <a id="msg05"></a>MSG05 | Categorias de sistema não podem ser excluídas nem ter o código alterado. |
| <a id="msg06"></a>MSG06 | Esta categoria é usada por lançamentos e não pode ser excluída. Desative-a para ocultá-la de novos lançamentos. |
| <a id="msg07"></a>MSG07 | Confirma a exclusão da categoria "{nome}"? |
| <a id="msg08"></a>MSG08 | Categoria excluída com sucesso. |
| <a id="msg09"></a>MSG09 | Nenhuma categoria encontrada com os filtros informados. |
| <a id="msg10"></a>MSG10 | Vínculo de categoria por provedor cadastrado com sucesso. |
| <a id="msg11"></a>MSG11 | Já existe um vínculo para este rótulo neste provedor. |
| <a id="msg12"></a>MSG12 | Vínculo atualizado com sucesso. |
| <a id="msg13"></a>MSG13 | Confirma a exclusão do vínculo "{rotuloExterno}" → "{categoria}"? |
| <a id="msg14"></a>MSG14 | Vínculo excluído com sucesso. |

---

## 11. Consultas

| CÓDIGO | DESCRIÇÃO |
|---|---|
| <a id="c1"></a>C1 | Listagem de categorias para o grid ([EDP01](#edp01)), com a contagem de uso. Os filtros do modal (`busca`, `aplicaA`, `tipo`, `situacao`) entram como predicados opcionais no `WHERE`.<br>`SELECT c.CATE_ID, c.CATE_CODIGO, c.CATE_NOME, c.CATE_APLICA_A, c.CATE_COR, c.CATE_ICONE,`<br>`       c.CATE_FL_SISTEMA, c.CATE_FL_ATIVO,`<br>`       (c.audit_data_exclusao IS NOT NULL) AS excluido,`<br>`       ((SELECT COUNT(*) FROM RECEITAS r WHERE r.CATE_ID = c.CATE_ID AND r.audit_data_exclusao IS NULL)`<br>`      + (SELECT COUNT(*) FROM DESPESAS d WHERE d.CATE_ID = c.CATE_ID AND d.audit_data_exclusao IS NULL)`<br>`      + (SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t WHERE t.CATE_ID = c.CATE_ID AND t.audit_data_exclusao IS NULL)) AS qtd_uso`<br>`FROM CATEGORIAS c`<br>`ORDER BY c.CATE_NOME ASC;` |
| <a id="c2"></a>C2 | Categorias ativas para o combobox das telas de lançamento ([EDP05](#edp05)) e para os comboboxes de categoria renderizados pela tela de Categorias por Provedor ([EDP06](#edp06)).<br>`SELECT c.CATE_ID, c.CATE_CODIGO, c.CATE_NOME, c.CATE_COR, c.CATE_ICONE`<br>`FROM CATEGORIAS c`<br>`WHERE c.audit_data_exclusao IS NULL`<br>`  AND c.CATE_FL_ATIVO = TRUE`<br>`  AND (:aplicaA IS NULL OR c.CATE_APLICA_A = :aplicaA OR c.CATE_APLICA_A = 'AMBOS')`<br>`ORDER BY c.CATE_NOME ASC;` |
| <a id="c3"></a>C3 | Listagem dos vínculos categoria × provedor para o grid ([EDP06](#edp06)), com o predicado opcional `OFPV_ID = :provedorId` do filtro.<br>`SELECT cp.CAPR_ID, cp.CAPR_ROTULO_EXTERNO,`<br>`       p.OFPV_ID, p.OFPV_NOME,`<br>`       c.CATE_ID, c.CATE_NOME`<br>`FROM CATEGORIAS_PROVEDOR cp`<br>`JOIN OPFI_PROVEDORES p ON p.OFPV_ID = cp.OFPV_ID`<br>`JOIN CATEGORIAS c ON c.CATE_ID = cp.CATE_ID`<br>`WHERE cp.audit_data_exclusao IS NULL`<br>`ORDER BY p.OFPV_NOME ASC, cp.CAPR_ROTULO_EXTERNO ASC;` |
| <a id="c4"></a>C4 | Verifica código de categoria duplicado (RN02). O serviço passa o código já normalizado.<br>`SELECT COUNT(*) FROM CATEGORIAS c`<br>`WHERE c.audit_data_exclusao IS NULL`<br>`  AND c.CATE_CODIGO = :codigo`<br>`  AND (:idAtual IS NULL OR c.CATE_ID <> :idAtual);` |
| <a id="c5"></a>C5 | Conta os lançamentos não excluídos que usam a categoria (RN06).<br>`SELECT`<br>`   (SELECT COUNT(*) FROM RECEITAS r WHERE r.CATE_ID = :cateId AND r.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM DESPESAS d WHERE d.CATE_ID = :cateId AND d.audit_data_exclusao IS NULL)`<br>` + (SELECT COUNT(*) FROM TRANSACOES_BANCARIAS t WHERE t.CATE_ID = :cateId AND t.audit_data_exclusao IS NULL) AS qtd_uso;` |
| <a id="c6"></a>C6 | Verifica rótulo externo duplicado no provedor (RN07).<br>`SELECT COUNT(*) FROM CATEGORIAS_PROVEDOR cp`<br>`WHERE cp.audit_data_exclusao IS NULL`<br>`  AND cp.OFPV_ID = :provedorId`<br>`  AND cp.CAPR_ROTULO_EXTERNO = :rotuloExterno`<br>`  AND (:idAtual IS NULL OR cp.CAPR_ID <> :idAtual);` |
| <a id="c7"></a>C7 | Provedores para os comboboxes da tela de Categorias por Provedor ([EDP06](#edp06)).<br>`SELECT p.OFPV_ID, p.OFPV_CODIGO, p.OFPV_NOME, p.OFPV_FL_ATIVO`<br>`FROM OPFI_PROVEDORES p`<br>`WHERE p.audit_data_exclusao IS NULL`<br>`ORDER BY p.OFPV_NOME ASC;` |

---

## 12. Parâmetros de Sistema

| PARÂMETRO | VALOR PADRÃO | DESCRIÇÃO |
|---|---|---|
| CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO | true | Se `true`, [RN06](#rn06) impede excluir uma categoria referenciada por lançamentos (só desativar). Se `false`, a exclusão é permitida e os lançamentos afetados ficam com `CATE_ID` nulo. |
| CATEGORIA_COMBOBOX_CACHE | true | Se `true`, a lista de [EDP05](#edp05) é cacheada em memória e invalidada nas gravações de [EDP02](#edp02), [EDP03](#edp03) e [EDP04](#edp04). |

---

## 13. Permissões

Quatro do módulo **Categorias** (`PERM_MODULO = 'Categorias'`) e quatro do módulo **Categorias por Provedor** (`PERM_MODULO = 'Categorias por Provedor'`) — uma por operação verificável. Fazem parte do catálogo do código e da carga inicial (Documento 0, [QUADRO_DESCRITIVO_26](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-26)). Convenção domínio-primeiro; cada uma vira a autoridade `PERM_{CÓDIGO}`. Desativar/reativar categoria **não** é permissão própria — é gravar `CATE_FL_ATIVO` pela operação de editar, coberta por `CATEGORIAS_EDITAR`.

| CÓDIGO | DESCRIÇÃO | PERFIS COM ACESSO |
|---|---|---|
| <a id="perm01"></a>PERM01 | `CATEGORIAS_LISTAR` — abrir a tela de Categorias, listar e filtrar. Controla a visibilidade do menu 'Categorias'. | [PERF01](#perf01) |
| <a id="perm02"></a>PERM02 | `CATEGORIAS_INSERIR` — cadastrar nova categoria. | [PERF01](#perf01) |
| <a id="perm03"></a>PERM03 | `CATEGORIAS_EDITAR` — editar categoria existente, inclusive desativar/reativar (`CATE_FL_ATIVO`). | [PERF01](#perf01) |
| <a id="perm04"></a>PERM04 | `CATEGORIAS_EXCLUIR` — exclusão lógica de categoria, respeitadas as travas ([RN05](#rn05), [RN06](#rn06)). | [PERF01](#perf01) |
| <a id="perm05"></a>PERM05 | `CATEGORIAS_PROVEDOR_LISTAR` — abrir a tela de Categorias por Provedor, listar e filtrar. Controla a visibilidade do menu 'Categorias por Provedor'. | [PERF01](#perf01) |
| <a id="perm06"></a>PERM06 | `CATEGORIAS_PROVEDOR_INSERIR` — cadastrar novo vínculo categoria × provedor. | [PERF01](#perf01) |
| <a id="perm07"></a>PERM07 | `CATEGORIAS_PROVEDOR_EDITAR` — editar vínculo categoria × provedor existente. | [PERF01](#perf01) |
| <a id="perm08"></a>PERM08 | `CATEGORIAS_PROVEDOR_EXCLUIR` — exclusão lógica de vínculo categoria × provedor. | [PERF01](#perf01) |

> `CATEGORIAS_INSERIR` / `CATEGORIAS_EDITAR` / `CATEGORIAS_EXCLUIR` pressupõem `CATEGORIAS_LISTAR` (portão do menu e da tela); o mesmo entre as permissões de Categorias por Provedor. Ainda assim cada uma é concedida à parte e aparece isolada na matriz abaixo. Estas permissões e seus vínculos vêm de carga inicial. O combobox de categorias das telas de lançamento ([EDP05](#edp05)) não usa permissão — basta o usuário estar autenticado —, portanto não entra na matriz.

### 13.1 Matriz Perfil × Permissão

| PERMISSÃO | ADMIN | USER |
|---|:-:|:-:|
| `CATEGORIAS_LISTAR` | ✓ | · |
| `CATEGORIAS_INSERIR` | ✓ | · |
| `CATEGORIAS_EDITAR` | ✓ | · |
| `CATEGORIAS_EXCLUIR` | ✓ | · |
| `CATEGORIAS_PROVEDOR_LISTAR` | ✓ | · |
| `CATEGORIAS_PROVEDOR_INSERIR` | ✓ | · |
| `CATEGORIAS_PROVEDOR_EDITAR` | ✓ | · |
| `CATEGORIAS_PROVEDOR_EXCLUIR` | ✓ | · |

`USER` não recebe nenhuma permissão destes módulos — os menus 'Categorias' e 'Categorias por Provedor' e todos os endpoints administrativos ficam indisponíveis. O `USER` continua enxergando as categorias ativas nas telas de lançamento por meio de [EDP05](#edp05), que exige apenas autenticação. Um perfil "consultor de categorias" (só `CATEGORIAS_LISTAR`) é uma configuração possível que o editor de perfil (documento `02`) permite montar.

---

## 14. Perfis

| CÓDIGO | NOME | DESCRIÇÃO |
|---|---|---|
| <a id="perf01"></a>PERF01 | ADMIN | Administrador do sistema. `PERF_FL_SISTEMA = TRUE`. Recebe todas as permissões na carga inicial, inclusive as oito destes módulos (Seção 13.1). Corresponde a `ROLE_ADMIN`. |
| <a id="perf02"></a>PERF02 | USER | Usuário comum. `PERF_FL_SISTEMA = TRUE`. Não tem nenhuma permissão de Categorias ou Categorias por Provedor; consome as categorias ativas nas próprias telas de lançamento. Corresponde a `ROLE_USER`. |

---

## 15. Fluxo de Eventos

**Excluir ou desativar uma categoria:**

```
1. ADMIN clica no ícone Excluir de uma linha do grid.
2. Sistema exibe a confirmação MSG07.
3. ADMIN confirma → POST (_method=DELETE) para EDP04.
        │
        ├─ Categoria de sistema (RN05)              → redirect + flash MSG05, nada muda.
        ├─ Categoria em uso (RN06 / C5) e o
        │  parâmetro bloqueia a exclusão            → redirect + flash MSG06 + oferta de desativar.
        │        └─ ADMIN aceita desativar → EDP03 (_method=PUT) com CATE_FL_ATIVO = FALSE
        │                                            → redirect + flash MSG04.
        └─ OK → preenche audit_data_exclusao / audit_excluido_por,
                 audita (Envers), invalida o cache do combobox,
                 redirect para /categorias/listar + flash MSG08.
```

**Manter um vínculo categoria × provedor:**

```
1. ADMIN clica em "Novo vínculo" → modal QUADRO_DESCRITIVO_5.
        Provedor e Categoria já renderizados pelo servidor na carga da página (C7 / C2).
2. ADMIN informa provedor, rótulo externo e categoria, clica em Salvar → POST para EDP07.
        │
        ├─ Rótulo já mapeado no provedor (RN07 / C6)  → re-renderiza a listagem com o modal e MSG11.
        ├─ Categoria inativa/inexistente (RN10)        → re-renderiza a listagem com o modal e MSG02 no campo Categoria.
        └─ OK → grava o vínculo, audita, redirect para /categorias-provedor/listar + flash MSG10.
```

---

## 16. Critérios de Aceitação / BDD

### 16.0 Listar categorias

Dado que estou autenticado com um usuário que tem a permissão [PERM01](#perm01).
E que acesso o menu "Administração > Categorias".
Quando a tela carregar.
Então o sistema deve exibir o grid com as categorias, ordenadas por nome, mostrando o código, o nome, o aplica-se a, o número de lançamentos, o tipo e a situação.
E o botão "Nova categoria" aparece só se eu tiver [PERM02](#perm02); o ícone Editar, só com [PERM03](#perm03); o ícone Excluir, só com [PERM04](#perm04).

### 16.1 Bloquear acesso de usuário sem permissão

Dado que estou autenticado com um usuário de perfil USER.
Quando eu tentar acessar "/categorias/listar".
Então o sistema deve negar o acesso (HTTP 403).

### 16.2 Cadastrar categoria

Dado que estou na tela de Categorias com [PERM01](#perm01) e [PERM02](#perm02) e clico em "Nova categoria".
Quando eu informar o nome "Pets", o código "PETS", o aplica-se a "Despesa" e clicar em "Salvar".
Então o sistema deve criar a categoria como comum e ativa, exibir [MSG01](#msg01) e recarregar o grid.

### 16.3 Normalização do código

Dado que estou cadastrando uma categoria e digito "Cartão de Crédito" no campo Código.
Quando o campo perder o foco.
Então o valor exibido deve ser "CARTAO_DE_CREDITO".

### 16.4 Código de categoria duplicado

Dado que já existe a categoria com o código "ALIMENTACAO".
Quando eu tentar criar outra categoria com o código "alimentacao".
Então o sistema deve impedir e exibir [MSG03](#msg03).

### 16.5 Não editar o código de uma categoria de sistema

Dado que abro a categoria "SALARIO" (de sistema) em edição.
Então o campo Código deve estar desabilitado.
E ao salvar, o código deve permanecer "SALARIO".

### 16.6 Editar os demais campos de uma categoria de sistema

Dado que abro a categoria "LAZER" (de sistema) em edição.
Quando eu alterar o nome para "Lazer e Hobbies", escolher uma cor e salvar.
Então o sistema deve atualizar o nome e a cor e exibir [MSG04](#msg04).

### 16.7 Não excluir categoria de sistema

Quando eu tentar excluir a categoria "MORADIA".
Então o sistema deve impedir e exibir [MSG05](#msg05).

### 16.8 Não excluir categoria em uso

Dado que a categoria "Pets" é usada por 2 despesas.
E que o parâmetro CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO está em "true".
Quando eu tentar excluí-la.
Então o sistema deve impedir, exibir [MSG06](#msg06) e oferecer a desativação.

### 16.9 Desativar categoria em uso

Dado que a categoria "Pets" é usada por lançamentos.
Quando eu desativá-la.
Então as 2 despesas que a usam permanecem com a categoria "Pets".
E a categoria "Pets" não deve mais aparecer no combobox de uma nova despesa.

### 16.10 Excluir categoria comum sem uso

Dado que a categoria "Teste" é comum e não tem nenhum lançamento.
Quando eu excluí-la e confirmar.
Então o sistema deve fazer a exclusão lógica, exibir [MSG08](#msg08) e recarregar o grid.

### 16.11 Combobox de lançamento filtra por aplica-se a

Dado que existem as categorias "Salário" (Receita), "Aluguel" (Despesa) e "Ajuste" (Ambos), todas ativas.
Quando a tela de nova Despesa carregar o combobox de categoria.
Então devem aparecer "Aluguel" e "Ajuste", e não "Salário".

### 16.12 Cadastrar vínculo categoria × provedor

Dado que estou na tela de Categorias por Provedor com [PERM05](#perm05) e [PERM06](#perm06) e clico em "Novo vínculo".
Quando eu escolher o provedor "Pluggy", informar o rótulo externo "Food and drinks", escolher a categoria "Alimentação" e salvar.
Então o sistema deve criar o vínculo, exibir [MSG10](#msg10) e recarregar o grid.

### 16.13 Rótulo externo duplicado no mesmo provedor

Dado que já existe o vínculo "Food and drinks" no provedor "Pluggy".
Quando eu tentar criar outro vínculo "Food and drinks" no provedor "Pluggy".
Então o sistema deve impedir e exibir [MSG11](#msg11).

### 16.14 Mesmo rótulo em provedores diferentes é permitido

Dado que existe o vínculo "Food and drinks" no provedor "Pluggy".
Quando eu criar o vínculo "Food and drinks" no provedor "Belvo".
Então o sistema deve aceitar o cadastro.

### 16.15 Vínculo só aceita categoria ativa

Dado que a categoria "Pets" está inativa.
Quando eu tentar criar um vínculo apontando para "Pets".
Então o sistema deve impedir e sinalizar o campo Categoria com [MSG02](#msg02).

### 16.16 Auditoria da categoria

Dado que altero o nome de uma categoria e salvo.
Quando eu consultar a auditoria de `CATEGORIAS`.
Então deve haver o registro de quem alterou e quando, com o nome anterior e o novo.

---

## 17. Workshop de Análise

Data: —
Convidados: Diego Cordeiro
Participantes: Diego Cordeiro
Descrição: Levantamento a partir do Documento 0 (Observações 14, 16 e 19; [QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3) e [_15](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-15)) e do enum `CategoriaRegistroFinanceiro` da geração 1 (`dsc-backend`).

**Decisões tomadas:**
- O enum `CategoriaRegistroFinanceiro` vira a tabela `CATEGORIAS`, editável por tela; `Receita`/`Despesa`/`TransacaoBancaria` passam a ter `CATE_ID` (FK nullable).
- Categorias são **globais** e geridas só por ADMIN — `CATEGORIAS` não tem `USU_ID` no Documento 0. O `USER` consome as categorias ativas via [EDP05](#edp05) (só autenticação).
- Categoria de sistema espelha o "perfil de sistema" do documento `02`: código imutável, flag de sistema não editável pela tela, não excluível; demais campos editáveis.
- O mapa `CATEGORIAS_PROVEDOR` fica numa **tela administrativa própria** (não numa aba do modal de categoria), com permissões próprias. Funciona antes das telas `14`/`15` porque `OPFI_PROVEDORES` vem de carga inicial.
- Exclusão de categoria em uso: bloqueada por padrão ([RN06](#rn06)), com a desativação como alternativa; comportamento parametrizável por `CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO`.
- Permissões: uma por operação (`LISTAR` / `INSERIR` / `EDITAR` / `EXCLUIR` por módulo), na convenção domínio-primeiro do documento `01 - manter-usuario` (revisão 1.1). Desativar/reativar categoria não é permissão própria — cai em `CATEGORIAS_EDITAR`.
- As duas telas são CRUD sem AJAX (revisão 1.1): grid renderizado no servidor, modal preenchido pelos dados da linha do grid, filtro por GET, gravação por POST → redirect → flash. AJAX só no combobox [EDP05](#edp05).
- Carga inicial: 19 categorias equivalentes ao enum, com `CATE_FL_SISTEMA = TRUE` (Seção 6.4).

**A Confirmar:**
- `CATE_APLICA_A` da carga inicial: `INVESTIMENTO` deve ser `RECEITA` (como no agrupamento da geração 1) ou `AMBOS`? `OUTRO` deve ser `AMBOS` ou existir uma variante por tipo?
- Categoria de sistema: além do código, o campo `CATE_APLICA_A` também deve ser imutável, para não mudar em que telas de lançamento a categoria aparece depois de já estar em uso?
- Manter `CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO` como parâmetro, ou fixar o bloqueio em regra dura (sem parâmetro)?
- Conjunto de ícones aceitos em `CATE_ICONE`: qualquer nome da biblioteca Tabler (validação livre) ou uma lista curada mantida no front?
- Cor obrigatória para categorias que aparecem nos gráficos do Dashboard, ou o Dashboard atribui uma cor automática quando `CATE_COR` é nulo?
- Categoria própria por usuário (futuro): fica descartada nesta versão; se um dia entrar, exige `USU_ID` em `CATEGORIAS` no Documento 0 e revisão do escopo global × por usuário.
- Necessidade de uma tela/modal de histórico de alterações da categoria (Envers) nesta versão, ou basta a auditoria em banco.

---

## 18. Anexos

- **Protótipo e diagramas (v1.0):** gerados. Casos de uso (`prototipo/manter-categoria-casos-uso.drawio` + `images/manter-categoria-casos-uso.png`), DER do subconjunto (`prototipo/manter-categoria-der.drawio` + `images/manter-categoria-der.png`), wireframes das cinco telas/modais (`prototipo/manter-categoria-prototipo.drawio` + `images/mc-tela-1..5.png`) e protótipo navegável (`prototipo/manter-categoria-prototipo.html`). PNGs regeráveis por `prototipo/render-pngs.py` (Playwright).
- Documento 0 — Fundação: `../00 - analise-geral/documento-0-fundacao.md` ([QUADRO_DESCRITIVO_3](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-3), [QUADRO_DESCRITIVO_13](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-13), [QUADRO_DESCRITIVO_15](../00%20-%20analise-geral/documento-0-fundacao.md#quadro-descritivo-15)).
- Documento `01 - manter-usuario`: `../01 - manter-usuario/documento-analise-manter-usuario.md` (referência de forma e voz).
- Documento `02 - manter-perfil-permissao`: `../02 - manter-perfil-permissao/documento-analise-manter-perfil-permissao.md` (regra do "perfil de sistema" espelhada aqui na categoria de sistema).
- Código de referência geração 1: `dsc-backend` (`enums/CategoriaRegistroFinanceiro.java`, `enums/TipoRegistroFinanceiro.java`; uso em `Receita`, `Despesa`, `TransacaoBancaria` e no `DashCardDespesaPorCategoriaDTO`).
