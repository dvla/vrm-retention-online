package controllers

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class ConfirmBusiness @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok("Hello, world")
  }
}