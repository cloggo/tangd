BUILDER_IMAGE := registry.delite.ca/docker/base/alpine/3_8:clojure-lein-node
SERVICE_IMAGE := registry.delite.ca/docker/base/node/10:dev
DOCKER_BUILDER_CMD := docker run --rm -it -w /app -v $(CURDIR)/tmp:/root -v $(CURDIR)/tangd:/app
DOCKER_SERVICE_CMD := docker run --rm -it -w /app -v $(CURDIR)/tangd:/app
NODEMON_BIN := node_modules/.bin/nodemon 
DEV_MAIN_JS := target/js/compiled/tangd.js
DEV_TEST_JS := target/js/compiled/test.js

tangd/node_modules: tangd/package.json tangd/package-lock.json
	$(DOCKER_SERVICE_CMD)  $(SERVICE_IMAGE) npm install

.PHONY: figwheel

figwheel:
	$(DOCKER_BUILDER_CMD) -p 4001:4001 -p 3449:3449 $(BUILDER_IMAGE) lein figwheel

.PHONY: build-auto

build-auto:
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) lein cljsbuild auto dev

.PHONY: build-test-auto

build-test-auto:
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) lein cljsbuild auto test

# older nodejs < 10 required must use nodemon -L to work properly
start-test: node_modules
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) $(NODEMON_BIN) --watch src --watch test -L -e js,cljs --exec "lein" doo node test once

.PHONY: build-auto-daemon

build-auto-daemon:
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) lein cljsbuild auto dev

.PHONY: build

build:
	$(DOCKER_BUILDER_CMD) $(BUILDER_IMAGE) lein cljsbuild once prod

.PHONY: nrepl

nrepl:
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) lein repl

.PHONY: start-dev

start-dev: node_modules
	$(DOCKER_SERVICE_CMD) -e NODE_ENV=dev -p 8081:8080 $(SERVICE_IMAGE) $(NODEMON_BIN) -e js --watch $(DEV_MAIN_JS) $(DEV_MAIN_JS)

.PHONY: start

start: node_modules
	$(DOCKER_SERVICE_CMD) -e NODE_ENV=production -p 8080:8080 $(SERVICE_IMAGE) node server.js

.PHONY: js-cli

js-cli: node_modules
	$(DOCKER_SERVICE_CMD) -p 8081:8080  $(SERVICE_IMAGE) sh

.PHONY: cl-cli

cl-cli: node_modules
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) bash

.PHONY: clean

clean:
	$(DOCKER_BUILDER_CMD) -v $(CURDIR)/tmp:/root $(BUILDER_IMAGE) lein clean
	@RM -rf node_modules
