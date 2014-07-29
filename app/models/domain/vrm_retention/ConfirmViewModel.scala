package models.domain.vrm_retention

import models.domain.common.AddressViewModel

final case class ConfirmViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressViewModel],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessAddress: Option[AddressViewModel])