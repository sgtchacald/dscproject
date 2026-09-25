#!/usr/bin/env python3
# Diagramas draw.io do documento 2.05 - Manter Receita.
# Gera 3 arquivos: wireframes das telas, DER do subconjunto e casos de uso.
# Marcadores numericos emitidos POR ULTIMO em cada pagina (ficam na frente no PNG),
# em parent="1".
import html, os, xml.dom.minidom as M

OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1280, 860
def e(s): return html.escape(str(s), quote=True)

INK="#1B2430"; MUT="#69747F"; BORD="#D8DEE6"; SURF="#FFFFFF"; HDRF="#F1F3F6"
PRIM="#1F6FD0"; MARK="#D1367F"; EDIT="#FFF8E1"; EDGE="#E0A800"

class P:
    def __init__(s, name):
        s.name = name; s.b = []; s.m = []; s.n = 0
    def i(s):
        s.n += 1
        return f"c{abs(hash(s.name)) % 9973}_{s.n}"
    def rect(s, x, y, w, h, v="", st=""):
        style = (f"rounded=0;whiteSpace=wrap;html=1;fillColor={SURF};strokeColor={BORD};"
                 f"fontColor={INK};fontFamily=Helvetica;fontSize=12;{st}")
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def line(s, x1, y1, x2, y2):
        s.b.append(f'<mxCell id="{s.i()}" style="endArrow=none;html=1;strokeColor={BORD};" edge="1" parent="1">'
                   f'<mxGeometry as="geometry"><Array as="points"><mxPoint x="{x1}" y="{y1}"/>'
                   f'<mxPoint x="{x2}" y="{y2}"/></Array></mxGeometry></mxCell>')
    def txt(s, x, y, w, h, v, st=""):
        style = (f"text;html=1;align=left;verticalAlign=middle;fontFamily=Helvetica;"
                 f"fontColor={INK};fontSize=12;{st}")
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def btn(s, x, y, w, v, pri=False):
        f = PRIM if pri else SURF
        fc = "#FFFFFF" if pri else INK
        style = (f"rounded=1;arcSize=30;whiteSpace=wrap;html=1;fillColor={f};"
                 f"strokeColor={PRIM if pri else BORD};fontColor={fc};fontFamily=Helvetica;fontSize=12;fontStyle=1;")
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="34" as="geometry"/></mxCell>')
    def fld(s, x, y, w, lb, edit=False):
        s.txt(x, y, w, 16, lb, f"fontSize=11;fontColor={MUT};fontStyle=1;")
        s.rect(x, y + 18, w, 32, "",
               f"fillColor={EDIT if edit else '#FBFCFD'};strokeColor={EDGE if edit else BORD};")
    def mk(s, x, y, num):
        st = (f"ellipse;whiteSpace=wrap;html=1;fillColor={MARK};strokeColor=#FFFFFF;strokeWidth=2;"
              f"fontColor=#FFFFFF;fontFamily=Helvetica;fontSize=11;fontStyle=1;")
        s.m.append(f'<mxCell id="mk{s.n}_{num}" value="{num}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="22" height="22" as="geometry"/></mxCell>')
    def page(s, pw=W, ph=H):
        cells = ('<mxCell id="0"/><mxCell id="1" parent="0"/>'
                 + "".join(s.b) + "".join(s.m))
        return (f'<diagram name="{e(s.name)}"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                f'pageWidth="{pw}" pageHeight="{ph}" math="0" shadow="0"><root>{cells}</root>'
                f'</mxGraphModel></diagram>')


NAV = ["Início", "Minhas Contas", "Receitas", "Despesas",
       "Instituições Financeiras", "Categorias"]

def shell(p, titulo, ativo):
    p.rect(0, 0, W, H, "", "fillColor=#F5F6F8;strokeColor=none;")
    p.rect(0, 0, 224, H, "", f"fillColor={SURF};strokeColor={BORD};")
    p.txt(20, 22, 180, 20, "dscproject", "fontStyle=1;fontSize=14;")
    for idx, t in enumerate(NAV):
        y = 70 + idx * 34
        a = (t == ativo)
        if a:
            p.rect(12, y - 4, 200, 28, "", "fillColor=#E8F0FB;strokeColor=none;")
        p.txt(24, y, 190, 20, t, f"fontColor={PRIM if a else MUT};{'fontStyle=1;' if a else ''}")
    p.rect(224, 0, W - 224, 56, "", f"fillColor={SURF};strokeColor={BORD};")
    p.txt(248, 18, 360, 20, titulo, "fontStyle=1;")
    p.txt(W - 240, 18, 220, 20, "Diego Cordeiro (DC)", f"fontColor={MUT};align=right;")


def wrap(pages, pw=W, ph=H):
    return ('<mxfile host="app.diagrams.net">'
            + "".join(pg.page(pw, ph) for pg in pages) + '</mxfile>')


# =========================================================================
# ARQUIVO 1 - WIREFRAMES DAS TELAS
# =========================================================================
CX = 258
gw = W - CX - 40            # 982
rh = 56

# ---- 7.1 Receitas (Listagem) - QUADRO_DESCRITIVO_1 ----
p1 = P("7.1 Receitas - Listagem")
shell(p1, "Receitas", "Receitas")
p1.txt(CX, 74, 560, 16, "Finanças  ›  Receitas", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 460, 28, "Receitas", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 760, 30,
       "Cadastre e acompanhe as suas receitas previstas e recebidas.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W - 300, 94, 110, "Filtrar")
p1.btn(W - 180, 94, 140, "Nova receita", True)

gy = 178
p1.rect(CX, gy, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
COLS = [
    ("COMPETÊNCIA",              CX + 12,  80),
    ("NOME",                     CX + 100, 130),
    ("CATEGORIA",                CX + 238, 100),
    ("CONTA",                    CX + 344, 130),
    ("VALOR",                    CX + 480, 80),
    ("DATA DE LANÇAMENTO",       CX + 566, 100),
    ("SITUAÇÃO",                 CX + 672, 80),
    ("DATA DE RECEBIMENTO",      CX + 758, 100),
    ("ORIGEM",                   CX + 864, 80),
    ("AÇÃO",                     CX + 950, 66),
]
for lb, x, wd in COLS:
    p1.txt(x, gy + 8, wd, 24, lb, f"fontSize=9;fontColor={MUT};fontStyle=1;")

rows = [
    ("09/2026", "Salário",                    "Salário",              "Nubank Conta Corrente", "R$ 6.500,00", "05/09/2026", "Recebida", "05/09/2026", "Manual"),
    ("09/2026", "Freela Site Institucional",  "Freelance / Renda Extra","Nubank Conta Corrente", "R$ 1.800,00", "15/09/2026", "Prevista", "—",          "Manual"),
    ("09/2026", "Reembolso viagem",           "Reembolso",            "Carteira / Dinheiro em espécie","R$ 450,00","10/09/2026","Recebida","12/09/2026","Manual"),
    ("08/2026", "Dividendos XP",              "Rendimentos",          "XP Investimentos",      "R$ 342,15",   "28/08/2026", "Recebida", "28/08/2026", "Open Finance"),
    ("10/2026", "Salário",                    "Salário",              "Nubank Conta Corrente", "R$ 6.500,00", "05/10/2026", "Prevista", "—",          "Manual"),
]
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw, ry)
    for (lb, x, wd), v in zip(COLS[:9], r):
        st = "fontSize=10;"
        if v in ("Prevista",):
            st += f"fontColor={MUT};"
        p1.txt(x, ry + 18, wd, 24, v, st)
    p1.txt(COLS[9][1], ry + 18, COLS[9][2], 16, "✎   💰   🗑", f"fontSize=12;fontColor={MUT};")
p1.txt(CX + 12, gy + 380, 520, 16, "Mostrando 5 de 6", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw - 120, gy + 380, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 414, gw, 40,
       "Grid client-side (DataTables) sobre a lista completa carregada uma vez (EDP02 → C1). O modal Filtrar aplica em memória (RT02). "
       "Nova receita e ícones de ação = PERM_RECEITAS_MANTER; a tela = PERM_RECEITAS_LISTAR. O grid nunca traz receita de outro usuário (RN02). "
       "Ícones de ação: Editar (RT05), Registrar recebimento (RT08, oculto quando já recebida), Excluir (RT07, oculto quando excluída ou origem ≠ MANUAL — RN08).",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(200, 70 + 2 * 34 - 2, 0)               # LINK -> item de menu "Receitas"
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W - 308, 86, 4)
p1.mk(W - 46, 86, 5)
p1.mk(CX - 22, gy + 78, 6)
for j, (lb, x, wd) in enumerate(COLS):
    p1.mk(x + min(wd, 90) - 20, gy + 6, 7 + j)   # 7..16

# ---- 7.2 Modal Filtrar Receitas - QUADRO_DESCRITIVO_2 ----
p2 = P("7.2 Modal Filtrar Receitas")
p2.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 560, (W - 560) // 2, 120
p2.rect(mx, my, mw, 452, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Receitas", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (nome ou descrição — sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "COMPETÊNCIA INICIAL   (mês/ano)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "COMPETÊNCIA FINAL   (mês/ano)")
p2.fld(mx + 24, my + 226, cw, "SITUAÇÃO   (Todas / Prevista / Recebida / Excluída)")
p2.fld(mx + 24 + cw + 16, my + 226, cw, "CONTA   (Todas + opções do módulo 06 — SB01)")
p2.fld(mx + 24, my + 302, cw, "CATEGORIA   (Todas + opções do módulo 04 — SB02)")
p2.btn(mx + mw - 230, my + 380, 100, "Limpar")
p2.btn(mx + mw - 120, my + 380, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx - 11, my + 230, 5)
p2.mk(mx + 24 + cw + 4, my + 230, 6)
p2.mk(mx - 11, my + 306, 7)
p2.mk(mx + mw - 124, my + 372, 8)
p2.mk(mx + mw - 234, my + 372, 9)

# ---- 7.3 Modal Cadastro / Edicao de Receita - QUADRO_DESCRITIVO_3 ----
p3 = P("7.3 Modal Receita")
p3.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 600, (W - 600) // 2, 40
p3.rect(mx, my, mw, 780, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 400, 20, "Nova receita  /  Editar receita", "fontStyle=1;fontSize=14;")
cw3 = (mw - 64) // 2
p3.rect(mx + 24, my + 66, mw - 48, 40,
        "ⓘ  Modo edição: exibido em receita importada — \"Esta receita foi importada do Open Finance. A conta e a origem não podem ser alteradas, e ela não pode ser excluída por esta tela.\"",
        f"fillColor=#EAF1FB;strokeColor={PRIM};fontColor={PRIM};fontSize=9;align=left;spacingLeft=8;")
p3.fld(mx + 24, my + 120, mw - 48, "NOME *   (100)", edit=True)
p3.fld(mx + 24, my + 190, mw - 48, "DESCRIÇÃO   (512, opcional)")
p3.fld(mx + 24, my + 280, cw3, "VALOR *   (monetário; > 0 — RN05)", edit=True)
p3.fld(mx + 24 + cw3 + 16, my + 280, cw3, "DATA DE LANÇAMENTO *   (default: hoje)")
p3.fld(mx + 24, my + 350, cw3, "COMPETÊNCIA *   (mês/ano; segue a data — RT11)")
p3.fld(mx + 24 + cw3 + 16, my + 350, cw3, "CONTA *   (SB01; DESABILITADA se importada — RN08)")
p3.fld(mx + 24, my + 420, mw - 48, "CATEGORIA   (SB02; opcional)")
p3.txt(mx + 24, my + 490, cw3, 16, "RECEBIDO   (default: Não — RT12)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, my + 508, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.fld(mx + 24 + cw3 + 16, my + 490, cw3, "DATA DE RECEBIMENTO   (só quando Recebido = Sim — RN06)", edit=True)
p3.btn(mx + mw - 230, my + 700, 100, "Cancelar")
p3.btn(mx + mw - 120, my + 700, 96, "Salvar", True)
p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 122, 2)
p3.mk(mx - 11, my + 192, 3)
p3.mk(mx - 11, my + 282, 4)
p3.mk(mx + 24 + cw3 + 4, my + 282, 5)
p3.mk(mx - 11, my + 352, 6)
p3.mk(mx + 24 + cw3 + 4, my + 352, 7)
p3.mk(mx - 11, my + 422, 8)
p3.mk(mx - 11, my + 504, 9)
p3.mk(mx + 24 + cw3 + 4, my + 504, 10)
p3.mk(mx - 11, my + 66, 11)
p3.mk(mx + mw - 124, my + 692, 12)
p3.mk(mx + mw - 234, my + 692, 13)

# ---- 7.4 Modal Registrar Recebimento - QUADRO_DESCRITIVO_4 ----
p4 = P("7.4 Modal Registrar Recebimento")
p4.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 220
p4.rect(mx, my, mw, 340, "", f"fillColor={SURF};strokeColor={BORD};")
p4.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p4.txt(mx + 20, my + 15, 440, 20, "Registrar recebimento — {nome da receita}", "fontStyle=1;fontSize=14;")
p4.txt(mx + 24, my + 74, mw - 48, 16, "VALOR DA RECEITA   (somente leitura)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p4.rect(mx + 24, my + 92, mw - 48, 32, "R$ 1.800,00", f"fillColor=#F5F6F8;strokeColor={BORD};fontStyle=2;dashed=1;")
p4.fld(mx + 24, my + 140, mw - 48, "DATA DE RECEBIMENTO *   (default: hoje)", edit=True)
p4.btn(mx + mw - 250, my + 260, 110, "Cancelar")
p4.btn(mx + mw - 130, my + 260, 106, "Registrar", True)
p4.mk(mx - 11, my + 4, 1)
p4.mk(mx - 11, my + 96, 2)
p4.mk(mx - 11, my + 144, 3)
p4.mk(mx + mw - 134, my + 252, 4)
p4.mk(mx + mw - 254, my + 252, 5)

xml1 = wrap([p1, p2, p3, p4])
M.parseString(xml1)
open(os.path.join(OUT, "manter-receita-prototipo.drawio"), "w", encoding="utf-8").write(xml1)

# =========================================================================
# ARQUIVO 2 - DER (subconjunto do Documento 0)
# =========================================================================
class Der:
    def __init__(s): s.b = []; s.k = 0
    def i(s): s.k += 1; return f"d{s.k}"
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def entity(s, x, y, title, fields, fill, stroke, wd=320):
        th, rh = 28, 22
        h = th + rh * len(fields)
        tid = s.i()
        s.b.append(f'<mxCell id="{tid}" value="{e(title)}" style="shape=table;startSize={th};container=1;'
                   f'collapsible=0;childLayout=tableLayout;fixedRows=1;rowLines=0;fontStyle=1;align=center;html=1;'
                   f'fontFamily=Helvetica;fontSize=12;fillColor={fill};strokeColor={stroke};fontColor={stroke};" '
                   f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{wd}" height="{h}" as="geometry"/></mxCell>')
        for idx, (label, bold) in enumerate(fields):
            rid = f"{tid}r{idx}"
            s.b.append(f'<mxCell id="{rid}" value="" style="shape=tableRow;horizontal=0;startSize=0;collapsible=0;'
                       f'dropTarget=0;fillColor=none;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;html=1;" '
                       f'vertex="1" parent="{tid}"><mxGeometry y="{th + idx * rh}" width="{wd}" height="{rh}" as="geometry"/></mxCell>')
            s.b.append(f'<mxCell id="{rid}c" value="{e(label)}" style="shape=partialRectangle;html=1;whiteSpace=wrap;'
                       f'connectable=0;strokeColor=inherit;fillColor=none;top=0;left=0;bottom=1;right=0;align=left;'
                       f'fontColor=#333;fontFamily=Helvetica;fontSize=10;spacingLeft=6;fontStyle={1 if bold else 0};" '
                       f'vertex="1" parent="{rid}"><mxGeometry width="{wd}" height="{rh}" as="geometry">'
                       f'<mxRectangle width="{wd}" height="{rh}" as="alternateBounds"/></mxGeometry></mxCell>')
        return tid
    def edge(s, src, dst, lbl, dashed=False):
        d = "dashed=1;" if dashed else ""
        s.b.append(f'<mxCell id="{s.i()}" value="{e(lbl)}" style="endArrow=ERmany;startArrow=ERone;html=1;{d}'
                   f'fontFamily=Helvetica;fontSize=10;fontColor={MUT};strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{src}" target="{dst}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="DER - Manter Receita"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1360" pageHeight="900" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1360, 900, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 18, 1280, 20,
       "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_9 (RECEITAS), _5 (CONTAS), _3 (CATEGORIAS) e _2 (USUARIOS). "
       "Este documento não cria tabela nova nem faz ALTER TABLE.",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")
usu = d.entity(60, 80, "USUARIOS", [
    ("USU_ID  (PK)", 1), ("USU_NOME", 0),
    ("USU_LOGIN  (UK)  ·  USU_EMAIL  (UK)", 0),
    ("PERF_ID  (FK → PERFIS)", 0),
    ("dono da receita — via CONTAS.USU_ID", 0),
    ("audit_*", 0),
], "#F1EAF7", "#5B3B86", wd=360)
cta = d.entity(60, 380, "CONTAS", [
    ("CTA_ID  (PK)", 1), ("CTA_DESCRICAO", 0),
    ("CTA_TIPO  {CORRENTE|POUPANCA|INVESTIMENTO|CARTEIRA}", 0),
    ("CTA_FL_ATIVO  BOOLEAN", 0),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 1),
    ("catálogo do documento 2.03 — só leitura aqui", 0),
    ("audit_*", 0),
], "#EAF2FB", "#1F3864", wd=360)
cate = d.entity(60, 680, "CATEGORIAS", [
    ("CATE_ID  (PK)", 1), ("CATE_NOME", 0),
    ("CATE_APLICA_A  {RECEITA|DESPESA|AMBOS}", 0),
    ("CATE_FL_ATIVO  BOOLEAN", 0),
    ("catálogo do documento 2.01 — só leitura aqui", 0),
    ("audit_*", 0),
], "#EAF7EF", "#1F6B45", wd=360)
rec = d.entity(520, 200, "RECEITAS", [
    ("RECE_ID  (PK)", 1),
    ("RECE_COMPETENCIA  CHAR(7)  'yyyy-MM'  NOT NULL", 0),
    ("RECE_NOME  VARCHAR(100)  NOT NULL", 0),
    ("RECE_DESCRICAO  VARCHAR(512)  NULL", 0),
    ("RECE_VALOR  DECIMAL(15,2)  NOT NULL  (> 0 — RN05)", 0),
    ("RECE_DT_LANCAMENTO  DATE  NOT NULL", 0),
    ("RECE_FL_RECEBIDO  BOOLEAN  DEFAULT FALSE", 0),
    ("RECE_DT_RECEBIMENTO  DATE  NULL", 0),
    ("RECE_ORIGEM  VARCHAR(20)  {MANUAL|OPEN_FINANCE|IMPORTACAO}  DEFAULT 'MANUAL'", 0),
    ("CTA_ID  (FK → CONTAS)  NULLABLE no schema · obrigatória na aplicação (RN03)", 1),
    ("CATE_ID  (FK → CATEGORIAS)  NULLABLE", 1),
    ("audit_*  — @Audited (Hibernate Envers)", 0),
], "#FBF3E6", "#8A5A12", wd=460)
opfi = d.entity(1060, 200, "OPFI_TRANSACOES", [
    ("OPFI_ID  (PK)", 1),
    ("vínculo LÓGICO com a receita gerada — não é FK", 0),
    ("base da restrição de exclusão da receita importada (RN08)", 0),
    ("Documento 0, Observações 19 e 21", 0),
], "#F3F0FB", "#5B3B86", wd=280)
d.edge(usu, cta, "1  →  N   USU_ID (NOT NULL)")
d.edge(cta, rec, "1  →  N   CTA_ID (NULLABLE no schema · obrigatório na aplicação — RN03)")
d.edge(cate, rec, "1  →  N   CATE_ID (NULLABLE)", dashed=True)
d.edge(opfi, rec, "vínculo lógico (não FK) — RN08", dashed=True)
d.rect(520, 480, 460, 220,
       "RECEITAS: CRUD + registro de recebimento, sempre no escopo do USU_ID resolvido por CTA_ID → CONTAS.USU_ID (RN02).\n\n"
       "Sem trava de \"em uso\": nenhuma tabela do domínio tem FK para RECEITAS (RN09). A única restrição de exclusão é a "
       "receita importada do Open Finance (RECE_ORIGEM ≠ MANUAL — RN08), cuja transação de staging guarda um vínculo lógico "
       "(não físico) com a receita gerada.\n\n"
       "CONTAS e CATEGORIAS vêm dos documentos 2.03 e 2.01 — só leitura nesta tela.",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-receita-der.drawio"), "w", encoding="utf-8").write(xml2)

# =========================================================================
# ARQUIVO 3 - CASOS DE USO
# =========================================================================
class Uc:
    def __init__(s): s.b = []; s.k = 0
    def i(s): s.k += 1; return f"u{s.k}"
    def actor(s, x, y, name):
        cid = s.i()
        s.b.append(f'<mxCell id="{cid}" value="{e(name)}" style="shape=umlActor;verticalLabelPosition=bottom;'
                   f'verticalAlign=top;html=1;fontFamily=Helvetica;fontSize=11;fontColor={INK};" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="36" height="80" as="geometry"/></mxCell>')
        return cid
    def uc(s, x, y, code, name):
        cid = s.i()
        s.b.append(f'<mxCell id="{cid}" value="{e(code + " — " + name)}" style="ellipse;whiteSpace=wrap;html=1;'
                   f'fillColor={SURF};strokeColor={PRIM};fontColor={INK};fontFamily=Helvetica;fontSize=11;" '
                   f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="270" height="50" as="geometry"/></mxCell>')
        return cid
    def link(s, a, b):
        s.b.append(f'<mxCell id="{s.i()}" style="endArrow=none;html=1;strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{a}" target="{b}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="Casos de Uso - Manter Receita"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1200" pageHeight="740" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1200, 740, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(360, 40, 540, 640, "Manter Receita — tela \"Receitas\"",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(120, 210, "ADMIN (PERF01)")
usr = u.actor(120, 430, "USER (PERF02)")
ucs = [
    ("CAUS01", "Listar Minhas Receitas"),
    ("CAUS02", "Filtrar Receitas"),
    ("CAUS03", "Cadastrar Receita"),
    ("CAUS04", "Editar Receita"),
    ("CAUS05", "Registrar Recebimento"),
    ("CAUS06", "Excluir Receita"),
]
ids = [u.uc(475, 70 + idx * 100, c_, n) for idx, (c_, n) in enumerate(ucs)]
for cid in ids:
    u.link(adm, cid)
    u.link(usr, cid)
u.rect(360, 686, 540, 40,
       "ADMIN e USER têm o mesmo acesso; cada um opera só sobre as próprias receitas (RN02). "
       "CAUS06 não se aplica a receitas importadas do Open Finance (RN08).",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-receita-casos-uso.drawio"), "w", encoding="utf-8").write(xml3)

print("ok - 3 arquivos gerados (prototipo 4 telas, der, casos-uso)")
