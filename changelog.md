# Changelog
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
