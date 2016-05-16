package composition

import java.io.File
import java.util.{TimeZone, UUID}

import com.typesafe.config.ConfigFactory
import org.joda.time.DateTimeZone
import play.api.Play.current
import play.api.i18n.Lang
import play.api.mvc.Results.NotFound
import play.api.mvc.{RequestHeader, Result}
import play.api.{Application, Configuration, Logger, Mode, Play}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Application configuration is in a hierarchy of files:
 *
 *                        application.conf
 *                    /             |            \
 * application.prod.conf  application.dev.conf  application.test.conf <- these can override and add to application.conf
 *
 * play test  <- test mode picks up application.test.conf
 * play run   <- dev mode picks up application.dev.conf
 * play start <- prod mode picks up application.prod.conf
 *
 * To override and stipulate a particular "conf" e.g.
 * play -Dconfig.file=conf/application.test.conf run
 */
trait GlobalWithFilters extends WithFilters {
  /**
   * Controllers must be resolved through the application context. There is a special method of GlobalSettings
   * that we can override to resolve a given controller. This resolution is required by the Play router.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)

  override def onLoadConfig(configuration: Configuration,
                            path: File,
                            classloader: ClassLoader,
                            mode: Mode.Mode): Configuration = {
    val dynamicConfig = Configuration.from(Map("session.cookieName" -> UUID.randomUUID().toString.substring(0, 16)))
    val applicationConf = System.getProperty("config.file", s"application.${mode.toString.toLowerCase}.conf")
    val environmentOverridingConfiguration = configuration ++
      Configuration(ConfigFactory.load(applicationConf)) ++
      dynamicConfig
    super.onLoadConfig(environmentOverridingConfiguration, path, classloader, mode)
  }

  override def onStart(app: Application) {
    Logger.info("vrm-retention-online Started") // used for operations, do not remove
    val localTimeZone = "Europe/London"
    TimeZone.setDefault(TimeZone.getTimeZone(localTimeZone))
    DateTimeZone.setDefault(DateTimeZone.forID(localTimeZone))
  }

  override def onStop(app: Application) {
    super.onStop(app)
    Logger.info("vrm-retention-online Stopped") // used for operations, do not remove
  }

  // 404 - page not found error http://alvinalexander.com/scala/handling-scala-play-framework-2-404-500-errors
  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Future.successful {
      val playLangCookie = request.cookies.get(Play.langCookieName)
      val value: String = playLangCookie match {
        case Some(cookie) => cookie.value
        case None => "en"
      }
      implicit val lang: Lang = Lang(value)
      implicit val config = injector.getInstance(classOf[Config])
      implicit val dateService =  injector.getInstance(classOf[DateService])
      Logger.warn(s"Broken link returning http code 404. uri: ${request.uri}")
      NotFound(views.html.errors.onHandlerNotFound(request))
    }
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future(errorStrategy(request, ex))
}
