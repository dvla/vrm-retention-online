package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmBusinessPage._

class ConfirmBusinessPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    currentUrl should equal(url)
    pageTitle should equal(title)
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
}
