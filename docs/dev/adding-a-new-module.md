# Adding a new module

Following is a checklist to follow when adding a new module to the repo.

- [ ] Create the module directory under `modules/`. If it make sense prefix the module name with `broker-` similar to
 `broker-common` and `broker-amqp`.
- [ ] Add the pom.xml file in `module/{module-name}`. Sample pom will look something similar to following.
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>io.ballerina.messaging</groupId>
        <artifactId>broker-parent</artifactId>
        <version><!-- The most recent snapshot version. E.g. 1.0.47-SNAPSHOT --></version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId><!-- module artifact id. E.g. broker-common --></artifactId>
    <name><!-- module name. E.g. Broker - Common --></name>

    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.findbugs</groupId>
            <artifactId>annotations</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <configuration>
                    <destFile>${basedir}/target/coverage-reports/jacoco-unit-fix.exec</destFile>
                </configuration>
                <executions>
                    <execution>
                        <id>jacoco-initialize</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
- [ ] Add the new module in the parent pom's module list in correct location depending on the dependent modules.
```xml
    <modules>
        <module>modules/broker-common</module>
        <module><!-- modules/new-module-name --></module>
        <module>modules/broker-core</module>
        <module>modules/broker-amqp</module>
        <module>modules/broker-coordination</module>
        <module>modules/launcher</module>
        <module>modules/integration</module>
        <module>modules/coverage-report</module>
    </modules>
```
- [ ] Add the new module as a dependency in parent pom.
- [ ] Add the new module as a dependency in `modules/coverage-report/pom.xml`.
- [ ] Add the new module to package dependency list in `modules/launcher/src/main/assembly/assembly.xml'