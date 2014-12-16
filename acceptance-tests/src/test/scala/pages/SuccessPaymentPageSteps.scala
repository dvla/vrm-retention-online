package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SuccessPaymentPage._

class SuccessPaymentPageSteps(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    waiting
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }
    this
  }
}