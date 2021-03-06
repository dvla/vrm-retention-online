package PersonalizedRegistration.runner

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/PersonalizedRegistration/VehiclesRegistration.feature"),
  glue = Array("PersonalizedRegistration.StepDefs")
)
class VehiclesRegistration
