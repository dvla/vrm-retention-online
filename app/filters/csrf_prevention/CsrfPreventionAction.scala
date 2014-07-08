package filters.csrf_prevention

import app.ConfigProperties.getProperty
import common.ClientSideSessionFactory
import play.api.libs.Crypto
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Iteratee, Enumerator, Traversable}
import play.api.mvc.BodyParsers.parse.tolerantFormUrlEncoded
import play.api.mvc.{EssentialAction, Headers, RequestHeader}
import utils.helpers.AesEncryption
import common.CookieImplicits.RichCookies
import play.api.http.HeaderNames.REFERER
import scala.util.Try

final case class CsrfPreventionException(nestedException: Throwable) extends Exception(nestedException: Throwable)

/**
 * This class is based upon the Play's v2.2 CSRF protection. It has been stripped of code not relevant to this project, and
 * project specific exception handling and aesEncryption has been added. The unmarshalling and onward streaming in the
 * checkBody method is as Play intended it apart from the token comparison.
 *
 * https://www.playframework.com/documentation/2.2.x/ScalaCsrf
 *
 */
class CsrfPreventionAction(next: EssentialAction)
                          (implicit clientSideSessionFactory: ClientSideSessionFactory) extends EssentialAction {
  import CsrfPreventionAction._

  def apply(requestHeader: RequestHeader) = {

    // check if csrf prevention is switched on
    if (preventionEnabled) {
      if (requestHeader.method == "POST") {
        val headerToken = buildTokenWithReferer(
          requestHeader.cookies.trackingId,
          requestHeader.headers
        )

        if (requestHeader.contentType.get == "application/x-www-form-urlencoded")
          checkBody(requestHeader, headerToken, next)
        else
          throw new CsrfPreventionException(new Throwable("No CSRF token found in body"))

      } else if (requestHeader.method == "GET" && requestHeader.accepts("text/html")) {

        // No token in header and we have to create one if not found, so create a new token
        val newToken = buildTokenWithUri(requestHeader.cookies.trackingId, requestHeader.uri)
        val newEncryptedToken = aesEncryption.encrypt(newToken)
        val newSignedEncryptedToken = Crypto.signToken(newEncryptedToken)

        val requestWithNewToken = requestHeader.copy(
          tags = requestHeader.tags + (TokenName -> newSignedEncryptedToken)
        )

        // Once done, add it to the result
        next(requestWithNewToken)

      } else next(requestHeader)
    } else next(requestHeader)
  }

  private def checkBody(request: RequestHeader, headerToken: String, next: EssentialAction) = {

    val firstPartOfBody: Iteratee[Array[Byte], Array[Byte]] =
      Traversable.take[Array[Byte]](102400L.asInstanceOf[Int]) &>> Iteratee.consume[Array[Byte]]()

    firstPartOfBody.flatMap { bytes: Array[Byte] =>
      val parsedBody = Enumerator(bytes) |>>> tolerantFormUrlEncoded(request)

      Iteratee.flatten(parsedBody.map { parseResult =>
        val validToken = parseResult.fold(
          simpleResult => false, // valid token not found
          body => (for { // valid token found
            values <- identity(body).get(TokenName)
            token <- values.headOption
          } yield {
            val decryptedExtractedSignedToken = aesEncryption.decrypt(Crypto.extractSignedToken(token).getOrElse(
              throw new CsrfPreventionException(new Throwable("Invalid token found in form body"))))
            //TODO name the tuple parts accordingly instead of referencing it by number
            val splitDecryptedExtractedSignedToken = splitToken(decryptedExtractedSignedToken)
            val splitTokenFromHeader = splitToken(headerToken)
            (splitDecryptedExtractedSignedToken._1 == splitTokenFromHeader._1) &&
              splitTokenFromHeader._2.contains(splitDecryptedExtractedSignedToken._2)
          }).getOrElse(false)
        )

        if (validToken)
          Iteratee.flatten(Enumerator(bytes) |>> next(request))
        else
          throw new CsrfPreventionException(new Throwable("Invalid token found in form body"))
      })
    }
  }
}

object CsrfPreventionAction {
  final val TokenName = "csrf_prevention_token"
  private final val Delimiter = "-"
  lazy val preventionEnabled = getProperty("csrf.prevention", default = true)
  private val aesEncryption = new AesEncryption()

  case class CsrfPreventionToken(value: String)

  // TODO : Trap the missing token exception differently?
  implicit def getToken(implicit request: RequestHeader,
                        clientSideSessionFactory: ClientSideSessionFactory): CsrfPreventionToken =
    Try {
      CsrfPreventionToken(
        Crypto.signToken(
          aesEncryption.encrypt(
            buildTokenWithUri(request.cookies.trackingId, request.uri)
          )
        )
      )
    }.getOrElse(throw new CsrfPreventionException(new Throwable("No CSRF token found")))

  private def buildTokenWithReferer(trackingId: String, requestHeaders: Headers) = {
    trackingId + Delimiter + requestHeaders.get(REFERER)
  }

  private def buildTokenWithUri(trackingId: String, uri: String) = {
    trackingId + Delimiter + uri
  }

  private def splitToken(token: String): (String, String) = {
    (token.split(Delimiter)(0), token.drop(token.indexOf(Delimiter) + 1))
  }
}