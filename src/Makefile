
classpath=.:../javalib/jgrapht-jdk1.6.jar:../javalib/commons-lang-2.6.jar:../javalib/commons-cli-1.2.jar

tmpdir=/tmp/rizzly/
outdir=../

all:
	mkdir -p ${tmpdir}
	javac -d ${tmpdir} -s ${tmpdir} -classpath ${classpath} -encoding UTF8 main/Main.java
	jar cvfm ${outdir}rizzly.jar Manifest.txt -C ${tmpdir} .

