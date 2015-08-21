package views.vrm_retention

import composition.TestHarness
import controllers.routes.CookiePolicy
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageSource, pageTitle}
import pages.common.AlternateLanguages.{isCymraegDisplayed, isEnglishDisplayed}
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.BeforeYouStartPage.footerItem
import pages.vrm_retention.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.CyId
import uk.gov.dvla.vehicles.presentation.common.controllers.routes.AlternateLanguages

class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      currentUrl should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and " +
      "comes back)" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessDetails().
          vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.RetainSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }

    "display the global cookie message when cookie 'seen_cookie_message' " +
      "does not exist" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageSource should include("Find out more about cookies")
    }

    "display a link to the cookie policy" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      footerItem(index = 0).findElement(By.tagName("a")).getAttribute("href") should
        include(CookiePolicy.present().toString())
    }

    "display a Cymraeg link" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      footerItem(index = 1).findElement(By.tagName("a")).getAttribute("href") should
        include(AlternateLanguages.withLanguage(CyId).toString())
    }

    "change language to welsh when Cymraeg link clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on footerItem(index = 1).findElement(By.tagName("a"))
      pageTitle should equal(BeforeYouStartPage.titleCy)
    }
  }

  "display the 'Cymraeg' language button and not the 'English' language button when " +
    "the play language cookie has value 'en'" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage // By default will load in English.
    CookieFactoryForUISpecs.withLanguageEn()
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(true)
    isEnglishDisplayed should equal(false)
  }

  "display the 'English' language button and not the 'Cymraeg' language button when " +
    "the play language cookie has value 'cy'" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage // By default will load in English.
    CookieFactoryForUISpecs.withLanguageCy()
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(false)
    isEnglishDisplayed should equal(true)
    pageTitle should equal(BeforeYouStartPage.titleCy)
  }

  "display the 'Cymraeg' language button and not the 'English' language button and mailto when " +
    "the play language cookie does not exist " +
    "(assumption that the browser default language is English)" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(true)
    isEnglishDisplayed should equal(false)
  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on BeforeYouStartPage.startNow
      currentUrl should equal(VehicleLookupPage.url)
    }
  }
}