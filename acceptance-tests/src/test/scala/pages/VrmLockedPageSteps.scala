package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VrmLockedPage._

class VrmLockedPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }
}
