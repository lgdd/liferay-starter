# Changelog

## [2.6.0](https://github.com/lgdd/liferay-starter/compare/v2.5.4...v2.6.0) (2024-04-16)

### Chore

* Upgrade Quarkus to 3.9.3
* Upgrade Java to 17

## [2.5.4]

### Features

* add 7.4 support ([6a5f448](https://github.com/lgdd/liferay-starter/commit/6a5f448f88196f543efd71075d203321c23a40e7))
* **theme:** add support for classic
  template ([6713e28](https://github.com/lgdd/liferay-starter/commit/6713e2830962123f73da66e8d8b4461297e6de00))

### Bug Fixes

* docker images ([3f03e99](https://github.com/lgdd/liferay-starter/commit/3f03e99a7614d5e647f3879e6c3bdbb9510feed5))
* service builder gradle
  goals ([4cc8a3b](https://github.com/lgdd/liferay-starter/commit/4cc8a3b2e1fcf760ae0b01c8ff3dc5d08b3d91cd))
* **theme:** broken generation for
  7.4 ([2e7b854](https://github.com/lgdd/liferay-starter/commit/2e7b854d89a7c820ed00d6af80579541a0db3647))

## [2.5.3] - 2021-07-27

### Changed

- Update Liferay Portal 7.3 to GA8

## [2.5.2] - 2021-06-24

### Changed

- Update Liferay Portal 7.4 to GA2

## [2.5.1] - 2021-04-30

### Changed

- Update blade version to 4.0.9-snapshot

## [2.5.0] - 2021-04-30

### Added

- Support for classic themes

### Fixed

- Theme generation for 7.4 (temporarily generate 7.3 theme, waiting for official update)

## [2.4.0] - 2021-04-28

### Added

- Support for Portal 7.4-GA1

### Fixed

- Service builder command for Gradle (buildService)

## [2.2.1] - 2020-10-13

### Added

- Support for DXP 7.3-GA1 and Portal 7.3-GA6

### Changed

- Update blade to v4.0.5

## [2.2.0] - 2020-09-27

### Added

- REST Builder Template

### Changed

- Use Liferay Product Version to support DXP, Portal and Commerce
- Upgrade Quarkus to v1.8.1.Final
- Upgrade GraalVM to v22.2.0

## [2.1.3] - 2020-07-03

### Changed

- Dockerfiles location and build stage

### Fixed

- Package name in generated Java apps

## [2.1.2] - 2020-06-17

### Added

- Javadoc
- docs folder to .dockerignore

### Fixed

- Remove temp test methods
- Update default name when updating template
- Theme generation and deployment for Liferay <= 7.1

## [2.1.1] - 2020-06-12

### Changed

- Refacotring backend code.

### Fixed

- Build failing (Java) because the default incremental app name contains a number.

## [2.1.0] - 2020-06-09

### Fixed

- JavaScript apps and themes builds with Maven by adding opinionated pom.xml files.

## [2.0.1] - 2020-06-08

### Changed

- Upgrade websocket-extensions to v0.1.4

## [2.0.0] - 2020-06-07

### Added

- Support to generate Liferay apps from templates (Java, JS, Theme)
- Header subtitle
- SEO metadata description

### Changed

- Upgrade Quarkus to v1.4.1.Final
- Dockerfiles with multi-stage builds
- API design
- Header layout

## [1.2.0] - 2020-04-26

### Added

- Project group ID & version validation (backend)
- Feedback validation for project group ID & version (frontend)

### Changed

- Archive static class & methods to standard service

## [1.1.0] - 2020-04-24

### Added

- Windows command
- Auto-select platform for init command

### Removed

- Unused bootstrap.js

## [1.0.0] - 2020-04-22

### Added

- Build tool option
- Liferay version option
- Project informations option (group id, artifact id, version)
- One line command to initialize Liferay bundle for Linux/Mac users
- Visual feedback about workspace generation
- Light/Dark theme switch
- GitHub buttons
- Footer with license & author