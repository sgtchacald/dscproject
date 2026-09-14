import { getJson } from '../comum/http.js';
import { semAcento } from '../comum/ui.js';
import { inicializarFiltro, obterFiltroAtual, abrirModalFiltro } from './modal-filtro.js';
import { inicializarForm, abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';
import { inicializarAjusteSaldo, abrirAjusteSaldo } from './modal-ajuste-saldo.js';

const cfg = () => document.getElementById('dadosTelaConta').dataset;

let todas = [];
let ordenacao = { col: 'descricao', asc: true };

const corpo = document.getElementById('corpoTabelaContas');
const rodape = document.getElementById('rodapeContagemContas');

const pode = {
    inserir: () => !!document.querySelector('[data-perm="inserir"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]'),
    excluir: () => !!document.querySelector('[data-perm="excluir"]'),
    ajustarSaldo: () => !!document.querySelector('[data-perm="ajustar-saldo"]')
};

async function carregar() {
    try {
        todas = await getJson(cfg().urlDados);
        render();
    } catch (e) {
        corpo.innerHTML = '<tr><td colspan="9" class="text-center text-danger">Erro ao carregar contas.</td></tr>';
    }
}

function filtrar(lista) {
    const f = obterFiltroAtual();
    const buscaNorm = semAcento(f.busca);

    return lista.filter(c => {
        if (buscaNorm) {
            const descNorm = semAcento(c.descricao);
            const agNorm = semAcento(c.agencia);
            const numNorm = semAcento(c.numero);
            if (!descNorm.includes(buscaNorm) && !agNorm.includes(buscaNorm) && !numNorm.includes(buscaNorm)) {
                return false;
            }
        }

        if (f.tipo) {
            const tipoCodigo = c.tipoCodigo || (c.tipo && c.tipo.codigo) || c.tipo;
            if (tipoCodigo !== f.tipo && c.tipo !== f.tipo) {
                return false;
            }
        }

        if (f.instituicaoId && String(c.instituicaoId) !== String(f.instituicaoId)) {
            return false;
        }

        if (f.situacao === 'ATIVA' && (!c.ativo || c.excluido)) return false;
        if (f.situacao === 'INATIVA' && (c.ativo || c.excluido)) return false;
        if (f.situacao === 'EXCLUIDA' && !c.excluido) return false;

        return true;
    });
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let va = a[col];
        let vb = b[col];

        if (col === 'instituicao') {
            va = a.instituicaoNome || '';
            vb = b.instituicaoNome || '';
        } else if (col === 'tipo') {
            va = a.tipoDescricao || a.tipo || '';
            vb = b.tipoDescricao || b.tipo || '';
        } else if (col === 'agenciaConta') {
            va = `${a.agencia || ''} ${a.numero || ''}`.trim();
            vb = `${b.agencia || ''} ${b.numero || ''}`.trim();
        } else if (col === 'saldo') {
            va = a.saldo != null ? Number(a.saldo) : 0;
            vb = b.saldo != null ? Number(b.saldo) : 0;
            return asc ? va - vb : vb - va;
        } else if (col === 'consideraSaldo') {
            va = a.consideraSaldo ? 1 : 0;
            vb = b.consideraSaldo ? 1 : 0;
        } else if (col === 'qtdUso') {
            va = a.qtdUso != null ? Number(a.qtdUso) : 0;
            vb = b.qtdUso != null ? Number(b.qtdUso) : 0;
            return asc ? va - vb : vb - va;
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

function formatarMoeda(valor, moeda = 'BRL') {
    const num = Number(valor != null ? valor : 0);
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: moeda || 'BRL' }).format(num);
}

function render() {
    const filtradas = filtrar(todas);
    const lista = ordenar(filtradas);
    corpo.innerHTML = '';

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="9" class="text-center text-secondary">${cfg().msgFiltroVazio}</td></tr>`;
    } else {
        lista.forEach(c => {
            const tr = document.createElement('tr');

            const descricaoHtml = `<strong>${c.descricao}</strong>`;
            const instituicaoHtml = c.instituicaoNome || '<span class="text-muted">—</span>';
            const tipoHtml = `<span class="badge bg-blue-lt">${c.tipoDescricao || c.tipo}</span>`;

            let agenciaConta = '';
            if (c.agencia && c.numero) {
                agenciaConta = `Ag. ${c.agencia} / Cc. ${c.numero}`;
            } else if (c.agencia) {
                agenciaConta = `Ag. ${c.agencia}`;
            } else if (c.numero) {
                agenciaConta = `Cc. ${c.numero}`;
            } else {
                agenciaConta = '<span class="text-muted">—</span>';
            }

            const saldoNum = Number(c.saldo != null ? c.saldo : 0);
            const saldoClasse = saldoNum < 0 ? 'text-danger fw-bold' : (saldoNum > 0 ? 'text-success fw-bold' : 'fw-bold');
            const saldoHtml = `<span class="${saldoClasse}">${formatarMoeda(saldoNum, c.moeda)}</span>`;

            const consideraSaldoHtml = c.consideraSaldo
                ? `<span class="badge bg-green-lt">${cfg().labelSim || 'Sim'}</span>`
                : `<span class="badge bg-secondary-lt">${cfg().labelNao || 'Não'}</span>`;

            const qtdUsoHtml = `<span class="badge bg-light text-secondary">${c.qtdUso != null ? c.qtdUso : 0}</span>`;

            let situacaoHtml = '';
            if (c.excluido) {
                situacaoHtml = `<span class="badge bg-red text-red-fg">${cfg().labelExcluida || 'Excluída'}</span>`;
            } else if (c.ativo) {
                situacaoHtml = `<span class="badge bg-green text-green-fg">${cfg().labelAtiva || 'Ativa'}</span>`;
            } else {
                situacaoHtml = `<span class="badge bg-secondary text-secondary-fg">${cfg().labelInativa || 'Inativa'}</span>`;
            }

            let acaoHtml = '';
            if (pode.editar()) {
                acaoHtml += `<button type="button" class="btn btn-action" data-acao="editar" data-id="${c.id}" title="Editar conta" aria-label="Editar conta">
                    <i class="ph ph-pencil-simple" aria-hidden="true"></i>
                </button> `;
            }
            if (!c.excluido) {
                if (pode.ajustarSaldo()) {
                    acaoHtml += `<button type="button" class="btn btn-action text-warning" data-acao="ajustar-saldo" data-id="${c.id}" data-saldo="${c.saldo}" data-moeda="${c.moeda}" title="Ajustar saldo" aria-label="Ajustar saldo">
                        <i class="ph ph-currency-dollar" aria-hidden="true"></i>
                    </button> `;
                }
                if (pode.excluir()) {
                    acaoHtml += `<button type="button" class="btn btn-action text-danger" data-acao="excluir" data-id="${c.id}" data-descricao="${c.descricao}" title="Excluir conta" aria-label="Excluir conta">
                        <i class="ph ph-trash" aria-hidden="true"></i>
                    </button>`;
                }
            }

            tr.innerHTML = `
                <td>${descricaoHtml}</td>
                <td>${instituicaoHtml}</td>
                <td>${tipoHtml}</td>
                <td>${agenciaConta}</td>
                <td class="text-end">${saldoHtml}</td>
                <td class="text-center">${consideraSaldoHtml}</td>
                <td class="text-end">${qtdUsoHtml}</td>
                <td>${situacaoHtml}</td>
                <td class="col-acoes text-start"><div class="d-flex gap-1 justify-content-start">${acaoHtml}</div></td>
            `;

            corpo.appendChild(tr);
        });
    }

    rodape.textContent = `Mostrando ${lista.length} de ${todas.length} contas`;
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaContas thead th.sortable').forEach(th => {
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
        const saldo = btn.dataset.saldo;
        const moeda = btn.dataset.moeda;

        if (acao === 'editar') {
            abrirEdicao(id);
        } else if (acao === 'ajustar-saldo') {
            abrirAjusteSaldo(id, saldo, moeda);
        } else if (acao === 'excluir') {
            excluir(id, descricao);
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    inicializarForm();
    inicializarAjusteSaldo();
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
