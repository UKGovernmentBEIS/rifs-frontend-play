
# RIFS Frontend Play

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2adc6d84a6b54a5c8cbae30b36fce922)](https://www.codacy.com/app/doug/rifs-frontend-play?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=UKGovernmentBEIS/rifs-frontend-play&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/UKGovernmentBEIS/rifs-frontend-play.svg?style=svg)](https://circleci.com/gh/UKGovernmentBEIS/rifs-frontend-play)

This is the frontend to the [RIFS business](https://github.com/UKGovernmentBEIS/rifs-business) service.

## Nomenclature

- **RIFS**: Research and Innovation Funding Service

## Dependencies

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs 
a [JRE to](http://www.oracle.com/technetwork/java/javase/overview/index.html) run.  You will need the 
[Scala Build Tool](http://www.scala-sbt.org/) to run the app locally.

* [JRE](http://www.oracle.com/technetwork/java/javase/overview/index.html)
* [Scala Build Tool](http://www.scala-sbt.org/)
* [RIFS business](https://github.com/UKGovernmentBEIS/rifs-business) service

### Running the application

You can run the application using the [Scala Build Tool](http://www.scala-sbt.org/):

```
sbt compile run
```

The application can be viewed on `http://localhost:9000`

### Running the test suite

```
sbt test
```

####Acceptance Tests

NOTE: Cucumber/acceptance tests are available in a separate project at: [rifs-test](https://github.com/UKGovernmentBEIS/rifs-test) 

### License

[MIT License](LICENSE)