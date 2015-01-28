package composition

import com.google.inject.Guice
import composition.webserviceclients.audit2.AuditServiceBinding
import composition.webserviceclients.paymentsolve.{PaymentSolveBinding, RefererFromHeaderBinding}
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupBinding
import composition.webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityBinding
import composition.webserviceclients.vrmretentionretain.VrmRetentionRetainBinding
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new ConfigBinding,
    new DevModule,
    new BruteForcePreventionWebServiceBinding,
    new DateServiceBinding,
    new AddressServiceBinding,
    new VehicleAndKeeperLookupBinding,
    new VRMRetentionEligibilityBinding,
    new VrmRetentionRetainBinding,
    new PaymentSolveBinding,
    new RefererFromHeaderBinding,
    new AuditServiceBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}