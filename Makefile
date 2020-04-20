dev:
	./mvnw compile quarkus:dev

devFront:
	cd src/main/frontend && yarn start

dockerBuild:
	./mvnw clean package -Dquarkus.container-image.build=true

dockerBuildNative:
	./mvnw clean package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true

dockerRun:
	docker run -it --rm -p 9080:8080 lgdd/liferay-starter
