package composition

import com.tzavellas.sse.guice.ScalaModule
import org.joda.time.{DateTime, Instant}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}

class TestDateService extends ScalaModule with MockitoSugar {

  def configure() = {
    val dateTimeISOChronology: String = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0).toString
    val today = DayMonthYear(
      DayValid.toInt,
      MonthValid.toInt,
      YearValid.toInt
    )
    val now = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0).toInstant

    val dateService = mock[DateService]
    when(dateService.dateTimeISOChronology).thenReturn(dateTimeISOChronology)
    when(dateService.today).thenReturn(today)
    when(dateService.now).thenReturn(now)
    bind[DateService].toInstance(dateService)
  }
}
