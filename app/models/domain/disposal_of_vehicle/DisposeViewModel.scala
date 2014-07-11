package models.domain.disposal_of_vehicle

import models.domain.common.AddressViewModel

final case class DisposeViewModel(vehicleMake: String,
                                  vehicleModel: String,
                                  dealerName: String,
                                  dealerAddress: AddressViewModel,
                                  transactionId: Option[String] = None,
                                  registrationNumber: String)
