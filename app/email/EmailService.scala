package email

import models._
import org.apache.commons.mail.HtmlEmail
import play.twirl.api.HtmlFormat

trait EmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel,
                transactionId: String,
                confirmFormModel: Option[ConfirmFormModel],
                businessDetailsModel: Option[BusinessDetailsModel])

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  eligibilityModel: EligibilityModel,
                  retainModel: RetainModel,
                  transactionId: String,
                  htmlEmail: HtmlEmail,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel]): HtmlFormat.Appendable
}
