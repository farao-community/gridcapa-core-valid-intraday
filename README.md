[![Actions Status](https://github.com/farao-community/gridcapa-core-valid-intraday/actions/workflows/ci-master.yml/badge.svg)](https://github.com/farao-community/gridcapa-core-valid-intraday/actions/workflows/ci-master.yml)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=farao-community_gridcapa-core-valid-id&metric=coverage)](https://sonarcloud.io/component_measures?id=farao-community_gridcapa-core-valid-id&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=farao-community_gridcapa-core-valid-id&metric=alert_status)](https://sonarcloud.io/dashboard?id=farao-community_gridcapa-core-valid-id)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/farao-community)
# gridcapa-core-valid-intraday
It's provide a full suite to perform CEP 70% intraday validation process on CORE zone 

## Functional overview
The Clean Energy Package CEP 70% project provides for increases in exchange capacity at interconnections in the CORE region.
The aim of this application is to validate these capacity levels by studying the effect of the increasing capacity on certain study-points of interest of the flow-based domain.

## Developer documentation

## Build application

Application is using Maven as base build framework. Application is simply built with following command.

```bash
mvn install
```

## Build docker image

For building Docker image of the application, start by building application.

```bash
mvn install
```

Then build docker image

```bash
docker build -t farao/gridcapa-core-valid-intraday .
```
