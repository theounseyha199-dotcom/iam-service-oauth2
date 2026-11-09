document.querySelectorAll('[data-password]').forEach(button => {
    button.hidden = false;
    button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.password);
        const showing = input.type === 'password';
        input.type = showing ? 'text' : 'password';
        button.setAttribute('aria-pressed', String(showing));
        button.setAttribute('aria-label', showing ? button.dataset.hide : button.dataset.show);
        button.textContent = showing ? button.dataset.hideText : button.dataset.showText;
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
        dirty = false;
        form.setAttribute('aria-busy', 'true');
        form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = true; });
    });
});
window.addEventListener('pageshow', () => {
    document.querySelectorAll('form[data-submit]').forEach(form => {
        form.removeAttribute('aria-busy');
        form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = false; });
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
