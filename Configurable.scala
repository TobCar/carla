package carla

//Can be modified by "using" and "returning"
abstract class Configurable(val name: String)  {
  var usingName = Set[String]()
  var passingName = Set[String]()
  
  /**
   * Remembers the name of the variable the Configurable needs before running.
   * 
   * Pre: dependentName is not empty or null
   * Post: usingName contains dependentName
   * 
   * Throws IllegalArgumentException if dependentName is empty
   */
  def addUsing( dependentName: String ) {
    dependentName match {
      case "" => throw new IllegalArgumentException("dependentName cannot be empty")
        
      case null => throw new NullArgumentException("dependentName cannot be null")
      
      case _ =>  println("Configured using for '"+name+"' with name: '"+dependentName+"'")
                 usingName += dependentName
    }
  }
  
  /**
   * Remembers the name of the variable the Configurable is supposed to pass after running.
   * 
   * Pre: dependentName is not empty
   * 
   * Throws IllegalArgumentException if dependentName is empty
   */
  def addPassing( dependentName: String ) {
    dependentName match {
      case "" => throw new IllegalArgumentException("dependentName cannot be empty")
        
      case null => throw new NullArgumentException("dependentName cannot be null")
      
      case _ =>  println("Configured passing for '"+name+"' with name: '"+dependentName+"'")
                 passingName += dependentName
    }
  }
}