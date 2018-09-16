FOREVER_BIN := node_modules/.bin/forever
NODEMON_BIN := node_modules/.bin/nodemon
DEV_MAIN_JS := target/js/compiled/app.js
DEV_TEST_JS := target/js/compiled/test.js

app/node_modules: package.json
	npm install --registry https://npm.delite.ca

.PHONY: figwheel

figwheel: app/node_modules
	lein figwheel

.PHONY: once

build-once: app/node_modules
	lein cljsbuild once dev

.PHONY: build-auto

build-auto: app/node_modules
	lein cljsbuild auto dev

.PHONY: build-test-auto

build-test-auto: app/node_modules
	lein cljsbuild auto test

# older nodejs < 10 required must use nodemon -L to work properly
start-test: app/node_modules
	$(NODEMON_BIN) --watch src --watch test --watch lib -L -e js,cljs --exec "lein" doo node test once

.PHONY: build-auto-daemon

build-auto-daemon: app/node_modules
	lein cljsbuild auto dev

.PHONY: build

build: app/node_modules
	@RM -f app/index.js
	lein cljsbuild once prod

.PHONY: nrepl

nrepl:
	lein repl

.PHONY: start-dev

start-dev: app/node_modules
	$(NODEMON_BIN) -e js --watch $(DEV_MAIN_JS) $(DEV_MAIN_JS)

start-dbg: app/node_modules
	$(NODEMON_BIN) -e js --watch $(DEV_MAIN_JS) --inspect-brk=0.0.0.0 $(DEV_MAIN_JS)

.PHONY: start

start: app/node_modules
	node index.js

.PHONY: clean

clean:
	lein clean
	@RM -rf app/node_modules
