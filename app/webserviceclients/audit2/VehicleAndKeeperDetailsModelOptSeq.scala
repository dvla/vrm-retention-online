package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

object VehicleAndKeeperDetailsModelOptSeq {

  def from(vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel]) = {
    vehicleAndKeeperDetailsModel match {
      case Some(vehicleAndKeeperDetails) =>
        val currentVrmOpt = Some(("currentVrm", vehicleAndKeeperDetails.registrationNumber))
        val makeOpt = vehicleAndKeeperDetails.make.map(make => ("make", make))
        val modelOpt = vehicleAndKeeperDetails.model.map(model => ("model", model))
        val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetails).map(
          keeperName => ("keeperName", keeperName))
        val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetails.address).map(
          keeperAddress => ("keeperAddress", keeperAddress))
        Seq(currentVrmOpt, makeOpt, modelOpt, keeperNameOpt, keeperAddressOpt)
      case _ => Seq.empty
    }
  }
}
