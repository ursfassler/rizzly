#!/bin/sh

rizzlypath=~/projekte/rizzlygit/rizzly

java -ea -classpath $rizzlypath/rizzly.jar:$rizzlypath/javalib/jgrapht-jdk1.6.jar:$rizzlypath/javalib/commons-lang-2.6.jar:$rizzlypath/javalib/commons-cli-1.2.jar main.Main $rootdir $@

return $?

