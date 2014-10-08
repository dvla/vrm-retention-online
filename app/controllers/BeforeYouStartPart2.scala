package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class BeforeYouStartPart2 @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                            config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.before_you_start_part_2())
  }
}