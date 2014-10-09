package audit

// TODO very draft - needs breaking down into other classes - won't serialise as more than 23 attributes
case class AuditMessage(fromPage: String, toPage: String,
                        vehicleMake: Option[String], vehicleModel: Option[String],
                        retainedVRM: Option[String], replacementVRM: Option[String],
                        transactionId: Option[String], trackingId: Option[String],
                        keeperEmail: Option[String], // TODO keeper name and address?
                        businessName: Option[String], businessContact: Option[String], businessEmail: Option[String], // TODO business address
                        retentionCertId: Option[String], transactionTimestamp: Option[String],
                        paymentTrxRef: Option[String], maskedPAN: Option[String], paymentAuthCode: Option[String],
                        merchantId: Option[String], paymentType: Option[String], CardType: Option[String], totalAmountPaid: Option[Double])
