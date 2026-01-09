#!/bin/bash

#####################################################################
# build-all.sh - Build all microservices Docker images
# Usage: bash docker/build-all.sh
#####################################################################

set -e  # Exit on error

echo "======================================================"
echo "Building all Docker images"
echo "======================================================"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

#####################################################################
# Build Membership Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Building Membership Service (Port 8081)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

cd ms-membership

if [ ! -f "pom.xml" ]; then
    echo " Error: pom.xml not found in ms-membership"
    exit 1
fi

echo " Compiling Membership Service..."
mvn clean package -DskipTests -q

echo "Building Docker image: ecommerce-membership:1.0"
docker build -t ecommerce-membership:1.0 .

echo -e "${GREEN} Membership Service built successfully${NC}"

cd "$PROJECT_ROOT"

#####################################################################
# Build Product Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Building Product Service (Port 8082)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

cd service-product

if [ ! -f "pom.xml" ]; then
    echo " Error: pom.xml not found in service-product"
    exit 1
fi

echo " Compiling Product Service..."
mvn clean package -DskipTests -q

echo " Building Docker image: ecommerce-product:1.0"
docker build -t ecommerce-product:1.0 .

echo -e "${GREEN} Product Service built successfully${NC}"

cd "$PROJECT_ROOT"

#####################################################################
# Build Order Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Building Order Service (Port 8083)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

cd service-order

if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in service-order"
    exit 1
fi

echo " Compiling Order Service..."
mvn clean package -DskipTests -q

echo " Building Docker image: ecommerce-order:1.0"
docker build -t ecommerce-order:1.0 .

echo -e "${GREEN} Order Service built successfully${NC}"

cd "$PROJECT_ROOT"

#####################################################################
# Summary
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Build Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

docker images | grep ecommerce

echo -e "${GREEN}"
echo "======================================================"
echo " All services built successfully!"
echo "======================================================"
echo -e "${NC}"

echo ""
echo "Next steps:"
echo "  1. Run: bash docker/publish-all.sh (to push to Docker Hub)"
echo "  2. Or run: docker-compose up -d"
echo ""
