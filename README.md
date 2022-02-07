# consensus-analyzer

TODO: add a brief description of the tool.
This repo contains code for analyzation consensus protocols.
In the output one can see the probability and expected number of messages for different combinations of 
backward transitions.

## Installation instructions

Download a copy of PRISM and build it

* ``git clone https://github.com/prismmodelchecker/prism prism``
* ``cd prism/prism``
* ``make``


Download the ``consensus-analyzer`` repo and build the examples

* ``cd ../..``
* ``git clone https://github.com/prismmodelchecker/prism-api``
* ``cd consensus-analyzer``
* ``make``

The second part of the above assumes that PRISM is in a directory called ``prism`` one level up.
If you want to use a PRISM distribution located elsewhere, build like this:

* ``make PRISM_DIR=/some/copy/of/prism``

## Usage instructions

Run ``bin/run``with parameters: 
* path to configuration file
* path to the output file
* flag true if running with backward transitions, false otherwise

Configuration example: ./resources/cnf_simple.yaml

Run example: bin/run ./resources/cnf_simple.yaml resources true

TODO:
* split configuration file on distinct 2 files: for probability and for specification - Folu
* for each backward transition create a separate .dot representation
* how to add prism precision
* figure out how to connect prism with maven and build the project as a single jar with fat jar
* visualization