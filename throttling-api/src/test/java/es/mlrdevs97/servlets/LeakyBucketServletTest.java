package es.mlrdevs97.servlets;

import es.mlrdevs97.throttling.LeakyBucket;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeakyBucketServletTest {
    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private LeakyBucket mockBucket;

    @InjectMocks
    private LeakyBucketServlet servlet;

    private StringWriter stringWriter;

    @BeforeEach
    void setup() throws IOException {
        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(printWriter);
    }

    @Nested
    class getRequests {
        @Test
        void whenBucketIsNotConfigured_thenShouldReturnBadRequest() throws NoSuchFieldException, IllegalAccessException, IOException {
            // Arrange: Use reflection to set the private field to null for this test
            Field bucketField = LeakyBucketServlet.class.getDeclaredField("apiBucket");
            bucketField.setAccessible(true);
            bucketField.set(servlet, null);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Leaky Bucket not configured"));
        }

        @Test
        void whenBucketIsNotFull_thenShouldReturnOk() throws IOException {
            // Arrange
            when(mockBucket.tryAdd()).thenReturn(true);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(SC_OK);
            assertTrue(stringWriter.toString().contains("Request processed."));
        }

        @Test
        void whenBucketIsFull_thenShouldReturn429() throws IOException {
            // Arrange
            when(mockBucket.tryAdd()).thenReturn(false);

            // Act
            servlet.doGet(req, res);

            // Assert
            verify(res).setStatus(429);
            assertTrue(stringWriter.toString().contains("Too Many Requests. Please try again later."));
        }
    }

    @Nested
    class postRequests {
        @Test
        void WhenParamsAreMissing_thenShouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn(null);
            when(req.getParameter("leakRate")).thenReturn(null);

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Missing 'capacity' or 'leakRate' parameters."));
        }

        @Test
        void WhenParamsAreBlank_thenShouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("");
            when(req.getParameter("leakRate")).thenReturn("");

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Missing 'capacity' or 'leakRate' parameters."));
        }

        @Test
        void whenParamsAreEInvalidFormat_thenShouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("invalid");
            when(req.getParameter("leakRate")).thenReturn("invalid");

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Invalid 'capacity' or 'leakRate' format. Must be numbers."));
        }

        @Test
        void whenParamsAreNotGreaterThanZero_thenShouldReturnBadRequest() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("0");
            when(req.getParameter("leakRate")).thenReturn("0");

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_BAD_REQUEST);
            assertTrue(stringWriter.toString().contains("Invalid 'capacity' or 'leakRate' value. Must be valid positive numbers."));
        }

        @Test
        void whenParamsAreCorrect_thenShouldReturnOk() throws IOException {
            // Arrange
            when(req.getParameter("capacity")).thenReturn("10");
            when(req.getParameter("leakRate")).thenReturn("2");

            // Act
            servlet.doPost(req, res);

            // Assert
            verify(res).setStatus(SC_OK);
            assertTrue(stringWriter.toString().contains("Leaky Bucket configured successfully"));
        }
    }
}
