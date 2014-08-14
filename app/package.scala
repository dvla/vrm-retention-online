package object app {

  import play.api.Play
  import scala.util.{Success, Try}

  // TODO move to common project, probably better put in an object instead of a "package obj"
  object ConfigProperties {

    def getProperty(property: String, default: Int) =
      Try(Play.current.configuration.getInt(property).getOrElse(default)) match {
        case Success(s) => s
        case _ => default
      }

    def getProperty(property: String, default: String) =
      Try(Play.current.configuration.getString(property).getOrElse(default)) match {
        case Success(s) => s
        case _ => default
      }

    def getProperty(property: String, default: Boolean) =
      Try(Play.current.configuration.getBoolean(property).getOrElse(default)) match {
        case Success(s) => s
        case _ => default
      }

    def getProperty(property: String, default: Long) =
      Try(Play.current.configuration.getLong(property).getOrElse(default)) match {
        case Success(s) => s
        case _ => default
      }

    def getDurationProperty(property: String, default: Long) =
      Try(Play.current.configuration.getMilliseconds(property).getOrElse(default)) match {
        case Success(s) => s
        case _ => default
      }
  }

}