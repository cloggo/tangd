#!/bin/sh

if [ ! -f /var/db/tangd/keys.sqlite3 ]; then
    /usr/bin/rotate-keys
fi

exec "$@"
