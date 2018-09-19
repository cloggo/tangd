# Tang

[![npm](https://img.shields.io/npm/v/tangd.svg)](https://www.npmjs.com/package/tangd)

Node.JS javascript binding for [JOSE](https://github.com/latchset/jose.git) using N-API

## Installation 

### prerequisites
* jansson
* openssl-dev
* zlib-dev

```
npm install
```

darwin (from source):

```bash
## assumed dependencies were installed with darwin ports 
CFLAGS=-I/opt/local/include LDFLAGS=-L/opt/local/lib npm install -g
```



A full implementation of [tang](https://github.com/latchset/tang) server run on node.js

* keys are stored in sqlite database
* low memory usage, required less than 30MB of memory
* very high performance and scalable

### recommendation
* run the server behind a firewall
* to rotate keys, use the /keys/rotate REST api

```sh
## assuming the tang server is running behind a firewall
curl -X POST http://localhost/keys/rotate -d "{}"
```

## Build from source

```sh
lein cljsbuild prod once
```

## Installation from npmjs.org
* does not require compile from source

```sh
npm install tangd
```

## Running

```sh
node node_module/tangd/index.js --data /var/db/tangd/key.sqlite3 --port 8080

# must also initiate a key rotation to create new keys
```

## Docker container

```sh
docker run -p 80:8080 -v /secret/data:/var/db/tangd cloggo/tangd:latest

# must also initiate a key rotation to create new keys 
```
