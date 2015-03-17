package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SetupBusinessDetailsPage._
import pages.vrm_retention.SetupBusinessDetailsPage.traderContact
import pages.vrm_retention.SetupBusinessDetailsPage.traderEmail
import pages.vrm_retention.SetupBusinessDetailsPage.traderName
import pages.vrm_retention.SetupBusinessDetailsPage.traderPostcode

class SetupBusinessDetailsPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      `enter business details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains title
    }
    this
  }

  def `enter business details` = {
    traderName.value = "TRADER-NAME"
    traderContact.value = "Valtech"
    traderEmail.value = "business.example@test.com"
    traderPostcode.value = "QQ99QQ"
    click on lookup
    this
  }

  def `form is filled with the values I previously entered` = {
    traderContact.value should equal("VALTECH")
    traderEmail.value should equal("business.example@test.com")
    traderName.value should equal("TRADER-NAME")
    traderPostcode.value should equal("QQ99QQ")
    this
  }

  def `form is not filled` = {
    traderContact.value should equal("")
    traderEmail.value should equal("")
    traderName.value should equal("")
    traderPostcode.value should equal("")
    this
  }
}
