package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.vrm_retention.BeforeYouStartPage.startNow
import pages.vrm_retention.{BeforeYouStartPart2Page, VehicleLookupPage}

final class BeforeYouStartPart2IntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPart2Page

      page.url should equal(BeforeYouStartPart2Page.url)
    }
  }

  "startNow button" should {

    "go to next page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPart2Page

      click on startNow

      page.url should equal(VehicleLookupPage.url)
    }
  }
}