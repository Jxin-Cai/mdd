FROM openjdk:11


ADD mini-faas.jar /usr/local/grb/core/mini-faas.jar

# 启动脚本
COPY start.sh  /usr/local/grb/core/start.sh

RUN chmod 755 /usr/local/grb/core/start.sh

#暴露端口
EXPOSE 10600

WORKDIR  /usr/local/grb/core

COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas

CMD ["./start.sh"]
