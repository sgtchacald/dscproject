import { getJson } from '../comum/http.js';
import { semAcento, dataBr } from '../comum/ui.js';
import { abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';
import { abrirHistorico } from './modal-historico.js';
import { abrirAlterarSenha } from './modal-alterar-senha.js';

const cfg = document.getElementById('dadosTela').dataset;
const loginAtual = cfg.loginAtual || '';

const pode = {
    editar: !!document.querySelector('[data-perm="editar"]'),
    excluir: !!document.querySelector('[data-perm="excluir"]'),
    historico: !!document.querySelector('[data-perm="historico"]'),
    alterarSenha: !!document.querySelector('[data-perm="alterar-senha"]')
};

let todos = [];
let ordenacao = { col: 'nome', asc: true };

const corpo = document.getElementById('corpoTabela');
const rodape = document.getElementById('rodapeContagem');

async function carregar() {
    todos = await getJson(cfg.urlDados);
    render();
}

function filtrados() {
    const busca = semAcento(document.getElementById('filtroBusca').value);
    const perfil = document.getElementById('filtroPerfil').value;
    const situacao = document.getElementById('filtroSituacao').value;
    return todos.filter(function (u) {
        if (busca && !(semAcento(u.nome).includes(busca)
            || semAcento(u.login).includes(busca)
            || semAcento(u.email).includes(busca))) return false;
        if (perfil && u.perfilCodigo !== perfil) return false;
        if (situacao === 'ATIVO' && u.excluido) return false;
        if (situacao === 'EXCLUIDO' && !u.excluido) return false;
        return true;
    });
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
    excluir: 'ph-trash',
    historico: 'ph-clock-counter-clockwise',
    'alterar-senha': 'ph-key'
};

function botaoAcao(acao, rotulo, u, classeCor) {
    return '<button type="button" class="btn btn-action' + classeCor + '"'
        + ' data-acao="' + acao + '" data-id="' + u.id + '"'
        + (acao === 'excluir' || acao === 'alterar-senha' ? ' data-nome="' + u.nome + '"' : '')
        + ' title="' + rotulo + '" aria-label="' + rotulo + '">'
        + '<i class="ph ' + ICONES[acao] + '" aria-hidden="true"></i></button> ';
}

function acoes(u) {
    let html = '';
    if (pode.editar && !u.excluido) {
        html += botaoAcao('editar', 'Editar', u, '');
    }
    if (pode.excluir && !u.excluido && u.login !== loginAtual) {
        html += botaoAcao('excluir', 'Excluir', u, ' text-danger');
    }
    if (pode.alterarSenha && !u.excluido) {
        html += botaoAcao('alterar-senha', 'Alterar senha', u, '');
    }
    if (pode.historico) {
        html += botaoAcao('historico', 'Ver histórico', u, '');
    }
    return html;
}

function render() {
    const lista = ordenar(filtrados());
    corpo.innerHTML = '';
    if (lista.length === 0) {
        corpo.innerHTML = '<tr><td colspan="8" class="text-center text-secondary">' + cfg.msgFiltroVazio + '</td></tr>';
    } else {
        lista.forEach(function (u) {
            const tr = document.createElement('tr');
            const situacao = u.excluido
                ? '<span class="badge bg-red text-red-fg">Excluído</span>'
                : '<span class="badge bg-green text-green-fg">Ativo</span>';
            tr.innerHTML = '<td>' + u.nome + '</td><td>' + u.login + '</td><td>' + u.email + '</td>'
                + '<td><span class="badge bg-blue-lt">' + (u.perfilNome || '') + '</span></td>'
                + '<td>' + (u.generoDescricao || '') + '</td>'
                + '<td>' + dataBr(u.criadoEm) + '</td><td>' + situacao + '</td>'
                + '<td class="col-acoes text-start text-nowrap">' + acoes(u) + '</td>';
            corpo.appendChild(tr);
        });
    }
    rodape.textContent = lista.length + ' de ' + todos.length + ' usuário(s)';
}

document.getElementById('btnAplicarFiltro').addEventListener('click', render);
document.getElementById('btnLimparFiltro').addEventListener('click', function () {
    document.getElementById('filtroBusca').value = '';
    document.getElementById('filtroPerfil').value = '';
    document.getElementById('filtroSituacao').value = 'ATIVO';
    render();
});
document.querySelectorAll('#tabelaUsuarios th.sortable').forEach(function (th) {
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
    if (acao === 'alterar-senha') abrirAlterarSenha(id, nome);
    if (acao === 'historico') abrirHistorico(id);
});

document.addEventListener(EVENTO_ALTERADO, carregar);

carregar();
