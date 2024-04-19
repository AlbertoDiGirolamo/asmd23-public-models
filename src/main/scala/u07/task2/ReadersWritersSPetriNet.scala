package scala.u07.task2

import u07.modelling.SPN
import u07.modelling.SPN.*
import u07.utils.MSet

import java.util.Random

object ReadersWritersSPetriNet:
  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

  val spnReadersWriters = SPN[Place](
    Trn(MSet(Idle), m => 1 , MSet(ChooseAction), MSet()),
    Trn(MSet(ChooseAction), m => 200000 , MSet(ReadyToRead), MSet()),
    Trn(MSet(ChooseAction), m => 100000 , MSet(ReadyToWrite), MSet()),
    Trn(MSet(ReadyToRead, HasPermission), m => 100000 , MSet(Reading, HasPermission), MSet()),
    Trn(MSet(Reading), m => 0.1 * m(Reading) , MSet(Idle), MSet()),
    Trn(MSet(ReadyToWrite, HasPermission), m => 100000 , MSet(Writing), MSet(Reading)),
    Trn(MSet(Writing), m => 0.2 , MSet(Idle, HasPermission), MSet())
  )

  def averageTimeInReadersWritersState(nRun: Int, initSet: MSet[Place], stateToCheck: Place) : Double =
    val totalTimes = (0 to nRun).foldLeft((0.0, 0.0))((acc, _) => {
      val (rwTime, totTime) = toCTMC(spnReadersWriters).newSimulationTrace(initSet, new Random)
        .take(10)
        .toList
        .sliding(2)
        .foldLeft((0.0, 0.0)) ( (z, s) =>  if (s(0).state(stateToCheck) > 0) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))
      (acc._1 + rwTime, acc._2 + totTime)
    })
   
    totalTimes._1 / totalTimes._2

  @main def mainSPNReadersWriters =
    val initialMSet = MSet().fill(2)(Idle) union MSet().fill(1)(HasPermission)
    /*val execution = toCTMC(spnReadersWriters).newSimulationTrace(initialMSet, new Random)
      .take(10)
      .toList
    execution.foreach(println)*/
    println(averageTimeInReadersWritersState(10, initialMSet, Place.Reading))
    println(averageTimeInReadersWritersState(10, initialMSet, Place.Writing))