package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

object KeeperNameOptString {

  def from(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel) = {

    // flatten and then iterate
    val keeperNameList = List(vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName).flatten

    if (keeperNameList.size > 0) {
      var nameString = keeperNameList(0)
      for (nameItem <- keeperNameList.drop(1)) {
        nameString += (" " + nameItem)
      }
      Some(nameString)
    } else {
      None
    }
  }
}
