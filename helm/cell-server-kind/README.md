# cell-server-kind Helm Chart

Helm-Chart für `j316-cell-server` auf einem `kind`-Cluster mit HTTPS über Let's Encrypt und automatischer Zertifikatserneuerung durch cert-manager.

## Voraussetzungen

- Kind-Cluster mit Ingress Controller (z. B. ingress-nginx)
- cert-manager installiert
- Öffentliche Erreichbarkeit der Host-Domain für HTTP-01 Challenge

## Installation

```bash
helm upgrade --install cell-server ./helm/cell-server-kind \
  --set ingress.host=cell.example.com \
  --set certManager.clusterIssuer.email=ops@example.com
```

## Erfüllung der Anforderungen

1. **Verschlüsselung per HTTPS / Let's Encrypt**
   - Ingress mit TLS und cert-manager ClusterIssuer (`letsencrypt-prod`).
2. **Automatische Zertifikatserneuerung**
   - `Certificate`-Ressource mit `duration` und `renewBefore`.
3. **Cellserver über Root-URL erreichbar**
   - Ingress-Path ist auf `/` gesetzt.
4. **Wesentliche Umgebungsvariablen vorkonfiguriert**
   - Relevante Werte aus `application.yaml` liegen unter `values.yaml -> env`.

## Wichtige Werte

- `ingress.host`: DNS-Name für den Zugriff
- `certManager.clusterIssuer.email`: Let's Encrypt Kontaktadresse
- `env.ADAPTER_CELL_IP`, `env.ADAPTER_CELL_PORT`: CELL Target
- `env.BASIC_AUTH_USERNAME`, `env.BASIC_AUTH_PASSWORD`: Basis-Authentifizierung
- `env.AUTH0_ENABLED` und Auth0 Variablen für OIDC

