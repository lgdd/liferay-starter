dev:
	./mvnw compile quarkus:dev

devFront:
	cd src/main/frontend && yarn start

build:
	./mvnw clean package

docker:
	docker build -f Dockerfile.jvm -t lgdd/liferay-starter:latest-jvm .

dockerNative:
	docker build -f Dockerfile.native -t lgdd/liferay-starter .

dockerRun:
	docker run -it --rm -p 8000:8000 lgdd/liferay-starter:latest-jvm

dockerRunNative:
	docker run -it --rm -p 8000:8000 lgdd/liferay-starter
