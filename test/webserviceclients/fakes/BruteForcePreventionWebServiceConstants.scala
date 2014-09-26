package webserviceclients.fakes

import play.api.libs.json.Json

object BruteForcePreventionWebServiceConstants {

  final val VrmAttempt2 = "ST05YYB"
  final val VrmLocked = "ST05YYC"
  final val VrmThrows = "ST05YYD"
  final val MaxAttempts = 3
  lazy val responseFirstAttempt = Some(Json.parse( s"""{"attempts":0}"""))
  lazy val responseSecondAttempt = Some(Json.parse( s"""{"attempts":1}"""))
}