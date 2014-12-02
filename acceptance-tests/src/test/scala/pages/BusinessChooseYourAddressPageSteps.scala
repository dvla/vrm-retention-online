package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.BusinessChooseYourAddressPage
import pages.vrm_retention.BusinessChooseYourAddressPage._

class BusinessChooseYourAddressPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `proceed to next page` = {
    page.url should equal(url)
    page.source contains title
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    this
  }
}
