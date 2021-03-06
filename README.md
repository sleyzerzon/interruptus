Interruptus
================

A framework for scalable monitoring.


[![Build Status](https://travis-ci.org/interruptus/interruptus.svg?branch=master)](https://travis-ci.org/interruptus/interruptus)

<pre>
            _.---._
        .-'         '-.
     .'                 '.
    '       '.   .'       '
   / /        \ /        \ \
  '  |         :         |  '
 /   |         .         |   \
 |   \         |         /   |
 '. . \        |        / . .'
  |   .\      .'.      /.   |
  \  .  `-           -'  .  /
   '.      .. ... ..      .'
    |  `` ` .     . ` ``  |
    | .-_-.  '. .'  .-_-. |
   .'( (O) )|  :  |( (O) )'.
    \.'---'/   :   \'---'./
      \_ .'  . ' .  '. _/
     .' /             \ '.
     './ / /  / \ \  \ \.'
      : | | /|  : |  | :
      | : | \\  | '  : |
      | /\ \/ \ | : /\ :
      ' :/\ \ : ' ||  \ \
      / | /\ \| : ' \  \ \
     / / /  \/ /| :  |  \ \
    / / :   / /\ \ \ /   \ \
   ' /\ \  | /\ :.\ \    / |
   \ \ \ \ \/ / || \ \   \/
    \/  \|    \/ \/ |/
</pre>

Authors
=======

 Mark Steele <mark@control-alt-del.org>
 
 Fabio "Fantastico" B. Silva <fabio.bat.silva@gmail.com>


Configuration example
======================

The following example configures an event type in Interruptus and uses an AMQP inbound data flow to read AMQP messages.


```
curl -X POST 'http://localhost:8080/api/type' -H "Content-Type:application/json" -d '{
    "name":"CollectdMetric",
    "properties":{
      "plugin":"string",
      "plugin_instance":"string",
      "type":"string",
      "type_instance":"string",
      "datacenter":"string",
      "time":"long",
      "value":"double",
      "name":"string",
      "host":"string"
    }
}'

curl -X POST 'http://localhost:8080/api/statement' -H "Content-Type:application/json" -d '{
    "name":"eventlogdebug",
    "query":"SELECT * FROM CollectdMetric WHERE host = \"mq01.ss\"",
    "debug":true,
    "started":true
}'

curl -X POST 'http://localhost:8080/api/flow' -H "Content-Type:application/json" -d '{
  "name":"EventsIn",
  "started":true,
  "query":"
    create dataflow EventsIn AMQPSource -> EventsIn<CollectdMetric>
    {
        collector:    {class: \"org.cad.interruptus.AMQPJsonToMap\"},
        host:         \"localhost\",
        exchange:     \"collectd_metrics\",
        port:         5672,
        username:     \"guest\",
        password:     \"guest\",
        routingKey:   \"#\",
        logMessages:  true
    } EventBusSink(EventsIn){}"
}'

```

API USAGE
=========

[View the HTML rest API DOC](http://htmlpreview.github.io/?https://raw.github.com/interruptus/interruptus/master/generated/strapdown.html)




Dependencies
======
- Maven 3
- Java JDK
- ZooKeeper

Build
======

______

Maven 3
------

```
$ add-apt-repository ppa:natecarlson/maven3
```

```
$ apt-get update && sudo apt-get install maven3
```

```
$ ln -s /usr/bin/mvn3 /usr/bin/mvn
```

______

Java JDK
------

```
$ apt-get purge oracle-java7-installer*
```

```
$ apt-get install ppa-purge
```

```
$ ppa-purge ppa:eugenesan/java
```

```
$ apt-get clean
```

```
$ apt-get update
```

______

ZooKeeper
------

```
$ apt-get install zookeeper zookeeper-bin zookeeperd
```

______

Build
------

```
$ cd path/to/interruptus
$ mvn jetty:run
```
