package fr.andri.bank

import com.fasterxml.jackson.databind.node.{JsonNodeFactory, ObjectNode}
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig, ProducerRecord}

import java.time.Instant
import java.util.Properties
import java.util.concurrent.ThreadLocalRandom
import scala.util.control.Breaks.break

object BankTransactionProducer {
  def configurations: Properties={
      val conf: Properties = new Properties()
      val serializer:String = "org.apache.kafka.common.serialization.StringSerializer"
      conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092, localhost:9093, localhost:9094")
      conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer)
      conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer)
    
      conf.put(ProducerConfig.ACKS_CONFIG, "all")
      conf.put(ProducerConfig.RETRIES_CONFIG, "3")
      conf.put(ProducerConfig.LINGER_MS_CONFIG, "1")
      conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
      conf
  }

  def continuousProducer: Unit={
      val producer: Producer[String, String] = new KafkaProducer[String, String](configurations)
      var i: Int = 0
      while (true){
        println(s"Producing batch: ${i}")
        try{
          producer.send(newRandomTransaction("andri"))
          Thread.sleep(100)
          producer.send(newRandomTransaction("tantely"))
          Thread.sleep(100)
          producer.send(newRandomTransaction("tiana"))
          Thread.sleep(100)
          i+=1
        }catch {
          case e:  InterruptedException=>break()
        }
      }
      producer.close()
  }

   private def newRandomTransaction(name: String): ProducerRecord[String,String]={
    val transaction: ObjectNode = JsonNodeFactory.instance.objectNode()
    val amount = ThreadLocalRandom.current().nextInt(0,100)
    val now: Instant = Instant.now();
    transaction.put("name", name)
    transaction.put("amount", amount)
    transaction.put("time", now.toString)
    new ProducerRecord[String, String]("bank-transactions", name, transaction.toString)
  }

  def main(array: Array[String]):Unit={
    continuousProducer
  }

}
