#!/bin/sh
APP_HOME=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
exec java -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
