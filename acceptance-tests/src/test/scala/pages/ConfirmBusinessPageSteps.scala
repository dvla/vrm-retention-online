package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.ConfirmBusinessPage.rememberDetails
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmBusinessPage._

class ConfirmBusinessPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`
    click on rememberDetails
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
    }
    this
  }

  def `store details` = {
    click on ConfirmBusinessPage.rememberDetails
    this
  }

  def `proceed to confirm` = {
    click on ConfirmBusinessPage.confirm
    this
  }

  def `form is filled with the values I previously entered`() = {
    rememberDetails.isSelected should equal(true)
    this
  }

  def `form is not filled`() = {
    rememberDetails.isSelected should equal(false)
    this
  }
}
