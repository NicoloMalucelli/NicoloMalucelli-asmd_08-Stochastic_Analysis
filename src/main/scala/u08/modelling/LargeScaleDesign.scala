package scala.u08.modelling

import u08.modelling.DAP.Token
import u08.modelling.{DAP, DAPGrid}

import scala.u08.utils.{Grids, MSet}
import java.util.Random
import scala.u08.modelling.LargeScaleDesign.{Place, gossipCTMC, state}

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

  val rows = 5
  val cols = 5

  val gossipCTMC = DAP.toCTMC[ID, Place](rules)
  val net = Grids.createRectangularGrid(rows, cols)
  // an `a` initial on top left
  val state = State[ID, Place](MSet(Token((0, 0), S), Token((0, 0), M1), Token((rows-1, cols-1), T)), MSet(), net)

@main def mainDAPGossip =
  import LargeScaleDesign.Place.*

  gossipCTMC.newSimulationTrace(state, new Random).takeWhile(e  => {
    !(e._2.tokens matches MSet(Token((0, 0), R2)))
  }).toList.foreach(step =>
    println(step._1) // print time
    println(DAPGrid.simpleGridStateToString2[Place](step._2, M1, M2)) // print state, i.e., A's
  )