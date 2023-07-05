#!/bin/bash

/usr/lib/jvm/java-11-openjdk-amd64/bin/java \
-Dfile.encoding=UTF-8 \
-p /home/tanktarta/Documents/Git/jsixel/converters/target/classes:/home/tanktarta/Documents/Git/jsixel/lib/target/classes:/home/tanktarta/.m2/repository/info/picocli/picocli/4.7.4/picocli-4.7.4.jar:/home/tanktarta/.m2/repository/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar \
-m com.sshtools.jsixel.coverters/com.sshtools.jsixel.converters.Sixel2Png $*