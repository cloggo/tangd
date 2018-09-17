FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app

WORKDIR /app

RUN rm package-lock.json \
    && rm -rf node_modules \
    && npm install --registry https://npm.delite.ca

CMD ["node", "/app/index.js"]
