#!/bin/sh

rizzlypath=~/projekte/rizzly/compiler

java -ea -classpath $rizzlypath/bin/rizzlyj.jar:$rizzlypath/javalib/cloning-1.8.1.jar:$rizzlypath/javalib/jgrapht-jdk1.6.jar:$rizzlypath/javalib/commons-lang-2.6.jar:$rizzlypath/javalib/objenesis-1.2.jar:$rizzlypath/javalib/commons-cli-1.2.jar main.Main $rootdir $@

return $?

