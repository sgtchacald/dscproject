import { getJson } from '../comum/http.js';
import { abrirModal, fecharModal } from '../comum/ui.js';

const cfg = () => document.getElementById('dadosTelaTransacao').dataset;

function obterMesAnteriorIso() {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
}

const mesPadrao = () => cfg().competenciaPadrao || obterMesAnteriorIso();

let opcoesCarregadas = false;

const filtro = {
    busca: '',
    competenciaInicio: '',
    competenciaFim: '',
    contaId: '',
    natureza: '',
    categoriaId: ''
};

export function obterFiltroAtual() {
    if (!filtro.competenciaInicio && !filtro.competenciaFim) {
        filtro.competenciaInicio = mesPadrao();
        filtro.competenciaFim = mesPadrao();
    }
    return { ...filtro };
}

export function abrirModalFiltro() {
    carregarOpcoesFiltro();
    const elCompIni = document.getElementById('filtroCompetenciaInicio');
    const elCompFim = document.getElementById('filtroCompetenciaFim');
    if (elCompIni && !elCompIni.value) elCompIni.value = filtro.competenciaInicio || mesPadrao();
    if (elCompFim && !elCompFim.value) elCompFim.value = filtro.competenciaFim || mesPadrao();
    abrirModal('modalFiltroTransacao');
}

async function carregarOpcoesFiltro() {
    if (opcoesCarregadas) return;

    try {
        const [contas, categorias] = await Promise.all([
            getJson(cfg().urlContasOpcoes),
            getJson(cfg().urlCategoriasOpcoes)
        ]);

        const selConta = document.getElementById('filtroTransacaoContaId');
        if (selConta && selConta.options.length <= 1) {
            (contas || []).forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.descricao;
                selConta.appendChild(opt);
            });
        }

        const selCat = document.getElementById('filtroTransacaoCategoriaId');
        if (selCat && selCat.options.length <= 1) {
            (categorias || []).forEach(cat => {
                const opt = document.createElement('option');
                opt.value = cat.id;
                opt.textContent = cat.nome;
                selCat.appendChild(opt);
            });
        }

        opcoesCarregadas = true;
    } catch (e) {
        console.error('Erro ao carregar opções para o filtro de transações', e);
    }
}

export function inicializarFiltro(onAplicar) {
    filtro.competenciaInicio = mesPadrao();
    filtro.competenciaFim = mesPadrao();

    const btnAplicar = document.getElementById('btnAplicarFiltros');
    const btnLimpar = document.getElementById('btnLimparFiltros');

    if (btnAplicar) {
        btnAplicar.addEventListener('click', () => {
            filtro.busca = (document.getElementById('filtroTransacaoBusca')?.value || '').trim();
            filtro.competenciaInicio = document.getElementById('filtroCompetenciaInicio')?.value || '';
            filtro.competenciaFim = document.getElementById('filtroCompetenciaFim')?.value || '';
            filtro.contaId = document.getElementById('filtroTransacaoContaId')?.value || '';
            filtro.natureza = document.getElementById('filtroTransacaoNatureza')?.value || '';
            filtro.categoriaId = document.getElementById('filtroTransacaoCategoriaId')?.value || '';

            fecharModal('modalFiltroTransacao');
            if (typeof onAplicar === 'function') onAplicar();
        });
    }

    if (btnLimpar) {
        btnLimpar.addEventListener('click', () => {
            document.getElementById('formFiltroTransacao')?.reset();
            filtro.busca = '';
            filtro.competenciaInicio = mesPadrao();
            filtro.competenciaFim = mesPadrao();
            filtro.contaId = '';
            filtro.natureza = '';
            filtro.categoriaId = '';

            const elCompIni = document.getElementById('filtroCompetenciaInicio');
            const elCompFim = document.getElementById('filtroCompetenciaFim');
            if (elCompIni) elCompIni.value = filtro.competenciaInicio;
            if (elCompFim) elCompFim.value = filtro.competenciaFim;

            fecharModal('modalFiltroTransacao');
            if (typeof onAplicar === 'function') onAplicar();
        });
    }
}
