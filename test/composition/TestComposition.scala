package composition

import _root_.webserviceclients.paymentsolve.TestRefererFromHeader
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import composition.webserviceclients.addresslookup.TestAddressLookupBinding
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.AuditMicroServiceCallNotOk
import composition.webserviceclients.bruteforceprevention.{TestBruteForcePreventionWebService, BruteForcePreventionServiceBinding}
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, TestPaymentSolveWebService}
import composition.webserviceclients.vehicleandkeeperlookup.{TestVehicleAndKeeperLookupWebService, VehicleAndKeeperLookupServiceBinding}
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityServiceBinding
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
      new VehicleAndKeeperLookupServiceBinding,
      new VRMRetentionEligibilityServiceBinding,
      new BruteForcePreventionServiceBinding,
      new LoggerLikeBinding,
      new PdfServiceBinding,
      new EmailServiceBinding,
      new VrmRetentionRetainServiceBinding,
      new SessionFactoryBinding,
      new CookieFlagsBinding,
      new PaymentServiceBinding,
      // Completely mocked web services below...
      new TestConfig(),
      new TestBruteForcePreventionWebService,
      new TestDateService,
      new TestAddressLookupBinding,
      new TestVehicleAndKeeperLookupWebService,
      new TestVRMRetentionEligibilityWebService,
      new TestVrmRetentionRetainWebService,
      new TestPaymentSolveWebService,
      new TestRefererFromHeader,
      new audit1.AuditLocalService,
      new audit2.AuditServiceDoesNothing,
      new AuditMicroServiceCallNotOk
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

