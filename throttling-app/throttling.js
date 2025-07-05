
window.addEventListener('DOMContentLoaded', () => {
    const capacityInput = document.getElementById('capacity');
    const refillRateInput = document.getElementById('refillRate');
    const configureBucketBtn = document.getElementById('configureBucketBtn');
    const sendSingleRequestBtn = document.getElementById('sendSingleRequestBtn');
    const tokenFill = document.getElementById('tokenFill');
    const currentTokensDisplay = document.getElementById('currentTokensDisplay');
    const capacityDisplay = document.getElementById('capacityDisplay');
    const lastRefillTimeDisplay = document.getElementById('lastRefillTimeDisplay');
    const logArea = document.getElementById('logArea');
    const API_ENDPOINT = "http://localhost:8888/token-bucket";

    let capacity = 0;
    let refillRatePerSecond = 0;
    let currentTokens = 0;
    let lastRefillTime = Date.now(); 
    let refillIntervalId = null;

    /**
     * Logs a message to the log area.
     * @param {string} message - The message to log.
     * @param {string} type - 'info', 'success', or 'failure' for styling.
     */
    function log(message, type = 'info') {
        const entry = document.createElement('p');
        entry.classList.add('log-entry', type);
        entry.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;

        if (logArea) {
            logArea.prepend(entry);
            if (logArea.children.length > 50) {
                logArea.removeChild(logArea.lastChild);
            }
        } else {
            console.error("logArea element not found. Cannot log message:", message);
        }
    }

    /**
     * Updates the visual representation of the token bucket.
     */
    function updateBucketVisuals() {
        const fillPercentage = capacity > 0 ? (currentTokens / capacity) * 100 : 0;
        tokenFill.style.width = `${fillPercentage}%`;
        currentTokensDisplay.textContent = currentTokens.toFixed(2);
        capacityDisplay.textContent = capacity;
        lastRefillTimeDisplay.textContent = new Date(lastRefillTime).toLocaleTimeString();
    }

    /**
     * Refills tokens based on the refill rate and elapsed time.
     * The actual token count is synchronized with the server's response after each request.
     */
    function refillTokens() {
        const now = Date.now();
        const elapsedTimeSeconds = (now - lastRefillTime) / 1000;
        const tokensToAdd = elapsedTimeSeconds * refillRatePerSecond;

        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        lastRefillTime = now;
        updateBucketVisuals();
    }

    /**
     * Configures the token bucket parameters on the server via a POST request.
     */
    configureBucketBtn.addEventListener('click', async () => {
        const newCapacity = parseInt(capacityInput.value);
        const newRefillRate = parseInt(refillRateInput.value);

        if (isNaN(newCapacity) || newCapacity <= 0 || isNaN(newRefillRate) || newRefillRate < 0) {
            log('Please enter valid positive numbers for Capacity and Refill Rate.', 'failure');
            sendSingleRequestBtn.disabled = true;
            return;
        }

        configureBucketBtn.disabled = true;
        sendSingleRequestBtn.disabled = true;
        log('Attempting to configure bucket on server...', 'info');

        try {
            const response = await fetch(API_ENDPOINT, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `capacity=${newCapacity}&refillRate=${newRefillRate}`
            });
            const data = await response.json();

            if (response.ok) {
                capacity = newCapacity;
                refillRatePerSecond = newRefillRate;
                currentTokens = capacity;
                lastRefillTime = Date.now();

                log(`Server Response: SUCCESS - ${data.message}`, 'success');
                log(`Bucket configured on server: Capacity=${data.capacity}, Refill Rate=${data.refillRate} tokens/sec.`, 'info');

                // Clear any existing refill interval and start a new one
                if (refillIntervalId) {
                    clearInterval(refillIntervalId);
                }
                refillIntervalId = setInterval(refillTokens, 100);
                updateBucketVisuals();
                sendSingleRequestBtn.disabled = false;
            } else {
                log(`Server Response: FAILED (Status: ${response.status}) - ${data.message}`, 'failure');
            }
        } catch (error) {
            log(`Network Error during configuration: ${error.message}. Check API endpoint or server status.`, 'failure');
        } finally {
            configureBucketBtn.disabled = false;
        }
    });

    /**
     * Sends a single GET request to the API endpoint and updates client-side state.
     */
    sendSingleRequestBtn.addEventListener('click', async () => {
        sendSingleRequestBtn.disabled = true;
        configureBucketBtn.disabled = true;

        refillTokens();
        log(`Attempting to send request to ${API_ENDPOINT}...`, 'info');

        try {
            const response = await fetch(API_ENDPOINT);
            const data = await response.json(); // Parse JSON response

            if (data.currentTokens !== undefined) {
                currentTokens = data.currentTokens;
            } else {
                log('Warning: Server response did not contain currentTokens.', 'info');
            }

            if (response.ok) {
                log(`Server Response: SUCCESS (Status: ${response.status}) - ${data.message}`, 'success');
            } else {
                log(`Server Response: FAILED (Status: ${response.status}) - ${data.message}`, 'failure');
            }
        } catch (error) {
            log(`Network Error: ${error.message}. Check API endpoint or server status.`, 'failure');
        } finally {
            updateBucketVisuals();
            sendSingleRequestBtn.disabled = false;
            configureBucketBtn.disabled = false;
        }
    });
});