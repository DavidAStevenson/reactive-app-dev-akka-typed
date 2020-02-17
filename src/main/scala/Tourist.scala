object Tourist {
  sealed trait Command
  case class Start(codes: Seq[String]) extends Command
}
