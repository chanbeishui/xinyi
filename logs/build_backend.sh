#!/usr/bin/env bash
# 后端编译校验（本机未装独立 mvn，改用 fat jar 里的 BOOT-INF 作为类路径）。
#
# 用法：bash logs/build_backend.sh
# 退出码 0 表示 xinyi-rongli 全部源码编译通过。
#
# 说明：只校验 com.xinyi.rongli 模块。框架模块（common/framework/system）改动很少，
# 且它们的类已经在 xinyi-admin.jar 的 BOOT-INF/classes 内，改动框架后需重新打包 jar。
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVAC="/d/develop/Java/jdk-21/bin/javac"
JAR="$ROOT/xinyi-admin/target/xinyi-admin.jar"
WORK="/e/codex/.cprep"

if [ ! -f "$JAVAC" ]; then
  echo "未找到 JDK 21：$JAVAC" >&2
  exit 2
fi
if [ ! -f "$JAR" ]; then
  echo "未找到骨架 jar：$JAR（请先在 IDEA 里 Build 一次）" >&2
  exit 2
fi

if [ ! -d "$WORK/BOOT-INF/lib" ]; then
  mkdir -p "$WORK"
  (cd "$WORK" && unzip -qo "$JAR" "BOOT-INF/lib/*" "BOOT-INF/classes/*")
fi

rm -rf "$WORK/out" && mkdir -p "$WORK/out"
# javac 的 @argfile 不接受 MSYS 的 /e/... 路径，改用相对路径
cd "$ROOT"
find xinyi-rongli/src/main/java -name "*.java" > "$WORK/srcs.txt"

CP="$(cygpath -w "$WORK")/BOOT-INF/classes;$(cygpath -w "$WORK")/BOOT-INF/lib/*"
echo "编译 $(wc -l < "$WORK/srcs.txt") 个源文件 ..."
"$JAVAC" -J-Duser.language=en -nowarn -proc:none -encoding UTF-8 -parameters \
  -d "$WORK/out" -cp "$CP" "@$WORK/srcs.txt"
rc=$?
if [ $rc -eq 0 ]; then
  echo "OK: xinyi-rongli 编译通过（产出 $(ls -1 "$WORK/out" | wc -l) 个顶层包目录）"
fi
exit $rc
