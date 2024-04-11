################################################################################################
# Katanox Makefile
#
# This Makefile is split into three sections:
#   - Application: for building, testing, and publishing the project.
#   - Docker Image: for building, testing, and publishing the Docker image that is used to run the project.
#   - Deployments: for deploying the project and its infrastructure.
#
# We write our rule names in the following format: [verb]-[noun]-[noun], e.g. "build-jars".
#
# Variables ####################################################################################

VERSION?=DEV-SNAPSHOT

APP_NAME=modern-reactive-spring-backend-kotlin
APP_JAR_LOCATION=./build/libs/$(APP_NAME)-$(VERSION).jar
APP_PORT?=8080

IMAGE_ID=$(APP_NAME):$(VERSION)
IMAGE_SAVE_LOCATION?=./build/images

# Application ##################################################################################

build-app: generate-jooq
	@echo "******** Building the application... ********"
	VERSION=$(VERSION) ./gradlew build -x test --info
	$(call clean-app-infrastructure)

test-app: generate-jooq
	@echo "******** Testing the application... ********"
	VERSION=$(VERSION) ./gradlew test --tests *Test --info
	$(call clean-app-infrastructure)

test-integration-app: generate-jooq
	@echo "******** Testing (Integration) the application... ********"
	VERSION=$(VERSION) ./gradlew test --tests *IT --info
	$(call clean-app-infrastructure)

generate-jooq: deploy-app-infrastructure-database
	@echo "******** Generating Jooq classes... ********"
	./gradlew clean generateJooq --info
	$(call clean-app-infrastructure)

# Docker Image ##################################################################################

build-docker-image: build-app
	@echo "******** Building the Docker image... ********"
	docker build \
		--build-arg APP_NAME=$(APP_NAME) \
		--build-arg APP_VERSION=$(VERSION) \
		--build-arg APP_JAR_LOCATION=$(APP_JAR_LOCATION) \
		--build-arg APP_PORT=$(APP_PORT) \
		--tag $(IMAGE_ID) .

save-docker-image: build-docker-image
	@echo "******** Saving the Docker image... ********"
	mkdir -p $(IMAGE_SAVE_LOCATION) && \
	docker save -o $(IMAGE_SAVE_LOCATION)/$(APP_NAME)-$(VERSION).tar $(IMAGE_ID)

run-docker-image: build-docker-image
	@echo "******** Running the Docker image... ********"
	docker run -p $(APP_PORT):$(APP_PORT) $(IMAGE_ID)

# Deployments ###################################################################################

deploy-app: build-docker-image
	@echo "******** Deploying the application... ********"
	docker compose -f local/docker-compose.yml up -d --force-recreate --remove-orphans

deploy-app-infrastructure-database:
	@echo "******** Deploying the application database... ********"
	docker compose -f local/docker-compose.yml up -d postgres --wait

deploy-app-infrastructure-rabbitmq:
	@echo "******** Deploying the application RabbitMQ... ********"
	docker acompose -f local/docker-compose.yml up -d rabbitmq --wait

undeploy-app:
	@echo "******** Undeploying the application... ********"
	$(call clean-app-infrastructure)

# Functions ####################################################################################

define clean-app-infrastructure
	@echo "******** Cleaning the application infrastructure... ********"
	docker compose -f local/docker-compose.yml down
endef
