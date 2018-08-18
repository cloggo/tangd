BUILDER_IMAGE := registry.delite.ca/docker/base/alpine/3_8:clojure-lein
SERVICE_IMAGE := registry.delite.ca/docker/base/node/10:latest
DOCKER_CMD := docker run -it -w /app -e HOME=/app -v $(CURDIR)/tmp:/root -v $(CURDIR)/tangd:/app

node_modules:
	$(DOCKER_CMD) $(SERVICE_IMAGE) npm install

nrepl:
	$(DOCKER_CMD) -p 4001:4001 $(BUILDER_IMAGE) lein repl

start: node_modules
	$(DOCKER_CMD) $(SERVICE_IMAGE) node target/js/compiled/tangd.js
