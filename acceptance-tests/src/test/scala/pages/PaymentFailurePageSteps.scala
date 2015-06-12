package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{currentUrl, pageSource}
import pages.vrm_retention.PaymentFailurePage.{title, url}

class PaymentFailurePageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }(timeout)
    this
  }
}
