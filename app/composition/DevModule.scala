package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import email.{EmailService, EmailServiceImpl}
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{AesEncryption, CookieEncryption, CookieNameHashGenerator, Sha1HashGenerator, _}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{AddressLookupServiceImpl, WebServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import utils.helpers.{Config2Impl, Config2, RetentionCookieFlags}
import webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl, PaymentSolveWebService, PaymentSolveWebServiceImpl}
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService, VehicleAndKeeperLookupWebServiceImpl}
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService, VRMRetentionEligibilityWebServiceImpl}
import webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService, VRMRetentionRetainWebServiceImpl}
import webserviceclients.audit2

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
    bind[Config2].to[Config2Impl].asEagerSingleton()

    bind[AddressLookupService].to[AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupWebService].to[VehicleAndKeeperLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityWebService].to[VRMRetentionEligibilityWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainWebService].to[VRMRetentionRetainWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
    bind[PaymentSolveWebService].to[PaymentSolveWebServiceImpl].asEagerSingleton()
    bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()
    bind[DateService].to[DateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[RetentionCookieFlags].asEagerSingleton()

    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[bruteforceprevention.WebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[audit1.AuditService].to[audit1.AuditLocalServiceImpl].asEagerSingleton()
    bind[RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
    bind[audit2.AuditMicroService].to[audit2.AuditMicroServiceImpl].asEagerSingleton()
    bind[audit2.AuditService].to[audit2.AuditServiceImpl].asEagerSingleton()
  }
}