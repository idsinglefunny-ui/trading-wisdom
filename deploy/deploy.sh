#!/bin/bash
# Deployment Script for Trading Wisdom
# Usage: sudo ./deploy.sh

set -e

PROJECT_ROOT="/home/mnyagent/tradeyourplan"
API_DEPLOY_DIR="/opt/trading-api"
WEB_DEPLOY_DIR="/opt/trading-web"
NGINX_SITE="/etc/nginx/sites-available/trading.tangping.me"
NGINX_ENABLED="/etc/nginx/sites-enabled/trading.tangping.me"

echo "========================================="
echo "  Trading Wisdom Deployment"
echo "========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (sudo ./deploy.sh)"
    exit 1
fi

# Create directories
echo "Creating deployment directories..."
mkdir -p $API_DEPLOY_DIR
mkdir -p $WEB_DEPLOY_DIR
mkdir -p /etc/nginx/ssl

# Copy API files
echo ""
echo "Deploying Go API..."
cp $PROJECT_ROOT/trading-api/trading-api $API_DEPLOY_DIR/
chmod +x $API_DEPLOY_DIR/trading-api

# Initialize database
echo ""
echo "Initializing database..."
mysql -u root -p < $PROJECT_ROOT/trading-api/scripts/init.sql

# Copy web files
echo ""
echo "Deploying Next.js web..."
cd $PROJECT_ROOT/trading-web
npm run build
mkdir -p $WEB_DEPLOY_DIR
cp -r out/* $WEB_DEPLOY_DIR/

# Configure Nginx
echo ""
echo "Configuring Nginx..."
cp $PROJECT_ROOT/deploy/nginx.conf $NGINX_SITE
ln -sf $NGINX_SITE $NGINX_ENABLED
nginx -t

# Setup systemd services
echo ""
echo "Setting up systemd services..."
cp $PROJECT_ROOT/deploy/trading-api.service /etc/systemd/system/
cp $PROJECT_ROOT/deploy/trading-web.service /etc/systemd/system/

# Reload systemd
systemctl daemon-reload

# Enable and start services
echo ""
echo "Enabling and starting services..."
systemctl enable trading-api
systemctl enable trading-web
systemctl restart trading-api
systemctl restart trading-web
systemctl reload nginx

echo ""
echo "========================================="
echo "  Deployment Complete!"
echo "========================================="
echo ""
echo "Services status:"
systemctl status trading-api --no-pager -l
echo ""
systemctl status trading-web --no-pager -l
echo ""
echo "Website: https://trading.tangping.me"
