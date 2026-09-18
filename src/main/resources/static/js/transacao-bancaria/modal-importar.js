import { getJson, csrf } from '../comum/http.js';
import { toast, abrirModal, fecharModal } from '../comum/ui.js';

const cfg = () => document.getElementById('dadosTelaTransacao').dataset;

let contasCarregadas = false;

export async function abrirModalImportar() {
    const form = document.getElementById('formImportarOfx');
    form?.reset();

    const alerta = document.getElementById('alertaImportarOfx');
    if (alerta) {
        alerta.textContent = '';
        alerta.style.display = 'none';
    }

    if (!contasCarregadas) {
        try {
            const contas = await getJson(cfg().urlContasOpcoes);
            const selConta = document.getElementById('importarContaId');
            if (selConta) {
                selConta.innerHTML = '<option value="">Selecione a conta de destino...</option>';
                (contas || []).forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.id;
                    opt.textContent = c.descricao;
                    selConta.appendChild(opt);
                });
                contasCarregadas = true;
            }
        } catch (e) {
            console.error('Erro ao carregar contas para importação OFX', e);
        }
    }

    abrirModal('modalImportarOfx');
}

export function inicializarImportacao(onSucesso) {
    const form = document.getElementById('formImportarOfx');
    const btn = document.getElementById('btnEnviarOfx');

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const alerta = document.getElementById('alertaImportarOfx');
            if (alerta) {
                alerta.textContent = '';
                alerta.style.display = 'none';
            }

            const contaId = document.getElementById('importarContaId').value;
            const arquivoInput = document.getElementById('importarArquivoOfx');

            if (!contaId) {
                if (alerta) {
                    alerta.textContent = 'Por favor, selecione uma conta bancária.';
                    alerta.style.display = 'block';
                }
                return;
            }

            if (!arquivoInput.files || arquivoInput.files.length === 0) {
                if (alerta) {
                    alerta.textContent = 'Por favor, selecione um arquivo .ofx.';
                    alerta.style.display = 'block';
                }
                return;
            }

            const formData = new FormData();
            formData.append('contaId', contaId);
            formData.append('arquivo', arquivoInput.files[0]);

            const textoOriginal = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Processando...';

            try {
                const resp = await fetch(cfg().urlImportarOfx, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        [csrf.header]: csrf.token,
                        'Accept': 'application/json'
                    }
                });

                const data = await resp.json();

                if (resp.ok && data.sucesso) {
                    fecharModal('modalImportarOfx');
                    toast(data.mensagem || 'Importação realizada com sucesso.');
                    if (typeof onSucesso === 'function') onSucesso();
                } else {
                    if (alerta) {
                        alerta.textContent = data?.mensagem || 'Erro ao processar arquivo OFX.';
                        alerta.style.display = 'block';
                    }
                }
            } catch (err) {
                console.error('Erro ao importar OFX', err);
                if (alerta) {
                    alerta.textContent = 'Falha de comunicação durante o envio do arquivo.';
                    alerta.style.display = 'block';
                }
            } finally {
                btn.disabled = false;
                btn.innerHTML = textoOriginal;
            }
        });
    }
}
