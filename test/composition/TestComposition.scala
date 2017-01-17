package composition

import com.google.inject.util.Modules
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import composition.webserviceclients.audit2.AuditMicroServiceCallNotOk
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.bruteforceprevention.TestBruteForcePreventionWebService
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, TestPaymentSolveWebService, TestRefererFromHeader}
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceBinding
import composition.webserviceclients.vrmretentioneligibility.TestVRMRetentionEligibilityWebService
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityServiceBinding
import composition.webserviceclients.vrmretentionretain.TestVrmRetentionRetainWebService
import composition.webserviceclients.vrmretentionretain.VrmRetentionRetainServiceBinding

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  // This class is used to IoC components that don't call out to real web services. When you use IoC to create them, any
  // dependencies that call real web services must be mocked.
  // The testInjector is setup with mocks stubbed to return success instead of making actual web service calls. If for a
  // particular test you want different behaviour from the mock, you should call testInjector and pass in a module
  // that stubs the mock for your test's requirements.
  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
      new BruteForcePreventionServiceBinding,
      new CookieFlagsBinding,
      new LoggerLikeBinding,
      new PaymentServiceBinding,
      new PdfServiceBinding,
      new SessionFactoryBinding,
      new VehicleAndKeeperLookupServiceBinding,
      new VRMRetentionEligibilityServiceBinding,
      new VrmRetentionRetainServiceBinding,
      // Completely mocked web services below...
      new TestConfig(),
      new TestBruteForcePreventionWebService,
      new TestDateService,
      new TestVehicleAndKeeperLookupWebService,
      new TestVRMRetentionEligibilityWebService,
      new TestVrmRetentionRetainWebService,
      new TestPaymentSolveWebService,
      new TestRefererFromHeader,
      new AuditServiceDoesNothing,
      new AuditMicroServiceCallNotOk,
      new TestEmailService,
      new TestReceiptEmailService,
      new TestDateTimeZoneServiceBinding
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}