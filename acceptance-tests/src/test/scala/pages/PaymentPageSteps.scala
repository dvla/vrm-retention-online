package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.PaymentPage._

class PaymentPageSteps(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
    }
    this
  }

  def enter(CardName: String, CardNumber: String, SecurityCode: String) = {
    cardName.value = CardName
    cardNumber.value = CardNumber
    securityCode.value = SecurityCode
    this
  }

  def `paynow` = {
    org.scalatest.selenium.WebBrowser.click on payNow

    //DO NOT REMOVE COMMENTED CODE
    //     maximize
    //     theLogicaGroupLogo
    //    printf("The URL" + pageTitle)
    //    maximize
    //    //theLogicaGroupLogo
    //    printf("The URL" + pageTitle)

    //org.scalatest.selenium.WebBrowser.implicitlyWait(Span(2,Minutes))
    this
  }

  def `expiryDate` = {
    org.scalatest.selenium.WebBrowser.singleSel(expiryMonth()).value = "08"
    org.scalatest.selenium.WebBrowser.singleSel(expiryYear()).value = "2016"
    this
  }

  def `Message is displayed` = {
    currentUrl should equal(url)
    this
  }
}
