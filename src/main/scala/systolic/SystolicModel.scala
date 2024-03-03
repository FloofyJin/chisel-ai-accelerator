/*
 * File: SystolicModel.scala
 * Author: Rian Borah, Jinsung Park
 *
 * Date: 1 March, 2024
 *
 * Main file for the Systolic Array Scala Model Implementation for
 * CSE-228A Final Project - Winter 2024
 */

/*******************************************************************************
 * MODULE #INCLUDE                                                             *
 ******************************************************************************/
package systolic

import scala.collection.mutable.ArrayBuffer

/*******************************************************************************
 * PROCESSING ELEMENT MODULE DEFINITION                                        *
 ******************************************************************************/

/*
* @class ProcessingElement
* @param operation: operation to be performed by the PE on the inputs
* @return:
* @brief: Processing Element Module to perform computations in the systolic array
*
* @author: Rian Borah, 2024.03.02
* */

// Could change from operation on Ints to UInts for chisel
class ProcessingElementModel(operation: (Int, Int) => Int){
    private var value: Int = 0
    def execute(inputA: Int, inputB: Int): Int = {
        value = value + operation(inputA, inputB)
        value
    }
    def getValue: Int = {
        value
    }
}

/*******************************************************************************
 * SYSTOLIC ARRAY MODULE DEFINITION                                            *
 ******************************************************************************/

/*
* @class SystolicArrayParams
* @param rows: No. of rows in the systolic array
* @param cols: No. of columns in the systolic array
* @param peOperation: operation to be performed by the PE on the inputs
* @return:
* @brief: Parameters for the Systolic Array Module
*
* @author: Rian Borah, 2024.03.02
* */
class SystolicArrayParams(
    val rows: Int,
    val cols: Int,
    val peOperation: (Int, Int) => Int,
)

/*
* @class SystolicArrayModel
* @param p: SystolicArrayParams
* @return:
* @brief: Scala Model for the Systolic Array
*
* @author: Rian Borah, 2024.03.02
* */
class SystolicArrayModel(p: SystolicArrayParams) {
    // Initialize a 2D array of Processing Element Models
    private val peGrid: ArrayBuffer[ArrayBuffer[ProcessingElementModel]] =
      ArrayBuffer.fill(p.rows, p.cols)(new ProcessingElementModel(p.peOperation))

    val resultGrid: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)
    val rowGrid: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)
    val columnGrid: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)

    /*
    * @function performOperation
    * @param inputA: Row from input Matrix A
    * @param inputB: Column from input Matrix B
    * @return: resultGrid
    * @brief: Perform the operation on the inputs using the systolic array
    *
    * @author: Rian Borah, 2024.03.02
    * */
    def performOperation(i: Int, inputA: ArrayBuffer[ArrayBuffer[Int]], inputB: ArrayBuffer[ArrayBuffer[Int]])= {
        require(inputA.length == p.rows && inputB.length == p.cols, "Input dimensions must match the systolic array dimensions.")

        // shift value right or down
        for (i <- p.rows-1 to 0 by -1) {
            for (j <- p.cols -1 to 0 by -1) {
                if(j < p.cols-1){//shift right
                    rowGrid(i)(j+1) = rowGrid(i)(j)
                }
                if(i < p.rows-1){//shift down
                    columnGrid(i+1)(j) = columnGrid(i)(j)
                }
            }
        }
        // add value to beginning of row or column
        for(r <- 0 until p.rows){
            if(i >= r && i < p.cols+r){
                rowGrid(r)(0) = inputA(r)(p.cols-1-i+r)
            }else{
                rowGrid(r)(0) = 0
            }
        }
        for(c <- 0 until p.cols){
            if(i >= c && i < p.rows+c){
                columnGrid(0)(c) = inputB(p.rows-1-i+c)(c)
            }else{
                columnGrid(0)(c) = 0
            }
        }
        
        // rowGrid.foreach(row => println(row.map(i=>i).mkString(" ")))//inputA
        // columnGrid.foreach(row => println(row.map(i=>i).mkString(" ")))//inputB
        // println()

        // Perform the operation
        for (i <- 0 until p.rows) {
            for (j <- 0 until p.cols) {
                peGrid(i)(j).execute(rowGrid(i)(j), columnGrid(i)(j));
            }
        }
    }

    /*
    * @function displayGrid
    * @param: None
    * @return: None
    * @brief: Display the current state of the systolic array
    *
    * @author: Rian Borah, 2024.03.02
    * */
    def displayGrid(): Unit = {
        println("Systolic Array State:")
        peGrid.foreach(row => println(row.map(_.getValue).mkString(" ")))
    }
}

object SystolicArrayModel {
    def apply(params: SystolicArrayParams): SystolicArrayModel = new SystolicArrayModel(params)
}

object SystolicArrayMatrixMult extends App {
    type Matrix = ArrayBuffer[ArrayBuffer[Int]]

    def apply(n: Int, inputA: Matrix, inputB: Matrix) = {
        val params = new SystolicArrayParams(n, n, (a: Int, b: Int) => a * b)

        val systolicArrayModel = SystolicArrayModel(params)

        for (i <- 0 until n*2+1) {
            systolicArrayModel.performOperation(i, inputA, inputB)
        }
        systolicArrayModel.displayGrid()
    }
}