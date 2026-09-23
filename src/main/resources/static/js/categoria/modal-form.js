import { abrirModal } from '../comum/ui.js';

// Catálogo do seletor de ícone de categoria. As chaves são o valor gravado em
// CATE_ICONE — não renomear (dados existentes dependem delas); só o ícone Phosphor
// associado a cada chave pode mudar.
export const ICONES_CATEGORIA = {
    tag: 'ph-tag',
    home: 'ph-house',
    cart: 'ph-shopping-cart-simple',
    car: 'ph-car',
    heart: 'ph-heart',
    book: 'ph-book',
    plane: 'ph-airplane-tilt',
    coins: 'ph-coins',
    money: 'ph-money',
    gift: 'ph-gift',
    'hand-coins': 'ph-hand-coins',
    sun: 'ph-sun',
    'trend-up': 'ph-trend-up',
    'fork-knife': 'ph-fork-knife',
    popcorn: 'ph-popcorn',
    't-shirt': 'ph-t-shirt',
    bus: 'ph-bus',
    heartbeat: 'ph-heartbeat',
    'graduation-cap': 'ph-graduation-cap',
    wrench: 'ph-wrench',
    bank: 'ph-bank',
    'credit-card': 'ph-credit-card',
    receipt: 'ph-receipt',
    buildings: 'ph-buildings',
    'dots-three-circle': 'ph-dots-three-circle'
};

export function obterIconeHtml(chave, tamanhoPx = 18) {
    const classe = ICONES_CATEGORIA[chave] || ICONES_CATEGORIA.tag;
    return `<i class="ph ${classe}" style="font-size:${tamanhoPx}px" aria-hidden="true"></i>`;
}

export function initModalForm() {
    const modalEl = document.getElementById('modalCategoriaForm');
    if (!modalEl) return;

    const form = document.getElementById('formCategoria');
    const inputHttp = document.getElementById('categoriaHttpMethod');
    const inputId = document.getElementById('categoriaId');
    const inputSistema = document.getElementById('categoriaSistema');
    const inputCodigo = document.getElementById('categoriaCodigo');
    const inputNome = document.getElementById('categoriaNome');
    const selectAplicaA = document.getElementById('categoriaAplicaA');
    const inputCor = document.getElementById('categoriaCor');
    const btnLimparCor = document.getElementById('btnLimparCor');
    const inputIcone = document.getElementById('categoriaIcone');
    const iconPickerBox = document.getElementById('iconPickerBox');
    const checkAtivo = document.getElementById('categoriaAtivo');
    const blocoAtivo = document.getElementById('blocoAtivo');
    const tituloModal = document.getElementById('modalCategoriaTitulo');
    const hintCodigoSistema = document.getElementById('hintCodigoSistema');
    const avisoUso = document.getElementById('avisoCategoriaEmUso');
    const textoAvisoUso = document.getElementById('textoAvisoCategoriaEmUso');
    const previewBox = document.getElementById('previewIconeCor');
    const previewIcone = document.getElementById('previewIcone');

    // Constrói o seletor de ícones
    function montarIconPicker() {
        if (!iconPickerBox) return;
        iconPickerBox.innerHTML = '';
        Object.keys(ICONES_CATEGORIA).forEach(chave => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-icon btn-sm btn-outline-secondary';
            btn.dataset.iconeKey = chave;
            btn.title = chave;
            btn.innerHTML = obterIconeHtml(chave, 18);
            btn.addEventListener('click', () => {
                const atual = inputIcone ? inputIcone.value : '';
                if (inputIcone) {
                    inputIcone.value = (atual === chave ? '' : chave);
                }
                sincronizarIconPicker();
                atualizarPreview();
            });
            iconPickerBox.appendChild(btn);
        });
        sincronizarIconPicker();
    }

    function sincronizarIconPicker() {
        if (!iconPickerBox) return;
        const atual = inputIcone ? inputIcone.value : '';
        iconPickerBox.querySelectorAll('button').forEach(btn => {
            if (btn.dataset.iconeKey === atual) {
                btn.className = 'btn btn-icon btn-sm btn-primary active';
            } else {
                btn.className = 'btn btn-icon btn-sm btn-outline-secondary';
            }
        });
    }

    function atualizarPreview() {
        if (!previewBox) return;
        const cor = inputCor ? inputCor.value : '';
        const icone = inputIcone ? inputIcone.value.trim() : '';

        if (icone || cor) {
            previewBox.style.display = 'inline-flex';
            if (previewIcone) {
                previewIcone.innerHTML = obterIconeHtml(icone || 'tag', 16);
                if (cor) {
                    previewIcone.style.color = cor;
                } else {
                    previewIcone.style.color = '';
                }
            }
        } else {
            previewBox.style.display = 'none';
        }
    }

    // Normalização em tempo real do campo Código (RT08)
    if (inputCodigo) {
        const normalizar = () => {
            const original = inputCodigo.value;
            const norm = original.normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
                .replace(/[\s-]+/g, '_')
                .replace(/[^a-zA-Z0-9_]/g, '')
                .toUpperCase();
            if (original !== norm) {
                inputCodigo.value = norm;
            }
        };
        inputCodigo.addEventListener('input', normalizar);
        inputCodigo.addEventListener('blur', normalizar);
    }

    if (inputCor) inputCor.addEventListener('input', atualizarPreview);
    if (btnLimparCor) {
        btnLimparCor.addEventListener('click', () => {
            if (inputCor) inputCor.value = '#206bc4';
            atualizarPreview();
        });
    }

    montarIconPicker();
    atualizarPreview();

    window.abrirModalNovaCategoria = function() {
        if (!form) return;
        form.action = '/categorias/inserir';
        if (inputHttp) inputHttp.value = 'POST';
        if (inputId) inputId.value = '';
        if (inputSistema) inputSistema.value = 'false';
        if (inputCodigo) {
            inputCodigo.value = '';
            inputCodigo.readOnly = false;
        }
        if (inputNome) inputNome.value = '';
        if (selectAplicaA) selectAplicaA.value = '';
        if (inputCor) inputCor.value = '#206bc4';
        if (inputIcone) inputIcone.value = '';
        if (checkAtivo) checkAtivo.checked = true;
        if (blocoAtivo) blocoAtivo.style.display = 'none';
        if (hintCodigoSistema) hintCodigoSistema.style.display = 'none';
        if (avisoUso) avisoUso.style.display = 'none';
        if (tituloModal) tituloModal.textContent = 'Nova categoria';

        sincronizarIconPicker();
        atualizarPreview();
        abrirModal('modalCategoriaForm');
    };

    window.abrirModalEditarCategoria = function(tr) {
        if (!form || !tr) return;
        const id = tr.getAttribute('data-id');
        const codigo = tr.getAttribute('data-codigo');
        const nome = tr.getAttribute('data-nome');
        const aplicaA = tr.getAttribute('data-aplica-a');
        const cor = tr.getAttribute('data-cor') || '#206bc4';
        const icone = tr.getAttribute('data-icone') || '';
        const sistema = tr.getAttribute('data-sistema') === 'true';
        const ativo = tr.getAttribute('data-ativo') === 'true';
        const qtdUso = parseInt(tr.getAttribute('data-qtd-uso') || '0', 10);

        form.action = '/categorias/editar/' + id;
        if (inputHttp) inputHttp.value = 'PUT';
        if (inputId) inputId.value = id;
        if (inputSistema) inputSistema.value = sistema ? 'true' : 'false';
        if (inputCodigo) {
            inputCodigo.value = codigo;
            inputCodigo.readOnly = sistema;
        }
        if (inputNome) inputNome.value = nome;
        if (selectAplicaA) selectAplicaA.value = aplicaA;
        if (inputCor) inputCor.value = cor;
        if (inputIcone) inputIcone.value = icone;
        if (checkAtivo) checkAtivo.checked = ativo;
        if (blocoAtivo) blocoAtivo.style.display = 'block';

        if (hintCodigoSistema) {
            hintCodigoSistema.style.display = sistema ? 'block' : 'none';
        }

        if (avisoUso && textoAvisoUso) {
            if (qtdUso > 0) {
                textoAvisoUso.textContent = `Esta categoria é usada por ${qtdUso} lançamento(s). Ela não pode ser excluída; você pode desativá-la.`;
                avisoUso.style.display = 'block';
            } else {
                avisoUso.style.display = 'none';
            }
        }

        if (tituloModal) tituloModal.textContent = 'Editar categoria';
        sincronizarIconPicker();
        atualizarPreview();
        abrirModal('modalCategoriaForm');
    };

    // Reabre o modal automaticamente se houve erro de validação no servidor
    const dadosTela = document.getElementById('dadosTela');
    if (dadosTela && dadosTela.dataset.abrirModalForm === 'true') {
        sincronizarIconPicker();
        atualizarPreview();
        abrirModal('modalCategoriaForm');
    }
}

