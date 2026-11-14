/** Continue a validated form through Spring's existing CSRF/session/redirect flow. */
export function submitNativeForm(form, submitter) {
    // Native submit() bypasses the React onSubmit handler, preventing validation recursion.
    // Include the clicked action value, which submit() does not otherwise serialize.
    if (submitter?.name) {
        form.querySelector("[data-submit-value]")?.remove();
        const value = document.createElement("input");
        value.type = "hidden";
        value.name = submitter.name;
        value.value = submitter.value;
        value.dataset.submitValue = "true";
        form.append(value);
    }
    if (submitter?.hasAttribute("formaction"))
        form.action = submitter.formAction;
    if (submitter?.hasAttribute("formmethod"))
        form.method = submitter.formMethod;
    HTMLFormElement.prototype.submit.call(form);
}
