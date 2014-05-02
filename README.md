HippoDB
========

HippoDB is a distributed database meant to serve data which lives in Hadoop clusters (but not only) to frontend applications. In order to keep things as simple and as robust as possible, HippoDB is a read-only key-value store. This means that:

* data is *never* written. Instead dumps for HippoDB are created externally and an atomic switch is made when a new version is available.
* the *only* pattern of access to resident data is **by key**, although there is support for splitting data relative to a single key into columns.

This restrictions allow to write a tool which has very few moving parts, and so it is extremely simple, both in terms of logic and in terms of lines of code. Hence, although very young, HippoDB is also very robust.

It also allows to switch back to a past version, in the event that the last version deployed should contain erroneous data.

Workflow
--------

A typical workflow would have HippoDB work in tandem with another more sophisticated store such as **HBase**. Data comes in HBase and possibly batch jobs (for instance map-reduce) are ran.

Periodically - say every two hours - relevant data is dumped to HippoDB for serving to frontend applications. Once it is ready, a command is issued to HippoDB, to instantaneously switch version. In case something went wrong, a rollback can be made with no downtime.

What HippoDB supports
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

Since HippoDB is read-only, the elastic growth of the cluster means that new servers will be recognized and will become part of the cluster with no downtime, but data will have to be dumped taking into account the presence of the new servers. Hence, it only makes sense to increase the cluster size on a version switch.

What HippoDB does *not* support
------------------------------

Anything else :-)

Structure of HippoDB
---------------------

HippoDB consists of a few separate subprojects. This does not mean it is necessarily complicated, as some of them only amount to a few lines.

* **hippodb-server** is the core of the project. It takes care of actually serving data and mantaining contacts with the cluster.
* **hippodb-client** can be used by JVM applications. Akka applications can just use a dedicated actor (HippoDB is based on Akka), while other applications will have a separate client.
* **hippodb-http** allows to serve data which is present on HippoDB through an HTTP interface. It is useful in case some applications that are not on the JVM need a way to talk to HippoDB.
* **hippodb-hbase-sync** is a map reduce job that can be used to keep in sync HBase tables with HippoDB. It produces a collection of SequenceFile on HDFS
* **hippodb-retriever** is a command line tool that retrieves the output from hbase-sync job on the local filesystem and creates the indices that are necessary for HippoDB, so that the output is ready to be served

Hence a typical deployment would have hippodb-server on each serving machine and hippodb-client used as a library from client applications. Peridocally, hippodb-hbase-sync can be run to read data from HBase and this data can be made ready on the servers using hippodb-retriever. Finally, one or more instances of hippodb-http can be deployed at will.

Since hippodb-http is based on hippodb-client, and the latter already takes care of load balancing, there should be no need to deploy hippodb-http to more that one machine and load balance between them. The workload done by hippodb-http is minimal and there will probably be no gain in having another server in front of it.

Kudos
-----

The idea for HippoDB is essentially the same as [ElephantDB](https://github.com/nathanmarz/elephantdb "ElephantDB") from [Nathan Marz](https://github.com/nathanmarz/). Unfortunately the latter project seems underdocumented. HippoDB is so simple thanks to the wonderful [Akka framework](http://akka.io/ "Akka").