package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.Payment.AcsPassword
import views.vrm_retention.Payment.CancelId
import views.vrm_retention.Payment.CardNumber
import views.vrm_retention.Payment.CardSecurityCode
import views.vrm_retention.Payment.CardholderName
import views.vrm_retention.Payment.ExpiryMonth
import views.vrm_retention.Payment.ExpiryYear
import views.vrm_retention.Payment.IFrame
import views.vrm_retention.Payment.NoJavaScriptContinueButton
import views.vrm_retention.Payment.PayNow

object PaymentPage extends Page {

  def address = s"$applicationContext/payment/begin"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment"

  def cancel(implicit driver: WebDriver) = find(id(CancelId)).get

  def cardholderName(implicit driver: WebDriver) = {
    driver.switchTo().frame(driver.findElement(By.cssSelector(IFrame)))
    textField(id(CardholderName))
  }

  def cardNumber(implicit driver: WebDriver) = textField(id(CardNumber))

  def cardSecurityCode(implicit driver: WebDriver) = textField(id(CardSecurityCode))

  def payNow(implicit driver: WebDriver) = find(id(PayNow)).get

  def maximize(implicit driver: WebDriver) = driver.manage().window().maximize()

  def theLogicaGroupLogo(implicit driver: WebDriver) = driver.findElement(By.xpath("//*[@id=\"CompanyLogo\"]")).click()

  def expiryMonth() = ExpiryMonth

  def expiryYear() = ExpiryYear

  def acsPassword(implicit driver: WebDriver) = pwdField(id(AcsPassword))

  def noJavaScriptContinueButton(implicit driver: WebDriver) = find(id(NoJavaScriptContinueButton)).get
}
