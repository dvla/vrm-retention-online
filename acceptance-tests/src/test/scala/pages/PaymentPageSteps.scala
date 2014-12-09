package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.PaymentPage._

class PaymentPageSteps (implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers{

  def `is displayed` = {
    page.url should equal(url)
    page.title should equal(title)
    this
  }

  def enter(cardName:String, cardNumber:String, securityCode :String) = {
//    CardName.value = cardName
//    CardNumber.value = cardNumber
//    SecurityCode.value = securityCode
    this
  }
}
