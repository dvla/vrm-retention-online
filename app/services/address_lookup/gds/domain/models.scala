package services.address_lookup.gds.domain

import org.joda.time.DateTime

case class Details(
                    usrn: String,
                    isResidential: Boolean,
                    isCommercial: Boolean,
                    isPostalAddress: Boolean,
                    classification: String,
                    state: String,
                    organisation: Option[String]
                    // blpuUpdatedAt: DateTime,
                    // blpuCreatedAt: DateTime
                    )

case class Presentation(
                         property: Option[String],
                         street: Option[String],
                         town: Option[String],
                         area: Option[String],
                         postcode: String,
                         uprn: String){
  def toViewModel = Seq(property, street, town, area, Some(postcode)).flatten
}

case class Address(
                    gssCode: String,
                    countryCode: String,
                    postcode: String,
                    houseName: Option[String],
                    houseNumber: Option[String],
                    // createdAt: DateTime,
                    presentation: Presentation,
                    details: Details,
                    location: Location){
  def toViewModel = presentation.toViewModel
}

case class Ero(telephoneNumber: String = "")
case class LocalAuthority(gssId: String, opcsId: String, name: String, ero: Ero = Ero())

object JsonFormats {

  import play.api.libs.json._

  val pattern = "yyyy-MM-dd'T'HH:mm:ssz"
  implicit val dateFormat =
    Format[DateTime](Reads.jodaDateReads(pattern), Writes.jodaDateWrites(pattern))

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val streetFormat = Json.format[Street]
  implicit val locationFormat = Json.format[Location]
  implicit val presentationFormat = Json.format[Presentation]
  implicit val detailsFormat = Json.format[Details]
  implicit val addressFormat = Json.format[Address]
  implicit val eroFormat = Json.format[Ero]
  implicit val authorityFormat = Json.format[LocalAuthority]
}
