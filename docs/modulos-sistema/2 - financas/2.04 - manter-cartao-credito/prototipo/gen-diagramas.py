#!/usr/bin/env python3
# Diagramas draw.io do documento 2.04 - Manter Cartao de Credito.
# Gera 3 arquivos: wireframes das telas, DER do subconjunto e casos de uso.
# XML numa linha so; marcadores numericos emitidos POR ULTIMO em cada pagina
# (ficam na frente no PNG), em parent="1".
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
    def btn(s, x, y, w, v, pri=False, danger=False):
        f = PRIM if pri else ("#C0392B" if danger else SURF)
        fc = "#FFFFFF" if (pri or danger) else INK
        stroke = PRIM if pri else ("#C0392B" if danger else BORD)
        style = (f"rounded=1;arcSize=30;whiteSpace=wrap;html=1;fillColor={f};"
                 f"strokeColor={stroke};fontColor={fc};fontFamily=Helvetica;fontSize=12;fontStyle=1;")
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


NAV = ["Início", "Minhas Contas", "Cartões", "Categorias",
       "Instituições Financeiras"]

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

# ---- 7.1 Meus Cartões (Listagem) - QUADRO_DESCRITIVO_1 ----
p1 = P("7.1 Meus Cartoes - Listagem")
shell(p1, "Meus Cartões", "Cartões")
p1.txt(CX, 74, 560, 16, "Finanças  ›  Cartões", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 460, 28, "Meus Cartões", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 760, 30,
       "Cadastre os seus cartões de crédito e defina os dias de fechamento e vencimento da fatura.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W - 300, 94, 110, "Filtrar")
p1.btn(W - 180, 94, 140, "Novo cartão", True)

gy = 178
p1.rect(CX, gy, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
COLS = [
    ("DESCRIÇÃO",              CX + 12,  160),
    ("BANDEIRA",               CX + 176, 96),
    ("FINAL",                  CX + 276, 66),
    ("LIMITE",                 CX + 346, 96),
    ("FECHAMENTO / VENCIMENTO",CX + 446, 160),
    ("CONTA DE DÉBITO",        CX + 610, 150),
    ("SITUAÇÃO",               CX + 764, 90),
    ("AÇÃO",                   CX + 858, 66),
]
for lb, x, wd in COLS:
    p1.txt(x, gy + 8, wd, 24, lb, f"fontSize=9;fontColor={MUT};fontStyle=1;")

rows = [
    ("Itaú Click",          "Visa",       "•••• 5678", "R$ 8.000,00",  "Dia 10 / Dia 17", "Não definida",           "Ativo"),
    ("Nubank Ultravioleta", "Mastercard", "•••• 1234", "R$ 15.000,00", "Dia 5 / Dia 12",   "Nubank Conta Corrente", "Ativo"),
    ("XP Visa Infinite",    "Visa",       "•••• 9999", "—",            "—",                "XP Investimentos",      "Ativo"),
    ("C6 Carbon",           "Mastercard", "•••• 4321", "R$ 5.000,00",  "Dia 1 / Dia 8",    "Não definida",          "Inativo"),
]
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw, ry)
    for (lb, x, wd), v in zip(COLS[:7], r):
        st = "fontSize=10;"
        if v == "Inativo":
            st += f"fontColor={MUT};"
        p1.txt(x, ry + 18, wd, 24, v, st)
    p1.txt(COLS[7][1], ry + 18, COLS[7][2], 16, "✎   🗑", f"fontSize=12;fontColor={MUT};")
p1.txt(CX + 12, gy + 380, 520, 16, "Mostrando 4 de 5", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw - 120, gy + 380, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 414, gw, 40,
       "Grid client-side sobre a lista completa dos cartões do usuário, carregada uma vez (EDP02 → C1). O modal Filtrar aplica em memória (RT02). "
       "Novo cartão e ícones de ação = PERM_CARTOES_MANTER; a tela = PERM_CARTOES_LISTAR. O grid nunca traz cartão de outro usuário (RN02). "
       "Ícones de ação: Editar (RT05), Excluir (RT07) — ocultos quando o cartão já está excluído.",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(200, 70 + 2 * 34 - 2, 0)                # LINK -> item de menu "Cartões"
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W - 308, 86, 4)
p1.mk(W - 46, 86, 5)
p1.mk(CX - 22, gy + 78, 6)
for j, (lb, x, wd) in enumerate(COLS):
    p1.mk(x + min(wd, 120) - 20, gy + 6, 7 + j)   # 7..14

# ---- 7.2 Modal Filtrar Cartões - QUADRO_DESCRITIVO_2 ----
p2 = P("7.2 Modal Filtrar Cartoes")
p2.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 540, (W - 540) // 2, 190
p2.rect(mx, my, mw, 340, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Cartões", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (descrição ou final do cartão — sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "BANDEIRA   (Todas / Visa / Mastercard / Elo / Amex)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "SITUAÇÃO   (default: Ativo)")
p2.btn(mx + mw - 230, my + 268, 100, "Limpar")
p2.btn(mx + mw - 120, my + 268, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx + mw - 124, my + 260, 5)
p2.mk(mx + mw - 234, my + 260, 6)

# ---- 7.3 Modal Cadastro / Edicao de Cartão - QUADRO_DESCRITIVO_3 ----
p3 = P("7.3 Modal Cartao")
p3.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 600, (W - 600) // 2, 40
p3.rect(mx, my, mw, 780, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 400, 20, "Novo cartão  /  Editar cartão", "fontStyle=1;fontSize=14;")
cw3 = (mw - 64) // 2
p3.rect(mx + 24, my + 66, mw - 48, 44,
        "ⓘ  Modo edição: exibido quando o cartão tem vínculos — \"Este cartão possui N fatura(s)/despesa(s) vinculada(s). Ele não pode ser excluído; você pode desativá-lo.\"",
        f"fillColor=#EAF1FB;strokeColor={PRIM};fontColor={PRIM};fontSize=9;align=left;spacingLeft=8;")
p3.fld(mx + 24, my + 128, mw - 48, "DESCRIÇÃO *   (100 — RN03)", edit=True)
p3.fld(mx + 24, my + 198, cw3, "BANDEIRA   (Visa / Mastercard / Elo / Amex — SB01, RN06)")
p3.fld(mx + 24 + cw3 + 16, my + 198, cw3, "FINAL DO CARTÃO   (4 dígitos — RN05)")
p3.fld(mx + 24, my + 268, cw3, "LIMITE (R$)   (informativo — Observação 11)")
p3.fld(mx + 24, my + 338, cw3, "DIA DE FECHAMENTO   (1 a 31 — RN04)")
p3.fld(mx + 24 + cw3 + 16, my + 338, cw3, "DIA DE VENCIMENTO   (1 a 31 — RN04)")
p3.fld(mx + 24, my + 408, mw - 48, "CONTA DE DÉBITO   (opcional; contas ativas do usuário — SB02, RN07)")
p3.txt(mx + 24, my + 478, cw3, 16, "ATIVO   (só na edição — RN09)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, my + 496, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.btn(mx + mw - 230, my + 700, 100, "Cancelar", danger=True)
p3.btn(mx + mw - 120, my + 700, 96, "Salvar", True)
p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 130, 2)
p3.mk(mx - 11, my + 200, 3)
p3.mk(mx + 24 + cw3 + 4, my + 200, 4)
p3.mk(mx - 11, my + 270, 5)
p3.mk(mx - 11, my + 340, 6)
p3.mk(mx + 24 + cw3 + 4, my + 340, 7)
p3.mk(mx - 11, my + 410, 8)
p3.mk(mx - 11, my + 492, 9)
p3.mk(mx - 11, my + 66, 10)
p3.mk(mx + mw - 124, my + 692, 11)
p3.mk(mx + mw - 234, my + 692, 12)

xml1 = wrap([p1, p2, p3])
M.parseString(xml1)
open(os.path.join(OUT, "manter-cartao-credito-prototipo.drawio"), "w", encoding="utf-8").write(xml1)

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
        return ('<diagram name="DER - Manter Cartao de Credito"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1360" pageHeight="880" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1360, 880, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 18, 1200, 20,
       "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_6 (CARTOES_CREDITO), _5 (CONTAS) e _2 (USUARIOS), "
       "mais as FKs CACR_ID de FATURAS_CARTAO e DESPESAS. Este documento não cria tabela nova nem faz ALTER TABLE.",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")
usu = d.entity(60, 90, "USUARIOS", [
    ("USU_ID  (PK)", 1), ("USU_NOME", 0),
    ("USU_LOGIN  (UK)  ·  USU_EMAIL  (UK)", 0),
    ("PERF_ID  (FK → PERFIS)", 0),
    ("dono do cartão — CARTOES_CREDITO.USU_ID", 0),
    ("audit_*", 0),
], "#F1EAF7", "#5B3B86", wd=370)
cta = d.entity(60, 400, "CONTAS", [
    ("CTA_ID  (PK)", 1),
    ("CTA_DESCRICAO", 0),
    ("CTA_FL_ATIVO  BOOLEAN", 0),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 0),
    ("documento 2.03 — só leitura aqui (SB02, RN07)", 0),
    ("audit_*", 0),
], "#EAF2FB", "#1F3864", wd=370)
cacr = d.entity(520, 150, "CARTOES_CREDITO", [
    ("CACR_ID  (PK)", 1),
    ("CACR_DESCRICAO  VARCHAR(100)  NOT NULL", 0),
    ("CACR_BANDEIRA  VARCHAR(30)  NULL", 0),
    ("CACR_FINAL_CARTAO  CHAR(4)  NULL", 0),
    ("CACR_LIMITE  DECIMAL(15,2)  NULL", 0),
    ("CACR_DIA_FECHAMENTO  TINYINT  NULL  CHECK 1..31", 0),
    ("CACR_DIA_VENCIMENTO  TINYINT  NULL  CHECK 1..31", 0),
    ("CACR_FL_ATIVO  BOOLEAN  DEFAULT TRUE", 0),
    ("CTA_ID  (FK → CONTAS)  NULLABLE", 1),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 1),
    ("audit_*  — @Audited (Hibernate Envers)", 0),
], "#EAF7EF", "#1F6B45", wd=430)
fat = d.entity(1030, 70, "FATURAS_CARTAO", [
    ("FACA_ID  (PK)", 1),
    ("CACR_ID  (FK → CARTOES_CREDITO)  NOT NULL", 1),
    ("cartão com fatura nunca é excluível (RN08)", 0),
    ("documento 2.08 — fora do escopo aqui", 0),
    ("audit_data_exclusao", 0),
], "#FBF3E6", "#8A5A12", wd=310)
des = d.entity(1030, 420, "DESPESAS", [
    ("DESP_ID  (PK)", 1),
    ("CACR_ID  (FK → CARTOES_CREDITO)  NULLABLE", 0),
    ("bloqueio parametrizável (RN08 · CARTAO_EXCLUSAO_...)", 0),
    ("documento 2.06 — fora do escopo aqui", 0),
    ("audit_data_exclusao", 0),
], "#FBF3E6", "#8A5A12", wd=310)
d.edge(usu, cacr, "1  →  N   USU_ID (NOT NULL · RN02)")
d.edge(cta, cacr, "1  →  N   CTA_ID (NULLABLE · RN07)", dashed=True)
d.edge(cacr, fat, "1  →  N   CACR_ID (NOT NULL)")
d.edge(cacr, des, "1  →  N   CACR_ID (NULLABLE)", dashed=True)
d.rect(520, 600, 430, 210,
       "CARTOES_CREDITO: CRUD + desativação, sempre no escopo do USU_ID autenticado (RN02). "
       "CTA_ID (conta de débito) é opcional e, quando informado, precisa ser de uma conta ativa do mesmo usuário (RN07, via C5).\n\n"
       "A trava de exclusão (RN08 / C4) atravessa as duas FKs CACR_ID. FATURAS_CARTAO.CACR_ID é NOT NULL e sempre bloqueia; "
       "DESPESAS.CACR_ID é NULLABLE e o bloqueio depende do parâmetro CARTAO_EXCLUSAO_BLOQUEIA_EM_USO.\n\n"
       "CONTAS vem do documento 2.03 — só leitura nesta tela.",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-cartao-credito-der.drawio"), "w", encoding="utf-8").write(xml2)

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
        return ('<diagram name="Casos de Uso - Manter Cartao de Credito"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1200" pageHeight="720" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1200, 720, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(360, 40, 540, 620, "Manter Cartão de Crédito — tela \"Meus Cartões\"",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(120, 210, "ADMIN (PERF01)")
usr = u.actor(120, 420, "USER (PERF02)")
ucs = [
    ("CAUS01", "Listar Meus Cartões"),
    ("CAUS02", "Filtrar Cartões"),
    ("CAUS03", "Cadastrar Cartão"),
    ("CAUS04", "Editar Cartão"),
    ("CAUS05", "Excluir ou Desativar Cartão"),
    ("CAUS06", "Selecionar Cartão numa Despesa"),
]
ids = [u.uc(475, 70 + idx * 90, c_, n) for idx, (c_, n) in enumerate(ucs)]
for cid in ids:
    u.link(adm, cid)
    u.link(usr, cid)
u.rect(360, 666, 540, 40,
       "ADMIN e USER têm o mesmo acesso; cada um opera só sobre os próprios cartões (RN02). "
       "CAUS06 usa EDP07 — o cartão é escolhido na tela de Despesa (documento 2.06).",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-cartao-credito-casos-uso.drawio"), "w", encoding="utf-8").write(xml3)

print("ok - 3 arquivos gerados (prototipo 3 telas, der, casos-uso)")
