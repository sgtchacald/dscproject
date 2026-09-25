#!/usr/bin/env python3
# Gera DER (draw.io), diagrama de classes (draw.io), PlantUML e XMI/Umbrello
# do Documento 0 - Fundação do dscproject-spring-mvc.
import html, os, textwrap

OUT = os.path.dirname(os.path.abspath(__file__))

AUDIT = [("audit_data_criacao", "DATETIME(6)"), ("audit_criado_por", "VARCHAR(400)"),
         ("audit_data_alteracao", "DATETIME(6)"), ("audit_alterado_por", "VARCHAR(400)"),
         ("audit_data_exclusao", "DATETIME(6)"), ("audit_excluido_por", "VARCHAR(400)")]

# tabela: (id_curto, NOME, camada, [ (coluna, tipo, flag)  flag: PK|FK:Destino|"" ], grupo)
TABELAS = [
 ("perf","PERFIS","rbac",[
   ("PERF_ID","BIGINT","PK"),("PERF_CODIGO","VARCHAR(30) U",""),("PERF_NOME","VARCHAR(100)",""),
   ("PERF_DESCRICAO","VARCHAR(255)",""),("PERF_FL_SISTEMA","BOOLEAN","")],0),
 ("perm","PERMISSOES","rbac",[
   ("PERM_ID","BIGINT","PK"),("PERM_CODIGO","VARCHAR(60) U",""),("PERM_NOME","VARCHAR(100)",""),
   ("PERM_DESCRICAO","VARCHAR(255)",""),("PERM_FL_CONCEDIVEL_POR_PLANO","BOOLEAN","")],0),
 ("pepe","PERFIL_PERMISSAO","rbac",[
   ("PEPE_ID","BIGINT","PK"),("PERF_ID","BIGINT","FK:perf"),("PERM_ID","BIGINT","FK:perm")],0),
 ("usu","USUARIOS","dominio",[
   ("USU_ID","BIGINT","PK"),("USU_NOME","VARCHAR(100)",""),("USU_GENERO","CHAR(1)",""),
   ("USU_DT_NASCIMENTO","DATE",""),("USU_EMAIL","VARCHAR(512) U",""),("USU_LOGIN","VARCHAR(40) U",""),
   ("USU_SENHA","VARCHAR(1024)",""),("PERF_ID","BIGINT","FK:perf")],1),
 ("cate","CATEGORIAS","dominio",[
   ("CATE_ID","BIGINT","PK"),("CATE_CODIGO","VARCHAR(40) U",""),("CATE_NOME","VARCHAR(100)",""),
   ("CATE_APLICA_A","VARCHAR(10)",""),("CATE_COR","CHAR(7)",""),("CATE_ICONE","VARCHAR(40)",""),
   ("CATE_FL_ATIVO","BOOLEAN",""),("CATE_FL_SISTEMA","BOOLEAN","")],2),
 ("infi","INSTITUICOES_FINANCEIRAS","dominio",[
   ("INFI_ID","BIGINT","PK"),("INFI_NOME","VARCHAR(100) U",""),("INFI_CODIGO","VARCHAR(100) U",""),
   ("INFI_TIPO_INSTITUICAO","CHAR(1)",""),("INFI_FL_ATIVO","BOOLEAN","")],3),
 ("cta","CONTAS","dominio",[
   ("CTA_ID","BIGINT","PK"),("CTA_DESCRICAO","VARCHAR(100)",""),("CTA_TIPO","VARCHAR(20)",""),
   ("CTA_AGENCIA","VARCHAR(30)",""),("CTA_NUMERO","VARCHAR(30)",""),("CTA_MOEDA","CHAR(3)",""),
   ("CTA_SALDO","DECIMAL(15,2)",""),("CTA_SALDO_SINCRONIZADO_EM","DATETIME(6)",""),
   ("CTA_NOME_GERENTE","VARCHAR(100)",""),("CTA_TEL_GERENTE","VARCHAR(20)",""),
   ("CTA_FL_ATIVO","BOOLEAN",""),("CTA_FL_CONSIDERA_SALDO","BOOLEAN",""),
   ("INFI_ID","BIGINT","FK:infi"),("USU_ID","BIGINT","FK:usu")],4),
 ("cacr","CARTOES_CREDITO","dominio",[
   ("CACR_ID","BIGINT","PK"),("CACR_DESCRICAO","VARCHAR(100)",""),("CACR_BANDEIRA","VARCHAR(30)",""),
   ("CACR_FINAL_CARTAO","CHAR(4)",""),("CACR_LIMITE","DECIMAL(15,2)",""),
   ("CACR_DIA_FECHAMENTO","TINYINT",""),("CACR_DIA_VENCIMENTO","TINYINT",""),
   ("CACR_FL_ATIVO","BOOLEAN",""),("CTA_ID","BIGINT","FK:cta"),("USU_ID","BIGINT","FK:usu")],5),
 ("trba","TRANSACOES_BANCARIAS","dominio",[
   ("TRBA_ID","BIGINT","PK"),("TRBA_DESCRICAO","VARCHAR(512)",""),("TRBA_VALOR","DECIMAL(15,2)",""),
   ("TRBA_COMPETENCIA","CHAR(7)",""),("TRBA_DT_LANCAMENTO","DATE",""),
   ("TRBA_NATUREZA_MOVIMENTO","VARCHAR(10)",""),("TRBA_FL_PAGAMENTO_FATURA","BOOLEAN",""),
   ("TRBA_ID_EXTERNO","VARCHAR(120) U",""),("TRBA_ORIGEM","VARCHAR(20)",""),
   ("CTA_ID","BIGINT","FK:cta"),("CATE_ID","BIGINT","FK:cate")],6),
 ("ftca","FATURAS_CARTAO","dominio",[
   ("FTCA_ID","BIGINT","PK"),("FTCA_COMPETENCIA","CHAR(7)",""),("FTCA_DT_FECHAMENTO","DATE",""),
   ("FTCA_DT_VENCIMENTO","DATE",""),("FTCA_VALOR_TOTAL","DECIMAL(15,2)",""),
   ("FTCA_VALOR_MINIMO","DECIMAL(15,2)",""),("FTCA_VALOR_ENCARGOS","DECIMAL(15,2)",""),
   ("FTCA_VALOR_PAGO","DECIMAL(15,2)",""),("FTCA_STATUS","VARCHAR(20)",""),("FTCA_ORIGEM","VARCHAR(20)",""),
   ("CACR_ID","BIGINT","FK:cacr"),("TRBA_ID_PAGAMENTO","BIGINT","FK:trba")],7),
 ("rece","RECEITAS","dominio",[
   ("RECE_ID","BIGINT","PK"),("RECE_COMPETENCIA","CHAR(7)",""),("RECE_NOME","VARCHAR(100)",""),
   ("RECE_DESCRICAO","VARCHAR(512)",""),("RECE_VALOR","DECIMAL(15,2)",""),("RECE_DT_LANCAMENTO","DATE",""),
   ("RECE_DT_RECEBIMENTO","DATE",""),("RECE_FL_RECEBIDO","BOOLEAN",""),("RECE_ORIGEM","VARCHAR(20)",""),
   ("CTA_ID","BIGINT","FK:cta"),("CATE_ID","BIGINT","FK:cate")],8),
 ("desp","DESPESAS","dominio",[
   ("DESP_ID","BIGINT","PK"),("DESP_COMPETENCIA","CHAR(7)",""),("DESP_NOME","VARCHAR(100)",""),
   ("DESP_DESCRICAO","VARCHAR(512)",""),("DESP_DT_LANCAMENTO","DATE",""),("DESP_DT_VENCIMENTO","DATE",""),
   ("DESP_DT_PAGAMENTO","DATE",""),("DESP_VALOR","DECIMAL(15,2)",""),("DESP_VALOR_TOTAL_COMPRA","DECIMAL(15,2)",""),
   ("DESP_FL_PARCELADA","BOOLEAN",""),("DESP_NRO_PARCELA","SMALLINT",""),("DESP_QTD_PARCELAS","SMALLINT",""),
   ("DESP_MEIO_PAGAMENTO","VARCHAR(20)",""),("DESP_IND_STATUS_PAGAMENTO","VARCHAR(20)",""),
   ("DESP_FL_PAGAMENTO_FATURA","BOOLEAN",""),("DESP_ORIGEM","VARCHAR(20)",""),
   ("DESP_ID_PARCELA_PAI","BIGINT","FK:desp"),("CTA_ID","BIGINT","FK:cta"),
   ("CACR_ID","BIGINT","FK:cacr"),("FTCA_ID","BIGINT","FK:ftca"),("CATE_ID","BIGINT","FK:cate")],9),
 ("depu","DESPESAS_USUARIO","dominio",[
   ("DEPU_ID","BIGINT","PK"),("DEPU_VALOR","DECIMAL(15,2)",""),("DEPU_IND_STATUS_PAGAMENTO","VARCHAR(20)",""),
   ("DEPU_DT_ACERTO","DATE",""),("DESP_ID","BIGINT","FK:desp"),("USU_ID","BIGINT","FK:usu")],10),
 ("inve","INVESTIMENTOS","dominio",[
   ("INVE_ID","BIGINT","PK"),("INVE_DESCRICAO","VARCHAR(150)",""),("INVE_TIPO","VARCHAR(30)",""),
   ("INVE_EMISSOR","VARCHAR(100)",""),("INVE_VALOR_APLICADO","DECIMAL(15,2)",""),
   ("INVE_VALOR_BRUTO","DECIMAL(15,2)",""),("INVE_VALOR_LIQUIDO","DECIMAL(15,2)",""),
   ("INVE_DT_APLICACAO","DATE",""),("INVE_DT_VENCIMENTO","DATE",""),
   ("INVE_DT_ATUALIZACAO_VALOR","DATETIME(6)",""),("INVE_ORIGEM","VARCHAR(20)",""),
   ("CTA_ID","BIGINT","FK:cta"),("INFI_ID","BIGINT","FK:infi"),("USU_ID","BIGINT","FK:usu")],11),
 ("ofpv","OPFI_PROVEDORES","opfi",[
   ("OFPV_ID","BIGINT","PK"),("OFPV_CODIGO","VARCHAR(30) U",""),("OFPV_NOME","VARCHAR(100)",""),
   ("OFPV_URL_BASE","VARCHAR(300)",""),("OFPV_FL_SUPORTA_WEBHOOK","BOOLEAN",""),
   ("OFPV_PARAMETROS","JSON",""),("OFPV_FL_ATIVO","BOOLEAN","")],12),
 ("ofip","OPFI_INSTITUICAO_PROVEDOR","opfi",[
   ("OFIP_ID","BIGINT","PK"),("OFIP_ID_EXTERNO","VARCHAR(80)",""),
   ("INFI_ID","BIGINT","FK:infi"),("OFPV_ID","BIGINT","FK:ofpv")],13),
 ("capr","CATEGORIAS_PROVEDOR","dominio",[
   ("CAPR_ID","BIGINT","PK"),("CAPR_ROTULO_EXTERNO","VARCHAR(120)",""),
   ("CATE_ID","BIGINT","FK:cate"),("OFPV_ID","BIGINT","FK:ofpv")],14),
 ("ofcr","OPFI_CREDENCIAIS","opfi",[
   ("OFCR_ID","BIGINT","PK"),("OFCR_CLIENT_ID","VARCHAR(200)",""),("OFCR_CLIENT_SECRET","TEXT (cifrado)",""),
   ("OFCR_SECRET_ATUALIZADO_EM","DATETIME(6)",""),("OFCR_AMBIENTE","VARCHAR(20)",""),
   ("OFCR_FL_ATIVO","BOOLEAN",""),("USU_ID","BIGINT","FK:usu"),("OFPV_ID","BIGINT","FK:ofpv")],15),
 ("ofcx","OPFI_CONEXOES","opfi",[
   ("OFCX_ID","BIGINT","PK"),("OFCX_ID_EXTERNO","VARCHAR(60)",""),("OFCX_STATUS","VARCHAR(30)",""),
   ("OFCX_STATUS_DETALHE","VARCHAR(200)",""),("OFCX_EXECUCAO_STATUS","VARCHAR(40)",""),
   ("OFCX_ULTIMA_SINCRONIZACAO_EM","DATETIME(6)",""),("OFCX_PROXIMA_SINCRONIZACAO_EM","DATETIME(6)",""),
   ("OFCX_ERRO_CODIGO","VARCHAR(60)",""),("OFCX_ERRO_MENSAGEM","VARCHAR(500)",""),
   ("OFCX_PARAMETROS","JSON",""),("USU_ID","BIGINT","FK:usu"),("INFI_ID","BIGINT","FK:infi"),
   ("OFPV_ID","BIGINT","FK:ofpv"),("OFCR_ID","BIGINT","FK:ofcr")],16),
 ("ofcs","OPFI_CONSENTIMENTOS","opfi",[
   ("OFCS_ID","BIGINT","PK"),("OFCS_ID_EXTERNO","VARCHAR(60)",""),("OFCS_ESCOPOS","VARCHAR(255)",""),
   ("OFCS_DT_CONCESSAO","DATETIME(6)",""),("OFCS_DT_EXPIRACAO","DATETIME(6)",""),
   ("OFCS_STATUS","VARCHAR(20)",""),("OFCS_DT_REVOGACAO","DATETIME(6)",""),
   ("OFCX_ID","BIGINT","FK:ofcx")],17),
 ("ofce","OPFI_CONTAS_EXTERNAS","opfi",[
   ("OFCE_ID","BIGINT","PK"),("OFCE_ID_EXTERNO","VARCHAR(60) U",""),("OFCE_TIPO","VARCHAR(20)",""),
   ("OFCE_SUBTIPO","VARCHAR(40)",""),("OFCE_NOME","VARCHAR(150)",""),("OFCE_NUMERO","VARCHAR(40)",""),
   ("OFCE_SALDO","DECIMAL(15,2)",""),("OFCE_SALDO_EM","DATETIME(6)",""),("OFCE_MOEDA","CHAR(3)",""),
   ("OFCE_DADOS_BRUTOS","JSON",""),("OFCX_ID","BIGINT","FK:ofcx"),
   ("CTA_ID","BIGINT","FK:cta"),("CACR_ID","BIGINT","FK:cacr")],18),
 ("oftr","OPFI_TRANSACOES","opfi",[
   ("OFTR_ID","BIGINT","PK"),("OFTR_ID_EXTERNO","VARCHAR(60) U",""),("OFTR_DESCRICAO","VARCHAR(255)",""),
   ("OFTR_VALOR","DECIMAL(15,2)",""),("OFTR_DT_TRANSACAO","DATE",""),("OFTR_NATUREZA","VARCHAR(10)",""),
   ("OFTR_CATEGORIA_EXTERNA","VARCHAR(100)",""),("OFTR_STATUS_EXTERNO","VARCHAR(20)",""),
   ("OFTR_STATUS_CONCILIACAO","VARCHAR(20)",""),("OFTR_TIPO_LANCAMENTO_GERADO","VARCHAR(20)",""),
   ("OFTR_ID_LANCAMENTO_GERADO","BIGINT (logico)",""),("OFTR_DT_CONCILIACAO","DATETIME(6)",""),
   ("OFTR_DADOS_BRUTOS","JSON",""),("OFCE_ID","BIGINT","FK:ofce")],19),
 ("offa","OPFI_FATURAS","opfi",[
   ("OFFA_ID","BIGINT","PK"),("OFFA_ID_EXTERNO","VARCHAR(60) U",""),("OFFA_DT_VENCIMENTO","DATE",""),
   ("OFFA_DT_FECHAMENTO","DATE",""),("OFFA_VALOR_TOTAL","DECIMAL(15,2)",""),
   ("OFFA_VALOR_MINIMO","DECIMAL(15,2)",""),("OFFA_STATUS_CONCILIACAO","VARCHAR(20)",""),
   ("OFFA_ID_FATURA_GERADA","BIGINT (logico)",""),("OFFA_DADOS_BRUTOS","JSON",""),
   ("OFCE_ID","BIGINT","FK:ofce")],20),
 ("ofin","OPFI_INVESTIMENTOS","opfi",[
   ("OFIN_ID","BIGINT","PK"),("OFIN_ID_EXTERNO","VARCHAR(60) U",""),("OFIN_NOME","VARCHAR(150)",""),
   ("OFIN_TIPO_EXTERNO","VARCHAR(40)",""),("OFIN_VALOR_APLICADO","DECIMAL(15,2)",""),
   ("OFIN_VALOR_BRUTO","DECIMAL(15,2)",""),("OFIN_VALOR_LIQUIDO","DECIMAL(15,2)",""),
   ("OFIN_DT_POSICAO","DATE",""),("OFIN_STATUS_CONCILIACAO","VARCHAR(20)",""),
   ("OFIN_ID_INVESTIMENTO_GERADO","BIGINT (logico)",""),("OFIN_DADOS_BRUTOS","JSON",""),
   ("OFCX_ID","BIGINT","FK:ofcx")],21),
 ("ofev","OPFI_EVENTOS_WEBHOOK","opfi",[
   ("OFEV_ID","BIGINT","PK"),("OFEV_ID_EVENTO_EXTERNO","VARCHAR(80)",""),("OFEV_TIPO","VARCHAR(60)",""),
   ("OFEV_ID_EXTERNO","VARCHAR(60)",""),("OFEV_PAYLOAD","JSON",""),("OFEV_RECEBIDO_EM","DATETIME(6)",""),
   ("OFEV_PROCESSADO_EM","DATETIME(6)",""),("OFEV_STATUS","VARCHAR(20)",""),
   ("OFEV_ERRO_MENSAGEM","VARCHAR(500)",""),("OFEV_TENTATIVAS","SMALLINT",""),
   ("OFPV_ID","BIGINT","FK:ofpv"),("OFCX_ID","BIGINT","FK:ofcx")],22),
 ("ofsi","OPFI_SINCRONIZACOES","opfi",[
   ("OFSI_ID","BIGINT","PK"),("OFSI_TIPO","VARCHAR(20)",""),("OFSI_INICIADO_EM","DATETIME(6)",""),
   ("OFSI_FINALIZADO_EM","DATETIME(6)",""),("OFSI_STATUS","VARCHAR(20)",""),
   ("OFSI_QTD_TRANSACOES_NOVAS","INT",""),("OFSI_QTD_CONTAS_ATUALIZADAS","INT",""),
   ("OFSI_ERRO_MENSAGEM","VARCHAR(500)",""),("OFCX_ID","BIGINT","FK:ofcx"),("OFEV_ID","BIGINT","FK:ofev")],23),
]

BYID = {t[0]: t for t in TABELAS}

# ---------------------------------------------------------------- DER draw.io
def esc(s): return html.escape(s, quote=True)

def gen_der():
    COLW = 340
    HDR = 30
    ROW = 22
    GAP_X = 120
    GAP_Y = 70
    PERCOL = 5   # tabelas por coluna visual
    cells = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    pos = {}
    # layout em colunas
    col = 0; row = 0; x = 40; y = 40; colmax = 0
    order = sorted(TABELAS, key=lambda t: t[4])
    for t in order:
        sid, nome, camada, cols, grp = t
        h = HDR + ROW * (len(cols) + 6)  # +6 auditoria
        if row == PERCOL:
            x += colmax + GAP_X; y = 40; row = 0; colmax = 0
        pos[sid] = (x, y, COLW, h)
        colmax = max(colmax, COLW)
        y += h + GAP_Y
        row += 1
    for t in order:
        sid, nome, camada, cols, grp = t
        x, y, w, h = pos[sid]
        fill = "#FFF4E5" if camada == "opfi" else "#EAF2FB"
        strokec = "#B26B00" if camada == "opfi" else "#1F3864"
        tstyle = (f"shape=table;startSize={HDR};container=1;collapsible=0;childLayout=tableLayout;"
                  f"fixedRows=1;rowLines=0;fontStyle=1;align=center;resizeLast=1;html=1;"
                  f"fontFamily=Helvetica;fontSize=12;fillColor={fill};strokeColor={strokec};fontColor={strokec};")
        cells.append(f'<mxCell id="{sid}" parent="1" style="{esc(tstyle)}" value="{esc(nome)}" vertex="1">'
                     f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
        allcols = [(c, ty, fl) for (c, ty, fl) in cols] + [(c, ty, "AUD") for (c, ty) in AUDIT]
        for i, (c, ty, fl) in enumerate(allcols):
            rid = f"{sid}_r{i}"
            rowstyle = ("shape=tableRow;horizontal=0;startSize=0;swimlaneHead=0;swimlaneBody=0;fillColor=none;"
                        "collapsible=0;dropTarget=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;"
                        "top=0;left=0;right=0;bottom=0;html=1;")
            cells.append(f'<mxCell id="{rid}" parent="{sid}" style="{esc(rowstyle)}" value="" vertex="1">'
                         f'<mxGeometry y="{HDR+i*ROW}" width="{w}" height="{ROW}" as="geometry"/></mxCell>')
            mark = ""
            fc = "#333333"; fs = "0"
            if fl == "PK": mark = "  (PK)"; fs = "1"
            elif fl.startswith("FK:"): mark = "  (FK)"; fc = "#8A4B08"
            elif fl == "AUD": fc = "#999999"
            label = f"{c} : {ty}{mark}"
            cstyle = (f"shape=partialRectangle;html=1;whiteSpace=wrap;connectable=0;strokeColor=inherit;"
                      f"overflow=hidden;fillColor=none;top=0;left=0;bottom=1;right=0;pointerEvents=1;align=left;"
                      f"fontColor={fc};fontFamily=Helvetica;fontSize=10;spacingLeft=6;fontStyle={fs};")
            cells.append(f'<mxCell id="{rid}_c" parent="{rid}" style="{esc(cstyle)}" value="{esc(label)}" vertex="1">'
                         f'<mxGeometry width="{w}" height="{ROW}" as="geometry">'
                         f'<mxRectangle width="{w}" height="{ROW}" as="alternateBounds"/></mxGeometry></mxCell>')
    # edges FK -> PK
    en = 0
    for t in order:
        sid, nome, camada, cols, grp = t
        for i, (c, ty, fl) in enumerate(cols):
            if fl.startswith("FK:"):
                dst = fl[3:]
                en += 1
                estyle = ("edgeStyle=entityRelationEdgeStyle;fontSize=9;html=1;endArrow=ERmandOne;"
                          "startArrow=ERzeroToMany;endFill=0;startFill=0;strokeColor=#5B7FA6;rounded=0;"
                          "fontFamily=Helvetica;")
                cells.append(f'<mxCell id="e{en}" parent="1" style="{esc(estyle)}" value="{esc(c)}" edge="1" '
                             f'source="{sid}_r{i}" target="{dst}_r0"><mxGeometry relative="1" as="geometry"/></mxCell>')
    body = "".join(cells)
    xml = (f'<mxfile host="app.diagrams.net">'
           f'<diagram id="der" name="DER — Documento 0 (dscproject-spring-mvc)">'
           f'<mxGraphModel dx="1600" dy="1000" grid="0" gridSize="10" guides="1" tooltips="1" connect="1" '
           f'arrows="1" fold="1" page="1" pageScale="1" pageWidth="4000" pageHeight="3200" math="0" shadow="0">'
           f'<root>{body}</root></mxGraphModel></diagram></mxfile>')
    open(os.path.join(OUT, "documento-0-fundacao-der.drawio"), "w", encoding="utf-8").write(xml)
    print("DER:", en, "FKs")

# ---------------------------------------------------------------- PlantUML
def gen_puml():
    L = ["@startuml documento-0-fundacao-classes",
         "skinparam linetype ortho", "hide circle", "hide empty members",
         "skinparam classAttributeIconSize 0", "left to right direction", ""]
    L += ['abstract class AbstractAuditoria {',
          '  +dataCriacao : Instant', '  +criadoPor : String', '  +dataAlteracao : Instant',
          '  +alteradoPor : String', '  +dataExclusao : Instant', '  +excluidoPor : String', '}', '',
          'abstract class LancamentoFinanceiro {', '  +competencia : YearMonth', '  +valor : BigDecimal',
          '  +dataLancamento : LocalDate', '  +origem : OrigemLancamento', '}',
          'AbstractAuditoria <|-- LancamentoFinanceiro', '']
    ENT = {
      "Perfil":("perf","AbstractAuditoria"),"Permissao":("perm","AbstractAuditoria"),"PerfilPermissao":("pepe","AbstractAuditoria"),
      "Usuario":("usu","AbstractAuditoria"),"Categoria":("cate","AbstractAuditoria"),
      "Usuario":("usu","AbstractAuditoria"),"Categoria":("cate","AbstractAuditoria"),
      "CategoriaProvedor":("capr","AbstractAuditoria"),"InstituicaoFinanceira":("infi","AbstractAuditoria"),
      "Conta":("cta","AbstractAuditoria"),"CartaoCredito":("cacr","AbstractAuditoria"),
      "TransacaoBancaria":("trba","LancamentoFinanceiro"),"FaturaCartao":("ftca","AbstractAuditoria"),
      "Receita":("rece","LancamentoFinanceiro"),"Despesa":("desp","LancamentoFinanceiro"),
      "DespesaUsuario":("depu","AbstractAuditoria"),"Investimento":("inve","AbstractAuditoria"),
      "OpfiProvedor":("ofpv","AbstractAuditoria"),"OpfiInstituicaoProvedor":("ofip","AbstractAuditoria"),
      "OpfiCredencial":("ofcr","AbstractAuditoria"),"OpfiConexao":("ofcx","AbstractAuditoria"),
      "OpfiConsentimento":("ofcs","AbstractAuditoria"),"OpfiContaExterna":("ofce","AbstractAuditoria"),
      "OpfiTransacao":("oftr","AbstractAuditoria"),"OpfiFatura":("offa","AbstractAuditoria"),
      "OpfiInvestimento":("ofin","AbstractAuditoria"),"OpfiEventoWebhook":("ofev","AbstractAuditoria"),
      "OpfiSincronizacao":("ofsi","AbstractAuditoria"),
    }
    sid2ent = {v[0]: k for k, v in ENT.items()}
    for ent,(sid,sup) in ENT.items():
        t = BYID[sid]
        cols = t[3]
        L.append(f'class {ent} {{')
        for (c,ty,fl) in cols:
            if fl.startswith("FK:") or fl == "PK": continue
            attr = c.split("_",1)[1].lower()
            L.append(f'  +{attr} : {ty.split()[0]}')
        L.append('}')
        L.append(f'{sup} <|-- {ent}')
    L.append('')
    for ent,(sid,sup) in ENT.items():
        t = BYID[sid]
        for (c,ty,fl) in t[3]:
            if fl.startswith("FK:"):
                dst = sid2ent.get(fl[3:])
                if dst:
                    rel = "1" if "NOT NULL" not in ty else "1"
                    L.append(f'{ent} --> "{"0..1"}" {dst} : {c.split("_",1)[1].lower() if "_" in c else c}')
    L += ['', 'enum OrigemLancamento { MANUAL\nOPEN_FINANCE\nIMPORTACAO }',
          'enum TipoLancamento { RECEITA\nDESPESA }',
          'enum NaturezaMovimento { CREDITO\nDEBITO }',
          'enum MeioPagamento { DINHEIRO\nDEBITO\nCREDITO\nPIX\nBOLETO\nTRANSFERENCIA }',
          'enum StatusFatura { ABERTA\nFECHADA\nPAGA\nPAGA_PARCIAL }',
          'enum TipoConta { CORRENTE\nPOUPANCA\nINVESTIMENTO\nCARTEIRA }',
          'enum TipoInvestimento { RENDA_FIXA\nRENDA_VARIAVEL\nFUNDO\nTESOURO\nPREVIDENCIA\nCRIPTO\nOUTRO }',
          'enum StatusConexao { ATUALIZANDO\nATUALIZADO\nERRO_LOGIN\nDESATUALIZADO\nAGUARDANDO_USUARIO }',
          'enum StatusConciliacao { PENDENTE\nCONCILIADA\nIGNORADA }',
          '@enduml']
    open(os.path.join(OUT, "documento-0-fundacao-classes.puml"), "w", encoding="utf-8").write("\n".join(L))
    print("PUML ok")

# ---------------------------------------------------------------- XMI / Umbrello
def gen_xmi():
    import time
    ENT = ["Perfil","Permissao","PerfilPermissao","Usuario","Categoria","CategoriaProvedor","InstituicaoFinanceira","Conta","CartaoCredito",
           "TransacaoBancaria","FaturaCartao","Receita","Despesa","DespesaUsuario","Investimento",
           "OpfiProvedor","OpfiInstituicaoProvedor","OpfiCredencial","OpfiConexao","OpfiConsentimento",
           "OpfiContaExterna","OpfiTransacao","OpfiFatura","OpfiInvestimento","OpfiEventoWebhook","OpfiSincronizacao"]
    ent2sid = {"Perfil":"perf","Permissao":"perm","PerfilPermissao":"pepe","Usuario":"usu","Categoria":"cate","CategoriaProvedor":"capr","InstituicaoFinanceira":"infi",
               "Conta":"cta","CartaoCredito":"cacr","TransacaoBancaria":"trba","FaturaCartao":"ftca",
               "Receita":"rece","Despesa":"desp","DespesaUsuario":"depu","Investimento":"inve",
               "OpfiProvedor":"ofpv","OpfiInstituicaoProvedor":"ofip","OpfiCredencial":"ofcr","OpfiConexao":"ofcx",
               "OpfiConsentimento":"ofcs","OpfiContaExterna":"ofce","OpfiTransacao":"oftr","OpfiFatura":"offa",
               "OpfiInvestimento":"ofin","OpfiEventoWebhook":"ofev","OpfiSincronizacao":"ofsi"}
    sup = {e:"LancamentoFinanceiro" for e in ["TransacaoBancaria","Receita","Despesa"]}
    ids = {}
    n = [1000]
    def nid(k):
        n[0]+=1; ids[k]=str(n[0]); return ids[k]
    classes = ["AbstractAuditoria","LancamentoFinanceiro"]+ENT
    for c in classes: nid("c:"+c)
    prim = {}
    for p in ["BIGINT","VARCHAR","CHAR","DECIMAL","DATE","DATETIME","BOOLEAN","JSON","TEXT","INT","SMALLINT","TINYINT"]:
        prim[p]=nid("p:"+p)
    def typ(t):
        base=t.split("(")[0].split()[0]
        return prim.get(base, prim["VARCHAR"])
    out=[]
    out.append('<?xml version="1.0" encoding="UTF-8"?>')
    out.append('<XMI xmi.version="1.2" xmlns:UML="http://schema.omg.org/spec/UML/1.4" timestamp="%s">' % time.strftime("%Y-%m-%dT%H:%M:%S"))
    out.append('<XMI.header><XMI.documentation><XMI.exporter>dscproject gen_diagrams.py</XMI.exporter>'
               '<XMI.exporterVersion>1.0</XMI.exporterVersion></XMI.documentation>'
               '<XMI.metamodel xmi.name="UML" xmi.version="1.4"/></XMI.header>')
    out.append('<XMI.content>')
    out.append('<UML:Model isSpecification="false" isAbstract="false" name="dscproject" xmi.id="m1">')
    out.append('<UML:Namespace.ownedElement>')
    # datatypes
    for p,i in prim.items():
        out.append(f'<UML:DataType isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false" name="{p}" xmi.id="{i}"/>')
    # classes
    def cls(name, abstract=False, attrs=None):
        cid=ids["c:"+name]
        ab = "true" if abstract else "false"
        out.append(f'<UML:Class isSpecification="false" isRoot="false" isLeaf="false" isAbstract="{ab}" name="{name}" xmi.id="{cid}">')
        if attrs:
            out.append('<UML:Classifier.feature>')
            for (an,at) in attrs:
                aid=nid(f"a:{name}:{an}")
                out.append(f'<UML:Attribute isSpecification="false" name="{esc(an)}" visibility="public" xmi.id="{aid}" type="{typ(at)}"/>')
            out.append('</UML:Classifier.feature>')
        out.append('</UML:Class>')
    cls("AbstractAuditoria", True, [("dataCriacao","DATETIME"),("criadoPor","VARCHAR"),
        ("dataAlteracao","DATETIME"),("alteradoPor","VARCHAR"),("dataExclusao","DATETIME"),("excluidoPor","VARCHAR")])
    cls("LancamentoFinanceiro", True, [("competencia","VARCHAR"),("valor","DECIMAL"),
        ("dataLancamento","DATE"),("origem","VARCHAR")])
    for e in ENT:
        t=BYID[ent2sid[e]]
        attrs=[]
        for (c,ty,fl) in t[3]:
            if fl.startswith("FK:") : continue
            an = c.split("_",1)[1].lower() if "_" in c else c.lower()
            attrs.append((an,ty))
        cls(e, False, attrs)
    # generalizations
    def gen(child, parent):
        gid=nid(f"g:{child}")
        out.append(f'<UML:Generalization isSpecification="false" xmi.id="{gid}" child="{ids["c:"+child]}" parent="{ids["c:"+parent]}"/>')
    gen("LancamentoFinanceiro","AbstractAuditoria")
    for e in ENT:
        gen(e, sup.get(e,"AbstractAuditoria"))
    # associations (FK)
    sid2ent={v:k for k,v in ent2sid.items()}
    for e in ENT:
        t=BYID[ent2sid[e]]
        for (c,ty,fl) in t[3]:
            if fl.startswith("FK:"):
                dst=sid2ent.get(fl[3:])
                if not dst: continue
                aid=nid(f"as:{e}:{c}")
                e1=nid("ae1:"+aid); e2=nid("ae2:"+aid)
                nm = c.split("_",1)[1].lower() if "_" in c else c.lower()
                out.append(f'<UML:Association isSpecification="false" name="{esc(nm)}" xmi.id="{aid}">'
                           f'<UML:Association.connection>'
                           f'<UML:AssociationEnd isSpecification="false" isNavigable="true" aggregation="none" '
                           f'name="" multiplicity="0..*" type="{ids["c:"+dst]}" xmi.id="{e1}"/>'
                           f'<UML:AssociationEnd isSpecification="false" isNavigable="true" aggregation="none" '
                           f'name="" multiplicity="0..1" type="{ids["c:"+e]}" xmi.id="{e2}"/>'
                           f'</UML:Association.connection></UML:Association>')
    out.append('</UML:Namespace.ownedElement>')
    out.append('</UML:Model>')
    # Umbrello extension: um diagrama de classes com widgets posicionados em grade
    out.append('<XMI.extension xmi.extender="umbrello">')
    out.append('<diagrams>')
    out.append('<diagram name="Classes — Documento 0" xmi.id="9000" type="1" snapgrid="0" showattsig="1" '
               'showopsig="1" showattributes="1" showoperations="0" showpackage="0" localid="9000" '
               'linewidth="0" font="Sans Serif,9,-1,5,50,0,0,0,0,0">')
    out.append('<widgets>')
    allc = ["AbstractAuditoria","LancamentoFinanceiro"]+ENT
    for k,name in enumerate(allc):
        gx = 40 + (k % 6) * 260
        gy = 40 + (k // 6) * 220
        out.append(f'<classwidget xmi.id="{ids["c:"+name]}" x="{gx}" y="{gy}" width="230" height="170" '
                   f'showattributes="1" showoperations="0" showpackage="0" showscope="1" '
                   f'font="Sans Serif,9,-1,5,50,0,0,0,0,0" usesdiagramfillcolor="1" usesdiagramusefillcolor="1"/>')
    out.append('</widgets>')
    out.append('<messages/>')
    out.append('<associations>')
    parent_of = {"LancamentoFinanceiro": "AbstractAuditoria"}
    for e in ENT:
        parent_of[e] = sup.get(e, "AbstractAuditoria")
    for k in list(ids):
        if k.startswith("g:"):
            child = k[2:]
            par = parent_of.get(child, "AbstractAuditoria")
            out.append(f'<assocwidget xmi.id="{ids[k]}" type="500" '
                       f'widgetaid="{ids["c:"+child]}" widgetbid="{ids["c:"+par]}" '
                       f'indexa="0" indexb="0" totalcounta="0" totalcountb="0"><linepath/></assocwidget>')
    out.append('</associations>')
    out.append('</diagram>')
    out.append('</diagrams>')
    out.append('</XMI.extension>')
    out.append('</XMI.content>')
    out.append('</XMI>')
    open(os.path.join(OUT, "documento-0-fundacao-classes.xmi"), "w", encoding="utf-8").write("\n".join(out))
    print("XMI ok")

def _tablecell(cells, cid, title, rows, x, y, w, fill, stroke):
    HDR, ROW = 30, 22
    h = HDR + ROW*max(1,len(rows))
    tstyle = (f"shape=table;startSize={HDR};container=1;collapsible=0;childLayout=tableLayout;"
              f"fixedRows=1;rowLines=0;fontStyle=1;align=center;resizeLast=1;html=1;fontFamily=Helvetica;"
              f"fontSize=12;fillColor={fill};strokeColor={stroke};fontColor={stroke};")
    cells.append(f'<mxCell id="{cid}" parent="1" style="{esc(tstyle)}" value="{esc(title)}" vertex="1">'
                 f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    for i,(txt,bold,color) in enumerate(rows):
        rid=f"{cid}_r{i}"
        rowstyle=("shape=tableRow;horizontal=0;startSize=0;swimlaneHead=0;swimlaneBody=0;fillColor=none;"
                  "collapsible=0;dropTarget=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;"
                  "top=0;left=0;right=0;bottom=0;html=1;")
        cells.append(f'<mxCell id="{rid}" parent="{cid}" style="{esc(rowstyle)}" value="" vertex="1">'
                     f'<mxGeometry y="{HDR+i*ROW}" width="{w}" height="{ROW}" as="geometry"/></mxCell>')
        cstyle=(f"shape=partialRectangle;html=1;whiteSpace=wrap;connectable=0;strokeColor=inherit;overflow=hidden;"
                f"fillColor=none;top=0;left=0;bottom=1;right=0;pointerEvents=1;align=left;fontColor={color};"
                f"fontFamily=Helvetica;fontSize=10;spacingLeft=6;fontStyle={'1' if bold else '0'};")
        cells.append(f'<mxCell id="{rid}_c" parent="{rid}" style="{esc(cstyle)}" value="{esc(txt)}" vertex="1">'
                     f'<mxGeometry width="{w}" height="{ROW}" as="geometry">'
                     f'<mxRectangle width="{w}" height="{ROW}" as="alternateBounds"/></mxGeometry></mxCell>')
    return h

def gen_classes_drawio():
    W=260; GX=90; GY=80; PERROW=6
    cells=['<mxCell id="0"/>','<mxCell id="1" parent="0"/>']
    AA=[("+dataCriacao : Instant",0,"#555"),("+criadoPor : String",0,"#555"),
        ("+dataAlteracao : Instant",0,"#555"),("+alteradoPor : String",0,"#555"),
        ("+dataExclusao : Instant",0,"#555"),("+excluidoPor : String",0,"#555")]
    LF=[("+competencia : YearMonth",0,"#555"),("+valor : BigDecimal",0,"#555"),
        ("+dataLancamento : LocalDate",0,"#555"),("+origem : OrigemLancamento",0,"#555")]
    _tablecell(cells,"AbstractAuditoria","«abstract» AbstractAuditoria",AA,760,20,W,"#F2F2F2","#555555")
    _tablecell(cells,"LancamentoFinanceiro","«abstract» LancamentoFinanceiro",LF,1120,20,W,"#F2F2F2","#555555")
    cells.append('<mxCell id="gLF" parent="1" style="edgeStyle=orthogonalEdgeStyle;endArrow=block;endFill=0;'
                 'endSize=16;html=1;strokeColor=#555555;rounded=0;" edge="1" source="LancamentoFinanceiro" '
                 'target="AbstractAuditoria"><mxGeometry relative="1" as="geometry"/></mxCell>')
    ENTm = {
      "Perfil":("perf","AbstractAuditoria"),"Permissao":("perm","AbstractAuditoria"),"PerfilPermissao":("pepe","AbstractAuditoria"),
      "Usuario":("usu","AbstractAuditoria"),"Categoria":("cate","AbstractAuditoria"),
      "CategoriaProvedor":("capr","AbstractAuditoria"),"InstituicaoFinanceira":("infi","AbstractAuditoria"),
      "Conta":("cta","AbstractAuditoria"),"CartaoCredito":("cacr","AbstractAuditoria"),
      "TransacaoBancaria":("trba","LancamentoFinanceiro"),"FaturaCartao":("ftca","AbstractAuditoria"),
      "Receita":("rece","LancamentoFinanceiro"),"Despesa":("desp","LancamentoFinanceiro"),
      "DespesaUsuario":("depu","AbstractAuditoria"),"Investimento":("inve","AbstractAuditoria"),
      "OpfiProvedor":("ofpv","AbstractAuditoria"),"OpfiInstituicaoProvedor":("ofip","AbstractAuditoria"),
      "OpfiCredencial":("ofcr","AbstractAuditoria"),"OpfiConexao":("ofcx","AbstractAuditoria"),
      "OpfiConsentimento":("ofcs","AbstractAuditoria"),"OpfiContaExterna":("ofce","AbstractAuditoria"),
      "OpfiTransacao":("oftr","AbstractAuditoria"),"OpfiFatura":("offa","AbstractAuditoria"),
      "OpfiInvestimento":("ofin","AbstractAuditoria"),"OpfiEventoWebhook":("ofev","AbstractAuditoria"),
      "OpfiSincronizacao":("ofsi","AbstractAuditoria")}
    sid2ent = {v[0]: k for k, v in ENTm.items()}
    names = list(ENTm)
    for k,ent in enumerate(names):
        sid,sup = ENTm[ent]
        t=BYID[sid]
        rows=[]
        for (c,ty,fl) in t[3]:
            if fl.startswith("FK:"):
                an=c.split("_",1)[1].lower() if "_" in c else c.lower()
                rows.append((f"+ {an} : {sid2ent.get(fl[3:], fl[3:])}", 0, "#8A4B08"))
            else:
                an=c.split("_",1)[1].lower() if "_" in c else c.lower()
                bold = 1 if fl=="PK" else 0
                rows.append((f"+ {an} : {ty.split()[0]}", bold, "#333333"))
        camada=t[2]
        fill="#FFF4E5" if camada=="opfi" else "#EAF2FB"
        stroke="#B26B00" if camada=="opfi" else "#1F3864"
        x=40 + (k % PERROW)*(W+GX)
        y=360 + (k // PERROW)*440
        _tablecell(cells, ent, ent, rows, x, y, W, fill, stroke)
        tgt = "LancamentoFinanceiro" if sup=="LancamentoFinanceiro" else "AbstractAuditoria"
        cells.append(f'<mxCell id="g_{ent}" parent="1" style="edgeStyle=orthogonalEdgeStyle;endArrow=block;'
                     f'endFill=0;endSize=12;html=1;strokeColor=#B0B0B0;dashed=1;rounded=0;" edge="1" '
                     f'source="{ent}" target="{tgt}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    en=0
    for ent in names:
        sid,sup=ENTm[ent]; t=BYID[sid]
        for (c,ty,fl) in t[3]:
            if fl.startswith("FK:"):
                dst=sid2ent.get(fl[3:])
                if not dst: continue
                en+=1
                nm=c.split("_",1)[1].lower() if "_" in c else c.lower()
                cells.append(f'<mxCell id="a{en}" parent="1" style="edgeStyle=entityRelationEdgeStyle;'
                             f'endArrow=open;endFill=1;html=1;strokeColor=#5B7FA6;fontSize=9;fontFamily=Helvetica;" '
                             f'value="{esc(nm)}" edge="1" source="{ent}" target="{dst}">'
                             f'<mxGeometry relative="1" as="geometry"/></mxCell>')
    xml=(f'<mxfile host="app.diagrams.net"><diagram id="classes" name="Classes — Documento 0">'
         f'<mxGraphModel dx="1600" dy="1000" grid="0" gridSize="10" guides="1" tooltips="1" connect="1" '
         f'arrows="1" fold="1" page="1" pageScale="1" pageWidth="4000" pageHeight="2600" math="0" shadow="0">'
         f'<root>{"".join(cells)}</root></mxGraphModel></diagram></mxfile>')
    open(os.path.join(OUT,"documento-0-fundacao-classes.drawio"),"w", encoding="utf-8").write(xml)
    print("classes.drawio:", en, "assoc")

gen_der()
gen_classes_drawio()
gen_puml()
gen_xmi()
print("feito ->", OUT)
