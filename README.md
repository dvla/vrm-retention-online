DVLA Vehicles Online
====================

`vrm-retention-online` is the Web frontend for retaining a personalised registration number online.

Architectural overview
----------------------

### Presentation layer

This project encapsulates the presentation layer of the application.

The codebase is predominantly [Scala][scala] and is implemented against [Play][play-framework]: a 'full stack' Web
framework for the JVM.

### Microservices

Most complex business decisions are deferred to a network of [RESTful][rest] microservices. These are maintained through
separate projects:

-   `os-address-lookup`
-   `vehicles-lookup`
-   `vrm-retention-eligibility`
-   `vrm-retention-retain`

These services are mocked for automated testing, but must be running locally for manual testing/development of dependant
components within the presentation layer.

Development environment
-----------------------

1.  Each project must be checked-out to the same directory using the following names:

    -   `vrm-retention-online`
    -   `vrm-retention-eligibility`
    -   `vrm-retention-retain`
    -   `vehicles-and-keeper-lookup`
    -   `<<the appropriate secrets repo>>'
    -   `os-address-lookup`
    -   `legacy-stub-services`
    -   `vehicles-gatling`
    -   `payment-solve`  		

2.  JDK 1.7 must be installed

3.  Install SASS. The [current documentation][install-sass] suggests:

        sudo gem install sass

4.  Install SBT.  The [current documentation][install-sbt] suggests:

        brew install sbt

5.  Increase 'permanent generation space' requirements for SBT.

    Create the file `~/.sbtconfig` with the following content:

        SBT_OPTS="$SBT_OPTS -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:PermSize=256M -XX:MaxPermSize=2048M"

6.  Decrypt secret keys:

        cd <<the appropriate secrets repo>>
        ./setup XYZ

    *where `XYZ` is an offline secret key obtained through a trusted team member*

Running the application
-----------------------

1.  Run the `vrm-retention-online` application:

        cd vrm-retention-online
        sbt run

2.  Open in Web browser:

        http://localhost:9000/

3.  Repeat *step 1* for each required microservice (if any).

### Running with production logging

To emulate production-level logging:

1.  Ensure `syslog` is configured. Details have been provided for [configuring `syslog` on OSX][syslog-osx].

2.  Run the `vrm-retention-online` application:

        cd vrm-retention-online
        ./startWithLog.sh
        
3.  Open in Web browser:

        http://localhost:9000/

Testing the application
-----------------------

Unit and Selenium tests written using Scalatest can be run using `sbt test`

Acceptance tests written using Cucumber can be run using `sbt acceptance-tests/test-only`

Session encryption
------------------

Please refer to the [session encryption][session-encryption] document for details on the encryption algorithm used.

[install-sass]: http://sass-lang.com/install "Install SASS"
[install-sbt]: http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt "Install SBT"
[rest]: https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm "REST"
[play-framework]: http://www.playframework.com/ "Play Framework"
[scala]: http://www.scala-lang.org/ "Scala Language"
[syslog-osx]: syslog-osx.md "Configuring syslog on OSX"
[session-encryption]: encrypted-session-state.md "Session Encryption"
