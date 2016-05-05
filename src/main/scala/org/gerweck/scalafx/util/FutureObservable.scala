package org.gerweck.scalafx.util

import scala.concurrent._
import scala.util._

import org.log4s._

import scalafx.application.Platform.runLater
import scalafx.beans.property._

/** An [[scalafx.beans.value.ObservableValue]] that pulls its value from a future.
  *
  * @author Sarah Gerweck <sarah.a180@gmail.com>
  */
object FutureObservable {
  private[this] val logger = getLogger

  /** Construct an observable that gives the value of the future on success.
    *
    * Until the future completes successfully, the value will be that of
    * `defaultValue`. If there is an error, the value will persist as
    * `defaultValue`.
    *
    * If you want to change state in case of an error, I recommend you use
    * [[scala.concurrent.Future.recover]] to choose the values that will be
    * used in that case. The `defaultValue` is provided because `Future` has
    * no equivalent mechanism for this mandatory functionality, but recovery
    * is already a built-in feature.
    */
  def apply[A](defaultValue: A)(future: Future[A])(implicit ec: ExecutionContext): ReadOnlyObjectProperty[A] = {
    logger.debug(s"Got request to create new FutureObservable")
    future.value match {
      case Some(Success(a)) =>
        ObjectProperty(a)

      case Some(Failure(f)) =>
        logger.info(s"Got failure from FutureObservable's result: $f")
        ObjectProperty(defaultValue)

      case None =>
        val prop = ObjectProperty[A](defaultValue)
        future onSuccess { case a =>
          runLater {
            prop.value = a
          }
        }
        prop
    }
  }
}