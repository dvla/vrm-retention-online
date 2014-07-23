package models.domain.vrm_retention

import models.domain.common.AddressViewModel

final case class SuccessViewModel(registrationNumber: String,
                                  vehicleMake: String,
                                  vehicleModel: String,
                                  keeperTitle: String,
                                  keeperFirstName: String,
                                  keeperLastName: String,
                                  keeperAddress: AddressViewModel,
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessEmail: Option[String],
                                  businessAddress: Option[AddressViewModel],
                                  replacementRegistrationNumber: String,
                                  retentionCertificationNumber: String,
                                  transactionId: String,
                                  transactionTimestamp: String)