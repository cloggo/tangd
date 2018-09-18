A full implementation of tang server run on node.js

## build

```sh
lein cljsbuild prod once
```

## Installation

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
