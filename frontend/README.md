# lottery-platform-frontend

## Start

```bash
npm install
npm run dev
```

Default local testing ports:

- Frontend dev server: `9510`
- Frontend preview server: `9511`
- Backend gateway target: `http://localhost:9008`
- Local Nexus npm group: `http://localhost:8081/repository/npm-public/`

The Vite dev server proxies all `/lottery-*`, `/swagger-ui*`, `/v3/api-docs*`, and `/actuator*`
requests to the backend gateway.

This project now includes a checked-in `.npmrc`, so host-side `npm install` will go through the local
Nexus npm group after the Docker stack is started.

If you want to override the defaults, copy `.env.example` to `.env` and adjust the values.

For public tunnel testing (for example `natapp`), the dev server now supports:

- `VITE_ALLOWED_HOSTS=*`: allow requests from any host during development.
- `VITE_PUBLIC_HOST=<your tunnel domain>`: optional. Use this if the page opens through the tunnel but Vite hot reload
  cannot connect.
- `VITE_PUBLIC_PROTOCOL=https`: optional. Keep `https` for `natapp` HTTPS tunnels.

## Build

```bash
npm run build
```

## Docker + Nginx

The backend compose stack now builds a dedicated frontend container from `Dockerfile.dev` and lets the
Nginx container proxy browser traffic to it.

If you are using a public tunnel such as `natapp`, expose the Nginx port instead of `9510`, otherwise
the request path will bypass Nginx entirely.
