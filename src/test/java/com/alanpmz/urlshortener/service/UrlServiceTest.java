package com.alanpmz.urlshortener.service;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.exception.ShortCodeGenerationException;
import com.alanpmz.urlshortener.exception.UrlExpiredException;
import com.alanpmz.urlshortener.exception.UrlNotFoundException;
import com.alanpmz.urlshortener.model.Url;
import com.alanpmz.urlshortener.repository.UrlRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "expirationDays", 7L);
    }

    @Nested
    class CreateUrl {

        @Test
        void shouldCreateUrl() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com");
            when(shortCodeGenerator.generate()).thenReturn("abc123");
            when(urlRepository.saveAndFlush(any(Url.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime before = LocalDateTime.now().plusDays(7).minusSeconds(1);
            UrlResponse response = urlService.createUrl(request);
            LocalDateTime after = LocalDateTime.now().plusDays(7).plusSeconds(1);

            assertEquals(request.url(), response.originalUrl());
            assertEquals("abc123", response.shortCode());
            assertNotNull(response.expiresAt());
            assertTrue(response.expiresAt().isAfter(before));
            assertTrue(response.expiresAt().isBefore(after));
            assertEquals(0L, response.clickCount());
            assertTrue(response.active());

            verify(shortCodeGenerator).generate();
            verify(urlRepository).saveAndFlush(argThat(url ->
                    url.getOriginalUrl().equals(request.url())
                            && url.getShortCode().equals("abc123")
            ));
        }

        @Test
        void shouldRetryFiveTimesAndThrowWhenAllShortCodesCollide() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com");
            when(shortCodeGenerator.generate())
                    .thenReturn("abc1", "abc2", "abc3", "abc4", "abc5");

            DataIntegrityViolationException collision = shortCodeCollision();
            when(urlRepository.saveAndFlush(any(Url.class))).thenThrow(collision);

            assertThrows(ShortCodeGenerationException.class,
                    () -> urlService.createUrl(request));

            verify(shortCodeGenerator, times(5)).generate();
            verify(urlRepository, times(5)).saveAndFlush(any(Url.class));
        }

        @Test
        void shouldRetryAfterCollisionAndEventuallySucceed() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com");
            when(shortCodeGenerator.generate()).thenReturn("abc1", "abc2", "abc3");

            Url savedUrl = createTestUrl();
            savedUrl.setShortCode("abc3");
            savedUrl.setOriginalUrl(request.url());

            when(urlRepository.saveAndFlush(any(Url.class)))
                    .thenThrow(shortCodeCollision())
                    .thenThrow(shortCodeCollision())
                    .thenReturn(savedUrl);

            UrlResponse response = urlService.createUrl(request);

            assertEquals("abc3", response.shortCode());
            verify(shortCodeGenerator, times(3)).generate();
            verify(urlRepository, times(3)).saveAndFlush(any(Url.class));
        }

        @Test
        void shouldRethrowUnexpectedDataIntegrityViolation() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com");
            when(shortCodeGenerator.generate()).thenReturn("abc123");

            DataIntegrityViolationException unexpected =
                    new DataIntegrityViolationException("unexpected database error");
            when(urlRepository.saveAndFlush(any(Url.class))).thenThrow(unexpected);

            DataIntegrityViolationException thrown = assertThrows(
                    DataIntegrityViolationException.class,
                    () -> urlService.createUrl(request)
            );

            assertSame(unexpected, thrown);
            verify(shortCodeGenerator).generate();
            verify(urlRepository).saveAndFlush(any(Url.class));
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnMappedPageAndUseRequestedPaginationAndSorting() {
            Url first = createTestUrl();
            Url second = createTestUrl();
            second.setId(2L);
            second.setOriginalUrl("https://openai.com");
            second.setShortCode("xyz789");

            when(urlRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(first, second)));

            var response = urlService.findAll(1, 10, "createdAt", Sort.Direction.DESC);

            assertEquals(2, response.content().size());
            assertEquals(first.getShortCode(), response.content().get(0).shortCode());
            assertEquals(second.getShortCode(), response.content().get(1).shortCode());

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(urlRepository).findAll(captor.capture());

            Pageable pageable = captor.getValue();
            assertEquals(1, pageable.getPageNumber());
            assertEquals(10, pageable.getPageSize());
            assertEquals(Sort.Direction.DESC,
                    pageable.getSort().getOrderFor("createdAt").getDirection());
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindById() {
            Url url = createTestUrl();
            when(urlRepository.findById(1L)).thenReturn(Optional.of(url));

            UrlResponse response = urlService.findById(1L);

            assertEquals(url.getOriginalUrl(), response.originalUrl());
            assertEquals(url.getShortCode(), response.shortCode());
            verify(urlRepository).findById(1L);
        }

        @Test
        void shouldThrowWhenUrlDoesNotExist() {
            when(urlRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> urlService.findById(1L));

            verify(urlRepository).findById(1L);
        }
    }

    @Nested
    class Redirect {

        @Test
        void shouldIncrementClickCountAndReturnUrlWhenActiveAndNotExpired() {
            Url url = createTestUrl();
            url.setClickCount(8L);
            when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));
            when(urlRepository.save(any(Url.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UrlResponse response = urlService.redirect("abc123");

            assertEquals(9L, response.clickCount());
            assertEquals(9L, url.getClickCount());
            verify(urlRepository).save(url);
        }

        @Test
        void shouldThrowWhenShortCodeDoesNotExist() {
            when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> urlService.redirect("missing"));

            verify(urlRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenUrlIsExpired() {
            Url url = createTestUrl();
            url.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

            assertThrows(UrlExpiredException.class,
                    () -> urlService.redirect("abc123"));

            verify(urlRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenUrlIsInactive() {
            Url url = createTestUrl();
            url.setActive(false);
            when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

            assertThrows(UrlExpiredException.class,
                    () -> urlService.redirect("abc123"));

            verify(urlRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateById {

        @Test
        void shouldUpdateExpirationAndActiveStatus() {
            Url existing = createTestUrl();
            LocalDateTime newExpiration = LocalDateTime.now().plusDays(30);
            UpdateUrlRequest request = new UpdateUrlRequest(newExpiration, false);

            when(urlRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(urlRepository.save(any(Url.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UrlResponse response = urlService.updateById(1L, request);

            assertEquals(newExpiration, response.expiresAt());
            assertFalse(response.active());
            verify(urlRepository).save(existing);
        }

        @Test
        void shouldKeepExistingValuesWhenPatchFieldsAreNull() {
            Url existing = createTestUrl();
            LocalDateTime existingExpiration = existing.getExpiresAt();
            boolean existingActive = existing.getActive();
            UpdateUrlRequest request = new UpdateUrlRequest(null, null);

            when(urlRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(urlRepository.save(any(Url.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UrlResponse response = urlService.updateById(1L, request);

            assertEquals(existingExpiration, response.expiresAt());
            assertEquals(existingActive, response.active());
            verify(urlRepository).save(existing);
        }

        @Test
        void shouldThrowWhenUpdatingMissingUrl() {
            when(urlRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> urlService.updateById(1L, new UpdateUrlRequest(null, false)));

            verify(urlRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteExistingUrl() {
            when(urlRepository.existsById(1L)).thenReturn(true);

            urlService.deleteById(1L);

            verify(urlRepository).existsById(1L);
            verify(urlRepository).deleteById(1L);
        }

        @Test
        void shouldThrowWhenMissingOnDeletion() {
            when(urlRepository.existsById(1L)).thenReturn(false);

            assertThrows(UrlNotFoundException.class,
                    () -> urlService.deleteById(1L));

            verify(urlRepository).existsById(1L);
            verify(urlRepository, never()).deleteById(anyLong());
        }
    }

    private DataIntegrityViolationException shortCodeCollision() {
        ConstraintViolationException cause = new ConstraintViolationException(
                "duplicate key", null, "tb_urls_short_code_key"
        );
        return new DataIntegrityViolationException("error", cause);
    }

    private Url createTestUrl() {
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
