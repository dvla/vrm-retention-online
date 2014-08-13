package composition

import app.ConfigProperties.getProperty
import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import services.address_lookup.{AddressLookupService, AddressLookupWebService}
import services.brute_force_prevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import services.fakes._
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.vehicle_and_keeper_lookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import services.vehicle_lookup.{VehicleLookupService, VehicleLookupServiceImpl, VehicleLookupWebService}
import services.vrm_retention_eligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService}
import services.vrm_retention_retain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory, CookieFlags, NoCookieFlags}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName

class TestModule() extends ScalaModule with MockitoSugar {

  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    getProperty("addressLookupService.type", "ordnanceSurvey") match {
      case "ordnanceSurvey" => ordnanceSurveyAddressLookup()
      case _ => gdsAddressLookup()
    }
    bind[VehicleLookupWebService].to[FakeVehicleLookupWebService].asEagerSingleton()
    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupWebService].to[FakeVehicleAndKeeperLookupWebService].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[DateService].to[FakeDateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[FakeBruteForcePreventionWebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))

    bind[VRMRetentionEligibilityWebService].to[FakeVRMRetentionEligibilityWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()

    bind[VRMRetentionRetainWebService].to[FakeVRMRetentionRetainWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
  }

  private def ordnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[services.address_lookup.ordnance_survey.AddressLookupServiceImpl]

    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }

  private def gdsAddressLookup() = {
    bind[AddressLookupService].to[services.address_lookup.gds.AddressLookupServiceImpl]
    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForGdsAddressLookup,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForGdsAddressLookup
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }
}