import { enviar } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';
import { parseDecimal } from '../comum/mascara.js';
import { EVENTO_ALTERADO } from './modal-form.js';

const cfg = () => document.getElementById('dadosTelaConta').dataset;
const form = () => document.getElementById('formAjusteSaldo');

function limparErros() {
    const f = form();
    if (!f) return;
    f.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    f.querySelectorAll('.invalid-feedback').forEach(el => (el.textContent = ''));
    const alerta = document.getElementById('alertaFormAjusteSaldo');
    if (alerta) {
        alerta.style.display = 'none';
        alerta.textContent = '';
    }
}

export function abrirAjusteSaldo(id, saldoAtual, moeda = 'BRL') {
    limparErros();
    const f = form();
    if (!f) return;
    f.reset();

    document.getElementById('ajusteContaId').value = id;

    const saldoNum = typeof saldoAtual === 'number' ? saldoAtual : parseFloat(saldoAtual || 0);
    const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: moeda || 'BRL' });
    document.getElementById('ajusteSaldoAtual').value = formatador.format(saldoNum);
    document.getElementById('ajusteNovoSaldo').value = '';
    document.getElementById('ajusteObservacao').value = '';

    abrirModal('modalAjusteSaldo');
}

export function inicializarAjusteSaldo() {
    const f = form();
    if (!f) return;

    f.addEventListener('submit', async function (e) {
        e.preventDefault();
        limparErros();

        const id = document.getElementById('ajusteContaId').value;
        const novoSaldoInput = document.getElementById('ajusteNovoSaldo').value || '';
        const observacao = document.getElementById('ajusteObservacao').value || '';

        let novoSaldoNum = parseDecimal(novoSaldoInput).toFixed(2);

        const body = new URLSearchParams();
        body.append('novoSaldo', novoSaldoNum);
        if (observacao) {
            body.append('observacao', observacao);
        }

        try {
            const resp = await enviar(`${cfg().urlAjustarSaldo}/${id}`, 'PUT', body);
            if (resp.sucesso) {
                fecharModal('modalAjusteSaldo');
                toast(resp.mensagem || 'Saldo ajustado com sucesso.', false);
                document.dispatchEvent(new CustomEvent(EVENTO_ALTERADO));
            } else {
                if (resp.errosCampos && resp.errosCampos.novoSaldo) {
                    const input = document.getElementById('ajusteNovoSaldo');
                    const err = document.getElementById('erroAjusteNovoSaldo');
                    if (input) input.classList.add('is-invalid');
                    if (err) err.textContent = resp.errosCampos.novoSaldo;
                }
                if (resp.errosNegocio && resp.errosNegocio.geral) {
                    const alerta = document.getElementById('alertaFormAjusteSaldo');
                    if (alerta) {
                        alerta.textContent = resp.errosNegocio.geral;
                        alerta.style.display = 'block';
                    }
                }
            }
        } catch (err) {
            toast('Erro de comunicação ao ajustar saldo.', true);
        }
    });
}
