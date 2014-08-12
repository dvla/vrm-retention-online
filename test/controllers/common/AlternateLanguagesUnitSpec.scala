package controllers.common

import controllers.common.AlternateLanguages.withLanguage
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.{UnitSpec, WithApplication}
import mappings.common.AlternateLanguages.{CyId, EnId}
import pages.vrm_retention.BeforeYouStartPage
import play.api.Play
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, REFERER, SEE_OTHER}

final class AlternateLanguagesUnitSpec extends UnitSpec {
  "withLanguageCy" should {
    "redirect back to the same page" in new WithApplication {
      val result = withLanguage(CyId)(request)
      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER) // Redirect...
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address)) // ... back to the same page.
      }
    }

    "writes language cookie set to 'cy'" in new WithApplication {
      val result = withLanguage(CyId)(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == Play.langCookieName) match {
          case Some(cookie) => cookie.value should equal("cy")
          case None => fail("langCookieName not found")
        }
      }
    }
  }

  "withLanguageEn" should {
    "redirect back to the same page" in new WithApplication {
      val result = withLanguage(EnId)(request)
      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER) // Redirect...
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address)) // ... back to the same page.
      }
    }

    "writes language cookie set to 'en'" in new WithApplication {
      val result = withLanguage(EnId)(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == Play.langCookieName) match {
          case Some(cookie) => cookie.value should equal("en")
          case None => fail("langCookieName not found")
        }
      }
    }
  }

  private val request = FakeRequest().withHeaders(REFERER -> BeforeYouStartPage.address)
}