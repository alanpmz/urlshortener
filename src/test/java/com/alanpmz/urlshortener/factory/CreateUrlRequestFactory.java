package com.alanpmz.urlshortener.factory;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;

public class CreateUrlRequestFactory {

    public static CreateUrlRequest buildWithValidUrl(){
        return new CreateUrlRequest("https://google.com");
    }

}
