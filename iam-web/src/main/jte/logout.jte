@import kh.edu.iam.web.view.Page
@param Page p
@template.shared.layout(p=p, title=p.t("common.logout"), content=@`
<section class="card"><h1>${p.t("common.logout")}</h1><p class="intro">${p.t("logout.confirm")}</p>
<form method="post" action="${p.url("/logout")}" novalidate data-submit>@template.shared.csrf(p=p)
<button type="submit">${p.t("common.logout")}</button></form>
<p><a href="${p.url("/")}">${p.t("common.cancel")}</a></p></section>
`)
