#  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
#  Copyright 2024-2025 Dataport. All rights reserved. Extended as part of the POSSIBLE project.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

FROM maven:3-eclipse-temurin-17-alpine AS build
COPY . /opt/
RUN --mount=type=secret,id=GIT_AUTH_TOKEN env GITHUB_TOKEN=$(cat /run/secrets/GIT_AUTH_TOKEN)  \
    mvn -ntp -f /opt/pom.xml -s /opt/settings.xml clean package

FROM eclipse-temurin:17-jre-alpine
EXPOSE 8088
EXPOSE 8443
RUN mkdir /app
WORKDIR /app
COPY --from=build /opt/target/did-web-service-*.jar /app/did-web-service.jar
ENTRYPOINT ["java","-jar","/app/did-web-service.jar"]
