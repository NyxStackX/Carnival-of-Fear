package com.epitech.fnah.core;

import java.util.Locale;
import java.util.Objects;

/**
 * Reconnaît un mot tapé au clavier, lettre par lettre (par exemple « HONK »
 * dans le menu). Les majuscules et minuscules sont équivalentes.
 */
public final class SecretCode {

    private final String code;
    private int matched;

    /**
     * Crée un code à reconnaître.
     *
     * @param code mot secret, non vide
     * @throws IllegalArgumentException si le mot est vide
     */
    public SecretCode(String code) {
        Objects.requireNonNull(code, "code");
        if (code.isEmpty()) {
            throw new IllegalArgumentException("code must not be empty");
        }
        this.code = code.toUpperCase(Locale.ROOT);
    }

    /**
     * Ajoute une lettre tapée.
     *
     * @param typed caractère tapé
     * @return {@code true} si cette lettre termine le mot secret
     */
    public boolean type(char typed) {
        char c = Character.toUpperCase(typed);
        if (c == code.charAt(matched)) {
            matched++;
        } else {
            matched = c == code.charAt(0) ? 1 : 0;
        }
        if (matched == code.length()) {
            matched = 0;
            return true;
        }
        return false;
    }

    /**
     * Ajoute un texte tapé (la touche peut produire plusieurs caractères ou aucun).
     *
     * @param text texte tapé, éventuellement {@code null}
     * @return {@code true} si le mot secret vient d'être terminé
     */
    public boolean type(String text) {
        boolean found = false;
        if (text != null) {
            for (char c : text.toCharArray()) {
                found |= type(c);
            }
        }
        return found;
    }
}
