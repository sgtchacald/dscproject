import { enviar, getJson } from './comum/http.js';
import { toast, abrirModal, fecharModal } from './comum/ui.js';

// ---------- Minha Conta (Dados e Senha) ----------

const form = document.getElementById('formMinhaConta');
const alerta = document.getElementById('alertaMinhaConta');
const rodapeMinhaConta = document.getElementById('rodapeMinhaConta');

function limpar() {
    if (alerta) alerta.style.display = 'none';
    form.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    form.querySelectorAll('.invalid-feedback').forEach((el) => (el.textContent = ''));
}

function aplicarErros(erros) {
    Object.entries(erros || {}).forEach(([campo, msg]) => {
        const input = form.querySelector('[name="' + campo + '"]');
        const feedback = document.getElementById('erro-' + campo);
        if (input) input.classList.add('is-invalid');
        if (feedback) feedback.textContent = msg;
    });
}

form.addEventListener('submit', async function (e) {
    e.preventDefault();
    limpar();
    const btn = document.getElementById('btnSalvarMinhaConta');
    btn.disabled = true;
    try {
        const data = await enviar(form.dataset.url, 'PUT', new URLSearchParams(new FormData(form)));
        if (data.sucesso) {
            toast(data.mensagem || form.dataset.msgSucesso);
            const senha = document.getElementById('senha');
            const conf = document.getElementById('confirmacaoSenha');
            if (senha) senha.value = '';
            if (conf) conf.value = '';
        } else {
            aplicarErros(data.errosCampos);
            aplicarErros(data.errosNegocio);
        }
    } catch (err) {
        if (alerta) {
            alerta.textContent = form.dataset.erroComunicacao;
            alerta.style.display = 'block';
        }
    } finally {
        btn.disabled = false;
    }
});

// ---------- Controle de Abas ----------

const tabLinks = document.querySelectorAll('.list-group-item[data-bs-toggle="tab"]');
tabLinks.forEach((tab) => {
    tab.addEventListener('shown.bs.tab', (e) => {
        const target = e.target.getAttribute('href');
        if (target === '#tab-redes-sociais') {
            if (rodapeMinhaConta) rodapeMinhaConta.style.display = 'none';
            carregarRedesSociais();
        } else {
            if (rodapeMinhaConta) rodapeMinhaConta.style.display = '';
        }
    });
});

// ---------- Redes Sociais do Usuário ----------

const corpoTabela = document.getElementById('corpoTabelaRedesSociais');
const formRedeSocial = document.getElementById('formRedeSocial');
const btnNovaRedeSocial = document.getElementById('btnNovaRedeSocial');
const tituloModalRedeSocial = document.getElementById('tituloModalRedeSocial');
const btnSalvarRedeSocial = document.getElementById('btnSalvarRedeSocial');

function limparErrosModal() {
    formRedeSocial.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    formRedeSocial.querySelectorAll('.invalid-feedback').forEach((el) => (el.textContent = ''));
}

function aplicarErrosModal(erros) {
    Object.entries(erros || {}).forEach(([campo, msg]) => {
        let input = formRedeSocial.querySelector('[name="' + campo + '"]');
        let feedback = document.getElementById('erro-rede-' + campo);
        if (input) input.classList.add('is-invalid');
        if (feedback) feedback.textContent = msg;
    });
}

function escapeHtml(texto) {
    if (!texto) return '';
    const div = document.createElement('div');
    div.textContent = texto;
    return div.innerHTML;
}

async function carregarRedesSociais() {
    if (!corpoTabela) return;
    corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center text-secondary">Carregando redes sociais...</td></tr>';
    try {
        const redes = await getJson('/minha-conta/redes-sociais');
        if (!redes || redes.length === 0) {
            corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center text-secondary">Nenhuma rede social cadastrada.</td></tr>';
            return;
        }

        corpoTabela.innerHTML = redes.map((r) => {
            const icone = r.tipoIcone || 'ph-globe';
            const desc = escapeHtml(r.tipoDescricao || r.tipo);
            const urlEsc = escapeHtml(r.url);
            const identEsc = r.identificador ? escapeHtml(r.identificador) : '-';
            const statusBadge = r.ativo
                ? '<span class="badge bg-success-lt">Sim</span>'
                : '<span class="badge bg-secondary-lt">Não</span>';

            return `
                <tr>
                    <td>
                        <div class="d-flex align-items-center">
                            <i class="ph ${icone} me-2 fs-4" aria-hidden="true"></i>
                            <strong>${desc}</strong>
                        </div>
                    </td>
                    <td>
                        <a href="${urlEsc}" target="_blank" rel="noopener noreferrer" class="text-truncate d-inline-block" style="max-width: 250px;" title="${urlEsc}">
                            ${urlEsc} <i class="ph ph-arrow-square-out fs-6" aria-hidden="true"></i>
                        </a>
                    </td>
                    <td>${identEsc}</td>
                    <td class="text-center">${statusBadge}</td>
                    <td class="col-acoes text-start">
                        <div class="btn-list flex-nowrap">
                            <button type="button" class="btn btn-sm btn-outline-primary btn-editar-rede" data-id="${r.id}" title="Editar">
                                <i class="ph ph-pencil" aria-hidden="true"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-danger btn-excluir-rede" data-id="${r.id}" data-tipo="${desc}" title="Excluir">
                                <i class="ph ph-trash" aria-hidden="true"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        vincularBotoesAcao();
    } catch (err) {
        corpoTabela.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Erro ao carregar redes sociais.</td></tr>';
    }
}

function vincularBotoesAcao() {
    document.querySelectorAll('.btn-editar-rede').forEach((btn) => {
        btn.addEventListener('click', () => editarRede(btn.dataset.id));
    });
    document.querySelectorAll('.btn-excluir-rede').forEach((btn) => {
        btn.addEventListener('click', () => excluirRede(btn.dataset.id, btn.dataset.tipo));
    });
}

if (btnNovaRedeSocial) {
    btnNovaRedeSocial.addEventListener('click', () => {
        formRedeSocial.reset();
        limparErrosModal();
        document.getElementById('redeSocialId').value = '';
        document.getElementById('redeSocialAtivo').checked = true;
        if (tituloModalRedeSocial) tituloModalRedeSocial.textContent = 'Nova Rede Social';
        abrirModal('modalRedeSocial');
    });
}

async function editarRede(id) {
    limparErrosModal();
    try {
        const rede = await getJson('/minha-conta/redes-sociais/' + id);
        if (!rede) return;

        document.getElementById('redeSocialId').value = rede.id;
        document.getElementById('redeSocialTipo').value = rede.tipo;
        document.getElementById('redeSocialUrl').value = rede.url;
        document.getElementById('redeSocialIdentificador').value = rede.identificador || '';
        document.getElementById('redeSocialAtivo').checked = rede.ativo;

        if (tituloModalRedeSocial) tituloModalRedeSocial.textContent = 'Editar Rede Social';
        abrirModal('modalRedeSocial');
    } catch (err) {
        toast('Erro ao buscar dados da rede social.', true);
    }
}

if (formRedeSocial) {
    formRedeSocial.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErrosModal();
        if (btnSalvarRedeSocial) btnSalvarRedeSocial.disabled = true;

        const id = document.getElementById('redeSocialId').value;
        const url = id ? '/minha-conta/redes-sociais/' + id : '/minha-conta/redes-sociais';
        const metodo = id ? 'PUT' : 'POST';

        const params = new URLSearchParams(new FormData(formRedeSocial));
        if (!document.getElementById('redeSocialAtivo').checked) {
            params.set('ativo', 'false');
        }

        try {
            const data = await enviar(url, metodo, params);
            if (data.sucesso) {
                toast(data.mensagem || 'Rede social salva com sucesso!');
                fecharModal('modalRedeSocial');
                carregarRedesSociais();
            } else {
                aplicarErrosModal(data.errosCampos);
                aplicarErrosModal(data.errosNegocio);
                if (data.mensagem && !data.errosCampos && !data.errosNegocio) {
                    toast(data.mensagem, true);
                }
            }
        } catch (err) {
            toast('Erro de comunicação ao salvar rede social.', true);
        } finally {
            if (btnSalvarRedeSocial) btnSalvarRedeSocial.disabled = false;
        }
    });
}

async function excluirRede(id, tipo) {
    const templateMsg = formRedeSocial ? formRedeSocial.dataset.msgConfirmaExclusao : '';
    const msg = templateMsg
        ? templateMsg.replace('{0}', tipo || '')
        : 'Confirma a exclusão da rede social ' + (tipo || '') + '?';

    if (!confirm(msg)) return;

    try {
        const data = await enviar('/minha-conta/redes-sociais/' + id, 'DELETE');
        if (data.sucesso) {
            toast(data.mensagem || 'Rede social excluída com sucesso!');
            carregarRedesSociais();
        } else {
            toast(data.mensagem || 'Erro ao excluir rede social.', true);
        }
    } catch (err) {
        toast('Erro de comunicação ao excluir rede social.', true);
    }
}

