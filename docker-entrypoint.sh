#!/bin/sh

if [ -f /usr/bin/rotate-keys ]; then
    /usr/bin/rotate-keys
fi

exec "$@"
