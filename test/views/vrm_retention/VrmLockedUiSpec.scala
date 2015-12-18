package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageSource}
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.VrmLockedPage
import pages.vrm_retention.VrmLockedPage.exit

class VrmLockedUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      currentUrl should equal(VrmLockedPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "page should not contain contact information" should {
      "contains contact information" taggedAs UiTag in  new WebBrowserForSelenium  {
        go to BeforeYouStartPage
        cacheSetup
        go to VrmLockedPage
        pageSource should include("Telephone")
      }
    }

    "contain the time of locking" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }

    "contain the time of locking when JavaScript is disabled" taggedAs UiTag in new WebBrowserWithJsDisabled {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }
  }

  "exit button" should {
    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage
      click on exit
      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperDetailsModel().
      vehicleAndKeeperLookupFormModel()
}