package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, pageSource, singleSel, submit}
import pages.vrm_retention.PaymentPage
import pages.vrm_retention.PaymentPage.{expiryMonth, expiryYear, noJavaScriptContinueButton, payNow, submitButton, url}

class PaymentPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

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
    this
  }

  def `expiryDate` = {
    singleSel(expiryMonth()).value = "08"
    singleSel(expiryYear()).value = org.joda.time.LocalDate.now.plusYears(3).getYear.toString
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

  def `no javascript continue` = {
    eventually {
      pageSource should include("please click the Continue button below.")
      noJavaScriptContinueButton.underlying.submit()
    }
    this
  }

  def `no javascript submit` = {
    eventually {
      pageSource should include("please click the Submit button below.")
      submitButton.underlying.submit()
    }
    this
  }
}
