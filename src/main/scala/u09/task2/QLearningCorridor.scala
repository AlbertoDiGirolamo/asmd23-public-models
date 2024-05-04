package scala.u09.task2

import u09.model.QMatrix

object TryQMatrix extends App :

  import u09.model.QMatrix.Move.*
  import u09.model.QMatrix.*

  def obstacles: Set[(Int, Int)] = Set((2,1),(5,2))

  val corridor: QMatrix.Facade = Facade(
    width = 10,
    height = 4,
    initial = (0,1),// column 0, row 1
    terminal = {case (10,1)=>true
                case _ => false},
    reward = {case ((1,1),RIGHT) => -10;
              case ((2,2),UP) => -10;
              case ((4,2),RIGHT) => -10;
              case ((5,1),DOWN) => -10;
              case ((_,0),RIGHT) => -10;
              case ((_,3),RIGHT) => -10;
              case ((_,1),RIGHT) => 10;
              case ((_,2),RIGHT) => 10;
              case ((_,1),LEFT) => 0;
              case _ => -1;     },
    jumps = { PartialFunction.empty },
    //jumps = { case ((x,y),_) => (x,y)},
    gamma = 0.9,
    alpha = 0.5,
    epsilon = 0.3,
    v0 = 1
  )


  val q0 = corridor.qFunction
  println(corridor.show(q0.vFunction,"%2.2f"))
  val q1 = corridor.makeLearningInstance().learn(10000,100,q0) //10000 episodes, 100 max length
  println(corridor.show(q1.vFunction,"%2.2f"))

  val bestPolicy = corridor.showPath(s => q1.bestPolicy(s).toString, "%7s", obstacles)
  println(bestPolicy)