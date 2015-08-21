package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.booleanProp
import common.ConfigProperties.getOptionalProperty
import common.clientsidesession.AesEncryption
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieEncryption
import common.clientsidesession.CookieNameHashGenerator
import common.clientsidesession.EncryptedClientSideSessionFactory
import common.clientsidesession.Sha1HashGenerator

final class SessionFactoryBinding extends ScalaModule {

  def configure() = if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
    bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
    bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
    bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
  } else
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
}