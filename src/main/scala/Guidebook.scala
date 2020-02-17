object Guidebook {
  sealed trait Command
  case class Inquiry(countryCode: String) extends Command

  sealed trait Response
  case class Guidance(countryCode: String, description: String) extends Response
}
