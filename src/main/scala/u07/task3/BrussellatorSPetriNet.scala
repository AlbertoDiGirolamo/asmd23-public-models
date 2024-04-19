package scala.u07.task3

import u07.utils.MSet
import u07.modelling.SPN
import u07.modelling.SPN.toCTMC

import java.util.Random

import org.knowm.xchart.{QuickChart, SwingWrapper, XYChart}


object BrussellatorSPetriNet:
  enum Place:
    case A, B, D, E, X, Y

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

  val spnBrussellator = SPN[Place](
    
    Trn(MSet(A), m => 1 , MSet(X, A), MSet()),
    Trn(MSet(X, X, Y), m => m(Y)*0.6 , MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => m(X)*0.2 , MSet(Y, D, B), MSet()),
    Trn(MSet(X), m => m(X)*0.5 ,MSet(E), MSet())
  )

  val spnBrussellator2 = SPN[Place](
    Trn(MSet(A), m => 1, MSet(X, A), MSet()),
    Trn(MSet(X, X, Y), m => m(Y), MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => m(X)*0.5, MSet(Y, D, B), MSet()),
    Trn(MSet(X), m => m(X)*0.5, MSet(E), MSet())
  )


  @main def mainSPNBrussellator =

    val initialMSet = MSet().fill(1)(A) union MSet().fill(3)(B) union MSet().fill(1)(X) union MSet().fill(1)(Y)union MSet().fill(0)(D)union MSet().fill(0)(E)
    val execution = toCTMC(spnBrussellator2).newSimulationTrace(initialMSet, new Random)
      .take(1000)
      .toList
    execution.foreach(println)

    val times = execution.map(_._1).toArray
    val xCounts = execution.map(_._2.countOccurences(X)).map(_.toDouble).toArray
    val yCounts = execution.map(_._2.countOccurences(Y)).map(_.toDouble).toArray

    val chart = QuickChart.getChart("Brusselator Simulation", "Time", "Count", "X", times, xCounts)
    chart.addSeries("Y", times, yCounts)
    chart.getStyler.setLegendVisible(true)

    new SwingWrapper[XYChart](chart).displayChart().setTitle("Brusselator Simulation")

   /* execution.foreach(s => {
      println("Time: " + s._1)
      println("Number of X: " + s._2.countOccurences(X))
      println("Number of Y: " +s._2.countOccurences(Y))
    })*/
