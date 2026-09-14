import { getJson } from '../comum/http.js';
import { semAcento } from '../comum/ui.js';
import { inicializarFiltro, obterFiltroAtual, abrirModalFiltro } from './modal-filtro.js';
import { inicializarForm, abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';

const cfg = () => document.getElementById('dadosTelaCartao').dataset;

let todos = [];
let ordenacao = { col: 'descricao', asc: true };

const corpo = document.getElementById('corpoTabelaCartoes');
const rodape = document.getElementById('rodapeContagemCartoes');

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
        corpo.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Erro ao carregar cartões.</td></tr>';
    }
}

function filtrar(lista) {
    const f = obterFiltroAtual();
    const buscaNorm = semAcento(f.busca);

    return lista.filter(c => {
        if (buscaNorm) {
            const descNorm = semAcento(c.descricao);
            const finalNorm = semAcento(c.finalCartao);
            if (!descNorm.includes(buscaNorm) && !finalNorm.includes(buscaNorm)) {
                return false;
            }
        }

        if (f.bandeira && c.bandeira !== f.bandeira) {
            return false;
        }

        if (f.situacao === 'ATIVO' && (!c.ativo || c.excluido)) return false;
        if (f.situacao === 'INATIVO' && (c.ativo || c.excluido)) return false;
        if (f.situacao === 'EXCLUIDO' && !c.excluido) return false;

        return true;
    });
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let va = a[col];
        let vb = b[col];

        if (col === 'limite') {
            va = a.limite != null ? Number(a.limite) : -1;
            vb = b.limite != null ? Number(b.limite) : -1;
            return asc ? va - vb : vb - va;
        } else if (col === 'contaDebito') {
            va = a.contaDescricao || '';
            vb = b.contaDescricao || '';
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

function acaoHtml(c) {
    let html = '';
    if (pode.editar()) {
        html += `<button type="button" class="btn btn-action" data-acao="editar" data-id="${c.id}" title="Editar cartão" aria-label="Editar cartão">
            <i class="ph ph-pencil-simple" aria-hidden="true"></i>
        </button>`;
    }
    if (pode.excluir() && !c.excluido) {
        html += `<button type="button" class="btn btn-action text-danger" data-acao="excluir" data-id="${c.id}" data-descricao="${c.descricao}" title="Excluir cartão" aria-label="Excluir cartão">
            <i class="ph ph-trash" aria-hidden="true"></i>
        </button>`;
    }
    return html;
}

function formatarMoeda(valor) {
    if (valor == null) return '';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(valor));
}

function render() {
    const filtradas = filtrar(todos);
    const lista = ordenar(filtradas);
    corpo.innerHTML = '';

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="8" class="text-center text-secondary">${cfg().msgFiltroVazio}</td></tr>`;
    } else {
        lista.forEach(c => {
            const tr = document.createElement('tr');

            const descricaoHtml = `<strong>${c.descricao}</strong>`;
            const bandeiraHtml = c.bandeira ? `<span class="badge bg-blue-lt">${c.bandeira}</span>` : '';
            const finalHtml = c.finalCartao ? `•••• ${c.finalCartao}` : '';
            const limiteHtml = formatarMoeda(c.limite);

            let fechVencHtml = '—';
            if (c.diaFechamento != null || c.diaVencimento != null) {
                const fech = c.diaFechamento != null ? `Dia ${c.diaFechamento}` : '—';
                const venc = c.diaVencimento != null ? `Dia ${c.diaVencimento}` : '—';
                fechVencHtml = `${fech} / ${venc}`;
            }

            const contaHtml = c.contaDescricao || (cfg().labelContaNaoDefinida || 'Não definida');

            let situacaoHtml = '';
            if (c.excluido) {
                situacaoHtml = `<span class="badge bg-red text-red-fg">${cfg().labelExcluido || 'Excluído'}</span>`;
            } else if (c.ativo) {
                situacaoHtml = `<span class="badge bg-green text-green-fg">${cfg().labelAtivo || 'Ativo'}</span>`;
            } else {
                situacaoHtml = `<span class="badge bg-secondary text-secondary-fg">${cfg().labelInativo || 'Inativo'}</span>`;
            }

            tr.innerHTML = `
                <td>${descricaoHtml}</td>
                <td>${bandeiraHtml}</td>
                <td>${finalHtml}</td>
                <td class="text-end">${limiteHtml}</td>
                <td>${fechVencHtml}</td>
                <td>${contaHtml}</td>
                <td>${situacaoHtml}</td>
                <td class="col-acoes text-start"><div class="d-flex gap-1 justify-content-start">${acaoHtml(c)}</div></td>
            `;

            corpo.appendChild(tr);
        });
    }

    rodape.textContent = `Mostrando ${lista.length} de ${todos.length} cartões`;
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaCartoes thead th.sortable').forEach(th => {
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
        const descricao = btn.dataset.descricao;

        if (acao === 'editar') {
            abrirEdicao(id);
        } else if (acao === 'excluir') {
            excluir(id, descricao);
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
