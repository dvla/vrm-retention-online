package services.ordinance_survey.domain

import play.api.libs.json.Json

case class OSAddressbaseSearchResponse(
                                        header: OSAddressbaseHeader,
                                        results: Option[Seq[OSAddressbaseResult]]
                                        )

object OSAddressbaseSearchResponse {
  implicit val readsOSAddressbaseSearchResponse = Json.reads[OSAddressbaseSearchResponse]
}