# ThinkAble Deployment Guide for Render

This guide will help you deploy the ThinkAble application to Render.

## Prerequisites

1. A GitHub account with your code repository
2. A Render account (free tier available)
3. Your code pushed to a GitHub repository

## Deployment Steps

### 1. Prepare Your Repository

Ensure your code is pushed to GitHub with all the deployment files:
- `render.yaml` (root directory)
- `frontend/Dockerfile`
- `frontend/nginx.conf`
- `frontend/.env.production`
- `backend/src/main/resources/application-production.properties`

### 2. Connect to Render

1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click "New" → "Blueprint"
3. Connect your GitHub repository
4. Select the repository containing your ThinkAble code

### 3. Configure Environment Variables

Render will automatically read the `render.yaml` file, but you need to set these environment variables:

#### Backend Service Environment Variables:
```
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=your-super-secure-jwt-secret-key-here
GEMINI_API_KEY=your-gemini-api-key (optional)
CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name (optional)
CLOUDINARY_API_KEY=your-cloudinary-api-key (optional)
CLOUDINARY_API_SECRET=your-cloudinary-api-secret (optional)
GCS_BUCKET_NAME=your-gcs-bucket-name (optional)
GOOGLE_CLOUD_PROJECT_ID=your-gcp-project-id (optional)
```

#### Frontend Service Environment Variables:
```
REACT_APP_API_URL=https://thinkable-backend.onrender.com
REACT_APP_ENVIRONMENT=production
```

### 4. Database Setup

The `render.yaml` file automatically creates a PostgreSQL database. The connection string will be automatically provided to your backend service via the `DATABASE_URL` environment variable.

### 5. Deploy

1. Click "Apply" to start the deployment
2. Render will:
   - Create the PostgreSQL database
   - Build and deploy the backend Spring Boot service
   - Build and deploy the frontend React application

### 6. Access Your Application

Once deployed, you'll have:
- **Frontend**: `https://thinkable-frontend.onrender.com`
- **Backend API**: `https://thinkable-backend.onrender.com`
- **Database**: Automatically managed PostgreSQL instance

## Local Testing

Before deploying, you can test the build process locally:

### Windows:
```bash
build.bat
```

### Linux/Mac:
```bash
chmod +x build.sh
./build.sh
```

## Troubleshooting

### Common Issues:

1. **Build Failures**: Check the build logs in Render dashboard
2. **Database Connection**: Ensure the DATABASE_URL is properly set
3. **CORS Issues**: Verify the CORS_ORIGINS environment variable
4. **API Connection**: Check that REACT_APP_API_URL points to your backend service

### Logs:

- View logs in the Render dashboard for each service
- Backend logs will show Spring Boot startup and any errors
- Frontend logs will show the build process

## Environment Variables Reference

See `.env.example` for a complete list of all available environment variables.

## Support

For deployment issues:
1. Check Render documentation: https://render.com/docs
2. Review the build logs in your Render dashboard
3. Ensure all environment variables are properly set

## Free Tier Limitations

Render's free tier includes:
- Services spin down after 15 minutes of inactivity
- 750 hours per month of runtime
- Limited build minutes

For production use, consider upgrading to a paid plan for better performance and uptime.