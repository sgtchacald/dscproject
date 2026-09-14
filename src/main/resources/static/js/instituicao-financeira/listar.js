import { getJson } from '../comum/http.js';
import { semAcento } from '../comum/ui.js';
import { inicializarFiltro, obterFiltroAtual, abrirModalFiltro } from './modal-filtro.js';
import { inicializarForm, abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';

const cfg = () => document.getElementById('dadosTelaInstituicao').dataset;

let todas = [];
let ordenacao = { col: 'nome', asc: true };

const corpo = document.getElementById('corpoTabelaInstituicoes');
const rodape = document.getElementById('rodapeContagemInstituicoes');

const pode = {
    inserir: () => !!document.querySelector('[data-perm="inserir"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]'),
    excluir: () => !!document.querySelector('[data-perm="excluir"]')
};

async function carregar() {
    try {
        todas = await getJson(cfg().urlDados);
        render();
    } catch (e) {
        corpo.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Erro ao carregar instituições financeiras.</td></tr>';
    }
}

function filtrar(lista) {
    const f = obterFiltroAtual();
    const buscaNorm = semAcento(f.busca);

    return lista.filter(i => {
        if (buscaNorm) {
            const nomeNorm = semAcento(i.nome);
            const codNorm = semAcento(i.codigo);
            if (!nomeNorm.includes(buscaNorm) && !codNorm.includes(buscaNorm)) {
                return false;
            }
        }

        if (f.tipo) {
            const tipoCodigo = i.tipoCodigo || (i.tipo && i.tipo.codigo) || i.tipo;
            if (tipoCodigo !== f.tipo && i.tipo !== f.tipo) {
                return false;
            }
        }

        if (f.situacao === 'ATIVA' && (!i.ativo || i.excluido)) return false;
        if (f.situacao === 'INATIVA' && (i.ativo || i.excluido)) return false;
        if (f.situacao === 'EXCLUIDA' && !i.excluido) return false;

        return true;
    });
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let va = a[col];
        let vb = b[col];

        if (col === 'tipo') {
            va = a.tipoDescricao || a.tipo || '';
            vb = b.tipoDescricao || b.tipo || '';
        } else if (col === 'situacao') {
            va = a.excluido ? 2 : (a.ativo ? 0 : 1);
            vb = b.excluido ? 2 : (b.ativo ? 0 : 1);
        }

        if (va == null) va = '';
        if (vb == null) vb = '';

        if (typeof va === 'string') {
            const res = va.localeCompare(vb, 'pt-BR');
            return asc ? res : -res;
        }

        if (va === vb) return 0;
        return (va > vb ? 1 : -1) * (asc ? 1 : -1);
    });
}

function render() {
    const filtradas = filtrar(todas);
    const lista = ordenar(filtradas);
    corpo.innerHTML = '';

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="7" class="text-center text-secondary">${cfg().msgFiltroVazio}</td></tr>`;
    } else {
        lista.forEach(i => {
            const tr = document.createElement('tr');

            const seloSistema = i.sistema ? ` <span class="badge bg-purple-lt ms-1">${cfg().labelSistema || 'Sistema'}</span>` : '';
            const nomeHtml = `<strong>${i.nome}</strong>${seloSistema}`;
            const codigoHtml = i.codigo ? `<code>${i.codigo}</code>` : '<span class="text-muted">—</span>';

            const tipoBadgeClass = (i.tipo === 'BANCO' || (i.tipo && i.tipo.codigo === 'B') || i.tipoCodigo === 'B') ? 'bg-blue-lt' : 'bg-azure-lt';
            const tipoDesc = i.tipoDescricao || (i.tipo === 'BANCO' ? 'Banco' : 'Corretora');
            const tipoHtml = `<span class="badge ${tipoBadgeClass}">${tipoDesc}</span>`;

            let situacaoHtml = '';
            if (i.excluido) {
                situacaoHtml = `<span class="badge bg-red text-red-fg">${cfg().labelExcluida || 'Excluída'}</span>`;
            } else if (i.ativo) {
                situacaoHtml = `<span class="badge bg-green text-green-fg">${cfg().labelAtiva || 'Ativa'}</span>`;
            } else {
                situacaoHtml = `<span class="badge bg-secondary text-secondary-fg">${cfg().labelInativa || 'Inativa'}</span>`;
            }

            let acaoHtml = '';
            if (pode.editar()) {
                acaoHtml += `<button type="button" class="btn btn-action" data-acao="editar" data-id="${i.id}" title="Editar instituição">
                    <i class="ph ph-pencil-simple" aria-hidden="true"></i>
                </button> `;
            }
            if (pode.excluir() && !i.sistema && !i.excluido) {
                acaoHtml += `<button type="button" class="btn btn-action text-danger" data-acao="excluir" data-id="${i.id}" data-nome="${i.nome}" title="Excluir instituição">
                    <i class="ph ph-trash" aria-hidden="true"></i>
                </button>`;
            }

            tr.innerHTML = `
                <td>${nomeHtml}</td>
                <td>${codigoHtml}</td>
                <td>${tipoHtml}</td>
                <td class="text-end">${i.qtdUso}</td>
                <td class="text-end">${i.qtdProvedores}</td>
                <td>${situacaoHtml}</td>
                <td class="col-acoes text-start"><div class="d-flex gap-1 justify-content-start">${acaoHtml}</div></td>
            `;

            corpo.appendChild(tr);
        });
    }

    rodape.textContent = `Mostrando ${lista.length} de ${todas.length} instituições`;
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaInstituicoes thead th.sortable').forEach(th => {
        th.addEventListener('click', function () {
            const col = this.dataset.col;
            if (ordenacao.col === col) {
                ordenacao.asc = !ordenacao.asc;
            } else {
                ordenacao.col = col;
                ordenacao.asc = true;
            }
            render();
        });
    });
}

function inicializarAcoes() {
    corpo.addEventListener('click', function (e) {
        const btn = e.target.closest('button[data-acao]');
        if (!btn) return;
        const acao = btn.dataset.acao;
        const id = btn.dataset.id;
        const nome = btn.dataset.nome;

        if (acao === 'editar') {
            abrirEdicao(id);
        } else if (acao === 'excluir') {
            excluir(id, nome);
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    inicializarForm();
    inicializarFiltro(() => render());
    inicializarOrdenacao();
    inicializarAcoes();

    const btnFiltrar = document.getElementById('btnFiltrar');
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', function (e) {
            e.preventDefault();
            abrirModalFiltro();
        });
    }

    document.addEventListener(EVENTO_ALTERADO, carregar);

    carregar();
});
