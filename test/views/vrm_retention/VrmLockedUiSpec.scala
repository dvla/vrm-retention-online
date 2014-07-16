package views.vrm_retention

import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import mappings.vrm_retention.RelatedCacheKeys
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.vrm_retention.VrmLockedPage.exit
import pages.vrm_retention.{BeforeYouStartPage, VrmLockedPage}

final class VrmLockedUiSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      page.url should equal(VrmLockedPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      page.url should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.FullSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel()
}