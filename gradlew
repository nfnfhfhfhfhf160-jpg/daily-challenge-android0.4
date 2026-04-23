#!/usr/bin/env sh

# Gradle Wrapper script for UNIX
APP_HOME=$( cd "${0%/*}" && pwd )
APP_NAME="Gradle"

# Find Java
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    exit 1
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Auto-download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" -o "$WRAPPER_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
    else
        echo "ERROR: curl or wget required to download gradle-wrapper.jar"
        exit 1
    fi
fi

exec "$JAVACMD" \
    $GRADLE_OPTS \
    -Xmx64m \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
