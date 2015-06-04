package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmBusinessPage.{confirm, url}

class ConfirmBusinessPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`
//    click on rememberDetails
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  // TODO: ian move this to previous step
//  def `store details` = {
//    click on ConfirmBusinessPage.rememberDetails
//    this
//  }

  def `proceed to confirm` = {
    click on ConfirmBusinessPage.confirm
    this
  }
// TODO: ian delete this
  def `form is filled with the values I previously entered`() = {
//    rememberDetails.isSelected should equal(true)
    this
  }

  def `form is not filled`() = {
//    rememberDetails.isSelected should equal(false)
    this
  }

  def `exit the service` = {
    click on ConfirmBusinessPage.exit
    this
  }
}
