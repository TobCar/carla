package carla

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class LexicalScanner {
  //Constant Declarations
  val Spacing = "[ \11\n]".r
  val ValidKeyword = "[a-zA-Z_0-9\\.]".r //Also applies to names and variable types
  val ValidSpecialCharacter = "[\\[\\](),]".r
  val ValidSpecialToken = "[->]{1,2}".r
  
  //Variable Declarations
  var currentChar: Char = '\u0000'
  var waitingForBracket = false
  
  var tokens = new ArrayBuffer[String]()
  var currentTokenIndex = 0;

  /**
   * Split up a file into tokens that can be read with nextToken() and hasNextToken() 
   */
  def readFile(fileName: String) {
    readFile(Source.fromFile(fileName).iter)
  }
  
  /**
   * Split up a file into tokens that can be read with nextToken() and hasNextToken() 
   */
  def readFile(lineIterator: Iterator[Char]) {
    //Make sure currentChar isn't starting as null
    getChar(lineIterator)
    
    //Process the characters
    while( currentChar != '\u0000' ) {
       currentChar match {
         case '{' => saveCharacter('{', lineIterator)
                     if( waitingForBracket ) {
                       waitingForBracket = false
                       saveToken(readAllContentInBrackets(lineIterator))
                     }
         case '}' => saveCharacter('}', lineIterator)
         case ValidKeyword(_*) => readKeyword(lineIterator)
         case ValidSpecialCharacter(_*) => saveCharacter(currentChar, lineIterator)
         case Spacing(_*) => skipSpacing(lineIterator)
         case ValidSpecialToken(_*) => readSpecialToken(lineIterator)
         case '\57' => foundForwardSlash(lineIterator)
         case _ => throw new UnsupportedOperationException("Could not match character: " + currentChar)
       }
    }
  }
  
  /**
   * Post: A new token has been saved.
   */
  def readKeyword(lineIterator: Iterator[Char]) {
    val keyword = readToken(ValidKeyword.toString(), lineIterator)
    saveToken(keyword)
    if( keyword == "step" || keyword == "last" ) {
      waitingForBracket = true
    } else if( keyword == "import" ) {
      saveUntil('\n', lineIterator)
    }
  }
  
  /**
   * Post: A token has been saved containing every character until toStop was encountered
   * 			 or there was nothing left to read.
   */
  def saveUntil(toStop: Character, lineIterator: Iterator[Char]) { 
    var currentToken = ""
    do {
      currentToken += currentChar
      getChar(lineIterator)
    } while( currentChar != toStop && currentChar != '\u0000' )
    saveToken(currentToken)
  }
  
  /**
   * Post: A new token has been saved.
   */
  def readSpecialToken(lineIterator: Iterator[Char]) {
    val specialToken = readToken(ValidSpecialToken.toString(), lineIterator)
    saveToken(specialToken)
  }
  
  /**
   * Post: char has been saved to the scanner's list of tokens
   */
  def saveCharacter( char: Character, lineIterator: Iterator[Char] ) {
    saveToken(char.toString())
    getChar(lineIterator)
  }
  
  /**
   * Pre: The last character read and saved was '{'
   * Post: All characters are loaded until there is a '}' for every '{'
   */
  def readAllContentInBrackets(lineIterator: Iterator[Char]): String = {
    //Read the character after the '{' and check if there is content to load.
    getChar(lineIterator)
    if( currentChar == '}' ) {
      ""
    } else {
      var depth = 1
      var allContent = ""
      while( depth > 0 ) {        
        if( currentChar == '{' ) {
          depth += 1
        } else if( currentChar == '}' ) {
          depth -= 1
        }
  
        if( depth > 0 ) {
          allContent += currentChar
          getChar(lineIterator)
        }
      }
      allContent
    }
  }
  
  /**
   * Pre: lineIterator is not null
   * Returning: A string that matches regexString
   */
  def readToken( regexString: String, lineIterator: Iterator[Char] ): String = {
    var currentToken = ""
    do {
      currentToken += currentChar
      getChar(lineIterator)
    } while( currentChar.toString().matches(regexString) )
    currentToken
  }
  
  /**
   * Pre: currentChar == '/' and lineIterator is not null
   * Post: The contents of the comment are skipped.
   */
  def foundForwardSlash( lineIterator: Iterator[Char] ) {
    if( currentChar != '\57' ) {
      println("ERROR: currentChar != '/'")
    } else {
      val prevChar = '\57'
      getChar(lineIterator)
      currentChar match {
        case '=' => saveToken("\57\75")
        case '*' => skipTo('*', '/', lineIterator)
        case '/' => skipTo('\n', lineIterator)
        case _ => println("Could not match character after forward slash: " + currentChar)
      }
    }
  }
  
  /**
   * Pre: String is not empty
   * Post: token has been stored to be sent to the compiler later
   */
  def saveToken( token: String ) {
    tokens += token
  }
  
  
  /**
   * Pre: currentChar == '*' and lineIterator is not null
   * Post: currentChar is the character after end1 and end2 appear one after
	 *       another through getChar()
   */
  def skipTo( end1: Char, end2: Char, lineIterator: Iterator[Char] ) {
    getChar(lineIterator)
    var prevChar = '\u0000'
    do {
      prevChar = currentChar
      getChar(lineIterator)
    } while( prevChar != end1 && currentChar != end2 )
    getChar(lineIterator)
  }
  
  /**
   * Pre: lineIterator is not null
   * Post: currentChar is the character after end appears through getChar()
   */
  def skipTo( end: Char, lineIterator: Iterator[Char] ) {
    while( currentChar != end ) {
      getChar(lineIterator)
    }
    getChar(lineIterator)
  }
  
  /**
   * Pre: spacingRegexString is not empty or null and lineIterator is not null
   * Post: spacingRegexString does not match currentChar.
   * 			 currentChar will equal null (\u0000) if there is nothing left to read
   */
  def skipSpacing(lineIterator: Iterator[Char]) {
    val spacingRegexString = Spacing.toString()
    while( currentChar.toString().matches(spacingRegexString) ) {
      getChar(lineIterator)
    }
  }
  
  /**
   * Get the next character through the iterator
   * Pre: lineIterator is not null
   * Post: currentChar is a non-null character unless there is nothing left to read.
   */
  def getChar(lineIterator: Iterator[Char]) {
    if( lineIterator.hasNext )
        currentChar = lineIterator.next()
    else
        currentChar = '\u0000'
  }
  
  /**
   * Returns: True if nextToken() has more tokens to output.
   */
  def hasNextToken(): Boolean = {
    currentTokenIndex < tokens.size
  }
  
  /**
   * Access the tokens sequentially in the order they were in the original file.
   * Post: Calling nextToken() or lookAtNextToken() will return the next stored token.
   */
  def nextToken(): String = {
    val toReturn = tokens(currentTokenIndex)
    currentTokenIndex += 1
    toReturn
  }
  
  /**
   * Returns: What nextToken() will return if called
   */
  def lookAtNextToken(): String = {
    tokens(currentTokenIndex)
  }
}