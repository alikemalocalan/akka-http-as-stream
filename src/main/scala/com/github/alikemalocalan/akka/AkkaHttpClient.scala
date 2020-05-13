package com.github.alikemalocalan.akka

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import com.github.alikemalocalan.akka.UserModel._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.extras.Configuration

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

object AkkaHttpClient extends LazyLogging {
  val concurrentRequest                    = 5
  implicit val customConfig: Configuration = io.circe.generic.extras.Configuration.default.withDefaults

  def pathToHttpRequest(host: String, path: String): HttpRequest = {
    val hs = Seq(
      RawHeader("x-correlationId", UUID.randomUUID().toString),
      RawHeader("x-agentname", "akka-test")
    )
    val req = HttpRequest(
      uri = s"https://$host$path",
      method = HttpMethods.GET
    )
    hs.foldLeft(req)((r, h) => r.addHeader(h))
  }

  implicit def asyncOp(
      implicit executionContext: ExecutionContextExecutor,
      system: ActorSystem): HttpResponse => Future[Option[UserModel]] = {
    case HttpResponse(OK, _, entity, _) =>
      Unmarshal(entity)
        .to[UserModel]
        .map(Some(_))
        .recover {
          case ex: Exception =>
            logger.error(s"${UserModel.getClass.getSimpleName} parsing Error :", ex)
            None
        }
    case response =>
      val errorStr = s"Returned non-200  with error response: ${response.entity.toString}"
      logger.error(errorStr)
      Future.failed(new Exception(errorStr))

  }

  def makeBulkHttpsRequest[T](requests: List[HttpRequest], host: String, port: Int = 443)(
      implicit executionContext: ExecutionContextExecutor,
      system: ActorSystem,
      asyncOp: HttpResponse => Future[T]): Future[Seq[T]] = {

    val poolClientFlow = Http().outgoingConnectionHttps(host, port)

    Source(requests)
      .via(poolClientFlow)
      .mapAsync(parallelism = concurrentRequest)(asyncOp)
      .runWith(Sink.seq)
  }

}
