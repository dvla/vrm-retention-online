package models

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

final case class DayMonthYear(day: Int,
                              month: Int,
                              year: Int,
                              hour: Option[Int] = None,
                              minutes: Option[Int] = None) extends Ordered[DayMonthYear] {

  def withTime(hour: Int, minutes: Int) = copy(hour = Some(hour), minutes = Some(minutes))
  def `yyyy-MM-dd`: String = format("yyyy-MM-dd")
  def `dd month, yyyy`: String = format("dd MMMM, yyyy")
  def `yyyy-MM-dd'T'HH:mm:00`: String = format("yyyy-MM-dd'T'HH:mm:00")
  def `HH:mm`: String = format("HH:mm")

  def `dd/MM/yyyy`: String = {
    def pad(i: Int): String = if (i < 10) s"0$i" else s"$i"
    pad(day) + "/" + pad(month) + "/" + year.toString
  }

  def -(amount: Int) = new Period {
    override def days = adjust { _.minusDays(amount) }
    override def weeks = adjust { _.minusWeeks(amount) }
    override def months = adjust { _.minusMonths(amount) }
    override def years = adjust { _.minusYears(amount) }
  }

  private def adjust(f: DateTime => DateTime) = toDateTime match {
    case Some(dt: DateTime) =>
      val newDateTime = f(dt) // Apply function to make a change to the DateTime
      DayMonthYear(newDateTime.dayOfMonth().get, newDateTime.monthOfYear().get, newDateTime.year().get, hour, minutes)
    case _ => this
  }

  private def format(pattern: String): String =
    Try(new DateTime(year, month, day, hour.getOrElse(0), minutes.getOrElse(0))) match {
      case Success(dt: DateTime) => DateTimeFormat.forPattern(pattern).print(dt)
      case Failure(_) => ""
    }

  def toDateTime: Option[DateTime] =
    try Some(new DateTime(year, month, day, hour.getOrElse(0), minutes.getOrElse(0)))
    catch {
      case e: Exception => None
    }

  def compare(that: DayMonthYear) = {
    def compareMinutes(that: DayMonthYear) = (this.minutes, that.minutes) match {
      case (None, Some(_)) => -1  // There is nothing to compare to so it is less.
      case (Some(x: Int), Some(y: Int)) => x.compare(y)
      case (Some(_), None) => 1 // There is nothing to compare to so it is greater.
      case (None, None) => 0 // Neither have hours, and the only way to get here is is year, month and day are equal so must be equal.
    }

    def compareHours(that: DayMonthYear) = (this.hour, that.hour) match {
      case (None, Some(_)) => -1  // There is nothing to compare to so it is less.
      case (Some(thisHours: Int), Some(thatHours: Int)) => thisHours.compare(thatHours) match {
        case 0 => compareMinutes(that)
        case notEqual => notEqual
      }
      case (Some(_), None) => 1 // There is nothing to compare to so it is greater.
      case (None, None) => 0 // Neither have hours, and the only way to get here is is year, month and day are equal so must be equal.
    }

    this.year.compareTo(that.year) match {
      case 0 => this.month.compareTo(that.month) match {
        case 0 => this.day.compareTo(that.day) match {
          case 0 => compareHours(that)
          case notEqual => notEqual
        }
        case notEqual => notEqual
      }
      case notEqual => notEqual
    }
  }

  @tailrec
  final def yearRangeToDropdown(yearsIntoThePast: Int,
                                accumulator: Seq[(String, String)] = Seq.empty): Seq[(String, String)] = {

    // Populate a dropdown with the years between a starting year and now.
    def dropdownFormat = {
      // Turn year into a format that can be displayed by a dropdown
      val yearInPast = (year - yearsIntoThePast).toString
      Seq(yearInPast -> yearInPast)
    }

    yearsIntoThePast match {
      case 0 => accumulator ++ dropdownFormat // Add to this before recursion and finally on exit
      case _ => yearRangeToDropdown(yearsIntoThePast - 1, accumulator ++ dropdownFormat) // Add to end of the list, then recurse
    }
  }
}

object DayMonthYear {
  implicit val JsonFormat = Json.format[DayMonthYear]

  def from(dt: DateTime) =
    DayMonthYear(
      day = dt.dayOfMonth().get,
      month = dt.monthOfYear().get,
      year = dt.year().get,
      hour = Some(dt.hourOfDay().get()),
      minutes = Some(dt.minuteOfHour().get)
    )

  def today = {
    val now = DateTime.now()
    new DayMonthYear(
      now.dayOfMonth().get,
      now.monthOfYear().get,
      now.year().get,
      Some(now.hourOfDay().get()),
      Some(now.minuteOfHour().get)
    )
  }
}

sealed trait Period {
  def day: DayMonthYear = days
  def days: DayMonthYear
  def week: DayMonthYear = weeks
  def weeks: DayMonthYear
  def month: DayMonthYear = months
  def months: DayMonthYear
  def year: DayMonthYear = years
  def years: DayMonthYear
}