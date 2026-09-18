import { getJson, enviar } from '../comum/http.js';
import { dataBr, toast, abrirModal, fecharModal } from '../comum/ui.js';
import { formatarMoeda, formatarElemento, definirValorMoeda, parseDecimal } from '../comum/mascara.js';

const cfg = () => document.getElementById('dadosTelaFatura').dataset;

let todasFaturas = [];
let ordenacao = { col: 'competencia', asc: true };

const corpo = document.getElementById('corpoTabelaFaturas');
const rodape = document.getElementById('rodapeContagemFaturas');
const selectCartao = document.getElementById('selectCartaoFatura');
const buscaCompetencia = document.getElementById('buscaCompetenciaFatura');
const btnLimparCompetencia = document.getElementById('btnLimparBuscaCompetencia');

const pode = {
    listar: () => !!document.querySelector('[data-perm="listar"]'),
    fechar: () => !!document.querySelector('[data-perm="fechar"]'),
    reabrir: () => !!document.querySelector('[data-perm="reabrir"]'),
    pagar: () => !!document.querySelector('[data-perm="pagar"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]')
};

async function carregar() {
    const cartaoId = selectCartao ? selectCartao.value : null;
    if (!cartaoId) {
        corpo.innerHTML = `<tr><td colspan="9" class="text-center text-secondary py-4">Selecione um cartão de crédito para visualizar as faturas.</td></tr>`;
        rodape.textContent = 'Nenhum cartão selecionado';
        return;
    }

    try {
        const url = `${cfg().urlDados}?cartaoId=${cartaoId}`;
        todasFaturas = await getJson(url);
        render();
    } catch (e) {
        console.error('Erro ao carregar faturas', e);
        corpo.innerHTML = `<tr><td colspan="9" class="text-center text-danger py-4">Erro ao carregar dados das faturas.</td></tr>`;
    }
}

function badgeStatus(status) {
    if (status === 'PAGA') {
        return '<span class="badge bg-success-lt"><i class="ph ph-check-circle me-1"></i>' + (cfg().labelPaga || 'Paga') + '</span>';
    }
    if (status === 'PAGA_PARCIAL') {
        return '<span class="badge bg-azure-lt"><i class="ph ph-clock me-1"></i>' + (cfg().labelPagaParcial || 'Paga Parcial') + '</span>';
    }
    if (status === 'FECHADA') {
        return '<span class="badge bg-warning-lt"><i class="ph ph-lock-simple me-1"></i>' + (cfg().labelFechada || 'Fechada') + '</span>';
    }
    return '<span class="badge bg-secondary-lt"><i class="ph ph-lock-simple-open me-1"></i>' + (cfg().labelAberta || 'Aberta') + '</span>';
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let cmp = 0;
        if (col === 'competencia') {
            cmp = (a.competencia || '').localeCompare(b.competencia || '');
        } else if (col === 'vencimento') {
            cmp = (a.dataVencimento || '').localeCompare(b.dataVencimento || '');
        } else if (col === 'fechamento') {
            cmp = (a.dataFechamento || '').localeCompare(b.dataFechamento || '');
        } else if (col === 'totalCompras') {
            cmp = (a.totalCompras != null ? Number(a.totalCompras) : 0) - (b.totalCompras != null ? Number(b.totalCompras) : 0);
        } else if (col === 'valorEncargos') {
            cmp = (a.valorEncargos != null ? Number(a.valorEncargos) : 0) - (b.valorEncargos != null ? Number(b.valorEncargos) : 0);
        } else if (col === 'valorTotal') {
            cmp = (a.valorTotal != null ? Number(a.valorTotal) : 0) - (b.valorTotal != null ? Number(b.valorTotal) : 0);
        } else if (col === 'valorPago') {
            cmp = (a.valorPago != null ? Number(a.valorPago) : 0) - (b.valorPago != null ? Number(b.valorPago) : 0);
        } else if (col === 'status') {
            cmp = (a.status || '').localeCompare(b.status || '');
        }
        return asc ? cmp : -cmp;
    });
}

function render() {
    let lista = todasFaturas;
    const filtroComp = buscaCompetencia ? buscaCompetencia.value.trim() : '';
    if (filtroComp) {
        lista = lista.filter(f => (f.competencia || '').includes(filtroComp));
    }
    lista = ordenar(lista);
    atualizarClassesCabecalho();

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="9" class="text-center text-secondary py-4">${cfg().msgFiltroVazio || 'Nenhuma fatura encontrada.'}</td></tr>`;
        rodape.textContent = 'Exibindo 0 faturas';
        return;
    }

    corpo.innerHTML = lista.map(f => {
        const isAberta = f.status === 'ABERTA';
        const isFechada = f.status === 'FECHADA';
        const isPaga = f.status === 'PAGA';
        const isPagaParcial = f.status === 'PAGA_PARCIAL';
        const semPagamento = !f.valorPago || Number(f.valorPago) === 0;

        let btnCompras = `<button class="btn btn-sm btn-icon btn-ghost-secondary btn-ver-compras" data-id="${f.id}" title="Ver compras da fatura">
            <i class="ph ph-eye" aria-hidden="true"></i>
        </button>`;

        let btnFechar = '';
        if (pode.fechar() && isAberta) {
            btnFechar = `<button class="btn btn-sm btn-icon btn-ghost-primary btn-fechar-fatura" data-id="${f.id}" title="Fechar fatura">
                <i class="ph ph-lock-simple" aria-hidden="true"></i>
            </button>`;
        }

        let btnPagar = '';
        if (pode.pagar() && (isFechada || isPagaParcial)) {
            btnPagar = `<button class="btn btn-sm btn-icon btn-ghost-success btn-pagar-fatura" data-id="${f.id}" title="Registrar pagamento">
                <i class="ph ph-credit-card" aria-hidden="true"></i>
            </button>`;
        }

        let btnReabrir = '';
        if (pode.reabrir() && isFechada && semPagamento) {
            btnReabrir = `<button class="btn btn-sm btn-icon btn-ghost-warning btn-reabrir-fatura" data-id="${f.id}" data-comp="${f.competencia}" title="Reabrir fatura">
                <i class="ph ph-lock-simple-open" aria-hidden="true"></i>
            </button>`;
        }

        let btnEncargos = '';
        if (pode.editar() && isFechada && semPagamento) {
            btnEncargos = `<button class="btn btn-sm btn-icon btn-ghost-secondary btn-editar-encargos" data-id="${f.id}" title="Ajustar encargos">
                <i class="ph ph-sliders-horizontal" aria-hidden="true"></i>
            </button>`;
        }

        return `<tr>
            <td class="text-nowrap fw-medium">${f.competencia || ''}</td>
            <td class="text-nowrap">${dataBr(f.dataVencimento)}</td>
            <td class="text-nowrap">${f.dataFechamento ? dataBr(f.dataFechamento) : '<span class="text-muted">-</span>'}</td>
            <td class="text-end text-nowrap">${formatarMoeda(f.totalCompras)}</td>
            <td class="text-end text-nowrap text-secondary">${formatarMoeda(f.valorEncargos)}</td>
            <td class="text-end text-nowrap fw-bold text-primary">${formatarMoeda(f.valorTotal)}</td>
            <td class="text-end text-nowrap ${Number(f.valorPago) > 0 ? 'text-success fw-medium' : 'text-muted'}">${formatarMoeda(f.valorPago)}</td>
            <td class="text-nowrap">${badgeStatus(f.status)}</td>
            <td class="text-start text-nowrap col-acoes">
                <div class="d-inline-flex gap-1">
                    ${btnCompras}
                    ${btnFechar}
                    ${btnPagar}
                    ${btnEncargos}
                    ${btnReabrir}
                </div>
            </td>
        </tr>`;
    }).join('');

    rodape.textContent = `Exibindo ${lista.length} fatura${lista.length === 1 ? '' : 's'}`;

    // Eventos de ação
    corpo.querySelectorAll('.btn-ver-compras').forEach(btn => {
        btn.addEventListener('click', () => abrirModalCompras(btn.dataset.id));
    });

    corpo.querySelectorAll('.btn-fechar-fatura').forEach(btn => {
        btn.addEventListener('click', () => abrirModalFechar(btn.dataset.id));
    });

    corpo.querySelectorAll('.btn-pagar-fatura').forEach(btn => {
        btn.addEventListener('click', () => abrirModalPagar(btn.dataset.id));
    });

    corpo.querySelectorAll('.btn-reabrir-fatura').forEach(btn => {
        btn.addEventListener('click', () => acaoReabrir(btn.dataset.id, btn.dataset.comp));
    });

    corpo.querySelectorAll('.btn-editar-encargos').forEach(btn => {
        btn.addEventListener('click', () => abrirModalEncargos(btn.dataset.id));
    });
}

// -------------------------------------------------------------
// Modal: Detalhes das Compras
// -------------------------------------------------------------
async function abrirModalCompras(faturaId) {
    const corpoCompras = document.getElementById('corpoTabelaCompras');
    const totalComprasModal = document.getElementById('totalComprasModal');
    corpoCompras.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-3">Carregando compras...</td></tr>';
    abrirModal('modalComprasFatura');

    try {
        const url = `${cfg().urlBase}/${faturaId}/compras`;
        const itens = await getJson(url);

        if (!itens || itens.length === 0) {
            corpoCompras.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-3">Nenhuma despesa vinculada a esta fatura.</td></tr>';
            totalComprasModal.textContent = formatarMoeda(0);
            return;
        }

        let total = 0;
        corpoCompras.innerHTML = itens.map(item => {
            const val = Number(item.valor || 0);
            total += val;
            return `<tr>
                <td class="text-nowrap">${dataBr(item.data)}</td>
                <td class="fw-medium">${item.descricao || ''}</td>
                <td><span class="badge badge-outline text-secondary">${item.categoriaNome || 'Sem categoria'}</span></td>
                <td class="text-nowrap">${item.parcela || '-'}</td>
                <td class="text-end text-nowrap fw-bold">${formatarMoeda(val)}</td>
            </tr>`;
        }).join('');

        totalComprasModal.textContent = formatarMoeda(total);
    } catch (e) {
        console.error('Erro ao buscar compras da fatura', e);
        corpoCompras.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-3">Erro ao buscar compras.</td></tr>';
    }
}

// -------------------------------------------------------------
// Modal: Fechar Fatura
// -------------------------------------------------------------
function abrirModalFechar(faturaId) {
    const fatura = todasFaturas.find(f => String(f.id) === String(faturaId));
    if (!fatura) return;

    document.getElementById('formFecharFatura')?.reset();
    document.getElementById('fecharFaturaId').value = fatura.id;

    const alerta = document.getElementById('alertaFormFechar');
    if (alerta) {
        alerta.textContent = '';
        alerta.style.display = 'none';
    }

    const hoje = new Date().toISOString().slice(0, 10);
    document.getElementById('fecharDataFechamento').value = hoje;
    document.getElementById('fecharDataVencimento').value = fatura.dataVencimento || hoje;

    const comprasNum = Number(fatura.totalCompras || 0);
    document.getElementById('fecharValorCompras').value = formatarMoeda(comprasNum);
    definirValorMoeda(document.getElementById('fecharValorEncargos'), 0, false);
    document.getElementById('fecharValorTotal').value = formatarMoeda(comprasNum);
    definirValorMoeda(document.getElementById('fecharValorMinimo'), 0, false);

    const inputEncargos = document.getElementById('fecharValorEncargos');
    inputEncargos.oninput = () => {
        formatarElemento(inputEncargos, false);
        const encargos = parseDecimal(inputEncargos.value);
        const total = comprasNum + encargos;
        document.getElementById('fecharValorTotal').value = formatarMoeda(total);
    };

    abrirModal('modalFecharFatura');
}

// -------------------------------------------------------------
// Modal: Registrar Pagamento
// -------------------------------------------------------------
function abrirModalPagar(faturaId) {
    const fatura = todasFaturas.find(f => String(f.id) === String(faturaId));
    if (!fatura) return;

    document.getElementById('formPagarFatura')?.reset();
    document.getElementById('pagarFaturaId').value = fatura.id;

    const alerta = document.getElementById('alertaFormPagar');
    if (alerta) {
        alerta.textContent = '';
        alerta.style.display = 'none';
    }

    const valorDevido = Number(fatura.valorTotal || 0) - Number(fatura.valorPago || 0);
    document.getElementById('pagarValorFaturaExibicao').textContent = formatarMoeda(fatura.valorTotal);

    const avisoSaldo = document.getElementById('avisoSaldoRestante');
    if (Number(fatura.valorPago) > 0) {
        avisoSaldo.textContent = `Já pago: ${formatarMoeda(fatura.valorPago)} — Saldo restante: ${formatarMoeda(valorDevido)}`;
        avisoSaldo.style.display = 'block';
    } else {
        avisoSaldo.style.display = 'none';
    }

    const hoje = new Date().toISOString().slice(0, 10);
    document.getElementById('pagarDataPagamento').value = hoje;
    definirValorMoeda(document.getElementById('pagarValorPago'), valorDevido, false);

    abrirModal('modalPagarFatura');
}

// -------------------------------------------------------------
// Modal: Ajustar Encargos
// -------------------------------------------------------------
function abrirModalEncargos(faturaId) {
    const fatura = todasFaturas.find(f => String(f.id) === String(faturaId));
    if (!fatura) return;

    document.getElementById('formEncargosFatura')?.reset();
    document.getElementById('encargosFaturaId').value = fatura.id;

    definirValorMoeda(document.getElementById('encargosValorEncargos'), fatura.valorEncargos, false);
    definirValorMoeda(document.getElementById('encargosValorMinimo'), fatura.valorMinimo, false);

    abrirModal('modalEncargosFatura');
}

// -------------------------------------------------------------
// Ação: Reabrir Fatura
// -------------------------------------------------------------
async function acaoReabrir(faturaId, competencia) {
    const aviso = cfg().msgConfirmaReabertura || `Deseja realmente reabrir a fatura de competência ${competencia}?`;
    if (!confirm(aviso)) return;

    try {
        const resp = await enviar(`${cfg().urlBase}/${faturaId}/reabrir`, 'POST');
        if (resp && resp.sucesso) {
            toast(resp.mensagem || 'Fatura reaberta com sucesso.');
            carregar();
        } else {
            toast(resp?.mensagem || 'Erro ao reabrir fatura.', true);
        }
    } catch (e) {
        console.error('Erro ao reabrir fatura', e);
        toast('Erro de comunicação ao reabrir fatura.', true);
    }
}

// -------------------------------------------------------------
// Inicialização de Formulários e Eventos
// -------------------------------------------------------------
function inicializarFormularios() {
    // Form Fechar
    const formFechar = document.getElementById('formFecharFatura');
    if (formFechar) {
        formFechar.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('fecharFaturaId').value;
            const dataFechamento = document.getElementById('fecharDataFechamento').value;
            const dataVencimento = document.getElementById('fecharDataVencimento').value;
            const encargos = parseDecimal(document.getElementById('fecharValorEncargos').value);
            const minimo = parseDecimal(document.getElementById('fecharValorMinimo').value);

            const params = new URLSearchParams();
            params.append('dataFechamento', dataFechamento);
            params.append('dataVencimento', dataVencimento);
            params.append('valorEncargos', encargos);
            if (minimo > 0) params.append('valorMinimo', minimo);

            try {
                const resp = await enviar(`${cfg().urlBase}/${id}/fechar`, 'POST', params);
                if (resp && resp.sucesso) {
                    fecharModal('modalFecharFatura');
                    toast(resp.mensagem || 'Fatura fechada com sucesso.');
                    carregar();
                } else {
                    const alerta = document.getElementById('alertaFormFechar');
                    if (alerta) {
                        alerta.textContent = resp?.mensagem || 'Erro ao fechar fatura.';
                        alerta.style.display = 'block';
                    }
                }
            } catch (err) {
                console.error('Erro ao fechar fatura', err);
            }
        });
    }

    // Form Pagar
    const formPagar = document.getElementById('formPagarFatura');
    if (formPagar) {
        const campoValorPago = document.getElementById('pagarValorPago');
        if (campoValorPago) {
            campoValorPago.addEventListener('input', () => formatarElemento(campoValorPago, false));
        }

        formPagar.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('pagarFaturaId').value;
            const contaId = document.getElementById('pagarContaId').value;
            const dataPagamento = document.getElementById('pagarDataPagamento').value;
            const valorPago = parseDecimal(document.getElementById('pagarValorPago').value);

            const params = new URLSearchParams();
            params.append('contaId', contaId);
            params.append('dataPagamento', dataPagamento);
            params.append('valorPago', valorPago);

            try {
                const resp = await enviar(`${cfg().urlBase}/${id}/pagar`, 'POST', params);
                if (resp && resp.sucesso) {
                    fecharModal('modalPagarFatura');
                    toast(resp.mensagem || 'Pagamento registrado com sucesso.');
                    carregar();
                } else {
                    const alerta = document.getElementById('alertaFormPagar');
                    if (alerta) {
                        alerta.textContent = resp?.mensagem || 'Erro ao registrar pagamento.';
                        alerta.style.display = 'block';
                    }
                }
            } catch (err) {
                console.error('Erro ao registrar pagamento', err);
            }
        });
    }

    // Form Encargos
    const formEncargos = document.getElementById('formEncargosFatura');
    if (formEncargos) {
        const inputEnc = document.getElementById('encargosValorEncargos');
        if (inputEnc) inputEnc.addEventListener('input', () => formatarElemento(inputEnc, false));

        const inputMin = document.getElementById('encargosValorMinimo');
        if (inputMin) inputMin.addEventListener('input', () => formatarElemento(inputMin, false));

        formEncargos.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('encargosFaturaId').value;
            const encargos = parseDecimal(document.getElementById('encargosValorEncargos').value);
            const minimo = parseDecimal(document.getElementById('encargosValorMinimo').value);

            const params = new URLSearchParams();
            params.append('valorEncargos', encargos);
            if (minimo > 0) params.append('valorMinimo', minimo);

            try {
                const resp = await enviar(`${cfg().urlBase}/${id}/encargos`, 'PUT', params);
                if (resp && resp.sucesso) {
                    fecharModal('modalEncargosFatura');
                    toast(resp.mensagem || 'Encargos atualizados com sucesso.');
                    carregar();
                } else {
                    const alerta = document.getElementById('alertaFormEncargos');
                    if (alerta) {
                        alerta.textContent = resp?.mensagem || 'Erro ao atualizar encargos.';
                        alerta.style.display = 'block';
                    }
                }
            } catch (err) {
                console.error('Erro ao atualizar encargos', err);
            }
        });
    }
}

function atualizarClassesCabecalho() {
    document.querySelectorAll('#tabelaFaturasCartao thead th.sortable').forEach(th => {
        th.classList.remove('table-sort-asc', 'table-sort-desc');
        if (th.dataset.col === ordenacao.col) {
            th.classList.add(ordenacao.asc ? 'table-sort-asc' : 'table-sort-desc');
        }
    });
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaFaturasCartao thead th.sortable').forEach(th => {
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

function inicializarBuscaCompetencia() {
    if (buscaCompetencia) {
        buscaCompetencia.addEventListener('input', () => render());
        buscaCompetencia.addEventListener('change', () => render());
    }
    if (btnLimparCompetencia) {
        btnLimparCompetencia.addEventListener('click', () => {
            if (buscaCompetencia) {
                buscaCompetencia.value = '';
            }
            render();
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (selectCartao) {
        selectCartao.addEventListener('change', carregar);
    }
    inicializarFormularios();
    inicializarOrdenacao();
    inicializarBuscaCompetencia();
    carregar();
});
