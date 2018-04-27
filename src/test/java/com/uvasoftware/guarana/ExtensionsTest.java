package com.uvasoftware.guarana;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionsTest {
    @Test
    void shouldProperlyResolveContentType() {
        Assertions.assertEquals(".html", Extensions.resolveOrDefault("text/html; charset=utf-8"));
        Assertions.assertEquals(".html", Extensions.resolveOrDefault("text/html"));
        Assertions.assertEquals(".json", Extensions.resolveOrDefault("application/json; charset=latin1"));
    }
}