
instdir=~/bin/

classpath=.:javalib/jgrapht-jdk1.6.jar:javalib/commons-lang-2.6.jar:javalib/commons-cli-1.2.jar

tmpdir=/tmp/rizzly/

all: jar rizzly

jar:
	mkdir -p ${tmpdir}
	javac -d ${tmpdir} -s ${tmpdir} -classpath ${classpath} -encoding UTF8 -sourcepath src/ src/main/Main.java
	jar cfm rizzly.jar src/Manifest.txt -C ${tmpdir} .
	zipmerge rizzly.jar javalib/*.jar
	
rizzly: Makefile
	echo "#!/bin/sh\n\njava -ea -classpath ${instdir}/rizzly.jar main.Main $$""@\n\nreturn $$""?\n" > rizzly
	chmod +x rizzly

clean:
	rm -f rizzly.jar rizzly

install: all
	cp rizzly.jar rizzly ${instdir}

