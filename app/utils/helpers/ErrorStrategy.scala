package utils.helpers

import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.routes
import play.api.LoggerLike
import play.api.mvc.RequestHeader
import play.api.mvc.Results.Redirect
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.ErrorStrategyBase
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.filters.ClfEntryBuilder
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.CookieHelper._

class ErrorStrategy @Inject()(clfEntryBuilder: ClfEntryBuilder,
                              @Named(AccessLoggerName) accessLogger: LoggerLike,
                              dateService: DateService )(implicit clientSideSessionFactory: ClientSideSessionFactory)
  extends ErrorStrategyBase(clfEntryBuilder, clfEntry => accessLogger.info(clfEntry), accessLogger, dateService) with DVLALogger {

  protected override def sessionExceptionResult(request: RequestHeader) = {
//    logMessage(request.cookies.trackingId(),Warn,"Removing all cookies except seen cookie." )
    CookieHelper.discardAllCookies(request)
  }

  protected override def errorPageResult(exceptionDigest: String) =
    Redirect(routes.Error.present(exceptionDigest))
}