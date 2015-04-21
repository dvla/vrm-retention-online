package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.AddressServiceBinding
import composition.webserviceclients.audit2.AuditMicroServiceBinding
import composition.webserviceclients.audit2.AuditServiceBinding
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionWebServiceBinding
import composition.webserviceclients.emailservice.EmailServiceBinding
import composition.webserviceclients.emailservice.EmailServiceWebServiceBinding
import composition.webserviceclients.paymentsolve.PaymentServiceBinding
import composition.webserviceclients.paymentsolve.PaymentWebServiceBinding
import composition.webserviceclients.paymentsolve.RefererFromHeaderBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceBinding
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityServiceBinding
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityWebServiceBinding
import composition.webserviceclients.vrmretentionretain.VrmRetentionRetainServiceBinding
import composition.webserviceclients.vrmretentionretain.VrmRetentionRetainWebServiceBinding
import filters.ServiceOpenFilter
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionFilter
import uk.gov.dvla.vehicles.presentation.common.filters.EnsureSessionCreatedFilter
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
    new RetainEmailServiceBinding,
    new EmailServiceBinding,
    new EmailServiceWebServiceBinding,
    new AuditMicroServiceBinding,
    new SessionFactoryBinding,
    new DateTimeZoneServiceBinding,
    new HealthStatsBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter]),
    injector.getInstance(classOf[ServiceOpenFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}
