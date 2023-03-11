
## Kafka topic
Topic input
``` 
kafka-topics --create --bootstrap-server broker0:29092 --replication-factor 2 --partitions 2 --topic word-count-input
```
Topic output

``` 
kafka-topics --create --bootstrap-server broker0:29092 --replication-factor 2 --partitions 2 --topic word-count-output
```


## Kafka consumer output topic

```
kafka-console-consumer --bootstrap-server broker0:29092 \
    --topic word-count-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer 
```

## Producer input topic
```
kafka-console-producer --bootstrap-server broker0:29092 --topic word-count-input
```
# bank balancing:

Creation topic:
```
 kafka-topics --create --bootstrap-server broker0:29092 \
 --replication-factor 2 \
 --partitions 2 \
 --topic bank-transactions
```

```
 kafka-topics --create --bootstrap-server broker0:29092 \
 --replication-factor 2 --partitions 2 \
 --topic bank-balance-exactly-once \
 --config cleanup.policy=compact
```

Lancement consumer balance:
```
kafka-console-consumer --bootstrap-server broker0:29092 \
  --topic bank-balance-exactly-once \
  --from-beginning --formatter kafka.tools.DefaultMessageFormatter\
  --property print.key=true  \
  --property print.value=true  \
  --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```
Comsumer transaction:
```
 kafka-console-consumer --from-beginning --bootstrap-server broker0:29092 --topic bank-transactions
```

Pour le producer, il faut lancer le producer depuis:
fr/andri/bank/BankTransactionProducer.scala

Après lancer kafkastream depuis:
fr/andri/bank/BankBalanceExactlyOnceApp.scala
