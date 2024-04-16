package scala.u06.task1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.shouldBe

class ReadersWritersTest extends AnyFunSuite:

  import scala.u06.task1.ReadersWritersPetriNet.*


  test("Mutual exclusion"):
    isMutuallyExclusive(MSet(Idle, Idle, HasPermission), 10) shouldBe true

  test("Not in mutual exclusion"):
    isMutuallyExclusive(MSet(Idle, Idle, Reading, Writing), 10) shouldBe false

  test("Reachability"):
    isReachable(MSet(Idle, Idle, HasPermission), 10) shouldBe true

  test("Boundness"):
    isBounded(MSet(Idle, Idle, HasPermission), 10) shouldBe true

