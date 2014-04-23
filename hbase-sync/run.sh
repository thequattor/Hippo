#! /bin/sh

SDIR=`dirname $(readlink -f $0)`
cd "$SDIR"
JAR_NAME=$(ls hippodb-hbase-sync-assembly-*.jar)
HADOOP_JAR="hadoop jar ${JAR_NAME}"

$HADOOP_JAR \
unicredit.hippo.jobs.HBaseSync \
-Dmapred.map.tasks=18 \
--hdfs \
--quorum levy.rnd \
--table wiki_it_index \
--output hippoprova \
--cf stats \
--columns count,documents \
--replicas 3 \
--partitions 24 \
--servers belle,cinderella,jasmine,ariel,rapunzel,showwhite
