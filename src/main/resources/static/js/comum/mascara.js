/**
 * Utilitário de máscara de moeda em tempo real para todo o sistema (pt-BR, R$ 0,00).
 * Suporta valores positivos e negativos, formatação dinâmica e conversão para envio.
 */

export function parseDecimal(str) {
    if (str == null) return 0;
    if (typeof str === 'number') return str;
    const s = String(str).trim();
    if (!s) return 0;
    const isNeg = s.includes('-');
    const apenasDigitosEPonto = s.replace(/[^\d,]/g, '').replace(',', '.');
    const num = parseFloat(apenasDigitosEPonto) || 0;
    return isNeg ? -num : num;
}

export function formatarMoeda(valor, moeda = 'BRL') {
    const num = Number(valor != null ? valor : 0);
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: moeda || 'BRL' }).format(num);
}

export function formatarValorPtBr(val, comPrefixo = true) {
    const isNegativo = String(val || '').includes('-');
    const apenasDigitos = String(val || '').replace(/\D/g, '');
    if (!apenasDigitos || parseInt(apenasDigitos, 10) === 0) {
        return comPrefixo ? 'R$ 0,00' : '0,00';
    }
    let numero = parseInt(apenasDigitos, 10) / 100;
    if (isNegativo) numero = -numero;
    const formatado = Math.abs(numero).toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    const prefixo = isNegativo ? '-R$ ' : 'R$ ';
    return comPrefixo ? `${prefixo}${formatado}` : (isNegativo ? `-${formatado}` : formatado);
}

export function formatarElemento(input, comPrefixo = true) {
    if (!input) return;
    input.value = formatarValorPtBr(input.value, comPrefixo);
}

export function definirValorMoeda(input, valor, comPrefixo = true) {
    if (!input) return;
    if (valor == null || valor === '') {
        input.value = comPrefixo ? 'R$ 0,00' : '0,00';
        return;
    }
    const num = typeof valor === 'number' ? valor : parseDecimal(valor);
    const isNeg = num < 0;
    const formatado = Math.abs(num).toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    if (comPrefixo) {
        input.value = isNeg ? `-R$ ${formatado}` : `R$ ${formatado}`;
    } else {
        input.value = isNeg ? `-${formatado}` : formatado;
    }
}

export function obterValorDecimal(input) {
    if (!input) return '0.00';
    return parseDecimal(input.value).toFixed(2);
}

export function inicializarMascarasMoeda(container = document) {
    container.querySelectorAll('input.mascara-moeda, input[data-mascara="moeda"]').forEach((input) => {
        formatarElemento(input);
    });
}

// Inicialização e deleção de eventos global
if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        inicializarMascarasMoeda();
    });

    document.addEventListener('focusin', (e) => {
        if (e.target && e.target.matches && e.target.matches('input.mascara-moeda, input[data-mascara="moeda"]')) {
            if (!e.target.value || e.target.value.trim() === '') {
                e.target.value = 'R$ 0,00';
            }
        }
    });

    document.addEventListener('input', (e) => {
        if (e.target && e.target.matches && e.target.matches('input.mascara-moeda, input[data-mascara="moeda"]')) {
            formatarElemento(e.target);
        }
    });
}
