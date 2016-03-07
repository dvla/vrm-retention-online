package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{currentUrl, pageSource}
import pages.vrm_retention.PaymentFailurePage.{title, url}

class PaymentFailurePageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }
    this
  }
}
