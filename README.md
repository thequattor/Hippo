Hippo DB
========

Hippo DB is a distributed database meant to serve data which lives in Hadoop clusters (but not only) to frontend applications. In order to keep things as simple and as robust as possible, Hippo DB is a read-only key-value store. This means that:

* data is never written. Instead dumps for Hippo DB are created externally and an atomic switch is made when a new version is available.
* the only pattern of access to resident data is by key, although there is support for splitting data relative to a single key into columns.

This restrictions allow to write a tool which has very few moving parts, and so it is extremely simple, both in terms of logic and of lines of code. Hence, although very young, Hippo DB is also very robust.

It also allows to switch back to a past version, in the event that the last version deployed should contain erroneous data.

Workflow
--------

A typical workflow would have Hippo DB work in tandem with another more sophisticated store such as HBase. Data comes in HBase and possibly batch jobs (for instance map-reduce) are ran.

Periodically - say every two hours - relevant data is dumped to Hippo DB for serving to frontend applications. Once it is ready, a command is issued to Hippo DB, to instantaneously switch version. In case something went wrong, a rollback can be made with no downtime.

What Hippo DB supports
----------------------

* Distributed stores with a chosen replication factor
* Multiget requests for any number of keys and columns
* Reliable serving even when a few servers fail
* Elastic growth of the cluster, with no downtime
* Native (JVM) or HTTP clients
* Clients perform automatic load-balancing between existing servers, in a cache-friendly way (so the same request will always be sent to the same server, if possible)

The third line needs a little explanation. If r is the replication factor, data can still be served if up to r-1 servers fail.

If more than this number fails, data that is present on the remaining servers will still be served without failures, although of course some data may only be present on the failed machines.

Finally, the client transparently switches to any server which is still live. This means that, although the client is initialized with the address of any of the servers, it will still be able to connect even if this particular server fails (although of the server needs to be up in the first instant when the client connects). In fact, requests are always load-balanced by clients between live servers.

Since Hippo DB is read-only, the elastic growth of the cluster means that new servers will be recognized and will become part of the cluster with no downtime, but data will have to be dumped taking into account the presence of the new servers. Hence, it only makes sense to increase the cluster size on a version switch.

What Hippo DB does not support
------------------------------

Anything else :-)

Structure of Hippo DB
---------------------

Hippo DB consists of a few separate subprojects. This does not mean it is necessarily complicated, as some of them only amount to a few lines.

* hippodb-server is the core of the project. It takes care of actually serving data and mantaining contacts with the cluster.
* hippodb-client can be used by JVM applications. Akka applications can just use a dedicated actor (Hippo DB is based on Akka), while other applications will have a separate client.
* hippodb-http allows to serve data which is present on Hippo DB through an HTTP interface. It is useful in case some applications that are not on the JVM need a way to talk to Hippo DB.
* hippodb-tap is a Scalding tap that can be used to generate Hippo DB dumps as output of map-reduce jobs. It is the preferred way to populate Hippo DB.
* hippodb-hbase-sync can be used by people who do not already have a Scalding workflow. It effectively used Scalding and hippodb-tap to keep in sync HBase tables with Hippo DB.

Hence a typical deployment for teams who use Scalding would have hippodb-server on each serving machine, hippodb-tap used as a library inside Scalding jobs and hippodb-client used as a library from client applications.

When Scalding is not a tool in use, hippodb-hbase-sync can be used in its place, assuming data resides on HBase in the first place. Finally, one or more instances of hippodb-http can be deployed at will.

Since hippodb-http is based on hippodb-client, and the latter already takes care of load balancing, there should be no need to deploy hippodb-http to more that one machine and load balance between them. The workload done by hippodb-http is minimal and there will probably be no gain in having another server in front of it.

Kudos
-----

The idea for Hippo DB is essentially the same as Elephant DB from Nathan Marz. Unfortunately the latter project seems underdocumented. Hippo DB is so simple thanks to the wonderful Akka framework.