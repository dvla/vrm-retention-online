package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.AddressServiceBinding
import composition.webserviceclients.audit2.AuditServiceBinding
import composition.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceBinding, BruteForcePreventionWebServiceBinding}
import composition.webserviceclients.paymentsolve.{PaymentSolveBinding, RefererFromHeaderBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceBinding, VehicleAndKeeperLookupWebServiceBinding}
import composition.webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityServiceBinding, VRMRetentionEligibilityWebServiceBinding}
import composition.webserviceclients.vrmretentionretain.VrmRetentionRetainBinding
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new ConfigBinding,
    new DevModule,
    new BruteForcePreventionWebServiceBinding,
    new BruteForcePreventionServiceBinding,
    new DateServiceBinding,
    new AddressServiceBinding,
    new VehicleAndKeeperLookupWebServiceBinding,
    new VehicleAndKeeperLookupServiceBinding,
    new VRMRetentionEligibilityWebServiceBinding,
    new VRMRetentionEligibilityServiceBinding,
    new VrmRetentionRetainBinding,
    new PaymentSolveBinding,
    new RefererFromHeaderBinding,
    new AuditServiceBinding,
    new CookieFlagsBinding,
    new LoggerLikeBinding,
    new PdfServiceBinding,
    new EmailServiceBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}