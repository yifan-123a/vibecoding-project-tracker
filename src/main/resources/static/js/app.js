/*
 * VibeTracker — the only client-side script for now.
 *
 * Two jobs:
 *   1. Open / close the colour drawer, with the keyboard behaving like a dialog.
 *   2. Live preview: changing a swatch or typing a hex immediately re-binds the
 *      CSS variable on :root, so the board behind the drawer is the preview.
 *
 * Saving is a normal form POST — no fetch, no client state to keep in sync.
 */
(function () {
    'use strict';

    /* --------------------------------------------------- first-run hint + keys */

    const HINT_KEY = 'vibetracker.hint.dismissed';
    const hint = document.querySelector('[data-hint]');

    if (hint) {
        let dismissed = false;
        try {
            dismissed = window.localStorage.getItem(HINT_KEY) === '1';
        } catch (error) {
            dismissed = false;
        }
        hint.hidden = dismissed;

        const dismiss = hint.querySelector('[data-dismiss-hint]');
        if (dismiss) {
            dismiss.addEventListener('click', function () {
                hint.hidden = true;
                try {
                    window.localStorage.setItem(HINT_KEY, '1');
                } catch (error) {
                    /* private mode: the hint simply comes back next time */
                }
            });
        }
    }

    // Two shortcuts, both advertised in the hint bar. Ignored while typing.
    document.addEventListener('keydown', function (event) {
        const target = event.target;
        const typing = target && (
            target.tagName === 'INPUT' ||
            target.tagName === 'TEXTAREA' ||
            target.tagName === 'SELECT' ||
            target.isContentEditable);

        if (typing || event.metaKey || event.ctrlKey || event.altKey) {
            return;
        }
        if (document.querySelector('.modal')) {
            return; // the modal owns the keyboard while it is open
        }

        if (event.key === '/') {
            const search = document.querySelector('.search input');
            if (search) {
                event.preventDefault();
                search.focus();
                search.select();
            }
        } else if (event.key === 'n') {
            event.preventDefault();
            window.location.assign('/?create=true');
        }
    });

    const drawer = document.querySelector('[data-drawer]');
    const scrim = document.querySelector('[data-scrim]');
    const openButton = document.querySelector('[data-open-settings]');
    const closeButton = document.querySelector('[data-close-settings]');

    if (!drawer || !scrim || !openButton) {
        return;
    }

    let lastFocused = null;

    function openDrawer() {
        lastFocused = document.activeElement;
        scrim.hidden = false;
        // Next frame, so the transition has a start value to animate from.
        requestAnimationFrame(function () {
            scrim.classList.add('open');
            drawer.classList.add('open');
        });
        drawer.setAttribute('aria-hidden', 'false');
        openButton.setAttribute('aria-expanded', 'true');
        if (closeButton) {
            closeButton.focus();
        }
    }

    function closeDrawer() {
        scrim.classList.remove('open');
        drawer.classList.remove('open');
        drawer.setAttribute('aria-hidden', 'true');
        openButton.setAttribute('aria-expanded', 'false');
        window.setTimeout(function () {
            scrim.hidden = true;
        }, 200);
        if (lastFocused && typeof lastFocused.focus === 'function') {
            lastFocused.focus();
        }
    }

    openButton.addEventListener('click', openDrawer);
    if (closeButton) {
        closeButton.addEventListener('click', closeDrawer);
    }
    scrim.addEventListener('click', closeDrawer);

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && drawer.classList.contains('open')) {
            closeDrawer();
        }
    });

    /* -------------------------------------------------- live colour preview */

    const HEX = /^#?([0-9a-f]{6})$/i;

    function normalize(value) {
        const match = HEX.exec((value || '').trim());
        return match ? '#' + match[1].toUpperCase() : null;
    }

    function applyVariable(cssVar, hex) {
        if (cssVar && hex) {
            document.documentElement.style.setProperty(cssVar, hex);
        }
    }

    document.querySelectorAll('.color-row-inputs').forEach(function (row) {
        const swatch = row.querySelector('.swatch');
        const hex = row.querySelector('.hex');
        if (!swatch || !hex) {
            return;
        }
        const cssVar = swatch.getAttribute('data-css-var');

        swatch.addEventListener('input', function () {
            const value = normalize(swatch.value);
            if (value) {
                hex.value = value;
                applyVariable(cssVar, value);
            }
        });

        hex.addEventListener('input', function () {
            const value = normalize(hex.value);
            if (!value) {
                return;
            }
            swatch.value = value;
            applyVariable(cssVar, value);
            // Notice text is server-rendered; a client-side recount would be a
            // second source of truth, so the preview keeps it as-is until save.
        });

        hex.addEventListener('blur', function () {
            const value = normalize(hex.value);
            if (value) {
                hex.value = value;
            }
        });
    });

    /* ------------------------------------------------------------- task modal */

    const modal = document.querySelector('.modal');
    if (modal) {
        // The modal is server-rendered from a URL (?create / ?edit=id), so
        // "closing" is just going back to the plain board. The back button and
        // a bookmarked URL behave the same way.
        function closeModal() {
            window.location.assign('/');
        }

        document.querySelectorAll('[data-close-modal]').forEach(function (element) {
            element.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });

        const deleteButton = modal.querySelector('[data-confirm-delete]');
        if (deleteButton) {
            deleteButton.addEventListener('click', function (event) {
                if (!window.confirm("Delete this task? It won't come back.")) {
                    event.preventDefault();
                }
            });
        }

        const firstField = modal.querySelector('input:not([type=hidden]), select, textarea');
        if (firstField) {
            firstField.focus();
        }
    }

    /* -------------------------------------------------- drag between columns (M12) */
    // The modal's status dropdown stays the accessible path; this is a second
    // entry point, not a replacement. Drop → POST → reload, so derived state
    // (tints, counts, empty columns) can never drift from the database.

    const dragCards = document.querySelectorAll('.card[data-task-id]');
    const dropColumns = document.querySelectorAll('.column[data-status]');

    if (dragCards.length && dropColumns.length) {
        let draggedId = null;

        dragCards.forEach(function (card) {
            card.addEventListener('dragstart', function (event) {
                draggedId = card.dataset.taskId;
                event.dataTransfer.setData('text/plain', draggedId);
                event.dataTransfer.effectAllowed = 'move';
                card.classList.add('is-dragging');
            });

            card.addEventListener('dragend', function () {
                card.classList.remove('is-dragging');
                dropColumns.forEach(function (column) {
                    column.classList.remove('is-drop-target');
                });
            });
        });

        dropColumns.forEach(function (column) {
            column.addEventListener('dragover', function (event) {
                event.preventDefault();
                event.dataTransfer.dropEffect = 'move';
                column.classList.add('is-drop-target');
            });

            column.addEventListener('dragleave', function (event) {
                if (!column.contains(event.relatedTarget)) {
                    column.classList.remove('is-drop-target');
                }
            });

            column.addEventListener('drop', async function (event) {
                event.preventDefault();
                column.classList.remove('is-drop-target');

                const id = event.dataTransfer.getData('text/plain') || draggedId;
                const status = column.dataset.status;
                if (!id || !status) {
                    return;
                }

                const card = document.querySelector('.card[data-task-id="' + id + '"]');
                const from = card && card.closest('.column') ? card.closest('.column').dataset.status : null;
                if (from === status) {
                    // Dropped back where it started: no request, no reload.
                    return;
                }

                // Move the node first so the drop feels immediate, then confirm.
                const target = column.querySelector('.cards');
                if (card && target) {
                    target.appendChild(card);
                }

                try {
                    const response = await fetch('/tasks/' + id + '/status', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: 'status=' + encodeURIComponent(status)
                    });
                    if (!response.ok) {
                        throw new Error('HTTP ' + response.status);
                    }
                    window.location.reload();
                } catch (error) {
                    showToast('Could not move that task — reloading.');
                    window.location.reload();
                }
            });
        });
    }

    /* ------------------------------------------------------- copy context (M10) */

    function showToast(message) {
        const previous = document.querySelector('[data-js-toast]');
        if (previous) {
            previous.remove();
        }
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.setAttribute('data-js-toast', '');
        toast.textContent = message;
        document.body.appendChild(toast);
        window.setTimeout(function () {
            toast.style.transition = 'opacity 200ms ease';
            toast.style.opacity = '0';
            window.setTimeout(function () {
                toast.remove();
            }, 220);
        }, 2400);
    }

    async function copyText(text) {
        if (navigator.clipboard && window.isSecureContext) {
            try {
                await navigator.clipboard.writeText(text);
                return true;
            } catch (error) {
                /* fall through to the textarea path */
            }
        }
        // Needed when the demo is opened over plain http on a LAN address,
        // where the async clipboard API is not available.
        const area = document.createElement('textarea');
        area.value = text;
        area.setAttribute('readonly', '');
        area.style.position = 'fixed';
        area.style.top = '-1000px';
        document.body.appendChild(area);
        area.select();
        let copied = false;
        try {
            copied = document.execCommand('copy');
        } catch (error) {
            copied = false;
        }
        area.remove();
        return copied;
    }

    document.querySelectorAll('[data-copy-url]').forEach(function (button) {
        button.addEventListener('click', async function () {
            try {
                const response = await fetch(button.dataset.copyUrl, {
                    headers: { Accept: 'text/markdown' }
                });
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                const markdown = await response.text();
                const copied = await copyText(markdown);
                button.classList.toggle('copied', copied);
                showToast(copied
                    ? 'Copied. Paste it into your AI.'
                    : 'Copy was blocked — select the text manually.');
            } catch (error) {
                showToast('Copy failed — is the server still running?');
            }
        });
    });

    /* ----------------------------------------------------------------- toast */

    const toast = document.querySelector('[data-toast]');
    if (toast) {
        window.setTimeout(function () {
            toast.style.transition = 'opacity 200ms ease';
            toast.style.opacity = '0';
            window.setTimeout(function () {
                toast.remove();
            }, 220);
        }, 2400);
    }
})();
