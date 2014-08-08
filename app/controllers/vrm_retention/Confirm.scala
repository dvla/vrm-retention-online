package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import mappings.vrm_retention.Confirm.EmailAddressID
import models.domain.vrm_retention._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import utils.helpers.Config

final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

   val form = Form(
    mapping(
      EmailAddressID -> optional(email)
  )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  )

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel,form))
        case (Some(vehicleAndKeeperDetails), None) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form))
        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    Redirect(routes.Payment.present())
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.FullSet)
  }
}