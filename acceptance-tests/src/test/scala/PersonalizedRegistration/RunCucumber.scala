package PersonalizedRegistration

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/PersonalizedRegistration/"),
  glue = Array("PersonalizedRegistration.StepDefs"),
  plugin = Array("pretty", "html:target/cucumber-report"),
  tags = Array("~@WIP")
)
class RunCucumber

