package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.AddressServiceBinding
import composition.webserviceclients.audit2.{AuditMicroServiceBinding, AuditServiceBinding}
import composition.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceBinding, BruteForcePreventionWebServiceBinding}
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, PaymentWebServiceBinding, RefererFromHeaderBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceBinding, VehicleAndKeeperLookupWebServiceBinding}
import composition.webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityServiceBinding, VRMRetentionEligibilityWebServiceBinding}
import composition.webserviceclients.vrmretentionretain.{VrmRetentionRetainServiceBinding, VrmRetentionRetainWebServiceBinding}
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

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
  lazy val injector = Guice.createInjector(
    new ConfigBinding,
    new BruteForcePreventionWebServiceBinding,
    new BruteForcePreventionServiceBinding,
    new DateServiceBinding,
    new AddressServiceBinding,
    new VehicleAndKeeperLookupWebServiceBinding,
    new VehicleAndKeeperLookupServiceBinding,
    new VRMRetentionEligibilityWebServiceBinding,
    new VRMRetentionEligibilityServiceBinding,
    new VrmRetentionRetainWebServiceBinding,
    new VrmRetentionRetainServiceBinding,
    new PaymentServiceBinding,
    new PaymentWebServiceBinding,
    new RefererFromHeaderBinding,
    new AuditServiceBinding,
    new CookieFlagsBinding,
    new LoggerLikeBinding,
    new PdfServiceBinding,
    new EmailServiceBinding,
    new composition.audit1.AuditServiceBinding,
    new AuditMicroServiceBinding,
    new SessionFactoryBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}