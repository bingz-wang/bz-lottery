FROM nginx:1.27-alpine

COPY conf/nginx/docker-entrypoint.d/40-generate-default-conf.sh /docker-entrypoint.d/40-generate-default-conf.sh

RUN chmod +x /docker-entrypoint.d/40-generate-default-conf.sh
