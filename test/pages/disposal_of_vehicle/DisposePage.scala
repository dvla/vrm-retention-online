package pages.disposal_of_vehicle

import helpers.webbrowser.{Checkbox, Element, Page, SingleSel, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.common.DayMonthYear.{DayId, MonthId, YearId}
import mappings.disposal_of_vehicle.Dispose.BackId
import mappings.disposal_of_vehicle.Dispose.ConsentId
import mappings.disposal_of_vehicle.Dispose.DateOfDisposalId
import mappings.disposal_of_vehicle.Dispose.LossOfRegistrationConsentId
import mappings.disposal_of_vehicle.Dispose.MileageId
import mappings.disposal_of_vehicle.Dispose.SubmitId
import mappings.disposal_of_vehicle.Dispose.TodaysDateOfDisposal
import org.openqa.selenium.WebDriver
import services.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import services.fakes.FakeDisposeWebServiceImpl.MileageValid

object DisposePage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/complete-and-confirm"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete & confirm"

  def mileage(implicit driver: WebDriver): TextField = textField(id(MileageId))

  def dateOfDisposalDay(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$DayId"))

  def dateOfDisposalMonth(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$MonthId"))

  def dateOfDisposalYear(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$YearId"))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def lossOfRegistrationConsent(implicit driver: WebDriver): Element = find(id(LossOfRegistrationConsentId)).get

  def useTodaysDate(implicit driver: WebDriver): Element = find(id(TodaysDateOfDisposal)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def dispose(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposePage
    mileage enter MileageValid
    dateOfDisposalDay select DateOfDisposalDayValid
    dateOfDisposalMonth select DateOfDisposalMonthValid
    dateOfDisposalYear select DateOfDisposalYearValid
    click on consent
    click on lossOfRegistrationConsent
    click on dispose
  }

  def sadPath(implicit driver: WebDriver) = {
    go to DisposePage
    dateOfDisposalDay select ""
    dateOfDisposalMonth select ""
    dateOfDisposalYear select ""
    click on dispose
  }
}