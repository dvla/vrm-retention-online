package composition

import _root_.webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl, TestRefererFromHeader}
import _root_.webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl}
import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService
import composition.webserviceclients.vehicleandkeeperlookup.{TestVehicleAndKeeperLookupWebService, VehicleAndKeeperLookupServiceBinding}
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityServiceBinding
import email.{EmailService, EmailServiceImpl}
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import utils.helpers.RetentionCookieFlags

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
      new TestModule(),
      new VehicleAndKeeperLookupServiceBinding,
      new VRMRetentionEligibilityServiceBinding,
      new BruteForcePreventionServiceBinding,
      new LoggerLikeBinding,
      new PdfServiceBinding,
      // Completely mocked web services below...
      new TestConfig(),
      new TestBruteForcePreventionWebService,
      new TestDateService,
      new TestOrdnanceSurveyBinding,
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
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
    bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[RetentionCookieFlags].asEagerSingleton()

    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
  }
}