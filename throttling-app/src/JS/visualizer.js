window.addEventListener('DOMContentLoaded', () => {
    // Get algorithm from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const algorithm = urlParams.get('algorithm') || 'token-bucket';

    // DOM elements
    const capacityInput = document.getElementById('capacity');
    const rateInput = document.getElementById('rate');
    const configureBtn = document.getElementById('configureBtn');
    const sendRequestBtn = document.getElementById('sendRequestBtn');
    const algorithmFill = document.getElementById('algorithmFill');
    const currentDisplay = document.getElementById('currentDisplay');
    const capacityDisplay = document.getElementById('capacityDisplay');
    const lastUpdateDisplay = document.getElementById('lastUpdateDisplay');
    const logArea = document.getElementById('logArea');

    // Algorithm configuration
    const algorithmConfig = {
        'token-bucket': {
            title: 'Token Bucket Configuration',
            description: 'Configure your token bucket parameters. The visualizer will simulate token consumption and refill.',
            capacityLabel: 'Bucket Capacity (Tokens)',
            rateLabel: 'Refill Rate (Tokens/Second)',
            statusTitle: 'Token Bucket Status & Log',
            currentLabel: 'Current Tokens',
            lastUpdateLabel: 'Last Refill',
            endpoint: 'http://localhost:8888/token-bucket',
            rateParam: 'refillRate',
            currentField: 'currentTokens',
            fillClass: 'token-bucket',
            statusClass: 'token-bucket',
            buttonText: 'Configure Token Bucket',
            requestText: 'Send Single Request',
            startsEmpty: false
        },
        'leaky-bucket': {
            title: 'Leaky Bucket Configuration',
            description: 'Configure your leaky bucket parameters. The visualizer will simulate request queueing and leaking.',
            capacityLabel: 'Bucket Capacity (Requests)',
            rateLabel: 'Leak Rate (Requests/Second)',
            statusTitle: 'Leaky Bucket Status & Log',
            currentLabel: 'Current Queue Size',
            lastUpdateLabel: 'Last Leak',
            endpoint: 'http://localhost:8888/leaky-bucket',
            rateParam: 'leakRate',
            currentField: 'currentSize',
            fillClass: 'leaky-bucket',
            statusClass: 'leaky-bucket',
            buttonText: 'Configure Leaky Bucket',
            requestText: 'Add Request to Queue',
            startsEmpty: true
        }
    };

    // Current algorithm config
    const config = algorithmConfig[algorithm];

    // State variables
    let capacity = 0;
    let ratePerSecond = 0;
    let currentValue = 0;
    let lastUpdateTime = Date.now();
    let updateIntervalId = null;

    // Initialize UI based on algorithm
    function initializeUI() {
        document.title = `${config.title} - Throttling Visualizer`;
        document.getElementById('algorithmTitle').textContent = config.title;
        document.getElementById('algorithmDescription').textContent = config.description;
        document.getElementById('capacityLabel').textContent = config.capacityLabel;
        document.getElementById('rateLabel').textContent = config.rateLabel;
        document.getElementById('statusTitle').textContent = config.statusTitle;
        document.getElementById('currentStatus').innerHTML = `${config.currentLabel}: <span id="currentDisplay">0</span> / <span id="capacityDisplay">0</span>`;
        document.getElementById('lastUpdateStatus').innerHTML = `${config.lastUpdateLabel}: <span id="lastUpdateDisplay">N/A</span>`;
        
        configureBtn.textContent = config.buttonText;
        sendRequestBtn.textContent = config.requestText;
        
        // Apply algorithm-specific styling
        algorithmFill.className = `algorithm-fill ${config.fillClass}`;
        document.querySelector('.status-info').className = `status-info ${config.statusClass}`;
        
        log(`${config.title} interface initialized.`, 'info');
    }

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
     * Updates the visual representation of the algorithm.
     */
    function updateVisualization() {
        const fillPercentage = capacity > 0 ? (currentValue / capacity) * 100 : 0;
        algorithmFill.style.width = `${fillPercentage}%`;
        
        document.getElementById('currentDisplay').textContent = currentValue.toFixed(2);
        document.getElementById('capacityDisplay').textContent = capacity;
        document.getElementById('lastUpdateDisplay').textContent = new Date(lastUpdateTime).toLocaleTimeString();
        
        algorithmFill.classList.add('updating');
        setTimeout(() => algorithmFill.classList.remove('updating'), 500);
    }

    /**
     * Simulates the algorithm behavior on the client side.
     * For Token Bucket: refills tokens
     * For Leaky Bucket: leaks requests
     */
    function simulateAlgorithm() {
        const now = Date.now();
        const elapsedTimeSeconds = (now - lastUpdateTime) / 1000;
        
        if (algorithm === 'token-bucket') {
            const tokensToAdd = elapsedTimeSeconds * ratePerSecond;
            currentValue = Math.min(capacity, currentValue + tokensToAdd);
        } else {
            const requestsToLeak = elapsedTimeSeconds * ratePerSecond;
            currentValue = Math.max(0, currentValue - requestsToLeak);
        }
        
        lastUpdateTime = now;
        updateVisualization();
    }

    /**
     * Configures the algorithm parameters on the server via a POST request.
     */
    configureBtn.addEventListener('click', async () => {
        const newCapacity = parseInt(capacityInput.value);
        const newRate = parseFloat(rateInput.value);

        if (isNaN(newCapacity) || newCapacity <= 0 || isNaN(newRate) || newRate <= 0) {
            log('Please enter valid positive numbers for Capacity and Rate.', 'failure');
            sendRequestBtn.disabled = true;
            return;
        }

        configureBtn.disabled = true;
        sendRequestBtn.disabled = true;
        log(`Attempting to configure ${algorithm} on server...`, 'info');

        try {
            const response = await fetch(config.endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `capacity=${newCapacity}&${config.rateParam}=${newRate}`
            });
            const data = await response.json();

            if (response.ok) {
                capacity = newCapacity;
                ratePerSecond = newRate;
                currentValue = config.startsEmpty ? 0 : capacity;
                lastUpdateTime = Date.now();

                log(`Server Response: SUCCESS - ${data.message}`, 'success');
                log(`${config.title} configured on server: Capacity=${data.capacity}, Rate=${data[config.rateParam]} per second.`, 'info');

                if (updateIntervalId) {
                    clearInterval(updateIntervalId);
                }
                updateIntervalId = setInterval(simulateAlgorithm, 100);
                updateVisualization();
                sendRequestBtn.disabled = false;
            } else {
                log(`Server Response: FAILED (Status: ${response.status}) - ${data.message}`, 'failure');
            }
        } catch (error) {
            log(`Network Error during configuration: ${error.message}. Check API endpoint or server status.`, 'failure');
        } finally {
            configureBtn.disabled = false;
        }
    });

    /**
     * Sends a single GET request to the API endpoint and updates client-side state.
     */
    sendRequestBtn.addEventListener('click', async () => {
        sendRequestBtn.disabled = true;
        configureBtn.disabled = true;

        simulateAlgorithm();
        
        const actionText = algorithm === 'token-bucket' ? 'consume a token' : 'add a request to the queue';
        log(`Attempting to ${actionText} via ${config.endpoint}...`, 'info');

        try {
            const response = await fetch(config.endpoint);
            const data = await response.json();

            if (data[config.currentField] !== undefined) {
                currentValue = data[config.currentField];
            } else {
                log(`Warning: Server response did not contain ${config.currentField}.`, 'info');
            }

            if (response.ok) {
                log(`Server Response: SUCCESS (Status: ${response.status}) - ${data.message}`, 'success');
            } else {
                log(`Server Response: FAILED (Status: ${response.status}) - ${data.message}`, 'failure');
            }
        } catch (error) {
            log(`Network Error: ${error.message}. Check API endpoint or server status.`, 'failure');
        } finally {
            updateVisualization();
            sendRequestBtn.disabled = false;
            configureBtn.disabled = false;
        }
    });

    initializeUI();
}); 