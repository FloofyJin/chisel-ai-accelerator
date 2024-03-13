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
class ProcessingElementModel(operation: (Int, Int) => Int) {
    private var value: Int = 0

    def execute(inputA: Int, inputB: Int): Int = {
        value = value + operation(inputA, inputB)
        value
    }

    def getValue: Int = {
        value
    }

    // Method to directly update the value of the processing element
    def updateValue(newValue: Int): Unit = {
        value = newValue
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

    private val resultGrid: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)
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
    // New method to simulate waiting for results from upstream PEs before proceeding.
    def performOperation(i: Int, inputA: ArrayBuffer[ArrayBuffer[Int]], inputB: ArrayBuffer[ArrayBuffer[Int]]): Unit = {
        require(inputA.length == p.rows && inputB.length == p.cols, "Input dimensions must match the systolic array dimensions.")

        // Simulating the clock cycles for synchronization by iterating through 'steps'
        for (step <- 0 until (p.rows + p.cols - 1)) {
            // Temporary storage to hold the next state before updating the PE values
            val nextState: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)

            // Iterate over the grid to calculate the next state based on current inputs
            for (i <- 0 until p.rows) {
                for (j <- 0 until p.cols) {
                    // Only update if the PE is supposed to receive new inputs at this step
                    if (i + j == step) {
                        // Here, we're mimicking the behavior of waiting for upstream PEs by only updating
                        // the PEs that are ready to receive and process new data.
                        nextState(i)(j) = peGrid(i)(j).execute(rowGrid(i)(j), columnGrid(i)(j))
                    } else if (i + j < step) {
                        // Keep the previous state for PEs that have already received their inputs
                        nextState(i)(j) = peGrid(i)(j).getValue
                    }
                    // PEs that haven't received their input yet will remain at their initial state (0)
                }
            }

            // After calculating the next state for all PEs, update the grid
            for (i <- 0 until p.rows) {
                for (j <- 0 until p.cols) {
                    peGrid(i)(j).updateValue(nextState(i)(j))
                }
            }

            // You may need to adjust the logic for shifting data right or down here, as this example
            // focuses on the synchronization aspect.
        }

        // Other operations like shifting data right or down should be integrated here
        // following the logic that ensures synchronization and data dependency between PEs.
    }

    // Note: Add an `updateValue` method in the `ProcessingElementModel` class to update the PE's value directly.
    // This is necessary to apply the nextState values to the PEs.


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

    def returnResult(): ArrayBuffer[ArrayBuffer[Int]] = {
        resultGrid
    }
}

object SystolicArrayModel {
    def apply(params: SystolicArrayParams): SystolicArrayModel = new SystolicArrayModel(params)
}

object SystolicArrayMatrixMult extends App {
    type Matrix = ArrayBuffer[ArrayBuffer[Int]]

    def apply(n: Int, inputA: Matrix, inputB: Matrix): Matrix = {
        val params = new SystolicArrayParams(n, n, (a: Int, b: Int) => a * b)

        val systolicArrayModel = SystolicArrayModel(params)

        for (i <- 0 until n*2+1) {
            systolicArrayModel.performOperation(i, inputA, inputB)
        }
        systolicArrayModel.displayGrid()
        systolicArrayModel.returnResult()
    }
}