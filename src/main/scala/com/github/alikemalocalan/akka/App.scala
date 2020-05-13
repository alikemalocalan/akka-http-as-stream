package com.github.alikemalocalan.akka

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

object App extends App {

  val testApiAddress = "jsonplaceholder.typicode.com"

  implicit val system: ActorSystem                        = ActorSystem("Stream-Actor-System")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  import com.github.alikemalocalan.akka.AkkaHttpClient._

  val logger = system.log

  val requests = Seq(1, 3, 5)
    .map(n => s"/todos/$n")
    .map(pathToHttpRequest(testApiAddress, _))
    .toList

  val lines = makeBulkHttpsRequest[Option[UserModel]](requests, testApiAddress)
    .map(x => x.filter(_.isDefined).map(_.get))

  lines
    .map { value =>
      logger.info(s"Result size is : ${value.size}")
    }
    .onComplete { _ =>
      system.terminate()
    }
}
