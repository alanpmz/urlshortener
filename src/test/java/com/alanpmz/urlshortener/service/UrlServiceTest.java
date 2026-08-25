package com.alanpmz.urlshortener.service;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.exception.ShortCodeGenerationException;
import com.alanpmz.urlshortener.exception.UrlNotFoundException;
import com.alanpmz.urlshortener.model.Url;
import com.alanpmz.urlshortener.repository.UrlRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(
                urlService,
                "expirationDays",
                7L
        );
    }


    // to remember : AAA -> arrange, act, assert

    @Test
    void shouldCreateUrl(){
        CreateUrlRequest urlRequest = new CreateUrlRequest("https://google.com");
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        UrlResponse response = urlService.createUrl(urlRequest);


        assertEquals("https://google.com", response.originalUrl());
        assertEquals("abc123", response.shortCode());

        verify(shortCodeGenerator).generate();
        verify(urlRepository).saveAndFlush(argThat(url -> url.getOriginalUrl().equals(urlRequest.url()) &&
                url.getShortCode().equals("abc123")
        ));
    }

    @Test
    void shouldRetryWhenShortCodeCollisionOccurs() {
        CreateUrlRequest request = new CreateUrlRequest("https://google.com");

        when(shortCodeGenerator.generate())
                .thenReturn("abc1", "abc2", "abc3", "abc4", "abc5");

        ConstraintViolationException cause = new ConstraintViolationException(
                        "duplicate key", null, "tb_urls_short_code_key");

        DataIntegrityViolationException exception = new DataIntegrityViolationException("error", cause);

        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenThrow(exception);


        assertThrows(ShortCodeGenerationException.class,
                () -> urlService.createUrl(request)
        );

        verify(shortCodeGenerator, times(5)).generate();
        verify(urlRepository, times(5)).saveAndFlush(any());
    }

    @Test
    void shouldRetryWhenShortCodeCollisionOccursAndEventuallySucceed(){
        CreateUrlRequest urlRequest = new CreateUrlRequest("https://google.com");

        when(shortCodeGenerator.generate())
                .thenReturn("abc1", "abc2", "abc3");

        ConstraintViolationException cause = new ConstraintViolationException(
                "duplicate key", null, "tb_urls_short_code_key");

        DataIntegrityViolationException ex = new DataIntegrityViolationException("error", cause);

        Url savedUrl = new Url();
        savedUrl.setShortCode("abc3");
        savedUrl.setOriginalUrl(urlRequest.url());

        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenThrow(ex)
                .thenThrow(ex)
                .thenReturn(savedUrl);

        UrlResponse response = urlService.createUrl(urlRequest);

        assertEquals("abc3", response.shortCode());

        verify(shortCodeGenerator, times(3)).generate();
        verify(urlRepository, times(3)).saveAndFlush(any());

    }

    @Test
    void shouldFindById(){
        Url testUrl = createTestUrl();

        when(urlRepository.findById(1L))
                .thenReturn(Optional.of(testUrl));

        UrlResponse response = urlService.findById(1L);

        assertEquals(testUrl.getOriginalUrl(), response.originalUrl());
        assertEquals(testUrl.getShortCode(), response.shortCode());

        verify(urlRepository).findById(1L);
    }

    @Test
    void shouldUpdateUrl(){
        LocalDateTime oldExpiration = LocalDateTime.now().plusDays(7);
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(5);

        UpdateUrlRequest request = new UpdateUrlRequest(newExpiration, null);

        Url existingUrl = createTestUrl();
        existingUrl.setExpiresAt(oldExpiration);

        when(urlRepository.findById(1L))
                .thenReturn(Optional.of(existingUrl));

        when(urlRepository.save(any(Url.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.updateById(1L, request);

        assertEquals(newExpiration, response.expiresAt());
        assertTrue(response.active());

        verify(urlRepository).save(argThat(url ->
                url.getExpiresAt().equals(newExpiration)
        ));
    }

    @Test
    void shouldThrowWhenMissingOnDeletion(){
        when(urlRepository.existsById(1L)).thenReturn(false);

        assertThrows(UrlNotFoundException.class, () -> urlService.deleteById(1L));

        verify(urlRepository).existsById(1L);
    }

    private Url createTestUrl(){
        return new Url(
                1L,
                "https://google.com",
                "abc123",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                8L,
                true
        );
    }
}

