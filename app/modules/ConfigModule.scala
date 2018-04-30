package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

class ConfigModule extends AbstractModule {
  override def configure(): Unit = {
    ConfigFactory.load.entrySet.map(e => e.getKey -> e.getValue.unwrapped.asInstanceOf[Any]).toMap
      .foreach {
        case (key, value: String) => bind(classOf[String]).annotatedWith(Names.named(key)).toInstance(value)
        case (key, value: Boolean) => bind(classOf[Boolean]).annotatedWith(Names.named(key)).toInstance(value)
        case (key, value: Int) => bind(classOf[Int]).annotatedWith(Names.named(key)).toInstance(value)
        case (key, value: Long) => bind(classOf[Long]).annotatedWith(Names.named(key)).toInstance(value)
        case (key, value: Double) => bind(classOf[Double]).annotatedWith(Names.named(key)).toInstance(value)
        case (key, value: Float) => bind(classOf[Float]).annotatedWith(Names.named(key)).toInstance(value)
        case _ =>
      }
  }
}
