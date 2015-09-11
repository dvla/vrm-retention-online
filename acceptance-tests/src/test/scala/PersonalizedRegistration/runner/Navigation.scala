package PersonalizedRegistration

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/PersonalizedRegistration/Navigation.feature"),
  glue = Array("PersonalizedRegistration.StepDefs"),
  tags = Array("~@WIP", "~@browser", "~@live-payment")
)
class Navigation
