FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app

WORKDIR /app

RUN apk add --no-cache --update jansson openssl zlib \
    && rm package-lock.json \
    && rm -rf node_modules \
    && npm install

CMD ["node", "/app/index.js"]
