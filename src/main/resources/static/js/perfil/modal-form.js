import { getJson, enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';

// Emitido após inserir, editar ou excluir; listar.js recarrega o grid ao ouvir.
export const EVENTO_ALTERADO = 'perfil:alterado';

const cfg = () => document.getElementById('dadosTelaPerfil').dataset;
const form = () => document.getElementById('formPerfil');
const seletor = () => document.getElementById('seletorPermissoes');

let catalogo = [];
let perfilEmEdicao = null;

function limparForm() {
    const f = form();
    f.reset();
    f.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach((el) => (el.textContent = ''));
    document.getElementById('alertaFormPerfil').style.display = 'none';
    document.getElementById('perfCodigo').disabled = false;
}

async function garantirCatalogo() {
    if (catalogo.length === 0) {
        catalogo = await getJson(cfg().urlPermissoes);
    }
    return catalogo;
}

function agrupadoPorModulo(itens) {
    const grupos = new Map();
    itens.forEach((p) => {
        if (!grupos.has(p.modulo)) grupos.set(p.modulo, []);
        grupos.get(p.modulo).push(p);
    });
    return grupos;
}

function renderSeletor(marcadas) {
    const marcadasSet = new Set(marcadas || []);
    const alvo = seletor();
    alvo.innerHTML = '';

    agrupadoPorModulo(catalogo).forEach((permissoes, modulo) => {
        const grupo = document.createElement('div');
        grupo.className = 'mb-3';
        grupo.dataset.modulo = modulo;

        const cabecalho = document.createElement('label');
        cabecalho.className = 'form-check fw-bold';
        cabecalho.innerHTML = '<input class="form-check-input" type="checkbox" data-marcar-todos>'
            + '<span class="form-check-label">' + modulo + ' — ' + (cfg().labelMarcarTodos || 'marcar todos') + '</span>';
        grupo.appendChild(cabecalho);

        permissoes.forEach((p) => {
            const linha = document.createElement('label');
            linha.className = 'form-check ms-3';
            const marcada = marcadasSet.has(p.codigo);
            // órfã: editável só quando o perfil já a concede (permite desmarcar/remover, nunca marcar de novo)
            const desabilitada = p.orfa && !marcada;
            const selos = (p.orfa ? ' <span class="text-warning">' + (cfg().labelOrfa || '(órfã)') + '</span>' : '')
                + (p.concedivelPorPlano ? ' <span class="badge bg-azure-lt">' + (cfg().labelPorPlano || 'concedível por plano') + '</span>' : '');
            linha.innerHTML = '<input class="form-check-input" type="checkbox"'
                + ' data-codigo="' + p.codigo + '"'
                + (p.orfa ? ' data-orfa="true"' : '')
                + (marcada ? ' checked' : '')
                + (desabilitada ? ' disabled' : '')
                + '><span class="form-check-label">' + p.nome + selos + '</span>';
            grupo.appendChild(linha);
        });

        alvo.appendChild(grupo);
    });

    alvo.querySelectorAll('input[data-marcar-todos]').forEach((cb) => {
        cb.addEventListener('change', function () {
            const grupo = cb.closest('[data-modulo]');
            // "marcar todos" nunca reativa uma permissão órfã
            grupo.querySelectorAll('input[data-codigo]:not([data-orfa])').forEach((item) => {
                item.checked = cb.checked;
            });
            atualizarContador();
        });
    });

    alvo.querySelectorAll('input[data-codigo]').forEach((cb) => {
        cb.addEventListener('change', function () {
            if (!aplicarTravaProprioPerfil(cb)) return;
            sincronizarMarcarTodos(cb.closest('[data-modulo]'));
            atualizarContador();
        });
    });

    alvo.querySelectorAll('[data-modulo]').forEach(sincronizarMarcarTodos);
    atualizarContador();
}

// o usuário não pode desmarcar a permissão de gerir perfis do próprio perfil
function aplicarTravaProprioPerfil(cb) {
    const ehProprioPerfil = perfilEmEdicao && perfilEmEdicao.codigo
        && perfilEmEdicao.codigo === cfg().perfilLogado;
    if (ehProprioPerfil && cb.dataset.codigo === 'PERFIS_VINCULAR_PERMISSAO' && !cb.checked) {
        cb.checked = true;
        toast(cfg().msgAntilockoutProprio, true);
        return false;
    }
    return true;
}

function sincronizarMarcarTodos(grupo) {
    // o tri-state considera só as permissões não-órfãs do módulo
    const itens = Array.from(grupo.querySelectorAll('input[data-codigo]:not([data-orfa])'));
    const marcados = itens.filter((i) => i.checked).length;
    const todos = grupo.querySelector('input[data-marcar-todos]');
    todos.checked = itens.length > 0 && marcados === itens.length;
    todos.indeterminate = marcados > 0 && marcados < itens.length;
}

function codigosMarcados() {
    return Array.from(seletor().querySelectorAll('input[data-codigo]:checked')).map((i) => i.dataset.codigo);
}

function atualizarContador() {
    const n = codigosMarcados().length;
    const tpl = cfg().labelContador || '{n} de {total} permissões selecionadas';
    document.getElementById('contadorPermissoes').textContent = tpl.replace('{n}', n).replace('{total}', catalogo.length);
}

export async function abrirNovo() {
    limparForm();
    perfilEmEdicao = null;
    document.getElementById('perfId').value = '';
    document.getElementById('modalPerfilTitulo').textContent = cfg().labelNovo || 'Novo perfil';
    await garantirCatalogo();
    renderSeletor([]);
}

export async function abrirEdicao(id) {
    limparForm();
    await garantirCatalogo();
    const perfil = await getJson(cfg().urlBuscar + '/' + id);
    perfilEmEdicao = perfil;
    document.getElementById('perfId').value = perfil.id;
    document.getElementById('perfCodigo').value = perfil.codigo;
    document.getElementById('perfCodigo').disabled = perfil.sistema;
    document.getElementById('perfNome').value = perfil.nome;
    document.getElementById('perfDescricao').value = perfil.descricao || '';
    document.getElementById('modalPerfilTitulo').textContent = cfg().labelEditar || 'Editar perfil';
    renderSeletor(perfil.permissoes || []);
    abrirModal('modalPerfil');
}

function aplicarErros(erros) {
    Object.entries(erros || {}).forEach(([campo, msg]) => {
        const input = form().querySelector('[name="' + campo + '"]');
        const feedback = document.getElementById('erro-' + campo);
        if (input) input.classList.add('is-invalid');
        if (feedback) feedback.textContent = msg;
        if (campo === 'geral') {
            const a = document.getElementById('alertaFormPerfil');
            a.textContent = msg;
            a.style.display = 'block';
        }
    });
}

async function submeter(e) {
    e.preventDefault();
    const f = form();
    f.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach((el) => (el.textContent = ''));
    document.getElementById('alertaFormPerfil').style.display = 'none';

    const id = document.getElementById('perfId').value;
    const params = new URLSearchParams();
    params.append('codigo', document.getElementById('perfCodigo').value);
    params.append('nome', document.getElementById('perfNome').value);
    params.append('descricao', document.getElementById('perfDescricao').value);
    codigosMarcados().forEach((c) => params.append('permissoes', c));

    const url = id ? (cfg().urlEditar + '/' + id) : cfg().urlInserir;
    try {
        const data = await enviar(url, id ? 'PUT' : 'POST', params);
        if (data.sucesso) {
            fecharModal('modalPerfil');
            toast(data.mensagem || 'OK');
            document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
        } else {
            aplicarErros(data.errosCampos);
            aplicarErros(data.errosNegocio);
        }
    } catch (err) {
        const a = document.getElementById('alertaFormPerfil');
        a.textContent = cfg().erroComunicacao;
        a.style.display = 'block';
    }
}

// código sempre em maiúsculas
document.getElementById('perfCodigo').addEventListener('input', function () {
    const pos = this.selectionStart;
    this.value = this.value.toUpperCase();
    this.setSelectionRange(pos, pos);
});

form().addEventListener('submit', submeter);
