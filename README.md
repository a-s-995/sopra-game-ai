# AI Project

## Changing the build configuration
This repository contains a gradle build configuration which you will use for the develeopment of your sopra project.
You are not allowed to change the files that are part of the build configuration. This includes the files contained in the `config` directory and the main build script called `build.gradle`. The `Main.java` file can be changed but the
 package and name must be left intact and the `main()` method must be the entry point of your server implementation.

## Building your project
To build your project you first need to install the build system `gradle`. Further information can be found here: https://gradle.org/gradle-download/.
After installation the project build can be started using the command
```
gradle build
```
This will gather all dependencies, compile the projects source code, execute the test cases, perform analysis jobs such as PMD and findbugs and finally bundle all project resources. The resulting `.jar` file is located in `build/libs`.

## Project reports
The build configuration contains several analysis tools. These tools generate reports in `html` format, which can be found in `build/reports/$reportname`. The following reports are currently enabled:

* pmd: Analysis of code quality
* findbugs: Detection for commons sources of software faults
* testResults: Detailed report for passing and failing test cases
* jacoco: Coverage data of the executed test cases
