package com.alanpmz.urlshortener.controller;


import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.PageResponse;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.service.UrlService;
import com.alanpmz.urlshortener.factory.CreateUrlRequestFactory;
import com.alanpmz.urlshortener.factory.PageResponseFactory;
import com.alanpmz.urlshortener.factory.UpdateUrlRequestFactory;
import com.alanpmz.urlshortener.factory.UrlResponseFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlControllerTest {

    @Mock
    UrlService urlService;

    @InjectMocks
    UrlController urlController;

    @Nested
    class Create {

        @Test
        void shouldPassCorrectParametersToService() {
            CreateUrlRequest request = CreateUrlRequestFactory.buildWithValidUrl();
            stubCreate(request);

            urlController.create(request);

            verify(urlService).createUrl(request);
        }

    }

    @Nested
    class FindById {

        @Test
        void shouldPassCorrectParametersToService(){
            Long ID = 1L;
            stubFindById(ID);

            urlController.findById(ID);

            verify(urlService).findById(ID);
        }
    }

    @Nested
    class Redirect {

        @Test
        void shouldPassCorrectParametersToService(){
            var response = stubRedirect();

            urlController.redirect(response.shortCode());

            verify(urlService).redirect(response.shortCode());
        }

        @Test
        void shouldReturn302FoundWithCorrectLocation(){
            var response = stubRedirect();

            ResponseEntity<Void> result = urlController.redirect(response.shortCode());

            assertEquals(HttpStatus.FOUND, result.getStatusCode());
            assertEquals(response.originalUrl(), result.getHeaders().getLocation().toString());
        }
    }

    @Nested
    class UpdateById {
        private final Long ID = 1L;

        @Test
        void shouldPassCorrectParametersToService(){
            UpdateUrlRequest request = UpdateUrlRequestFactory.build();

            urlController.updateById(ID, request);

            verify(urlService).updateById(ID, request);
        }

        @Test
        void shouldReturn200OkWithUpdatedBody(){
            UpdateUrlRequest request = UpdateUrlRequestFactory.build();
            UrlResponse response = UrlResponseFactory.buildWithUpdate(request.expiresAt(), request.active());

            when(urlService.updateById(ID, request))
                    .thenReturn(response);

            ResponseEntity<UrlResponse> result = urlController.updateById(ID, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(request.expiresAt(), result.getBody().expiresAt());
            assertEquals(request.active(), result.getBody().active());
        }
    }

    @Nested
    class DeleteById {

        private final Long ID = 1L;

        @Test
        void shouldPassCorrectParametersToService(){
            urlController.deleteById(ID);

            verify(urlService).deleteById(ID);
        }

        @Test
        void shouldReturn204NoContent(){
            ResponseEntity<Void> result = urlController.deleteById(ID);

            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        }
    }

    @Nested
    class FindAll {

        private final int page = 0;
        private final int size = 5;
        private final String sortBy = "id";
        private final Sort.Direction direction = Sort.Direction.ASC;

        @Test
        void shouldPassCorrectParametersToService(){
            urlController.findAll(page, size, sortBy, direction);

            verify(urlService).findAll(page, size, sortBy, direction);
        }

        @Test
        void shouldReturn200OkWithCorrectBody(){
            var response = PageResponseFactory.buildWithTwoUrls(page, size);

            when(urlService.findAll(page, size, sortBy, direction))
                    .thenReturn(response);

            ResponseEntity<PageResponse<UrlResponse>> result = urlController.findAll(page, size, sortBy, direction);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(result.getBody(), response);
        }
    }

    private UrlResponse stubCreate(CreateUrlRequest request){
        UrlResponse response = UrlResponseFactory.build();
        when(urlService.createUrl(request))
                .thenReturn(response);

        return response;
    }


    private UrlResponse stubRedirect(){
        UrlResponse response = UrlResponseFactory.build();
        when(urlService.redirect(response.shortCode()))
                .thenReturn(response);

        return response;
    }

    private UrlResponse stubFindById(Long id){
        UrlResponse response = UrlResponseFactory.build();
        when(urlService.findById(id))
                .thenReturn(response);

        return response;
    }
}
