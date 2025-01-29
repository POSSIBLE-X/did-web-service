# POSSIBLE DID Web Service

The DID Web service is a microservice in the POSSIBLE dataspace which handles:

- Provisioning of a new did:web that can be used for signing and verification.
- Deleting and updating existing did:web identities.
- Provisioning of the DID document for a did:web that the service manages.
- Provisioning of the certificates associated with a did:web that the service manages.

## Structure

```
├── src/main/java/eu/possiblex/didservice
│   ├── config          # configuration-related components
│   ├── controller      # external REST API controllers
│   ├── models          # internal data models
│   ├── repositories    # DAOs for accessing the stored data
│   ├── service         # internal services for processing data from the controller layer
│   ├── utils           # shared static functionality
```

## Configuration

For a full list of configuration options (including Spring/JPA options) please see the
[application.yml](src/main/resources/application.yml).

| Key                                  | Description                                                                                                                                                       | Default   |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| server.port                          | Sets the https port under which the service will run                                                                                                              | 4443      |
| did-web-domain                       | Domain of the server hosting this DID-Web-Service, used to reference it in the did documents                                                                      | localhost |
| common-verification-method.enabled   | if enabled, all did:web identities hosted by this service will reference a common (federation) verification method in addition to their own verification methods. | true      |
| common-verification-method.cert-path | see previous, path to the common certificate that corresponds to the common verification method                                                                   |           |
| common-verification-method.id        | see previous, id of the common verification method in each did document                                                                                           |           |

## Run

    # note that sudo is needed on most systems to bind to the port 443 for https
    sudo java -jar target/did-web-service-X.Y.Z.jar

## Endpoints

The following endpoints are made available by the DID service:

| Endpoint                                         | Description                                                                                                   |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| POST /internal/didweb                            | management endpoint that generates a new did-web identity on demand given the subject and data in the payload |
| PATCH /internal/didweb                           | management endpoint that updates an existing did-web identity given the non-null content in the payload.      |
| DELETE /internal/didweb/{did}                    | management endpoint that deletes an existing did-web identity with the given did.                             |
| /participant/{participantId}/did.json            | returns the DID document for a given participant id.                                                          |
| /participant/{participantId}/{certificateId}.pem | returns the participant specific certificate with the given id.                                               |
| /.well-known/did.json                            | returns the common did document for the dataspace federation identity.                                        |
| /.well-known/cert.ss.pem                         | returns the common certificate for the dataspace federation identity.                                         |

For a more detailed documentation, check out the Swagger UI that is available upon starting the app
at https://localhost:4443/swagger-ui/index.html .