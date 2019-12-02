package rpc;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

// piege : le constructeur ne fait pas partie de l'interface
// le constructeur qui prend des parametres c'est une erreur
// le constructeur devrait juste servir à faire l'allocation mémoire, et pas à initialiser

// sur des SYD, comme on ne maitrise ni la mémoire ni le temps, la responsabilité du cycle de vie
// de l'objet ne devrait pas être au client

public class Compilateur {

    public static void main(String[] args) throws Exception {

        // paramètre reçu par la ligne de commande
        String prefix = "Calcul";
        String path = "./rpc/Calcul";

        // récupérer les méthodes
        Class ifc = Class.forName("rpc."+prefix+"Ifc");
        ArrayList<Method> methods = new ArrayList<Method>(Arrays.asList(ifc.getMethods()));

        // on ouvre le Stub
        Writer w = new FileWriter(new File(path+"Stub.java"));

        // template du fichier
        String stub = "// CECI EST UN FICHIER GENERE AUTOMATIQUEMENT PAR rpc.Compilateur\n"
                    +"package rpc;\n"
                    +"\n"
                    +"import java.net.*;\n"
                    +"import java.io.*;\n"
                    +"\n"
                    +"public class "+prefix+"Stub implements "+prefix+"Ifc {\n"
                    +"\n"
                    +"    public java.net.Socket s;\n"
                    +"    public java.io.DataOutputStream dos;\n"
                    +"    public java.io.ObjectInputStream ois;\n"
                    +"\n";

        // constructeur
        stub = stub +"    public "+prefix+"Stub() {\n"
                    +"        System.out.println(\"constructeur\");\n"
                    +"        try {\n"
                    +"            s = new java.net.Socket(\"localhost\", 1234);\n"
                    +"            dos = new java.io.DataOutputStream(s.getOutputStream());\n"
                    +"            ois = new java.io.ObjectInputStream(s.getInputStream());\n"
                    +"        } catch (Exception e) {\n"
                    +"            throw new ExceptionInInitializerError(e);\n"
                    +"        }\n"
                    +"    }\n"
                    +"\n";

        // méthodes de l'interface
        String strMethod = "";
        String returnType = "";
        int i = 1;

        for (Method m : methods) {

            returnType = m.getReturnType().getName();
            i = 1;

            // 1 - Ecrire le prototype (paramètres variables)
            strMethod += "    public "+cut(returnType)+" ";
            strMethod += m.getName()+"(";
            for (Class param : m.getParameterTypes()) {
                strMethod += cut(param.getName())+" param"+i+", ";
                i++;
            }
            if (m.getParameterTypes().length > 0) strMethod = strMethod.substring(0, strMethod.length()-2);
            strMethod += ") throws Exception {\n";

            // 2 - Le client annonce la méthode au serveur
            strMethod +="        System.out.println(\""+m.getName()+"\");\n";
            strMethod +="        dos.writeUTF(\""+m.getName()+"\");\n";

            // 3 - On envoie les paramètres un par un avec des writeObject
            i=1;
            for (Class param : m.getParameterTypes()) {
                strMethod += "        dos.writeObject(param"+i+");\n";
                i++;
            }
            strMethod += "        dos.flush();\n";

            // 4 - Si elle doit recevoir, elle fait un readObject + cast
            if  (!returnType.equals("void")) {
                strMethod += "        "+returnType+" result = ("+returnType+")ois.readObject();\n";
                strMethod += "        return result;\n";
            }
            strMethod += "    }\n\n";   
        }

        stub = stub + strMethod + "}";


                    /*
                    +"    public void init(int i) throws IOException {\n"
                    +"        System.out.println(\"Stub:: init\");\n"
                    +"        dos.writeUTF(\"init\");\n"
                    +"        dos.writeInt(i);\n"
                    +"        dos.flush();\n"
                    +"    }\n"
                    +"\n"
                    +"    public Result calcul(int i) throws ClassNotFoundException, IOException {\n"
                    +"        System.out.println(\"Stub:: calcul\");\n"
                    +"        dos.writeUTF(\"calcul\");\n"
                    +"        dos.writeInt(3);\n"
                    +"        dos.flush();\n"
                    +"\n"
                    +"        Result r = (Result)ois.readObject();\n"
                    +"        s.close();\n"
                    +"        return r;\n"
                    +"    }\n"
                    +"}\n";*/

        w.write(stub);
        w.close();
    }

/*    public static void execute(String cmd) throws IOException, InterruptedException {
        final Process p = Runtime.getRuntime().exec("javac -verbose -cp rpc/ rpc/CalculStub.java");

        new Thread(new Runnable() {
            public void run() {
             BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line = null; 

             try {
                while ((line = input.readLine()) != null)
                    System.out.println(line);
             } catch (IOException e) {
                    e.printStackTrace();
             }
            }
        }).start();

        p.waitFor();
        System.out.println("wesh !");
    }*/

    public static String cut(String s) {
        if (s.split("rpc.").length>1) {
            return s.split("rpc\\.")[1];
        } else {
            return s;
        }
    }
}
