package common

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags

class ClearTextClientSideSessionFactory @Inject()(implicit cookieFlags: CookieFlags) extends ClientSideSessionFactory {

  override def newSessionCookiesIfNeeded(request: Traversable[Cookie]): Option[Seq[Cookie]] =
    Some(Seq(Cookie("PLAY_LANG", "en")))  // Force English language until Welsh translation is finalised

  override def getSession(request: Traversable[Cookie]): ClientSideSession =
    getTrackingId(request) match {
      case Some(trackingId) => new ClearTextClientSideSession(trackingId)
      case None => new ClearTextClientSideSession(ClearTextClientSideSessionFactory.DefaultTrackingId)
    }

  private def getTrackingId(request: Traversable[Cookie]): Option[String] =
    request.find(_.name == ClientSideSessionFactory.TrackingIdCookieName).map(_.value)
}

object ClearTextClientSideSessionFactory {
  final val DefaultTrackingId = "default_test_tracking_id"
}