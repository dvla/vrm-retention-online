package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.PaymentPage
import pages.vrm_retention.PaymentPage
import pages.vrm_retention.PaymentPage._

class PaymentPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      enter(cardholderName = "test", cardNumber = "4444333322221111", cardSecurityCode = "123").
      `expiryDate`.
      `paynow`.
      `enter password`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
    }
    this
  }

  def enter(cardholderName: String, cardNumber: String, cardSecurityCode: String) = {
    PaymentPage.cardholderName.value = cardholderName
    PaymentPage.cardNumber.value = cardNumber
    PaymentPage.cardSecurityCode.value = cardSecurityCode
    this
  }

  def `paynow` = {
    click on payNow

    //DO NOT REMOVE COMMENTED CODE
    //     maximize
    //     theLogicaGroupLogo
    //    printf("The URL" + pageTitle)
    //    maximize
    //    //theLogicaGroupLogo
    //    printf("The URL" + pageTitle)

    //implicitlyWait(Span(2,Minutes))
    this
  }

  def `expiryDate` = {
    singleSel(expiryMonth()).value = "08"
    singleSel(expiryYear()).value = "2016"
    this
  }

  def `Message is displayed` = {
    currentUrl should equal(url)
    this
  }

  def `enter password` = {
    eventually {
      pageSource should include("Please enter your password")
      PaymentPage.acsPassword.value = "password"
      submit()
    }
    this
  }
}
