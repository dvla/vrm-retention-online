package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.currentUrl
import pages.vrm_retention.SuccessPage.url

class SuccessPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }
}
