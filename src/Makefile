JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	QueryOptimizer.java \
	QueryPlan.java \
	QueryOptimizerUtils.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class