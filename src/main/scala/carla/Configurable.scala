package carla

import scala.collection.mutable.Map

abstract class Configurable(val name: String)  {
  var using = Map[String, String]() // (variableName, variableType)
  var passing = Map[String, String]() // (variableName, variableType)
  
  /**
   * Remembers the name and type of the variable the Configurable needs before running.
   * 
   * Pre: dependentName and dependentType are not empty
   * Post: using contains dependentName as a key and dependentType as a value
   * 
   * Throws IllegalArgumentException if dependentName or dependentType are empty
   */
  def addUsing(dependentName: String, dependentType: String) {
    dependentName match {
      case "" => throw new IllegalArgumentException("dependentName cannot be empty")
        
      case null => throw new NullArgumentException("dependentName cannot be null")
      
      case _ => // Do nothing as the default
    }
    
    dependentType match {
      case "" => throw new IllegalArgumentException("dependentType cannot be empty")
        
      case null => throw new NullArgumentException("dependentType cannot be null")
      
      case _ => // Do nothing as the default
    }
    
    println("Configured 'using' for "+name+" with name: "+dependentName+", and type: "+dependentType)
    using.put(dependentName, dependentType)
  }
  
  /**
   * Remembers the name and type of the variable the Configurable is supposed to pass after running.
   * 
   * Pre: dependentName and dependentType are not empty
   * Post: passing contains dependentName as a key and dependentType as a value
   * 
   * Throws IllegalArgumentException if dependentName or dependentType are empty
   */
  def addPassing(dependentName: String, dependentType: String) {
    dependentName match {
      case "" => throw new IllegalArgumentException("dependentName cannot be empty")
        
      case null => throw new NullArgumentException("dependentName cannot be null")
      
      case _ => // Do nothing as the default
    }
    
    dependentType match {
      case "" => throw new IllegalArgumentException("dependentType cannot be empty")
        
      case null => throw new NullArgumentException("dependentType cannot be null")
      
      case _ => // Do nothing as the default
    }
    
    println("Configured 'passing' for "+name+" with name: "+dependentName+", and type: "+dependentType)
    passing.put(dependentName, dependentType)
  }
}