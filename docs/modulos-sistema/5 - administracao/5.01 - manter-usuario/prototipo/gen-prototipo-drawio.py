#!/usr/bin/env python3
# Wireframes draw.io (multi-página) das telas do documento 5.01 - Manter Usuário.
# Marcadores numéricos emitidos POR ÚLTIMO em cada página (ficam na frente no PNG).
import html, os

OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1280, 860
def esc(s): return html.escape(str(s), quote=True)

INK="#1B2430"; MUT="#69747F"; BORD="#D8DEE6"; SURF="#FFFFFF"; GRID_HDR="#F1F3F6"
PRIM="#1F6FD0"; MARK="#D1367F"; BADGE="#E8EEF7"

class Page:
    def __init__(s, name):
        s.name=name; s.body=[]; s.marks=[]; s.n=0
    def _id(s): s.n+=1; return f"{s.name[:3]}{s.n}"
    def rect(s,x,y,w,h,val="",style=""):
        st=f"rounded=0;whiteSpace=wrap;html=1;fillColor={SURF};strokeColor={BORD};fontColor={INK};fontFamily=Helvetica;fontSize=12;{style}"
        s.body.append(f'<mxCell id="{s._id()}" value="{esc(val)}" style="{esc(st)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def text(s,x,y,w,h,val,style=""):
        st=f"text;html=1;align=left;verticalAlign=middle;fontFamily=Helvetica;fontColor={INK};fontSize=12;{style}"
        s.body.append(f'<mxCell id="{s._id()}" value="{esc(val)}" style="{esc(st)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def btn(s,x,y,w,val,primary=False):
        fill=PRIM if primary else SURF; fc="#FFFFFF" if primary else INK
        st=f"rounded=1;arcSize=30;whiteSpace=wrap;html=1;fillColor={fill};strokeColor={PRIM if primary else BORD};fontColor={fc};fontFamily=Helvetica;fontSize=12;fontStyle=1;"
        s.body.append(f'<mxCell id="{s._id()}" value="{esc(val)}" style="{esc(st)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="34" as="geometry"/></mxCell>')
    def field(s,x,y,w,label):
        s.text(x,y,w,16,label,f"fontSize=11;fontColor={MUT};fontStyle=1;")
        s.rect(x,y+18,w,32,"",f"fillColor=#FBFCFD;")
    def mark(s,x,y,num):
        st=f"ellipse;whiteSpace=wrap;html=1;fillColor={MARK};strokeColor=#FFFFFF;strokeWidth=2;fontColor=#FFFFFF;fontFamily=Helvetica;fontSize=11;fontStyle=1;"
        s.marks.append(f'<mxCell id="mk_{s.name[:3]}_{num}" value="{num}" style="{esc(st)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="22" height="22" as="geometry"/></mxCell>')
    def xml(s):
        cells='<mxCell id="0"/><mxCell id="1" parent="0"/>'+"".join(s.body)+"".join(s.marks)
        return (f'<diagram name="{esc(s.name)}">'
                f'<mxGraphModel dx="1400" dy="900" grid="0" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" '
                f'fold="1" page="1" pageScale="1" pageWidth="{W}" pageHeight="{H}" math="0" shadow="0"><root>{cells}</root></mxGraphModel></diagram>')

def shell(p, titulo):
    p.rect(0,0,W,H,"",f"fillColor=#F5F6F8;strokeColor=none;")
    p.rect(0,0,224,H,"",f"fillColor={SURF};strokeColor={BORD};")          # sidebar
    p.text(20,22,180,20,"dscproject",f"fontStyle=1;fontSize=14;")
    for i,(t,act) in enumerate([("Início",0),("Contas",0),("Despesas",0),("Investimentos",0),("Usuários",1)]):
        y=70+i*38
        if act: p.rect(12,y-4,200,32,"",f"fillColor=#E8F0FB;strokeColor=none;")
        p.text(24,y,180,20,t,f"fontColor={PRIM if act else MUT};{'fontStyle=1;' if act else ''}")
    p.rect(224,0,W-224,56,"",f"fillColor={SURF};strokeColor={BORD};")     # topbar
    p.text(248,18,300,20,titulo,f"fontStyle=1;")
    p.text(W-220,18,200,20,"Diego Cordeiro  (DC)",f"fontColor={MUT};align=right;")

# ---------- Página 1: Listagem ----------
p1=Page("7.1 Usuarios - Listagem")
shell(p1,"Usuários")
CX=260
p1.text(CX,78,400,16,"Administração  ›  Usuários",f"fontColor={MUT};fontSize=11;")
p1.text(CX,98,300,28,"Usuários",f"fontStyle=1;fontSize=20;")
p1.text(CX,132,420,16,"Gerencie os usuários do sistema.",f"fontColor={MUT};")
p1.btn(W-360,96,110,"Filtrar")
p1.btn(W-238,96,150,"Novo usuário",True)
# grid
gy=170
p1.rect(CX,gy,W-CX-40,470,"",f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX,gy,W-CX-40,40,"",f"fillColor={GRID_HDR};strokeColor={BORD};")
cols=["NOME","LOGIN","E-MAIL","PERFIL","GÊNERO","CRIADO EM","SITUAÇÃO","AÇÃO"]
colx=[CX+16,CX+150,CX+250,CX+430,CX+520,CX+620,CX+740,CX+860]
for c,x in zip(cols,colx):
    p1.text(x,gy+12,110,16,c,f"fontSize=10;fontColor={MUT};fontStyle=1;")
sample=[("Diego Cordeiro","diego","diego@dscproject.dev","ADMIN","Masculino","12/03/2024","Ativo"),
        ("Ana Beatriz Lima","ana.lima","ana.lima@exemplo.com","USER","Feminino","04/06/2024","Ativo"),
        ("Carlos Nunes","cnunes","carlos.nunes@exemplo.com","USER","Masculino","19/08/2024","Ativo"),
        ("Marina Souza","marina.s","marina@exemplo.com","USER","Feminino","02/11/2024","Ativo"),
        ("Bruno Carvalho","bcarv","bruno.c@exemplo.com","USER","Outro","09/04/2025","Excluído")]
for i,r in enumerate(sample):
    ry=gy+40+i*54
    if i: p1.body.append(f'<mxCell id="ln{i}" style="endArrow=none;strokeColor={BORD};html=1;" edge="1" parent="1"><mxGeometry as="geometry"><Array as="points"><mxPoint x="{CX}" y="{ry}"/><mxPoint x="{W-40}" y="{ry}"/></Array></mxGeometry></mxCell>')
    for v,x in zip(r,colx):
        p1.text(x,ry+16,120,16,v,"fontSize=11;")
    p1.text(colx[7],ry+16,110,16,"✎   🗑   ↺" if r[6]=="Ativo" else "↺","fontSize=12;fontColor="+MUT+";")
p1.text(CX+16,gy+430,300,16,"Mostrando 1–8 de 12",f"fontColor={MUT};fontSize=11;")
p1.text(W-160,gy+430,110,16,"‹  1  2  ›",f"fontColor={MUT};fontSize=11;align=right;")
# marcadores (por último)
p1.mark(196,66,0); p1.mark(CX-24,74,1); p1.mark(CX-24,100,2); p1.mark(CX-24,128,3)
p1.mark(W-372,88,4); p1.mark(W-96,88,5); p1.mark(CX-24,gy+90,6)
for j,x in enumerate(colx): p1.mark(x+70,gy+8,7+j)   # 7..14
p1.mark(colx[7]+2,gy+40+1*54+8,15); p1.mark(colx[7]+24,gy+40+1*54+8,16); p1.mark(colx[7]+46,gy+40+1*54+8,17)

# ---------- Página 2: Modal Filtrar ----------
p2=Page("7.2 Modal Filtrar")
p2.rect(0,0,W,H,"",f"fillColor=#EDEFF3;strokeColor=none;")
mx,my,mw=(W-520)//2,150,520
p2.rect(mx,my,mw,360,"",f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx,my,mw,52,"",f"fillColor=#FBFCFD;strokeColor={BORD};")
p2.text(mx+20,my+16,300,20,"Filtrar Usuários",f"fontStyle=1;fontSize=14;")
p2.field(mx+24,my+80,mw-48,"BUSCA (nome, login ou e-mail)")
p2.field(mx+24,my+150,(mw-64)//2,"PERFIL")
p2.field(mx+24+(mw-64)//2+16,my+150,(mw-64)//2,"SITUAÇÃO")
p2.btn(mx+mw-230,my+300,100,"Limpar")
p2.btn(mx+mw-120,my+300,96,"Aplicar",True)
p2.mark(mx-4,my+6,1); p2.mark(mx+8,my+92,2); p2.mark(mx+8,my+162,3)
p2.mark(mx+24+(mw-64)//2+4,my+162,4); p2.mark(mx+mw-124,my+292,5); p2.mark(mx+mw-234,my+292,6)

# ---------- Página 3: Modal Cadastro/Edição ----------
p3=Page("7.3 Modal Cadastro-Edicao")
p3.rect(0,0,W,H,"",f"fillColor=#EDEFF3;strokeColor=none;")
mw=620; mx=(W-mw)//2; my=90
p3.rect(mx,my,mw,660,"",f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx,my,mw,52,"",f"fillColor=#FBFCFD;strokeColor={BORD};")
p3.text(mx+20,my+16,320,20,"Novo usuário  /  Editar usuário",f"fontStyle=1;fontSize=14;")
cw=(mw-64)//2
p3.field(mx+24,my+80,mw-48,"NOME *")
p3.field(mx+24,my+150,cw,"GÊNERO *"); p3.field(mx+24+cw+16,my+150,cw,"DATA DE NASCIMENTO *")
p3.field(mx+24,my+220,cw,"E-MAIL *"); p3.field(mx+24+cw+16,my+220,cw,"LOGIN *")
p3.field(mx+24,my+290,cw,"SENHA *"); p3.field(mx+24+cw+16,my+290,cw,"CONFIRMAÇÃO DE SENHA *")
p3.field(mx+24,my+360,mw-48,"PERFIL *   (só o administrador vê)")
p3.btn(mx+mw-230,my+600,100,"Cancelar")
p3.btn(mx+mw-120,my+600,96,"Salvar",True)
for i,(dx,dy) in enumerate([(mx-4,my+6),(mx+8,my+92),(mx+8,my+162),(mx+24+cw+4,my+162),
    (mx+8,my+232),(mx+24+cw+4,my+232),(mx+8,my+302),(mx+24+cw+4,my+302),(mx+8,my+372)],start=1):
    p3.mark(dx,dy,i)
p3.mark(mx+mw-124,my+592,10); p3.mark(mx+mw-234,my+592,11)

# ---------- Página 4: Auto-cadastro ----------
p4=Page("7.4 Auto-cadastro publico")
p4.rect(0,0,W,H,"",f"fillColor=#F5F6F8;strokeColor=none;")
pw=560; px=(W-pw)//2; py=70
p4.rect(px,py,pw,700,"",f"fillColor={SURF};strokeColor={BORD};")
p4.text(px+28,py+26,300,24,"Cadastrar Usuário",f"fontStyle=1;fontSize=17;")
p4.text(px+28,py+56,400,18,"Crie a sua conta no dscproject.",f"fontColor={MUT};")
cw=(pw-72)//2
labels=[("NOME *",0),("GÊNERO *",1),("DATA DE NASCIMENTO *",0),("E-MAIL *",1),("LOGIN *",0),("SENHA *",1),("CONFIRMAÇÃO DE SENHA *",0)]
for i,(lb,col) in enumerate(labels):
    row=i//2; c=i%2
    p4.field(px+28+c*(cw+16), py+100+row*70, cw, lb)
p4.text(px+28,py+520,240,18,"Já tenho conta",f"fontColor={PRIM};")
p4.btn(px+pw-170,py+512,140,"Cadastrar",True)
p4.text(px+28,py+570,pw-56,18,"Cria sempre com perfil USER (RN08). Sem verificação de e-mail na v1 (RN17).",f"fontColor={MUT};fontSize=11;")
p4.mark(px-4,py+22,1); p4.mark(px+8,py+112,2); p4.mark(px+pw-176,py+504,3); p4.mark(px+16,py+514,4)

# ---------- Página 5: Recuperar Senha ----------
p5=Page("7.5 Recuperar senha publico")
p5.rect(0,0,W,H,"",f"fillColor=#F5F6F8;strokeColor=none;")
pw=460; px=(W-pw)//2; py=60
# etapa 1
p5.rect(px,py,pw,300,"",f"fillColor={SURF};strokeColor={BORD};")
p5.text(px+24,py+20,300,18,"Etapa 1 — Informar e-mail",f"fontStyle=1;fontColor={PRIM};fontSize=11;")
p5.text(px+24,py+44,300,22,"Recuperar senha",f"fontStyle=1;fontSize=16;")
p5.text(px+24,py+72,pw-48,32,"Informe o e-mail da conta. Enviaremos um link para redefinir a senha.",f"fontColor={MUT};fontSize=11;")
p5.field(px+24,py+118,pw-48,"E-MAIL *")
p5.btn(px+24,py+180,pw-48,"Enviar link",True)
p5.text(px+24,py+228,pw-48,30,"A resposta é sempre a mesma, exista o e-mail ou não (RNF07).",f"fontColor={MUT};fontSize=10;")
# etapa 2
p5.rect(px,py+330,pw,320,"",f"fillColor={SURF};strokeColor={BORD};")
p5.text(px+24,py+350,300,18,"Etapa 2 — Nova senha (via link do e-mail)",f"fontStyle=1;fontColor={PRIM};fontSize=11;")
p5.field(px+24,py+386,pw-48,"NOVA SENHA *")
p5.field(px+24,py+452,pw-48,"CONFIRMAÇÃO *")
p5.btn(px+24,py+516,pw-48,"Definir nova senha",True)
p5.mark(px+8,py+130,1); p5.mark(px+pw-40,py+172,2); p5.mark(px+8,py+398,3); p5.mark(px+8,py+464,4); p5.mark(px+pw-40,py+508,5)

pages=[p1,p2,p3,p4,p5]
xml='<mxfile host="app.diagrams.net">'+"".join(pg.xml() for pg in pages)+'</mxfile>'
path=os.path.join(OUT,"manter-usuario-prototipo.drawio")
open(path,"w", encoding="utf-8").write(xml)
# valida
import xml.dom.minidom as m; m.parseString(xml)
print("ok:",path,"—",len(pages),"páginas")
