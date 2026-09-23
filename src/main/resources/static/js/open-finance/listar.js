import { getJson, headers } from '../comum/http.js';
import { abrirModal, fecharModal, toast } from '../comum/ui.js';

const cfg = () => document.getElementById('dadosTelaOpenFinance').dataset;

let conexoes = [];
let credencialAtual = null;
let pluggyWidget = null;
let historicoConexaoIdAtual = null;
let historicoPaginaAtual = 0;
let historicoTotalPaginas = 1;

// Elementos principais
const corpoTabela = document.getElementById('corpoTabelaConexoes');
const rodapeContagem = document.getElementById('rodapeContagemConexoes');
const btnConfigurarCredenciais = document.getElementById('btnConfigurarCredenciais');
const btnNovaConexao = document.getElementById('btnNovaConexao');

// Elementos do card de credencial
const badgeStatusCredencial = document.getElementById('badgeStatusCredencial');
const subtituloStatusCredencial = document.getElementById('subtituloStatusCredencial');
const barraStatusCredencial = document.getElementById('barraStatusCredencial');

// Permissões
const pode = {
    listar: () => !!document.querySelector('[data-perm="listar"]'),
    conectar: () => !!document.querySelector('[data-perm="conectar"]'),
    sincronizar: () => !!document.querySelector('[data-perm="sincronizar"]'),
    desconectar: () => !!document.querySelector('[data-perm="desconectar"]'),
    configurarCredencial: () => !!document.querySelector('[data-perm="configurar-credencial"]')
};

// Formatação de data/hora pt-BR
function formatarDataHora(iso) {
    if (!iso) return '—';
    try {
        const d = new Date(iso);
        return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    } catch {
        return iso;
    }
}

function formatarData(iso) {
    if (!iso) return '—';
    try {
        const d = new Date(iso);
        return d.toLocaleDateString('pt-BR');
    } catch {
        return iso;
    }
}

function formatarMoeda(valor, moeda = 'BRL') {
    if (valor === null || valor === undefined) return '—';
    try {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: moeda }).format(valor);
    } catch {
        return String(valor);
    }
}

// ----------------------------------------------------------------------------
// 1. CARREGAMENTO INICIAL E STATUS DE CREDENCIAIS
// ----------------------------------------------------------------------------

async function carregarCredenciais() {
    try {
        credencialAtual = await getJson(cfg().urlCredenciais);
        atualizarCardCredencial();
    } catch (e) {
        console.error('Erro ao carregar credenciais:', e);
    }
}

function atualizarCardCredencial() {
    if (!credencialAtual || !credencialAtual.possuiSecret) {
        badgeStatusCredencial.className = 'badge bg-warning-lt';
        badgeStatusCredencial.textContent = 'Não Configurado';
        barraStatusCredencial.className = 'card-status-top bg-warning';
        subtituloStatusCredencial.textContent = 'Credenciais BYOK não configuradas. Configure sua chave antes de conectar.';
        if (btnNovaConexao) {
            btnNovaConexao.setAttribute('disabled', 'disabled');
            btnNovaConexao.title = 'Configure suas credenciais antes de conectar.';
        }
    } else {
        const amb = credencialAtual.ambiente === 'SANDBOX' ? 'Sandbox (Testes)' : 'Produção';
        badgeStatusCredencial.className = 'badge bg-success-lt';
        badgeStatusCredencial.textContent = 'Configurado (' + amb + ')';
        barraStatusCredencial.className = 'card-status-top bg-success';
        subtituloStatusCredencial.textContent = 'Client ID: ' + credencialAtual.clientId;
        if (btnNovaConexao) {
            btnNovaConexao.removeAttribute('disabled');
            btnNovaConexao.title = '';
        }
    }
}

async function carregarConexoes() {
    try {
        conexoes = await getJson(cfg().urlDados);
        renderConexoes();
    } catch (e) {
        console.error('Erro ao carregar conexões:', e);
        corpoTabela.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-danger">Erro ao carregar conexões Open Finance.</td></tr>';
    }
}

// ----------------------------------------------------------------------------
// 2. RENDERIZAÇÃO DA TABELA DE CONEXÕES
// ----------------------------------------------------------------------------

function renderConexoes() {
    if (!conexoes || conexoes.length === 0) {
        corpoTabela.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="ph ph-bank fs-1 d-block mb-2 text-muted"></i>
                    Nenhuma instituição bancária conectada no momento.<br>
                    Clique em <strong>Nova Conexão</strong> para conectar sua primeira conta.
                </td>
            </tr>`;
        rodapeContagem.textContent = '0 conexões ativas';
        return;
    }

    corpoTabela.innerHTML = conexoes.map(c => {
        const badgeConsentimento = c.consentimentoExpirando
            ? `<span class="badge bg-warning-lt" title="Expira em ${c.diasParaExpirar} dias"><i class="ph ph-warning me-1"></i>Expira em ${c.diasParaExpirar} dias</span>`
            : (c.consentimentoExpiracao ? `<span class="badge bg-secondary-lt">Até ${formatarData(c.consentimentoExpiracao)}</span>` : '—');

        return `
            <tr data-id="${c.id}">
                <td>
                    <div class="d-flex align-items-center">
                        <span class="avatar avatar-sm me-2 text-white" style="background-color: ${c.instituicaoCorHex || '#206bc4'};">
                            <i class="ph ph-bank"></i>
                        </span>
                        <div>
                            <div class="font-weight-medium">${c.instituicaoNome || 'Não Identificada'}</div>
                            <div class="text-secondary small">ID: ${c.idExterno}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="badge bg-purple-lt">${c.provedorNome || 'Pluggy'}</span>
                </td>
                <td>
                    <span class="badge bg-blue-lt">
                        <i class="ph ph-cards me-1"></i>${c.totalContas} conta(s) (${c.totalContasVinculadas} vinculada(s))
                    </span>
                </td>
                <td>
                    <span class="badge ${c.statusBadgeClass || 'bg-secondary-lt'}">
                        <i class="ph ${c.statusIcone || 'ph-question'} me-1"></i>${c.statusDescricao || c.status}
                    </span>
                </td>
                <td>
                    ${badgeConsentimento}
                </td>
                <td>
                    <div class="small">${formatarDataHora(c.ultimaSincronizacaoEm)}</div>
                </td>
                <td class="text-end">
                    <div class="btn-group">
                        <button type="button" class="btn btn-sm btn-icon btn-outline-secondary btn-sincronizar" 
                                data-id="${c.id}" title="${c.podeSincronizar ? 'Sincronizar agora' : 'Aguarde para sincronizar novamente'}"
                                ${!c.podeSincronizar ? 'disabled' : ''}>
                            <i class="ph ph-arrows-clockwise"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-icon btn-outline-secondary btn-contas" 
                                data-id="${c.id}" data-nome="${c.instituicaoNome || 'Instituição'}" title="Contas vinculadas">
                            <i class="ph ph-cards"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-icon btn-outline-secondary btn-reconectar" 
                                data-id="${c.id}" title="Reconectar / Atualizar banco">
                            <i class="ph ph-arrow-counter-clockwise"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-icon btn-outline-secondary btn-historico" 
                                data-id="${c.id}" data-nome="${c.instituicaoNome || 'Instituição'}" title="Histórico de sincronizações">
                            <i class="ph ph-clock-counter-clockwise"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-icon btn-outline-danger btn-desconectar" 
                                data-id="${c.id}" title="Desconectar instituição">
                            <i class="ph ph-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>`;
    }).join('');

    rodapeContagem.textContent = `${conexoes.length} conexão(ões) encontrada(s)`;

    // Registra listeners de ação na tabela
    corpoTabela.querySelectorAll('.btn-sincronizar').forEach(b => b.addEventListener('click', () => sincronizarConexao(b.dataset.id, b)));
    corpoTabela.querySelectorAll('.btn-contas').forEach(b => b.addEventListener('click', () => abrirModalContas(b.dataset.id, b.dataset.nome)));
    corpoTabela.querySelectorAll('.btn-reconectar').forEach(b => b.addEventListener('click', () => abrirWidgetConexao(b.dataset.id)));
    corpoTabela.querySelectorAll('.btn-historico').forEach(b => b.addEventListener('click', () => abrirModalHistorico(b.dataset.id, b.dataset.nome)));
    corpoTabela.querySelectorAll('.btn-desconectar').forEach(b => b.addEventListener('click', () => desconectarConexao(b.dataset.id)));
}

// ----------------------------------------------------------------------------
// 3. MODAL DE CREDENCIAIS (BYOK)
// ----------------------------------------------------------------------------

function abrirModalCredenciais() {
    const feedback = document.getElementById('alertaCredenciaisFeedback');
    feedback.className = 'alert d-none';
    feedback.textContent = '';

    if (credencialAtual) {
        document.getElementById('credencialAmbiente').value = credencialAtual.ambiente || 'PRODUCTION';
        document.getElementById('credencialClientId').value = credencialAtual.clientId || '';
        document.getElementById('credencialClientSecret').value = '';
        document.getElementById('credencialClientSecret').placeholder = credencialAtual.possuiSecret ? '•••••••••••••••••••••••• (Inalterado)' : 'Informe o Client Secret';
    } else {
        document.getElementById('credencialAmbiente').value = 'PRODUCTION';
        document.getElementById('credencialClientId').value = '';
        document.getElementById('credencialClientSecret').value = '';
        document.getElementById('credencialClientSecret').placeholder = 'Informe o Client Secret';
    }

    abrirModal('modalCredenciais');
}

async function testarCredencial() {
    const spinner = document.getElementById('spinnerTestarCredencial');
    const feedback = document.getElementById('alertaCredenciaisFeedback');
    const btn = document.getElementById('btnTestarCredencial');

    const clientId = document.getElementById('credencialClientId').value.trim();
    const clientSecret = document.getElementById('credencialClientSecret').value.trim();
    const ambiente = document.getElementById('credencialAmbiente').value;
    const provedorId = Number(document.getElementById('credencialProvedorId').value);

    if (!clientId || (!clientSecret && (!credencialAtual || !credencialAtual.possuiSecret))) {
        feedback.className = 'alert alert-danger';
        feedback.textContent = 'Preencha o Client ID e o Client Secret para testar a conexão.';
        return;
    }

    spinner.classList.remove('d-none');
    btn.setAttribute('disabled', 'disabled');
    feedback.className = 'alert d-none';

    try {
        const resp = await fetch(cfg().urlTestarCredenciais, {
            method: 'POST',
            headers: headers(true),
            body: JSON.stringify({ provedorId, clientId, clientSecret, ambiente })
        });
        const dados = await resp.json();

        if (dados.sucesso) {
            feedback.className = 'alert alert-success';
            feedback.textContent = dados.mensagem || 'Conexão com a API validada com sucesso!';
        } else {
            feedback.className = 'alert alert-danger';
            feedback.textContent = dados.mensagem || 'Falha na validação das credenciais junto ao provedor.';
        }
    } catch (e) {
        feedback.className = 'alert alert-danger';
        feedback.textContent = 'Erro ao se comunicar com o servidor: ' + e.message;
    } finally {
        spinner.classList.add('d-none');
        btn.removeAttribute('disabled');
    }
}

async function salvarCredenciais(e) {
    e.preventDefault();
    const spinner = document.getElementById('spinnerSalvarCredencial');
    const feedback = document.getElementById('alertaCredenciaisFeedback');
    const btn = document.getElementById('btnSalvarCredencial');

    const clientId = document.getElementById('credencialClientId').value.trim();
    const clientSecret = document.getElementById('credencialClientSecret').value.trim();
    const ambiente = document.getElementById('credencialAmbiente').value;
    const provedorId = Number(document.getElementById('credencialProvedorId').value);

    if (!clientId) {
        feedback.className = 'alert alert-danger';
        feedback.textContent = 'O Client ID é obrigatório.';
        return;
    }

    spinner.classList.remove('d-none');
    btn.setAttribute('disabled', 'disabled');
    feedback.className = 'alert d-none';

    try {
        const resp = await fetch(cfg().urlCredenciais, {
            method: 'POST',
            headers: headers(true),
            body: JSON.stringify({ provedorId, clientId, clientSecret, ambiente })
        });
        const dados = await resp.json();

        if (resp.ok && dados.sucesso) {
            toast(dados.mensagem || 'Credenciais salvas com sucesso!');
            fecharModal('modalCredenciais');
            await carregarCredenciais();
        } else {
            feedback.className = 'alert alert-danger';
            feedback.textContent = dados.mensagem || (dados.errosNegocio && dados.errosNegocio.geral) || 'Falha ao salvar credenciais.';
        }
    } catch (err) {
        feedback.className = 'alert alert-danger';
        feedback.textContent = 'Erro ao se comunicar com o servidor: ' + err.message;
    } finally {
        spinner.classList.add('d-none');
        btn.removeAttribute('disabled');
    }
}

// ----------------------------------------------------------------------------
// 4. FLUXO DO WIDGET PLUGGY CONNECT (NOVA CONEXÃO / RECONEXÃO)
// ----------------------------------------------------------------------------

function carregarSdkPluggy() {
    return new Promise((resolve, reject) => {
        if (typeof window.PluggyConnect !== 'undefined') {
            resolve(window.PluggyConnect);
            return;
        }

        const scriptId = 'pluggy-connect-sdk-script';
        let script = document.getElementById(scriptId);

        if (!script) {
            script = document.createElement('script');
            script.id = scriptId;
            script.src = 'https://cdn.pluggy.ai/pluggy-connect/v2.8.2/pluggy-connect.js';
            script.async = true;
            document.head.appendChild(script);
        }

        const timer = setTimeout(() => {
            if (typeof window.PluggyConnect !== 'undefined') {
                resolve(window.PluggyConnect);
            } else {
                reject(new Error('Tempo limite excedido ao baixar o SDK Pluggy Connect via CDN.'));
            }
        }, 10000);

        script.onload = () => {
            clearTimeout(timer);
            if (typeof window.PluggyConnect !== 'undefined') {
                resolve(window.PluggyConnect);
            } else {
                reject(new Error('SDK Pluggy Connect baixado, mas window.PluggyConnect não foi definido.'));
            }
        };

        script.onerror = () => {
            clearTimeout(timer);
            reject(new Error('Falha ao baixar o SDK Pluggy Connect via CDN (verifique se a rede/bloqueador permite o domínio cdn.pluggy.ai).'));
        };
    });
}

async function abrirWidgetConexao(conexaoId = null) {
    if (!credencialAtual || !credencialAtual.possuiSecret) {
        toast(cfg().msgSemCredenciais, true);
        abrirModalCredenciais();
        return;
    }

    const container = document.getElementById('pluggyConnectContainer');
    const loading = document.getElementById('pluggyConnectLoading');
    container.innerHTML = '';
    loading.classList.remove('d-none');

    abrirModal('modalConectar');

    try {
        // Assegura carregamento do SDK Pluggy Connect v2.8.2
        await carregarSdkPluggy();

        const resp = await fetch(cfg().urlConnectToken, {
            method: 'POST',
            headers: headers(true),
            body: JSON.stringify({ conexaoId: conexaoId ? Number(conexaoId) : null })
        });
        const dados = await resp.json();

        if (!dados.connectToken) {
            throw new Error(dados.mensagem || 'Falha ao obter connectToken');
        }

        loading.classList.add('d-none');

        // Inicializa SDK Pluggy Connect
        if (typeof window.PluggyConnect === 'undefined') {
            throw new Error('SDK Pluggy Connect não carregado. Verifique sua conexão à internet.');
        }

        if (pluggyWidget && typeof pluggyWidget.destroy === 'function') {
            try { pluggyWidget.destroy(); } catch (ignored) {}
        }

        pluggyWidget = new window.PluggyConnect({
            connectToken: dados.connectToken,
            includeSandbox: credencialAtual.ambiente === 'SANDBOX',
            onSuccess: async (itemData) => {
                try {
                    fecharModal('modalConectar');
                    toast('Autenticação concluída! Registrando conexão e iniciando sincronização...');
                    await processarCallback(itemData.item.id);
                } catch (err) {
                    toast('Erro ao processar retorno da conexão: ' + err.message, true);
                }
            },
            onError: (error) => {
                console.error('Pluggy Connect Error:', error);
                toast('Erro durante a conexão bancária: ' + (error.message || 'Tente novamente.'), true);
            },
            onClose: () => {
                fecharModal('modalConectar');
            }
        });

        pluggyWidget.init(container);

    } catch (e) {
        loading.classList.add('d-none');
        container.innerHTML = `<div class="alert alert-danger m-3">${e.message}</div>`;
        toast(e.message, true);
    }
}

async function processarCallback(itemId) {
    const resp = await fetch(cfg().urlCallback, {
        method: 'POST',
        headers: headers(true),
        body: JSON.stringify({ itemId })
    });
    const dados = await resp.json();
    if (resp.ok && dados.sucesso) {
        toast(dados.mensagem || 'Instituição conectada com sucesso!');
        await carregarConexoes();
    } else {
        throw new Error(dados.mensagem || 'Erro ao registrar conexão.');
    }
}

// ----------------------------------------------------------------------------
// 5. AÇÕES SOBRE CONEXÕES (SINCRONIZAR, DESCONECTAR)
// ----------------------------------------------------------------------------

async function sincronizarConexao(id, btn) {
    if (!btn) return;
    const url = cfg().urlSincronizar.replace('{id}', id);
    const icone = btn.querySelector('i');
    icone.classList.add('ph-spin');
    btn.setAttribute('disabled', 'disabled');

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: headers(true)
        });
        const dados = await resp.json();

        if (resp.ok && dados.sucesso) {
            toast(dados.mensagem || 'Sincronização iniciada com sucesso!');
            await carregarConexoes();
        } else {
            toast(dados.mensagem || 'Não foi possível sincronizar no momento.', true);
        }
    } catch (e) {
        toast('Erro ao disparar sincronização: ' + e.message, true);
    } finally {
        icone.classList.remove('ph-spin');
        btn.removeAttribute('disabled');
    }
}

async function desconectarConexao(id) {
    if (!confirm(cfg().msgConfirmaDesconectar)) {
        return;
    }

    const url = cfg().urlDesconectar.replace('{id}', id);
    try {
        const resp = await fetch(url, {
            method: 'DELETE',
            headers: headers(true)
        });
        const dados = await resp.json();

        if (resp.ok && dados.sucesso) {
            toast(dados.mensagem || 'Conexão removida com sucesso!');
            await carregarConexoes();
        } else {
            toast(dados.mensagem || 'Erro ao desconectar instituição.', true);
        }
    } catch (e) {
        toast('Erro ao comunicar com o servidor: ' + e.message, true);
    }
}

// ----------------------------------------------------------------------------
// 6. MODAL DE CONTAS E CARTÕES VINCULADOS
// ----------------------------------------------------------------------------

async function abrirModalContas(conexaoId, instituicaoNome) {
    document.getElementById('modalContasTitulo').textContent = `Contas Detectadas — ${instituicaoNome}`;
    const loading = document.getElementById('modalContasLoading');
    const container = document.getElementById('containerTabelaContasExternas');
    const vazio = document.getElementById('modalContasVazio');
    const corpo = document.getElementById('corpoTabelaContasExternas');

    loading.classList.remove('d-none');
    container.classList.add('d-none');
    vazio.classList.add('d-none');
    corpo.innerHTML = '';

    abrirModal('modalContasExternas');

    try {
        const [contasExternas, contasOpcoes, cartoesOpcoes] = await Promise.all([
            getJson(cfg().urlContas.replace('{id}', conexaoId)),
            getJson(cfg().urlContasOpcoes),
            getJson(cfg().urlCartoesOpcoes)
        ]);

        loading.classList.add('d-none');

        if (!contasExternas || contasExternas.length === 0) {
            vazio.classList.remove('d-none');
            return;
        }

        container.classList.remove('d-none');

        corpo.innerHTML = contasExternas.map(ce => {
            const isBank = ce.tipo === 'BANK';
            const opcoes = isBank ? contasOpcoes : cartoesOpcoes;
            const selecionadoId = isBank ? ce.contaId : ce.cartaoId;

            const selectOptions = `
                <option value="">Não vinculado</option>
                ${opcoes.map(o => `<option value="${o.id}" ${String(o.id) === String(selecionadoId) ? 'selected' : ''}>${o.descricao || o.nome}</option>`).join('')}
            `;

            return `
                <tr data-ce-id="${ce.id}" data-tipo="${ce.tipo}">
                    <td>
                        <span class="badge ${isBank ? 'bg-primary-lt' : 'bg-purple-lt'}">
                            <i class="ph ${isBank ? 'ph-bank' : 'ph-credit-card'} me-1"></i>${ce.tipoDescricao || ce.tipo}
                        </span>
                    </td>
                    <td>
                        <div class="font-weight-medium">${ce.nome || 'Conta Externa'}</div>
                        <div class="text-secondary small">Nº: ${ce.numero || '—'}</div>
                    </td>
                    <td class="text-end">
                        <strong>${formatarMoeda(ce.saldo, ce.moeda)}</strong>
                    </td>
                    <td>
                        <select class="form-select form-select-sm select-vinculo" data-ce-id="${ce.id}" data-tipo="${ce.tipo}">
                            ${selectOptions}
                        </select>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-primary btn-salvar-vinculo" data-ce-id="${ce.id}">
                            <i class="ph ph-check"></i>
                        </button>
                    </td>
                </tr>`;
        }).join('');

        corpo.querySelectorAll('.btn-salvar-vinculo').forEach(b => {
            b.addEventListener('click', () => salvarVinculoConta(b.dataset.ceId, b));
        });

    } catch (e) {
        loading.classList.add('d-none');
        vazio.classList.remove('d-none');
        vazio.innerHTML = `<div class="text-danger">Erro ao carregar contas: ${e.message}</div>`;
    }
}

async function salvarVinculoConta(contaExternaId, btn) {
    const tr = btn.closest('tr');
    const select = tr.querySelector('.select-vinculo');
    const tipo = select.dataset.tipo;
    const valor = select.value ? Number(select.value) : null;

    const payload = {
        contaId: tipo === 'BANK' ? valor : null,
        cartaoId: tipo === 'CREDIT' ? valor : null
    };

    const icone = btn.querySelector('i');
    icone.className = 'spinner-border spinner-border-sm';
    btn.setAttribute('disabled', 'disabled');

    try {
        const url = cfg().urlVincular.replace('{id}', contaExternaId);
        const resp = await fetch(url, {
            method: 'PUT',
            headers: headers(true),
            body: JSON.stringify(payload)
        });
        const dados = await resp.json();

        if (resp.ok && dados.sucesso) {
            toast(dados.mensagem || 'Vínculo atualizado com sucesso!');
            await carregarConexoes();
        } else {
            toast(dados.mensagem || 'Erro ao vincular conta.', true);
        }
    } catch (e) {
        toast('Erro ao atualizar vínculo: ' + e.message, true);
    } finally {
        icone.className = 'ph ph-check';
        btn.removeAttribute('disabled');
    }
}

// ----------------------------------------------------------------------------
// 7. MODAL DE HISTÓRICO DE SINCRONIZAÇÕES
// ----------------------------------------------------------------------------

async function abrirModalHistorico(conexaoId, instituicaoNome) {
    historicoConexaoIdAtual = conexaoId;
    historicoPaginaAtual = 0;
    document.getElementById('modalHistoricoTitulo').textContent = `Histórico de Sincronizações — ${instituicaoNome}`;

    abrirModal('modalHistoricoSincronizacao');
    await carregarPaginaHistorico();
}

async function carregarPaginaHistorico() {
    const loading = document.getElementById('modalHistoricoLoading');
    const container = document.getElementById('containerTabelaHistorico');
    const vazio = document.getElementById('modalHistoricoVazio');
    const corpo = document.getElementById('corpoTabelaHistorico');
    const paginacao = document.getElementById('paginacaoHistorico');
    const infoPaginacao = document.getElementById('infoPaginacaoHistorico');

    loading.classList.remove('d-none');
    container.classList.add('d-none');
    vazio.classList.add('d-none');
    paginacao.classList.add('d-none');
    corpo.innerHTML = '';

    try {
        const url = `${cfg().urlHistorico.replace('{id}', historicoConexaoIdAtual)}?page=${historicoPaginaAtual}&size=10`;
        const dados = await getJson(url);

        loading.classList.add('d-none');

        if (!dados.content || dados.content.length === 0) {
            vazio.classList.remove('d-none');
            return;
        }

        container.classList.remove('d-none');
        historicoTotalPaginas = dados.totalPages;

        corpo.innerHTML = dados.content.map(s => {
            const badgeStatus = s.status === 'CONCLUIDA'
                ? '<span class="badge bg-success-lt"><i class="ph ph-check-circle me-1"></i>Concluída</span>'
                : (s.status === 'EXECUTANDO'
                    ? '<span class="badge bg-azure-lt"><i class="ph ph-arrows-clockwise me-1"></i>Executando</span>'
                    : '<span class="badge bg-danger-lt"><i class="ph ph-warning-circle me-1"></i>Erro</span>');

            return `
                <tr>
                    <td>${formatarDataHora(s.iniciadoEm)}</td>
                    <td>${formatarDataHora(s.finalizadoEm)}</td>
                    <td><span class="badge bg-secondary-lt">${s.tipoDescricao || s.tipo}</span></td>
                    <td class="text-center font-weight-medium">${s.qtdTransacoesNovas}</td>
                    <td class="text-center font-weight-medium">${s.qtdContasAtualizadas}</td>
                    <td>${badgeStatus}</td>
                    <td class="small text-secondary text-truncate" style="max-width: 200px;" title="${s.erroMensagem || ''}">
                        ${s.erroMensagem || '—'}
                    </td>
                </tr>`;
        }).join('');

        if (dados.totalPages > 1) {
            paginacao.classList.remove('d-none');
            infoPaginacao.textContent = `Página ${dados.number + 1} de ${dados.totalPages} (${dados.totalElements} registros)`;
            document.getElementById('btnPaginaAnteriorHistorico').disabled = dados.first;
            document.getElementById('btnPaginaProximaHistorico').disabled = dados.last;
        }

    } catch (e) {
        loading.classList.add('d-none');
        vazio.classList.remove('d-none');
        vazio.innerHTML = `<div class="text-danger">Erro ao carregar histórico: ${e.message}</div>`;
    }
}

// ----------------------------------------------------------------------------
// 8. INICIALIZAÇÃO GERAL E EVENT LISTENERS
// ----------------------------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {
    // Carregamento de dados iniciais
    carregarCredenciais();
    carregarConexoes();

    // Botões principais
    if (btnConfigurarCredenciais) {
        btnConfigurarCredenciais.addEventListener('click', abrirModalCredenciais);
    }
    if (btnNovaConexao) {
        btnNovaConexao.addEventListener('click', () => abrirWidgetConexao(null));
    }

    // Formulário de credenciais
    const formCredenciais = document.getElementById('formCredenciais');
    if (formCredenciais) {
        formCredenciais.addEventListener('submit', salvarCredenciais);
    }
    const btnTestar = document.getElementById('btnTestarCredencial');
    if (btnTestar) {
        btnTestar.addEventListener('click', testarCredencial);
    }

    // Alternar visibilidade da senha
    const btnToggle = document.getElementById('btnToggleSecret');
    if (btnToggle) {
        btnToggle.addEventListener('click', () => {
            const input = document.getElementById('credencialClientSecret');
            const icon = document.getElementById('iconeToggleSecret');
            if (input.type === 'password') {
                input.type = 'text';
                icon.className = 'ph ph-eye-slash';
            } else {
                input.type = 'password';
                icon.className = 'ph ph-eye';
            }
        });
    }

    // Paginação de Histórico
    const btnAnt = document.getElementById('btnPaginaAnteriorHistorico');
    const btnProx = document.getElementById('btnPaginaProximaHistorico');
    if (btnAnt) {
        btnAnt.addEventListener('click', () => {
            if (historicoPaginaAtual > 0) {
                historicoPaginaAtual--;
                carregarPaginaHistorico();
            }
        });
    }
    if (btnProx) {
        btnProx.addEventListener('click', () => {
            if (historicoPaginaAtual < historicoTotalPaginas - 1) {
                historicoPaginaAtual++;
                carregarPaginaHistorico();
            }
        });
    }

    // Limpeza mandatória do widget ao fechar o modal
    const modalConectarEl = document.getElementById('modalConectar');
    if (modalConectarEl) {
        modalConectarEl.addEventListener('hidden.bs.modal', () => {
            const container = document.getElementById('pluggyConnectContainer');
            if (container) container.innerHTML = '';
            if (pluggyWidget && typeof pluggyWidget.destroy === 'function') {
                try { pluggyWidget.destroy(); } catch (ignored) {}
            }
        });
    }
});
