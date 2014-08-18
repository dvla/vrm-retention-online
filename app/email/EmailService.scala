package email

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

trait EmailService {

  def sendEmail(emailAddress: String,
                vrm: String,
                retentionCertId: String,
                transactionId: String,
                transactionTimestamp: String,
                keeperTitle: Option[String],
                keeperFirstName: Option[String],
                keeperLasttName: Option[String],
                keeperAddress: Option[AddressModel],
                amountDebited: String,
                replacementVrm: String)
}