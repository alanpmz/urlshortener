package com.alanpmz.urlshortener.service.impl;

import com.alanpmz.urlshortener.service.ShortCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@ConditionalOnProperty(
        prefix = "app.short-code",
        name = "generator",
        havingValue = "base62",
        matchIfMissing = true
)
public final class Base62CodeGenerator implements ShortCodeGenerator {

    private static final String BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62CodeGenerator() {
    }

    @Override
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }

        return code.toString();
    }
}