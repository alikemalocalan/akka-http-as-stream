package com.github.alikemalocalan.akka

import io.circe.{Decoder, Encoder}

case class UserModel(
    userId: Int,
    id: Int,
    title: String,
    completed: Boolean
)

object UserModel {

  implicit val encodeUserModel: Encoder[UserModel] =
    Encoder.forProduct4("userId", "id", "title", "completed")(UserModel.unapply(_).get)

  implicit val decodeUserModel: Decoder[UserModel] =
    Decoder.forProduct4("userId", "id", "title", "completed")(UserModel.apply)
}
