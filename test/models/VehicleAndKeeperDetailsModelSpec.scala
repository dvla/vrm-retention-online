package models

import helpers.UnitSpec
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto

final class VehicleAndKeeperDetailsModelSpec extends UnitSpec with MockitoSugar {

  "from" should {

    "return keeper title when keeper title starts with the letter 'M'" in {
      val keeperTitle = Some("MR")
      val vehicleAndKeeperDetailsDto = VehicleAndKeeperDetailsDto(
        registrationNumber = "stub-registrationNumber",
        vehicleMake = None,
        vehicleModel = None,
        keeperTitle = keeperTitle,
        keeperFirstName = None,
        keeperLastName = None,
        keeperAddressLine1 = Some("stub-keeperAddressLine1"),
        keeperAddressLine2 = None,
        keeperAddressLine3 = None,
        keeperAddressLine4 = None,
        keeperPostTown = Some("stub-keeperPostTown"),
        keeperPostcode = Some("stub-keeperPostcode"),
        disposeFlag = None,
        keeperEndDate = None,
        suppressedV5Flag = None
      )
      VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto).title should equal(keeperTitle)
    }

    "return keeper title uppercased when keeper title starts with a lowercase letter 'M'" in {
      val keeperTitle = Some("mr")
      val vehicleAndKeeperDetailsDto = VehicleAndKeeperDetailsDto(
        registrationNumber = "stub-registrationNumber",
        vehicleMake = None,
        vehicleModel = None,
        keeperTitle = keeperTitle,
        keeperFirstName = None,
        keeperLastName = None,
        keeperAddressLine1 = Some("stub-keeperAddressLine1"),
        keeperAddressLine2 = None,
        keeperAddressLine3 = None,
        keeperAddressLine4 = None,
        keeperPostTown = Some("stub-keeperPostTown"),
        keeperPostcode = Some("stub-keeperPostcode"),
        disposeFlag = None,
        keeperEndDate = None,
        suppressedV5Flag = None
      )
      val expected = Some("MR")
      VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto).title should equal(expected)
    }

    "return None when keeper title does not start with the letter 'M'" in {
      val keeperTitle = None
      val vehicleAndKeeperDetailsDto = VehicleAndKeeperDetailsDto(
        registrationNumber = "stub-registrationNumber",
        vehicleMake = None,
        vehicleModel = None,
        keeperTitle = keeperTitle,
        keeperFirstName = None,
        keeperLastName = None,
        keeperAddressLine1 = Some("stub-keeperAddressLine1"),
        keeperAddressLine2 = None,
        keeperAddressLine3 = None,
        keeperAddressLine4 = None,
        keeperPostTown = Some("stub-keeperPostTown"),
        keeperPostcode = Some("stub-keeperPostcode"),
        disposeFlag = None,
        keeperEndDate = None,
        suppressedV5Flag = None
      )
      VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto).title should equal(keeperTitle)
    }
  }
}