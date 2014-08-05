package services.brute_force_prevention

import javax.inject.Inject
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import utils.helpers.Config
import models.domain.common.BruteForcePreventionResponse
import models.domain.common.BruteForcePreventionResponse.JsonFormat
import play.api.libs.json.Json
import services.DateService
import models.domain.common.BruteForcePreventionViewModel

final class BruteForcePreventionServiceImpl @Inject()(config: Config,
                                                      ws: BruteForcePreventionWebService,
                                                      dateService: DateService) extends BruteForcePreventionService {
  private val maxAttempts: Int = config.bruteForcePreventionMaxAttemptsHeader.toInt

  override def isVrmLookupPermitted(vrm: String): Future[BruteForcePreventionViewModel] =
  // TODO US270 this if-statement is a temporary feature toggle until all developers have Redis setup locally.
    if (config.isBruteForcePreventionEnabled) {
      val returnedFuture = scala.concurrent.Promise[BruteForcePreventionViewModel]()
      ws.callBruteForce(vrm).map { resp =>
        def permitted(): Unit = {
          Json.fromJson[BruteForcePreventionResponse](resp.json).asOpt match {
            case Some(model) =>
              val resultModel = BruteForcePreventionViewModel.from(
                permitted = true,
                model,
                dateService,
                maxAttempts
              )
              returnedFuture.success(resultModel)
            case _ =>
              Logger.error(s"Brute force prevention service returned invalid Json: ${resp.json}")
              returnedFuture.failure(new Exception("TODO"))
          }
        }
        def notPermitted = BruteForcePreventionViewModel.from(permitted = false,
                                                                      BruteForcePreventionResponse(attempts = 0),
                                                                      dateService,
                                                                      maxAttempts = maxAttempts)
        resp.status match {
          case play.api.http.Status.OK => permitted()
          case play.api.http.Status.FORBIDDEN => returnedFuture.success(notPermitted)
          case _ => returnedFuture.failure(new Exception("unknownPermission"))
        }
      }.recover {
        case e: Throwable =>
          Logger.error(s"Brute force prevention service throws: ${e.getStackTraceString}")
          returnedFuture.failure(e)
      }
      returnedFuture.future
    }
    else Future {
      BruteForcePreventionViewModel.from(permitted = true,
                                                 BruteForcePreventionResponse(attempts = 0),
                                                 dateService,
                                                 maxAttempts = maxAttempts)
    }
}