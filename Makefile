BUILDER_IMAGE := registry.delite.ca/docker/base/alpine/3_8:clojure-lein
SERVICE_IMAGE := registry.delite.ca/docker/base/node/10:dev
DOCKER_BUILDER_CMD := docker run -it -w /app -v $(CURDIR)/tmp:/root -v $(CURDIR)/tangd:/app
DOCKER_SERVICE_CMD := docker run -it -w /app -v $(CURDIR)/tangd:/app -e NODE_ENV=dev
NODEMON_BIN := node_modules/.bin/nodemon
DEV_MAIN_JS := target/js/compiled/tangd.js

node_modules: tangd/package.json tangd/package-lock.json
	$(DOCKER_SERVICE_CMD)  $(SERVICE_IMAGE) npm install

figwheel:
	$(DOCKER_BUILDER_CMD) -p 4001:4001 -p 3449:3449 $(BUILDER_IMAGE) lein figwheel

build-auto:
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) lein cljsbuild auto dev

nrepl:
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) lein repl

start-dev: node_modules
	$(DOCKER_SERVICE_CMD) -p 8081:8080 $(SERVICE_IMAGE) $(NODEMON_BIN) -e js --watch $(DEV_MAIN_JS) $(DEV_MAIN_JS)

start: node_modules
	$(DOCKER_SERVICE_CMD) -p 8080:8080 $(SERVICE_IMAGE) node target/js/compiled/server.js

js-cli: node_modules
	$(DOCKER_SERVICE_CMD) -p 8081:8080  $(SERVICE_IMAGE) sh

cl-cli: node_modules
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) bash
