# Multi-stage build for React frontend

# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY public/ ./public/
COPY src/ ./src/
COPY tsconfig.json ./

# Build the application
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx content
RUN rm -rf ./*

# Copy built app from build stage
COPY --from=build /app/build .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 3000

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
