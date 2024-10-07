# POSSIBLE DID Web Service
The DID Web service is a microservice in the POSSIBLE dataspace which handles:
- Provision of a new did:web that can be used for signing and verification.
- Provision of the DID document for a did:web that the service manages.
  - Currently, the DID document lists one verification method that refers to the common public key.
- Provision of the certificate associated with a did:web that the service manages.

## Structure

```
├── src/main/java/eu/merloteducation/didservice
│   ├── config          # configuration-related components
│   ├── controller      # external REST API controllers
│   ├── models          # internal data models
│   ├── repositories    # DAOs for accessing the stored data
│   ├── service         # internal services for processing data from the controller layer
```

## Configuration

For a full list of configuration options (including Spring/JPA options) please see the 
[application.yml](src/main/resources/application.yml).

| Key                                | Description                                                                                                                              | Default |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|-------|
| server.port                        | Sets the https port under which the service will run                                                                                     | 443   |
| did-domain                         | Domain of the server hosting this DID-Service, used to reference it in the did documents                                                 |   localhost    |
| certificate-issuer                 | Issuer that will be set in the self-generated certificates                                                                               |  MERLOT Federation     |
| common-cert-path                   | (optional) path to a certificate that should be listed in all generated did documents (e.g. for a common public key within a federation) ||
| common-verification-method-enabled | flag to use the above mentioned common certificate in the did documents                                                                  | false |



## Run
    # note that sudo is needed on most systems to bind to the port 443 for https
    sudo java -jar target/did-service-X.Y.Z.jar

## Deploy (Docker)

This microservice can be deployed as part of the full MERLOT docker stack at
[localdeployment](https://github.com/merlot-education/localdeployment).

## Deploy (Helm)
### Prerequisites
Before you begin, ensure you have Helm installed and configured to the desired Kubernetes cluster.

### Setting Up Minikube (if needed)
If you don't have a Kubernetes cluster set up, you can use Minikube for local development. Follow these steps to set up Minikube:

1. **Install Minikube:**
   Follow the instructions [here](https://minikube.sigs.k8s.io/docs/start/) to install Minikube on your machine.

2. **Start Minikube:**
   Start Minikube using the following command:
   ```
   minikube start
   ```
3. **Verify Minikube Status:**
   Check the status of Minikube to ensure it's running:   
   ```
   minikube status
   ```

### Usage
1. **Clone the Repository:**
   Clone the repository containing the Helm chart:
   ```
   git clone https://github.com/merlot-education/gitops.git
   ```
   
2. **Navigate to the Helm Chart:**
   Change into the directory of the Helm chart:
   ```
   cd gitops/charts/orchestrator
   ```
   
3. **Customize Values (if needed):**
   If you need to customize any values, modify the values.yaml file in this directory according to your requirements. This file contains configurable parameters such as image repository, tag, service ports, etc. An example containing the values used in Merlot dev environment is available in gitops/environments/dev/didservice-orchestrator.yaml

4. **Install the Chart:**
   Run the following command to install the chart from the local repository:
   ```
   helm install [RELEASE_NAME] .
   ```
   Replace [RELEASE_NAME] with the name you want to give to this deployment. In this case it can be did-service.

5. **Verify Deployment:**
   Check the status of your deployment using the following commands:
   ```
   kubectl get pods
   kubectl get services
   ```
   
### Additional Resources 
- [Helm Documentation](https://helm.sh/docs/)
