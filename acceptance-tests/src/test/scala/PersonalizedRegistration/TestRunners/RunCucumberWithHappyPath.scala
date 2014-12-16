package PersonalizedRegistration.TestRunners

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/PersonalizedRegistration/"),
  glue = Array("PersonalizedRegistration"),
  plugin = Array("pretty","html:target/cucumber-report"),
  tags = Array("@HappyPath,~@UnHappyPath")
)
class RunCucumberWithHappyPath
