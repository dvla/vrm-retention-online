package PersonalizedRegistration

import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@Options(
  features = Array("acceptance-tests/src/test/resources/PersonalizedRegistration/"),
  glue = Array("PersonalizedRegistration"),
  format = Array("pretty", "html:target/cucumber-report"),
  tags = Array("@HappyPath,@UnHappyPath-InProgress,@UnHappyPath")
)
class RunCucumber {
}
