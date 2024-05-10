package scala.u09.task2


object QLearningCorridor extends App :

  import scala.u09.task2.ExtendedQMatrix
  import scala.u09.task2.ExtendedQMatrix.Facade
  import scala.u09.task2.ExtendedQMatrix.Move.*

  def obstacles: Set[(Int, Int)] = Set((2,1), (5,2))

  val rlCorridor: ExtendedQMatrix.Facade = Facade(
    width = 10,
    height = 5,
    initial = (0,1),// column 0, row 1
    terminal = {case _=>false},
    reward = {case (s, _) if obstacles.contains(s) => -10;
              case ((_,0),RIGHT) => -10;
              case ((_,4),RIGHT) => -10;
              case ((9, 1), RIGHT) => 10;
              case _ => 0;     },
    itemsToCollect = Set.empty,
    obstacles = obstacles,
    jumps = { PartialFunction.empty },
    //jumps = { case ((x,y),_) => (x,y)},
    gamma = 0.9,
    alpha = 0.5,
    epsilon = 0.3,
    v0 = 1,
    resetMap = () => (),
  )


  val q0 = rlCorridor.qFunction
  println(rlCorridor.show(q0.vFunction,"%2.2f"))
  val q1 = rlCorridor.makeLearningInstance().learn(10000,100,q0) //10000 episodes, 100 max length
  println(rlCorridor.show(q1.vFunction,"%2.2f"))

//  val bestPolicy = corridor.showPath(s => q1.bestPolicy(s).toString, "%7s", obstacles)
//  println(bestPolicy)
  println(rlCorridor.show(s => if rlCorridor.obstacles.contains(s) then "*" else q1.bestPolicy(s).toString,"%7s"))