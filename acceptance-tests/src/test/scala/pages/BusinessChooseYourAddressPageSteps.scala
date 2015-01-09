package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.BusinessChooseYourAddressPage
import pages.vrm_retention.BusinessChooseYourAddressPage._

class BusinessChooseYourAddressPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `proceed to next page` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
      BusinessChooseYourAddressPage.chooseAddress.value = "0"
      click on BusinessChooseYourAddressPage.select
    }
    this
  }
}
