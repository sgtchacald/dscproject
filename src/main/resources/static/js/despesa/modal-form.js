import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { definirValorMoeda, formatarElemento, formatarValorPtBr } from '../comum/mascara.js';
import { obterFiltroAtual } from './modal-filtro.js';

export const EVENTO_ALTERADO = 'despesa:alterada';

const cfg = () => document.getElementById('dadosTelaDespesa').dataset;
const form = () => document.getElementById('formDespesa');

let contasOriginais = [];
let cartoesOriginais = [];
let categoriasOriginais = [];
let opcoesCarregadas = false;
let formaAtual = 'CONTA';
let modoEdicao = false;
let origemAtual = 'MANUAL';
let taxaBuscaTimeout = null;
let contatosBuscadosCache = new Map();
let contatoSelecionadoAtual = null;

// Mapa de usuários adicionados ao rateio na tela: { id, nome, valor, statusPagamento, dataAcerto }
let itensRateio = [];

function mesDaData(dataIso) {
    return dataIso ? dataIso.slice(0, 7) : '';
}

function parseDecimal(valorStr) {
    if (!valorStr) return 0;
    if (typeof valorStr === 'number') return valorStr;
    const limpo = valorStr.replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, '');
    return parseFloat(limpo) || 0;
}

function formatarMoeda(valor) {
    const num = Number(valor != null ? valor : 0);
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(num);
}

function formatarInputDecimal(valor) {
    if (valor == null) return '';
    return Number(valor).toFixed(2).replace('.', ',');
}

async function carregarOpcoes() {
    if (opcoesCarregadas) return;

    try {
        const [contas, cartoes, categorias] = await Promise.all([
            getJson(cfg().urlContasOpcoes),
            getJson(cfg().urlCartoesOpcoes),
            getJson(cfg().urlCategoriasOpcoes)
        ]);

        contasOriginais = contas || [];
        cartoesOriginais = cartoes || [];
        categoriasOriginais = categorias || [];

        // Preencher Cartões
        const selectCartao = document.getElementById('despesaCartaoId');
        if (selectCartao) {
            selectCartao.innerHTML = '';
            cartoesOriginais.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.descricao;
                selectCartao.appendChild(opt);
            });
        }

        // Preencher Categorias
        const selectCategoria = document.getElementById('despesaCategoriaId');
        if (selectCategoria) {
            selectCategoria.innerHTML = '<option value="">Sem categoria</option>';
            categoriasOriginais.forEach(cat => {
                const opt = document.createElement('option');
                opt.value = cat.id;
                opt.textContent = cat.nome;
                selectCategoria.appendChild(opt);
            });
        }

        popularContas('CONTA');
        opcoesCarregadas = true;
    } catch (e) {
        console.error('Erro ao carregar opções para o formulário de despesa', e);
    }
}

function popularContas(forma) {
    const selectConta = document.getElementById('despesaContaId');
    if (!selectConta) return;
    selectConta.innerHTML = '';

    const contasFiltradas = forma === 'DINHEIRO'
        ? contasOriginais.filter(c => c.tipo === 'CARTEIRA')
        : contasOriginais;

    contasFiltradas.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = c.descricao;
        selectConta.appendChild(opt);
    });

    if (forma === 'DINHEIRO' && contasFiltradas.length === 0) {
        selectConta.classList.add('is-invalid');
        document.getElementById('erroContaId').textContent = 'Você ainda não tem uma conta do tipo Carteira. Crie uma em "Minhas Contas" para lançar despesas em dinheiro.';
    } else {
        selectConta.classList.remove('is-invalid');
    }
}

export function definirForma(forma) {
    formaAtual = forma;
    document.getElementById('despesaFormaPagamento').value = forma;

    document.querySelectorAll('#grupoBotoesForma button').forEach(btn => {
        if (btn.dataset.forma === forma) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    const grupoConta = document.getElementById('grupoConta');
    const grupoCartao = document.getElementById('grupoCartao');
    const labelConta = document.getElementById('labelConta');
    const grupoMeio = document.getElementById('grupoMeioPagamento');
    const linhaStatus = document.getElementById('linhaStatusPagamento');

    if (forma === 'CARTAO') {
        grupoConta.style.display = 'none';
        grupoCartao.style.display = 'block';
        grupoMeio.style.display = 'none';
        linhaStatus.style.display = 'flex';
    } else if (forma === 'DINHEIRO') {
        grupoConta.style.display = 'block';
        grupoCartao.style.display = 'none';
        labelConta.textContent = 'Conta (Carteira)';
        grupoMeio.style.display = 'none';
        linhaStatus.style.display = 'flex';
        popularContas('DINHEIRO');
    } else {
        grupoConta.style.display = 'block';
        grupoCartao.style.display = 'none';
        labelConta.textContent = 'Conta';
        grupoMeio.style.display = 'block';
        linhaStatus.style.display = 'flex';
        popularContas('CONTA');
    }
}

function limparErros() {
    const f = form();
    if (!f) return;
    f.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach(el => (el.textContent = ''));
    const alerta = document.getElementById('alertaFormDespesa');
    if (alerta) {
        alerta.style.display = 'none';
        alerta.textContent = '';
    }
    const inputMinhaCota = document.getElementById('despesaMinhaCota');
    if (inputMinhaCota) {
        inputMinhaCota.classList.remove('is-invalid', 'text-danger');
    }
    const erroRateio = document.getElementById('erroRateio');
    if (erroRateio) {
        erroRateio.textContent = '';
        erroRateio.classList.remove('d-block');
    }
}

function atualizarValorParcelaCalculada() {
    const valor = parseDecimal(document.getElementById('despesaValor').value);
    const n = parseInt(document.getElementById('despesaQtdParcelas').value, 10) || 1;
    const parcela = n > 0 ? valor / n : 0;
    const el = document.getElementById('despesaValorParcelaCalculada');
    if (el) el.textContent = formatarMoeda(parcela);

    const parcelada = document.getElementById('despesaParcelada')?.checked;
    const detalheRateio = document.getElementById('despesaDetalheParcelaRateio');
    const somaRateios = itensRateio.reduce((acc, curr) => acc + (Number(curr.valor) || 0), 0);
    const cota = +(valor - somaRateios).toFixed(2);

    if (detalheRateio) {
        if (parcelada && n > 1 && itensRateio.length > 0) {
            const parcelaDono = Math.max(0, +(cota / n).toFixed(2));
            const parcelaContatos = +(somaRateios / n).toFixed(2);
            detalheRateio.style.display = 'block';
            detalheRateio.innerHTML = `Sua parcela: <strong class="text-dark">${formatarMoeda(parcelaDono)}</strong> | Contatos: <strong class="text-dark">${formatarMoeda(parcelaContatos)}</strong>`;
        } else {
            detalheRateio.style.display = 'none';
            detalheRateio.textContent = '';
        }
    }

    // Atualizar informativos de parcela por contato na tabela de rateio
    document.querySelectorAll('.info-parcela-rateio').forEach(infoEl => {
        const idx = Number(infoEl.dataset.index);
        const item = itensRateio[idx];
        if (parcelada && n > 1 && item && (Number(item.valor) || 0) > 0) {
            infoEl.style.display = 'block';
            infoEl.textContent = `${n}x de ${formatarMoeda((Number(item.valor) || 0) / n)}`;
        } else {
            infoEl.style.display = 'none';
            infoEl.textContent = '';
        }
    });

    atualizarMinhaCota(false);
}

function atualizarMinhaCota(recalcularParcela = true) {
    const total = parseDecimal(document.getElementById('despesaValor').value);
    const somaRateios = itensRateio.reduce((acc, curr) => acc + (Number(curr.valor) || 0), 0);
    const cota = +(total - somaRateios).toFixed(2);
    const parcelada = document.getElementById('despesaParcelada')?.checked;
    const n = parseInt(document.getElementById('despesaQtdParcelas')?.value, 10) || 1;

    const inputMinhaCota = document.getElementById('despesaMinhaCota');
    if (inputMinhaCota && document.activeElement !== inputMinhaCota) {
        definirValorMoeda(inputMinhaCota, Math.max(0, cota));
    }

    const resumoTotal = document.getElementById('resumoTotalDespesa');
    if (resumoTotal) resumoTotal.textContent = formatarMoeda(total);

    const resumoFatias = document.getElementById('resumoFatiasContatos');
    if (resumoFatias) resumoFatias.textContent = formatarMoeda(somaRateios);

    const badgeZerada = document.getElementById('badgeCotaZerada');
    const infoStatus = document.getElementById('infoStatusRateio');
    const erroRateio = document.getElementById('erroRateio');

    if (somaRateios > 0 && Math.abs(cota) < 0.001) {
        if (badgeZerada) badgeZerada.style.display = 'inline-block';
        if (infoStatus) infoStatus.textContent = '100% repassado aos contatos (sua cota: R$ 0,00)';
        if (inputMinhaCota) inputMinhaCota.classList.remove('is-invalid', 'text-danger');
        if (erroRateio) {
            erroRateio.textContent = '';
            erroRateio.classList.remove('d-block');
        }
    } else {
        if (badgeZerada) badgeZerada.style.display = 'none';
        if (cota < -0.001) {
            if (infoStatus) infoStatus.textContent = 'A soma das fatias excede o valor da despesa';
            if (inputMinhaCota) inputMinhaCota.classList.add('is-invalid', 'text-danger');
            if (erroRateio) {
                erroRateio.textContent = `A soma das fatias (${formatarMoeda(somaRateios)}) não pode ser maior que o valor da despesa (${formatarMoeda(total)}).`;
                erroRateio.classList.add('d-block');
            }
        } else {
            const sufParcela = (parcelada && n > 1) ? ` (${n}x de ${formatarMoeda(cota / n)})` : '';
            if (infoStatus) infoStatus.textContent = `Parte da despesa sob sua responsabilidade${sufParcela}`;
            if (inputMinhaCota) inputMinhaCota.classList.remove('is-invalid', 'text-danger');
            if (erroRateio) {
                erroRateio.textContent = '';
                erroRateio.classList.remove('d-block');
            }
        }
    }

    if (recalcularParcela) {
        atualizarValorParcelaCalculada();
    }
}

function dividirRateioIgualmente() {
    if (itensRateio.length === 0) return;
    const total = parseDecimal(document.getElementById('despesaValor').value);
    if (total <= 0) return;

    const inputMinhaCota = document.getElementById('despesaMinhaCota');
    const cotaAtual = inputMinhaCota ? parseDecimal(inputMinhaCota.value) : total;
    const titularZerado = Math.abs(cotaAtual) < 0.001;

    if (titularZerado) {
        const qtd = itensRateio.length;
        const fatiaBase = Math.floor((total / qtd) * 100) / 100;
        let acumulado = 0;
        itensRateio.forEach((item, i) => {
            if (i === qtd - 1) {
                item.valor = +(total - acumulado).toFixed(2);
            } else {
                item.valor = fatiaBase;
                acumulado += fatiaBase;
            }
        });
        if (inputMinhaCota) definirValorMoeda(inputMinhaCota, 0);
    } else {
        const participantes = 1 + itensRateio.length;
        const fatiaBase = Math.floor((total / participantes) * 100) / 100;
        let acumulado = 0;
        itensRateio.forEach(item => {
            item.valor = fatiaBase;
            acumulado += fatiaBase;
        });
        const cotaDono = +(total - acumulado).toFixed(2);
        if (inputMinhaCota) definirValorMoeda(inputMinhaCota, cotaDono);
    }

    renderizarRateios();
}

function limparRateio() {
    itensRateio = [];
    const switchRateio = document.getElementById('despesaHabilitarRateio');
    if (switchRateio) switchRateio.checked = false;

    const conteudo = document.getElementById('conteudoRateio');
    if (conteudo) conteudo.style.display = 'none';

    const blocoItens = document.getElementById('blocoItensRateio');
    if (blocoItens) blocoItens.style.display = 'none';

    const btnDividir = document.getElementById('btnDividirIgualmenteRateio');
    if (btnDividir) btnDividir.style.display = 'none';

    const corpo = document.getElementById('corpoTabelaRateio');
    if (corpo) corpo.innerHTML = '';

    const buscaInput = document.getElementById('buscaUsuarioRateio');
    if (buscaInput) buscaInput.value = '';

    const dataList = document.getElementById('listaUsuariosRateio');
    if (dataList) dataList.innerHTML = '';

    contatoSelecionadoAtual = null;
    contatosBuscadosCache.clear();

    const inputMinhaCota = document.getElementById('despesaMinhaCota');
    if (inputMinhaCota) definirValorMoeda(inputMinhaCota, 0);

    const resumoTotal = document.getElementById('resumoTotalDespesa');
    if (resumoTotal) resumoTotal.textContent = 'R$ 0,00';

    const resumoFatias = document.getElementById('resumoFatiasContatos');
    if (resumoFatias) resumoFatias.textContent = 'R$ 0,00';

    const badgeZerada = document.getElementById('badgeCotaZerada');
    if (badgeZerada) badgeZerada.style.display = 'none';

    const infoStatus = document.getElementById('infoStatusRateio');
    if (infoStatus) infoStatus.textContent = '';

    const erroRateio = document.getElementById('erroRateio');
    if (erroRateio) {
        erroRateio.textContent = '';
        erroRateio.classList.remove('d-block');
    }

    const detalheParcela = document.getElementById('despesaDetalheParcelaRateio');
    if (detalheParcela) {
        detalheParcela.style.display = 'none';
        detalheParcela.textContent = '';
    }

    const cardNovoContatoRateio = document.getElementById('cardNovoContatoRateio');
    if (cardNovoContatoRateio) {
        const bs = window.bootstrap || window.tabler;
        if (bs && bs.Collapse) {
            const collapse = bs.Collapse.getInstance(cardNovoContatoRateio);
            if (collapse) collapse.hide();
        } else {
            cardNovoContatoRateio.classList.remove('show');
        }
    }
    const nomeInput = document.getElementById('novoContatoNome');
    if (nomeInput) nomeInput.value = '';
    const emailInput = document.getElementById('novoContatoEmail');
    if (emailInput) emailInput.value = '';
    const telInput = document.getElementById('novoContatoTelefone');
    if (telInput) telInput.value = '';
    const pixInput = document.getElementById('novoContatoPix');
    if (pixInput) pixInput.value = '';
    const alertaContatoRapido = document.getElementById('alertaContatoRapido');
    if (alertaContatoRapido) {
        alertaContatoRapido.style.display = 'none';
        alertaContatoRapido.textContent = '';
    }
}

function renderizarRateios() {
    const corpo = document.getElementById('corpoTabelaRateio');
    const blocoItens = document.getElementById('blocoItensRateio');
    const btnDividir = document.getElementById('btnDividirIgualmenteRateio');
    if (!corpo) return;
    corpo.innerHTML = '';

    if (itensRateio.length === 0) {
        if (blocoItens) blocoItens.style.display = 'none';
        if (btnDividir) btnDividir.style.display = 'none';
        const inputMinhaCota = document.getElementById('despesaMinhaCota');
        if (inputMinhaCota) definirValorMoeda(inputMinhaCota, 0);
        const detalheParcela = document.getElementById('despesaDetalheParcelaRateio');
        if (detalheParcela) {
            detalheParcela.style.display = 'none';
            detalheParcela.textContent = '';
        }
        const resumoFatias = document.getElementById('resumoFatiasContatos');
        if (resumoFatias) resumoFatias.textContent = 'R$ 0,00';
        return;
    }

    if (blocoItens) blocoItens.style.display = 'block';
    if (btnDividir) btnDividir.style.display = '';

    const parcelada = document.getElementById('despesaParcelada')?.checked;
    const n = parseInt(document.getElementById('despesaQtdParcelas')?.value, 10) || 1;

    itensRateio.forEach((item, index) => {
        const badgeTipo = item.tipo === 'SISTEMA'
            ? '<span class="badge bg-blue-lt ms-1">Sistema</span>'
            : '<span class="badge bg-secondary-lt ms-1">Externo</span>';
        const infoPix = item.chavePix
            ? `<div class="text-muted" style="font-size: 0.75rem;"><i class="ph ph-qr-code me-1"></i>PIX: ${item.chavePix}</div>`
            : '';

        const valorNum = Number(item.valor) || 0;
        const exibeParcela = parcelada && n > 1 && valorNum > 0;
        const textoParcela = exibeParcela ? `${n}x de ${formatarMoeda(valorNum / n)}` : '';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <strong>${item.nome}</strong> ${badgeTipo}
                ${infoPix}
            </td>
            <td>
                <input type="text" class="form-control form-control-sm input-fatia-rateio mascara-moeda text-end"
                       data-index="${index}" value="${formatarValorPtBr(Math.round(valorNum * 100))}">
                <div class="text-muted text-end info-parcela-rateio mt-1" data-index="${index}"
                     style="font-size: 0.75rem; ${exibeParcela ? '' : 'display:none;'}">${textoParcela}</div>
            </td>
            <td>
                <label class="form-check form-check-inline m-0">
                    <input class="form-check-input check-acerto-rateio" type="checkbox"
                           data-index="${index}" ${item.statusPagamento === 'SIM' ? 'checked' : ''}>
                    <span class="form-check-label small">${item.statusPagamento === 'SIM' ? 'Pago' : 'Pendente'}</span>
                </label>
            </td>
            <td class="text-end">
                <button type="button" class="btn btn-action btn-outline-primary btn-sm btn-total-rateio me-1"
                        data-index="${index}" title="Atribuir valor total a este contato (zerar minha cota)">
                    <span class="small fw-bold">100%</span>
                </button>
                <button type="button" class="btn btn-action text-danger btn-remover-rateio" data-index="${index}" title="Remover">
                    <i class="ph ph-trash" aria-hidden="true"></i>
                </button>
            </td>
        `;
        corpo.appendChild(tr);
    });

    atualizarMinhaCota();
}

export async function abrirNovo() {
    const f = form();
    if (!f) return;
    f.reset();
    limparErros();
    limparRateio();
    modoEdicao = false;
    origemAtual = 'MANUAL';

    await carregarOpcoes();

    document.getElementById('despesaId').value = '';
    document.getElementById('tituloModalDespesa').textContent = cfg().labelNovo || 'Nova despesa';
    document.getElementById('avisoDespesaImportada').style.display = 'none';

    document.getElementById('despesaContaId').disabled = false;
    document.getElementById('despesaCartaoId').disabled = false;
    document.getElementById('btnFormaConta').disabled = false;
    document.getElementById('btnFormaCartao').disabled = false;
    document.getElementById('btnFormaDinheiro').disabled = false;

    const hoje = new Date();
    const hojeIso = hoje.toISOString().slice(0, 10);
    const filtroAtual = typeof obterFiltroAtual === 'function' ? obterFiltroAtual() : null;
    const compFiltro = (filtroAtual && filtroAtual.competenciaInicio) ? filtroAtual.competenciaInicio : null;
    const competenciaPadrao = compFiltro || mesDaData(hojeIso);

    let dataLancamentoPadrao = hojeIso;
    if (compFiltro && compFiltro !== mesDaData(hojeIso)) {
        const [anoStr, mesStr] = compFiltro.split('-');
        const ano = parseInt(anoStr, 10);
        const mes = parseInt(mesStr, 10);
        const diaAtual = hoje.getDate();
        const ultimoDiaDoMes = new Date(ano, mes, 0).getDate();
        const diaAjustado = Math.min(diaAtual, ultimoDiaDoMes);
        dataLancamentoPadrao = `${compFiltro}-${String(diaAjustado).padStart(2, '0')}`;
    }

    document.getElementById('despesaDataLancamento').value = dataLancamentoPadrao;
    document.getElementById('despesaCompetencia').value = competenciaPadrao;

    definirForma('CONTA');

    document.getElementById('despesaPago').checked = false;
    document.getElementById('grupoDataPagamento').style.display = 'none';

    document.getElementById('despesaParcelada').checked = false;
    document.getElementById('despesaParcelada').disabled = false;
    document.getElementById('despesaRecorrente').checked = false;
    document.getElementById('despesaRecorrente').disabled = false;
    document.getElementById('secaoParcelamentoRecorrencia').style.display = 'block';
    document.getElementById('grupoCamposParcelamento').style.display = 'none';
    document.getElementById('grupoCamposRecorrencia').style.display = 'none';

    abrirModal('modalDespesa');
}

export async function abrirEdicao(id, focarRateio = false) {
    limparErros();
    limparRateio();
    modoEdicao = true;

    await carregarOpcoes();

    try {
        const d = await getJson(`${cfg().urlBuscar}/${id}`);

        document.getElementById('despesaId').value = d.id;
        document.getElementById('tituloModalDespesa').textContent = cfg().labelEditar || 'Editar despesa';

        document.getElementById('despesaNome').value = d.nome || '';
        document.getElementById('despesaDescricao').value = d.descricao || '';
        document.getElementById('despesaCompetencia').value = d.competencia || '';
        document.getElementById('despesaDataLancamento').value = d.dataLancamento || '';
        document.getElementById('despesaDataVencimento').value = d.dataVencimento || '';
        definirValorMoeda(document.getElementById('despesaValor'), d.valor);
        document.getElementById('despesaCategoriaId').value = d.categoriaId || '';

        // Se importada do Open Finance / Cartão
        origemAtual = d.origem || 'MANUAL';
        const ehOpenFinance = d.origem === 'OPEN_FINANCE';
        const ehManual = d.origem === 'MANUAL';
        document.getElementById('avisoDespesaImportada').style.display = ehOpenFinance ? 'block' : 'none';
        document.getElementById('despesaContaId').disabled = !ehManual;
        document.getElementById('despesaCartaoId').disabled = !ehManual;
        document.getElementById('btnFormaConta').disabled = !ehManual;
        document.getElementById('btnFormaCartao').disabled = !ehManual;
        document.getElementById('btnFormaDinheiro').disabled = !ehManual;

        // Forma de pagamento
        definirForma(d.formaPagamento || 'CONTA');
        if (d.formaPagamento === 'CARTAO') {
            document.getElementById('despesaCartaoId').value = d.cartaoId || '';
            if (d.origem === 'IMPORTACAO') {
                document.getElementById('linhaStatusPagamento').style.display = 'flex';
            }
        } else {
            document.getElementById('despesaContaId').value = d.contaId || '';
            if (d.meioPagamento) {
                document.getElementById('despesaMeioPagamento').value = d.meioPagamento;
            }
        }

        // Status de pagamento
        const pago = d.statusPagamento === 'SIM';
        document.getElementById('despesaPago').checked = pago;
        document.getElementById('grupoDataPagamento').style.display = pago ? 'block' : 'none';
        document.getElementById('despesaDataPagamento').value = d.dataPagamento || '';

        // Parcelamento e Recorrência (na edição de item isolado, esconde se já for parcelada/recorrente; exibe se for avulsa)
        const secaoParcRec = document.getElementById('secaoParcelamentoRecorrencia');
        if (secaoParcRec) {
            if (!d.parcelada && !d.recorrente) {
                secaoParcRec.style.display = 'block';
                document.getElementById('despesaParcelada').checked = false;
                document.getElementById('despesaParcelada').disabled = false;
                document.getElementById('despesaRecorrente').checked = false;
                document.getElementById('despesaRecorrente').disabled = false;
                document.getElementById('grupoCamposParcelamento').style.display = 'none';
                document.getElementById('grupoCamposRecorrencia').style.display = 'none';
                document.getElementById('despesaQtdParcelas').value = '2';
                document.getElementById('despesaQtdMesesRecorrencia').value = '12';
                const detalheParcela = document.getElementById('despesaDetalheParcelaRateio');
                if (detalheParcela) {
                    detalheParcela.style.display = 'none';
                    detalheParcela.textContent = '';
                }
            } else {
                secaoParcRec.style.display = 'none';
                document.getElementById('despesaParcelada').checked = false;
                document.getElementById('despesaRecorrente').checked = false;
                document.getElementById('grupoCamposParcelamento').style.display = 'none';
                document.getElementById('grupoCamposRecorrencia').style.display = 'none';
            }
        }

        // Rateio
        const temRateio = d.rateio && Array.isArray(d.rateio) && d.rateio.length > 0;
        const switchRateio = document.getElementById('despesaHabilitarRateio');
        const conteudoRateio = document.getElementById('conteudoRateio');

        if (temRateio || focarRateio) {
            if (switchRateio) switchRateio.checked = true;
            if (conteudoRateio) conteudoRateio.style.display = 'block';
        } else {
            if (switchRateio) switchRateio.checked = false;
            if (conteudoRateio) conteudoRateio.style.display = 'none';
        }

        if (temRateio) {
            itensRateio = d.rateio.map(r => ({
                id: r.contatoId || r.usuarioId,
                nome: r.contatoNome || r.usuarioNome,
                tipo: r.contatoTipo,
                chavePix: r.contatoChavePix,
                valor: r.valor,
                statusPagamento: r.statusPagamento || 'NAO',
                dataAcerto: r.dataAcerto
            }));
        } else {
            itensRateio = [];
        }
        renderizarRateios();

        abrirModal('modalDespesa');

        if (focarRateio) {
            setTimeout(() => {
                const sec = document.getElementById('secaoRateio');
                if (sec) {
                    sec.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    const busca = document.getElementById('buscaUsuarioRateio');
                    if (busca) busca.focus();
                }
            }, 300);
        }
    } catch (e) {
        toast('Erro ao carregar despesa para edição.', true);
    }
}

export function excluir(id, nome, parcelada, nroParcela, qtdParcelas, recorrente, idRecorrentePai) {
    let msg;
    if (parcelada && nroParcela === 1 && qtdParcelas > 1) {
        msg = (cfg().msgConfirmaExclusaoMae || '').replace('{0}', qtdParcelas);
    } else if (recorrente && !idRecorrentePai) {
        msg = (cfg().msgConfirmaExclusaoRecorrenteMae || 'Esta despesa é a geradora de uma série recorrente. Ao excluí-la, todas as ocorrências da série também serão excluídas. Deseja prosseguir?');
    } else {
        msg = (cfg().msgConfirmaExclusao || '').replace('{0}', nome);
    }

    if (!confirm(msg)) return;

    enviar(`${cfg().urlExcluir}/${id}`, 'DELETE')
        .then(resp => {
            if (resp.sucesso) {
                toast(resp.mensagem || 'Despesa excluída com sucesso.', false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                toast(resp.mensagem || 'Não foi possível excluir a despesa.', true);
            }
        })
        .catch(() => toast('Erro de comunicação ao excluir despesa.', true));
}

export function inicializarForm() {
    const f = form();
    if (!f) return;

    // Toggle Forma
    document.querySelectorAll('#grupoBotoesForma button').forEach(btn => {
        btn.addEventListener('click', function () {
            definirForma(this.dataset.forma);
        });
    });

    // Já paguei switch
    document.getElementById('despesaPago').addEventListener('change', function () {
        const grupo = document.getElementById('grupoDataPagamento');
        if (this.checked) {
            grupo.style.display = 'block';
            const dt = document.getElementById('despesaDataPagamento');
            if (!dt.value) dt.value = new Date().toISOString().slice(0, 10);
        } else {
            grupo.style.display = 'none';
            document.getElementById('despesaDataPagamento').value = '';
        }
    });

    // Parcelada switch
    document.getElementById('despesaParcelada').addEventListener('change', function () {
        const grupo = document.getElementById('grupoCamposParcelamento');
        const labelValor = document.getElementById('labelValor');
        if (this.checked) {
            document.getElementById('despesaRecorrente').checked = false;
            document.getElementById('grupoCamposRecorrencia').style.display = 'none';
            grupo.style.display = 'flex';
            labelValor.textContent = 'Valor total da compra';
        } else {
            grupo.style.display = 'none';
            labelValor.textContent = 'Valor';
        }
        atualizarValorParcelaCalculada();
    });

    // Recorrente switch
    document.getElementById('despesaRecorrente').addEventListener('change', function () {
        const grupo = document.getElementById('grupoCamposRecorrencia');
        const labelValor = document.getElementById('labelValor');
        if (this.checked) {
            document.getElementById('despesaParcelada').checked = false;
            document.getElementById('grupoCamposParcelamento').style.display = 'none';
            grupo.style.display = 'flex';
            labelValor.textContent = 'Valor mensal';
        } else {
            grupo.style.display = 'none';
            labelValor.textContent = 'Valor';
        }
    });

    // Rateio switch
    const switchRateio = document.getElementById('despesaHabilitarRateio');
    if (switchRateio) {
        switchRateio.addEventListener('change', function () {
            const conteudo = document.getElementById('conteudoRateio');
            if (this.checked) {
                if (conteudo) conteudo.style.display = 'block';
                const buscaInput = document.getElementById('buscaUsuarioRateio');
                if (buscaInput) setTimeout(() => buscaInput.focus(), 150);
            } else {
                if (conteudo) conteudo.style.display = 'none';
                limparRateio();
            }
        });
    }

    document.getElementById('despesaValor').addEventListener('input', atualizarValorParcelaCalculada);
    document.getElementById('despesaQtdParcelas').addEventListener('input', atualizarValorParcelaCalculada);

    // Rateio Autocomplete
    const buscaInput = document.getElementById('buscaUsuarioRateio');
    const dataList = document.getElementById('listaUsuariosRateio');
    if (buscaInput) {
        buscaInput.addEventListener('input', function () {
            const rawVal = this.value;
            const termo = rawVal.trim();
            if (!termo) {
                contatoSelecionadoAtual = null;
                return;
            }

            // 1. Verifica se o texto digitado/selecionado coincide com uma das opções já existentes na datalist
            const optExistente = Array.from(dataList.options).find(o => o.value === termo || o.dataset.nome === termo);
            if (optExistente) {
                contatoSelecionadoAtual = {
                    id: Number(optExistente.dataset.id),
                    nome: optExistente.dataset.nome,
                    tipo: optExistente.dataset.tipo,
                    chavePix: optExistente.dataset.chavePix || ''
                };
                clearTimeout(taxaBuscaTimeout);
                return;
            }

            // 2. Verifica se está no cache de contatos buscados
            if (contatosBuscadosCache.has(termo.toLowerCase())) {
                contatoSelecionadoAtual = contatosBuscadosCache.get(termo.toLowerCase());
                clearTimeout(taxaBuscaTimeout);
                return;
            }

            contatoSelecionadoAtual = null;
            const termoBusca = termo.split('(')[0].trim();
            if (termoBusca.length < 2) return;

            clearTimeout(taxaBuscaTimeout);
            taxaBuscaTimeout = setTimeout(async () => {
                try {
                    const url = cfg().urlContatosRateio || cfg().urlUsuariosRateio;
                    const contatos = await getJson(`${url}?termo=${encodeURIComponent(termoBusca)}`);
                    dataList.innerHTML = '';
                    contatos.forEach(c => {
                        const opt = document.createElement('option');
                        const tipoLabel = c.tipo === 'SISTEMA' ? 'Usuário' : 'Externo';
                        const info = c.email || c.telefone || '';
                        const displayVal = info ? `${c.nome} (${tipoLabel} - ${info})` : `${c.nome} (${tipoLabel})`;
                        opt.value = displayVal;
                        opt.dataset.id = c.id;
                        opt.dataset.nome = c.nome;
                        opt.dataset.tipo = c.tipo;
                        opt.dataset.chavePix = c.chavePix || '';
                        dataList.appendChild(opt);

                        const contatoObj = {
                            id: c.id,
                            nome: c.nome,
                            tipo: c.tipo,
                            chavePix: c.chavePix || ''
                        };
                        contatosBuscadosCache.set(displayVal.toLowerCase(), contatoObj);
                        contatosBuscadosCache.set(c.nome.toLowerCase(), contatoObj);
                    });

                    // Verifica se o valor atual do input casa com alguma opção retornada
                    const matchRecente = Array.from(dataList.options).find(o => o.value === buscaInput.value.trim() || o.dataset.nome === buscaInput.value.trim());
                    if (matchRecente) {
                        contatoSelecionadoAtual = {
                            id: Number(matchRecente.dataset.id),
                            nome: matchRecente.dataset.nome,
                            tipo: matchRecente.dataset.tipo,
                            chavePix: matchRecente.dataset.chavePix || ''
                        };
                    }
                } catch (e) {
                    console.error('Erro ao buscar contatos para rateio', e);
                }
            }, 300);
        });

        buscaInput.addEventListener('change', function () {
            const val = this.value.trim();
            if (!val) return;
            const opt = Array.from(dataList.options).find(o => o.value === val || o.dataset.nome === val);
            if (opt) {
                contatoSelecionadoAtual = {
                    id: Number(opt.dataset.id),
                    nome: opt.dataset.nome,
                    tipo: opt.dataset.tipo,
                    chavePix: opt.dataset.chavePix || ''
                };
            }
        });

        function executarAdicionarContato() {
            let contato = contatoSelecionadoAtual;
            const val = buscaInput.value.trim();

            if (!contato && val) {
                const opt = Array.from(dataList.options).find(o => o.value === val || o.dataset.nome === val);
                if (opt) {
                    contato = {
                        id: Number(opt.dataset.id),
                        nome: opt.dataset.nome,
                        tipo: opt.dataset.tipo,
                        chavePix: opt.dataset.chavePix || ''
                    };
                } else if (contatosBuscadosCache.has(val.toLowerCase())) {
                    contato = contatosBuscadosCache.get(val.toLowerCase());
                } else {
                    const nomeAntesParen = val.split('(')[0].trim().toLowerCase();
                    if (contatosBuscadosCache.has(nomeAntesParen)) {
                        contato = contatosBuscadosCache.get(nomeAntesParen);
                    }
                }
            }

            if (!contato) {
                toast('Selecione um contato válido da lista.', true);
                return;
            }

            if (itensRateio.some(i => i.id === contato.id)) {
                toast('Contato já adicionado ao rateio.', true);
                return;
            }

            itensRateio.push({
                id: contato.id,
                nome: contato.nome,
                tipo: contato.tipo,
                chavePix: contato.chavePix,
                valor: 0,
                statusPagamento: 'NAO',
                dataAcerto: null
            });

            buscaInput.value = '';
            dataList.innerHTML = '';
            contatoSelecionadoAtual = null;

            const total = parseDecimal(document.getElementById('despesaValor').value);
            if (total > 0) {
                dividirRateioIgualmente();
            } else {
                renderizarRateios();
            }
        }

        document.getElementById('btnAdicionarRateio')?.addEventListener('click', executarAdicionarContato);

        buscaInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                executarAdicionarContato();
            }
        });
    }

    // Cadastro rápido de contato externo inline
    const btnSalvarNovoContato = document.getElementById('btnSalvarNovoContato');
    const btnFecharCardContato = document.getElementById('btnFecharCardContato');
    const cardNovoContatoRateio = document.getElementById('cardNovoContatoRateio');
    const alertaContatoRapido = document.getElementById('alertaContatoRapido');

    if (btnFecharCardContato && cardNovoContatoRateio) {
        btnFecharCardContato.addEventListener('click', () => {
            if (typeof bootstrap !== 'undefined') {
                const collapse = bootstrap.Collapse.getInstance(cardNovoContatoRateio) || new bootstrap.Collapse(cardNovoContatoRateio, { toggle: false });
                collapse.hide();
            }
        });
    }

    if (btnSalvarNovoContato) {
        btnSalvarNovoContato.addEventListener('click', async () => {
            const nomeInput = document.getElementById('novoContatoNome');
            const emailInput = document.getElementById('novoContatoEmail');
            const telInput = document.getElementById('novoContatoTelefone');
            const pixInput = document.getElementById('novoContatoPix');

            const nome = nomeInput ? nomeInput.value.trim() : '';
            if (!nome) {
                if (alertaContatoRapido) {
                    alertaContatoRapido.style.display = 'block';
                    alertaContatoRapido.textContent = 'O nome do contato é obrigatório.';
                }
                if (nomeInput) nomeInput.focus();
                return;
            }

            if (alertaContatoRapido) {
                alertaContatoRapido.style.display = 'none';
                alertaContatoRapido.textContent = '';
            }

            const body = new URLSearchParams();
            body.append('nome', nome);
            if (emailInput && emailInput.value.trim()) body.append('email', emailInput.value.trim());
            if (telInput && telInput.value.trim()) body.append('telefone', telInput.value.trim());
            if (pixInput && pixInput.value.trim()) body.append('chavePix', pixInput.value.trim());

            try {
                const resp = await enviar(cfg().urlContatosRapido, 'POST', body);
                if (resp.sucesso && resp.contato) {
                    const c = resp.contato;
                    if (!itensRateio.some(i => i.id === c.id)) {
                        itensRateio.push({
                            id: c.id,
                            nome: c.nome,
                            tipo: c.tipo || 'EXTERNO',
                            chavePix: c.chavePix,
                            valor: 0,
                            statusPagamento: 'NAO',
                            dataAcerto: null
                        });
                        const total = parseDecimal(document.getElementById('despesaValor').value);
                        if (total > 0) {
                            dividirRateioIgualmente();
                        } else {
                            renderizarRateios();
                        }
                    }

                    if (nomeInput) nomeInput.value = '';
                    if (emailInput) emailInput.value = '';
                    if (telInput) telInput.value = '';
                    if (pixInput) pixInput.value = '';

                    if (cardNovoContatoRateio && typeof bootstrap !== 'undefined') {
                        const collapse = bootstrap.Collapse.getInstance(cardNovoContatoRateio) || new bootstrap.Collapse(cardNovoContatoRateio, { toggle: false });
                        collapse.hide();
                    }

                    toast(resp.mensagem || 'Contato cadastrado com sucesso!', false);
                } else {
                    if (alertaContatoRapido) {
                        alertaContatoRapido.style.display = 'block';
                        alertaContatoRapido.textContent = resp.mensagem || 'Não foi possível salvar o contato.';
                    }
                }
            } catch (err) {
                toast('Erro de comunicação ao salvar contato rápido.', true);
            }
        });
    }

    // Event delegation para tabela de rateio
    const tabelaRateio = document.getElementById('tabelaRateio');
    if (tabelaRateio) {
        tabelaRateio.addEventListener('input', function (e) {
            if (e.target.classList.contains('input-fatia-rateio')) {
                formatarElemento(e.target);
                const idx = Number(e.target.dataset.index);
                itensRateio[idx].valor = parseDecimal(e.target.value);
                atualizarMinhaCota();
            }
        });

        tabelaRateio.addEventListener('change', function (e) {
            if (e.target.classList.contains('check-acerto-rateio')) {
                const idx = Number(e.target.dataset.index);
                itensRateio[idx].statusPagamento = e.target.checked ? 'SIM' : 'NAO';
                if (e.target.checked) {
                    itensRateio[idx].dataAcerto = new Date().toISOString().slice(0, 10);
                } else {
                    itensRateio[idx].dataAcerto = null;
                }
                const label = e.target.closest('label').querySelector('.form-check-label');
                if (label) label.textContent = e.target.checked ? 'Pago' : 'Pendente';
            }
        });

        tabelaRateio.addEventListener('click', function (e) {
            const btnTotal = e.target.closest('.btn-total-rateio');
            if (btnTotal) {
                const idx = Number(btnTotal.dataset.index);
                const total = parseDecimal(document.getElementById('despesaValor').value);
                let outrosTotal = 0;
                itensRateio.forEach((item, i) => {
                    if (i !== idx) {
                        outrosTotal += Number(item.valor) || 0;
                    }
                });
                const valorAtribuir = Math.max(0, +(total - outrosTotal).toFixed(2));
                itensRateio[idx].valor = valorAtribuir;
                const inputFatia = tabelaRateio.querySelector(`.input-fatia-rateio[data-index="${idx}"]`);
                if (inputFatia) {
                    definirValorMoeda(inputFatia, valorAtribuir);
                }
                atualizarMinhaCota();
                return;
            }

            const btnRemover = e.target.closest('.btn-remover-rateio');
            if (btnRemover) {
                const idx = Number(btnRemover.dataset.index);
                itensRateio.splice(idx, 1);
                renderizarRateios();
            }
        });
    }

    // Interações com Minha Cota (Você - Titular)
    const inputMinhaCota = document.getElementById('despesaMinhaCota');
    if (inputMinhaCota) {
        inputMinhaCota.addEventListener('input', function (e) {
            formatarElemento(e.target);
            const cotaDigitada = parseDecimal(e.target.value);
            const total = parseDecimal(document.getElementById('despesaValor').value);

            if (itensRateio.length === 1) {
                const novaFatia = Math.max(0, +(total - cotaDigitada).toFixed(2));
                itensRateio[0].valor = novaFatia;
                const inputFatia = tabelaRateio?.querySelector('.input-fatia-rateio[data-index="0"]');
                if (inputFatia) {
                    definirValorMoeda(inputFatia, novaFatia);
                }
            }
            atualizarMinhaCota();
        });
    }

    const btnZerarMinhaCota = document.getElementById('btnZerarMinhaCota');
    if (btnZerarMinhaCota) {
        btnZerarMinhaCota.addEventListener('click', function () {
            const total = parseDecimal(document.getElementById('despesaValor').value);
            if (inputMinhaCota) {
                definirValorMoeda(inputMinhaCota, 0);
            }
            if (itensRateio.length === 1) {
                itensRateio[0].valor = total;
                const inputFatia = tabelaRateio?.querySelector('.input-fatia-rateio[data-index="0"]');
                if (inputFatia) {
                    definirValorMoeda(inputFatia, total);
                }
            } else if (itensRateio.length > 1) {
                const qtd = itensRateio.length;
                const fatiaBase = Math.floor((total / qtd) * 100) / 100;
                let acumulado = 0;
                itensRateio.forEach((item, i) => {
                    if (i === qtd - 1) {
                        item.valor = +(total - acumulado).toFixed(2);
                    } else {
                        item.valor = fatiaBase;
                        acumulado += fatiaBase;
                    }
                });
                renderizarRateios();
            }
            atualizarMinhaCota();
        });
    }

    const btnDividirIgualmente = document.getElementById('btnDividirIgualmenteRateio');
    if (btnDividirIgualmente) {
        btnDividirIgualmente.addEventListener('click', function () {
            dividirRateioIgualmente();
        });
    }

    // Submit
    f.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErros();

        const id = document.getElementById('despesaId').value;
        const parcelada = document.getElementById('despesaParcelada').checked;
        const recorrente = document.getElementById('despesaRecorrente').checked;
        const valorInformado = parseDecimal(document.getElementById('despesaValor').value);

        const habilitarRateio = document.getElementById('despesaHabilitarRateio')?.checked;
        if (!habilitarRateio) {
            itensRateio = [];
        }

        // Validação client-side de rateio
        if (itensRateio.length > 0) {
            const itemInvalido = itensRateio.find(item => !item.valor || Number(item.valor) <= 0);
            if (itemInvalido) {
                const erroRateio = document.getElementById('erroRateio');
                if (erroRateio) {
                    erroRateio.textContent = 'Informe um valor maior que zero para cada contato no rateio.';
                    erroRateio.classList.add('d-block');
                }
                toast('Informe um valor maior que zero para cada contato no rateio.', true);
                return;
            }
            const somaFatias = itensRateio.reduce((acc, curr) => acc + (Number(curr.valor) || 0), 0);
            if (somaFatias > valorInformado) {
                const erroRateio = document.getElementById('erroRateio');
                if (erroRateio) {
                    erroRateio.textContent = `A soma das fatias (${formatarMoeda(somaFatias)}) não pode ser maior que o valor da despesa (${formatarMoeda(valorInformado)}).`;
                    erroRateio.classList.add('d-block');
                }
                toast('A soma das fatias não pode ser maior que o valor da despesa.', true);
                return;
            }
        }

        const body = new URLSearchParams();
        body.append('nome', document.getElementById('despesaNome').value.trim());
        body.append('descricao', document.getElementById('despesaDescricao').value.trim());
        body.append('competencia', document.getElementById('despesaCompetencia').value);
        body.append('dataLancamento', document.getElementById('despesaDataLancamento').value);
        body.append('dataVencimento', document.getElementById('despesaDataVencimento').value || '');
        body.append('formaPagamento', formaAtual);

        if (formaAtual === 'CARTAO') {
            body.append('cartaoId', document.getElementById('despesaCartaoId').value || '');
        } else {
            body.append('contaId', document.getElementById('despesaContaId').value || '');
            if (formaAtual === 'CONTA') {
                body.append('meioPagamento', document.getElementById('despesaMeioPagamento').value);
            } else {
                body.append('meioPagamento', 'DINHEIRO');
            }
        }

        const pago = document.getElementById('despesaPago').checked;
        body.append('statusPagamento', pago ? 'SIM' : 'NAO');
        if (pago) {
            body.append('dataPagamento', document.getElementById('despesaDataPagamento').value || '');
        }

        const catId = document.getElementById('despesaCategoriaId').value;
        if (catId) body.append('categoriaId', catId);

        if (parcelada) {
            body.append('parcelada', 'true');
            body.append('recorrente', 'false');
            body.append('qtdParcelas', document.getElementById('despesaQtdParcelas').value);
            body.append('valorTotalCompra', valorInformado.toFixed(2));
            const n = parseInt(document.getElementById('despesaQtdParcelas').value, 10) || 1;
            body.append('valor', (valorInformado / n).toFixed(2));
        } else if (recorrente) {
            body.append('recorrente', 'true');
            body.append('parcelada', 'false');
            body.append('qtdMesesRecorrencia', document.getElementById('despesaQtdMesesRecorrencia').value || '12');
            body.append('valor', valorInformado.toFixed(2));
        } else {
            body.append('parcelada', 'false');
            body.append('recorrente', 'false');
            body.append('valor', valorInformado.toFixed(2));
        }

        // Rateio
        itensRateio.forEach((item, i) => {
            body.append(`rateio[${i}].contatoId`, item.id);
            body.append(`rateio[${i}].usuarioId`, item.id);
            body.append(`rateio[${i}].valor`, Number(item.valor).toFixed(2));
            body.append(`rateio[${i}].statusPagamento`, item.statusPagamento);
            if (item.dataAcerto) {
                body.append(`rateio[${i}].dataAcerto`, item.dataAcerto);
            }
        });

        const url = id ? `${cfg().urlEditar}/${id}` : cfg().urlInserir;
        const metodo = id ? 'PUT' : 'POST';

        try {
            const resp = await enviar(url, metodo, body);
            if (resp.sucesso) {
                fecharModal('modalDespesa');
                toast(resp.mensagem, false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                if (resp.errosCampos) {
                    Object.entries(resp.errosCampos).forEach(([campo, msg]) => {
                        const input = document.querySelector(`[name="${campo}"]`) || document.getElementById(`despesa${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                        if (input) input.classList.add('is-invalid');
                        const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                        if (divErro) divErro.textContent = msg;
                    });
                }
                if (resp.errosNegocio) {
                    const alerta = document.getElementById('alertaFormDespesa');
                    alerta.style.display = 'block';
                    alerta.textContent = Object.values(resp.errosNegocio).join(' ');
                }
            }
        } catch (err) {
            toast('Erro de comunicação ao salvar despesa.', true);
        }
    });

    document.getElementById('btnNovaDespesa')?.addEventListener('click', e => {
        e.preventDefault();
        abrirNovo();
    });

    const modalEl = document.getElementById('modalDespesa');
    if (modalEl) {
        modalEl.addEventListener('hidden.bs.modal', function () {
            limparRateio();
            limparErros();
            form()?.reset();
            const avisoImp = document.getElementById('avisoDespesaImportada');
            if (avisoImp) avisoImp.style.display = 'none';
            const grpPg = document.getElementById('grupoDataPagamento');
            if (grpPg) grpPg.style.display = 'none';
            const grpParc = document.getElementById('grupoCamposParcelamento');
            if (grpParc) grpParc.style.display = 'none';
            const grpRec = document.getElementById('grupoCamposRecorrencia');
            if (grpRec) grpRec.style.display = 'none';
            modoEdicao = false;
        });
    }
}
