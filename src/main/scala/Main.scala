object Hello extends App {
    println("App starting...")

    import Tourist._
    val start = Start(Seq("NZ"))

    import Guidebook._
    val inquiry = Inquiry("NZ")
    val guidance = Guidance("NZ", "New Zealand Dollars are used there")
}
