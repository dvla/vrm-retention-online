package composition

import _root_.webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl, TestRefererFromHeader}
import _root_.webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl}
import _root_.webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl}
import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import email.{EmailService, EmailServiceImpl}
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl}
import utils.helpers.RetentionCookieFlags

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      new TestConfig2(),
      new TestModule(),
      new TestBruteForcePreventionWebService,
      new TestDateService,
      new TestOrdnanceSurvey,
      new TestVehicleAndKeeperLookupWebService,
      new TestVRMRetentionEligibilityWebService,
      new TestVrmRetentionRetainWebService,
      new TestPaymentSolveWebService,
      new TestRefererFromHeader,
      new audit1.AuditLocalService,
      new composition.webserviceclients.audit2.AuditServiceDoesNothing
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

// This class is used to IoC components that don't call out to real web services. When you use IoC to create them, any
// dependencies that call real web services must be mocked.
// The testInjector is setup with mocks stubbed to return success instead of making actual web service calls. If for a
// particular test you want different behaviour from the mock, you should call testInjector and pass in a module
// that stubs the mock for your test's requirements.
final class TestModule extends ScalaModule {

  def configure() {
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
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
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
  }
}