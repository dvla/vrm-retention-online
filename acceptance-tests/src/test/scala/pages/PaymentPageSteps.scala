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

  def enter(CardName:String, CardNumber:String, SecurityCode :String) = {
    cardName.value = CardName
    cardNumber.value = CardNumber
    securityCode.value = SecurityCode
    this
  }

  def `paynow` = {
    org.scalatest.selenium.WebBrowser.click on payNow
    printf("The URL"+page.title)
    maximize
    theLogicaGroupLogo
    printf("The URL"+page.title)
    //org.scalatest.selenium.WebBrowser.implicitlyWait(Span(2,Minutes))

  }

  def `expiryDate` = {
    org.scalatest.selenium.WebBrowser.singleSel(expiryMonth()).value="08"
    org.scalatest.selenium.WebBrowser.singleSel(expiryYear()).value="2016"
  }

  def `Message is displayed` = {
    page.url should equal(url)
  }
}
