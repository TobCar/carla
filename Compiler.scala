package carla

import java.io.{File, FileWriter, BufferedWriter}

object Compiler {
  
  def main( args: Array[String] ) {
    val scanner = new LexicalScanner()
    scanner.readFile("/Users/TobiasC/Scala/Carla/src/carla/carla-example.crr")
    val superContainer = Container.createSuperContainer()
    loadContentInto(scanner, superContainer)
    ScalaWriter.createScalaFilesFrom(superContainer)
  }
  
  /**
   * Pre: There was just an open bracket "{" and there is a closing
   * 			bracket "}" to match it OR it is the start of the file.
   */
  def loadContentInto( scanner: LexicalScanner, container: Container ) {
    var depth = 1
    while( scanner.hasNextToken() ) {
      val currentToken = scanner.nextToken()
      currentToken match {
        case "process" => val internalContainer = new Container(scanner.nextToken())
                          processContainer(scanner, internalContainer)
                          container.insert(internalContainer)
        case "step" => val step = new Step(scanner.nextToken())
                       processStep(scanner, step)
                       container.insert(step)
        case "last" => val lastStep = Step.createLastStep()
                       processStep(scanner, lastStep)
                       container.insert(lastStep)
        case "}" => depth -= 1
                    if( depth == 0 )
                      return
        case _ => println("Unexpected token '"+currentToken+" in container '"+container.name+"'")
      }
    }
  }
  
  /**
   * Pre: The last token read was "step".
   * 			step is not null.
   * Post: A new Step object was created with the
   *       info about the step and its contents.
   */
  def processStep(scanner: LexicalScanner, step: Step) {
    var depth = 0
    
    while( scanner.hasNextToken() ) {
      val currentToken = scanner.nextToken()
      currentToken match {
        case "after" => configure(scanner, step.addAfter)
        
        case "using" => configure(scanner, step.addUsing)
        
        case "passing" => configure(scanner, step.addPassing)
        
        case "{" => depth += 1
          
        case "}" => depth -= 1
                    if( depth <= 0 )
                      return
        
        case _ => step.insert(currentToken)
      }
    }
  }
  
  /**
   * Pre: container is not null
   * Post: A new Container object was created with the
   *       info about the process and its contents.
   */
  def processContainer(scanner: LexicalScanner, container: Container) {
    while( scanner.hasNextToken() ) {
      val currentToken = scanner.nextToken()
      currentToken match {
        case "{" => loadContentInto(scanner, container)
                    return //Content has been loaded, processing is complete
                    
        case "using" => configure(scanner, container.addUsing)
          
        case "passing" => configure(scanner, container.addPassing)
          
        case _ => println("processContainerToken is not fully implemented. Skipping info: "+currentToken)
      }
    }
  }
  
  /**
   * Pre: The last token read was "passing", "using", or "after"
   * 			configureFunc should modify a Container or a Step by passing single tokens.
   * Post: Each token separated by a comma has been passed to configureFunc
   */
  def configure(scanner: LexicalScanner, configureFunc: (String) => Unit ) {
    var tokensToProcess = 1
    var varType = ""
    while( scanner.hasNextToken() ) {
      //Keep processing if there is a comma indicating there are more tokens
      if( tokensToProcess == 0 ) {
        if( scanner.lookAtNextToken() == "," ) {
          tokensToProcess = 1
          scanner.nextToken() //Skip the comma when processing in the next iteration
        } else {
          return
        }
      } else {
        val currentToken = scanner.nextToken()
        currentToken match {
          case "," => tokensToProcess = 2
          case _ => configureFunc(currentToken)
                    tokensToProcess -= 1
        }
      }
    }
  }
}