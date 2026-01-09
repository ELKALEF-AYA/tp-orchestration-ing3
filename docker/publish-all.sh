#!/bin/bash

#####################################################################
# publish-all.sh - Tag and push images to Docker Hub
# Usage: bash docker/publish-all.sh
#
# Make sure to set your Docker Hub username below!
#####################################################################

set -e  # Exit on error

echo "======================================================"
echo " Publishing images to Docker Hub"
echo "======================================================"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

#####################################################################
# Configuration - EDIT THIS WITH YOUR DOCKER HUB USERNAME
#####################################################################
DOCKER_USERNAME="${DOCKER_USERNAME:-your-username}"

# Check if username is default
if [ "$DOCKER_USERNAME" = "your-username" ]; then
    echo -e "${YELLOW}"
    echo "⚠  WARNING: Using default username 'your-username'"
    echo -e "${NC}"
    read -p "Enter your Docker Hub username (or press Enter to continue with default): " input_username
    if [ ! -z "$input_username" ]; then
        DOCKER_USERNAME="$input_username"
    fi
fi

VERSION="1.0"

echo " Configuration:"
echo "   Docker Hub Username: $DOCKER_USERNAME"
echo "   Version: $VERSION"
echo ""

#####################################################################
# Verify Docker login
#####################################################################
echo -e "${BLUE} Checking Docker login...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo " Error: Docker is not running or you're not logged in"
    echo "Run: docker login"
    exit 1
fi

echo -e "${GREEN} Docker login verified${NC}"
echo ""

#####################################################################
# Tag and Push Membership Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Publishing Membership Service"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

SOURCE_IMAGE="ecommerce-membership:1.0"
TARGET_IMAGE="$DOCKER_USERNAME/ecommerce-membership:$VERSION"

echo " Tagging: $SOURCE_IMAGE → $TARGET_IMAGE"
docker tag $SOURCE_IMAGE $TARGET_IMAGE

echo " Pushing: $TARGET_IMAGE"
docker push $TARGET_IMAGE

echo -e "${GREEN} Membership Service published${NC}"
echo ""

#####################################################################
# Tag and Push Product Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Publishing Product Service"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

SOURCE_IMAGE="ecommerce-product:1.0"
TARGET_IMAGE="$DOCKER_USERNAME/ecommerce-product:$VERSION"

echo " Tagging: $SOURCE_IMAGE → $TARGET_IMAGE"
docker tag $SOURCE_IMAGE $TARGET_IMAGE

echo " Pushing: $TARGET_IMAGE"
docker push $TARGET_IMAGE

echo -e "${GREEN} Product Service published${NC}"
echo ""

#####################################################################
# Tag and Push Order Service
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Publishing Order Service"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

SOURCE_IMAGE="ecommerce-order:1.0"
TARGET_IMAGE="$DOCKER_USERNAME/ecommerce-order:$VERSION"

echo " Tagging: $SOURCE_IMAGE → $TARGET_IMAGE"
docker tag $SOURCE_IMAGE $TARGET_IMAGE

echo " Pushing: $TARGET_IMAGE"
docker push $TARGET_IMAGE

echo -e "${GREEN} Order Service published${NC}"
echo ""

#####################################################################
# Summary
#####################################################################
echo -e "${BLUE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Published Images"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${NC}"

echo "🔗 Docker Hub URLs:"
echo "   • https://hub.docker.com/r/$DOCKER_USERNAME/ecommerce-membership"
echo "   • https://hub.docker.com/r/$DOCKER_USERNAME/ecommerce-product"
echo "   • https://hub.docker.com/r/$DOCKER_USERNAME/ecommerce-order"
echo ""

echo -e "${GREEN}"
echo "======================================================"
echo " All images published successfully!"
echo "======================================================"
echo -e "${NC}"

echo ""
echo "Next steps:"
echo "  1. Visit https://hub.docker.com/settings/security"
echo "  2. Share repositories with: rkarra.okad@gmail.com"
echo "  3. Run: bash docker/deploy.sh (to pull and deploy)"
echo ""
