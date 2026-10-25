package com.epitech.fnah.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretCodeTest {

    @Test
    void recognisesTheWordInAnyCase() {
        SecretCode code = new SecretCode("honk");
        assertFalse(code.type('H'));
        assertFalse(code.type('o'));
        assertFalse(code.type('N'));
        assertTrue(code.type('k'));
    }

    @Test
    void wrongLetterRestartsButCanBeginANewAttempt() {
        SecretCode code = new SecretCode("HONK");
        assertFalse(code.type("HOH"));
        assertTrue(code.type("ONK"));
    }

    @Test
    void worksAgainAfterASuccess() {
        SecretCode code = new SecretCode("HONK");
        assertTrue(code.type("HONK"));
        assertFalse(code.type("HON"));
        assertTrue(code.type("K"));
    }

    @Test
    void ignoresNullAndEmptyText() {
        SecretCode code = new SecretCode("HONK");
        assertFalse(code.type((String) null));
        assertFalse(code.type(""));
    }

    @Test
    void rejectsAnEmptyWord() {
        assertThrows(IllegalArgumentException.class, () -> new SecretCode(""));
        assertThrows(NullPointerException.class, () -> new SecretCode(null));
    }
}
