BUILDER_IMAGE := registry.delite.ca/docker/base/alpine/3_8:clojure-lein
SERVICE_IMAGE := registry.delite.ca/docker/base/node/10:dev
DOCKER_BUILDER_CMD := docker run -it -w /app -v $(CURDIR)/tmp:/root -v $(CURDIR)/tangd:/app
DOCKER_SERVICE_CMD := docker run -it -w /app -v $(CURDIR)/tangd:/app -e NODE_ENV=dev

node_modules:
	$(DOCKER_SERVICE_CMD)  $(SERVICE_IMAGE) npm install

figwheel:
	$(DOCKER_BUILDER_CMD) -p 4001:4001 -p 3449:3449 $(BUILDER_IMAGE) lein figwheel

nrepl:
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) lein repl

start: node_modules
	$(DOCKER_SERVICE_CMD)  $(SERVICE_IMAGE) node target/js/compiled/tangd.js

js-cli: node_modules
	$(DOCKER_SERVICE_CMD)  $(SERVICE_IMAGE) sh

cl-cli: node_modules
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) bash
