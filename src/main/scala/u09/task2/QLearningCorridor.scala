package scala.u09.task2


object TryQMatrix extends App :

  import scala.u09.task2.ExtendedQMatrix
  import scala.u09.task2.ExtendedQMatrix.Facade
  import scala.u09.task2.ExtendedQMatrix.Move.*

  def obstacles: Set[(Int, Int)] = Set((2,1), (5,2))

  val corridor: ExtendedQMatrix.Facade = Facade(
    width = 10,
    height = 5,
    initial = (0,1),// column 0, row 1
    terminal = {case _=>false},
    reward = {case (s, _) if obstacles.contains(s) => -10;
              case ((_,0),RIGHT) => -10;
              case ((_,4),RIGHT) => -10;
              case ((9, 1), RIGHT) => 10;
              case _ => 0;     },
    obstacles = obstacles,
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

//  val bestPolicy = corridor.showPath(s => q1.bestPolicy(s).toString, "%7s", obstacles)
//  println(bestPolicy)
  println(corridor.show(s => if corridor.obstacles.contains(s) then "*" else q1.bestPolicy(s).toString,"%7s"))