#!/bin/sh
set -eu

frontend_mode="${FRONTEND_MODE:-static}"
frontend_dev_server="${FRONTEND_DEV_SERVER:-http://host.docker.internal:9510}"
gateway_server="${GATEWAY_SERVER:-http://host.docker.internal:9008}"
target_file="/etc/nginx/conf.d/default.conf"

cat > "$target_file" <<EOF
map \$http_upgrade \$connection_upgrade {
  default upgrade;
  '' close;
}

server {
  listen 80;
  server_name _;

  client_max_body_size 20m;

  proxy_set_header Host \$host;
  proxy_set_header X-Real-IP \$remote_addr;
  proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto \$scheme;
EOF

if [ "$frontend_mode" = "dev" ]; then
  cat >> "$target_file" <<EOF

  location / {
    proxy_http_version 1.1;
    proxy_set_header Upgrade \$http_upgrade;
    proxy_set_header Connection \$connection_upgrade;
    proxy_pass ${frontend_dev_server};
  }
EOF
else
  cat >> "$target_file" <<'EOF'

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }
EOF
fi

cat >> "$target_file" <<EOF

  location /lottery-user/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-activity/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-lottery/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-award/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-pay/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-workflow/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-file/ {
    proxy_pass ${gateway_server};
  }

  location /lottery-monitor/ {
    proxy_pass ${gateway_server};
  }

  location /swagger-ui {
    proxy_pass ${gateway_server};
  }

  location /swagger-ui/ {
    proxy_pass ${gateway_server};
  }

  location /swagger-ui.html {
    proxy_pass ${gateway_server};
  }

  location /v3/api-docs {
    proxy_pass ${gateway_server};
  }

  location /v3/api-docs/ {
    proxy_pass ${gateway_server};
  }

  location /actuator/ {
    proxy_pass ${gateway_server};
  }
}
EOF
