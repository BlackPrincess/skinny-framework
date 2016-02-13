package skinny.oauth2.client.google

import skinny.oauth2.client.OAuth2User

/**
 * Authorized Google plus user basic information.
 */
case class GoogleUser(
  override val id: String,
  displayName: String,
  name: Name,
  url: Option[String],
  image: Option[Image],
  emails: Seq[Email]
) extends OAuth2User

case class Name(givenName: String, familyName: String)
case class Image(url: String, isDefault: Boolean)
case class Email(value: String, `type`: String)
