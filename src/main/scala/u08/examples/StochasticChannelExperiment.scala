package u08.examples

import scala.math.BigDecimal.double2bigDecimal
import scala.u08.utils.Time
import breeze.linalg.*
import breeze.plot.*

object StochasticChannelExperiment extends App with de.sciss.chart.module.Charting:
  import u08.modelling.CTMCExperiment.*
  import u08.examples.StochasticChannel.*

  val data =
    for
      t <- 0.1 to 10.0 by 0.1
      p = stocChannel.experiment(
        runs = 26000,
        prop = stocChannel.eventually(_ == DONE),
        s0 = IDLE,
      timeBound = t.toDouble)
    yield (t, p)

  val fig = Figure()
  val plt = fig.subplot(0)
  plt.setYAxisDecimalTickUnits()

  plt += breeze.plot.plot(
    DenseVector(data.map(_._1.toDouble).toArray),
    DenseVector(data.map(_._2).toArray),
    name="P")
  fig.refresh()