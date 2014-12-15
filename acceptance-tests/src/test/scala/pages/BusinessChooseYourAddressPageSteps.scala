package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.BusinessChooseYourAddressPage
import pages.vrm_retention.BusinessChooseYourAddressPage._

class BusinessChooseYourAddressPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `proceed to next page` = {
    currentUrl should equal(url)
    pageTitle should equal(title)
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    org.scalatest.selenium.WebBrowser.click on BusinessChooseYourAddressPage.select
    this
  }
}
