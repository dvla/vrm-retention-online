package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.BeforeYouStartPage

class BeforeYouStartPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `go to BeforeYouStart page` = {
    go to BeforeYouStartPage
    this
  }

  def `click 'Start now' button` = {
    click on BeforeYouStartPage.startNow
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should include(BeforeYouStartPage.address)
    }
    this
  }
}
