package composition

import com.google.inject.Guice
import composition.audit2.AuditServiceBinding
import composition.paymentsolve.RefererFromHeaderBinding
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new ConfigBinding,
    new DevModule,
    new BruteForcePreventionWebServiceBinding,
    new DateServiceBinding,
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