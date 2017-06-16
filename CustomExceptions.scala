package carla

case class NullArgumentException(message: String) extends IllegalArgumentException(message)
case class UnexpectedTokenException(message: String) extends IllegalStateException(message)
case class IllegalControlFlowException(message: String) extends IllegalStateException(message)