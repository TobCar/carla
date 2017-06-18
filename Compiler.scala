package carla

import java.io.{File, FileWriter, BufferedWriter}
import carla.including.OrderableRelationshipActor.createActorName
import javax.swing.JFileChooser

/**
 * Takes .crr files and generates .scala files that can be run in the JVM.
 */
object Compiler {
  
  def main( args: Array[String] ) {
    val selectedInputDirectory = userSelectDirectory("Select directory to compile from")
    val selectedOutputDirectory = userSelectDirectory("Select directory where Scala files will be written")
    
    compileAllFilesAt(selectedInputDirectory, selectedOutputDirectory)
  }
  
  /**
   * Pre: inputDirectory and outputDirectory are a valid directories
   * Post: All .crr files have been compiled in that directory
   * 			 or any subdirectories it may have
   * 
   * Throws: DirectoryExpectedException if directory is not a valid directory
   */
  def compileAllFilesAt( inputDirectory: File, outputDirectory: File ) {
    if( inputDirectory.isDirectory() == false )
      throw new DirectoryExpectedException("Input path '"+inputDirectory.getAbsolutePath+"' does not point to a directory")
    if( outputDirectory.isDirectory() == false )
      throw new DirectoryExpectedException("Output path '"+outputDirectory.getAbsolutePath+"' does not point to a directory")
    
    for( file <- inputDirectory.listFiles() ) {
      if( file.isDirectory() ) {
        compileAllFilesAt(file, outputDirectory)
      } else {
        //Assumes the file only has one file extension
        val periodIndex = file.getName.indexOf(".")
        if(periodIndex != -1 ) {
          val fileExtension = file.getName.substring(periodIndex)
          if( fileExtension == ".crr" )
            compileFile(file, outputDirectory)
        }
      }
    }
  }
  
  /**
   * Pre: file is a real file with the .crr extension
   * 			outputDirectory is a valid directory where files can be written.
   * Post: A Scala file has been created using the content of the .crr file.
   */
  def compileFile( file: File, outputDirectory: File ) {
    if( !outputDirectory.isDirectory() )
      throw new DirectoryExpectedException("Output path '"+outputDirectory.getAbsolutePath+"' does not point to a directory")
    
    val periodIndex = file.getName.indexOf(".")
    if(periodIndex != -1 ) {
      val fileExtension = file.getName.substring(periodIndex)
      if( fileExtension != ".crr" ) {
        throw new IllegalFileExtensionException("Expected '.crr' file, received '"+fileExtension+"'")
      }
      
      val scanner = new LexicalScanner()
      scanner.readFile(scala.io.Source.fromFile(file).iter)
      val superContainer = Container.createSuperContainer()
      loadContentInto(scanner, superContainer)
      ScalaWriter.createScalaFilesFrom(superContainer, outputDirectory)
    }
  }
  
  /**
   * Pre: There was just an open bracket "{" and there is a closing
   * 			bracket "}" to match it OR it is the start of the file.
   * 
   * Throws: UnexpectedTokenException if the token is not a keyword or
   * 				 a bracket.
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
        case "run" => val processToRun = new ProcessToRun(scanner.nextToken, createActorName(container.name))
                      processRunProcess(scanner, processToRun)
                      container.insert(processToRun)
        case "}" => depth -= 1
                    if( depth == 0 )
                      return
        case _ => throw new UnexpectedTokenException("Unexpected token '"+currentToken+" in container '"+container.name+"'")
      }
    }
  }
  
  /**
   * Pre: The second last token read was "step".
   * 			step is not null.
   * Post: step contains any code that was within brackets as tokens.
   * 			 step has been configured for the keywords "after",
   * 			 "using", and "passing".
   */
  def processStep(scanner: LexicalScanner, step: Step) {
    if( step == null ) {
      throw new NullArgumentException("step cannot be null")
    }
    
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
   * Pre: The second last token read was "run".
   * 			processToRun is not null.
   * Post: processToRun has been configured for the keywords "after",
   * 			 "using", and "passing".
   * 			 processToRun contains a single token / line of code that runs
   * 			 the function to activate the process named processToRun.name
   */
  def processRunProcess(scanner: LexicalScanner, processToRun: ProcessToRun) {
    if( processToRun == null ) {
      throw new NullArgumentException("processToRun cannot be null")
    }
    
    while( scanner.hasNextToken() ) {
      val currentToken = scanner.lookAtNextToken()
      currentToken match {
        case "after" => scanner.nextToken()
                        configure(scanner, processToRun.addAfter)
        
        case "using" => scanner.nextToken()
                        configureWithPair(scanner, processToRun.addUsing)
        
        case "passing" => scanner.nextToken()
                          configureWithPair(scanner, processToRun.addPassing)
        
        case _ => return
      }
    }
  }
  
  /**
   * Pre: container is not null
   * Post: A new Container object was created with the
   *       info about the process and its contents.
   */
  def processContainer(scanner: LexicalScanner, container: Container) {
    if( container == null ) {
      throw new NullArgumentException("container cannot be null")
    }
    
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
   * Pre: There is a single token between commas
   * 			configureFunc should modify a Container or an Orderable by passing single tokens.
   * Post: Each token separated by a comma has been passed to configureFunc
   */
  private def configure(scanner: LexicalScanner, configureFunc: (String) => Unit ) {
    var tokensToProcess = 1

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
  
  /**
   * Pre: There is a single token between commas
   * 			configureFunc should modify a Container or an Orderable by passing single tokens.
   * Post: Tokens representing each pair separated by commas have been passed to configureFunc
   * 
   * Throws: UnexpectedTokenException if the next 5 tokens did not follow the expected pattern
   * 				 of "(" "any string" "->" "any string" ")"
   */
  private def configureWithPair(scanner: LexicalScanner, configureFunc: (String) => Unit ) {
    var tokensToProcess = 5
    var pairToPass = ""
    
    while( scanner.hasNextToken() ) {
      //Keep processing if there is a comma indicating there are more tokens
      if( tokensToProcess == 0 ) {
        if( scanner.lookAtNextToken() == "," ) {
          tokensToProcess = 5
          pairToPass = ""
          scanner.nextToken() //Skip the comma when processing in the next iteration
        } else {
          return
        }
      } else {
        tokensToProcess -= 1
        val currentToken = scanner.nextToken()
        currentToken match {
          case "(" => if( tokensToProcess != 4 ) {
                        throw new UnexpectedTokenException("Unexpected token '('")
                      }
                      
          case "->" =>  if( tokensToProcess != 2 ) {
                          throw new UnexpectedTokenException("Unexpected token '->'")
                        } else {
                          pairToPass += " "
                        }
          
          case ")" => if( tokensToProcess != 0 ) {
                        throw new UnexpectedTokenException("Unexpected token ')'")
                      } else {
                        configureFunc(pairToPass)
                      }
          
          case _ => pairToPass += currentToken
        }
      }
    }
  }
  
  /**
   * Let the user pick a directory or exit the program if the user does not pick one.
   * 
   * Returns: A File object with a path to a directory.
   */
  def userSelectDirectory(title: String): File = {
    var output: File = null
    EventQueue.invokeAndWait((new Runnable() {
      override def run() {
        val chooser = new javax.swing.JFileChooser(System.getProperty("user.home")+"/Desktop")
        chooser.setDialogTitle(title)
        chooser.setApproveButtonText("Select Directory")
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        chooser.setAcceptAllFileFilterUsed(false)
        if( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ) {
          println("User selected directory: "+chooser.getSelectedFile.getAbsolutePath)
          output = chooser.getSelectedFile
        } else {
          println("Stopped compiling. User did not pick a directory.")
          System.exit(1)
        }
      }
    }))
    output
  }
}