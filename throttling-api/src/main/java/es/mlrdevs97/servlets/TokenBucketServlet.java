package es.mlrdevs97.servlets;

import es.mlrdevs97.throttling.TokenBucket;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

public class TokenBucketServlet extends HttpServlet {
    private TokenBucket apiBucket;

    /**
     * Handles GET requests to the servlet.
     * This method attempts to consume a token from the bucket.
     * If the bucket is not configured, it returns an error.
     * If a token is available, the request is processed (HTTP 200 OK).
     * If no token is available, the request is throttled (HTTP 429 Too Many Requests).
     *
     * @param req The HttpServletRequest object that contains the client's request.
     * @param res The HttpServletResponse object that contains the servlet's response.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter writer = res.getWriter();

        if (apiBucket == null) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Token Bucket not configured. Please configure it first.\", \"currentTokens\": 0}");
            System.out.println("GET Request DENIED: Token Bucket not configured.");
            return;
        }

        if (apiBucket.tryConsume()) {
            res.setStatus(SC_OK);
            long currentTokens = apiBucket.getCurrentTokens();
            writer.println("{\"status\": \"success\", \"message\": \"Request processed.\", \"currentTokens\": " + currentTokens + "}");
            System.out.println("Request GRANTED. Current tokens: " + currentTokens);
            return;
        }

        res.setStatus(429);
        long currentTokens = apiBucket.getCurrentTokens();
        writer.println("{\"status\": \"error\", \"message\": \"Too Many Requests. Please try again later.\", \"currentTokens\": " + currentTokens + "}");
        System.out.println("Request DENIED (throttled). Current tokens: " + currentTokens);
    }

    /**
     * Handles POST requests to the servlet.
     * This method allows the user to configure the TokenBucket's capacity and refill rate.
     * Expected parameters: 'capacity' and 'refillRate'.
     *
     * @param req The HttpServletRequest object that contains the client's request.
     * @param res The HttpServletResponse object that contains the servlet's response.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter writer = res.getWriter();

        String capacityParam = req.getParameter("capacity");
        String refillRateParam = req.getParameter("refillRate");

        if (capacityParam == null || capacityParam.isEmpty() || refillRateParam == null || refillRateParam.isEmpty()) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Missing 'capacity' or 'refillRate' parameters.\"}");
            System.out.println("POST Request DENIED: Missing parameters.");
            return;
        }

        long capacity;
        long refillRate;
        try {
            capacity = Long.parseLong(capacityParam);
            refillRate = Long.parseLong(refillRateParam);
        } catch (NumberFormatException ex) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Invalid 'capacity' or 'refillRate' format. Must be numbers.\"}");
            System.out.println("POST Request DENIED: Invalid number format for parameters.");
            return;
        }

        if (capacity <= 0 || refillRate <= 0) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Invalid 'capacity' or 'refillRate' value. Must be valid positive numbers.\"}");
            System.out.println("POST Request DENIED: Invalid parameter values.");
            return;
        }

        this.apiBucket = new TokenBucket(capacity, refillRate);
        res.setStatus(SC_OK);
        writer.println("{\"status\": \"success\", \"message\": \"Token Bucket configured successfully.\", \"capacity\": " + capacity + ", \"refillRate\": " + refillRate + "}");
        System.out.println("POST Request GRANTED: Token Bucket configured with Capacity=" + capacity + ", RefillRate=" + refillRate + " tokens/sec.");
    }
}
