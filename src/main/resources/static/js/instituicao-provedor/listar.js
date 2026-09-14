import { getJson } from '../comum/http.js';
import { inicializarForm, abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';

const cfg = () => document.getElementById('dadosTelaVinculo').dataset;

let todos = [];
let ordenacao = { col: 'provedorNome', asc: true };

const corpo = document.getElementById('corpoTabelaVinculos');
const rodape = document.getElementById('rodapeContagemVinculos');
const filtroProvedor = document.getElementById('filtroProvedor');

const pode = {
    inserir: () => !!document.querySelector('[data-perm="inserir"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]'),
    excluir: () => !!document.querySelector('[data-perm="excluir"]')
};

async function carregar() {
    try {
        todos = await getJson(cfg().urlDados);
        render();
    } catch (e) {
        corpo.innerHTML = '<tr><td colspan="4" class="text-center text-danger">Erro ao carregar vínculos.</td></tr>';
    }
}

function filtrar(lista) {
    const provId = filtroProvedor ? filtroProvedor.value : '';
    if (!provId) {
        return lista;
    }
    return lista.filter(v => String(v.provedorId) === String(provId));
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let va = a[col] || '';
        let vb = b[col] || '';

        const res = String(va).localeCompare(String(vb), 'pt-BR');
        if (res !== 0) {
            return asc ? res : -res;
        }

        // Critério secundário: se ordenado por provedor, desempata por instituição
        if (col === 'provedorNome') {
            return String(a.instituicaoNome || '').localeCompare(String(b.instituicaoNome || ''), 'pt-BR');
        }
        return 0;
    });
}

function render() {
    const filtrados = filtrar(todos);
    const lista = ordenar(filtrados);
    corpo.innerHTML = '';

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="4" class="text-center text-secondary">${cfg().msgFiltroVazio || 'Nenhum vínculo encontrado.'}</td></tr>`;
    } else {
        lista.forEach(v => {
            const tr = document.createElement('tr');

            let acaoHtml = '';
            if (pode.editar()) {
                acaoHtml += `
                    <button type="button" class="btn btn-action" data-acao="editar" data-id="${v.id}" title="Editar vínculo">
                        <i class="ph ph-pencil-simple" aria-hidden="true"></i>
                    </button>
                `;
            }
            if (pode.excluir()) {
                acaoHtml += `
                    <button type="button" class="btn btn-action text-danger" data-acao="excluir" data-id="${v.id}" data-inst="${v.instituicaoNome}" data-prov="${v.provedorNome}" title="Excluir vínculo">
                        <i class="ph ph-trash" aria-hidden="true"></i>
                    </button>
                `;
            }

            tr.innerHTML = `
                <td><strong>${v.provedorNome}</strong></td>
                <td>${v.instituicaoNome}</td>
                <td><code>${v.idExterno}</code></td>
                <td class="col-acoes text-start"><div class="d-flex gap-1 justify-content-start">${acaoHtml}</div></td>
            `;

            corpo.appendChild(tr);
        });
    }

    rodape.textContent = `Mostrando ${lista.length} de ${todos.length} vínculos`;
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaVinculos thead th.sortable').forEach(th => {
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

        if (acao === 'editar') {
            abrirEdicao(id);
        } else if (acao === 'excluir') {
            const inst = btn.dataset.inst;
            const prov = btn.dataset.prov;
            excluir(id, inst, prov);
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    inicializarForm();
    inicializarOrdenacao();
    inicializarAcoes();

    if (filtroProvedor) {
        filtroProvedor.addEventListener('change', render);
    }

    document.addEventListener(EVENTO_ALTERADO, carregar);

    carregar();
});
