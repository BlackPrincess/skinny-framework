package skinny.controller.feature

import org.scalatra._
import org.json4s._
import scala.xml._

import skinny.Format
import skinny.logging.Logging
import skinny.exception.ViewTemplateNotFoundException
import skinny.controller.SkinnyControllerBase

/**
 * TemplateEngine support for Skinny app.
 */
trait TemplateEngineFeature
    extends ScalatraBase
    with RequestScopeFeature
    with JSONFeature
    with Logging {

  /**
   * Default charset.
   */
  lazy val charset: Option[String] = Some("utf-8")

  /**
   * Renders body with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return body
   */
  def render(path: String)(implicit format: Format = Format.HTML): String = {

    // If Content-Type is already set, never overwrite it.
    if (contentType == null) {
      contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }

    if (templateExists(path)) {
      // template found, render with it
      renderWithTemplate(path)
    } else if (format == Format.HTML) {
      // template not found and should be found
      throw new ViewTemplateNotFoundException(s"View template not found. (expected: one of ${templatePaths(path)})")
    } else {
      // template not found, but try to render JSON or XML body if possible
      logger.debug(s"Template for ${path} not found.")
      val entity = (for {
        resourcesName <- requestScope[String](RequestScopeFeature.ATTR_RESOURCES_NAME)
        resources <- requestScope[Any](resourcesName)
      } yield resources) getOrElse {
        for {
          resourceName <- requestScope[String](RequestScopeFeature.ATTR_RESOURCE_NAME)
          resource <- requestScope[Any](resourceName)
        } yield resource
      }
      // renderWithFormat returns null when body is empty
      Option(renderWithFormat(entity)).getOrElse(haltWithBody(404))
    }
  }

  /**
   * Returns possible template paths.
   * Result is a list because the template engine may support multiple template languages.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return actual path
   */
  protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String]

  /**
   * Predicates the template exists.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return true/false
   */
  protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean

  /**
   * Renders with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return body
   */
  protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String

  /**
   * Renders body which responds to the specified format (JSON, XML) if possible.
   *
   * @param entity entity
   * @param format format (HTML,JSON,XML...)
   * @return body if possible
   */
  protected def renderWithFormat(entity: Any)(implicit format: Format = Format.HTML): String = {
    format match {
      case Format.XML =>
        val entityXml = toXml(toJSON(entity)).toString
        s"""<?xml version="1.0" encoding="${charset.getOrElse("UTF-8")}"?><${xmlRootName}>${entityXml}</${xmlRootName}>"""
      case Format.JSON => toJSONString(entity)
      case _ => null
    }
  }

  /**
   * Halts with body which responds to the specified format.
   * @param httpStatus  http status
   * @param format format (HTML,JSON,XML...)
   * @tparam A response type
   * @return body if possible
   */
  def haltWithBody[A](httpStatus: Int)(implicit format: Format = Format.HTML): A = {
    val body: String = format match {
      case Format.HTML => render(s"/error/${httpStatus}")
      case _ => renderWithFormat(Map("status" -> httpStatus, "message" -> ResponseStatus(httpStatus).message))
    }
    Option(body).map { b =>
      halt(status = httpStatus, body = b)
    }.getOrElse {
      halt(status = httpStatus)
    }
  }

  protected def xmlRootName: String = "response"

  protected def xmlItemName: String = "item"

  /**
   * {@link org.json4s.Xml.toXml(JValue)}
   */
  private[this] def toXml(json: JValue): NodeSeq = {
    def _toXml(name: String, json: JValue): NodeSeq = json match {
      case JObject(fields) => new XmlNode(name, fields flatMap { case (n, v) => _toXml(n, v) })
      case JArray(xs) => xs flatMap { v => _toXml(name, v) }
      case JInt(x) => new XmlElem(name, x.toString)
      case JDouble(x) => new XmlElem(name, x.toString)
      case JDecimal(x) => new XmlElem(name, x.toString)
      case JString(x) => new XmlElem(name, x)
      case JBool(x) => new XmlElem(name, x.toString)
      case JNull => new XmlElem(name, "null")
      case JNothing => Text("")
    }
    json match {
      case JObject(fields) => fields flatMap { case (n, v) => _toXml(n, v) }
      case x => _toXml(xmlItemName, x)
    }
  }
  private[this] class XmlNode(name: String, children: Seq[Node])
    extends Elem(null, name, xml.Null, TopScope, children.isEmpty, children: _*)
  private[this] class XmlElem(name: String, value: String)
    extends Elem(null, name, xml.Null, TopScope, Text(value).isEmpty, Text(value))

}
