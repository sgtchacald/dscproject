import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { parseDecimal, definirValorMoeda } from '../comum/mascara.js';

export const EVENTO_ALTERADO = 'receita:alterada';

const cfg = () => document.getElementById('dadosTelaReceita').dataset;
const form = () => document.getElementById('formReceita');

let opcoesCarregadas = false;
let competenciaEditadaManualmente = false;

function paraInputMonth(competencia) {
    return competencia || '';
}

function mesDaData(dataIso) {
    return dataIso ? dataIso.slice(0, 7) : '';
}

async function carregarOpcoesForm() {
    if (opcoesCarregadas) return;

    const selectConta = document.getElementById('receitaContaId');
    const selectCategoria = document.getElementById('receitaCategoriaId');

    try {
        const [contas, categorias] = await Promise.all([
            getJson(cfg().urlContasOpcoes),
            getJson(cfg().urlCategoriasOpcoes)
        ]);

        contas.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = c.descricao;
            selectConta.appendChild(opt);
        });

        categorias.forEach(cat => {
            const opt = document.createElement('option');
            opt.value = cat.id;
            opt.textContent = cat.nome;
            selectCategoria.appendChild(opt);
        });

        opcoesCarregadas = true;
    } catch (e) {
        console.error('Erro ao carregar opções de conta/categoria no formulário de receita', e);
    }
}

function limparErros() {
    const f = form();
    if (!f) return;
    f.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach(el => (el.textContent = ''));
    const alerta = document.getElementById('alertaFormReceita');
    if (alerta) {
        alerta.style.display = 'none';
        alerta.textContent = '';
    }
}

function aplicarVisibilidadeDataRecebimento() {
    const marcado = document.getElementById('receitaRecebido').checked;
    const grupo = document.getElementById('grupoDataRecebimento');
    const campoData = document.getElementById('receitaDataRecebimento');
    if (marcado) {
        grupo.style.display = 'block';
        if (!campoData.value) {
            campoData.value = new Date().toISOString().slice(0, 10);
        }
    } else {
        grupo.style.display = 'none';
        campoData.value = '';
    }
}

export async function abrirNovo() {
    const f = form();
    if (!f) return;
    f.reset();
    limparErros();

    await carregarOpcoesForm();

    document.getElementById('receitaId').value = '';
    document.getElementById('tituloModalReceita').textContent = cfg().labelNovo || 'Nova receita';
    document.getElementById('avisoReceitaImportada').style.display = 'none';
    document.getElementById('receitaContaId').disabled = false;

    const hoje = new Date().toISOString().slice(0, 10);
    document.getElementById('receitaDataLancamento').value = hoje;
    document.getElementById('receitaCompetencia').value = mesDaData(hoje);
    competenciaEditadaManualmente = false;

    document.getElementById('receitaRecebido').checked = false;
    aplicarVisibilidadeDataRecebimento();

    abrirModal('modalReceita');
}

export async function abrirEdicao(id) {
    limparErros();
    await carregarOpcoesForm();

    try {
        const dados = await getJson(`${cfg().urlBuscar}/${id}`);

        document.getElementById('receitaId').value = dados.id;
        document.getElementById('tituloModalReceita').textContent = cfg().labelEditar || 'Editar receita';
        document.getElementById('receitaNome').value = dados.nome || '';
        document.getElementById('receitaDescricao').value = dados.descricao || '';
        definirValorMoeda(document.getElementById('receitaValor'), dados.valor);
        document.getElementById('receitaDataLancamento').value = dados.dataLancamento || '';
        document.getElementById('receitaCompetencia').value = dados.competencia || '';
        competenciaEditadaManualmente = true; // não reajustar a competência já gravada ao reabrir em edição

        const selectConta = document.getElementById('receitaContaId');
        if (dados.contaId && !Array.from(selectConta.options).some(o => o.value == dados.contaId)) {
            const opt = document.createElement('option');
            opt.value = dados.contaId;
            opt.textContent = 'Conta vinculada';
            selectConta.appendChild(opt);
        }
        selectConta.value = dados.contaId || '';

        document.getElementById('receitaCategoriaId').value = dados.categoriaId || '';

        const ehImportada = dados.origem && dados.origem !== 'MANUAL';
        selectConta.disabled = ehImportada;
        document.getElementById('avisoReceitaImportada').style.display = ehImportada ? 'flex' : 'none';

        document.getElementById('receitaRecebido').checked = !!dados.recebido;
        document.getElementById('receitaDataRecebimento').value = dados.dataRecebimento || '';
        aplicarVisibilidadeDataRecebimento();
        if (dados.dataRecebimento) {
            document.getElementById('receitaDataRecebimento').value = dados.dataRecebimento;
        }

        abrirModal('modalReceita');
    } catch (err) {
        toast('Erro ao carregar dados da receita.', true);
    }
}

export async function excluir(id, nome) {
    const template = cfg().msgConfirmaExclusao || 'Confirma a exclusão da receita "{0}"?';
    const msg = template.replace('{0}', nome);
    if (!confirm(msg)) {
        return;
    }

    try {
        const resp = await enviar(`${cfg().urlExcluir}/${id}`, 'DELETE');
        if (resp.sucesso) {
            toast(resp.mensagem || 'Receita excluída com sucesso.', false);
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            toast(resp.mensagem || 'Não foi possível excluir a receita.', true);
        }
    } catch (e) {
        toast('Erro de comunicação ao excluir receita.', true);
    }
}

function corpoFormulario() {
    const body = new URLSearchParams();
    const id = document.getElementById('receitaId').value;
    if (id) body.append('id', id);
    body.append('nome', document.getElementById('receitaNome').value || '');
    body.append('descricao', document.getElementById('receitaDescricao').value || '');
    let valorStr = parseDecimal(document.getElementById('receitaValor').value).toFixed(2);
    body.append('valor', valorStr);
    body.append('dataLancamento', document.getElementById('receitaDataLancamento').value || '');
    body.append('competencia', document.getElementById('receitaCompetencia').value || '');
    body.append('contaId', document.getElementById('receitaContaId').value || '');
    body.append('categoriaId', document.getElementById('receitaCategoriaId').value || '');
    body.append('recebido', document.getElementById('receitaRecebido').checked);
    body.append('dataRecebimento', document.getElementById('receitaDataRecebimento').value || '');
    return body;
}

export function inicializarForm() {
    const f = form();
    if (!f) return;

    const btnNovo = document.getElementById('btnNovaReceita');
    if (btnNovo) {
        btnNovo.addEventListener('click', function (e) {
            e.preventDefault();
            abrirNovo();
        });
    }

    document.getElementById('receitaRecebido').addEventListener('change', aplicarVisibilidadeDataRecebimento);

    const marcarCompetenciaEditada = () => {
        competenciaEditadaManualmente = true;
    };
    document.getElementById('receitaCompetencia').addEventListener('input', marcarCompetenciaEditada);
    document.getElementById('receitaCompetencia').addEventListener('change', marcarCompetenciaEditada);

    const modalEl = document.getElementById('modalReceita');
    if (modalEl) {
        modalEl.addEventListener('shown.bs.modal', function () {
            const input = document.getElementById('receitaCompetencia');
            if (input) input.focus();
        });
    }

    document.getElementById('receitaDataLancamento').addEventListener('change', function () {
        if (!competenciaEditadaManualmente) {
            document.getElementById('receitaCompetencia').value = mesDaData(this.value);
        }
    });

    f.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErros();

        const id = document.getElementById('receitaId').value;
        const ehEdicao = !!id;
        const url = ehEdicao ? `${cfg().urlEditar}/${id}` : cfg().urlInserir;
        const metodo = ehEdicao ? 'PUT' : 'POST';

        try {
            const resp = await enviar(url, metodo, corpoFormulario());
            if (resp.sucesso) {
                fecharModal('modalReceita');
                toast(resp.mensagem || 'Operação realizada com sucesso.', false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                aplicarErros(resp);
            }
        } catch (err) {
            toast('Erro de comunicação ao salvar receita.', true);
        }
    });
}

function aplicarErros(resp) {
    if (resp.errosCampos) {
        for (const [campo, msg] of Object.entries(resp.errosCampos)) {
            const input = document.getElementById(`receita${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
            if (input) input.classList.add('is-invalid');
            if (divErro) divErro.textContent = msg;
        }
    }
    if (resp.errosNegocio) {
        for (const [campo, msg] of Object.entries(resp.errosNegocio)) {
            if (campo === 'geral') {
                const alerta = document.getElementById('alertaFormReceita');
                if (alerta) {
                    alerta.textContent = msg;
                    alerta.style.display = 'block';
                }
            } else {
                const input = document.getElementById(`receita${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                const divErro = document.getElementById(`erro${campo.charAt(0).toUpperCase() + campo.slice(1)}`);
                if (input) input.classList.add('is-invalid');
                if (divErro) divErro.textContent = msg;
            }
        }
    }
}
