package u07.examples

import u07.utils.Time

import java.util.Random
import u07.examples.StochasticChannel.{stocChannel, *}

@main def mainStochasticChannelSimulation =
  /*Time.timed:
    println:
      stocChannel.newSimulationTrace(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")*/
  println(averageTimeStochasticChannel(10))


def averageTimeStochasticChannel(nRun: Int) : Double =
  (0 to nRun).foldLeft(0.0)((z, t) =>
    z + stocChannel.newSimulationTrace(IDLE, new Random)
    .take(10)
    .toList
    .find(e => e._2 == DONE).map(e => e.time).getOrElse(0.0))/nRun

def percentageTimeFailStatusStochasticChannel(nRun: Int) : Double = ??? //forse .next



