package functional

import helpers.webbrowser.TestHarness
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpec}
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import services.fakes.FakeAddressLookupService.{TraderBusinessNameValid, PostcodeValidWithSpace}
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import services.fakes.FakeDisposeWebServiceImpl.MileageValid
import services.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

final class DemoSpec extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with TestHarness {

//  feature("Dispose of a vehicle to trade") {
//    info("As a vehicle trader")
//    info("I want to dispose of a vehicle for a customer")
//    info("So they will be removed from the vehicle record as current keeper")
//    info("")
//
//    scenario("Sell a vehicle to the trade: happy path") {
//
//      new WebBrowser {
//
//        Given("I am on the vehicles online prototype site")
//        go to BeforeYouStartPage
//
//        And("I click the Start now button to begin the transaction")
//        click on BeforeYouStartPage.startNow
//
//        And("I enter \"Car Giant\" in the business name field")
//        SetupTradeDetailsPage.traderName enter TraderBusinessNameValid
//
//        And("I enter \"CM8 1QJ\" in the business postcode field")
//        SetupTradeDetailsPage.traderPostcode enter PostcodeValidWithSpace
//
//        And("I click the Look-up button")
//        click on SetupTradeDetailsPage.lookup
//
//        And("I select \"1, OLIVERS DRIVE, WITHAM, CM8 1QJ\"")
//        BusinessChooseYourAddressPage.chooseAddress.value = traderUprnValid.toString
//
//        And("I click the Select button")
//        click on BusinessChooseYourAddressPage.select
//
//        And("I enter \"A1\" in the vehicle registration number field")
//        VehicleLookupPage.vehicleRegistrationNumber enter RegistrationNumberValid
//
//        And("I enter \"12345678910\" in the V5C document reference number field")
//        VehicleLookupPage.documentReferenceNumber enter ReferenceNumberValid
//
//        And("I click the find vehicle details button")
//        click on VehicleLookupPage.findVehicleDetails
//
//        And("I enter \"10000\" in the vehicle mileage field")
//        DisposePage.mileage enter MileageValid
//
//        And("I select \"01\" from the date of disposal day dropdown")
//        DisposePage.dateOfDisposalDay select DateOfDisposalDayValid
//
//        And("I select \"March\" from the date of disposal month dropdown")
//        DisposePage.dateOfDisposalMonth select DateOfDisposalMonthValid
//
//        And("I enter \"2014\" in the date of disposal year field")
//        DisposePage.dateOfDisposalYear select DateOfDisposalYearValid
//
//        And("I select \"I have the consent of the current keeper to dispose of this vehicle\"")
//        click on DisposePage.consent
//
//        And("I select \"I have the confirmation that the current keeper is aware that the registration will be disposed of with the vehicle\"")
//        click on DisposePage.lossOfRegistrationConsent
//
//        When("I click the dispose button")
//        click on DisposePage.dispose
//
//        Then("I should see \"Sell a vehicle into the motor trade: summary \"")
//        page.text should include("Sell a vehicle into the motor trade: summary")
//      }
//    }
//  }
}