all:
	java rpc.Compilateur && javac rpc/*.java

compilo:
	javac rpc/Result.java rpc/CalculIfc.java rpc/Compilateur.java