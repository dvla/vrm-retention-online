package controllers.vrm_retention

import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import common.{ClientSideSession, ClientSideSessionFactory}
import javax.inject.Inject
import mappings.common.DropDown.addressDropDown
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.BusinessChooseYourAddress.AddressSelectId
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{BusinessDetailsModel, BusinessChooseYourAddressViewModel, BusinessChooseYourAddressFormModel, SetupBusinessDetailsFormModel}
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.address_lookup.AddressLookupService
import utils.helpers.Config
import utils.helpers.FormExtensions.formBinding
import views.html.vrm_retention.business_choose_your_address

final class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  private[vrm_retention] val form = Form(
    mapping(
      /* We cannot apply constraints to this drop down as it is populated by web call to an address lookup service.
      We would need the request here to get the cookie.
      Validation is done when we make a second web call with the UPRN,
      so if a bad guy is injecting a non-existent UPRN then it will fail at that step instead */
      AddressSelectId -> addressDropDown
    )(BusinessChooseYourAddressFormModel.apply)(BusinessChooseYourAddressFormModel.unapply)
  )

  def present = Action.async { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleDetailsModel]) match {
      case (Some(setupBusinessDetailsFormModel), Some(vehicleDetailsModel)) =>
        val businessChooseYourAddressViewModel = createViewModel(setupBusinessDetailsFormModel, vehicleDetailsModel)
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupBusinessDetailsFormModel)(session, lang).map { addresses =>
          Ok(views.html.vrm_retention.business_choose_your_address(businessChooseYourAddressViewModel, form.fill(),
            addresses))
        }
      case _ => Future {
        Redirect(routes.SetUpBusinessDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleDetailsModel)) =>
        val businessChooseYourAddressViewModel = createViewModel(setupBusinessDetailsFormModel, vehicleDetailsModel)
        implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupBusinessDetailsFormModel).map { addresses =>
              BadRequest(business_choose_your_address(businessChooseYourAddressViewModel, formWithReplacedErrors(invalidForm),
                addresses))
            }
          case _ => Future {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupBusinessDetailsFormModel] match {
          case Some(setupBusinessDetailsFormModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm, setupBusinessDetailsFormModel.businessName)
          case None => Future {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        }
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "vrm_retention_businessChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def fetchAddresses(model: SetupBusinessDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.businessPostcode, session.trackingId)

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, businessName: String)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        val businessDetailsModel = BusinessDetailsModel(businessName = businessName, businessAddress = addressViewModel)
        /* The redirect is done as the final step within the map so that:
         1) we are not blocking threads
         2) the browser does not change page before the future has completed and written to the cache. */
        Redirect(routes.VehicleLookup.present()).
          discardingCookie(EnterAddressManuallyCacheKey).
          withCookie(model).
          withCookie(businessDetailsModel)
      case None => Redirect(routes.MicroServiceError.present()) // TODO
//      case None => Redirect(routes.UprnNotFound.present())
    }
  }

  private def createViewModel(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
                              vehicleDetails: VehicleDetailsModel): BusinessChooseYourAddressViewModel =
    BusinessChooseYourAddressViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      businessName = setupBusinessDetailsFormModel.businessName,
      businessPostCode = setupBusinessDetailsFormModel.businessPostcode
    )
}