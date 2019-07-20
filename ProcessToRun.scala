package carla

import scala.collection.mutable.Queue

class ProcessToRun(name: String, parentActorName: String) extends Orderable(name) {
  var hasGivenToken = false
  
  // The variable names used by the process that will be run
  val actualUsing = collection.mutable.Map[String, String]() // (expected, actual)
  val actualPassing = collection.mutable.Map[String, String]() // (expected, actual)
  
  /**
   * Pre: dependentName is not empty
   * 			dependentName has exactly one space. When the String is split using the
   * 			space as a delimiter, the first substring (start to space) is the variable
   * 			name the process expects, and the second substring (space to end) is the
   * 			variable name the current process is passing.
   * 
   * Throws IllegalArgumentException if dependentName or dependentType are empty
   * 				IllegalStateException if dependentName has more than one space
   */
  override def addUsing(dependentName: String, dependentType: String) {
    if( dependentName == "" )
      throw new IllegalArgumentException("dependentName cannot be empty")
    if( dependentType == "" )
      throw new IllegalArgumentException("dependentType cannot be empty")
    
    val splitString = dependentName.split(" ")
    if( splitString.length != 2 ) {
      throw new IllegalStateException("addUsing requires exactly one space in dependentName. It was passed: '"+dependentName+"'")
    }
    super.addUsing(splitString(1), dependentType)
    actualUsing.put(splitString(1), splitString(0))
  }
  
  /**
   * Pre: dependentName is not empty
   * 			dependentName has exactly one space. When the String is split using the
   * 			space as a delimiter, the first substring (start to space) is the variable
   * 			name the process is passing, and the second substring (space to end) is the
   * 			variable name the current process will receive.
   * 
   * Throws IllegalArgumentException if dependentName or dependentType are empty
   * 				IllegalStateException if dependentName has more than one space
   */
  override def addPassing(dependentName: String, dependentType: String) {
    if( dependentName == "" )
      throw new IllegalArgumentException("dependentName cannot be empty")
    if( dependentType == "" )
      throw new IllegalArgumentException("dependentType cannot be empty")
    
    val splitString = dependentName.split(" ")
    if( splitString.length != 2 ) {
      throw new IllegalStateException("addPassing requires exactly one space in dependentName. It was passed: '"+dependentName+"'")
    }
    super.addPassing(splitString(1), dependentType)
    actualPassing.put(splitString(1), splitString(0))
  }
  
  /**
   * This feature is not meant to be used in ProcessToRun
   */
  def insert(token: String) {
    throw new UnsupportedOperationException("insert is not implemented in ProcessToRun")
  }
  
  /**
   * Returning: Will getToken() succeed
   */
  def hasToken(): Boolean =
    hasGivenToken == false
  
  /**
   * Returning: A single line of code to activate a process
   */
  def getToken(): String = {
    if( hasToken() == false )
      throw new IllegalStateException("hasToken() == false, cannot call getToken()")
    hasGivenToken = true
    
    // Create Map literals
    var input = "collection.immutable.Map[String, Any]("
    var inputsAdded = 0
    for( (expectedName, nameBeingPassed) <- actualUsing ) {
      inputsAdded += 1
      input += "\""+nameBeingPassed+"\"->"+expectedName
      if( inputsAdded != actualUsing.size ) {
        input += ","
      }
    }
    input += ")"
    
    var outputKeys = "collection.immutable.Map[String, String]("
    var outputsAdded = 0
    for( (expectedName, nameBeingPassed) <- actualPassing ) {
      outputsAdded += 1
      outputKeys += "\""+nameBeingPassed+"\"->\""+expectedName+"\""
      if( outputsAdded != actualPassing.size ) {
        outputKeys += ","
      }
    }
    outputKeys += ")"
    
    // Only line of code is to activate the process thread
    name+".activate("+input+","+outputKeys+","+parentActorName+")\n"    
  }
}