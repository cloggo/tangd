# tangd

credit: [Nathanial McCallum et Al.](mailto:npmccallum@redhat.com)

[![npm](https://img.shields.io/npm/v/tangd.svg)](https://www.npmjs.com/package/tangd)

A full implementation of [tang](https://github.com/latchset/tang) server run on node.js

* fully backward compatible with the old implementation by McCallum et Al.
* keys are stored in sqlite database
* low memory usage, required less than 30MB of memory
* very high performance and scalable
* ease of use without sacrificing security

## Why develop a new tang server implemenation?

* design to be deployed on any platform
* design to be hosted as a containerized service application
* strive to achieve better performance, reliability and scalability
* resolve issues quicker because it is based on a better code base
* treat users with utmost respect

## Available REST API

| Method | Path	        | Operation                                                |
|--------|--------------|----------------------------------------------------------|
| GET	 | /adv	        | Fetch public keys                                        |
| GET	 | /adv/{kid}   | Fetch public keys using specified signing key            |
| POST	 | /rec/{kid}   | Perform recovery using specified exchange key            |
| POST   | /keys/rotate | Generate new keys (reserved for whitelisted ip addresses |

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
# command switches:
#   --port, -p          server port number
#   --data, -d          database file path
#   --ip-whitelist, -l  whitelisted ip to access security sensitive API

node node_module/tangd/index.js --data /var/db/tangd/key.sqlite3 --port 8080 --ip-whitelist "10.6.0.4 10.8.0.9"

# must also initiate a key rotation to create new keys
```

## Docker container

```sh
docker run -p 80:8080 -e IP_WHITELIST="10.6.0.4 10.8.0.9" -v /secret/data:/var/db/tangd cloggo/tangd:latest

# must also initiate a key rotation to create new keys 
```

## Keys rotation

* this is equivalent to the original tang command tangd-keygen
* only whitelisted ip addresses are allowed to access this API
* by defaults, the whitelisted ip addresses are [::1 127.0.0.1]

```sh
curl -X POST http://localhost/keys/rotate -d "{}"
```
