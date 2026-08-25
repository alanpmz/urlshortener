package com.alanpmz.urlshortener.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.UrlValidator;

public class UrlValidatorConstraint
        implements ConstraintValidator<ValidUrl, String> {

    private static final UrlValidator URL_VALIDATOR =
            new UrlValidator(new String[]{"http", "https"});

    @Override
    public boolean isValid(String value,
                           ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        return URL_VALIDATOR.isValid(value);
    }
}