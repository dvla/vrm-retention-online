package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.BeforeYouStartPage

class BeforeYouStartPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `go to BeforeYouStart page` = {
    go to BeforeYouStartPage
    this
  }

  def `click 'Start now' button` = {
    click on BeforeYouStartPage.startNow
    this
  }

  def `is displayed` = {
    page.url should equal(BeforeYouStartPage.url)
    this
  }
}
