package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.BeforeYouStartPage
import org.scalatest.selenium.WebBrowser._

class BeforeYouStartPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `go to BeforeYouStart page` = {
    go to BeforeYouStartPage
    this
  }

  def `click 'Start now' button` = {
    click on BeforeYouStartPage.startNow
    this
  }

  def `is displayed` = {
    currentUrl should equal(BeforeYouStartPage.url)
    this
  }
}
