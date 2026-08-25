package com.alanpmz.urlshortener.exception;

import com.alanpmz.urlshortener.controller.UrlController;
import com.alanpmz.urlshortener.service.UrlService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UrlController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlService urlService;


    @Nested
    class NoResourceFound {

        @Test
        void shouldReturn404NoResourceFoundWithCorrectBody() throws Exception {
            var path = "/invalid/path";

            mockMvc.perform(get(path))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.path").value(path));

            verifyNoInteractions(urlService);
        }
    }

    @Nested
    class HttpMessageNotReadable {

        @Test
        void shouldReturn400HttpMessageNotReadableWithCorrectBody() throws Exception {
            mockMvc.perform(post("/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "url":1235,
                            }
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(ErrorMessage.INVALID_REQUEST_BODY.getMessage()))
                    .andExpect(jsonPath("$.path").value("/urls"));
        }

    }

    @Nested
    class MethodArgumentTypeMismatchException {
        @Test
        void shouldReturn400MethodArgumentTypeMismatchWithCorrectBody () throws Exception {
            mockMvc.perform(get("/urls?page=0&size=5&sortBy=a&direction=Z"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400MethodArgumentTypeMismatchWithCorrectBod () throws Exception {
            mockMvc.perform(get("/urls?page=0&size=a&sortBy=id&direction=ASC"))
                    .andExpect(status().isBadRequest());
        }
    }


}
