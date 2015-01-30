package composition

import _root_.audit1.{AuditLocalServiceImpl, AuditService}
import _root_.webserviceclients.audit2.{AuditMicroService, AuditMicroServiceImpl}
import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import email.{EmailService, EmailServiceImpl}
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{AesEncryption, CookieEncryption, CookieNameHashGenerator, Sha1HashGenerator, _}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl}

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
class DevModule extends ScalaModule {

  def configure() {
    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[AuditService].to[AuditLocalServiceImpl].asEagerSingleton()
    bind[AuditMicroService].to[AuditMicroServiceImpl].asEagerSingleton()
  }
}