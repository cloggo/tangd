FROM registry.delite.ca/docker/base/node/10:latest

ADD app/ /app
ADD app/node_modules/ /app/node_modules
