#!/bin/bash

echo "🚀 Building ThinkAble for Production..."

# Build Frontend
echo "📦 Building React Frontend..."
cd frontend
npm ci --only=production
npm run build

if [ $? -eq 0 ]; then
    echo "✅ Frontend build successful!"
else
    echo "❌ Frontend build failed!"
    exit 1
fi

cd ..

# Build Backend
echo "📦 Building Spring Boot Backend..."
cd backend
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Backend build successful!"
else
    echo "❌ Backend build failed!"
    exit 1
fi

cd ..

echo "🎉 All builds completed successfully!"
echo "📋 Next steps:"
echo "   1. Push your code to GitHub"
echo "   2. Connect your GitHub repo to Render"
echo "   3. Deploy using the render.yaml configuration"