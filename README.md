# Tang

A full implementation of [tang](https://github.com/latchset/tang) server run on node.js

* keys are stored in sqlite database
* to rotate keys, use the /keys/rotate rest api

```sh
## assuming the tang server is running behind a firewall
curl -X POST http://localhost/keys/rotate -d "{}"
```

### recommendation
* run the server behind a firewall

## build from source

```sh
lein cljsbuild prod once
```

## installation from npmjs.org
* does not require compile from source

```sh
npm install tangd
```

## running

```sh
node node_module/tangd/index.js --data /var/db/tangd/key.sqlite3
```

## docker container

```sh
docker run  -p 80:8080 -v /secret/data:/var/db/tangd cloggo/tangd:latest
```
