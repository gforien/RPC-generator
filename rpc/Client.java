package rpc;

public class Client {

    public static void main(String [] arg) throws Exception {

        CalculIfc stub = new CalculStub();
        stub.init(10);
        Result r = stub.calcul(3);
        System.out.println(r);
    }

}


