package rpc;

public class Client {

    public static void main(String [] arg) throws Exception {

        CalculIfc s = new CalculStub();
        s.init(10);
        Result r = s.calcul(3);
    }

}


