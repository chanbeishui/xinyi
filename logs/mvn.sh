#!/usr/bin/env bash
# 用 IDEA 自带的 Maven 3.9.9 发行库在命令行跑 Maven（本机未安装独立 mvn）。
# 用法：bash logs/mvn.sh -o -q -pl xinyi-rongli -am compile
set -euo pipefail

JAVA="/d/develop/Java/jdk-21/bin/java"
MVN="/d/Program Files/JetBrains/IntelliJ IDEA 2024.1.4/plugins/maven/lib/maven3"
PROJ="$(cd "$(dirname "$0")/.." && pwd)"

# 显式拼 classpath：Java 的 lib/* 通配在含空格的 MSYS 路径下不可靠，逐 jar 拼更稳
CP=""
for jar in "$MVN"/lib/*.jar "$MVN"/boot/*.jar \
           "/d/Program Files/JetBrains/IntelliJ IDEA 2024.1.4/plugins/maven/lib/maven-slf4j-provider-3.9.9.jar" \
           "$HOME"/.m2/repository/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar \
           "$HOME"/.m2/repository/org/codehaus/plexus/plexus-utils/3.5.1/plexus-utils-3.5.1.jar; do
  [ -f "$jar" ] || continue
  CP="${CP}${CP:+;}$(cygpath -w "$jar")"
done

WIN_PROJ="$(cygpath -w "$PROJ")"
WIN_MVN="$(cygpath -w "$MVN")"

exec "$JAVA" -Xmx1g \
  -Dmaven.home="$WIN_MVN" \
  -Dmaven.multiModuleProjectDirectory="$WIN_PROJ" \
  -cp "$CP" \
  org.apache.maven.cli.MavenCli -f "$WIN_PROJ/pom.xml" "$@"
