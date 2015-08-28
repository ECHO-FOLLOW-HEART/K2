package formatter

import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper }
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import models.AbstractEntity
import org.bson.types.ObjectId

/**
 * Created by pengyt on 2015/8/27.
 */
class ObjectMapperFactory {
  private val mapper = new ObjectMapper()
  private val module = new SimpleModule()
  mapper.registerModule(DefaultScalaModule)
  module.addSerializer(classOf[ObjectId], new ObjectIdSerializer)

  def addSerializer[T <: AbstractEntity](clazz: Class[T], ser: JsonSerializer[T]): ObjectMapperFactory = {
    module.addSerializer(clazz, ser)
    this
  }

  def build() = {
    mapper.registerModule(module)
    mapper
  }
}

object ObjectMapperFactory {
  def apply() = new ObjectMapperFactory()
}