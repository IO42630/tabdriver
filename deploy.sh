#!/bin/bash

set -euo pipefail

skip_tests=()
for arg in "$@"; do
    case "$arg" in
        --skip-tests) skip_tests+=(-DskipTests) ;;
        *)
            printf 'Usage: %s [--skip-tests]\n' "$0" >&2
            exit 2
            ;;
    esac
done

cd "$(dirname "$0")"
. .env

export MAVEN_OPTS="${MAVEN_OPTS:-} --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow"

: "${MVN_SETTINGS:?MVN_SETTINGS must point to the Maven settings directory}"

for command in mvn curl gpg; do
    command -v "$command" >/dev/null 2>&1 || {
        printf 'Required command not found: %s\n' "$command" >&2
        exit 127
    }
done

project_value() {
    mvn --batch-mode --no-transfer-progress -q help:evaluate -Dexpression="$1" -DforceStdout
}

group_id="$(project_value project.groupId)"
artifact_id="$(project_value project.artifactId)"
version="$(project_value project.version)"
central_url="https://repo1.maven.org/maven2/${group_id//.//}/$artifact_id/$version/$artifact_id-$version.pom"

if ! http_code="$(curl --silent --show-error --location --output /dev/null \
    --connect-timeout 10 --max-time 30 --write-out '%{http_code}' "$central_url")"; then
    printf 'Maven Central could not be reached; deployment skipped\n' >&2
    exit 1
fi

case "$http_code" in
    200)
        printf 'Already published on Maven Central: %s:%s:%s\n' \
            "$group_id" "$artifact_id" "$version"
        exit 0
        ;;
    404) ;;
    *)
        printf 'Maven Central returned HTTP %s; deployment skipped\n' "$http_code" >&2
        exit 1
        ;;
esac

gpg --batch --yes --pinentry-mode loopback --import "$MVN_SETTINGS/gpg-private.asc"

exec mvn --batch-mode --no-transfer-progress clean deploy -e -Prelease \
    -s "$MVN_SETTINGS/maven-settings.xml" "${skip_tests[@]}"
