import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { definirValorMoeda, formatarElemento, formatarValorPtBr, parseDecimal } from '../comum/mascara.js';

export const EVENTO_ALTERADO = 'transacao:alterada';

const cfg = () => document.getElementById('dadosTelaTransacao').dataset;
const form = () => document.getElementById('formTransacao');

let contasCarregadas = false;
let categoriasCarregadas = false;

async function carregarOpcoesFormulario() {
    if (contasCarregadas && categoriasCarregadas) return;

    try {
        const [contas, categorias] = await Promise.all([
            getJson(cfg().urlContasOpcoes),
            getJson(cfg().urlCategoriasOpcoes)
        ]);

        const selConta = document.getElementById('transacaoContaId');
        if (selConta && (!contasCarregadas || selConta.options.length <= 1)) {
            selConta.innerHTML = '<option value="">Selecione a conta...</option>';
            (contas || []).forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.descricao;
                selConta.appendChild(opt);
            });
            contasCarregadas = true;
        }

        const selCat = document.getElementById('transacaoCategoriaId');
        if (selCat && (!categoriasCarregadas || selCat.options.length <= 1)) {
            selCat.innerHTML = '<option value="">Sem categoria</option>';
            (categorias || []).forEach(cat => {
                const opt = document.createElement('option');
                opt.value = cat.id;
                opt.textContent = cat.nome;
                selCat.appendChild(opt);
            });
            categoriasCarregadas = true;
        }
    } catch (e) {
        console.error('Erro ao carregar opções para o formulário de transação', e);
    }
}

function limparErros() {
    const alerta = document.getElementById('alertaFormTransacao');
    if (alerta) {
        alerta.textContent = '';
        alerta.style.display = 'none';
    }
    document.querySelectorAll('#formTransacao .is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.querySelectorAll('#formTransacao .invalid-feedback').forEach(el => {
        el.textContent = '';
    });
}

function exibirErros(errosCampos, erroGeral) {
    limparErros();
    if (erroGeral) {
        const alerta = document.getElementById('alertaFormTransacao');
        if (alerta) {
            alerta.textContent = erroGeral;
            alerta.style.display = 'block';
        }
    }
    if (errosCampos) {
        Object.entries(errosCampos).forEach(([campo, mensagem]) => {
            const idCapitalizado = campo.charAt(0).toUpperCase() + campo.slice(1);
            const input = document.getElementById('transacao' + idCapitalizado);
            const erroFeedback = document.getElementById('erro' + idCapitalizado);
            if (input) input.classList.add('is-invalid');
            if (erroFeedback) erroFeedback.textContent = mensagem;
        });
    }
}

export async function abrirCadastro() {
    limparErros();
    form()?.reset();
    document.getElementById('transacaoId').value = '';
    document.getElementById('tituloModalTransacao').textContent = cfg().labelNovo || 'Nova transação';

    document.getElementById('avisoTransacaoFatura').style.display = 'none';
    document.getElementById('avisoTransacaoOpenFinance').style.display = 'none';
    document.getElementById('transacaoContaId').disabled = false;
    document.getElementById('transacaoValor').disabled = false;
    document.getElementById('transacaoNatureza').disabled = false;

    await carregarOpcoesFormulario();

    const hoje = new Date().toISOString().slice(0, 10);
    const compPadrao = cfg().competenciaPadrao || hoje.slice(0, 7);
    document.getElementById('transacaoCompetencia').value = compPadrao;
    document.getElementById('transacaoDataLancamento').value = hoje;
    definirValorMoeda(document.getElementById('transacaoValor'), 0, false);

    abrirModal('modalTransacao');
}

export async function abrirEdicao(id) {
    limparErros();
    form()?.reset();
    document.getElementById('transacaoId').value = id;
    document.getElementById('tituloModalTransacao').textContent = cfg().labelEditar || 'Editar transação';

    await carregarOpcoesFormulario();

    try {
        const url = `${cfg().urlBuscar}/${id}`;
        const dados = await getJson(url);

        document.getElementById('transacaoCompetencia').value = dados.competencia || '';
        document.getElementById('transacaoDataLancamento').value = dados.dataLancamento || '';
        document.getElementById('transacaoContaId').value = dados.contaId || '';
        document.getElementById('transacaoNatureza').value = dados.natureza || 'DEBITO';
        document.getElementById('transacaoCategoriaId').value = dados.categoriaId || '';
        document.getElementById('transacaoDescricao').value = dados.descricao || '';
        definirValorMoeda(document.getElementById('transacaoValor'), dados.valor, false);

        const avisoFatura = document.getElementById('avisoTransacaoFatura');
        const avisoOf = document.getElementById('avisoTransacaoOpenFinance');
        const campoConta = document.getElementById('transacaoContaId');
        const campoValor = document.getElementById('transacaoValor');

        if (dados.pagamentoFatura) {
            avisoFatura.style.display = 'flex';
            campoConta.disabled = true;
            campoValor.disabled = true;
        } else {
            avisoFatura.style.display = 'none';
            campoConta.disabled = false;
            campoValor.disabled = false;
        }

        if (dados.origem === 'OPEN_FINANCE') {
            avisoOf.style.display = 'flex';
            campoConta.disabled = true;
        } else {
            avisoOf.style.display = 'none';
            if (!dados.pagamentoFatura) {
                campoConta.disabled = false;
            }
        }

        abrirModal('modalTransacao');
    } catch (e) {
        console.error('Erro ao buscar transação para edição', e);
        toast('Erro ao buscar dados da transação.', true);
    }
}

export async function excluir(id) {
    const confirmMsg = cfg().msgConfirmaExclusao || 'Deseja realmente excluir esta transação bancária?';
    if (!confirm(confirmMsg)) return;

    try {
        const resp = await enviar(`${cfg().urlExcluir}/${id}`, 'DELETE');
        if (resp && resp.sucesso) {
            toast(resp.mensagem || 'Transação excluída com sucesso.');
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            toast(resp?.mensagem || 'Erro ao excluir transação.', true);
        }
    } catch (e) {
        console.error('Erro ao excluir transação', e);
        toast('Erro de comunicação ao excluir transação.', true);
    }
}

export function abrirDuplicar(id, compOrigem) {
    const formDup = document.getElementById('formDuplicarTransacao');
    formDup?.reset();
    document.getElementById('duplicarTransacaoId').value = id;

    const alerta = document.getElementById('alertaDuplicarTransacao');
    if (alerta) {
        alerta.textContent = '';
        alerta.style.display = 'none';
    }
    document.getElementById('duplicarNovaCompetencia')?.classList.remove('is-invalid');
    const erroComp = document.getElementById('erroDuplicarCompetencia');
    if (erroComp) erroComp.textContent = '';

    // Projeta próximo mês
    let proxima = '';
    if (compOrigem && compOrigem.includes('-')) {
        const [ano, mes] = compOrigem.split('-').map(Number);
        const d = new Date(ano, mes, 1);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        proxima = `${y}-${m}`;
    }
    const inputComp = document.getElementById('duplicarNovaCompetencia');
    if (inputComp) inputComp.value = proxima;

    abrirModal('modalDuplicarTransacao');
}

export function inicializarForm() {
    const campoValor = document.getElementById('transacaoValor');
    if (campoValor) {
        campoValor.addEventListener('input', () => formatarElemento(campoValor, false));
    }

    const formEl = form();
    if (formEl) {
        formEl.addEventListener('submit', async (e) => {
            e.preventDefault();
            limparErros();

            const id = document.getElementById('transacaoId').value;
            const isEdicao = !!id;

            const valorFormatado = document.getElementById('transacaoValor').value;
            const valorNumerico = parseDecimal(valorFormatado);

            const params = new URLSearchParams();
            if (isEdicao) params.append('id', id);
            params.append('competencia', document.getElementById('transacaoCompetencia').value);
            params.append('dataLancamento', document.getElementById('transacaoDataLancamento').value);
            params.append('contaId', document.getElementById('transacaoContaId').value);
            params.append('natureza', document.getElementById('transacaoNatureza').value);
            params.append('descricao', document.getElementById('transacaoDescricao').value);
            params.append('valor', valorNumerico);

            const catId = document.getElementById('transacaoCategoriaId').value;
            if (catId) params.append('categoriaId', catId);

            const url = isEdicao ? `${cfg().urlEditar}/${id}` : cfg().urlInserir;
            const metodo = isEdicao ? 'PUT' : 'POST';

            try {
                const resp = await enviar(url, metodo, params);
                if (resp && resp.sucesso) {
                    fecharModal('modalTransacao');
                    toast(resp.mensagem || 'Transação salva com sucesso.');
                    document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
                } else if (resp && resp.errosCampos) {
                    exibirErros(resp.errosCampos, resp.mensagem);
                } else {
                    exibirErros(null, resp?.mensagem || 'Erro ao processar requisição.');
                }
            } catch (err) {
                console.error('Erro ao salvar transação', err);
                exibirErros(null, 'Erro de comunicação ao salvar transação.');
            }
        });
    }

    const formDup = document.getElementById('formDuplicarTransacao');
    if (formDup) {
        formDup.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('duplicarTransacaoId').value;
            const novaCompetencia = document.getElementById('duplicarNovaCompetencia').value;
            const novaData = document.getElementById('duplicarNovaData').value;

            const params = new URLSearchParams();
            if (novaCompetencia) params.append('novaCompetencia', novaCompetencia);
            if (novaData) params.append('novaData', novaData);

            try {
                const resp = await enviar(`${cfg().urlDuplicar}/${id}`, 'POST', params);
                if (resp && resp.sucesso) {
                    fecharModal('modalDuplicarTransacao');
                    toast(resp.mensagem || 'Transação duplicada com sucesso.');
                    document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
                } else {
                    const alerta = document.getElementById('alertaDuplicarTransacao');
                    if (alerta) {
                        alerta.textContent = resp?.mensagem || 'Erro ao duplicar transação.';
                        alerta.style.display = 'block';
                    }
                }
            } catch (err) {
                console.error('Erro ao duplicar transação', err);
                const alerta = document.getElementById('alertaDuplicarTransacao');
                if (alerta) {
                    alerta.textContent = 'Erro de comunicação ao duplicar transação.';
                    alerta.style.display = 'block';
                }
            }
        });
    }
}
