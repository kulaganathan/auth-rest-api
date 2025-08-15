#!/bin/bash

# Development startup script for Auth Server

echo "🚀 Starting Auth Server in development mode..."

# Check if Java 17+ is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6+."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 &> /dev/null; then
    echo "⚠️  PostgreSQL is not running. Starting with Docker Compose..."
    
    if command -v docker-compose &> /dev/null || command -v docker &> /dev/null; then
        echo "🐳 Starting PostgreSQL with Docker Compose..."
        docker-compose up -d postgres
        
        # Wait for PostgreSQL to be ready
        echo "⏳ Waiting for PostgreSQL to be ready..."
        sleep 10
        
        if pg_isready -h localhost -p 5432 &> /dev/null; then
            echo "✅ PostgreSQL is now ready"
        else
            echo "❌ Failed to start PostgreSQL. Please start it manually."
            exit 1
        fi
    else
        echo "❌ Docker is not available. Please start PostgreSQL manually."
        exit 1
    fi
else
    echo "✅ PostgreSQL is running"
fi

# Check if RSA keys exist
if [ ! -f "src/main/resources/keys/private.pem" ] || [ ! -f "src/main/resources/keys/public.pem" ]; then
    echo "🔑 RSA keys not found. Generating new keys..."
    chmod +x scripts/generate-keys.sh
    ./scripts/generate-keys.sh
fi

# Run Flyway migrations
echo "🔄 Running database migrations..."
mvn flyway:migrate

# Build the project
echo "🔨 Building the project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
    
    # Start the application
    echo "🚀 Starting Auth Server..."
    echo "📖 API Documentation will be available at: http://localhost:8080/swagger-ui/index.html"
    echo "🔍 Health check: http://localhost:8080/actuator/health"
    echo "⏹️  Press Ctrl+C to stop the application"
    echo ""
    
    mvn spring-boot:run
else
    echo "❌ Build failed. Please fix the errors and try again."
    exit 1
fi
