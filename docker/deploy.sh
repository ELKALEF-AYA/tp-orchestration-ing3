#!/bin/bash

#####################################################################
# deploy.sh - Pull images from Docker Hub and deploy with docker-compose
# Usage: bash docker/deploy.sh
#
# Make sure to set your Docker Hub username and have docker-compose
# in the parent directory!
#####################################################################

set -e  # Exit on error

echo "======================================================"
echo " Deploying from Docker Hub"
echo "======================================================"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

#####################################################################
# Configuration
#####################################################################
DOCKER_USERNAME="${DOCKER_USERNAME:-your-username}"
VERSION="1.0"

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Check if docker-compose.yml exists
if [ ! -f "$PROJECT_ROOT/docker-compose.yml" ]; then
    echo -e "${RED} Error: docker-compose.yml not found in $PROJECT_ROOT${NC}"
    echo "Make sure you run this script from the project root directory"
    exit 1
fi

# Check if username is default
if [ "$DOCKER_USERNAME" = "your-username" ]; then
    echo -e "${YELLOW}"
    echo "  WARNING: Using default username 'your-username'"
    echo -e "${NC}"
    read -p "Enter your Docker Hub username (or press Enter to continue with default): " input_username
    if [ ! -z "$input_username" ]; then
        DOCKER_USERNAME="$input_username"
    fi
fi

echo "Configuration:"
echo "   Docker Hub Username: $DOCKER_USERNAME"
echo "   Version: $VERSION"
echo "   docker-compose.yml: $PROJECT_ROOT/docker-compose.yml"
echo ""

#####################################################################
# Verify Docker is running
#####################################################################
echo -e "${BLUE} Checking Docker...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED} Error: Docker is not running${NC}"
    exit 1
fi
echo -e "${GREEN} Docker is running${NC}"
echo ""

#####################################################################
# Pull images
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Pulling images from Docker Hub"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

IMAGES=(
    "ecommerce-membership"
    "ecommerce-product"
    "ecommerce-order"
)

for image in "${IMAGES[@]}"; do
    FULL_IMAGE="$DOCKER_USERNAME/$image:$VERSION"
    echo " Pulling: $FULL_IMAGE"
    if docker pull $FULL_IMAGE; then
        echo -e "${GREEN} $image pulled${NC}"
    else
        echo -e "${RED} Failed to pull $image${NC}"
        echo "   Make sure the repository exists and is accessible"
        exit 1
    fi
    echo ""
done

#####################################################################
# Update docker-compose.yml images
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Configuring docker-compose.yml"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

# Create a temporary docker-compose with updated images
TEMP_COMPOSE=$(mktemp)
cp "$PROJECT_ROOT/docker-compose.yml" "$TEMP_COMPOSE"

# Update images in the docker-compose file (if using build context, replace with image)
# This assumes the docker-compose uses 'build:' for the services
sed -i.bak \
  -e "s|build:.*membership|image: $DOCKER_USERNAME/ecommerce-membership:$VERSION|g" \
  -e "s|build:.*product|image: $DOCKER_USERNAME/ecommerce-product:$VERSION|g" \
  -e "s|build:.*order|image: $DOCKER_USERNAME/ecommerce-order:$VERSION|g" \
  "$TEMP_COMPOSE" 2>/dev/null || true

echo " docker-compose configuration prepared"
echo ""

#####################################################################
# Stop and remove existing containers
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Cleaning up existing containers"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

cd "$PROJECT_ROOT"
docker-compose down 2>/dev/null || true

echo -e "${GREEN} Cleaned up${NC}"
echo ""

#####################################################################
# Start services with docker-compose
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Starting services with docker-compose"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

# Use original docker-compose (it uses build: context)
# Or update to use pulled images
docker-compose up -d

echo ""
echo -e "${GREEN}"
echo "======================================================"
echo " Deployment successful!"
echo "======================================================"
echo -e "${NC}"

# Wait for services to start
echo ""
echo " Waiting 10 seconds for services to start..."
sleep 10

#####################################################################
# Health checks
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Health Checks"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

SERVICES=(
    "Membership:8081"
    "Product:8082"
    "Order:8083"
)

for service in "${SERVICES[@]}"; do
    IFS=':' read -r name port <<< "$service"
    echo -n " $name Service (port $port)... "
    
    if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN} UP${NC}"
    else
        echo -e "${YELLOW} Starting...${NC}"
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Services Available At:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo " Frontend:     http://localhost:5173"
echo " Membership:   http://localhost:8081"
echo " Product:      http://localhost:8082"
echo " Order:        http://localhost:8083"
echo "Prometheus:   http://localhost:9090"
echo "Grafana:      http://localhost:3000 (admin/admin)"
echo ""
echo ""
echo "Useful commands:"
echo "   docker-compose logs -f              (view all logs)"
echo "   docker-compose logs -f membership-service"
echo "   docker-compose down                 (stop all services)"
echo ""
