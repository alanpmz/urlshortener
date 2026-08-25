package com.alanpmz.urlshortener.service;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.PageResponse;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.exception.*;
import com.alanpmz.urlshortener.model.Url;
import com.alanpmz.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${app.url.expiration-days}")
    private long expirationDays;

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request) {

        log.info("Creating shortened URL.");

        for (int attempt = 1; attempt <= 5; attempt++) {
            Url url = Url.builder()
                    .originalUrl(request.url())
                    .shortCode(shortCodeGenerator.generate())
                    .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                    .build();
            try {
                Url saved = urlRepository.saveAndFlush(url);
                log.info("URL created. shortCode={}", saved.getShortCode());
                return UrlResponse.fromEntity(saved);
            } catch (DataIntegrityViolationException ex) {
                if (isShortCodeCollision(ex)) {
                    log.warn("Short code collision on attempt {}. Generating a new code.", attempt);
                    continue;
                }

                log.error("Unexpected database error while creating URL.", ex);
                throw ex;
            }
        }

        log.error("Failed to generate unique short code after {} attempts.", 5);
        throw new ShortCodeGenerationException(
                ErrorMessage.SHORT_CODE_GENERATION_ERROR.getMessage()
        );
    }

    private boolean isShortCodeCollision(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();

        return cause instanceof ConstraintViolationException cve
                && "tb_urls_short_code_key".equals(cve.getConstraintName());
    }


    public PageResponse<UrlResponse> findAll(int page, int size, String sortBy, Sort.Direction direction) {
        log.debug(
                "Listing URLs. page={}, size={}, sortBy={}, direction={}",
                page,
                size,
                sortBy,
                direction
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        return PageResponse.fromPage(urlRepository.findAll(pageable)
                .map(UrlResponse::fromEntity));
    }

    public UrlResponse findById(Long id){
        log.debug("Searching URL by id={}", id);
        return UrlResponse.fromEntity(getExistingUrl(id));
    }

    public UrlResponse redirect(String shortCode){
        log.debug("Redirect requested. shortCode={}", shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(ErrorMessage.URL_NOT_FOUND.getMessage()));

        if (url.getExpiresAt().isAfter(LocalDateTime.now()) && url.getActive()){
            url.setClickCount(url.getClickCount()+1);
            urlRepository.save(url);
            log.info("Redirecting shortCode={}", shortCode);
            return UrlResponse.fromEntity(url);
        } else {
            log.warn("Attempt to access expired URL. shortCode={}", shortCode);
            throw new UrlExpiredException(ErrorMessage.URL_EXPIRED.getMessage());
        }
    }

    public UrlResponse updateById(Long id, UpdateUrlRequest request){
        log.info("Updating URL. id={}", id);

        Url url = getExistingUrl(id);

        url.setExpiresAt(request.expiresAt() == null ? url.getExpiresAt() : request.expiresAt());
        url.setActive(request.active() == null ? url.getActive() : request.active());
        Url updated = urlRepository.save(url);

        log.info("URL updated successfully. id={}", id);

        return UrlResponse.fromEntity(updated);
    }

    public void deleteById(Long id) {
        log.info("Deleting URL. id={}", id);
        if(!urlRepository.existsById(id)) throw new UrlNotFoundException(ErrorMessage.URL_NOT_FOUND.getMessage());

        urlRepository.deleteById(id);
        log.info("URL deleted. id={}", id);
    }

    private Url getExistingUrl(Long id){
        return urlRepository.findById(id)
                .orElseThrow(() -> new UrlNotFoundException(ErrorMessage.URL_NOT_FOUND.getMessage()));
    }
}
