package email

import models.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import org.apache.commons.mail.HtmlEmail
import play.twirl.api.HtmlFormat

trait EmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel,
                transactionId: String)

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  eligibilityModel: EligibilityModel,
                  retainModel: RetainModel,
                  transactionId: String,
                  htmlEmail: HtmlEmail): HtmlFormat.Appendable
}
