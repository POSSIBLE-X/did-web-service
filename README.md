# POSSIBLE DID Web Service

The DID Web service is a microservice in the POSSIBLE dataspace which handles:

- Provision of a new did:web that can be used for signing and verification.
- Provision of the DID document for a did:web that the service manages.
  - Currently, the DID document lists one verification method that refers to the common public key.
- Provision of the certificate associated with a did:web that the service manages.

## Structure

```
├── src/main/java/eu/possiblex/didservice
│   ├── config          # configuration-related components
│   ├── controller      # external REST API controllers
│   ├── models          # internal data models
│   ├── repositories    # DAOs for accessing the stored data
│   ├── service         # internal services for processing data from the controller layer
```

## Configuration

For a full list of configuration options (including Spring/JPA options) please see the
[application.yml](src/main/resources/application.yml).

| Key              | Description                                                                                                                   | Default   |
|------------------|-------------------------------------------------------------------------------------------------------------------------------|-----------|
| server.port      | Sets the https port under which the service will run                                                                          | 4443      |
| did-domain       | Domain of the server hosting this DID-Service, used to reference it in the did documents                                      | localhost |
| common-cert-path | path to a certificate that should be listed in all generated did documents (e.g. for a common public key within a federation) |           |

## Run

    # note that sudo is needed on most systems to bind to the port 443 for https
    sudo java -jar target/did-web-service-X.Y.Z.jar

## Endpoints

The following endpoints are made available by the DID service:

| Endpoint                       | Description                                                                                                                                                                         |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| POST /internal/didweb/generate | internal non-public endpoint that generates a new did-web identity on demand given the subject in the payload                                                                       |
| PATCH /internal/didweb/update  | internal non-public endpoint that updates an existing did-web identity given the non-null content in the payload. Currently this only supports adding/removing aliases for the did. |
| DELETE /internal/didweb/remove | internal non-public endpoint that deletes an existing did-web identity with the id in the content in the payload.                                                                   |
| /participant/{id}/did.json     | returns the DID document for a given participant id                                                                                                                                 |
| /.well-known/did.json          | returns a common did document for the dataspace federation identity                                                                                                                 |
| /.well-known/cert.ss.pem       | returns a common certificate for the dataspace federation identity (currently referenced in all generated did documents)                                                            |

