package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.PaymentFailurePage._

class PaymentFailurePageSteps(timeout: PatienceConfig)(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }
    this
  }
}
