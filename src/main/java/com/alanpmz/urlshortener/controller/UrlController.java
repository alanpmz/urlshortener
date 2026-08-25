package com.alanpmz.urlshortener.controller;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.PageResponse;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/urls")
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        UrlResponse response = urlService.createUrl(request);

        URI location = URI.create("/" + response.shortCode());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/urls")
    public ResponseEntity<PageResponse<UrlResponse>> findAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "5") @Max(value = 50) int size,
                                                            @RequestParam(defaultValue = "id") String sortBy,
                                                            @RequestParam(defaultValue = "ASC") Sort.Direction direction){

        return ResponseEntity.ok(urlService.findAll(page, size, sortBy, direction));
    }

    @GetMapping("/urls/{id}")
    public ResponseEntity<UrlResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(urlService.findById(id));
    }


    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        UrlResponse url = urlService.redirect(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.originalUrl()))
                .build();
    }

    @PatchMapping("/urls/{id}")
    public ResponseEntity<UrlResponse> updateById(@PathVariable Long id, @Valid @RequestBody UpdateUrlRequest request){
        return ResponseEntity.ok(urlService.updateById(id, request));
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        urlService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
