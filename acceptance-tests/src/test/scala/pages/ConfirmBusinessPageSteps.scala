package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmBusinessPage.{confirm, url}

class ConfirmBusinessPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` = {
    `is displayed`
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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
