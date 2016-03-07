package pages

import org.scalatest.selenium.WebBrowser.currentUrl
import pages.vrm_retention.PaymentPreventBackPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentPreventBack_PageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }
}
