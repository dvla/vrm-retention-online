package views.vrm_retention

import composition.TestHarness
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_retention.TermsAndConditionsPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class TermsAndConditionsUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to TermsAndConditionsPage

      currentUrl should equal(TermsAndConditionsPage.url)
    }
  }
}
