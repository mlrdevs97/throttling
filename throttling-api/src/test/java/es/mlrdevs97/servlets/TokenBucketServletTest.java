package es.mlrdevs97.servlets;

import es.mlrdevs97.throttling.TokenBucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBucketServletTest {
    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private TokenBucket mockBucket;

    @InjectMocks
    private TokenBucketServlet servlet;

    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(printWriter);
    }

    @Nested
    class GetRequests {
        @Test
        void whenBucketNotConfigured_shouldReturnBadRequest() throws IOException, NoSuchFieldException, IllegalAccessException {
            // Arrange: Use reflection to set the private field to null for this test
            Field bucketField = TokenBucketServlet.class.getDeclaredField("apiBucket");
            bucketField.setAccessible(true);
            bucketField.set(servlet, null);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Token Bucket not configured"));
        }

        @Test
        void whenTokensAvailable_shouldReturnOk() throws IOException {
            // Arrange: mock bucket returns true when try consume
            when(mockBucket.tryConsume()).thenReturn(true);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(SC_OK);
            assertTrue(stringWriter.toString().contains("Request processed."));
        }

        @Test
        void shouldReturnTooManyRequestsErrorIfNoTokensAvailable() throws IOException {
            // Arrange: mock bucket returns true when try consume
            when(mockBucket.tryConsume()).thenReturn(false);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(429);
            assertTrue(stringWriter.toString().contains("Too Many Requests. Please try again later."));
        }
    }

    @Nested
    class PostRequests {
        @Test
        void whenParamsMissing_shouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn(null);
            when(req.getParameter("capacity")).thenReturn(null);

            // Act
            servlet.doPost(req, res);

            // Assert
            assertTrue(stringWriter.toString().contains("Missing 'capacity' or 'refillRate' parameters."));
        }

        @Test
        void whenParamsBlank_shouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("");
            when(req.getParameter("refillRate")).thenReturn("");

            // Act
            servlet.doPost(req, res);

            // Assert
            assertTrue(stringWriter.toString().contains("Missing 'capacity' or 'refillRate' parameters."));
        }

        @Test
        void whenParamsINVALID_shouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("invalid");
            when(req.getParameter("refillRate")).thenReturn("invalid");

            // Act
            servlet.doPost(req, res);

            // Assert
            assertTrue(stringWriter.toString().contains("Invalid 'capacity' or 'refillRate' format. Must be numbers."));
        }

        @Test
        void whenParamsNotGreaterThanZero_shouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("0");
            when(req.getParameter("refillRate")).thenReturn("0");

            // Act
            servlet.doPost(req, res);

            // Assert
            assertTrue(stringWriter.toString().contains("Invalid 'capacity' or 'refillRate' value. Must be valid positive numbers."));
        }

        @Test
        void whenParametersValid_shouldReturnOk() throws Exception {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("10");
            when(req.getParameter("refillRate")).thenReturn("5");

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_OK);
            assertTrue(stringWriter.toString().contains("Token Bucket configured successfully"));
        }
    }
}