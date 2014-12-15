package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.Payment._

object PaymentPage extends Page {

  def address = s"$applicationContext/payment/begin"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment"

  def cancel(implicit driver: WebDriver) = find(id(CancelId)).get

  def cardName(implicit driver: WebDriver) = {
    driver.switchTo().frame(driver.findElement(By.cssSelector(IFrame)))
    textField(org.scalatest.selenium.WebBrowser.id(CardName))
  }

  def cardNumber(implicit driver: WebDriver) = textField(org.scalatest.selenium.WebBrowser.id(CardNumber))

  def securityCode(implicit driver: WebDriver) = textField(org.scalatest.selenium.WebBrowser.id(SecurityCode))

  def payNow(implicit driver: WebDriver) = find(id(PayNow)).get

  def maximize(implicit driver: WebDriver) = driver.manage().window().maximize()

  def theLogicaGroupLogo(implicit driver: WebDriver) = driver.findElement(By.id("CompanyLogo")).click()

  def expiryMonth() = ExpiryMonth

  def expiryYear() = ExpiryYear
}
