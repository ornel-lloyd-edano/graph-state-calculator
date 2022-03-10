FROM openjdk:8-jre-alpine
WORKDIR /opt/docker
ADD --chown=daemon:daemon opt /opt
USER daemon
ENTRYPOINT ["/opt/docker/bin/ornellloydassignment"]
CMD []
USER root
RUN ["apk", "add", "--no-cache", "bash"]
