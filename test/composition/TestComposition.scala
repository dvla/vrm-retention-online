package composition

import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import email.{EmailServiceImpl, EmailService}
import pdf.{PdfServiceImpl, PdfService}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import uk.gov.dvla.vehicles.presentation.common.services.{DateServiceImpl, DateService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{WebServiceImpl, AddressLookupServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceImpl, BruteForcePreventionService, BruteForcePreventionWebService}
import utils.helpers._
import webserviceclients.paymentsolve.{PaymentSolveServiceImpl, PaymentSolveService, PaymentSolveWebServiceImpl, PaymentSolveWebService}
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupService, VehicleAndKeeperLookupWebServiceImpl, VehicleAndKeeperLookupWebService}
import webserviceclients.vrmretentioneligibility._
import webserviceclients.vrmretentionretain.{VRMRetentionRetainServiceImpl, VRMRetentionRetainService, VRMRetentionRetainWebServiceImpl, VRMRetentionRetainWebService}

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector(

    new TestOrdnanceSurvey,
    new TestVehicleAndKeeperLookupWebService,
    new TestVRMRetentionEligibilityWebService,
    new TestVrmRetentionRetainWebService,
    new TestPaymentSolveWebService,
    new TestRefererFromHeader,
    new audit1.AuditLocalService,
    new audit2.AuditServiceDoesNothing
  )

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      new TestConfig2(),
      new TestModule(),
      new TestBruteForcePreventionWebService,
      new TestDateService
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

final class TestModule extends ScalaModule {

  def configure() {
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
    bind[CookieFlags].to[RetentionCookieFlags].asEagerSingleton()

    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[_root_.audit1.AuditService].to[_root_.audit1.AuditLocalServiceImpl].asEagerSingleton()
    bind[RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
    bind[webserviceclients.audit2.AuditMicroService].to[webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
    bind[webserviceclients.audit2.AuditService].to[webserviceclients.audit2.AuditServiceImpl].asEagerSingleton()
  }
}