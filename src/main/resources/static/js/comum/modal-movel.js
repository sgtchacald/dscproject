/**
 * modal-movel.js
 *
 * Torna todas as modais do sistema arrastáveis (drag & drop pelo cabeçalho)
 * e redimensionáveis (alças nas bordas e no canto inferior direito).
 * Suporta duplo clique no cabeçalho para maximizar/restaurar e
 * restaura o estado padrão ao fechar a modal.
 */

const LARGURA_MINIMA = 320;
const ALTURA_MINIMA = 200;

function inicializarModal(modalEl) {
    if (modalEl.dataset.modalMovelInit) return;
    modalEl.dataset.modalMovelInit = 'true';

    const dialog = modalEl.querySelector('.modal-dialog');
    if (!dialog) return;

    const content = dialog.querySelector('.modal-content');
    if (!content) return;

    const header = content.querySelector('.modal-header');
    if (!header) return;

    // Injeta as alças de redimensionamento se ainda não existirem
    if (!content.querySelector('.modal-resize-se')) {
        const handleE = document.createElement('div');
        handleE.className = 'modal-resize-handle modal-resize-e';

        const handleS = document.createElement('div');
        handleS.className = 'modal-resize-handle modal-resize-s';

        const handleSE = document.createElement('div');
        handleSE.className = 'modal-resize-handle modal-resize-se';
        handleSE.title = 'Arrastar para redimensionar';

        content.appendChild(handleE);
        content.appendChild(handleS);
        content.appendChild(handleSE);

        configurarRedimensionamento(dialog, content, handleE, 'e');
        configurarRedimensionamento(dialog, content, handleS, 's');
        configurarRedimensionamento(dialog, content, handleSE, 'se');
    }

    // Injeta botão de maximizar/restaurar no cabeçalho se ainda não existir
    let btnMaximize = header.querySelector('.btn-modal-maximize');
    if (!btnMaximize) {
        btnMaximize = document.createElement('button');
        btnMaximize.type = 'button';
        btnMaximize.className = 'btn-modal-maximize';
        btnMaximize.setAttribute('aria-label', 'Maximizar');
        btnMaximize.title = 'Maximizar';
        btnMaximize.innerHTML = '<i class="ph ph-corners-out"></i>';

        const btnClose = header.querySelector('.btn-close');
        if (btnClose) {
            header.insertBefore(btnMaximize, btnClose);
        } else {
            header.appendChild(btnMaximize);
        }

        btnMaximize.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            alternarMaximizacao(dialog, content, header);
        });
    }

    configurarArrasto(dialog, content, header);
}

function alternarMaximizacao(dialog, content, header) {
    const btnMax = header ? header.querySelector('.btn-modal-maximize') : dialog.querySelector('.btn-modal-maximize');

    if (dialog.classList.contains('is-maximized')) {
        // Restaurar
        const salvo = dialog.dataset.restaurarRect ? JSON.parse(dialog.dataset.restaurarRect) : null;
        dialog.classList.remove('is-maximized');
        if (salvo) {
            dialog.style.left = `${salvo.left}px`;
            dialog.style.top = `${salvo.top}px`;
            dialog.style.width = `${salvo.width}px`;
            dialog.style.height = '';
            content.style.height = `${salvo.height}px`;
        } else {
            resetarModal(dialog, content);
        }
        if (btnMax) {
            btnMax.setAttribute('aria-label', 'Maximizar');
            btnMax.title = 'Maximizar';
            btnMax.innerHTML = '<i class="ph ph-corners-out"></i>';
        }
    } else {
        // Maximizar
        const rect = dialog.getBoundingClientRect();
        const contentRect = content.getBoundingClientRect();
        dialog.dataset.restaurarRect = JSON.stringify({
            left: rect.left,
            top: rect.top,
            width: rect.width,
            height: contentRect.height
        });

        dialog.style.margin = '0';
        dialog.style.position = 'absolute';
        dialog.style.left = '10px';
        dialog.style.top = '10px';
        dialog.style.width = 'calc(100vw - 20px)';
        dialog.style.height = 'calc(100vh - 20px)';
        dialog.style.maxWidth = 'none';
        content.style.height = '100%';
        content.style.maxHeight = 'none';
        dialog.classList.add('is-maximized');

        if (btnMax) {
            btnMax.setAttribute('aria-label', 'Restaurar');
            btnMax.title = 'Restaurar';
            btnMax.innerHTML = '<i class="ph ph-corners-in"></i>';
        }
    }
}

function resetarModal(dialog, content) {
    dialog.style.position = '';
    dialog.style.margin = '';
    dialog.style.left = '';
    dialog.style.top = '';
    dialog.style.width = '';
    dialog.style.maxWidth = '';
    dialog.style.height = '';
    dialog.style.transform = '';
    dialog.classList.remove('is-dragging', 'is-maximized');

    content.style.width = '';
    content.style.height = '';
    content.style.maxHeight = '';
    content.classList.remove('is-resized');

    delete dialog.dataset.restaurarRect;

    const btnMax = dialog.querySelector('.btn-modal-maximize');
    if (btnMax) {
        btnMax.setAttribute('aria-label', 'Maximizar');
        btnMax.title = 'Maximizar';
        btnMax.innerHTML = '<i class="ph ph-corners-out"></i>';
    }
}

function configurarArrasto(dialog, content, header) {
    let startX = 0;
    let startY = 0;
    let initialLeft = 0;
    let initialTop = 0;
    let isDragging = false;

    header.addEventListener('pointerdown', (e) => {
        // Ignora cliques em botões de fechar, maximizar, inputs ou outros controles interativos
        if (e.target.closest('.btn-close, .btn-modal-maximize, button, input, select, textarea, a, .dropdown-menu')) {
            return;
        }

        if (dialog.classList.contains('is-maximized')) {
            return;
        }

        const rect = dialog.getBoundingClientRect();

        // Se ainda não estava posicionado absolutamente, fixa a posição atual
        dialog.style.margin = '0';
        dialog.style.position = 'absolute';
        dialog.style.left = `${rect.left}px`;
        dialog.style.top = `${rect.top}px`;
        dialog.style.width = `${rect.width}px`;
        dialog.style.maxWidth = 'none';

        startX = e.clientX;
        startY = e.clientY;
        initialLeft = rect.left;
        initialTop = rect.top;
        isDragging = true;

        dialog.classList.add('is-dragging');
        header.setPointerCapture(e.pointerId);

        function onPointerMove(ev) {
            if (!isDragging) return;
            const deltaX = ev.clientX - startX;
            const deltaY = ev.clientY - startY;

            let newLeft = initialLeft + deltaX;
            let newTop = initialTop + deltaY;

            // Mantém visível dentro da janela
            const maxLeft = window.innerWidth - 60;
            const minLeft = 20 - rect.width;
            const maxTop = window.innerHeight - 50;
            const minTop = 0;

            newLeft = Math.max(minLeft, Math.min(maxLeft, newLeft));
            newTop = Math.max(minTop, Math.min(maxTop, newTop));

            dialog.style.left = `${newLeft}px`;
            dialog.style.top = `${newTop}px`;
        }

        function onPointerUp(ev) {
            isDragging = false;
            dialog.classList.remove('is-dragging');
            header.removeEventListener('pointermove', onPointerMove);
            header.removeEventListener('pointerup', onPointerUp);
            try {
                header.releasePointerCapture(ev.pointerId);
            } catch (_) {}
        }

        header.addEventListener('pointermove', onPointerMove);
        header.addEventListener('pointerup', onPointerUp);
        e.preventDefault();
    });

    // Duplo clique no header maximiza / restaura
    header.addEventListener('dblclick', (e) => {
        if (e.target.closest('.btn-close, .btn-modal-maximize, button, input, select, a')) return;
        alternarMaximizacao(dialog, content, header);
    });
}

function configurarRedimensionamento(dialog, content, handle, direcao) {
    handle.addEventListener('pointerdown', (e) => {
        const dialogRect = dialog.getBoundingClientRect();
        const contentRect = content.getBoundingClientRect();

        // Fixa dimensões atuais
        dialog.style.margin = '0';
        dialog.style.position = 'absolute';
        dialog.style.left = `${dialogRect.left}px`;
        dialog.style.top = `${dialogRect.top}px`;
        dialog.style.width = `${dialogRect.width}px`;
        dialog.style.maxWidth = 'none';
        content.style.height = `${contentRect.height}px`;
        content.style.maxHeight = 'none';

        const startX = e.clientX;
        const startY = e.clientY;
        const startWidth = dialogRect.width;
        const startHeight = contentRect.height;

        handle.setPointerCapture(e.pointerId);

        function onPointerMove(ev) {
            const deltaX = ev.clientX - startX;
            const deltaY = ev.clientY - startY;

            if (direcao === 'e' || direcao === 'se') {
                const maxW = window.innerWidth - dialogRect.left - 15;
                const newWidth = Math.max(LARGURA_MINIMA, Math.min(maxW, startWidth + deltaX));
                dialog.style.width = `${newWidth}px`;
            }

            if (direcao === 's' || direcao === 'se') {
                const maxH = window.innerHeight - dialogRect.top - 15;
                const newHeight = Math.max(ALTURA_MINIMA, Math.min(maxH, startHeight + deltaY));
                content.style.height = `${newHeight}px`;
                content.classList.add('is-resized');
            }
        }

        function onPointerUp(ev) {
            handle.removeEventListener('pointermove', onPointerMove);
            handle.removeEventListener('pointerup', onPointerUp);
            try {
                handle.releasePointerCapture(ev.pointerId);
            } catch (_) {}
        }

        handle.addEventListener('pointermove', onPointerMove);
        handle.addEventListener('pointerup', onPointerUp);
        e.preventDefault();
        e.stopPropagation();
    });
}

// Inicialização automática em todas as modais
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.modal').forEach(inicializarModal);

    // Monitora modais que abrem dinamicamente ou via Bootstrap
    document.addEventListener('show.bs.modal', (e) => {
        if (e.target && e.target.classList.contains('modal')) {
            inicializarModal(e.target);
        }
    });

    document.addEventListener('hidden.bs.modal', (e) => {
        if (e.target && e.target.classList.contains('modal')) {
            const dialog = e.target.querySelector('.modal-dialog');
            const content = dialog ? dialog.querySelector('.modal-content') : null;
            if (dialog && content) {
                resetarModal(dialog, content);
            }
        }
    });
});
