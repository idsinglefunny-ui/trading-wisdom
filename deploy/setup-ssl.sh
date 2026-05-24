#!/bin/bash
# SSL Certificate Setup Script for trading.tangping.me
# Usage: sudo ./setup-ssl.sh

set -e

DOMAIN="trading.tangping.me"
EMAIL="admin@tangping.me"  # Change to your email

echo "========================================="
echo "  SSL Certificate Setup"
echo "  Domain: $DOMAIN"
echo "========================================="
echo ""

# Check if acme.sh is installed
if ! command -v acme.sh &> /dev/null; then
    echo "Installing acme.sh..."
    curl https://get.acme.sh | sh -s email=$EMAIL
    source ~/.bashrc
else
    echo "acme.sh already installed"
fi

# Request certificate
echo ""
echo "Requesting SSL certificate for $DOMAIN..."
acme.sh --issue -d $DOMAIN --nginx /etc/nginx

# Install certificate
echo ""
echo "Installing certificate..."
acme.sh --install-cert -d $DOMAIN \
    --ecc \
    --key-file /etc/nginx/ssl/$DOMAIN.key \
    --fullchain-file /etc/nginx/ssl/$DOMAIN.cer \
    --reloadcmd "systemctl reload nginx"

echo ""
echo "========================================="
echo "  SSL Certificate Installed Successfully!"
echo "========================================="
echo ""
echo "Certificate files:"
echo "  - /etc/nginx/ssl/$DOMAIN.key"
echo "  - /etc/nginx/ssl/$DOMAIN.cer"
echo ""
echo "Auto-renewal is configured by acme.sh."
