package common

import play.api.mvc.Cookie
import utils.helpers.{CookieEncryption, CookieNameHashGenerator}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags

class EncryptedClientSideSession(override val trackingId: String,
                                 val sessionSecretKey: String)
                                (implicit cookieFlags: CookieFlags,
                                 encryption: CookieEncryption,
                                 cookieNameHashGenerator: CookieNameHashGenerator) extends ClientSideSession {

  override def nameCookie(key: String): CookieName = CookieName(cookieNameHashGenerator.hash(sessionSecretKey + key))

  override def newCookie(name: CookieName, value: String): Cookie = {
    val nameCoupledToValue = name.value + value
    val cipherText = encryption.encrypt(nameCoupledToValue)
    cookieFlags.applyToCookie(
      Cookie(name = name.value, value = cipherText)
    )
  }

  override def getCookieValue(cookie: Cookie): String = {
    val cookieName = cookie.name
    val cipherText = cookie.value
    val valueCoupledToName = encryption.decrypt(cipherText)
    val (cookieNameFromPayload, value) = valueCoupledToName.splitAt(cookieName.length)
    assert(cookieName == cookieNameFromPayload, "The cookie name bytes from the payload must match the cookie name")
    value
  }

  override def equals(other: Any): Boolean = other match {
    case o: EncryptedClientSideSession if this eq o => true
    case o: EncryptedClientSideSession => this.sessionSecretKey == o.sessionSecretKey
    case _ => false
  }

  override def hashCode(): Int = this.sessionSecretKey.hashCode
}