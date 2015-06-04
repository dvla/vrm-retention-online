package email

import models._
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import webserviceclients.emailservice.EmailServiceSendRequest

trait RetainEmailService {
  def emailRequest(emailAddress: String,
                   vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                   eligibilityModel: EligibilityModel,
                   certificateNumber: String,
                   transactionTimestamp: String,
                   transactionId: String,
                   confirmFormModel: Option[ConfirmFormModel],
                   businessDetailsModel: Option[BusinessDetailsModel],
                   isKeeper: Boolean,
                   trackingId: String): Option[EmailServiceSendRequest]

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                certificateNumber: String,
                transactionTimestamp: String,
                transactionId: String,
                confirmFormModel: Option[ConfirmFormModel],
                businessDetailsModel: Option[BusinessDetailsModel],
                isKeeper: Boolean,
                trackingId: String)

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  eligibilityModel: EligibilityModel,
                  certificateNumber: String,
                  transactionTimestamp: String,
                  transactionId: String,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  isKeeper: Boolean): HtmlFormat.Appendable
}