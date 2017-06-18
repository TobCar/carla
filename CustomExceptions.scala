package carla

case class NullArgumentException(message: String) extends IllegalArgumentException(message)
case class UnexpectedTokenException(message: String) extends IllegalStateException(message)
case class IllegalControlFlowException(message: String) extends IllegalStateException(message)
case class DirectoryExpectedException(message: String) extends Exception(message)
case class IllegalFileExtensionException(message: String) extends Exception(message)
case class TypeMismatchException(message: String) extends Exception(message)