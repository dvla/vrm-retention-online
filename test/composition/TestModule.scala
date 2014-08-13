package composition

import app.ConfigProperties.getProperty
import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import common.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import filters.AccessLoggingFilter.AccessLoggerName
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import services.DateService
import services.brute_force_prevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import services.fakes._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import services.brute_force_prevention.BruteForcePreventionWebService
import services.brute_force_prevention.BruteForcePreventionService
import services.brute_force_prevention.BruteForcePreventionServiceImpl
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.vehicle_and_keeper_lookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import services.vrm_retention_eligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService}
import services.vrm_retention_retain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{CookieFlags, NoCookieFlags}

class TestModule() extends ScalaModule with MockitoSugar {

  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    ordnanceSurveyAddressLookup()

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
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }

}