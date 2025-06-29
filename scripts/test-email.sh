#!/bin/bash

# Email Testing Script for User Registration System
# This script tests the email functionality using the Email Controller

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:9090"
API_BASE="$BASE_URL/api/v1"

# Test email configuration
TEST_EMAIL="test@example.com"
TEST_USERNAME="testuser"

echo -e "${BLUE}🧪 Email Testing Script for User Registration System${NC}"
echo "=================================================="
echo

# Function to make HTTP requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$API_BASE$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data"
    else
        curl -s -X "$method" "$API_BASE$endpoint"
    fi
}

# Function to make POST request with query parameters
make_post_request() {
    local endpoint=$1
    local params=$2
    
    curl -s -X POST "$API_BASE$endpoint?$params"
}

# Function to check if application is running
check_app_running() {
    echo -e "${YELLOW}🔍 Checking if application is running...${NC}"
    
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Application is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Application is not running on $BASE_URL${NC}"
        echo "Please start the application first:"
        echo "  make dev"
        return 1
    fi
}

# Function to test email service status
test_email_status() {
    echo -e "\n${YELLOW}📧 Testing Email Service Status...${NC}"
    
    response=$(make_request "GET" "/email/status")
    echo "Response: $response"
    
    if echo "$response" | grep -q '"enabled":true'; then
        echo -e "${GREEN}✅ Email service is enabled${NC}"
    else
        echo -e "${RED}❌ Email service is disabled${NC}"
    fi
}

# Function to test email service connectivity
test_email_connectivity() {
    echo -e "\n${YELLOW}🔗 Testing Email Service Connectivity...${NC}"
    
    response=$(make_request "POST" "/email/test")
    echo "Response: $response"
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Email service connectivity test passed${NC}"
    else
        echo -e "${RED}❌ Email service connectivity test failed${NC}"
        echo "Make sure MailHog is running: docker-compose -f docker-compose.dev.yml up -d"
    fi
}

# Function to test welcome email sending
test_welcome_email() {
    echo -e "\n${YELLOW}📨 Testing Welcome Email Sending...${NC}"
    
    params="email=$TEST_EMAIL&username=$TEST_USERNAME"
    response=$(make_post_request "/email/send-welcome" "$params")
    echo "Response: $response"
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Welcome email sent successfully${NC}"
    else
        echo -e "${RED}❌ Welcome email sending failed${NC}"
    fi
}

# Function to test user registration with automatic email
test_user_registration() {
    echo -e "\n${YELLOW}👤 Testing User Registration with Automatic Email...${NC}"
    
    # Generate unique username and email
    timestamp=$(date +%s)
    username="testuser_$timestamp"
    email="test_$timestamp@example.com"
    
    user_data="{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"password123\"}"
    
    echo "Registering user: $username ($email)"
    response=$(make_request "POST" "/users" "$user_data")
    echo "Response: $response"
    
    if echo "$response" | grep -q '"id"'; then
        echo -e "${GREEN}✅ User registration successful${NC}"
        echo -e "${BLUE}📧 Check MailHog UI at http://localhost:8025 to see the welcome email${NC}"
    else
        echo -e "${RED}❌ User registration failed${NC}"
    fi
}

# Function to show MailHog instructions
show_mailhog_info() {
    echo -e "\n${BLUE}📬 MailHog Information:${NC}"
    echo "MailHog is a development SMTP server that captures emails for testing."
    echo
    echo "Access MailHog Web UI: http://localhost:8025"
    echo "SMTP Server: localhost:1025"
    echo
    echo "To start MailHog:"
    echo "  docker-compose -f docker-compose.dev.yml up -d"
    echo
    echo "To stop MailHog:"
    echo "  docker-compose -f docker-compose.dev.yml down"
}

# Main execution
main() {
    echo "Starting email functionality tests..."
    echo
    
    # Check if application is running
    if ! check_app_running; then
        exit 1
    fi
    
    # Run tests
    test_email_status
    test_email_connectivity
    test_welcome_email
    test_user_registration
    
    echo -e "\n${GREEN}🎉 Email testing completed!${NC}"
    show_mailhog_info
}

# Run main function
main "$@" 