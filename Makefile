all: stub
	javac rpc/*.java

stub: compilo
	java rpc.Compilateur ./rpc/CalculIfc.java

compilo:
	javac rpc/Result.java rpc/CalculIfc.java rpc/Compilateur.java