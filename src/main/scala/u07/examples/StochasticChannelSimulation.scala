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
  //println(averageTimeStochasticChannel(10))
  println(relativeFailTime(10))




def averageTimeStochasticChannel(nRun: Int) : Double =
  (0 to nRun).foldLeft(0.0)((z, t) =>
    z + stocChannel.newSimulationTrace(IDLE, new Random)
    .take(10)
    .toList
    .find(e => e._2 == DONE).map(e => e.time).getOrElse(0.0))/nRun

def relativeFailTime(nRun: Int): Double =
  val totalTimes = (0 to nRun).foldLeft((0.0, 0.0)) ((acc, _) => {
    val (failTime, totTime) = stocChannel.newSimulationTrace(IDLE, new Random)
      .take(10)
      .toList
      .sliding(2)
      .foldLeft((0.0, 0.0)) ( (z, s) => if (s(0).state == FAIL) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))

    (acc._1 + failTime, acc._2 + totTime)
  })

  totalTimes._1 / totalTimes._2
