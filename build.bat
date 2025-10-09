@echo off
echo 🚀 Building ThinkAble for Production...

REM Build Frontend
echo 📦 Building React Frontend...
cd frontend
call npm ci --only=production
call npm run build

if %errorlevel% neq 0 (
    echo ❌ Frontend build failed!
    exit /b 1
)

echo ✅ Frontend build successful!
cd ..

REM Build Backend
echo 📦 Building Spring Boot Backend...
cd backend
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo ❌ Backend build failed!
    exit /b 1
)

echo ✅ Backend build successful!
cd ..

echo 🎉 All builds completed successfully!
echo 📋 Next steps:
echo    1. Push your code to GitHub
echo    2. Connect your GitHub repo to Render
echo    3. Deploy using the render.yaml configuration