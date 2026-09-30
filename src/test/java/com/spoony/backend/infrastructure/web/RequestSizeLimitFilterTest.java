package com.spoony.backend.infrastructure.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RequestSizeLimitFilterTest {

    @Test
    void shouldRejectRequestWhenDeclaredContentLengthExceedsLimit() throws Exception {
        RequestSizeLimitFilter filter = new RequestSizeLimitFilter(8);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setContent("0123456789".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentAsString()).contains("PAYLOAD_TOO_LARGE");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldPassRequestWhenContentLengthIsWithinLimit() throws Exception {
        RequestSizeLimitFilter filter = new RequestSizeLimitFilter(16);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setContent("small".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(response));
    }
}
