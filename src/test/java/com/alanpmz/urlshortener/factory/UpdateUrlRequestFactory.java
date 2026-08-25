package com.alanpmz.urlshortener.factory;

import com.alanpmz.urlshortener.dto.UpdateUrlRequest;

import java.time.LocalDateTime;

public class UpdateUrlRequestFactory {

    public static UpdateUrlRequest build() {
        return new UpdateUrlRequest(LocalDateTime.now().plusDays(5), true);
    }

    public static UpdateUrlRequest buildWithPastExpiration(){
        return new UpdateUrlRequest(LocalDateTime.now().minusDays(7), false);
    }
}
