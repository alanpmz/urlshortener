package com.alanpmz.urlshortener.factory;

import com.alanpmz.urlshortener.dto.PageResponse;
import com.alanpmz.urlshortener.dto.UrlResponse;
import java.util.List;

public class PageResponseFactory {
    public static PageResponse<UrlResponse> buildWithTwoUrls(int page, int size) {
        return new PageResponse<>(
                List.of(UrlResponseFactory.build(),
                        UrlResponseFactory.buildWithUrlAndShortCode("https://www.youtube.com", "abc222")),
                page,
                size,
                2,
                1
                );
    }
}
