package PersonalizedRegistration.StepDefs

import cucumber.api.java.en.{Given, Then}
import java.security.cert.X509Certificate
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts
import scala.io.Source.fromInputStream
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebDriverFactory}

final class VersionStepDefs(implicit webDriver: WebBrowserDriver) extends helpers.AcceptanceTestHelper {
  private var versionString: Option[String] = None

  @Given("^the user is on the version page$")
  def the_user_is_on_the_version_page() {
    val sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
      override def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
    }).build()

    val httpClient = HttpClientBuilder.create.setSSLContext(sslContext).build()
    val post = new HttpGet(WebDriverFactory.testUrl + "version")
    val httpResponse = httpClient.execute(post)
    versionString = Option(fromInputStream(httpResponse.getEntity.getContent).mkString)
    httpResponse.close()
  }

  @Then("^The user should be able to see version and runtime information for the webapp$")
  def the_user_should_be_able_to_see_version_and_runtime_information_for_the_webapp() = {
    checkData("Name")
    checkData("Version")
    checkData("Build on")
    checkData("Runtime OS")
  }

  @Then("^The user should be able to see version and runtime information for the microservices$")
  def the_user_should_be_able_to_see_version_and_runtime_information_for_the_microservices() {
    checkData("audit")
    checkData("email-service")
    checkData("os-address-lookup")
    checkData("payment-solve")
    checkData("vehicle-and-keeper-lookup")
    checkData("vrm-retention-eligibility")
    checkData("vrm-retention-retain")
  }

  private def checkData(data: String ) =
    versionString.fold(fail("The version string should be populated")) { vs => vs should include(data) }
}
