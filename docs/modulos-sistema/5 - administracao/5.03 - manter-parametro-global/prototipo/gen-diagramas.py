#!/usr/bin/env python3
# Diagramas draw.io do documento 5.03 - Manter Parametro Global.
# Gera 3 arquivos: wireframes das telas, DER do subconjunto e casos de uso.
# Marcadores numericos emitidos POR ULTIMO em cada pagina (ficam na frente no PNG).
import html, os, xml.dom.minidom as M

OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1280, 860
def e(s): return html.escape(str(s), quote=True)

INK="#1B2430"; MUT="#69747F"; BORD="#D8DEE6"; SURF="#FFFFFF"; HDRF="#F1F3F6"
PRIM="#1F6FD0"; MARK="#D1367F"; EDIT="#FFF8E1"; EDGE="#E0A800"; WARN="#B26B00"; OKC="#1F9D57"

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
    def area(s, x, y, w, h, lb):
        s.txt(x, y, w, 16, lb, f"fontSize=11;fontColor={MUT};fontStyle=1;")
        s.rect(x, y + 18, w, h, "", "fillColor=#FBFCFD;")
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


def shell(p, titulo):
    p.rect(0, 0, W, H, "", "fillColor=#F5F6F8;strokeColor=none;")
    p.rect(0, 0, 224, H, "", f"fillColor={SURF};strokeColor={BORD};")
    p.txt(20, 22, 180, 20, "dscproject", "fontStyle=1;fontSize=14;")
    nav = [("Início", 0), ("Contas", 0), ("Despesas", 0), ("Usuários", 0),
           ("Perfis e Permissões", 0), ("Parâmetros Globais", 1)]
    for idx, (t, a) in enumerate(nav):
        y = 70 + idx * 36
        if a:
            p.rect(12, y - 4, 200, 30, "", "fillColor=#E8F0FB;strokeColor=none;")
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

# ---- 7.1 Listagem (grid com edicao inline) ----
p1 = P("7.1 Parametros Globais - Listagem")
shell(p1, "Parâmetros Globais")
CX = 258
p1.txt(CX, 74, 500, 16, "Administração  ›  Parâmetros Globais", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 400, 28, "Parâmetros Globais", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 640, 30,
       "Ajuste os valores de configuração que o sistema lê em tempo de execução. "
       "Os parâmetros são definidos no código; aqui você edita apenas o valor.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W - 150, 94, 110, "Filtrar")

gy = 178
gw = W - CX - 40            # 984
p1.rect(CX, gy, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
# colunas: (rotulo, x, largura util)
COLS = [
    ("MÓDULO",           CX + 12,  104),
    ("CÓDIGO",           CX + 122, 232),
    ("NOME",             CX + 366, 150),
    ("TIPO",             CX + 528, 78),
    ("VALOR",            CX + 614, 78),
    ("SITUAÇÃO",         CX + 700, 78),
    ("ÚLTIMA ALT.",      CX + 790, 118),
    ("AÇÕES",            CX + 916, 60),
]
for lb, x, wd in COLS:
    p1.txt(x, gy + 11, wd, 16, lb, f"fontSize=10;fontColor={MUT};fontStyle=1;")

rows = [
    ("Parâm. Globais", "PARAMETROS_VALOR_CACHE",             "Cachear valor por código",   "BOOLEAN", "Sim",    "Ativo", "D. Cordeiro\n10/09/2026"),
    ("Categoria",      "CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO", "Bloqueia excluir em uso",    "BOOLEAN", "Sim",    "Ativo", "D. Cordeiro\n18/09/2026"),
    ("Conta",          "CONTA_MOEDA_PADRAO",                 "Moeda padrão de contas",     "STRING",  "BRL",    "Ativo", "M. Souza\n22/08/2026"),
    ("Inst. Financ.",  "INSTITUICAO_COMBOBOX_CACHE",         "Cache do combobox (min)",    "DECIMAL", "15,5",   "Ativo", "R. Antunes\n01/09/2026"),
    ("—",              "RECURSO_ANTIGO",                     "(sem catálogo)",             "STRING",  "legado", "Órfão", "—"),
]
rh = 62
valor_x = COLS[4][1]; valor_w = COLS[4][2]
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw, ry)
    # celula editavel VALOR (fundo ambar tracejado); Tipo e somente-leitura
    p1.rect(valor_x - 8, ry + 10, valor_w + 4, 40, "", f"fillColor={EDIT};strokeColor={EDGE};dashed=1;")
    for (lb, x, wd), v in zip(COLS[:7], r):
        st = "fontSize=11;" + (f"fontColor={WARN};" if v == "Órfão" else "")
        if "\n" in v:
            st += "whiteSpace=wrap;fontSize=10;fontColor=" + MUT + ";"
        p1.txt(x, ry + 20, wd, 24, v, st)
    acao = "↺   ⟳" if r[6] != "—" else "⟳"
    p1.txt(COLS[7][1], ry + 20, COLS[7][2], 16, acao, f"fontSize=13;fontColor={MUT};")
p1.txt(CX + 12, gy + 380, 520, 16, "Mostrando 1–5 de 5", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw - 120, gy + 380, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 414, gw, 16,
       "Grid client-side (DataTables). Clique na célula Valor (em âmbar) para editar — "
       "o editor se adapta ao tipo da linha (RT04, RT06). Tipo é somente-leitura.",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W - 158, 86, 4)
p1.mk(CX - 22, gy + 78, 5)
for j, (lb, x, wd) in enumerate(COLS):
    p1.mk(x + wd - 20, gy + 7, 6 + j)     # 6..13, encostado na borda direita da coluna

# ---- 7.2 Modal Filtrar ----
p2 = P("7.2 Modal Filtrar")
p2.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 150
p2.rect(mx, my, mw, 404, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Parâmetros", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (código ou nome, sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "MÓDULO   (Todos)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "TIPO   (Todos)")
p2.fld(mx + 24, my + 226, cw, "SITUAÇÃO   (Todas)")
p2.btn(mx + mw - 230, my + 320, 100, "Limpar")
p2.btn(mx + mw - 120, my + 320, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx - 11, my + 230, 5)
p2.mk(mx + mw - 124, my + 312, 6)
p2.mk(mx + mw - 234, my + 312, 7)

# ---- 7.3 Modal Confirmar Alteracao ----
p3 = P("7.3 Modal Confirmar Alteracao")
p3.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 110
p3.rect(mx, my, mw, 520, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 320, 20, "Confirmar alteração", "fontStyle=1;fontSize=14;")
p3.txt(mx + 24, my + 70, mw - 48, 16, "PARÂMETRO", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.txt(mx + 24, my + 90, mw - 48, 16, "CONTA_MOEDA_PADRAO  —  Moeda padrão de novas contas", "fontSize=11;")
p3.txt(mx + 24, my + 128, mw - 48, 16, "VALOR", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, my + 146, mw - 48, 32, "BRL   →   USD",
        f"fillColor=#FBFCFD;fontSize=11;align=left;spacingLeft=8;fontColor={OKC};fontStyle=1;")
p3.rect(mx + 24, my + 194, mw - 48, 44,
        "⚠  Este parâmetro não existe mais no catálogo do sistema (aviso exibido só p/ órfão).",
        f"fillColor=#FBF1D9;strokeColor={WARN};fontColor={WARN};fontSize=10;align=left;spacingLeft=8;")
p3.area(mx + 24, my + 256, mw - 48, 76, "MOTIVO *   (obrigatório — vai para o histórico de revisões, RN03)")
p3.btn(mx + mw - 230, my + 390, 100, "Cancelar")
p3.btn(mx + mw - 120, my + 390, 96, "Salvar", True)
p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 80, 2)
p3.mk(mx - 11, my + 136, 3)
p3.mk(mx - 11, my + 202, 4)
p3.mk(mx - 11, my + 262, 5)
p3.mk(mx + mw - 124, my + 382, 6)
p3.mk(mx + mw - 234, my + 382, 7)

# ---- 7.4 Historico de Revisoes ----
p4 = P("7.4 Historico de Revisoes")
p4.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 780, (W - 780) // 2, 90
p4.rect(mx, my, mw, 560, "", f"fillColor={SURF};strokeColor={BORD};")
p4.rect(mx, my, mw, 66, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p4.txt(mx + 20, my + 13, 560, 20, "Histórico — CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO", "fontStyle=1;fontSize=14;")
p4.txt(mx + 20, my + 37, 560, 16, "Bloqueia excluir categoria em uso  ·  Módulo: Categoria",
       f"fontSize=11;fontColor={MUT};")
hy = my + 84
p4.rect(mx + 24, hy, mw - 48, 36, "", f"fillColor={HDRF};strokeColor={BORD};")
HC = [("DATA/HORA", mx + 40, 150), ("AUTOR", mx + 200, 130), ("TIPO", mx + 336, 90),
      ("VALOR", mx + 430, 90), ("MOTIVO", mx + 540, 200)]
for lb, x, wd in HC:
    p4.txt(x, hy + 10, wd, 16, lb, f"fontSize=10;fontColor={MUT};fontStyle=1;")
hist = [
    ("18/09/2026 14:22", "Diego Cordeiro", "BOOLEAN", "true", "Reforço de trava por auditoria"),
    ("03/07/2026 09:10", "Marina Souza", "BOOLEAN", "false", "Liberação temporária p/ limpeza"),
    ("08/09/2026 00:00", "sistema (carga)", "BOOLEAN", "true", "—"),
]
for idx, r in enumerate(hist):
    ry = hy + 36 + idx * 44
    if idx:
        p4.line(mx + 24, ry, mx + mw - 24, ry)
    for (lb, x, wd), v in zip(HC, r):
        p4.txt(x, ry + 12, wd, 16, v, "fontSize=11;")
p4.txt(mx + 24, hy + 180, mw - 48, 16,
       "Somente leitura. Revisões via Hibernate Envers, da mais recente para a mais antiga (EDP05).",
       f"fontSize=10;fontColor={MUT};")
p4.btn(mx + mw - 110, my + 500, 86, "Fechar")
p4.mk(mx - 11, my + 4, 1)
p4.mk(mx - 11, my + 38, 2)
p4.mk(mx - 11, hy + 172, 3)
for j, (lb, x, wd) in enumerate(HC):
    p4.mk(x + wd - 20, hy + 8, 4 + j)     # 4..8
p4.mk(mx + mw - 116, my + 492, 9)

xml1 = wrap([p1, p2, p3, p4])
M.parseString(xml1)
open(os.path.join(OUT, "manter-parametro-global-prototipo.drawio"), "w", encoding="utf-8").write(xml1)

# =========================================================================
# ARQUIVO 2 - DER (subconjunto do Documento 0)
# =========================================================================
class Der:
    def __init__(s): s.b = []; s.k = 0
    def i(s): s.k += 1; return f"d{s.k}"
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def entity(s, x, y, title, fields, fill, stroke):
        wd, th, rh = 340, 28, 22
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
    def edge(s, src, dst, lbl):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(lbl)}" style="endArrow=ERmany;startArrow=ERone;html=1;'
                   f'fontFamily=Helvetica;fontSize=10;fontColor={MUT};strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{src}" target="{dst}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="DER - Manter Parametro Global"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1120" pageHeight="720" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1120, 720, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 20, 900, 20, "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_28 (tabela-raiz, sem FK).",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")
pg = d.entity(60, 70, "PARAMETROS_GLOBAIS", [
    ("PAGL_ID  (PK)", 1), ("PAGL_CODIGO  (UK)", 1), ("PAGL_NOME", 0), ("PAGL_DESCRICAO", 0),
    ("PAGL_MODULO", 0), ("PAGL_TIPO_DADO  {STRING|INTEGER|DECIMAL|BOOLEAN}", 0),
    ("PAGL_VALOR  (TEXT)", 0), ("PAGL_VALOR_DEFAULT  (TEXT)", 0), ("PAGL_MOTIVO  (255, NULL)", 0),
    ("PAGL_FL_ORFA  (BOOLEAN, def. FALSE)", 0), ("audit_*  (criacao/alteracao/exclusao)", 0),
], "#EAF2FB", "#1F3864")
ag = d.entity(620, 70, "PARAMETROS_GLOBAIS_aud", [
    ("PAGL_ID  (PK)", 1), ("REV  (PK, FK REVINFO)", 1), ("REVTYPE  (0 add / 1 mod / 2 del)", 0),
    ("PAGL_TIPO_DADO", 0), ("PAGL_VALOR", 0), ("PAGL_MOTIVO", 0),
    ("PAGL_FL_ORFA", 0), ("audit_alterado_por / audit_criado_por", 0),
], "#F1EAF7", "#5B3B86")
rv = d.entity(620, 420, "REVINFO", [
    ("REV  (PK)", 1), ("REVTSTMP  (bigint)", 0), ("(autor via AbstractAuditoria)", 0),
], "#EAF7EF", "#1F6B45")
d.edge(pg, ag, "1  →  N   (auditada por)")
d.edge(rv, ag, "1  →  N   (revisão)")
d.rect(60, 430, 430, 120,
       "PARAMETROS_GLOBAIS é tabela-raiz: nenhuma FK de saída.\n"
       "Auditoria completa via Hibernate Envers (@Audited) — Documento 0, RNF02.\n"
       "Carga inicial pelo sincronizador do catálogo do código, não pelo V1__init.sql.",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-parametro-global-der.drawio"), "w", encoding="utf-8").write(xml2)

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
                   f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="250" height="54" as="geometry"/></mxCell>')
        return cid
    def link(s, a, b):
        s.b.append(f'<mxCell id="{s.i()}" style="endArrow=none;html=1;strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{a}" target="{b}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="Casos de Uso - Manter Parametro Global"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1120" pageHeight="780" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1120, 780, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(320, 40, 500, 680, "Manter Parâmetro Global",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(110, 320, "ADMIN (PERF01)")
sis = u.actor(958, 150, "Sistema (inicialização)")
mod = u.actor(958, 470, "Demais módulos")
ucs = [
    ("CAUS01", "Listar Parâmetros Globais"),
    ("CAUS02", "Filtrar Parâmetros"),
    ("CAUS03", "Editar Parâmetro (inline)"),
    ("CAUS04", "Restaurar Parâmetro ao Padrão"),
    ("CAUS05", "Ver Histórico de Revisões"),
    ("CAUS06", "Sincronizar o Catálogo"),
    ("CAUS07", "Consultar Valor por Código"),
]
ids = [u.uc(440, 86 + idx * 90, c_, n) for idx, (c_, n) in enumerate(ucs)]
for cid in ids[:5]:
    u.link(adm, cid)
u.link(sis, ids[5])
u.link(mod, ids[6])
u.rect(320, 726, 500, 24,
       "CAUS03 = edição inline no grid + modal de confirmação do motivo. CAUS06/CAUS07 são contexto.",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-parametro-global-casos-uso.drawio"), "w", encoding="utf-8").write(xml3)

print("ok - 3 arquivos gerados (prototipo 4 telas, der, casos-uso)")
