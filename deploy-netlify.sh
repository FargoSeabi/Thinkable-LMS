#!/bin/bash

echo "Building React frontend for Netlify deployment..."

cd frontend
echo "Installing dependencies..."
npm install

echo "Building production version..."
npm run build

echo "Creating _redirects file for client-side routing..."
echo "/*    /index.html   200" > build/_redirects

echo ""
echo "✅ Build complete!"
echo ""
echo "📁 Build files are in: frontend/build/"
echo ""
echo "🚀 Next steps for Netlify deployment:"
echo "1. Go to https://netlify.com and sign up/login"
echo "2. Click 'Add new site' → 'Deploy manually'"
echo "3. Drag and drop the frontend/build/ folder"
echo "4. Or connect to GitHub and auto-deploy from repository"
echo ""
echo "🔧 For GitHub auto-deployment:"
echo "1. Push this code to GitHub"
echo "2. Connect Netlify to your GitHub repository"
echo "3. Set build command: npm run build"
echo "4. Set publish directory: frontend/build"
echo "5. Set base directory: frontend"
echo ""