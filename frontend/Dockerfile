FROM node:20-alpine AS builder
WORKDIR /app

ARG VITE_API_BASE_URL
ARG FRONTEND_URL
ARG FILE_SERVER_URL

COPY package*.json ./
RUN npm ci --silent

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV FRONTEND_URL=$FRONTEND_URL
ENV FILE_SERVER_URL=$FILE_SERVER_URL

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80