# Throttling Algorithm Visualizer

An interactive web application that demonstrates **throttling** concepts through visual implementations of multiple rate limiting algorithms. This educational tool allows users to compare different throttling strategies and observe their real-time behavior.

## 🎯 Purpose

This application serves as a comprehensive visual learning tool to understand rate limiting and throttling mechanisms. It currently supports **Token Bucket** and **Leaky Bucket** algorithms, with an extensible architecture for adding more algorithms in the future.

## 🧠 What is Throttling?

**Throttling** is a technique used to control the rate at which requests are processed by a system. It prevents resource exhaustion and ensures fair usage by limiting the number of operations that can be performed within a specific time window.

### Supported Algorithms

#### Token Bucket Algorithm
- **Bucket**: A container with a fixed capacity that holds tokens
- **Tokens**: Represent permission to perform an operation
- **Refill Rate**: Tokens are added to the bucket at a constant rate
- **Consumption**: Each request consumes one token; requests are denied when the bucket is empty
- **Behavior**: Allows burst traffic (up to bucket capacity) while maintaining an average rate over time

#### Leaky Bucket Algorithm
- **Queue**: A container with a fixed capacity that holds requests
- **Requests**: Incoming requests are added to the queue
- **Leak Rate**: Requests are processed (leaked) from the queue at a constant rate
- **Overflow**: When the queue is full, new requests are dropped
- **Behavior**: Smooths out traffic by processing requests at a steady rate

## 🚀 Features

### Backend (Java Servlet API)
- **Multiple Algorithm Support**: Token Bucket and Leaky Bucket implementations
- **Thread-Safe**: Synchronized operations for concurrent access
- **RESTful API**: Simple endpoints for configuration and request processing
- **JSON Responses**: Structured API responses with status and algorithm-specific information

### Frontend (Interactive Web UI)
- **Algorithm Selection**: Choose between Token Bucket and Leaky Bucket
- **Real-Time Visualization**: Dynamic visual representation of algorithm state
- **Configuration Panel**: Live parameter adjustment for each algorithm
- **Request Simulation**: Interactive testing of algorithm behavior
- **Activity Logging**: Comprehensive request/response logging with timestamps
- **Responsive Design**: Modern UI that works on all devices

## 🏃 How to Run

### Prerequisites
- Docker
- Docker Compose

### Quick Start
```bash
# Clone the repository and navigate to the project directory
cd throttling

# Start the application
docker-compose up -d

# Access the application
# Frontend: http://localhost:88
# Backend API: http://localhost:8888
```

The application will start two services:
- **Server**: Java servlet API running on port 8888
- **Client**: Web application running on port 88

## 🔧 API Endpoints

### Token Bucket API

#### Configure Token Bucket
```http
POST /token-bucket
Content-Type: application/x-www-form-urlencoded

capacity=10&refillRate=2
```

#### Consume Token
```http
GET /token-bucket
```

### Leaky Bucket API

#### Configure Leaky Bucket
```http
POST /leaky-bucket
Content-Type: application/x-www-form-urlencoded

capacity=10&leakRate=2
```

#### Add Request to Queue
```http
GET /leaky-bucket
```

### Response Codes
- `200 OK`: Request processed successfully
- `400 Bad Request`: Algorithm not configured or invalid parameters
- `429 Too Many Requests`: Request throttled (no tokens available or queue full)

## 📁 Project Structure

```
throttling/
├── throttling-api/          # Java backend
│   ├── src/main/java/
│   │   └── es/mlrdevs97/
│   │       ├── servlets/
│   │       │   ├── TokenBucketServlet.java
│   │       │   └── LeakyBucketServlet.java
│   │       └── throttling/
│   │           ├── TokenBucket.java
│   │           └── LeakyBucket.java
│   └── Dockerfile
├── throttling-app/          # Frontend web application
│   ├── index.html           # Algorithm selection page
│   ├── index.css            # Styling for selection page
│   ├── index.js             # Selection page functionality
│   ├── visualizer.html      # Generic algorithm visualizer
│   ├── visualizer.css       # Visualizer styling
│   ├── visualizer.js        # Generic visualizer functionality
│   └── Dockerfile
└── docker-compose.yml       # Container orchestration
```

## 🛠 Technical Implementation

### Backend Architecture
- **Servlet-based**: Lightweight Java EE servlet container
- **Algorithm Implementations**: Separate classes for each throttling algorithm
- **Thread-Safe Operations**: Synchronized methods prevent race conditions
- **Time-Based Calculations**: Accurate token refill and request leak calculations
- **Error Handling**: Comprehensive validation and error responses

### Frontend Architecture
- **Algorithm Selection**: Dynamic UI that adapts to the selected algorithm
- **Generic Visualizer**: Single page that works with multiple algorithms@throttling.js
- **Vanilla JavaScript**: No framework dependencies for simplicity
- **Real-Time Updates**: Client-side simulation synchronized with server state
- **Responsive Design**: Modern CSS with Inter font family
- **Visual Feedback**: Color-coded logging and animated visualizations

### Key Technical Features
- **Multi-Algorithm Support**: Extensible architecture for adding new algorithms
- **Server Synchronization**: Client state synchronized with server after each request
- **Visual Differentiation**: Different colors and behaviors for each algorithm
- **Parameter Validation**: Client and server-side validation of algorithm parameters
- **Error Handling**: Comprehensive network error detection and user feedback

## 🎮 How to Use

1. **Select Algorithm**: Choose between Token Bucket or Leaky Bucket from the main page
2. **Configure Parameters**: Set capacity and rate values for your selected algorithm
3. **Monitor Visualization**: Watch the dynamic representation of algorithm state
4. **Send Requests**: Test the algorithm behavior by sending requests
5. **Observe Differences**: Switch between algorithms to compare their behaviors
6. **Experiment**: Try different configurations to understand rate limiting concepts

### Usage Tips
- **Token Bucket**: Start with high capacity for burst testing, then reduce for steady-state behavior
- **Leaky Bucket**: Observe how requests queue up and are processed at a constant rate
- **Comparison**: Use similar parameters for both algorithms to see their different behaviors

## 🔮 Future Enhancements

This application is designed to be extensible with additional throttling algorithms:
- **Fixed Window Counter**
- **Sliding Window Log**
- **Sliding Window Counter**
- **Adaptive Rate Limiting**

---