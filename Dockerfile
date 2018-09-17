FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app

WORKDIR /app

RUN rm /app/package-lock.json \
    && npm install

CMD ["node", "/app/index.js"]
