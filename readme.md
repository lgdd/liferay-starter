# Liferay Starter

Quickly generate & download your Liferay workspace. Inspired by [start.spring.io](https://start.spring.io/) and [code.quarkus.io](https://code.quarkus.io/).

Try it online: https://lfr-starter.duckdns.org/.

![preview](doc/preview.jpg)

## Try it locally

Using Docker:
```shell
docker run -it --rm -p 9080:8080 lgdd/liferay-starter
# or
docker run -it --rm -p 9080:8080 lgdd/liferay-starter:jvm-latest
```
> Default image `lgdd/liferay-starter:latest` is a container image including a native executable instead of a jar. See: https://quarkus.io/guides/building-native-image.

Using this repo:
```
git clone git@github.com:lgdd/liferay-starter.git
cd liferay-starter
make dev
```
> Please refer to the [Makefile](Makefile) to see the complete list of available commands.

## License
[MIT](LICENSE)
