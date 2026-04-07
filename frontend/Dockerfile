FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Wymuszamy, aby Vite nasłuchiwał na wszystkich interfejsach (wymagane w Dockerze)
ENV HOST=0.0.0.0
EXPOSE 5173
CMD ["npm", "run", "dev"]