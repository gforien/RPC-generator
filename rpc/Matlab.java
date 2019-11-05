package rpc;

public class Matlab {
    private int i;

    public Matlab(int i) {
        this.i = i;
    }

    public Result calcul(int in) {
        return new Result(in * this.i);
    }

    public static void main(String [] arg) throws Exception {
        Matlab m = null;
        java.net.ServerSocket sos = new java.net.ServerSocket(1234);
        java.net.Socket s = sos.accept();


        java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(s.getOutputStream());

        String fonction = dis.readUTF();
        if (fonction.equals("init")) {
            m = new Matlab(dis.readInt());
            System.out.println("reussi !");
        } else {
            System.out.println("pas réussi !");
        }


        fonction = dis.readUTF();
        if (fonction.equals("calcul")) {
            oos.writeObject(m.calcul(dis.readInt()));
            oos.flush();
            System.out.println("reussi !");
            Thread.sleep(5000);
        } else {
            System.out.println("pas reussi !");
        }
    }

}
