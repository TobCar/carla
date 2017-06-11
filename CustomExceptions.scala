package carla

case class NullArgumentException(message: String) extends IllegalArgumentException(message)