package edu.spbstu.BDD;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class CucumberRunnerTest {
    // runner for mvn test goal
    // use JetBrains plugin for running from IDE
}
