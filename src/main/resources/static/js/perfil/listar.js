import { getJson, enviar } from '../comum/http.js';
import { toast } from '../comum/ui.js';
import { abrirNovo, abrirEdicao, EVENTO_ALTERADO } from './modal-form.js';
import './modal-catalogo.js';

const cfg = document.getElementById('dadosTelaPerfil').dataset;

const pode = {
    inserir: !!document.querySelector('[data-perm="inserir"]'),
    editar: !!document.querySelector('[data-perm="editar"]'),
    excluir: !!document.querySelector('[data-perm="excluir"]'),
    vincular: !!document.querySelector('[data-perm="vincular"]')
};

let todos = [];
let ordenacao = { col: 'nome', asc: true };

const corpo = document.getElementById('corpoTabelaPerfis');
const rodape = document.getElementById('rodapeContagemPerfis');

async function carregar() {
    todos = await getJson(cfg.urlDados);
    render();
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort(function (a, b) {
        const va = a[col], vb = b[col];
        if (va === vb) return 0;
        return (va > vb ? 1 : -1) * (asc ? 1 : -1);
    });
}

// Ícones Phosphor (phosphoricons.com) — classe do webfont.
const ICONES = {
    editar: 'ph-pencil-simple',
    excluir: 'ph-trash'
};

function botaoAcao(acao, rotulo, p, classeCor) {
    return '<button type="button" class="btn btn-action' + classeCor + '"'
        + ' data-acao="' + acao + '" data-id="' + p.id + '" data-nome="' + p.nome + '"'
        + ' title="' + rotulo + '" aria-label="' + rotulo + '">'
        + '<i class="ph ' + ICONES[acao] + '" aria-hidden="true"></i></button> ';
}

function acoes(p) {
    let html = '';
    if (pode.editar) {
        html += botaoAcao('editar', cfg.labelEditar || 'Editar', p, '');
    }
    if (pode.excluir && !p.sistema && p.qtdUsuarios === 0) {
        html += botaoAcao('excluir', 'Excluir', p, ' text-danger');
    }
    return html;
}

function situacao(p) {
    const badges = [];
    if (p.sistema) {
        badges.push('<span class="badge bg-purple-lt">' + (cfg.labelBadgeSistema || 'Sistema') + '</span>');
    }
    badges.push('<span class="badge bg-green-lt">' + (cfg.labelBadgeAtivo || 'Ativo') + '</span>');
    return badges.join(' ');
}

function render() {
    const lista = ordenar(todos);
    corpo.innerHTML = '';
    if (lista.length === 0) {
        corpo.innerHTML = '<tr><td colspan="6" class="text-center text-secondary">'
            + (cfg.labelListaVazia || '') + '</td></tr>';
    } else {
        lista.forEach(function (p) {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td>' + p.codigo + '</td><td>' + p.nome + '</td>'
                + '<td>' + p.qtdPermissoes + '</td><td>' + p.qtdUsuarios + '</td>'
                + '<td>' + situacao(p) + '</td>'
                + '<td class="col-acoes text-start text-nowrap">' + acoes(p) + '</td>';
            corpo.appendChild(tr);
        });
    }
    rodape.textContent = lista.length + ' perfil(is)';
}

document.querySelectorAll('#tabelaPerfis th.sortable').forEach(function (th) {
    th.addEventListener('click', function () {
        const col = th.dataset.col;
        ordenacao = { col: col, asc: ordenacao.col === col ? !ordenacao.asc : true };
        render();
    });
});

corpo.addEventListener('click', function (e) {
    const btn = e.target.closest('button[data-acao]');
    if (!btn) return;
    const { acao, id, nome } = btn.dataset;
    if (acao === 'editar') abrirEdicao(id);
    if (acao === 'excluir') excluir(id, nome);
});

async function excluir(id, nome) {
    if (!window.confirm((cfg.msgConfirmaExclusao || 'Excluir "{0}"?').replace('{0}', nome))) return;
    try {
        const data = await enviar(cfg.urlExcluir + '/' + id, 'DELETE');
        if (data.sucesso) {
            toast(data.mensagem || 'OK');
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            toast(data.mensagem || 'Erro', true);
        }
    } catch (err) {
        toast(cfg.erroComunicacao, true);
    }
}

const btnNovo = document.getElementById('btnNovoPerfil');
if (btnNovo) btnNovo.addEventListener('click', abrirNovo);

document.addEventListener(EVENTO_ALTERADO, carregar);

carregar();
