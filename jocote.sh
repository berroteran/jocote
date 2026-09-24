#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if ! command -v java >/dev/null 2>&1; then
    printf '%s\n' 'Se requiere Java 21 o superior.' >&2
    exit 1
fi
if [ ! -f target/jocote-0.1.0-SNAPSHOT.jar ]; then
    if ! command -v mvn >/dev/null 2>&1; then
        printf '%s\n' 'Se requiere Maven 3.9 para compilar Jocote.' >&2
        exit 1
    fi
    mvn -B -ntp package
fi
exec java -jar target/jocote-0.1.0-SNAPSHOT.jar "$@"
