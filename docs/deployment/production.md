## Production Deployment Considerations

This repository is designed as a reference architecture.

In production, this stack would be deployed via **Helm** on Kubernetes/OpenShift, with:
- **ConfigMaps** for shared, non-secret configuration (Kafka bootstrap servers, Schema Registry URL, etc.)
- **Secrets** for sensitive data (database credentials, API keys, mail credentials)
- Environment-specific values provided via Helm `values.yaml`

Docker Compose is intentionally used here to keep the architecture easy to run and reason about locally.
