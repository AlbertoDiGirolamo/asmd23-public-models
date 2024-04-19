package u07.examples

import u07.utils.Time

import java.util.Random
import u07.examples.StochasticChannel.*
export u07.modelling.CTMCSimulation.*

@main def mainStochasticChannelSimulation =
  println(stocChannel.averageTimeToReachState(10, IDLE, DONE))
  println(stocChannel.relativeTimeInState(10, IDLE, FAIL))

  /*Time.timed:
    println:
      stocChannel.newSimulationTrace(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")

   */


