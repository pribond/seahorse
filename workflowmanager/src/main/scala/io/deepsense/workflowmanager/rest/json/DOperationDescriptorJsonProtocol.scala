/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

import scala.reflect.runtime.universe.Type

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.deeplang.catalogs.doperations.DOperationDescriptor

/**
 * Exposes various json formats of DOperationDescription.
 * Reading from json is not supported.
 */
trait DOperationDescriptorJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  class DOperationDescriptorShortFormat extends RootJsonFormat[DOperationDescriptor] {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(
        "id" -> obj.id.toJson,
        "name" -> obj.name.toJson)
    }

    override def read(json: JsValue): DOperationDescriptor = {
      throw new UnsupportedOperationException
    }
  }

  /**
   * Only id and name of operation.
   */
  object DOperationDescriptorShortFormat extends DOperationDescriptorShortFormat

  class DOperationDescriptorBaseFormat extends DOperationDescriptorShortFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields ++ Map(
        "version" -> obj.version.toJson,
        "category" -> obj.category.id.toJson,
        "description" -> obj.description.toJson,
        "deterministic" -> false.toJson,  // TODO use real value as soon as it is supported
        "ports" -> JsObject(
          "input" -> portTypesToJson(obj.inPorts, addRequiredField = true),
          "output" -> portTypesToJson(obj.outPorts, addRequiredField = false)
        )
      ))
    }

    private def portTypesToJson(portTypes: Seq[Type], addRequiredField: Boolean): JsValue = {
      val required = if (addRequiredField) Some(true) else None
      // TODO use real value as soon as it is supported

      val fields = for ((portType, index) <- portTypes.zipWithIndex)
      yield portToJson(index, required, portType)
      fields.toJson
    }

    private def portToJson(index: Int, required: Option[Boolean], portType: Type): JsValue = {
      val fields = Map(
        "portIndex" -> index.toJson,
        "typeQualifier" -> DOperationDescriptor.describeType(portType).toJson
      )
      JsObject(required match {
        case Some(value) => fields.updated("required", value.toJson)
        case None => fields
      })
    }
  }

  /**
   * All operation's info except for parameters.
   */
  object DOperationDescriptorBaseFormat extends DOperationDescriptorBaseFormat

  /**
   * Full operation's info.
   */
  object DOperationDescriptorFullFormat extends DOperationDescriptorBaseFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields.updated("parameters", obj.parameters.toJson))
    }
  }
}

object DOperationDescriptorJsonProtocol extends DOperationDescriptorJsonProtocol