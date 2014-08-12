package common

import helpers.webbrowser.TestGlobal
import helpers.{UnitSpec, WithApplication}
import play.api.test.FakeApplication
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import utils.helpers.AesEncryption
import utils.helpers.CookieEncryption
import utils.helpers.NoEncryption
import utils.helpers.NoHashGenerator
import utils.helpers.CookieNameHashGenerator
import utils.helpers.Sha1HashGenerator

final class EncryptedClientSideSessionSpec extends UnitSpec {
  "nameCookie" should {
    "return a new CookieName type consisting of the session secret key plus the cookie name that we can see in clear text when hashing is not used" in new WithApplication {
      val encryptedClientSideSession =
        new EncryptedClientSideSession("trackingId", SessionSecretKey)(noCookieFlags, noEncryption, noHashing)
      val encryptedCookieName = encryptedClientSideSession.nameCookie(CookieName)
      encryptedCookieName.value should equal(s"$SessionSecretKey$CookieName")
    }

    "return a deterministic hashed cookie name (the hash will always be the same value for the same inputs)" in new WithApplication {
      val encryptedClientSideSession = new EncryptedClientSideSession("trackingId", SessionSecretKey)(noCookieFlags, noEncryption, sha1Hashing)
      val encryptedCookieName1 = encryptedClientSideSession.nameCookie(CookieName)
      val encryptedCookieName2 = encryptedClientSideSession.nameCookie(CookieName)
      encryptedCookieName1.value should equal(encryptedCookieName2.value)
    }

    "return a cookie whose value is prefixed with the cookie name that we can see in clear text when hashing is not used" in new WithApplication {
      val encryptedClientSideSession =
        new EncryptedClientSideSession("trackingId", SessionSecretKey)(noCookieFlags, noEncryption, noHashing)
      val cookieNameType = encryptedClientSideSession.nameCookie(CookieName)
      val value = "value"
      val cookie = encryptedClientSideSession.newCookie(cookieNameType, value)
      cookie.value should equal(cookie.name + value)
    }

    "allow the client to extract the encrypted value from the cookie" in new WithApplication(app = fakeAppWithConfig) {
      implicit val aesEncryption =
        new AesEncryption with CookieEncryption
      val encryptedClientSideSession =
        new EncryptedClientSideSession("trackingId", SessionSecretKey)(noCookieFlags, aesEncryption, sha1Hashing)
      val cookieNameType = encryptedClientSideSession.nameCookie(CookieName)
      val value = "value"
      val cookie = encryptedClientSideSession.newCookie(cookieNameType, value)
      val valueFromCookie = encryptedClientSideSession.getCookieValue(cookie)
      valueFromCookie should equal(value)
    }
  }

  private final val SessionSecretKey = "sessionSecret"
  private final val CookieName = "cookieName"
  implicit val noCookieFlags = new NoCookieFlags
  implicit val noEncryption = new NoEncryption with CookieEncryption
  implicit val noHashing = new NoHashGenerator with CookieNameHashGenerator
  implicit val sha1Hashing = new Sha1HashGenerator with CookieNameHashGenerator

  private val fakeAppWithConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("application.secret256Bit" -> "MnPSvGpiEF5OJRG3xLAnsfmdMTLr6wpmJmZLv2RB9Vo="))
}