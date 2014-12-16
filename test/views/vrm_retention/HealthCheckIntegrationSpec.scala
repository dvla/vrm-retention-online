package views.vrm_retention

import helpers.UiSpec
import composition.TestHarness
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost, HttpPut, HttpRequestBase}
import org.apache.http.impl.client.HttpClients
import pages.ApplicationContext.applicationContext
import play.mvc.Http.Status
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

final class HealthCheckIntegrationSpec extends UiSpec with TestHarness {

  "Accessing the /healthcheck url" should {

    "return 200 for GET and POST" in new WebBrowserForFirefox {
      var httpResponse = execute(new HttpGet(WebDriverFactory.testUrl + s"$applicationContext/healthcheck"))
      try httpResponse.getStatusLine.getStatusCode should be(Status.OK)
      finally httpResponse.close()
      // TODO: the test below doesn't seem valid as there is no POST for this in the routes file.
      httpResponse = execute(new HttpPost(WebDriverFactory.testUrl + s"$applicationContext/healthcheck"))
      try httpResponse.getStatusLine.getStatusCode should be(Status.OK)
      finally httpResponse.close()
    }

    "return 404 for PUT etc." in new WebBrowserForFirefox {
      val httpResponse = execute(new HttpPut(WebDriverFactory.testUrl + s"$applicationContext/healthcheck"))
      try httpResponse.getStatusLine.getStatusCode should be(Status.NOT_FOUND)
      finally httpResponse.close()
    }
  }

  private def execute(method: HttpRequestBase): CloseableHttpResponse = {
    val httpClient = HttpClients.createDefault()
    httpClient.execute(method)
  }
}
