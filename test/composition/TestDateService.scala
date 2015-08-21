package composition

import _root_.webserviceclients.fakes.DateServiceConstants.DayValid
import _root_.webserviceclients.fakes.DateServiceConstants.MonthValid
import _root_.webserviceclients.fakes.DateServiceConstants.YearValid
import com.tzavellas.sse.guice.ScalaModule
import org.joda.time.DateTime
import org.joda.time.Instant
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

final class TestDateService extends ScalaModule with MockitoSugar {

  val stub = {
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
    val dateTime = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0)
    val now: Instant = dateTime.toInstant
    val dateService = mock[DateService]
    when(dateService.dateTimeISOChronology).thenReturn(dateTimeISOChronology)
    when(dateService.today).thenReturn(today)
    when(dateService.now).thenReturn(now)
    dateService
  }

  def configure() = bind[DateService].toInstance(stub)
}