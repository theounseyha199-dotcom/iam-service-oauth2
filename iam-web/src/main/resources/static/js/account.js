document.querySelectorAll('[data-password]').forEach(button => {
    button.hidden = false;
    button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.password);
        const showing = input.type === 'password';
        input.type = showing ? 'text' : 'password';
        button.setAttribute('aria-pressed', String(showing));
        button.setAttribute('aria-label', showing ? button.dataset.hide : button.dataset.show);
        if (!button.classList.contains("icon-toggle")) button.textContent = showing ? button.dataset.hideText : button.dataset.showText;
        input.focus();
    });
});
let dirty = false;
document.querySelectorAll('form[data-dirty]').forEach(form => {
    form.addEventListener('input', () => { dirty = true; });
});
document.querySelectorAll('form[data-submit]').forEach(form => {
    form.addEventListener('submit', event => {
        if (event.isComposing || form.getAttribute('aria-busy') === 'true') { event.preventDefault(); return; }
        if (form.matches('[data-login]')) {
            let firstInvalid = null;
            form.querySelectorAll('input[required]').forEach(input => {
                const empty = input.type === 'password' ? input.value.length === 0 : input.value.trim().length === 0;
                input.setAttribute('aria-invalid', String(empty));
                const error = document.getElementById(`${input.id}-error`);
                if (error) error.textContent = empty ? (input.dataset.requiredMessage || form.dataset.passwordRequired) : '';
                if (empty && !firstInvalid) firstInvalid = input;
            });
            if (firstInvalid) { event.preventDefault(); firstInvalid.focus(); return; }
        }
        dirty = false;
        form.setAttribute('aria-busy', 'true');
        form.querySelectorAll('button[type="submit"]').forEach(button => {
            button.disabled = true;
            const label = button.querySelector('[data-submit-label]');
            if (label && button.dataset.busyText) {
                button.dataset.idleText = label.textContent;
                label.textContent = button.dataset.busyText;
            }
        });
    });
});
window.addEventListener('pageshow', () => {
    document.querySelectorAll('form[data-submit]').forEach(form => {
        form.removeAttribute('aria-busy');
        form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = false;
            const label = button.querySelector('[data-submit-label]');
            if (label && button.dataset.idleText) label.textContent = button.dataset.idleText; });
    });
});
window.addEventListener('beforeunload', event => {
    if (dirty) { event.preventDefault(); event.returnValue = ''; }
});
const invalid = document.querySelector('[aria-invalid="true"]') || document.querySelector('[role="alert"][tabindex]');
if (invalid) invalid.focus();

const unsavedDialog = document.getElementById('unsaved-dialog');
let destination = null;
let originalFocus = null;
document.querySelectorAll('a[href]').forEach(link => {
    link.addEventListener('click', event => {
        if (!dirty || event.ctrlKey || event.metaKey || event.shiftKey || link.target === '_blank') return;
        event.preventDefault();
        destination = link.href;
        originalFocus = link;
        unsavedDialog.showModal();
        document.getElementById('unsaved-stay').focus();
    });
});
if (unsavedDialog) {
    document.getElementById('unsaved-stay').addEventListener('click', () => unsavedDialog.close());
    document.getElementById('unsaved-leave').addEventListener('click', () => {
        dirty = false;
        window.location.assign(destination);
    });
    unsavedDialog.addEventListener('close', () => { if (originalFocus) originalFocus.focus(); });
}

document.querySelectorAll('form[data-login] input[required]').forEach(input => {
    input.addEventListener('input', () => {
        if (input.value.length > 0) {
            input.removeAttribute('aria-invalid');
            document.getElementById(`${input.id}-error`).textContent = '';
        }
    });
});
