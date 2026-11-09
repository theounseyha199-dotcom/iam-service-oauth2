package kh.edu.iam.web.view;

import kh.edu.iam.IdentityProvider;

import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public record Page(
        String contextPath,
        String csrfName,
        String csrfToken,
        Locale locale,
        MessageSource messages,
        String languagePath) {
    public String t(String key, Object... arguments) {
        return messages.getMessage(key, arguments, locale);
    }

    public String language() {
        return locale.getLanguage().equals("km") ? "km" : "en";
    }

    public List<String> providers() {
        return Arrays.stream(IdentityProvider.values())
                .map(provider -> provider.name().toLowerCase(Locale.ROOT))
                .toList();
    }

    public String languageLink(String language) {
        return languagePath + (languagePath.contains("?") ? "&" : "?") + "lang=" + language;
    }

    public String error(String code) {
        return messages.getMessage("error." + code, null, t("error.generic"), locale);
    }

    public String scope(String scope) {
        return messages.getMessage("scope." + scope, null, scope, locale);
    }

    public String url(String path) {
        return contextPath + path;
    }
}
