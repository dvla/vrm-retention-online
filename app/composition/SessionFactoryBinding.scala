package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.booleanProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.AesEncryption
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieEncryption
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieNameHashGenerator
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.EncryptedClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.Sha1HashGenerator

final class SessionFactoryBinding extends ScalaModule {

  def configure() = if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
    bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
    bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
    bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
  } else
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
}