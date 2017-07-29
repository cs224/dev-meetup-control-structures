#!/bin/bash

cp .classpath cp.classpath
sed 's:path="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8.jar"/>:path="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8.jar" sourcepath="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8-sources.jar"/>:' cp.classpath >.classpath
#sed 's/path="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8.jar"\\/>/path="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8.jar" sourcepath="M2_REPO/co/paralleluniverse/quasar-core/0.7.5/quasar-core-0.7.5-jdk8-sources.jar"\\/>/' .classpath >.classpath
