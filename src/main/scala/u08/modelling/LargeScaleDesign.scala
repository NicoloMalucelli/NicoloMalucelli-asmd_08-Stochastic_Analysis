package scala.u08.modelling

import u08.modelling.DAP.{State, Token}
import u08.modelling.{CTMC, DAP, DAPGrid}

import scala.u08.utils.{Grids, MSet}
import java.util.Random
import scala.u08.modelling.LargeScaleDesign.{ID, Place, rules}

object LargeScaleDesign:
  enum Place:
    case S /*source*/, T /*target*/, M1/*source->target msg*/, M2/*target->source msg*/, R1/*M1 elaborated*/, R2/*M2 elaborated*/

  type ID = (Int, Int)
  export Place.*
  export u08.modelling.DAP.*
  export u08.modelling.DAPGrid.*
  export u08.modelling.CTMCSimulation.*

  val rules = DAP[Place](
    Rule(MSet(S, M1), m => 1, MSet(S, R1), MSet(R1, T), MSet(M1)), // start the communication
    Rule(MSet(M1), m => 1, MSet(R1), MSet(R1, T, S), MSet(M1)), // forward message M1
    Rule(MSet(T, M1), m => 1, MSet(T, R1), MSet(R1), MSet(M2)), // M1 message arrived at destination, forward M2 message
    Rule(MSet(M2), m => 1, MSet(R2), MSet(R2, T, S), MSet(M2)), // forward message M2
    Rule(MSet(M2, S), m => 1, MSet(S, R2), MSet(R2), MSet()), // forward message M2

    Rule(MSet(M1, M1), m => 100000, MSet(M1), MSet(), MSet()), // destroy messages in surplus
    Rule(MSet(M2, M2), m => 100000, MSet(M2), MSet(), MSet()), // destroy messages in surplus
  )

@main def mainDAPGossip =
  import LargeScaleDesign.Place.*

  val n = 10
  val results = (2 to 7).map(k  =>
    val gossipCTMC = DAP.toCTMC[ID, Place](rules)
    val net = Grids.createRectangularGrid(k, k)
    val state = State[ID, Place](MSet(Token((0, 0), S), Token((0, 0), M1), Token((k - 1, k - 1), T)), MSet(), net)

    print(k + ": ")
    k ->
    (0 until n).map(
      i =>
        print(i + "/" + n + ", " + (if (i == n-1) then "\n" else " "))
        gossipCTMC.newSimulationTrace(state, new Random).takeWhile(e => {
          !(e._2.tokens matches MSet(Token((0, 0), M2)))
        }).toList.last._1
    ).sum / n
  )

  println(results)

@main def plotResults =
  val data = Vector(
    (2,2.7539066286242266),
    (3,5.688126971949448),
    (4,7.227842771983525),
    (5,8.893584430366783),
    (6,11.675887325007295),
    (7,12.827257091973376)
  )

  import breeze.linalg.*
  import breeze.plot.*

  val fig = Figure()
  val plt = fig.subplot(0)

  plt += breeze.plot.plot(
    DenseVector(data.map(_._1.toDouble).toArray),
    DenseVector(data.map(_._2).toArray),
    name = "Communication from (0,0) to (k-1,k-1)")
  fig.refresh()
