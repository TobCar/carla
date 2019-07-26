# Welcome To Carla!

Carla makes it easy to create data flow oriented programs that are run concurrently.
It generates threads which know what threads have to be called after them and what variables need to be passed to them.
The syntax for most of the code run with Carla is the same as Scala code.
Carla simply removes classes, functions, and other OOP features to boil the code down to the control flow.

## Getting Started

All Carla files use the file extension "crr". 
Take a look at carla-example.crr to get a feel of what Carla is like then read the [wiki](https://github.com/TobCar/carla/wiki) to learn the syntax.

### Compiling Carla Files

Clone this repository to your computer or download all the files.
Run Compiler.scala, it will ask you to select the directory containing the Carla (".crr") files.
Then, select the directory where generated Scala files should go.
The name of the directory where files are generated to is also the package name of the generated files.

### Running Compiled Carla Code

Once a Carla file has been compiled into Scala code you can take the generated files and modify them or distribute them as you would with any other Scala file.

## Contributing

Please contribute if you believe Carla can be improved! Just make sure to comment your code.

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details

## The Name

Scala is a portmanteau of the words SCAlable and LAnguage. Carla is a portmanteau of my last name, CARryer, and LAnguage.
It is intended to be a similar name as Scala to show it is related to it and not a language of its own.
