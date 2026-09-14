import { abrirModal, fecharModal } from '../comum/ui.js';
import { getJson } from '../comum/http.js';

export const EVENTO_FILTRO_APLICADO = 'receita:filtro-aplicado';

const cfg = () => document.getElementById('dadosTelaReceita').dataset;

function obterMesAnteriorIso() {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
}

const mesPadrao = obterMesAnteriorIso();

const filtro = {
    busca: '',
    competenciaInicial: mesPadrao,
    competenciaFinal: mesPadrao,
    contaId: '',
    categoriaId: '',
    situacao: ''
};

let opcoesCarregadas = false;

export function obterFiltroAtual() {
    return { ...filtro };
}

async function carregarOpcoes() {
    if (opcoesCarregadas) return;

    const selectConta = document.getElementById('filtroConta');
    const selectCategoria = document.getElementById('filtroCategoria');

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
        console.error('Erro ao carregar opções de conta/categoria para o filtro', e);
    }
}

export function abrirModalFiltro() {
    carregarOpcoes().then(() => {
        document.getElementById('filtroBusca').value = filtro.busca;
        document.getElementById('filtroCompetenciaInicial').value = filtro.competenciaInicial;
        document.getElementById('filtroCompetenciaFinal').value = filtro.competenciaFinal;
        document.getElementById('filtroConta').value = filtro.contaId;
        document.getElementById('filtroCategoria').value = filtro.categoriaId;
        document.getElementById('filtroSituacao').value = filtro.situacao;
        abrirModal('modalFiltroReceita');
    });
}

export function inicializarFiltro(onAplicar) {
    const btnAplicar = document.getElementById('btnAplicarFiltroReceita');
    const btnLimpar = document.getElementById('btnLimparFiltroReceita');

    if (btnAplicar) {
        btnAplicar.addEventListener('click', function () {
            filtro.busca = (document.getElementById('filtroBusca').value || '').trim();
            filtro.competenciaInicial = document.getElementById('filtroCompetenciaInicial').value || '';
            filtro.competenciaFinal = document.getElementById('filtroCompetenciaFinal').value || '';
            filtro.contaId = document.getElementById('filtroConta').value || '';
            filtro.categoriaId = document.getElementById('filtroCategoria').value || '';
            filtro.situacao = document.getElementById('filtroSituacao').value || '';
            fecharModal('modalFiltroReceita');
            if (onAplicar) onAplicar(obterFiltroAtual());
        });
    }

    if (btnLimpar) {
        btnLimpar.addEventListener('click', function () {
            const padrao = obterMesAnteriorIso();
            filtro.busca = '';
            filtro.competenciaInicial = padrao;
            filtro.competenciaFinal = padrao;
            filtro.contaId = '';
            filtro.categoriaId = '';
            filtro.situacao = '';
            document.getElementById('filtroBusca').value = '';
            document.getElementById('filtroCompetenciaInicial').value = padrao;
            document.getElementById('filtroCompetenciaFinal').value = padrao;
            document.getElementById('filtroConta').value = '';
            document.getElementById('filtroCategoria').value = '';
            document.getElementById('filtroSituacao').value = '';
            fecharModal('modalFiltroReceita');
            if (onAplicar) onAplicar(obterFiltroAtual());
        });
    }
}
