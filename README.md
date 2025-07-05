# Token Bucket Throttling Visualizer

A interactive web application that demonstrates **throttling** concepts through a visual implementation of the **Token Bucket Algorithm**. This educational tool allows users to configure bucket parameters and observe real-time token consumption and refill behavior.

## 🎯 Purpose

This application serves as a visual learning tool to understand rate limiting and throttling mechanisms. While currently focused on the Token Bucket algorithm, it's designed to be expanded with other throttling algorithms in the future.

## 🧠 What is Throttling?

**Throttling** is a technique used to control the rate at which requests are processed by a system. It prevents resource exhaustion and ensures fair usage by limiting the number of operations that can be performed within a specific time window.

### Token Bucket Algorithm

The **Token Bucket Algorithm** is a popular rate limiting strategy that works as follows:

- **Bucket**: A container with a fixed capacity that holds tokens
- **Tokens**: Represent permission to perform an operation
- **Refill Rate**: Tokens are added to the bucket at a constant rate
- **Consumption**: Each request consumes one token; requests are denied when the bucket is empty

This algorithm allows for burst traffic (up to bucket capacity) while maintaining an average rate over time.

## 🚀 Features

### Backend (Java Servlet API)
- **Token Bucket Implementation**: Hand-crafted algorithm with configurable capacity and refill rate
- **RESTful API**: Simple endpoints for configuration and token consumption
- **Thread-Safe**: Synchronized operations for concurrent access
- **JSON Responses**: Structured API responses with status and token information

### Frontend (Interactive Web UI)
- **Real-Time Visualization**: Dynamic token bucket fill level representation
- **Configuration Panel**: Live parameter adjustment (capacity, refill rate)
- **Request Simulation**: Single-click token consumption testing
- **Activity Logging**: Comprehensive request/response logging with timestamps
- **Status Monitoring**: Current token count and last refill time display

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

### Configure Token Bucket
```http
POST /token-bucket
Content-Type: application/x-www-form-urlencoded

capacity=10&refillRate=2
```

**Parameters:**
- `capacity`: Maximum number of tokens (positive integer)
- `refillRate`: Tokens added per second (positive integer)

### Consume Token
```http
GET /token-bucket
```

**Responses:**
- `200 OK`: Token consumed successfully
- `400 Bad Request`: Bucket not configured
- `429 Too Many Requests`: No tokens available (throttled)

## 📁 Project Structure

```
throttling/
├── throttling-api/          # Java backend
│   ├── src/main/java/
│   │   └── es/mlrdevs97/
│   │       ├── servlets/
│   │       │   └── TokenBucketServlet.java
│   │       └── throttling/
│   │           └── TokenBucket.java
│   └── Dockerfile
├── throttling-app/          # Frontend web application
│   ├── throttling.html      # Main UI
│   ├── throttling.css       # Styling
│   ├── throttling.js        # Interactive functionality
│   └── Dockerfile
└── docker-compose.yml       # Container orchestration
```

## 🛠 Technical Implementation

### Backend Architecture
- **Servlet-based**: Lightweight Java EE servlet container
- **Thread-Safe Token Bucket**: Synchronized methods prevent race conditions
- **Time-Based Refilling**: Calculates token addition based on elapsed time
- **Error Handling**: Comprehensive validation and error responses

### Frontend Architecture
- **Vanilla JavaScript**: No framework dependencies for simplicity
- **Real-Time Updates**: Periodic token refill simulation
- **Responsive Design**: Modern CSS with Inter font family
- **Visual Feedback**: Color-coded logging and animated token fill

### Key Technical Features
- **Synchronization**: Server-side token count synchronization after each request
- **Visual Representation**: Percentage-based token bucket fill animation
- **Client-Side Simulation**: Local token refill calculation between server requests
- **Error Handling**: Network error detection and user feedback
- **Logging**: Timestamped activity log with request/response details

## 🎮 How to Use

1. **Configure the Bucket**: Set capacity and refill rate, then click "Configure Bucket"
2. **Monitor Status**: Watch the visual token bucket and current token count
3. **Send Requests**: Click "Send Single Request" to consume tokens
4. **Observe Behavior**: See how tokens are consumed and refilled over time
5. **Experiment**: Try different configurations to understand rate limiting behavior

## 🔮 Future Enhancements

This application is designed to be extensible with additional throttling algorithms:
- **Leaky Bucket Algorithm**
- **Fixed Window Counter**
- **Sliding Window Log**
- **Sliding Window Counter**

---