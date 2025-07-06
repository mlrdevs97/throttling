package es.mlrdevs97.servlets;

import es.mlrdevs97.throttling.LeakyBucket;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

/**
 * A servlet that demonstrates the Leaky Bucket algorithm for rate limiting API requests.
 */
public class LeakyBucketServlet extends HttpServlet {
    // The LeakyBucket instance used for rate limiting.
    private LeakyBucket apiBucket;

    /**
     * Handles GET requests to the servlet.
     * This method attempts to add a request to the leaky bucket.
     * If the bucket is not configured, it returns an error.
     * If there is space in the bucket, the request is accepted (HTTP 200 OK).
     * If the bucket is full, the request is throttled (HTTP 429 Too Many Requests).
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
            writer.println("{\"status\": \"error\", \"message\": \"Leaky Bucket not configured. Please configure it first.\", \"currentSize\": 0}");
            System.out.println("GET Request DENIED: Leaky Bucket not configured.");
            return;
        }

        if (apiBucket.tryAdd()) {
            res.setStatus(SC_OK);
            long currentSize = apiBucket.getCurrentSize();
            writer.println("{\"status\": \"success\", \"message\": \"Request processed.\", \"currentSize\": " + currentSize + "}");
            System.out.println("Request GRANTED. Current size: " + currentSize);
            return;
        }

        res.setStatus(429);
        long currentSize = apiBucket.getCurrentSize();
        writer.println("{\"status\": \"error\", \"message\": \"Too Many Requests. Please try again later.\", \"currentSize\": " + currentSize + "}");
        System.out.println("Request DENIED (throttled). Current size: " + currentSize);
    }

    /**
     * Handles POST requests to the servlet.
     * This method allows the user to configure the LeakyBucket's capacity and leak rate.
     * Expected parameters: 'capacity' and 'leakRate'.
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
        String leakRateParam = req.getParameter("leakRate");

        if (capacityParam == null || capacityParam.isEmpty() || leakRateParam == null || leakRateParam.isEmpty()) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Missing 'capacity' or 'leakRate' parameters.\"}");
            System.out.println("POST Request DENIED: Missing parameters.");
            return;
        }

        long capacity;
        long leakRate;
        try {
            capacity = Long.parseLong(capacityParam);
            leakRate = Long.parseLong(leakRateParam);
        } catch (NumberFormatException ex) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Invalid 'capacity' or 'leakRate' format. Must be numbers.\"}");
            System.out.println("POST Request DENIED: Invalid number format for parameters.");
            return;
        }

        if (capacity <= 0 || leakRate <= 0) {
            res.setStatus(SC_BAD_REQUEST);
            writer.println("{\"status\": \"error\", \"message\": \"Invalid 'capacity' or 'leakRate' value. Must be valid positive numbers.\"}");
            System.out.println("POST Request DENIED: Invalid parameter values.");
            return;
        }

        this.apiBucket = new LeakyBucket(capacity, leakRate);
        res.setStatus(SC_OK);
        writer.println("{\"status\": \"success\", \"message\": \"Leaky Bucket configured successfully.\", \"capacity\": " + capacity + ", \"leakRate\": " + leakRate + "}");
        System.out.println("POST Request GRANTED: Leaky Bucket configured with Capacity=" + capacity + ", LeakRate=" + leakRate + " requests/sec.");
    }
}
