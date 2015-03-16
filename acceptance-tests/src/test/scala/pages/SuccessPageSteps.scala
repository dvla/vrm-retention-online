package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SuccessPage.url
import pages.vrm_retention.SuccessPage.waiting

class SuccessPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    waiting
    eventually {
      currentUrl should equal(url)
    }
    this
  }
}
