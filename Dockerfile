FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app

WORKDIR /app

RUN npm install --registry https://npm.delite.ca

CMD ["node", "/app/index.js"]
