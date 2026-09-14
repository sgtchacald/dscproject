import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { parseDecimal, definirValorMoeda } from '../comum/mascara.js';

export const EVENTO_ALTERADO = 'cartao:alterado';

const cfg = () => document.getElementById('dadosTelaCartao').dataset;
const form = () => document.getElementById('formCartao');

let contasCarregadas = false;

export async function carregarContasForm() {
    const select = document.getElementById('cartaoContaId');
    if (!select) return;

    try {
        const opcoes = await getJson(cfg().urlContasOpcoes);
        select.innerHTML = `<option value="">${cfg().labelContaNenhuma || 'Nenhuma'}</option>`;
        opcoes.forEach(conta => {
            const opt = document.createElement('option');
            opt.value = conta.id;
            opt.textContent = conta.descricao;
            select.appendChild(opt);
        });
        contasCarregadas = true;
    } catch (e) {
        console.error('Erro ao carregar contas no formulário do cartão', e);
    }
}

function limparErros() {
    const f = form();
    if (!f) return;
    f.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach(el => (el.textContent = ''));
    const alerta = document.getElementById('alertaFormCartao');
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

    if (!contasCarregadas) {
        await carregarContasForm();
    }

    document.getElementById('cartaoId').value = '';
    document.getElementById('tituloModalCartao').textContent = cfg().labelNovo || 'Novo cartão';
    document.getElementById('grupoAtivoCartao').style.display = 'none';
    document.getElementById('avisoCartao').style.display = 'none';

    abrirModal('modalCartao');
}

export async function abrirEdicao(id) {
    limparErros();
    if (!contasCarregadas) {
        await carregarContasForm();
    }

    try {
        const dados = await getJson(`${cfg().urlBuscar}/${id}`);

        document.getElementById('cartaoId').value = dados.id;
        document.getElementById('tituloModalCartao').textContent = cfg().labelEditar || 'Editar cartão';
        document.getElementById('cartaoDescricao').value = dados.descricao || '';
        document.getElementById('cartaoBandeira').value = dados.bandeira || '';
        document.getElementById('cartaoFinalCartao').value = dados.finalCartao || '';
        definirValorMoeda(document.getElementById('cartaoLimite'), dados.limite);
        document.getElementById('cartaoDiaFechamento').value = dados.diaFechamento != null ? dados.diaFechamento : '';
        document.getElementById('cartaoDiaVencimento').value = dados.diaVencimento != null ? dados.diaVencimento : '';
        document.getElementById('cartaoContaId').value = dados.contaId || '';

        document.getElementById('cartaoAtivo').checked = !!dados.ativo;
        document.getElementById('grupoAtivoCartao').style.display = 'block';

        const aviso = document.getElementById('avisoCartao');
        const avisoTxt = document.getElementById('avisoCartaoTexto');
        if (dados.qtdVinculos > 0) {
            avisoTxt.textContent = (cfg().labelAvisoEmUso || 'Este cartão possui {0} fatura(s)/despesa(s) vinculada(s). Ele não pode ser excluído; você pode desativá-lo.').replace('{0}', dados.qtdVinculos);
            aviso.style.display = 'flex';
        } else {
            aviso.style.display = 'none';
        }

        abrirModal('modalCartao');
    } catch (err) {
        toast('Erro ao carregar dados do cartão.', true);
    }
}

export async function excluir(id, descricao) {
    const template = cfg().msgConfirmaExclusao || 'Confirma a exclusão do cartão "{0}"?';
    const msg = template.replace('{0}', descricao);
    if (!confirm(msg)) {
        return;
    }

    try {
        const resp = await enviar(`${cfg().urlExcluir}/${id}`, 'DELETE');
        if (resp.sucesso) {
            toast(resp.mensagem || 'Cartão excluído com sucesso.', false);
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            const erro = resp.mensagem || (resp.errosNegocio && resp.errosNegocio['geral']) || 'Não foi possível excluir o cartão.';
            const ehEmUso = erro && erro.includes('vinculad');
            if (ehEmUso) {
                const oferta = cfg().msgDesativarOferta || 'Deseja desativar este cartão agora?';
                if (confirm(`${erro}\n\n${oferta}`)) {
                    await desativar(id);
                }
            } else {
                toast(erro, true);
            }
        }
    } catch (e) {
        toast('Erro de comunicação ao excluir cartão.', true);
    }
}

export async function desativar(id) {
    try {
        const dados = await getJson(`${cfg().urlBuscar}/${id}`);

        const body = new URLSearchParams();
        body.append('id', dados.id);
        body.append('descricao', dados.descricao || '');
        body.append('bandeira', dados.bandeira || '');
        body.append('finalCartao', dados.finalCartao || '');
        body.append('limite', dados.limite != null ? dados.limite : '');
        body.append('diaFechamento', dados.diaFechamento != null ? dados.diaFechamento : '');
        body.append('diaVencimento', dados.diaVencimento != null ? dados.diaVencimento : '');
        body.append('contaId', dados.contaId || '');
        body.append('ativo', 'false');

        const resp = await enviar(`${cfg().urlEditar}/${id}`, 'PUT', body);
        if (resp.sucesso) {
            toast(resp.mensagem || 'Cartão desativado com sucesso.', false);
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            toast(resp.mensagem || 'Erro ao desativar cartão.', true);
        }
    } catch (e) {
        toast('Erro de comunicação ao desativar cartão.', true);
    }
}

export function inicializarForm() {
    const f = form();
    if (!f) return;

    const btnNovo = document.getElementById('btnNovoCartao');
    if (btnNovo) {
        btnNovo.addEventListener('click', function (e) {
            e.preventDefault();
            abrirNovo();
        });
    }

    const finalCartao = document.getElementById('cartaoFinalCartao');
    if (finalCartao) {
        finalCartao.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 4);
        });
    }

    ['cartaoDiaFechamento', 'cartaoDiaVencimento'].forEach(id => {
        const campo = document.getElementById(id);
        if (!campo) return;
        campo.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '');
            if (v !== '') {
                let n = Number(v);
                if (n > 31) n = 31;
                v = String(n);
            }
            this.value = v;
        });
    });

    f.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErros();

        const id = document.getElementById('cartaoId').value;
        const ehEdicao = !!id;
        const url = ehEdicao ? `${cfg().urlEditar}/${id}` : cfg().urlInserir;
        const metodo = ehEdicao ? 'PUT' : 'POST';

        const body = new URLSearchParams();
        if (ehEdicao) {
            body.append('id', id);
        }
        body.append('descricao', document.getElementById('cartaoDescricao').value || '');
        body.append('bandeira', document.getElementById('cartaoBandeira').value || '');
        body.append('finalCartao', document.getElementById('cartaoFinalCartao').value || '');
        const limiteStr = parseDecimal(document.getElementById('cartaoLimite').value).toFixed(2);
        body.append('limite', limiteStr);
        body.append('diaFechamento', document.getElementById('cartaoDiaFechamento').value || '');
        body.append('diaVencimento', document.getElementById('cartaoDiaVencimento').value || '');
        body.append('contaId', document.getElementById('cartaoContaId').value || '');

        if (ehEdicao) {
            body.append('ativo', document.getElementById('cartaoAtivo').checked);
        }

        try {
            const resp = await enviar(url, metodo, body);
            if (resp.sucesso) {
                fecharModal('modalCartao');
                toast(resp.mensagem || 'Operação realizada com sucesso.', false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                aplicarErros(resp);
            }
        } catch (err) {
            toast('Erro de comunicação ao salvar cartão.', true);
        }
    });
}

function aplicarErros(resp) {
    if (resp.errosCampos) {
        for (const [campo, msg] of Object.entries(resp.errosCampos)) {
            const input = document.getElementById(`cartao${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            if (input) input.classList.add('is-invalid');
            if (divErro) divErro.textContent = msg;
        }
    }
    if (resp.errosNegocio) {
        for (const [campo, msg] of Object.entries(resp.errosNegocio)) {
            if (campo === 'geral') {
                const alerta = document.getElementById('alertaFormCartao');
                if (alerta) {
                    alerta.textContent = msg;
                    alerta.style.display = 'block';
                }
            } else {
                const input = document.getElementById(`cartao${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                if (input) input.classList.add('is-invalid');
                if (divErro) divErro.textContent = msg;
            }
        }
    }
}
