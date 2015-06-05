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
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def `proceed to confirm` = {
    click on ConfirmBusinessPage.confirm
    this
  }

  def `exit the service` = {
    click on ConfirmBusinessPage.exit
    this
  }
}
