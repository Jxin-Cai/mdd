#!/bin/bash

APP=mini-faas
APP_JAR=${APP}".jar"

# 内存分配
JAVA_MEM_OPTS="-server -Xmx4g -Xms4g -XX:NewRatio=2 -XX:SurvivorRatio=8 -XX:MetaspaceSize=64m -Xss256k -XX:ThreadStackSize=256k -Dio.grpc.netty.shaded.io.netty.transport.noNative=true"

${JAVA_HOME}/bin/java $JAVA_MEM_OPTS  -jar /usr/local/grb/core/$APP_JAR

