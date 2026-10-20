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
