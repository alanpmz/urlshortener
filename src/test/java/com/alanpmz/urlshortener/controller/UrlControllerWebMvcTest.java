package com.alanpmz.urlshortener.controller;

import com.alanpmz.urlshortener.dto.CreateUrlRequest;
import com.alanpmz.urlshortener.dto.UpdateUrlRequest;
import com.alanpmz.urlshortener.dto.UrlResponse;
import com.alanpmz.urlshortener.exception.ErrorMessage;
import com.alanpmz.urlshortener.exception.UrlExpiredException;
import com.alanpmz.urlshortener.exception.UrlNotFoundException;
import com.alanpmz.urlshortener.factory.CreateUrlRequestFactory;
import com.alanpmz.urlshortener.factory.PageResponseFactory;
import com.alanpmz.urlshortener.factory.UpdateUrlRequestFactory;
import com.alanpmz.urlshortener.factory.UrlResponseFactory;
import com.alanpmz.urlshortener.service.UrlService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlController.class)
public class UrlControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlService urlService;

    @Nested
    class Create {

        @Test
        void shouldReturn201CreatedWithCorrectBody() throws Exception {
            var request = CreateUrlRequestFactory.buildWithValidUrl();
            var response = UrlResponseFactory.buildWithUrl(request.url());

            stubCreateWithResponse(request, response)
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));

            verify(urlService).createUrl(request);
        }

        @Test
        void shouldCreateCorrectLocation() throws Exception {
            var request = CreateUrlRequestFactory.buildWithValidUrl();
            var response = UrlResponseFactory.buildWithUrl(request.url());

            when(urlService.createUrl(any(CreateUrlRequest.class)))
                    .thenReturn(response);

            stubCreate(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/" + response.shortCode()));
        }

        @Test
        void shouldReturn400WhenOriginalUrlIsBlank() throws Exception {
            CreateUrlRequest request = new CreateUrlRequest("");

            stubCreate(request)
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(urlService);
        }

        @Test
        void shouldReturn400WhenOriginalUrlIsInvalid() throws Exception {
            CreateUrlRequest request = new CreateUrlRequest("google");
            var path = "/urls";

            stubCreate(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.path").value(path))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").value("url"))
                    .andExpect(jsonPath("$.errors[0].message").value(ErrorMessage.INVALID_URL.getMessage()));

            verifyNoInteractions(urlService);
        }
    }

    @Nested
    class FindAll {
        @Test
        void shouldReturn200OkWithCorrectBody() throws Exception {
            var response = PageResponseFactory.buildWithTwoUrls(0, 5);

            when(urlService.findAll(anyInt(), anyInt(), anyString(), any(Sort.Direction.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/urls"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));

            int page = 0;
            int size = 5;
            String sortBy = "id";
            Sort.Direction direction = Sort.Direction.ASC;
            verify(urlService).findAll(page, size, sortBy, direction);
        }
    }

    @Nested
    class FindById {
        private final Long ID = 1L;

        @Test
        void shouldReturn200OkWithCorrectBody() throws Exception {
            var response = UrlResponseFactory.build();

            stubFindById(ID, response)
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));

            verify(urlService).findById(ID);
        }

        @Test
        void shouldReturn400UrlNotFound() throws Exception {
            when(urlService.findById(anyLong())).thenThrow(new UrlNotFoundException(ErrorMessage.URL_NOT_FOUND.getMessage()));

            mockMvc.perform(get("/urls/" + ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(ErrorMessage.URL_NOT_FOUND.getMessage()));

            verify(urlService).findById(ID);
        }


    }

    @Nested
    class Redirect {

        @Test
        void shouldReturn302FoundWithCorrectLocation() throws Exception {
            var response = UrlResponseFactory.build();

            stubRedirect(response)
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", response.originalUrl()));

            verify(urlService).redirect(response.shortCode());
        }

        @Test
        void shouldReturn410GoneWhenUrlExpired() throws Exception {
            var response = UrlResponseFactory.buildWithUpdate(null, false);

            when(urlService.redirect(anyString()))
                    .thenThrow(new UrlExpiredException(ErrorMessage.URL_EXPIRED.getMessage()));

            mockMvc.perform(get("/" + response.shortCode()))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.error").value(ErrorMessage.URL_EXPIRED.getMessage()));

            verify(urlService).redirect(response.shortCode());
        }

    }


    @Nested
    class UpdateById {

        private final Long ID = 1L;

        @Test
        void shouldReturn200OkWithUpdatedBody() throws Exception {
            var request = UpdateUrlRequestFactory.build();
            var response = UrlResponseFactory.buildWithUpdate(request.expiresAt(), request.active());

            stubUpdateById(ID, request)
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));

            verify(urlService).updateById(ID, request);
        }

        @Test
        void shouldReturn400WhenExpiresAtIsPast() throws Exception {
            var request = UpdateUrlRequestFactory.buildWithPastExpiration();

            stubUpdateById(ID, request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").value("expiresAt"))
                    .andExpect(jsonPath("$.errors[0].message").value("expiry date need to be in the future"));

            verifyNoInteractions(urlService);
        }
    }

    @Nested
    class DeleteById {

        private final Long ID = 1L;

        @Test
        void shouldReturn204NoContent() throws Exception {
            mockMvc.perform(delete("/urls/" + ID))
                    .andExpect(status().isNoContent());

            verify(urlService).deleteById(ID);
        }

        @Test
        void shouldReturn400UrlNotFound() throws Exception {
            doThrow(new UrlNotFoundException(ErrorMessage.URL_NOT_FOUND.getMessage()))
                    .when(urlService).deleteById(anyLong());

            mockMvc.perform(delete("/urls/" + ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(ErrorMessage.URL_NOT_FOUND.getMessage()));

            verify(urlService).deleteById(ID);
        }
    }

    // create mocks

    private ResultActions stubCreate(CreateUrlRequest request) throws Exception {
        return mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions stubCreateWithResponse(CreateUrlRequest request, UrlResponse response) throws Exception {
        when(urlService.createUrl(any(CreateUrlRequest.class)))
                .thenReturn(response);

        return mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    // find by id mocks

    private ResultActions stubFindById(Long id, UrlResponse response) throws Exception {
        when(urlService.findById(anyLong())).thenReturn(response);

        return mockMvc.perform(get("/urls/" + id)
                .accept(MediaType.APPLICATION_JSON));
    }

    // redirect mocks

    private ResultActions stubRedirect(UrlResponse response) throws Exception{
        when(urlService.redirect(anyString())).thenReturn(response);

        return mockMvc.perform(get("/" + response.shortCode()));
    }

    private ResultActions stubUpdateById(Long id, UpdateUrlRequest request) throws Exception {
        var response = UrlResponseFactory.buildWithUpdate(request.expiresAt(), request.active());

        when(urlService.updateById(anyLong(), any(UpdateUrlRequest.class))).thenReturn(response);

        return mockMvc.perform(patch("/urls/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

}
