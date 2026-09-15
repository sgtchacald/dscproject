#!/usr/bin/env python3
# Diagramas draw.io do documento 05 - Manter Instituicao Financeira.
# Gera 3 arquivos: wireframes das telas, DER do subconjunto e casos de uso.
# Marcadores numericos emitidos POR ULTIMO em cada pagina (ficam na frente no PNG).
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


NAV = ["Início", "Contas", "Despesas", "Usuários", "Parâmetros Globais",
       "Instituições Financeiras", "Instituições por Provedor"]

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
rh = 60

# ---- 7.1 Instituicoes Financeiras (Listagem) - QUADRO_DESCRITIVO_1 ----
p1 = P("7.1 Instituicoes Financeiras - Listagem")
shell(p1, "Instituições Financeiras", "Instituições Financeiras")
p1.txt(CX, 74, 560, 16, "Administração  ›  Instituições Financeiras", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 460, 28, "Instituições Financeiras", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 700, 30,
       "Gerencie o catálogo de bancos e corretoras usado em contas, cartões e investimentos.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W - 300, 94, 110, "Filtrar")
p1.btn(W - 180, 94, 140, "Nova instituição", True)

gy = 178
p1.rect(CX, gy, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
COLS = [
    ("NOME",                 CX + 12,  230),
    ("CÓDIGO",               CX + 250, 92),
    ("TIPO",                 CX + 356, 96),
    ("Nº DE USOS",           CX + 470, 96),
    ("PROVEDORES MAPEADOS",  CX + 584, 168),
    ("SITUAÇÃO",             CX + 772, 92),
    ("AÇÃO",                 CX + 900, 62),
]
for lb, x, wd in COLS:
    p1.txt(x, gy + 11, wd, 16, lb, f"fontSize=10;fontColor={MUT};fontStyle=1;")

rows = [
    ("Banco do Brasil",          "001", "Banco",     "6",  "2", "Ativa"),
    ("Itaú Unibanco  ·  Sistema", "341", "Banco",     "9",  "2", "Ativa"),
    ("Nubank  ·  Sistema",        "260", "Banco",     "12", "2", "Ativa"),
    ("XP Investimentos  ·  Sist.","102", "Corretora", "7",  "1", "Ativa"),
    ("Banco Antigo S.A.",         "999", "Banco",     "1",  "0", "Inativa"),
]
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw, ry)
    for (lb, x, wd), v in zip(COLS[:6], r):
        st = "fontSize=11;"
        if v in ("Inativa",):
            st += f"fontColor={MUT};"
        p1.txt(x, ry + 19, wd, 24, v, st)
    p1.txt(COLS[6][1], ry + 19, COLS[6][2], 16, "✎   🗑", f"fontSize=13;fontColor={MUT};")
p1.txt(CX + 12, gy + 380, 520, 16, "Mostrando 1–5 de 5", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw - 120, gy + 380, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 414, gw, 30,
       "Grid client-side (DataTables) sobre a lista completa carregada uma vez (EDP02). O modal Filtrar aplica em memória (RT01/RT02). "
       "Nova instituição e ícones de ação = PERM_INSTITUICOES_MANTER; a tela = PERM_INSTITUICOES_LISTAR. "
       "Excluir some quando a instituição é de sistema ou já está excluída.",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(200, 70 + 5 * 34 - 2, 0)               # LINK -> item de menu "Instituicoes Financeiras"
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W - 308, 86, 4)
p1.mk(W - 46, 86, 5)
p1.mk(CX - 22, gy + 78, 6)
for j, (lb, x, wd) in enumerate(COLS):
    p1.mk(x + wd - 20, gy + 7, 7 + j)        # 7..13

# ---- 7.2 Modal Filtrar Instituicoes - QUADRO_DESCRITIVO_2 ----
p2 = P("7.2 Modal Filtrar Instituicoes")
p2.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 170
p2.rect(mx, my, mw, 330, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Instituições", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (nome ou código, sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "TIPO   (Todos / Banco / Corretora)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "SITUAÇÃO   (Ativa / Inativa / Excluída / Todas)")
p2.btn(mx + mw - 230, my + 248, 100, "Limpar")
p2.btn(mx + mw - 120, my + 248, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx + mw - 124, my + 240, 5)
p2.mk(mx + mw - 234, my + 240, 6)

# ---- 7.3 Modal Cadastro / Edicao de Instituicao - QUADRO_DESCRITIVO_3 ----
p3 = P("7.3 Modal Instituicao")
p3.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 560, (W - 560) // 2, 120
p3.rect(mx, my, mw, 560, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 360, 20, "Editar instituição", "fontStyle=1;fontSize=14;")
p3.fld(mx + 24, my + 70, mw - 48, "NOME *   (100; travado p/ instituição de sistema)", edit=True)
p3.fld(mx + 24, my + 146, mw - 48, "CÓDIGO   (COMPE 3 díg. ou ISPB 8 díg.; só dígitos; travado p/ sistema)", edit=True)
cw3 = (mw - 64) // 2
p3.fld(mx + 24, my + 222, cw3, "TIPO *   (Banco / Corretora)")
p3.txt(mx + 24 + cw3 + 16, my + 222, cw3, 16, "ATIVA   (só na edição)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24 + cw3 + 16, my + 240, 110, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.rect(mx + 24, my + 300, mw - 48, 46,
        "ⓘ  Instituição da lista-base do sistema: nome e código não podem ser alterados e ela não pode ser excluída.",
        f"fillColor=#EAF1FB;strokeColor={PRIM};fontColor={PRIM};fontSize=10;align=left;spacingLeft=8;")
p3.btn(mx + mw - 230, my + 428, 100, "Cancelar")
p3.btn(mx + mw - 120, my + 428, 96, "Salvar", True)
p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 74, 2)
p3.mk(mx - 11, my + 150, 3)
p3.mk(mx - 11, my + 226, 4)
p3.mk(mx + 24 + cw3 + 4, my + 226, 5)
p3.mk(mx - 11, my + 306, 6)
p3.mk(mx + mw - 124, my + 420, 7)
p3.mk(mx + mw - 234, my + 420, 8)

# ---- 7.4 Instituicoes por Provedor (Listagem) - QUADRO_DESCRITIVO_4 ----
p4 = P("7.4 Instituicoes por Provedor - Listagem")
shell(p4, "Instituições por Provedor", "Instituições por Provedor")
p4.txt(CX, 74, 600, 16, "Administração  ›  Instituições por Provedor", f"fontColor={MUT};fontSize=11;")
p4.txt(CX, 96, 500, 28, "Instituições por Provedor", "fontStyle=1;fontSize=20;")
p4.txt(CX, 130, 720, 30,
       "Associe cada instituição ao id que os provedores de Open Finance usam para identificá-la.",
       f"fontColor={MUT};fontSize=11;")
p4.rect(W - 470, 94, 200, 34, "Todos os provedores  ▾", f"fillColor=#FBFCFD;fontSize=11;align=left;spacingLeft=10;")
p4.btn(W - 160, 94, 120, "Novo vínculo", True)

gy4 = 178
p4.rect(CX, gy4, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p4.rect(CX, gy4, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
COLS4 = [
    ("PROVEDOR",    CX + 12,  220),
    ("INSTITUIÇÃO", CX + 280, 320),
    ("ID EXTERNO",  CX + 620, 240),
    ("AÇÃO",        CX + 900, 62),
]
for lb, x, wd in COLS4:
    p4.txt(x, gy4 + 11, wd, 16, lb, f"fontSize=10;fontColor={MUT};fontStyle=1;")
rows4 = [
    ("Belvo",  "Itaú Unibanco",   "itau-br"),
    ("Belvo",  "Nubank",          "nubank-br"),
    ("Pluggy", "Banco do Brasil", "200"),
    ("Pluggy", "Bradesco",        "237"),
    ("Pluggy", "Itaú Unibanco",   "201"),
    ("Pluggy", "Nubank",          "612"),
]
for idx, r in enumerate(rows4):
    ry = gy4 + 38 + idx * rh
    if idx:
        p4.line(CX, ry, CX + gw, ry)
    for (lb, x, wd), v in zip(COLS4[:3], r):
        p4.txt(x, ry + 19, wd, 24, v, "fontSize=11;")
    p4.txt(COLS4[3][1], ry + 19, COLS4[3][2], 16, "✎   🗑", f"fontSize=13;fontColor={MUT};")
p4.txt(CX, gy4 + 414, gw, 30,
       "Grid client-side; filtro de provedor aplicado em memória (RT02). Ordenação padrão: Provedor, depois Instituição. "
       "Par instituição + provedor único (RN08 / MSG11); par provedor + id externo único (RN09 / MSG12). "
       "Novo vínculo e ícones de ação = PERM_INSTITUICOES_PROVEDOR_MANTER.",
       f"fontColor={MUT};fontSize=10;")
p4.mk(200, 70 + 6 * 34 - 2, 0)               # LINK -> item de menu
p4.mk(CX - 24, 70, 1)
p4.mk(CX - 24, 92, 2)
p4.mk(CX - 24, 126, 3)
p4.mk(W - 478, 86, 4)
p4.mk(W - 46, 86, 5)
p4.mk(CX - 22, gy4 + 78, 6)
for j, (lb, x, wd) in enumerate(COLS4):
    p4.mk(x + wd - 20, gy4 + 7, 7 + j)       # 7..10

# ---- 7.5 Modal Vinculo Instituicao x Provedor - QUADRO_DESCRITIVO_5 ----
p5 = P("7.5 Modal Vinculo Instituicao x Provedor")
p5.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 150
p5.rect(mx, my, mw, 396, "", f"fillColor={SURF};strokeColor={BORD};")
p5.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p5.txt(mx + 20, my + 15, 320, 20, "Novo vínculo", "fontStyle=1;fontSize=14;")
p5.fld(mx + 24, my + 74, mw - 48, "PROVEDOR *   (provedores ativos de OPFI_PROVEDORES — EDP14)")
p5.fld(mx + 24, my + 150, mw - 48, "INSTITUIÇÃO *   (instituições ativas — EDP07 / RN11)")
p5.fld(mx + 24, my + 226, mw - 48, "ID EXTERNO *   (80; id do connector como o provedor o devolve; único no provedor)")
p5.btn(mx + mw - 230, my + 320, 100, "Cancelar")
p5.btn(mx + mw - 120, my + 320, 96, "Salvar", True)
p5.mk(mx - 11, my + 4, 1)
p5.mk(mx - 11, my + 78, 2)
p5.mk(mx - 11, my + 154, 3)
p5.mk(mx - 11, my + 230, 4)
p5.mk(mx + mw - 124, my + 312, 5)
p5.mk(mx + mw - 234, my + 312, 6)

xml1 = wrap([p1, p2, p3, p4, p5])
M.parseString(xml1)
open(os.path.join(OUT, "manter-instituicao-financeira-prototipo.drawio"), "w").write(xml1)

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
        return ('<diagram name="DER - Manter Instituicao Financeira"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1280" pageHeight="820" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1280, 820, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 18, 1100, 20,
       "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_4, _13 e _14. Este documento não cria tabela nova.",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")
infi = d.entity(60, 70, "INSTITUICOES_FINANCEIRAS", [
    ("INFI_ID  (PK)", 1), ("INFI_NOME  (UK)", 1),
    ("INFI_CODIGO  (UK quando informado, NULL)", 0),
    ("INFI_TIPO_INSTITUICAO  CHAR(1) {B|C}", 0),
    ("INFI_FL_ATIVO  (BOOLEAN, novo no Doc 0)", 0),
    ("INFI_FL_SISTEMA  (BOOLEAN, novo no Doc 0)", 0),
    ("audit_*  (criação/alteração/exclusão)", 0),
], "#EAF2FB", "#1F3864", wd=380)
prov = d.entity(60, 470, "OPFI_PROVEDORES", [
    ("OFPV_ID  (PK)", 1), ("OFPV_CODIGO  (UK)", 0), ("OFPV_NOME", 0),
    ("OFPV_FL_ATIVO  (BOOLEAN)", 0), ("audit_*", 0),
], "#EAF7EF", "#1F6B45", wd=300)
mapa = d.entity(560, 300, "OPFI_INSTITUICAO_PROVEDOR", [
    ("OFIP_ID  (PK)", 1), ("INFI_ID  (FK → INSTITUICOES_FINANCEIRAS)", 1),
    ("OFPV_ID  (FK → OPFI_PROVEDORES)", 1), ("OFIP_ID_EXTERNO  VARCHAR(80)", 0),
    ("UK (INFI_ID, OFPV_ID)", 0), ("UK (OFPV_ID, OFIP_ID_EXTERNO)", 0), ("audit_*", 0),
], "#F1EAF7", "#5B3B86", wd=360)
uso = d.entity(1000, 70, "CONTAS / INVESTIMENTOS / OPFI_CONEXOES", [
    ("<PK própria>", 1), ("INFI_ID  (FK → INSTITUICOES_FINANCEIRAS)", 0),
    ("CONTAS: NOT NULL · demais: NULLABLE", 0),
    ("audit_data_exclusao  (exclusão lógica)", 0), ("… demais colunas", 0),
], "#FBF3E6", "#8A5A12", wd=250)
d.edge(infi, mapa, "1  →  N   (instituição)")
d.edge(prov, mapa, "1  →  N   (provedor)")
d.edge(infi, uso, "1  →  N   (INFI_ID)", dashed=True)
d.rect(560, 490, 360, 150,
       "OPFI_INSTITUICAO_PROVEDOR: mapa consultado no passo de importação de contas de Open Finance "
       "(id externo + provedor → INFI_ID). Documento 15 é consumidor.\n\n"
       "Travas de exclusão de INSTITUICOES_FINANCEIRAS (RN06 sistema / RN07 em uso) e a contagem "
       "de uso (C1/C6) atravessam as FKs INFI_ID. A exclusão lógica cascateia os vínculos (RN12).\n\n"
       "OPFI_PROVEDORES vem de carga inicial — só leitura nesta tela.",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-instituicao-financeira-der.drawio"), "w").write(xml2)

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
                   f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="260" height="50" as="geometry"/></mxCell>')
        return cid
    def link(s, a, b):
        s.b.append(f'<mxCell id="{s.i()}" style="endArrow=none;html=1;strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{a}" target="{b}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="Casos de Uso - Manter Instituicao Financeira"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1200" pageHeight="900" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1200, 900, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(330, 40, 540, 820, "Manter Instituição Financeira",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(120, 380, "ADMIN (PERF01)")
usr = u.actor(120, 620, "Usuário autenticado\n(PERF01 / PERF02)")
sis = u.actor(1010, 690, "Sistema\n(job de importação)")
ucs = [
    ("CAUS01", "Listar Instituições Financeiras"),
    ("CAUS02", "Filtrar Instituições Financeiras"),
    ("CAUS03", "Cadastrar Instituição Financeira"),
    ("CAUS04", "Editar Instituição Financeira"),
    ("CAUS05", "Excluir ou Desativar Instituição Financeira"),
    ("CAUS06", "Selecionar Instituição num Cadastro"),
    ("CAUS07", "Listar Vínculos Instituição × Provedor"),
    ("CAUS08", "Manter Vínculo Instituição × Provedor"),
    ("CAUS09", "Excluir Vínculo Instituição × Provedor"),
    ("CAUS10", "Resolver Instituição de Conta Importada"),
]
ids = [u.uc(455, 70 + idx * 76, c_, n) for idx, (c_, n) in enumerate(ucs)]
for cid in [ids[0], ids[1], ids[2], ids[3], ids[4], ids[6], ids[7], ids[8]]:
    u.link(adm, cid)
u.link(usr, ids[5])
u.link(sis, ids[9])
u.rect(330, 866, 540, 24,
       "CAUS06 exige só autenticação (EDP07). CAUS10 é contexto — regra no documento 16.",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-instituicao-financeira-casos-uso.drawio"), "w").write(xml3)

print("ok - 3 arquivos gerados (prototipo 5 telas, der, casos-uso)")
