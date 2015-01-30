package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._

final class SessionFactoryBinding extends ScalaModule {

  def configure() = if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
    bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
    bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
    bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
  } else
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
}