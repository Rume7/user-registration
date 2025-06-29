#!/bin/bash

# User Registration Application Startup Script
# This script helps you start the application with Docker Compose

set -e  # Exit on any error

echo "🚀 Starting User Registration Application..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "📦 Building and starting services..."
docker-compose up --build -d

echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are healthy
echo "🔍 Checking service health..."

# Check application health
if curl -f http://localhost:9090/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is healthy"
else
    echo "⚠️  Application health check failed, but it might still be starting up"
fi

# Check database health
if docker-compose ps db | grep -q "healthy"; then
    echo "✅ Database is healthy"
else
    echo "⚠️  Database health check failed, but it might still be starting up"
fi

echo ""
echo "🎉 Application is starting up!"
echo ""
echo "📋 Useful URLs:"
echo "   🌐 Application: http://localhost:9090"
echo "   📚 API Documentation: http://localhost:9090/swagger-ui.html"
echo "   🗄️  Database: localhost:5432 (userdb)"
echo ""
echo "📝 To stop the application:"
echo "   docker-compose down"
echo ""
echo "📝 To view logs:"
echo "   docker-compose logs -f"
echo ""
echo "📝 To restart:"
echo "   docker-compose restart" 