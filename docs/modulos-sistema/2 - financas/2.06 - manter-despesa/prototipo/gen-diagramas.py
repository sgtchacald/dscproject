#!/usr/bin/env python3
# Diagramas draw.io do documento 2.06 - Manter Despesa.
# Gera 3 arquivos: wireframes das telas, DER do subconjunto e casos de uso.
# Marcadores numericos emitidos POR ULTIMO em cada pagina (ficam na frente no PNG),
# em parent="1". Segue o padrao dos modulos 04/06 (gen-diagramas.py).
import html, os, xml.dom.minidom as M

OUT = os.path.dirname(os.path.abspath(__file__))

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
    def fld(s, x, y, w, lb, edit=False, h=32):
        s.txt(x, y, w, 16, lb, f"fontSize=11;fontColor={MUT};fontStyle=1;")
        s.rect(x, y + 18, w, h, "",
               f"fillColor={EDIT if edit else '#FBFCFD'};strokeColor={EDGE if edit else BORD};")
    def sectitle(s, x, y, w, v):
        s.rect(x, y, w, 1, "", f"fillColor={BORD};strokeColor=none;")
        s.txt(x, y + 8, w, 20, v.upper(), f"fontStyle=1;fontSize=11;fontColor={MUT};")
    def mk(s, x, y, num):
        st = (f"ellipse;whiteSpace=wrap;html=1;fillColor={MARK};strokeColor=#FFFFFF;strokeWidth=2;"
              f"fontColor=#FFFFFF;fontFamily=Helvetica;fontSize=11;fontStyle=1;")
        s.m.append(f'<mxCell id="mk{s.n}_{num}" value="{num}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="22" height="22" as="geometry"/></mxCell>')
    def page(s, pw, ph):
        cells = ('<mxCell id="0"/><mxCell id="1" parent="0"/>'
                 + "".join(s.b) + "".join(s.m))
        return (f'<diagram name="{e(s.name)}"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                f'pageWidth="{pw}" pageHeight="{ph}" math="0" shadow="0"><root>{cells}</root>'
                f'</mxGraphModel></diagram>')


NAV = ["Início", "Minhas Contas", "Receitas", "Despesas",
       "Instituições Financeiras", "Categorias"]

def shell(p, titulo, ativo, w, h):
    p.rect(0, 0, w, h, "", "fillColor=#F5F6F8;strokeColor=none;")
    p.rect(0, 0, 224, h, "", f"fillColor={SURF};strokeColor={BORD};")
    p.txt(20, 22, 180, 20, "dscproject", "fontStyle=1;fontSize=14;")
    for idx, t in enumerate(NAV):
        y = 70 + idx * 34
        a = (t == ativo)
        if a:
            p.rect(12, y - 4, 200, 28, "", "fillColor=#E8F0FB;strokeColor=none;")
        p.txt(24, y, 190, 20, t, f"fontColor={PRIM if a else MUT};{'fontStyle=1;' if a else ''}")
    p.rect(224, 0, w - 224, 56, "", f"fillColor={SURF};strokeColor={BORD};")
    p.txt(248, 18, 360, 20, titulo, "fontStyle=1;")
    p.txt(w - 240, 18, 220, 20, "Diego Cordeiro (DC)", f"fontColor={MUT};align=right;")


def wrap(pages_sizes):
    return ('<mxfile host="app.diagrams.net">'
            + "".join(pg.page(pw, ph) for pg, pw, ph in pages_sizes) + '</mxfile>')


# =========================================================================
# ARQUIVO 1 - WIREFRAMES DAS TELAS (QUADRO_DESCRITIVO_1..5)
# =========================================================================
CX = 258

# ---- 7.1 Despesas (Listagem) - QUADRO_DESCRITIVO_1 ----
COLS = [
    ("SEL",           30,  "chk"),
    ("COMPETÊNCIA",   80,  "09/2026"),
    ("NOME",          125, "MERCADO"),
    ("PARCELA",       60,  "3/6"),
    ("CATEGORIA",     90,  "Mercado"),
    ("PAGAMENTO",     130, "Nubank C/C"),
    ("MEIO",          65,  "Débito"),
    ("VALOR",         80,  "R$ 285,40"),
    ("LANÇAMENTO",    85,  "03/09/2026"),
    ("VENCIMENTO",    85,  "10/09/2026"),
    ("STATUS",        80,  "Pago"),
    ("PAGAMENTO EM",  85,  "03/09/2026"),
    ("RAT.",          40,  "👥"),
    ("ORIGEM",        80,  "Manual"),
    ("AÇÃO",          95,  "✎ 💵 👥 🗑"),
]
gw1 = sum(c[1] for c in COLS)
W1 = CX + gw1 + 40
H1 = 820

p1 = P("7.1 Despesas - Listagem")
shell(p1, "Despesas", "Despesas", W1, H1)
p1.txt(CX, 74, 560, 16, "Finanças  ›  Despesas", f"fontColor={MUT};fontSize=11;")
p1.txt(CX, 96, 460, 28, "Despesas", "fontStyle=1;fontSize=20;")
p1.txt(CX, 130, 760, 30,
       "Cadastre e acompanhe as suas despesas, parcelamentos e divisões de conta.",
       f"fontColor={MUT};fontSize=11;")
p1.btn(W1 - 460, 94, 150, "Registrar pagamento")
p1.btn(W1 - 290, 94, 110, "Filtrar")
p1.btn(W1 - 170, 94, 140, "Nova despesa", True)

gy = 178
p1.rect(CX, gy, gw1, 380, "", f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX, gy, gw1, 38, "", f"fillColor={HDRF};strokeColor={BORD};")
xcur = CX
col_x = []
for lb, wd, _ in COLS:
    col_x.append(xcur)
    p1.txt(xcur + 8, gy + 8, wd - 8, 24, lb, f"fontSize=9;fontColor={MUT};fontStyle=1;")
    xcur += wd

rows = [
    ["", "07/2026", "NOTEBOOK",  "1/6", "Compras",   "Nubank Mastercard", "Crédito", "R$ 500,00", "05/07/2026", "10/07/2026", "N/A",  "",           "",  "Manual", "✎  🗑"],
    ["", "09/2026", "MERCADO",   "",    "Mercado",   "Nubank C/C",        "Débito",  "R$ 285,40", "03/09/2026", "",           "Pago", "03/09/2026", "",  "Manual", "✎ 💵  🗑"],
    ["", "09/2026", "ALUGUEL",   "",    "Moradia",   "Nubank C/C",        "Boleto",  "R$ 1.800,00","01/09/2026","10/09/2026", "Aberto","",          "",  "Manual", "✎ 💵  🗑"],
    ["", "09/2026", "JANTAR",    "",    "Lazer",     "Nubank C/C",        "Débito",  "R$ 200,00", "05/09/2026", "",           "Pago", "05/09/2026", "👥", "Manual", "✎ 💵 👥 🗑"],
    ["", "09/2026", "SUPERMERC. XYZ","","Mercado",  "Nubank C/C",        "Débito",  "R$ 145,90", "09/09/2026", "",           "Aberto","",          "",  "Open Finance", "✎ 💵"],
]
rh = 40
for idx, r in enumerate(rows):
    ry = gy + 38 + idx * rh
    if idx:
        p1.line(CX, ry, CX + gw1, ry)
    for j, (lb, wd, _) in enumerate(COLS):
        v = r[j]
        st = "fontSize=9.5;"
        if lb == "VALOR": st += "align=right;"
        if v in ("Aberto",): st += f"fontColor={EDGE};"
        if v in ("Pago",): st += "fontColor=#1F9D57;"
        if v in ("N/A",): st += f"fontColor={MUT};"
        p1.txt(col_x[j] + 8, ry + 10, wd - 12, 20, v, st)
p1.txt(CX + 12, gy + 348, 520, 16, "Mostrando 5 de 12", f"fontColor={MUT};fontSize=11;")
p1.txt(CX + gw1 - 120, gy + 348, 108, 16, "‹  1  ›", f"fontColor={MUT};fontSize=11;align=right;")
p1.txt(CX, gy + 396, gw1, 40,
       "Grid client-side (DataTables) sobre a lista completa carregada uma vez (EDP02 → C1); cada parcela é uma linha (RNF05). "
       "O modal Filtrar aplica em memória (RT01/RT02). Nova despesa e ícones de ação exigem PERM_DESPESAS_MANTER; a tela, PERM_DESPESAS_LISTAR. "
       "Coluna Rateio e ícone Ratear exigem PERM_DESPESA_RATEAR_MULTIUSUARIO (plano pago). O grid nunca traz despesa de outro usuário (RN02, RN19).",
       f"fontColor={MUT};fontSize=10;")
# marcadores (por ultimo)
p1.mk(200, 70 + 3 * 34 - 2, 0)               # LINK -> item de menu "Despesas"
p1.mk(CX - 24, 70, 1)
p1.mk(CX - 24, 92, 2)
p1.mk(CX - 24, 126, 3)
p1.mk(W1 - 468, 86, 4)     # Filtrar (aparece a direita de "Registrar pagamento" na tela real; aqui so ilustrativo)
p1.mk(W1 - 46, 86, 5)      # Nova despesa
p1.mk(W1 - 468 + 150, 86, 6)  # Registrar pagamento em lote
p1.mk(CX - 22, gy + 78, 7)
p1.mk(col_x[0] + 20, gy + 6, 8)
for j in range(1, 15):
    p1.mk(col_x[j] + min(COLS[j][1], 90) - 18, gy + 6, 8 + j)   # 9..22

# ---- 7.2 Modal Filtrar Despesas - QUADRO_DESCRITIVO_2 ----
W2, H2 = 1280, 720
p2 = P("7.2 Modal Filtrar Despesas")
p2.rect(0, 0, W2, H2, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 560, (W2 - 560) // 2, 60
p2.rect(mx, my, mw, 600, "", f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.txt(mx + 20, my + 15, 320, 20, "Filtrar Despesas", "fontStyle=1;fontSize=14;")
p2.fld(mx + 24, my + 74, mw - 48, "BUSCA   (nome ou descrição — sem acento)")
cw = (mw - 64) // 2
p2.fld(mx + 24, my + 150, cw, "COMPETÊNCIA INICIAL   (mês/ano)")
p2.fld(mx + 24 + cw + 16, my + 150, cw, "COMPETÊNCIA FINAL   (mês/ano)")
p2.fld(mx + 24, my + 226, cw, "STATUS DE PAGAMENTO   (default: Todos)")
p2.fld(mx + 24 + cw + 16, my + 226, cw, "FORMA DE PAGAMENTO   (Todas + contas + cartões)")
p2.fld(mx + 24, my + 302, cw, "CATEGORIA   (Todas as categorias)")
p2.fld(mx + 24 + cw + 16, my + 302, cw, "PARCELADA   (Todas / Sim / Não)")
p2.btn(mx + mw - 230, my + 380, 100, "Limpar")
p2.btn(mx + mw - 120, my + 380, 96, "Aplicar", True)
p2.mk(mx - 11, my + 4, 1)
p2.mk(mx - 11, my + 78, 2)
p2.mk(mx - 11, my + 154, 3)
p2.mk(mx + 24 + cw + 4, my + 154, 4)
p2.mk(mx - 11, my + 230, 5)
p2.mk(mx + 24 + cw + 4, my + 230, 6)
p2.mk(mx - 11, my + 306, 7)
p2.mk(mx + 24 + cw + 4, my + 306, 8)
p2.mk(mx + mw - 124, my + 372, 9)
p2.mk(mx + mw - 234, my + 372, 10)

# ---- 7.3 Modal Cadastro / Edicao de Despesa - QUADRO_DESCRITIVO_3 ----
W3, H3 = 1280, 1720
p3 = P("7.3 Modal Despesa")
p3.rect(0, 0, W3, H3, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 680, (W3 - 680) // 2, 40
mh = H3 - 80
p3.rect(mx, my, mw, mh, "", f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.txt(mx + 20, my + 15, 460, 20, "Nova despesa  /  Editar despesa", "fontStyle=1;fontSize=14;")
cw3 = (mw - 64) // 2
y = my + 66
p3.rect(mx + 24, y, mw - 48, 40,
        "ⓘ  Despesa importada do Open Finance: forma de pagamento e origem desabilitadas; sem exclusão (RN08).",
        f"fillColor=#FBF1D9;strokeColor={EDGE};fontColor={EDGE};fontSize=9;align=left;spacingLeft=8;")
y += 56
p3.sectitle(mx + 24, y, mw - 48, "Dados básicos"); y += 30
p3.fld(mx + 24, y, mw - 48, "NOME *   (100)", edit=True); mk2 = ("dNome", mx - 11, y + 22, 3); y += 62
p3.fld(mx + 24, y, mw - 48, "DESCRIÇÃO   (512)", h=48); mk3 = ("dDescricao", mx - 11, y + 22, 4); y += 78
p3.fld(mx + 24, y, cw3, "VALOR / VALOR TOTAL DA COMPRA *   (RN05)", edit=True)
p3.fld(mx + 24 + cw3 + 16, y, cw3, "CATEGORIA   (SB03, opcional)"); y += 62
p3.fld(mx + 24, y, (mw - 48 - 32) // 3, "DATA DE LANÇAMENTO *   (default: hoje)", edit=True)
p3.fld(mx + 24 + (mw - 48 - 32) // 3 + 16, y, (mw - 48 - 32) // 3, "DATA DE VENCIMENTO")
p3.fld(mx + 24 + 2 * ((mw - 48 - 32) // 3 + 16), y, (mw - 48 - 32) // 3, "COMPETÊNCIA *   (AAAA-MM — RN04)"); y += 62

p3.sectitle(mx + 24, y, mw - 48, "Forma de pagamento"); y += 30
p3.rect(mx + 24, y, mw - 48, 34, "  Conta à vista  |  Cartão de crédito  |  Dinheiro  ", f"fillColor=#FBFCFD;fontSize=11;")
ymk_forma = y; y += 50
p3.fld(mx + 24, y, cw3, "CONTA *   (SB01 — contas ativas)")
p3.fld(mx + 24 + cw3 + 16, y, cw3, "MEIO DE PAGAMENTO   (default Débito)"); y += 62
p3.txt(mx + 24, y, cw3, 16, "JÁ PAGUEI   (oculto se Cartão — RN23)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, y + 18, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
p3.fld(mx + 24 + cw3 + 16, y, cw3, "DATA DE PAGAMENTO   (se Já paguei = Sim)", edit=True); y += 62

p3.sectitle(mx + 24, y, mw - 48, "Parcelamento   (oculta ao editar uma parcela — RN13)"); y += 30
p3.txt(mx + 24, y, cw3, 16, "PARCELADA", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p3.rect(mx + 24, y + 18, 120, 30, "  Sim  |  Não  ", f"fillColor=#FBFCFD;fontSize=11;")
ymk_parc = y; y += 62
p3.fld(mx + 24, y, cw3, "NÚMERO DE PARCELAS   (2 a 72 — RN12)", edit=True)
p3.fld(mx + 24 + cw3 + 16, y, cw3, "VALOR DA PARCELA   (calculado, somente leitura)"); y += 62

p3.sectitle(mx + 24, y, mw - 48, "Rateio   (só com PERM03 — RT10)"); y += 30
rh3 = 26
p3.rect(mx + 24, y, mw - 48, rh3, "USUÁRIO", f"fillColor={HDRF};fontSize=9;fontColor={MUT};align=left;spacingLeft=8;")
y += rh3
p3.rect(mx + 24, y, mw - 48, rh3, "Maria Silva                                    R$ 100,00        Pendente", f"fontSize=10;align=left;spacingLeft=8;")
ymk_rateio = y; y += rh3
p3.rect(mx + 24, y, mw - 48, 34, "Buscar usuário (nome/e-mail — SB06)…                         [ Adicionar ]", f"fillColor=#FBFCFD;fontSize=10;align=left;spacingLeft=8;")
y += 50
p3.txt(mx + 24, y, mw - 48, 20, "Minha cota (valor − soma das fatias)                                              R$ 100,00", f"fontColor={MUT};fontSize=10.5;")
y += 40

p3.btn(mx + mw - 230, y, 100, "Cancelar")
p3.btn(mx + mw - 120, y, 96, "Salvar", True)

p3.mk(mx - 11, my + 4, 1)
p3.mk(mx - 11, my + 70, 24)
p3.mk(*mk2[1:])
p3.mk(*mk3[1:])
# valor/categoria/datas/competencia markers foram posicionados pelos ys acima; recomputa por indice fixo
p3.mk(mx - 11, ymk_forma - 92, 2)     # secao dados basicos (titulo)
p3.mk(mx - 11, ymk_forma - 30, 5)     # valor
p3.mk(mx + 24 + cw3 + 4, ymk_forma - 30, 9)   # categoria
p3.mk(mx - 11, ymk_forma - 62, 6)     # (posicoes aproximadas — ver nota abaixo)
p3.mk(mx - 11, ymk_forma - 8, 10)     # secao forma de pagamento (titulo)
p3.mk(mx - 11, ymk_forma + 46, 11)    # segmented forma de pagamento
p3.mk(mx - 11, ymk_forma + 96, 12)    # conta
p3.mk(mx + 24 + cw3 + 4, ymk_forma + 96, 15)  # meio de pagamento
p3.mk(mx - 11, ymk_forma + 158, 16)   # ja paguei
p3.mk(mx + 24 + cw3 + 4, ymk_forma + 158, 17) # data pagamento
p3.mk(mx - 11, ymk_parc - 30, 18)     # secao parcelamento (titulo)
p3.mk(mx - 11, ymk_parc + 20, 19)     # parcelada toggle
p3.mk(mx - 11, ymk_parc + 82, 20)     # numero parcelas
p3.mk(mx + 24 + cw3 + 4, ymk_parc + 82, 21)   # valor parcela calculado
p3.mk(mx - 11, ymk_rateio - 82, 22)   # secao rateio (titulo)
p3.mk(mx - 11, ymk_rateio - 26, 23)   # tabela de co-participantes
p3.mk(mx + mw - 124, y - 8, 25)
p3.mk(mx + mw - 234, y - 8, 26)
p3.mk(mx + 24 + (mw - 48 - 32) // 3 + 4, ymk_forma - 92 + 62, 7)   # data vencimento (aprox)
p3.mk(mx + 24 + 2 * ((mw - 48 - 32) // 3 + 16) - 12, ymk_forma - 92 + 62, 8)  # competencia (aprox)
p3.mk(mx + 24 + cw3 + 4, ymk_forma - 92 + 62 - 62, 13)   # cartao (referencia — oculto no estado exibido)
p3.mk(mx + 24 + cw3 + 4, ymk_forma - 92 + 62 - 62, 14)   # conta carteira (referencia — oculto no estado exibido)

# ---- 7.4 Modal Registrar Pagamento - QUADRO_DESCRITIVO_4 ----
W4, H4 = 1000, 620
p4 = P("7.4 Modal Registrar Pagamento")
p4.rect(0, 0, W4, H4, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 480, (W4 - 480) // 2, 130
p4.rect(mx, my, mw, 340, "", f"fillColor={SURF};strokeColor={BORD};")
p4.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p4.txt(mx + 20, my + 15, 400, 20, "Registrar pagamento — {despesa}", "fontStyle=1;fontSize=14;")
p4.txt(mx + 24, my + 74, mw - 48, 16, "VALOR DA DESPESA   (somente leitura)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p4.rect(mx + 24, my + 92, mw - 48, 32, "R$ 1.800,00", f"fillColor=#F5F6F8;strokeColor={BORD};fontStyle=2;dashed=1;")
p4.fld(mx + 24, my + 140, mw - 48, "DATA DE PAGAMENTO *   (default: hoje)", edit=True)
p4.btn(mx + mw - 230, my + 260, 100, "Cancelar")
p4.btn(mx + mw - 120, my + 260, 96, "Registrar", True)
p4.mk(mx - 11, my + 4, 1)
p4.mk(mx - 11, my + 96, 2)
p4.mk(mx - 11, my + 144, 3)
p4.mk(mx + mw - 124, my + 252, 4)
p4.mk(mx + mw - 234, my + 252, 5)

# ---- 7.5 Modal Confirmar Pagamento em Lote - QUADRO_DESCRITIVO_5 ----
W5, H5 = 1000, 680
p5 = P("7.5 Modal Pagamento em Lote")
p5.rect(0, 0, W5, H5, "", "fillColor=#EDEFF3;strokeColor=none;")
mw, mx, my = 520, (W5 - 520) // 2, 130
p5.rect(mx, my, mw, 400, "", f"fillColor={SURF};strokeColor={BORD};")
p5.rect(mx, my, mw, 50, "", f"fillColor=#FBFCFD;strokeColor={BORD};")
p5.txt(mx + 20, my + 15, 400, 20, "Registrar pagamento em lote", "fontStyle=1;fontSize=14;")
p5.txt(mx + 24, my + 74, mw - 48, 16, "RESUMO DA SELEÇÃO   (somente leitura)", f"fontSize=11;fontColor={MUT};fontStyle=1;")
p5.rect(mx + 24, my + 92, mw - 48, 100,
        "4 despesas selecionadas — total R$ 780,00\n09/2026 · MERCADO · R$ 285,40\n09/2026 · ALUGUEL · R$ 1.800,00 …",
        f"fillColor=#F5F6F8;strokeColor={BORD};align=left;spacingLeft=8;fontSize=10;")
p5.fld(mx + 24, my + 210, mw - 48, "DATA DE PAGAMENTO *   (default: hoje, aplicada a todas)", edit=True)
p5.btn(mx + mw - 230, my + 320, 100, "Cancelar")
p5.btn(mx + mw - 120, my + 320, 96, "Registrar", True)
p5.mk(mx - 11, my + 4, 1)
p5.mk(mx - 11, my + 96, 2)
p5.mk(mx - 11, my + 214, 3)
p5.mk(mx + mw - 124, my + 312, 4)
p5.mk(mx + mw - 234, my + 312, 5)

xml1 = wrap([(p1, W1, H1), (p2, W2, H2), (p3, W3, H3), (p4, W4, H4), (p5, W5, H5)])
M.parseString(xml1)
open(os.path.join(OUT, "manter-despesa-prototipo.drawio"), "w", encoding="utf-8").write(xml1)

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
        return ('<diagram name="DER - Manter Despesa"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                'pageWidth="1680" pageHeight="1060" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

d = Der()
d.rect(0, 0, 1680, 1060, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
d.rect(40, 18, 1560, 20,
       "Subconjunto do DER do Documento 0 — QUADRO_DESCRITIVO_10 (DESPESAS, com auto-relacionamento DESP_ID_PARCELA_PAI), "
       "_11 (DESPESAS_USUARIO), _5 (CONTAS), _6 (CARTOES_CREDITO), _3 (CATEGORIAS), _8 (FATURAS_CARTAO) e _2 (USUARIOS). "
       "Este documento não cria tabela nova nem faz ALTER TABLE.",
       f"text;html=1;align=left;fontFamily=Helvetica;fontSize=11;fontColor={MUT};")

usu = d.entity(40, 90, "USUARIOS", [
    ("USU_ID  (PK)", 1), ("USU_NOME", 0), ("USU_EMAIL  (UK)", 0),
    ("dono via CONTAS/CARTOES_CREDITO", 0),
    ("co-participante — DESPESAS_USUARIO.USU_ID", 0),
    ("audit_*", 0),
], "#F1EAF7", "#5B3B86", wd=330)

cta = d.entity(40, 320, "CONTAS", [
    ("CTA_ID  (PK)", 1), ("CTA_DESCRICAO", 0), ("CTA_TIPO  {…|CARTEIRA}", 0),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 1),
    ("âncora de dono — DESP.CTA_ID (RN02, RN03)", 0),
    ("dinheiro em espécie = conta CARTEIRA (Obs. 2a)", 0),
    ("audit_*", 0),
], "#EAF2FB", "#1F3864", wd=350)

cacr = d.entity(40, 590, "CARTOES_CREDITO", [
    ("CACR_ID  (PK)", 1), ("CACR_DESCRICAO", 0),
    ("USU_ID  (FK → USUARIOS)  NOT NULL", 1),
    ("âncora de dono alternativa — DESP.CACR_ID (RN02, RN03)", 0),
    ("audit_*", 0),
], "#FBF3E6", "#8A5A12", wd=350)

cate = d.entity(40, 800, "CATEGORIAS", [
    ("CATE_ID  (PK)", 1), ("CATE_NOME", 0),
    ("CATE_APLICA_A  {DESPESA|RECEITA|AMBOS}", 0),
    ("só leitura aqui — documento 2.01 (SB03)", 0),
    ("audit_*", 0),
], "#EAF7EF", "#1F6B45", wd=330)

desp = d.entity(500, 260, "DESPESAS", [
    ("DESP_ID  (PK)", 1),
    ("DESP_NOME  ·  DESP_DESCRICAO", 0),
    ("DESP_VALOR  DECIMAL(15,2)  — > 0 (RN05)", 0),
    ("DESP_VALOR_TOTAL_COMPRA  DECIMAL(15,2)  NULL", 0),
    ("DESP_COMPETENCIA  CHAR(7)  yyyy-MM  (YearMonthConverter)", 0),
    ("DESP_DT_LANCAMENTO  ·  DESP_DT_VENCIMENTO  ·  DESP_DT_PAGAMENTO", 0),
    ("DESP_FL_PARCELADA  ·  DESP_NRO_PARCELA  ·  DESP_QTD_PARCELAS", 0),
    ("DESP_ID_PARCELA_PAI  (FK → DESPESAS, self)  NULL", 1),
    ("DESP_MEIO_PAGAMENTO  {DINHEIRO|DEBITO|CREDITO|PIX|BOLETO|TRANSFERENCIA}", 0),
    ("DESP_IND_STATUS_PAGAMENTO  {NAO|SIM|NAO_SE_APLICA}", 0),
    ("DESP_FL_PAGAMENTO_FATURA  BOOLEAN", 0),
    ("DESP_ORIGEM  {MANUAL|OPEN_FINANCE|IMPORTACAO}  default MANUAL", 0),
    ("CTA_ID  (FK → CONTAS)  NULLABLE  — XOR com CACR_ID (RN03)", 1),
    ("CACR_ID  (FK → CARTOES_CREDITO)  NULLABLE", 1),
    ("FTCA_ID  (FK → FATURAS_CARTAO)  NULL  — preenchido no documento 2.08", 0),
    ("CATE_ID  (FK → CATEGORIAS)  NULLABLE", 0),
    ("audit_*  — @Audited (Hibernate Envers)", 0),
], "#FDEAF2", "#8A1D5C", wd=470)

depu = d.entity(1050, 300, "DESPESAS_USUARIO", [
    ("DESP_ID  (FK → DESPESAS)  +  USU_ID  (FK → USUARIOS)  — UNIQUE", 1),
    ("DEPU_VALOR  DECIMAL(15,2)  — fatia do co-participante", 0),
    ("DEPU_IND_STATUS_PAGAMENTO  {SIM|NAO|NAO_SE_APLICA}", 0),
    ("DEPU_DT_ACERTO  DATE  NULL", 0),
    ("soma das fatias ≤ DESP_VALOR (RN17)", 0),
    ("audit_*", 0),
], "#FDEAF2", "#8A1D5C", wd=380)

ftca = d.entity(1050, 560, "FATURAS_CARTAO", [
    ("FTCA_ID  (PK)", 1),
    ("ciclo ABERTA → FECHADA → PAGA", 0),
    ("DESP.FTCA_ID  aponta para cá — preenchido pelo documento 2.08", 0),
    ("audit_*", 0),
], "#EAEAF7", "#3B3B86", wd=380)

d.edge(usu, cta, "1 → N   USU_ID (NOT NULL)")
d.edge(usu, cacr, "1 → N   USU_ID (NOT NULL)")
d.edge(usu, depu, "1 → N   USU_ID (co-participante)")
d.edge(cta, desp, "1 → N   CTA_ID (NULLABLE · XOR)")
d.edge(cacr, desp, "1 → N   CACR_ID (NULLABLE · XOR)")
d.edge(cate, desp, "1 → N   CATE_ID (NULLABLE)")
d.edge(desp, desp, "1 → N   DESP_ID_PARCELA_PAI (self, NULLABLE)", dashed=True)
d.edge(desp, depu, "1 → N   DESP_ID (rateio)")
d.edge(ftca, desp, "1 → N   FTCA_ID (NULL — doc. 2.08)", dashed=True)

d.rect(500, 720, 470, 260,
       "DESPESAS: CRUD + parcelamento (série auto-relacionada por DESP_ID_PARCELA_PAI, RN12/RN13/RN14) + baixa de "
       "pagamento (RN21), sempre no escopo do USU_ID da conta OU do cartão (RN02).\n\n"
       "A âncora conta-XOR-cartão (RN03) é validada no serviço, não no schema — as duas FKs continuam anuláveis.\n\n"
       "DESPESAS_USUARIO é o rateio entre usuários reais (RN15-RN20); FATURAS_CARTAO e o vínculo FTCA_ID são do "
       "documento 2.08 — nesta tela FTCA_ID fica sempre nulo (RN23).",
       f"text;html=1;align=left;verticalAlign=top;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")

xml2 = '<mxfile host="app.diagrams.net">' + d.page() + '</mxfile>'
M.parseString(xml2)
open(os.path.join(OUT, "manter-despesa-der.drawio"), "w", encoding="utf-8").write(xml2)

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
    def uc(s, x, y, code, name, w=290, h=52):
        cid = s.i()
        s.b.append(f'<mxCell id="{cid}" value="{e(code + " — " + name)}" style="ellipse;whiteSpace=wrap;html=1;'
                   f'fillColor={SURF};strokeColor={PRIM};fontColor={INK};fontFamily=Helvetica;fontSize=11;" '
                   f'vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
        return cid
    def link(s, a, b, dashed=False):
        d = "dashed=1;" if dashed else ""
        s.b.append(f'<mxCell id="{s.i()}" style="endArrow=none;html=1;{d}strokeColor={MUT};" edge="1" parent="1" '
                   f'source="{a}" target="{b}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    def rect(s, x, y, w, h, v, st):
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(st)}" vertex="1" parent="1">'
                   f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def page(s):
        return ('<diagram name="Casos de Uso - Manter Despesa"><mxGraphModel dx="1400" dy="900" grid="0" '
                'page="1" pageWidth="1300" pageHeight="1120" math="0" shadow="0"><root>'
                '<mxCell id="0"/><mxCell id="1" parent="0"/>' + "".join(s.b) +
                '</root></mxGraphModel></diagram>')

u = Uc()
u.rect(0, 0, 1300, 1120, "", "rounded=0;html=1;fillColor=#F7F9FB;strokeColor=none;")
u.rect(380, 30, 620, 1040, "Manter Despesa — tela \"Despesas\"",
       f"rounded=0;html=1;fillColor=none;strokeColor={BORD};verticalAlign=top;fontColor={MUT};fontSize=12;spacingTop=8;")
adm = u.actor(110, 300, "ADMIN (PERF01)")
usr = u.actor(110, 620, "USER (PERF02)")

ucs = [
    ("CAUS01", "Listar Minhas Despesas", False),
    ("CAUS02", "Filtrar Despesas", False),
    ("CAUS03", "Cadastrar Despesa à Vista", False),
    ("CAUS04", "Cadastrar Despesa no Cartão", False),
    ("CAUS05", "Cadastrar Despesa Parcelada", False),
    ("CAUS06", "Ratear Despesa entre Usuários", True),
    ("CAUS07", "Registrar Acerto de Fatia", True),
    ("CAUS08", "Editar Despesa", False),
    ("CAUS09", "Excluir Despesa ou Série", False),
    ("CAUS10", "Registrar Pagamento", False),
]
ids = []
for idx, (c_, n, perm3) in enumerate(ucs):
    cy = 70 + idx * 96
    cid = u.uc(500, cy, c_, n + ("  (PERM03)" if perm3 else ""))
    ids.append((cid, perm3))
for cid, perm3 in ids:
    u.link(adm, cid, dashed=perm3)
    u.link(usr, cid, dashed=perm3)

u.rect(380, 1006 + 60, 620, 56,
       "ADMIN e USER têm o mesmo acesso; cada um opera só sobre as próprias despesas (RN02). "
       "CAUS06/CAUS07 (linha tracejada) exigem, além do perfil, a permissão DESPESA_RATEAR_MULTIUSUARIO — "
       "concedida por plano pago, sem vínculo a nenhum perfil na carga inicial (RN15).",
       f"text;html=1;align=center;fontFamily=Helvetica;fontSize=10;fontColor={MUT};whiteSpace=wrap;")
xml3 = '<mxfile host="app.diagrams.net">' + u.page() + '</mxfile>'
M.parseString(xml3)
open(os.path.join(OUT, "manter-despesa-casos-uso.drawio"), "w", encoding="utf-8").write(xml3)

print("ok - 3 arquivos gerados (prototipo 5 telas, der, casos-uso)")
