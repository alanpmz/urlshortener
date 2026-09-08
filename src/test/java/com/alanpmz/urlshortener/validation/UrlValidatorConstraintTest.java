package com.alanpmz.urlshortener.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlValidatorConstraintTest {

    private final UrlValidatorConstraint validator = new UrlValidatorConstraint();

    @Test
    void shouldAcceptHttpAndHttpsUrls() {
        assertTrue(validator.isValid("https://example.com", null));
        assertTrue(validator.isValid("http://example.com/path?query=1", null));
    }

    @Test
    void shouldRejectUnsupportedOrMalformedUrls() {
        assertFalse(validator.isValid("google", null));
        assertFalse(validator.isValid("ftp://example.com", null));
    }

    @Test
    void shouldLeaveNullAndBlankValidationToNotBlank() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }
}
