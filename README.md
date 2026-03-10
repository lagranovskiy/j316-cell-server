# j316-cell-server

This application is designed as a simple webserver running on the network node and providing some simple interface for producing TCP Socket calls to the CELL Instance.

## Authentication

The server supports two authentication methods:

- HTTP Basic auth (always enabled) via `adapter.security.username` and `adapter.security.password`
- Auth0 JWT Bearer auth (optional) via Spring OAuth2 resource server

### Auth0 configuration

Set the following environment variables to enable Auth0:

- `AUTH0_ENABLED=true`
- `AUTH0_ISSUER_URI=https://<your-tenant>.auth0.com/`

When Auth0 is enabled and an issuer URI is provided, bearer tokens signed by Auth0 are accepted in addition to HTTP Basic authentication.
