package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.PaymentCallbackPage._

class PaymentCallbackPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    currentUrl should equal(url)
    pageSource contains title
    this
  }
}
