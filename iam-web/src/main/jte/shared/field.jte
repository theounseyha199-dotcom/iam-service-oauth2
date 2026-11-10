@import kh.edu.iam.web.view.Page
@param Page p
@param String name
@param String value = ""
@param String type = "text"
@param String autocomplete = "off"
@param int max = 255
@param boolean required = false
@param String error = ""
@param boolean iconToggle = false
<div class="field"><label for="${name}">${p.t("field." + name)} <span class="muted">${p.t(required ? "common.required" : "common.optional")}</span></label>
<div class="input-row ${iconToggle ? "input-row-icon" : ""}"><input id="${name}" name="${name}" type="${type}" value="${value}" autocomplete="${autocomplete}" maxlength="${max}" required="${required}" aria-invalid="${Boolean.toString(!error.isEmpty())}" aria-describedby="${name}-error">
@if(type.equals("password"))
<button type="button" class="secondary password-toggle ${iconToggle ? "icon-toggle" : ""}" data-password="${name}" data-show-text="${p.t("common.show")}" data-hide-text="${p.t("common.hide")}" data-show="${p.t("common.showPassword")}" data-hide="${p.t("common.hidePassword")}" aria-label="${p.t("common.showPassword")}" aria-pressed="false" hidden>
@if(iconToggle)
<svg class="eye-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/><path class="eye-slash" d="m3 3 18 18"/></svg>
@else
${p.t("common.show")}
@endif
</button>
@endif
</div><p id="${name}-error" class="field-error" aria-live="polite">${error.isEmpty() ? "" : p.t(error)}</p></div>
