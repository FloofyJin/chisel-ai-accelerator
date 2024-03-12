Chisel Project Template
=======================

## Overview

For the purposes of this project, we are builder a particular segment of what makes ai accelerators special in modern chip. We took inspiration from [UC Berkely Gemmini](https://github.com/ucb-bar/gemmini) and [Eyeriss project from MIT](https://eyeriss.mit.edu/#websites). We will create a generator for DNN accelerators, mainly comprised of a spatial array implementation. This would be an implementation from a broader class of AI accelerators particularly for deep learning.

![aReference](https://github.com/ucb-bar/gemmini/blob/master/img/gemmini-system.png)

## Parameter

The main parameters for the generators would be the systolic array parameters - particularly the dimensions. The system will be able to interact with other components via a DMA controller for its memory banks. To allow us to focus on the important matters at hand, we will assume that matrixes will be of size n*n. By this, we mean there are same number of rows and columns.

## Plans and deliverables

Our first goal would be to get the systolic arrays to get matrix multiplications working.
Then eventually we would like to add additional features, such as quantization, inference engines, MACs, and possibly use an architecture for energy efficiency such as data reuse, gating, and compression. We would run the project via simulation.

If we have enough time, we would like to explore more domain specific DNN accelerators such as Binary neural networks like XNOR Net with special operations.

## Currrent Progress

### Done ✅
- Matrix multiplication using systolic array in Scala
  - `SystolicModel.scala`
  - Able to calculate dot multiplication of fixed size n*n


### Almost Done 🏗
- Testing of Scala functionality and cleaning up code for readability(**Working**)


### Work In Progress 🚧
- Translation of systolic array model in chisel
- delve into processing units with unique operations

## JDK 8 or newer

We recommend LTS releases Java 8 and Java 11. You can install the JDK as recommended by your operating system, or use the prebuilt binaries from [AdoptOpenJDK](https://adoptopenjdk.net/).

##  Testing

We recommend sbt

This program was mostly run on sbt. to test run ```sbt test```

## References
[Chisel Bootcamp](https://github.com/freechipsproject/chisel-bootcamp)
[Chisel3](https://www.chisel-lang.org/)
[structure](https://www.scala-sbt.org/1.x/docs/Directories.html) and [naming](http://docs.scala-lang.org/style/naming-conventions.html)
[SBT docs](https://www.scala-sbt.org/1.x/docs/Testing.html)
[chiseltest](https://github.com/ucb-bar/chisel-testers2)
