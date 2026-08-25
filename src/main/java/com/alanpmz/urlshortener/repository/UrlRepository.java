package com.alanpmz.urlshortener.repository;


import com.alanpmz.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    Boolean existsByShortCode(String shortCode);
}
