package rpc;

import java.net.*;
import java.io.*;

public class CalculStub implements CalculIfc {

    public java.net.Socket s;
    public java.io.DataOutputStream dos;
    public java.io.ObjectInputStream ois;

    public CalculStub() {
        System.out.println("Stub:: constructeur");
        try {
            s = new java.net.Socket("localhost", 1234);
            dos = new java.io.DataOutputStream(s.getOutputStream());
            ois = new java.io.ObjectInputStream(s.getInputStream());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void init(int i) throws IOException {
        System.out.println("Stub:: init");
        dos.writeUTF("init");
        dos.writeInt(i);
        dos.flush();
    }

    public Result calcul(int i) throws ClassNotFoundException, IOException {
        System.out.println("Stub:: calcul");
        dos.writeUTF("calcul");
        dos.writeInt(3);
        dos.flush();

        Result r = (Result)ois.readObject();
        s.close();
        return r;
    }
}