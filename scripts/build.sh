#!/usr/bin/env bash
# Build the time-microservice-java binary for the host platform.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p bin

VERSION="${VERSION:-$(git describe --tags --always --dirty 2>/dev/null || echo dev)}"
COMMIT="${COMMIT:-$(git rev-parse --short HEAD 2>/dev/null || echo none)}"
BUILD_DATE="${BUILD_DATE:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

# Java is platform independent; GOOS/GOARCH are accepted but ignored.
mvn -o -q -DskipTests \
    -Dmaven.compiler.release="${JAVA_RELEASE:-21}" \
    package

JAR="$(ls target/time-microservice-java-*.jar | head -n1)"
cp "$JAR" bin/time-microservice-java.jar

cat > bin/time-microservice-java <<'LAUNCHER'
#!/usr/bin/env bash
# Launcher for the time-microservice-java Java application.
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java ${JAVA_OPTS:-} -jar "$DIR/time-microservice-java.jar" "$@"
LAUNCHER
chmod +x bin/time-microservice-java

echo "Built bin/time-microservice-java"
echo "Built bin/time-microservice-java.jar"