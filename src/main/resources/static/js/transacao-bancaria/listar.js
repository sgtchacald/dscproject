import { getJson } from '../comum/http.js';
import { semAcento, dataBr, toast } from '../comum/ui.js';
import { formatarMoeda } from '../comum/mascara.js';
import { inicializarFiltro, obterFiltroAtual, abrirModalFiltro } from './modal-filtro.js';
import { inicializarForm, abrirCadastro, abrirEdicao, excluir, abrirDuplicar, EVENTO_ALTERADO } from './modal-form.js';
import { inicializarImportacao, abrirModalImportar } from './modal-importar.js';

const cfg = () => document.getElementById('dadosTelaTransacao').dataset;

let todas = [];
let totaisAtuais = null;
let ordenacao = { col: 'dataLancamento', asc: false };

const corpo = document.getElementById('corpoTabelaTransacoes');
const rodape = document.getElementById('rodapeContagemTransacoes');
const cardsTotais = document.getElementById('cardTotalizadorTransacao');
const elTotalCreditos = document.getElementById('totalTransacaoCreditos');
const elTotalDebitos = document.getElementById('totalTransacaoDebitos');
const elTotalLiquido = document.getElementById('totalTransacaoLiquido');

const pode = {
    inserir: () => !!document.querySelector('[data-perm="inserir"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]'),
    excluir: () => !!document.querySelector('[data-perm="excluir"]'),
    importar: () => !!document.querySelector('[data-perm="importar"]')
};

async function carregar() {
    try {
        const filtro = obterFiltroAtual();
        const params = new URLSearchParams();
        if (filtro.busca) params.append('busca', filtro.busca);
        if (filtro.competenciaInicio) params.append('competenciaInicio', filtro.competenciaInicio);
        if (filtro.competenciaFim) params.append('competenciaFim', filtro.competenciaFim);
        if (filtro.contaId) params.append('contaId', filtro.contaId);
        if (filtro.natureza) params.append('natureza', filtro.natureza);
        if (filtro.categoriaId) params.append('categoriaId', filtro.categoriaId);

        const url = `${cfg().urlDados}?${params.toString()}`;
        const resp = await getJson(url);

        todas = resp.dados || [];
        totaisAtuais = resp.totais || null;

        renderTotais(filtro);
        render();
    } catch (e) {
        console.error('Erro ao carregar transações', e);
        corpo.innerHTML = '<tr><td colspan="10" class="text-center text-danger">Erro ao carregar dados das transações.</td></tr>';
    }
}

function renderTotais(filtro) {
    if (!cardsTotais) return;

    const isCompetenciaUnica = filtro.competenciaInicio &&
        filtro.competenciaFim &&
        filtro.competenciaInicio === filtro.competenciaFim;

    if (isCompetenciaUnica && totaisAtuais) {
        cardsTotais.style.display = 'flex';
        elTotalCreditos.textContent = formatarMoeda(totaisAtuais.totalCreditos || 0);
        elTotalDebitos.textContent = formatarMoeda(totaisAtuais.totalDebitos || 0);

        const saldoLiq = totaisAtuais.saldoLiquido || 0;
        elTotalLiquido.textContent = formatarMoeda(saldoLiq);
        elTotalLiquido.className = 'text-secondary fw-bold ' + (saldoLiq >= 0 ? 'text-success' : 'text-danger');
    } else {
        cardsTotais.style.display = 'none';
    }
}

function labelOrigem(origem) {
    if (origem === 'IMPORTACAO') return cfg().labelOrigemImportacao || 'Importação';
    if (origem === 'OPEN_FINANCE') return cfg().labelOrigemOpenFinance || 'Open Finance';
    return cfg().labelOrigemManual || 'Manual';
}

function badgeOrigem(origem) {
    if (origem === 'IMPORTACAO') return '<span class="badge bg-azure-lt">' + (cfg().labelOrigemImportacao || 'Importação') + '</span>';
    if (origem === 'OPEN_FINANCE') return '<span class="badge bg-purple-lt">' + (cfg().labelOrigemOpenFinance || 'Open Finance') + '</span>';
    return '<span class="badge bg-secondary-lt">' + (cfg().labelOrigemManual || 'Manual') + '</span>';
}

function badgeNatureza(natureza) {
    if (natureza === 'CREDITO') {
        return '<span class="badge bg-success-lt"><i class="ph ph-arrow-up-right me-1"></i>' + (cfg().labelCredito || 'Crédito') + '</span>';
    }
    return '<span class="badge bg-danger-lt"><i class="ph ph-arrow-down-left me-1"></i>' + (cfg().labelDebito || 'Débito') + '</span>';
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let cmp = 0;
        if (col === 'competencia') {
            cmp = (a.competencia || '').localeCompare(b.competencia || '');
        } else if (col === 'dataLancamento') {
            cmp = (a.dataLancamento || '').localeCompare(b.dataLancamento || '');
        } else if (col === 'descricao') {
            cmp = (a.descricao || '').localeCompare(b.descricao || '', 'pt-BR');
        } else if (col === 'conta') {
            cmp = (a.contaDescricao || '').localeCompare(b.contaDescricao || '', 'pt-BR');
        } else if (col === 'categoria') {
            cmp = (a.categoriaNome || '').localeCompare(b.categoriaNome || '', 'pt-BR');
        } else if (col === 'natureza') {
            cmp = (a.natureza || '').localeCompare(b.natureza || '');
        } else if (col === 'valor') {
            cmp = (a.valor != null ? Number(a.valor) : 0) - (b.valor != null ? Number(b.valor) : 0);
        } else if (col === 'fatura') {
            cmp = (a.pagamentoFatura ? 1 : 0) - (b.pagamentoFatura ? 1 : 0);
        } else if (col === 'origem') {
            cmp = (a.origem || '').localeCompare(b.origem || '');
        }
        return asc ? cmp : -cmp;
    });
}

function render() {
    const lista = ordenar(todas);

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="10" class="text-center text-secondary py-4">${cfg().msgFiltroVazio || 'Nenhuma transação encontrada.'}</td></tr>`;
        rodape.textContent = 'Exibindo 0 transações';
        return;
    }

    corpo.innerHTML = lista.map(t => {
        const isCredito = t.natureza === 'CREDITO';
        const corValor = isCredito ? 'text-success' : 'text-danger';
        const sinal = isCredito ? '+ ' : '- ';
        const valorFmt = sinal + formatarMoeda(t.valor);
        const badgeFatura = t.pagamentoFatura
            ? `<span class="badge bg-warning-lt">${cfg().labelPagamentoFatura || 'Pagamento de Fatura'}</span>`
            : '<span class="text-muted">-</span>';

        const bloqueadoExclusao = t.pagamentoFatura || t.origem === 'OPEN_FINANCE';
        const bloqueadoDuplicar = t.pagamentoFatura || t.origem === 'OPEN_FINANCE';

        let btnEditar = '';
        if (pode.editar()) {
            btnEditar = `<button class="btn btn-sm btn-icon btn-ghost-secondary btn-editar" data-id="${t.id}" title="Editar">
                <i class="ph ph-pencil-simple" aria-hidden="true"></i>
            </button>`;
        }

        let btnDuplicar = '';
        if (pode.inserir()) {
            if (bloqueadoDuplicar) {
                btnDuplicar = `<button class="btn btn-sm btn-icon btn-ghost-secondary text-muted" disabled title="Transações geradas por fatura ou Open Finance não podem ser duplicadas">
                    <i class="ph ph-copy" aria-hidden="true"></i>
                </button>`;
            } else {
                btnDuplicar = `<button class="btn btn-sm btn-icon btn-ghost-secondary btn-duplicar" data-id="${t.id}" data-competencia="${t.competencia}" title="Duplicar">
                    <i class="ph ph-copy" aria-hidden="true"></i>
                </button>`;
            }
        }

        let btnExcluir = '';
        if (pode.excluir()) {
            if (bloqueadoExclusao) {
                btnExcluir = `<button class="btn btn-sm btn-icon btn-ghost-secondary text-muted" disabled title="Esta transação está vinculada e não pode ser excluída diretamente">
                    <i class="ph ph-trash" aria-hidden="true"></i>
                </button>`;
            } else {
                btnExcluir = `<button class="btn btn-sm btn-icon btn-ghost-danger btn-excluir" data-id="${t.id}" title="Excluir">
                    <i class="ph ph-trash" aria-hidden="true"></i>
                </button>`;
            }
        }

        return `<tr>
            <td class="text-nowrap">${t.competencia || ''}</td>
            <td class="text-nowrap">${dataBr(t.dataLancamento)}</td>
            <td class="fw-medium">${t.descricao || ''}</td>
            <td class="text-nowrap">${t.contaDescricao || ''}</td>
            <td class="text-nowrap">${t.categoriaNome ? `<span class="badge badge-outline text-secondary">${t.categoriaNome}</span>` : '<span class="text-muted">-</span>'}</td>
            <td class="text-nowrap">${badgeNatureza(t.natureza)}</td>
            <td class="text-end text-nowrap fw-bold ${corValor}">${valorFmt}</td>
            <td class="text-nowrap">${badgeFatura}</td>
            <td class="text-nowrap">${badgeOrigem(t.origem)}</td>
            <td class="text-start text-nowrap col-acoes">
                <div class="d-inline-flex gap-1">
                    ${btnEditar}
                    ${btnDuplicar}
                    ${btnExcluir}
                </div>
            </td>
        </tr>`;
    }).join('');

    rodape.textContent = `Exibindo ${lista.length} transaç${lista.length === 1 ? 'ão' : 'ões'}`;

    // Eventos nas ações do grid
    corpo.querySelectorAll('.btn-editar').forEach(btn => {
        btn.addEventListener('click', () => abrirEdicao(btn.dataset.id));
    });

    corpo.querySelectorAll('.btn-duplicar').forEach(btn => {
        btn.addEventListener('click', () => abrirDuplicar(btn.dataset.id, btn.dataset.competencia));
    });

    corpo.querySelectorAll('.btn-excluir').forEach(btn => {
        btn.addEventListener('click', () => excluir(btn.dataset.id));
    });
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaTransacoes thead th.sortable').forEach(th => {
        th.addEventListener('click', () => {
            const col = th.dataset.col;
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

function inicializarEventos() {
    const btnFiltrar = document.getElementById('btnFiltrar');
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', abrirModalFiltro);
    }

    const btnNovo = document.getElementById('btnNovaTransacao');
    if (btnNovo) {
        btnNovo.addEventListener('click', abrirCadastro);
    }

    const btnImportar = document.getElementById('btnImportarOfx');
    if (btnImportar) {
        btnImportar.addEventListener('click', abrirModalImportar);
    }

    document.addEventListener(EVENTO_ALTERADO, carregar);
}

document.addEventListener('DOMContentLoaded', () => {
    inicializarFiltro(carregar);
    inicializarForm();
    inicializarImportacao(carregar);
    inicializarOrdenacao();
    inicializarEventos();
    carregar();
});
