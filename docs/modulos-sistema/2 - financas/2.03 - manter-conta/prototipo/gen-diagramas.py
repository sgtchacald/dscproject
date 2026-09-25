#!/usr/bin/env python3
# Diagramas draw.io do documento 2.03 - Manter Conta.
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

# ---- 7.1 Minhas Contas (Listagem) - QUADRO_DESCRITIVO_1 ----
p1 = P("7.1 Minhas Contas - Listagem")
shell(p1, "Minhas Contas", "Minhas Contas")
p1.txt(CX, 74, 560, 16, "Finanças  ›  Minhas Contas", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 460, 28, "Minhas Contas", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 760, 30,
       "Cadastre e gerencie as suas contas bancárias, carteiras e contas de investimento.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W - 300, 94, 110, "Filtrar")
p1.btn(W - 180, 94, 140, "Nova conta", True)

gy = 178
p1.rect(CX, gy, gw, 402, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
COLS = [
    ("DESCRIÇÃO",               CX + 12,  180),
    ("TIPO",                    CX + 200, 90),
    ("INSTITUIÇÃO",             CX + 296, 120),
    ("AGÊNCIA / NÚMERO",        CX + 422, 120),
    ("SALDO",                   CX + 548, 90),
    ("CONSIDERA NO SALDO GERAL",CX + 644, 90),
    ("Nº DE LANÇAMENTOS",       CX + 740, 70),
    ("SITUAÇÃO",                CX + 816, 80),
    ("AÇÃO",                    CX + 904, 66),
]
for lb, x, wd in COLS:
    p1.txt(x, gy + 8, wd, 24, lb, f"fontSize=9;fontColor={MUT};fontStyle=1;")

rows = [
    ("Carteira / Dinheiro em espécie", "Carteira",     "Nubank",           "—",              "-R$ 120,00",   "Não", "2", "Ativa"),
    ("Conta Antiga BB",                "Corrente",     "Banco do Brasil",  "Ag. 0455 / C/C 55555-1", "R$ 0,00", "Sim", "0", "Inativa"),
    ("Itaú Poupança",                  "Poupança",     "Itaú Unibanco",    "Ag. 1234 / Poup. 98765-0", "R$ 8.200,50", "Sim", "0", "Ativa"),
    ("Nubank Conta Corrente",          "Corrente",     "Nubank",           "Ag. 0001 / C/C 12345-6", "R$ 1.500,00", "Sim", "3", "Ativa"),
    ("XP Investimentos",               "Investimento", "XP Investimentos", "—",              "R$ 23.450,00", "Sim", "5", "Ativa"),
]
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw, ry)
    for (lb, x, wd), v in zip(COLS[:8], r):
        st = "fontSize=10;"
        if v in ("Inativa",):
            st += f"fontColor={MUT};"
        if v.startswith("-R$"):
            st += "fontColor=#C0392B;"
        p1.txt(x, ry + 18, wd, 24, v, st)
    p1.txt(COLS[8][1], ry + 18, COLS[8][2], 16, "✎   ⇅   🗑", f"fontSize=12;fontColor={MUT};")
p1.txt(CX + 12, gy + 380, 520, 16, "Mostrando 5 de 6", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw - 120, gy + 380, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 414, gw, 40,
       "Grid client-side (DataTables) sobre a lista completa carregada uma vez (EDP02 → C1). O modal Filtrar aplica em memória (RT01/RT02). "
       "Nova conta e ícones de ação = PERM_CONTAS_MANTER; a tela = PERM_CONTAS_LISTAR. O grid nunca traz conta de outro usuário (RN02). "
       "Ícones de ação: Editar (RT05), Ajustar saldo (RT08), Excluir (RT07) — ocultos quando a conta já está excluída. Saldo negativo em vermelho (RT11).",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(200, 70 + 1 * 34 - 2, 0)               # LINK -> item de menu "Minhas Contas"
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W - 308, 86, 4)
p1.mk(W - 46, 86, 5)
p1.mk(CX - 22, gy + 78, 6)
for j, (lb, x, wd) in enumerate(COLS):
    p1.mk(x + min(wd, 120) - 20, gy + 6, 7 + j)   # 7..15

# ---- 7.2 Modal Filtrar Contas - QUADRO_DESCRITIVO_2 ----
p2 = P("7.2 Modal Filtrar Contas")
p2.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 540, (W - 540) // 2, 160
p2.rect(mx, my, mw, 372, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Contas", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (descrição, agência ou número — sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "TIPO   (Todos / Corrente / Poupança / Investimento / Carteira)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "INSTITUIÇÃO   (Todas + opções do módulo 05 — SB05)")
p2.fld(mx + 24, my + 226, cw, "SITUAÇÃO   (default: Ativa)")
p2.btn(mx + mw - 230, my + 300, 100, "Limpar")
p2.btn(mx + mw - 120, my + 300, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx - 11, my + 230, 5)
p2.mk(mx + mw - 124, my + 292, 6)
p2.mk(mx + mw - 234, my + 292, 7)

# ---- 7.3 Modal Cadastro / Edicao de Conta - QUADRO_DESCRITIVO_3 ----
p3 = P("7.3 Modal Conta")
p3.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 600, (W - 600) // 2, 70
p3.rect(mx, my, mw, 720, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 400, 20, "Nova conta  /  Editar conta", "fontStyle=1;fontSize=14;")
cw3 = (mw - 64) // 2
p3.rect(mx + 24, my + 66, mw - 48, 40,
        "ⓘ  Modo edição: exibido quando a conta tem lançamentos — \"Esta conta é usada por N lançamento(s). Não pode ser excluída; você pode desativá-la.\"",
        f"fillColor=#EAF1FB;strokeColor={PRIM};fontColor={PRIM};fontSize=9;align=left;spacingLeft=8;")
p3.fld(mx + 24, my + 120, mw - 48, "DESCRIÇÃO *   (100; única entre as suas contas não excluídas — RN03)", edit=True)
p3.fld(mx + 24, my + 190, cw3, "TIPO DE CONTA *   (Corrente / Poupança / Investimento / Carteira — SB01)")
p3.fld(mx + 24 + cw3 + 16, my + 190, cw3, "INSTITUIÇÃO *   (SB02; DESABILITADA na edição — RN06)")
p3.fld(mx + 24, my + 260, cw3, "AGÊNCIA   (30)")
p3.fld(mx + 24 + cw3 + 16, my + 260, cw3, "NÚMERO   (30)")
p3.fld(mx + 24, my + 330, cw3, "MOEDA *   (SB03; default BRL)")
p3.fld(mx + 24 + cw3 + 16, my + 330, cw3, "SALDO INICIAL *   (monetário; só na criação — RN10)", edit=True)
p3.fld(mx + 24, my + 400, cw3, "NOME DO GERENTE   (100)")
p3.fld(mx + 24 + cw3 + 16, my + 400, cw3, "TELEFONE DO GERENTE   (20)")
p3.txt(mx + 24, my + 470, cw3, 16, "CONSIDERA NO SALDO GERAL   (default: Sim — RN09)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, my + 488, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.txt(mx + 24 + cw3 + 16, my + 470, cw3, 16, "ATIVA   (só na edição — RN08)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24 + cw3 + 16, my + 488, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.btn(mx + mw - 230, my + 632, 100, "Cancelar")
p3.btn(mx + mw - 120, my + 632, 96, "Salvar", True)
p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 122, 2)
p3.mk(mx - 11, my + 192, 3)
p3.mk(mx + 24 + cw3 + 4, my + 192, 4)
p3.mk(mx - 11, my + 262, 5)
p3.mk(mx + 24 + cw3 + 4, my + 262, 6)
p3.mk(mx - 11, my + 332, 7)
p3.mk(mx + 24 + cw3 + 4, my + 332, 8)
p3.mk(mx - 11, my + 402, 9)
p3.mk(mx + 24 + cw3 + 4, my + 402, 10)
p3.mk(mx - 11, my + 484, 11)
p3.mk(mx + 24 + cw3 + 4, my + 484, 12)
p3.mk(mx - 11, my + 66, 13)
p3.mk(mx + mw - 124, my + 624, 14)
p3.mk(mx + mw - 234, my + 624, 15)

# ---- 7.4 Modal Ajustar Saldo - QUADRO_DESCRITIVO_4 ----
p4 = P("7.4 Modal Ajustar Saldo")
p4.rect(0, 0, W, H, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W - 520) // 2, 180
p4.rect(mx, my, mw, 400, "", f"fillColor={SURF};strokeColor={BORD};")
p4.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p4.txt(mx + 20, my + 15, 400, 20, "Ajustar saldo — {descrição da conta}", "fontStyle=1;fontSize=14;")
p4.txt(mx + 24, my + 74, mw - 48, 16, "SALDO ATUAL   (somente leitura — C1.saldo, na moeda da conta)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p4.rect(mx + 24, my + 92, mw - 48, 32, "R$ 1.500,00", f"fillColor=#F5F6F8;strokeColor={BORD};fontStyle=2;dashed=1;")
p4.fld(mx + 24, my + 140, mw - 48, "NOVO SALDO *   (monetário; aceita valor negativo)", edit=True)
p4.txt(mx + 24, my + 210, mw - 48, 16, "OBSERVAÇÃO   (255; nota livre — vai no comentário da revisão do Envers)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p4.rect(mx + 24, my + 228, mw - 48, 60, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p4.btn(mx + mw - 250, my + 320, 110, "Cancelar")
p4.btn(mx + mw - 130, my + 320, 106, "Salvar ajuste", True)
p4.mk(mx - 11, my + 4, 1)
p4.mk(mx - 11, my + 96, 2)
p4.mk(mx - 11, my + 144, 3)
p4.mk(mx - 11, my + 232, 4)
p4.mk(mx + mw - 134, my + 312, 5)
p4.mk(mx + mw - 254, my + 312, 6)

xml1 = wrap([p1, p2, p3, p4])
M.parseString(xml1)
open(os.path.join(OUT, "manter-conta-prototipo.drawio"), "w", encoding="utf-8").write(xml1)

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
        return ('<diagram name="DER - Manter Conta"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1360" pageHeight="880" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1360, 880, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 18, 1200, 20,
       "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_5 (CONTAS), _4 (INSTITUICOES_FINANCEIRAS) e _2 (USUARIOS). "
       "Este documento não cria tabela nova nem faz ALTER TABLE.",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")
infi = d.entity(60, 80, "INSTITUICOES_FINANCEIRAS", [
    ("INFI_ID  (PK)", 1), ("INFI_NOME  (UK)", 1),
    ("INFI_CODIGO  (UK quando informado, NULL)", 0),
    ("INFI_TIPO_INSTITUICAO  CHAR(1) {B|C}", 0),
    ("INFI_FL_ATIVO  (BOOLEAN)", 0),
    ("catálogo do documento 2.02 — só leitura aqui", 0),
    ("audit_*", 0),
], "#EAF7EF", "#1F6B45", wd=360)
usu = d.entity(60, 470, "USUARIOS", [
    ("USU_ID  (PK)", 1), ("USU_NOME", 0),
    ("USU_LOGIN  (UK)  ·  USU_EMAIL  (UK)", 0),
    ("PERF_ID  (FK → PERFIS)", 0),
    ("dono da conta — CONTAS.USU_ID", 0),
    ("audit_*", 0),
], "#F1EAF7", "#5B3B86", wd=360)
cta = d.entity(520, 150, "CONTAS", [
    ("CTA_ID  (PK)", 1),
    ("CTA_DESCRICAO  VARCHAR(100)  — única por usuário (RN03)", 0),
    ("CTA_TIPO  VARCHAR(20)  {CORRENTE|POUPANCA|INVESTIMENTO|CARTEIRA}", 0),
    ("CTA_AGENCIA  VARCHAR(30)  ·  CTA_NUMERO  VARCHAR(30)", 0),
    ("CTA_MOEDA  CHAR(3)  DEFAULT 'BRL'", 0),
    ("CTA_SALDO  DECIMAL(15,2)  DEFAULT 0.00", 0),
    ("CTA_SALDO_SINCRONIZADO_EM  DATETIME(6)  NULL", 0),
    ("CTA_NOME_GERENTE  ·  CTA_TEL_GERENTE", 0),
    ("CTA_FL_ATIVO  BOOLEAN  DEFAULT TRUE", 0),
    ("CTA_FL_CONSIDERA_SALDO  BOOLEAN  DEFAULT TRUE", 0),
    ("INFI_ID  (FK → INSTITUICOES_FINANCEIRAS)  NOT NULL", 1),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 1),
    ("audit_*  — @Audited (Hibernate Envers)", 0),
], "#EAF2FB", "#1F3864", wd=430)
trba = d.entity(1010, 90, "TRANSACOES_BANCARIAS", [
    ("TRBA_ID  (PK)", 1),
    ("CTA_ID  (FK → CONTAS)  NOT NULL", 1),
    ("conta com transação nunca é excluível (RN07)", 0),
    ("audit_data_exclusao", 0),
], "#FBF3E6", "#8A5A12", wd=310)
uso = d.entity(1010, 440, "RECEITAS / DESPESAS / INVESTIMENTOS / CARTOES_CREDITO", [
    ("<PK própria>", 1),
    ("CTA_ID  (FK → CONTAS)  NULLABLE", 0),
    ("CARTOES_CREDITO: conta de débito da fatura", 0),
    ("contagem de uso — C4 (soma das cinco)", 0),
    ("audit_data_exclusao", 0),
], "#FBF3E6", "#8A5A12", wd=310)
d.edge(infi, cta, "1  →  N   INFI_ID (NOT NULL)")
d.edge(usu, cta, "1  →  N   USU_ID (NOT NULL · RN02)")
d.edge(cta, trba, "1  →  N   CTA_ID (NOT NULL)")
d.edge(cta, uso, "1  →  N   CTA_ID (NULLABLE)", dashed=True)
d.rect(520, 560, 430, 170,
       "CONTAS: CRUD + ajuste de saldo + desativação, sempre no escopo do USU_ID autenticado (RN02). "
       "INFI_ID é imutável após a criação (RN06).\n\n"
       "A trava de exclusão (RN07 / C4) atravessa as cinco FKs CTA_ID. TRANSACOES_BANCARIAS.CTA_ID é NOT NULL "
       "e sempre bloqueia; as demais são NULLABLE e o bloqueio depende do parâmetro CONTA_EXCLUSAO_BLOQUEIA_EM_USO.\n\n"
       "INSTITUICOES_FINANCEIRAS vem do documento 2.02 — só leitura nesta tela.",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-conta-der.drawio"), "w", encoding="utf-8").write(xml2)

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
        return ('<diagram name="Casos de Uso - Manter Conta"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1200" pageHeight="820" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1200, 820, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(360, 40, 540, 720, "Manter Conta — tela \"Minhas Contas\"",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(120, 250, "ADMIN (PERF01)")
usr = u.actor(120, 470, "USER (PERF02)")
ucs = [
    ("CAUS01", "Listar Minhas Contas"),
    ("CAUS02", "Filtrar Contas"),
    ("CAUS03", "Cadastrar Conta"),
    ("CAUS04", "Editar Conta"),
    ("CAUS05", "Ajustar Saldo da Conta"),
    ("CAUS06", "Excluir ou Desativar Conta"),
    ("CAUS07", "Selecionar Conta num Lançamento"),
]
ids = [u.uc(475, 70 + idx * 90, c_, n) for idx, (c_, n) in enumerate(ucs)]
for cid in ids:
    u.link(adm, cid)
    u.link(usr, cid)
u.rect(360, 766, 540, 40,
       "ADMIN e USER têm o mesmo acesso; cada um opera só sobre as próprias contas (RN02). "
       "CAUS07 usa EDP07 — a conta é escolhida nas telas de lançamento (documentos 2.05 a 2.08).",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-conta-casos-uso.drawio"), "w", encoding="utf-8").write(xml3)

print("ok - 3 arquivos gerados (prototipo 4 telas, der, casos-uso)")
