import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { parseDecimal } from '../comum/mascara.js';

export const EVENTO_ALTERADO = 'conta:alterada';

const cfg = () => document.getElementById('dadosTelaConta').dataset;
const form = () => document.getElementById('formConta');

let instituicoesCarregadas = false;

export async function carregarInstituicoesForm() {
    const select = document.getElementById('contaInstituicaoId');
    if (!select) return;

    try {
        const opcoes = await getJson(cfg().urlInstituicoesOpcoes);
        select.innerHTML = `<option value="">Selecione…</option>`;
        opcoes.forEach(inst => {
            const opt = document.createElement('option');
            opt.value = inst.id;
            opt.textContent = inst.nome;
            select.appendChild(opt);
        });
        instituicoesCarregadas = true;
    } catch (e) {
        console.error('Erro ao carregar instituições no formulário', e);
    }
}

function limparErros() {
    const f = form();
    if (!f) return;
    f.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach(el => (el.textContent = ''));
    const alerta = document.getElementById('alertaFormConta');
    if (alerta) {
        alerta.style.display = 'none';
        alerta.textContent = '';
    }
}

export async function abrirNovo() {
    const f = form();
    if (!f) return;
    f.reset();
    limparErros();

    if (!instituicoesCarregadas) {
        await carregarInstituicoesForm();
    }

    document.getElementById('contaId').value = '';
    document.getElementById('tituloModalConta').textContent = cfg().labelNovo || 'Nova conta';
    document.getElementById('contaConsideraSaldo').checked = true;
    document.getElementById('contaMoeda').value = 'BRL';

    // Campos de criação
    document.getElementById('contaInstituicaoId').disabled = false;
    document.getElementById('hintInstituicao').style.display = 'none';
    document.getElementById('grupoSaldoInicial').style.display = 'block';
    document.getElementById('contaSaldoInicial').value = '0,00';
    document.getElementById('grupoAtivaConta').style.display = 'none';
    document.getElementById('avisoConta').style.display = 'none';

    abrirModal('modalConta');
}

export async function abrirEdicao(id) {
    limparErros();
    if (!instituicoesCarregadas) {
        await carregarInstituicoesForm();
    }

    try {
        const dados = await getJson(`${cfg().urlBuscar}/${id}`);

        document.getElementById('contaId').value = dados.id;
        document.getElementById('tituloModalConta').textContent = cfg().labelEditar || 'Editar conta';
        document.getElementById('contaDescricao').value = dados.descricao || '';

        // Se a instituição não estiver nas ativas (ex: desativada), adiciona temporariamente para seleção
        const selectInst = document.getElementById('contaInstituicaoId');
        if (dados.instituicaoId && !Array.from(selectInst.options).some(o => o.value == dados.instituicaoId)) {
            const opt = document.createElement('option');
            opt.value = dados.instituicaoId;
            opt.textContent = dados.instituicaoNome || 'Instituição Vinculada';
            selectInst.appendChild(opt);
        }
        selectInst.value = dados.instituicaoId || '';
        selectInst.disabled = true;
        document.getElementById('hintInstituicao').style.display = 'block';

        document.getElementById('contaTipo').value = dados.tipo || '';
        document.getElementById('contaMoeda').value = dados.moeda || 'BRL';
        document.getElementById('contaAgencia').value = dados.agencia || '';
        document.getElementById('contaNumero').value = dados.numero || '';
        document.getElementById('contaNomeGerente').value = dados.nomeGerente || '';
        document.getElementById('contaTelGerente').value = dados.telGerente || '';
        document.getElementById('contaConsideraSaldo').checked = !!dados.consideraSaldo;

        // Saldo inicial não é editável na tela de edição (usa ajuste de saldo)
        document.getElementById('grupoSaldoInicial').style.display = 'none';

        const chkAtiva = document.getElementById('contaAtiva');
        if (chkAtiva) {
            chkAtiva.checked = !!dados.ativo;
        }
        document.getElementById('grupoAtivaConta').style.display = 'block';

        const aviso = document.getElementById('avisoConta');
        const avisoTxt = document.getElementById('avisoContaTexto');
        if (dados.qtdUso > 0) {
            avisoTxt.textContent = (cfg().labelAvisoEmUso || 'Esta conta possui lançamentos ou vínculos ativos. Ela não pode ser excluída, apenas desativada.').replace('{0}', dados.qtdUso);
            aviso.style.display = 'flex';
        } else {
            aviso.style.display = 'none';
        }

        abrirModal('modalConta');
    } catch (err) {
        toast('Erro ao carregar dados da conta.', true);
    }
}

export async function excluir(id, descricao) {
    const template = cfg().msgConfirmaExclusao || 'Confirma a exclusão da conta "{0}"?';
    const msg = template.replace('{0}', descricao);
    if (!confirm(msg)) {
        return;
    }

    try {
        const resp = await enviar(`${cfg().urlExcluir}/${id}`, 'DELETE');
        if (resp.sucesso) {
            toast(resp.mensagem || 'Conta excluída com sucesso.', false);
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            const erro = resp.mensagem || (resp.errosNegocio && resp.errosNegocio['geral']) || 'Não foi possível excluir a conta.';
            const ehEmUso = (erro && erro.includes('uso')) || (resp.errosNegocio && resp.errosNegocio['geral'] && resp.errosNegocio['geral'].includes('uso'));
            if (ehEmUso) {
                const oferta = cfg().msgDesativarOferta || 'Deseja desativar esta conta agora?';
                if (confirm(`${erro}\n\n${oferta}`)) {
                    await desativar(id);
                }
            } else {
                toast(erro, true);
            }
        }
    } catch (e) {
        toast('Erro de comunicação ao excluir conta.', true);
    }
}

export async function desativar(id) {
    try {
        const resp = await enviar(`${cfg().urlDesativar}/${id}`, 'PUT');
        if (resp.sucesso) {
            toast(resp.mensagem || 'Conta desativada com sucesso.', false);
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            toast(resp.mensagem || 'Erro ao desativar conta.', true);
        }
    } catch (e) {
        toast('Erro de comunicação ao desativar conta.', true);
    }
}

export function inicializarForm() {
    const f = form();
    if (!f) return;

    const btnNovo = document.getElementById('btnNovaConta');
    if (btnNovo) {
        btnNovo.addEventListener('click', function (e) {
            e.preventDefault();
            abrirNovo();
        });
    }

    f.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErros();

        const id = document.getElementById('contaId').value;
        const ehEdicao = !!id;
        const url = ehEdicao ? `${cfg().urlEditar}/${id}` : cfg().urlInserir;
        const metodo = ehEdicao ? 'PUT' : 'POST';

        const body = new URLSearchParams();
        if (ehEdicao) {
            body.append('id', id);
        }
        body.append('descricao', document.getElementById('contaDescricao').value || '');
        body.append('instituicaoId', document.getElementById('contaInstituicaoId').value || '');
        body.append('tipo', document.getElementById('contaTipo').value || '');
        body.append('moeda', document.getElementById('contaMoeda').value || 'BRL');
        body.append('agencia', document.getElementById('contaAgencia').value || '');
        body.append('numero', document.getElementById('contaNumero').value || '');
        body.append('nomeGerente', document.getElementById('contaNomeGerente').value || '');
        body.append('telGerente', document.getElementById('contaTelGerente').value || '');
        body.append('consideraSaldo', document.getElementById('contaConsideraSaldo').checked);

        if (!ehEdicao) {
            let saldoInicialStr = parseDecimal(document.getElementById('contaSaldoInicial').value).toFixed(2);
            body.append('saldoInicial', saldoInicialStr);
        } else {
            const chk = document.getElementById('contaAtiva');
            body.append('ativo', chk ? chk.checked : true);
        }

        try {
            const resp = await enviar(url, metodo, body);
            if (resp.sucesso) {
                fecharModal('modalConta');
                toast(resp.mensagem || 'Operação realizada com sucesso.', false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                aplicarErros(resp);
            }
        } catch (err) {
            toast('Erro de comunicação ao salvar conta.', true);
        }
    });
}

function aplicarErros(resp) {
    if (resp.errosCampos) {
        for (const [campo, msg] of Object.entries(resp.errosCampos)) {
            const input = document.getElementById(`conta${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            if (input) input.classList.add('is-invalid');
            if (divErro) divErro.textContent = msg;
        }
    }
    if (resp.errosNegocio) {
        for (const [campo, msg] of Object.entries(resp.errosNegocio)) {
            if (campo === 'geral') {
                const alerta = document.getElementById('alertaFormConta');
                if (alerta) {
                    alerta.textContent = msg;
                    alerta.style.display = 'block';
                }
            } else {
                const input = document.getElementById(`conta${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                if (input) input.classList.add('is-invalid');
                if (divErro) divErro.textContent = msg;
            }
        }
    }
}
