import { getJson, enviar } from '../comum/http.js';
import { semAcento, dataBr, exibirToast } from '../comum/ui.js';
import { inicializarFiltro, obterFiltroAtual, abrirModalFiltro } from './modal-filtro.js';
import { inicializarForm, abrirEdicao, excluir, EVENTO_ALTERADO } from './modal-form.js';
import { inicializarRecebimento, abrirRecebimento, EVENTO_RECEBIMENTO_REGISTRADO } from './modal-recebimento.js';

const cfg = () => document.getElementById('dadosTelaReceita').dataset;

let todas = [];
let ordenacao = { col: 'competencia', asc: false };
let selecionadasMap = new Map();

const corpo = document.getElementById('corpoTabelaReceitas');
const rodape = document.getElementById('rodapeContagemReceitas');
const chkTodos = document.getElementById('chkTodosReceitas');
const btnDuplicarLote = document.getElementById('btnDuplicarReceitaLote');

const pode = {
    inserir: () => !!document.querySelector('[data-perm="inserir"]'),
    editar: () => !!document.querySelector('[data-perm="editar"]'),
    excluir: () => !!document.querySelector('[data-perm="excluir"]'),
    receber: () => !!document.querySelector('[data-perm="receber"]')
};

function atualizarBotaoDuplicarLote() {
    if (btnDuplicarLote) {
        btnDuplicarLote.disabled = selecionadasMap.size === 0;
    }
}

async function carregar() {
    try {
        todas = await getJson(cfg().urlDados);
        selecionadasMap.clear();
        atualizarBotaoDuplicarLote();
        if (chkTodos) chkTodos.checked = false;
        render();
    } catch (e) {
        corpo.innerHTML = '<tr><td colspan="11" class="text-center text-danger">Erro ao carregar receitas.</td></tr>';
    }
}

function situacaoCodigo(r) {
    if (r.excluido) return 'EXCLUIDA';
    return r.recebido ? 'RECEBIDA' : 'PREVISTA';
}

function filtrar(lista) {
    const f = obterFiltroAtual();
    const buscaNorm = semAcento(f.busca);

    return lista.filter(r => {
        if (buscaNorm) {
            const nomeNorm = semAcento(r.nome);
            const descNorm = semAcento(r.descricao);
            if (!nomeNorm.includes(buscaNorm) && !descNorm.includes(buscaNorm)) {
                return false;
            }
        }

        if (f.competenciaInicial && r.competencia < f.competenciaInicial) return false;
        if (f.competenciaFinal && r.competencia > f.competenciaFinal) return false;

        if (f.contaId && String(r.contaId) !== String(f.contaId)) return false;
        if (f.categoriaId && String(r.categoriaId) !== String(f.categoriaId)) return false;

        if (f.situacao && situacaoCodigo(r) !== f.situacao) return false;

        return true;
    });
}

function ordenar(lista) {
    const { col, asc } = ordenacao;
    return lista.slice().sort((a, b) => {
        let cmp = 0;

        if (col === 'competencia') {
            const compA = (a.competencia || '').trim();
            const compB = (b.competencia || '').trim();
            cmp = compA.localeCompare(compB);
        } else if (col === 'categoria') {
            const va = a.categoriaNome || '';
            const vb = b.categoriaNome || '';
            cmp = va.localeCompare(vb, 'pt-BR');
        } else if (col === 'conta') {
            const va = a.contaDescricao || '';
            const vb = b.contaDescricao || '';
            cmp = va.localeCompare(vb, 'pt-BR');
        } else if (col === 'valor') {
            const va = a.valor != null ? Number(a.valor) : 0;
            const vb = b.valor != null ? Number(b.valor) : 0;
            cmp = va - vb;
        } else if (col === 'situacao') {
            const ordem = { PREVISTA: 0, RECEBIDA: 1, EXCLUIDA: 2 };
            const va = ordem[situacaoCodigo(a)] ?? 0;
            const vb = ordem[situacaoCodigo(b)] ?? 0;
            cmp = va - vb;
        } else if (col === 'dataLancamento') {
            const va = a.dataLancamento || '';
            const vb = b.dataLancamento || '';
            cmp = va.localeCompare(vb);
        } else if (col === 'dataRecebimento') {
            const va = a.dataRecebimento || '';
            const vb = b.dataRecebimento || '';
            cmp = va.localeCompare(vb);
        } else if (col === 'origem') {
            const va = labelOrigem(a.origem) || '';
            const vb = labelOrigem(b.origem) || '';
            cmp = va.localeCompare(vb, 'pt-BR');
        } else {
            let va = a[col];
            let vb = b[col];
            if (va == null) va = '';
            if (vb == null) vb = '';
            if (typeof va === 'number' && typeof vb === 'number') {
                cmp = va - vb;
            } else {
                cmp = String(va).localeCompare(String(vb), 'pt-BR');
            }
        }

        if (cmp !== 0) {
            return asc ? cmp : -cmp;
        }

        // --- Desempate determinístico ---
        if (col === 'competencia') {
            // 1. Data de lançamento
            const lancA = a.dataLancamento || '';
            const lancB = b.dataLancamento || '';
            const lancCmp = lancA.localeCompare(lancB);
            if (lancCmp !== 0) {
                return asc ? lancCmp : -lancCmp;
            }

            // 2. Data de recebimento
            const recA = a.dataRecebimento || '';
            const recB = b.dataRecebimento || '';
            const recCmp = recA.localeCompare(recB);
            if (recCmp !== 0) {
                return asc ? recCmp : -recCmp;
            }

            // 3. Nome
            const nomeA = a.nome || '';
            const nomeB = b.nome || '';
            const nomeCmp = nomeA.localeCompare(nomeB, 'pt-BR');
            if (nomeCmp !== 0) {
                return asc ? nomeCmp : -nomeCmp;
            }

            // 4. Identificador único
            const idA = a.id || 0;
            const idB = b.id || 0;
            return asc ? idA - idB : idB - idA;
        } else {
            // 1. Competência
            const compA = (a.competencia || '').trim();
            const compB = (b.competencia || '').trim();
            const compCmp = compA.localeCompare(compB);
            if (compCmp !== 0) {
                return asc ? compCmp : -compCmp;
            }

            // 2. Data de lançamento
            const lancA = a.dataLancamento || '';
            const lancB = b.dataLancamento || '';
            const lancCmp = lancA.localeCompare(lancB);
            if (lancCmp !== 0) {
                return asc ? lancCmp : -lancCmp;
            }

            // 3. Nome
            const nomeA = a.nome || '';
            const nomeB = b.nome || '';
            const nomeCmp = nomeA.localeCompare(nomeB, 'pt-BR');
            if (nomeCmp !== 0) {
                return asc ? nomeCmp : -nomeCmp;
            }

            // 4. Identificador único
            const idA = a.id || 0;
            const idB = b.id || 0;
            return asc ? idA - idB : idB - idA;
        }
    });
}

function formatarMoeda(valor) {
    const num = Number(valor != null ? valor : 0);
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(num);
}

function formatarCompetencia(competencia) {
    if (!competencia) return '';
    const [ano, mes] = competencia.split('-');
    return `${mes}/${ano}`;
}

function badgeSituacao(r) {
    const cod = situacaoCodigo(r);
    if (cod === 'EXCLUIDA') return `<span class="badge bg-red text-red-fg">${cfg().labelExcluida || 'Excluída'}</span>`;
    if (cod === 'RECEBIDA') return `<span class="badge bg-green text-green-fg">${cfg().labelRecebida || 'Recebida'}</span>`;
    return `<span class="badge bg-secondary text-secondary-fg">${cfg().labelPrevista || 'Prevista'}</span>`;
}

function labelOrigem(origem) {
    if (origem === 'OPEN_FINANCE') return cfg().labelOrigemOpenFinance || 'Open Finance';
    if (origem === 'IMPORTACAO') return cfg().labelOrigemImportacao || 'Importação';
    return cfg().labelOrigemManual || 'Manual';
}

function atualizarCabecalhoOrdenacao() {
    document.querySelectorAll('#tabelaReceitas thead th.sortable').forEach(th => {
        const col = th.dataset.col;
        const iconeExistente = th.querySelector('.icone-ordenacao');
        if (iconeExistente) iconeExistente.remove();

        if (ordenacao.col === col) {
            const icone = document.createElement('i');
            icone.className = `icone-ordenacao ph ${ordenacao.asc ? 'ph-caret-up' : 'ph-caret-down'} ms-1`;
            th.appendChild(icone);
        }
    });
}

function render() {
    const filtradas = filtrar(todas);
    const lista = ordenar(filtradas);
    corpo.innerHTML = '';
    atualizarCabecalhoOrdenacao();

    // Totalizador por competência única
    const f = obterFiltroAtual();
    const cardTotalizador = document.getElementById('cardTotalizadorReceita');
    if (cardTotalizador) {
        if (f.competenciaInicial && f.competenciaInicial === f.competenciaFinal) {
            const totalRecebido = filtradas
                .filter(r => r.recebido && !r.excluido)
                .reduce((acc, r) => acc + (r.valor != null ? Number(r.valor) : 0), 0);
            const totalPrevisto = filtradas
                .filter(r => !r.recebido && !r.excluido)
                .reduce((acc, r) => acc + (r.valor != null ? Number(r.valor) : 0), 0);
            const totalGeral = totalRecebido + totalPrevisto;

            const elRec = document.getElementById('totalReceitaRecebido');
            const elPrev = document.getElementById('totalReceitaPrevisto');
            const elGeral = document.getElementById('totalReceitaGeral');
            if (elRec) elRec.textContent = formatarMoeda(totalRecebido);
            if (elPrev) elPrev.textContent = formatarMoeda(totalPrevisto);
            if (elGeral) elGeral.textContent = formatarMoeda(totalGeral);
            cardTotalizador.style.display = '';
        } else {
            cardTotalizador.style.display = 'none';
        }
    }

    if (lista.length === 0) {
        corpo.innerHTML = `<tr><td colspan="11" class="text-center text-secondary">${cfg().msgFiltroVazio}</td></tr>`;
    } else {
        lista.forEach(r => {
            const tr = document.createElement('tr');

            const categoriaHtml = r.categoriaNome
                ? `<span class="badge bg-blue-lt">${r.categoriaNome}</span>`
                : '<span class="text-muted">—</span>';

            const valorHtml = `<span class="fw-bold">${formatarMoeda(r.valor)}</span>`;

            let acaoHtml = '';
            if (!r.excluido) {
                if (pode.editar()) {
                    acaoHtml += `<button type="button" class="btn btn-action" data-acao="editar" data-id="${r.id}" title="Editar receita" aria-label="Editar receita">
                        <i class="ph ph-pencil-simple" aria-hidden="true"></i>
                    </button> `;
                }
                if (pode.inserir()) {
                    acaoHtml += `<button type="button" class="btn btn-action" data-acao="duplicar" data-id="${r.id}" title="Duplicar receita" aria-label="Duplicar receita">
                        <i class="ph ph-copy" aria-hidden="true"></i>
                    </button> `;
                }
                if (pode.receber() && !r.recebido) {
                    acaoHtml += `<button type="button" class="btn btn-action text-success" data-acao="registrar-recebimento" data-id="${r.id}" data-valor="${r.valor}" title="Registrar recebimento" aria-label="Registrar recebimento">
                        <i class="ph ph-money" aria-hidden="true"></i>
                    </button> `;
                }
                if (pode.excluir() && r.origem === 'MANUAL') {
                    acaoHtml += `<button type="button" class="btn btn-action text-danger" data-acao="excluir" data-id="${r.id}" data-nome="${r.nome}" title="Excluir receita" aria-label="Excluir receita">
                        <i class="ph ph-trash" aria-hidden="true"></i>
                    </button>`;
                }
            }

            const chkHtml = r.excluido
                ? ''
                : `<input class="form-check-input chk-receita" type="checkbox" data-id="${r.id}" ${selecionadasMap.has(String(r.id)) ? 'checked' : ''}>`;

            tr.innerHTML = `
                <td>${chkHtml}</td>
                <td>${formatarCompetencia(r.competencia)}</td>
                <td><strong>${r.nome}</strong></td>
                <td>${categoriaHtml}</td>
                <td>${r.contaDescricao || '<span class="text-muted">—</span>'}</td>
                <td class="text-end">${valorHtml}</td>
                <td>${dataBr(r.dataLancamento)}</td>
                <td>${badgeSituacao(r)}</td>
                <td>${r.dataRecebimento ? dataBr(r.dataRecebimento) : '<span class="text-muted">—</span>'}</td>
                <td>${labelOrigem(r.origem)}</td>
                <td class="col-acoes text-start"><div class="d-flex gap-1 justify-content-start">${acaoHtml}</div></td>
            `;

            corpo.appendChild(tr);
        });
    }

    rodape.textContent = `Mostrando ${lista.length} de ${todas.length} receitas`;
}

async function duplicar(ids) {
    if (!ids || ids.length === 0) return;
    try {
        const params = new URLSearchParams();
        ids.forEach(id => params.append('ids', id));
        const res = await enviar(cfg().urlDuplicar, 'POST', params);
        if (res.sucesso) {
            exibirToast(res.mensagem || 'Receita(s) duplicada(s) com sucesso.', 'success');
            selecionadasMap.clear();
            atualizarBotaoDuplicarLote();
            if (chkTodos) chkTodos.checked = false;
            await carregar();
        } else {
            const erroMsg = res.errosNegocio?.ids || res.mensagem || 'Erro ao duplicar receitas.';
            exibirToast(erroMsg, 'danger');
        }
    } catch (e) {
        exibirToast('Erro de comunicação ao duplicar receitas.', 'danger');
    }
}

function inicializarAcoes() {
    corpo.addEventListener('click', function (e) {
        const btn = e.target.closest('button[data-acao]');
        if (!btn) return;
        const acao = btn.dataset.acao;
        const id = btn.dataset.id;
        const valor = btn.dataset.valor;
        const nome = btn.dataset.nome;

        if (acao === 'editar') {
            abrirEdicao(id);
        } else if (acao === 'duplicar') {
            duplicar([id]);
        } else if (acao === 'registrar-recebimento') {
            abrirRecebimento(id, valor);
        } else if (acao === 'excluir') {
            excluir(id, nome);
        }
    });

    corpo.addEventListener('change', function (e) {
        const chk = e.target.closest('.chk-receita');
        if (!chk) return;
        const id = chk.dataset.id;
        if (chk.checked) {
            selecionadasMap.set(id, true);
        } else {
            selecionadasMap.delete(id);
        }
        atualizarBotaoDuplicarLote();
    });

    if (chkTodos) {
        chkTodos.addEventListener('change', function () {
            const checks = corpo.querySelectorAll('.chk-receita');
            checks.forEach(c => {
                c.checked = chkTodos.checked;
                const id = c.dataset.id;
                if (chkTodos.checked) {
                    selecionadasMap.set(id, true);
                } else {
                    selecionadasMap.delete(id);
                }
            });
            atualizarBotaoDuplicarLote();
        });
    }

    if (btnDuplicarLote) {
        btnDuplicarLote.addEventListener('click', function () {
            const ids = Array.from(selecionadasMap.keys());
            duplicar(ids);
        });
    }
}

function inicializarOrdenacao() {
    document.querySelectorAll('#tabelaReceitas thead th.sortable').forEach(th => {
        th.addEventListener('click', function () {
            const col = this.dataset.col;
            if (ordenacao.col === col) {
                ordenacao.asc = !ordenacao.asc;
            } else {
                ordenacao.col = col;
                ordenacao.asc = true;
            }
            render();
        });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    inicializarForm();
    inicializarRecebimento();
    inicializarFiltro(() => render());
    inicializarOrdenacao();
    inicializarAcoes();

    const btnFiltrar = document.getElementById('btnFiltrar');
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', function (e) {
            e.preventDefault();
            abrirModalFiltro();
        });
    }

    document.addEventListener(EVENTO_ALTERADO, carregar);
    document.addEventListener(EVENTO_RECEBIMENTO_REGISTRADO, carregar);

    carregar();
});
