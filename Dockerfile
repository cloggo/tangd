FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app

WORKDIR /app

RUN apk add --no-cache --update jansson openssl zlib \
    && rm package-lock.json \
    && rm -rf node_modules \
    && npm install

ADD ./docker-entrypoint.sh /docker-entrypoint.sh

RUN cp -f /app/rotate-keys.sh /usr/bin/rotate-keys \
    && chmod +x /usr/bin/rotate-keys \
    && chmod +x /docker-entrypoint.sh

CMD ["node", "/app/index.js", "--data", "/var/db/tangd/keys.sqlite3"]
