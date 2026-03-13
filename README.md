# j316-cell-server

This application is designed as a simple webserver running on the network node and providing some simple interface for producing TCP Socket calls to the CELL Instance.

## Authentication

The server supports two authentication methods:

- HTTP Basic auth (always enabled) via `adapter.security.username` and `adapter.security.password`
- OAuth2/OIDC Login via Okta (optional) using Spring Security `oauth2Login` (similar to the Auth0/Okta login sample approach)

### Okta OAuth2 configuration

Set the following environment variables to enable OAuth2 login via Okta:

- `OAUTH2_ENABLED=true`
- `OKTA_ISSUER_URI=https://<your-okta-domain>/oauth2/default`
- `OKTA_CLIENT_ID=<your-client-id>`
- `OKTA_CLIENT_SECRET=<your-client-secret>`

When all values are configured, Spring Security enables OAuth2 login and accepts authenticated user sessions from the OIDC provider.
HTTP Basic authentication remains available in parallel.

## Docker

### Build image

```bash
docker build -t j316/cell-server:local .
```

### Start with Docker Compose

```bash
docker compose up -d --build
```

The service is available on `http://localhost:8081`.

Default compose environment values:

- `BASIC_AUTH_USERNAME=test`
- `BASIC_AUTH_PASSWORD=test`
- `AUTH0_ENABLED=false`
- `ADAPTER_CELL_IP=host.docker.internal`
- `ADAPTER_CELL_PORT=5005`

You can adjust these values directly in `docker-compose.yml`.

