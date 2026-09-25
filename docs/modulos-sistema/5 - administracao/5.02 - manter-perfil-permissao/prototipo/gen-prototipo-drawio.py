#!/usr/bin/env python3
# Wireframes draw.io das telas do documento 5.02 - Manter Perfil e Permissões.
import html, os, xml.dom.minidom as M
OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1280, 860
def e(s): return html.escape(str(s), quote=True)
INK="#1B2430"; MUT="#69747F"; BORD="#D8DEE6"; SURF="#FFFFFF"; HDRF="#F1F3F6"
PRIM="#1F6FD0"; MARK="#D1367F"

class P:
    def __init__(s,name): s.name=name; s.b=[]; s.m=[]; s.n=0
    def i(s): s.n+=1; return f"{abs(hash(s.name))%997}_{s.n}"
    def rect(s,x,y,w,h,v="",st=""):
        style=f"rounded=0;whiteSpace=wrap;html=1;fillColor={SURF};strokeColor={BORD};fontColor={INK};fontFamily=Helvetica;fontSize=12;{st}"
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def txt(s,x,y,w,h,v,st=""):
        style=f"text;html=1;align=left;verticalAlign=middle;fontFamily=Helvetica;fontColor={INK};fontSize=12;{st}"
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    def btn(s,x,y,w,v,pri=False):
        f=PRIM if pri else SURF; fc="#FFFFFF" if pri else INK
        style=f"rounded=1;arcSize=30;whiteSpace=wrap;html=1;fillColor={f};strokeColor={PRIM if pri else BORD};fontColor={fc};fontFamily=Helvetica;fontSize=12;fontStyle=1;"
        s.b.append(f'<mxCell id="{s.i()}" value="{e(v)}" style="{e(style)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="34" as="geometry"/></mxCell>')
    def fld(s,x,y,w,lb):
        s.txt(x,y,w,16,lb,f"fontSize=11;fontColor={MUT};fontStyle=1;"); s.rect(x,y+18,w,32,"","fillColor=#FBFCFD;")
    def chk(s,x,y,v,on=True):
        s.rect(x,y,16,16,"","fillColor="+("#1F6FD0" if on else "#FBFCFD")+f";strokeColor={PRIM if on else BORD};")
        s.txt(x+24,y-2,260,20,v,"fontSize=11;")
    def mk(s,x,y,num):
        st=f"ellipse;whiteSpace=wrap;html=1;fillColor={MARK};strokeColor=#FFFFFF;strokeWidth=2;fontColor=#FFFFFF;fontFamily=Helvetica;fontSize=11;fontStyle=1;"
        s.m.append(f'<mxCell id="mk{s.n}_{num}" value="{num}" style="{e(st)}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="22" height="22" as="geometry"/></mxCell>')
    def xml(s):
        cells='<mxCell id="0"/><mxCell id="1" parent="0"/>'+"".join(s.b)+"".join(s.m)
        return (f'<diagram name="{e(s.name)}"><mxGraphModel dx="1400" dy="900" grid="0" page="1" '
                f'pageWidth="{W}" pageHeight="{H}" math="0" shadow="0"><root>{cells}</root></mxGraphModel></diagram>')

def shell(p,titulo):
    p.rect(0,0,W,H,"","fillColor=#F5F6F8;strokeColor=none;")
    p.rect(0,0,224,H,"",f"fillColor={SURF};strokeColor={BORD};")
    p.txt(20,22,180,20,"dscproject","fontStyle=1;fontSize=14;")
    for i,(t,a) in enumerate([("Início",0),("Financeiro",0),("Usuários",0),("Perfis e Permissões",1)]):
        y=70+i*38
        if a: p.rect(12,y-4,200,32,"","fillColor=#E8F0FB;strokeColor=none;")
        p.txt(24,y,190,20,t,f"fontColor={PRIM if a else MUT};{'fontStyle=1;' if a else ''}")
    p.rect(224,0,W-224,56,"",f"fillColor={SURF};strokeColor={BORD};")
    p.txt(248,18,320,20,titulo,"fontStyle=1;")
    p.txt(W-220,18,200,20,"Diego Cordeiro (DC)",f"fontColor={MUT};align=right;")

# ---- 1: Listagem de Perfis ----
p1=P("7.1 Perfis - Listagem"); shell(p1,"Perfis e Permissões")
CX=260
p1.txt(CX,78,400,16,"Administração  ›  Perfis e Permissões",f"fontColor={MUT};fontSize=11;")
p1.txt(CX,98,320,28,"Perfis e Permissões","fontStyle=1;fontSize=20;")
p1.btn(W-430,96,190,"Catálogo de permissões")
p1.btn(W-232,96,150,"Novo perfil",True)
gy=170
p1.rect(CX,gy,W-CX-40,320,"",f"fillColor={SURF};strokeColor={BORD};")
p1.rect(CX,gy,W-CX-40,40,"",f"fillColor={HDRF};strokeColor={BORD};")
cols=["CÓDIGO","NOME","Nº PERMISSÕES","Nº USUÁRIOS","TIPO / SITUAÇÃO","AÇÃO"]
cx=[CX+16,CX+150,CX+340,CX+490,CX+640,CX+860]
for c,x in zip(cols,cx): p1.txt(x,gy+12,150,16,c,f"fontSize=10;fontColor={MUT};fontStyle=1;")
rows=[("ADMIN","Administrador","24","2","Sistema · Ativo"),
      ("USER","Usuário comum","6","5","Sistema · Ativo"),
      ("RELATORIOS","Relatórios","4","1","Ativo")]
for i,r in enumerate(rows):
    ry=gy+40+i*56
    for v,x in zip(r,cx): p1.txt(x,ry+18,150,16,v,"fontSize=11;")
    p1.txt(cx[5],ry+18,110,16,"✎    🗑" if r[4]=="Ativo" else "✎","fontSize=12;fontColor="+MUT+";")
p1.mk(CX-24,74,1); p1.mk(CX-24,100,2); p1.mk(W-442,88,3); p1.mk(W-96,88,4); p1.mk(CX-24,gy+90,5)
for j,x in enumerate(cx): p1.mk(x+90,gy+8,6+j)   # 6..11

# ---- 2: Modal Cadastro/Edição de Perfil + seletor de permissões ----
p2=P("7.2 Modal Perfil"); p2.rect(0,0,W,H,"","fillColor=#EDEFF3;strokeColor=none;")
mw=680; mx=(W-mw)//2; my=50
p2.rect(mx,my,mw,760,"",f"fillColor={SURF};strokeColor={BORD};")
p2.rect(mx,my,mw,50,"","fillColor=#FBFCFD;strokeColor="+BORD+";")
p2.txt(mx+20,my+14,320,20,"Novo perfil  /  Editar perfil","fontStyle=1;fontSize=14;")
cw=(mw-64)//2
p2.fld(mx+24,my+74,cw,"CÓDIGO *   (desabilitado se for perfil de sistema)")
p2.fld(mx+24+cw+16,my+74,cw,"NOME *")
p2.fld(mx+24,my+144,mw-48,"DESCRIÇÃO")
# seletor de permissões
p2.txt(mx+24,my+210,300,16,"PERMISSÕES DO PERFIL",f"fontSize=11;fontColor={MUT};fontStyle=1;")
p2.txt(mx+mw-260,my+210,236,16,"18 de 26 permissões selecionadas",f"fontSize=11;fontColor={MUT};align=right;")
p2.rect(mx+24,my+232,mw-48,380,"","fillColor=#FBFCFD;")
groups=[("Usuários e acesso",["Administrar usuários","Gerenciar perfis","Ver histórico de usuário"]),
        ("Financeiro",["Manter despesa","Manter receita","Manter conta","Ver dashboard"]),
        ("Open Finance",["Conectar conta (por plano)","Conciliar transações"])]
yy=my+248
for g,items in groups:
    p2.chk(mx+40,yy,g+"  —  marcar todos",on=(g!="Open Finance"))
    yy+=26
    for it in items:
        p2.chk(mx+64,yy,it,on=("por plano" not in it))
        yy+=24
    yy+=8
p2.btn(mx+mw-230,my+700,100,"Cancelar")
p2.btn(mx+mw-120,my+700,96,"Salvar",True)
p2.mk(mx-4,my+4,1); p2.mk(mx+8,my+86,2); p2.mk(mx+24+cw+4,my+86,3); p2.mk(mx+8,my+156,4)
p2.mk(mx+8,my+224,5); p2.mk(mx+24,my+244,6); p2.mk(mx+mw-40,my+206,7)
p2.mk(mx+mw-124,my+692,8); p2.mk(mx+mw-234,my+692,9)

# ---- 3: Catálogo de Permissões (leitura) ----
p3=P("7.3 Catalogo Permissoes"); p3.rect(0,0,W,H,"","fillColor=#EDEFF3;strokeColor=none;")
mw=760; mx=(W-mw)//2; my=70
p3.rect(mx,my,mw,640,"",f"fillColor={SURF};strokeColor={BORD};")
p3.rect(mx,my,mw,50,"","fillColor=#FBFCFD;strokeColor="+BORD+";")
p3.txt(mx+20,my+14,300,20,"Catálogo de Permissões","fontStyle=1;fontSize=14;")
p3.btn(mx+mw-210,my+8,180,"Sincronizar catálogo")
hy=my+66
p3.rect(mx+24,hy,mw-48,36,"","fillColor="+HDRF+";strokeColor="+BORD+";")
for c,x in zip(["CÓDIGO","NOME","MÓDULO","POR PLANO","SITUAÇÃO"],[mx+40,mx+220,mx+430,mx+560,mx+660]):
    p3.txt(x,hy+10,130,16,c,f"fontSize=10;fontColor={MUT};fontStyle=1;")
cat=[("ADMINISTRAR_USUARIOS","Administrar usuários","Usuários","Não","Ativa"),
     ("GERENCIAR_PERFIS","Gerenciar perfis","Usuários","Não","Ativa"),
     ("MANTER_DESPESA","Manter despesa","Financeiro","Não","Ativa"),
     ("CONECTAR_OPEN_FINANCE","Conectar conta","Open Finance","Sim","Ativa"),
     ("RECURSO_ANTIGO","(sem nome no código)","—","—","Órfã")]
for i,r in enumerate(cat):
    ry=hy+36+i*40
    for v,x in zip(r,[mx+40,mx+220,mx+430,mx+560,mx+660]):
        p3.txt(x,ry+12,150,16,v,"fontSize=11;"+("fontColor=#B26B00;" if r[4]=="Órfã" else ""))
p3.btn(mx+mw-110,my+590,86,"Fechar")
p3.mk(mx-4,my+4,1); p3.mk(mx+8,hy+58,2); p3.mk(mx+mw-216,my+0,3); p3.mk(mx+mw-116,my+582,4)

pages=[p1,p2,p3]
xml='<mxfile host="app.diagrams.net">'+"".join(pg.xml() for pg in pages)+'</mxfile>'
M.parseString(xml)
open(os.path.join(OUT,"manter-perfil-permissao-prototipo.drawio"),"w", encoding="utf-8").write(xml)
print("ok —",len(pages),"páginas")
