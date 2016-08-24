package models

final case class VrmLockedViewModel(vehicleLookupFailureViewModel: VehicleLookupFailureViewModel,
                                    timeString: String,
                                    javascriptTimestamp: Long)

object VrmLockedViewModel {

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            timeString: String,
            javascriptTimestamp: Long)(implicit config: utils.helpers.Config): VrmLockedViewModel =
    VrmLockedViewModel(
      VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm, None, failureCode = ""),
      timeString,
      javascriptTimestamp
    )
}
