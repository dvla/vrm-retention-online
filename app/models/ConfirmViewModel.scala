package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

final case class ConfirmViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  userType: String)
