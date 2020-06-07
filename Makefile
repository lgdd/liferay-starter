dev:
	./mvnw compile quarkus:dev

devFront:
	cd src/main/frontend && yarn start

build:
	./mvnw clean package

docker:
	docker build -f src/main/docker/Dockerfile.jvm -t lgdd/liferay-starter:latest-jvm .

dockerNative:
	docker build -f src/main/docker/Dockerfile.native -t lgdd/liferay-starter .

dockerRun:
	docker run -it --rm -p 9080:8080 lgdd/liferay-starter:latest-jvm

dockerRunNative:
	docker run -it --rm -p 9080:8080 lgdd/liferay-starter
