<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

<!-- This md template has been copied from https://github.com/othneildrew/Best-README-Template -->




<!-- PROJECT LOGO -->
<br />
<div align="center">

<h3 align="center">RDFtoCSV library + command line conversion</h3>

  <p align="center">
    <br />
    <a href="https://github.com/LadyMalande/RDFtoCSV"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://ladymalande.github.io/rdf-to-csv.github.io/">View Live Instance</a>
    ·
   </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

The RDFtoCSV library converts RDF data to CSV on the Web.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

This section should list any major frameworks/libraries used to bootstrap your project. Leave any add-ons/plugins for the acknowledgements section. Here are a few examples.

* [![Next][Java]][Java-url] 17, 19
* Maven

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may give instructions on setting up your project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

Have Maven and Java 17 or 19 on your computer.

Download some test data, for example here: [RDFtoCSVNotes Repository - test data directory](https://github.com/LadyMalande/RDFtoCSVNotes/tree/main/test_data)

Or from this list: 

##### Test data
Here are links to some test data that you can use for trying out the web service:

* [Simpsons family (Turtle)](https://w3c.github.io/csvw/tests/test005.ttl)
* [Events in Brno](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/events_Brno.nt)
* [Lombardy's Payment Portal Tickets](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/lombardia.rdf)
* [Museums in Würzburg](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/museen.n3)
* [Sexes Code List](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/pohlav%C3%AD.nt)
* [Types of trees Code List](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/typy-d%C5%99evin.nt)
* [Types of Work agreement Code List](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/typy-pracovn%C3%ADch-vztah%C5%AF.nt)
* [Dissesto - places, curriencies, sparse data](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/dissesto_2k_triples.nt)
* [Parking Data](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/evaluation/1E%2B-GaragesGeo/parkovaci_garaze_r_n1_t1.rdf)

Then if you do not want to build the latest version of this repository, simply download last JAR version from this webpage: [RDFtoCSV-JAR Repository](https://github.com/LadyMalande/RDFtoCSV-JAR)

Have the test data and the JAR in the same directory.

Now you are ready to proceed to Usage.
#### Own build
If you want to build your own version of JAR, clone the repository:

   ```sh
   git clone https://github.com/LadyMalande/RDFtoCSV.git
   ```

In the repository directory, run (skip the tests for faster packaging):

```shell
 mvn clean package -DskipTests
```

The ready version of the JAR is in **RDFtoCSV/target/RDFtoCSV-1.0/SNAPSHOT.jar**.

### Usage

Enter this into your command line (use any RDF file in correct format):
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.jsonld
```

### Variants to try
Parameters:
* **-p** conversion method (RDF4J/streaming/BigFileStreaming), RDF4J is default
* **-n** either is present (true) or there is no -n in the command (false). If true, the data will be in first normal form. If false, the cells of the table can contain lists of values.
* **-t** either is present (true) or there is no -n in the command (false). If present, data will be converted into X tables. The number depends on the structure of the data and their types. If not present, the data will be converted into one CSV table.
* **-s** Truly streaming Streaming method experience. The data for the conversion are read from the Standard input. Works only with -p streaming.
* **-h** Help. Write out the options.
#### RDF4J method
Compatible RDF formats:
* .jsonld (JSON-LD)
* .nq (N-Quads)
* .nt (N-Triples)
* .rdf (RDF/XML)
* .trig (TriG)
* .ttl (Turtle)
##### 1 Table, First Normal Form, RDF4J method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.jsonld -n
```

##### 1 Table, No First Normal Form, RDF4J method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.jsonld 
```

##### X Tables, First Normal Form, RDF4J method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.jsonld -n -t
```

##### X Tables, No First Normal Form, RDF4J method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.jsonld -t 
```
#### Streaming method (from file)
Only works with **N-Triples** data (.nt). For larger data takes a really long time. Good datasets for trying out are smaller than 100 kB.
##### 1 Table, First Normal Form, Streaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -n
```

##### 1 Table, No First Normal Form, Streaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming 
```

##### X Tables, First Normal Form, Streaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -n -t
```

##### X Tables, No First Normal Form, Streaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -t
```

#### BigFileStreaming method
Only works with **N-Triples** data (.nt). For larger data takes a really long time. Good datasets for trying out are smaller than 100 kB.
##### 1 Table, First Normal Form, BigFileStreaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p BigFileStreaming -n
```

##### 1 Table, No First Normal Form, BigFileStreaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p BigFileStreaming 
```

##### X Tables, First Normal Form, BigFileStreaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p BigFileStreaming -n -t
```

##### X Tables, No First Normal Form, BigFileStreaming method
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p BigFileStreaming -t
```

#### Streaming method -s (from Standard Input)
Only works with **N-Triples** data (.nt). 
For larger data takes a really long time. Good datasets for trying out are smaller than 100 kB.
Now you can input RDF statements in N-Triples format into the terminal.

To end the stream input, write "END" into the terminal.

Or you can feed the program a special file, that is N-Triples file and contains "END" at the last line of the file.
That way you can simulate streaming input without writing out the triples into the terminal.
You can download such a file here: [Work agreements with END at the end](https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/typy-pracovn%C3%ADch-vztah%C5%AF.nt)
##### 1 Table, First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -n
```

##### 1 Table, No First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s  
```

##### X Tables, First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -n -t
```

##### X Tables, No First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -t
```
##### Streaming method -s with feeding changed .nt file to the conversion
##### 1 Table, First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -n < typy-pracovních-vztahů.nt 
```

##### 1 Table, No First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s < typy-pracovních-vztahů.nt   
```

##### X Tables, First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -n -t < typy-pracovních-vztahů.nt 
```

##### X Tables, No First Normal Form, Streaming method -s
```shell
java -jar RDFtoCSV-1.0-SNAPSHOT.jar -f testingInput.nt -p streaming -s -t < typy-pracovních-vztahů.nt 
```
<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- USAGE EXAMPLES -->
## Usage links

Project Link for web service using this library: [https://github.com/LadyMalande/RDFtoCSVWAPI](https://github.com/LadyMalande/RDFtoCSVWAPI)

Project link for web application using the web service: [https://github.com/LadyMalande/rdf-to-csv.github.io](https://github.com/LadyMalande/rdf-to-csv.github.io)


<!-- NEW CONFIGURATION USAGE -->
## New configuration usage
### Build from code
```java
// Build configuration using Builder pattern
AppConfig config = new AppConfig.Builder("input.ttl")
    .parsing("rdf4j")
    .multipleTables(true)
    .firstNormalForm(true)
    .output("output.csv")
    .preferredLanguages("en,cs")
    .columnNamingConvention("Title case")
    .logLevel("INFO")
    .build();

// Create RDFtoCSV with config
RDFtoCSV converter = new RDFtoCSV(config);
converter.convertToZip();
```

### Command line
```shell
java -jar RDFtoCSV.jar -f input.ttl -t -n -p rdf4j -o output.csv 
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Tereza Miklóšová [![LinkedIn][linkedin-shield]][linkedin-url]

Project Link for this library: [https://github.com/LadyMalande/RDFtoCSV](https://github.com/LadyMalande/RDFtoCSV)

<p align="right">(<a href="#readme-top">back to top</a>)</p>




<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/tereza-miklosova/
[Java]: https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white
[Java-url]: https://www.java.com/en/



