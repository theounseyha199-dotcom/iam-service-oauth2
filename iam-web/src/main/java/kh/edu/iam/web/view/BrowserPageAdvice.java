package kh.edu.iam.web.view;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
public class BrowserPageAdvice {
    private final MessageSource messages;

    @ModelAttribute("p")
    public Page page(HttpServletRequest request, Locale locale) {
        var csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Locale supported = locale.getLanguage().equals("km") ? Locale.forLanguageTag("km") : Locale.ENGLISH;
        var languagePath = org.springframework.web.util.UriComponentsBuilder.fromPath(request.getRequestURI());
        if (request.getRequestURI().endsWith("/oauth2/consent")) {
            for (String key : java.util.List.of("client_id", "scope", "state")) {
                if (request.getParameter(key) != null) {
                    languagePath.queryParam(key, request.getParameter(key));
                }
            }
        }
        if (request.getRequestURI().equals(request.getContextPath()+"/") || request.getRequestURI().endsWith("/workspace")) {
            for (String key : java.util.List.of("panel", "page", "search", "status", "actor", "target", "from", "to", "suspicious")) {
                if(request.getParameter(key)!=null) languagePath.queryParam(key,request.getParameter(key));
            }
        }
        return new Page(
                request.getContextPath(),
                csrf == null ? "_csrf" : csrf.getParameterName(),
                csrf == null ? "" : csrf.getToken(),
                supported,
                messages,
                languagePath.build().encode().toUriString());
    }
}
