package pages

import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_retention.ConfirmPage.{confirm, url, `don't supply keeper email`, `supply keeper email`}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` = {
    `is displayed`.
      `customer does not want an email`.
      `confirm the details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `confirm the details` = {
    click on confirm
    this
  }

  def `customer does not want an email` = {
    click on `don't supply keeper email`
    this
  }

  def `form is not filled`() = {
    `supply keeper email`.isSelected should equal(false)
    `don't supply keeper email`.isSelected should equal(false)
    this
  }
}
