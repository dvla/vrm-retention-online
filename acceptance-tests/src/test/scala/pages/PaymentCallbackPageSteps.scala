package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{currentUrl, pageSource}
import pages.vrm_retention.PaymentCallbackPage.{title, url}

class PaymentCallbackPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }
    this
  }
}
