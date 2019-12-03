package rpc;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class Compilateur {


    /**
     * main : récupère les méthodes de CalculIfc par réflexion, et génère les fichiers CalculStub et CalculSkeleton correspondants
     *
     * @param args = arguments de la ligne de commande
     */
    public static void main(String[] args) throws Exception {

        String prefix = "Calcul";
        String path   = "./rpc/Calcul";

        // on récupère les méthodes de l'interface par réflexion
        Class ifc = Class.forName("rpc."+prefix+"Ifc");
        ArrayList<Method> arrayMethods = new ArrayList<Method>(Arrays.asList(ifc.getMethods()));

        // on génère les fichiers
        genereStub(prefix, path, arrayMethods);
        genereSkeleton(prefix, path, arrayMethods);
    }



    /**
     * genereStub : génère le fichier CalculStub.java qui sera appelé par le Client
     *              il doit contenir une socket, des flux entrée-sortie avec le Skeleton
     *              et une méthode publique pour chaque méthode de l'interface CalculIfc
     *
     * @param prefix       = Calcul
     * @param path         = ./rpc/Calcul
     * @param arrayMethods = [ init, calcul, ...]
     */
    public static void genereStub(String prefix, String path, ArrayList<Method> arrayMethods) throws IOException {
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
                    +"    public ObjectInputStream ois;\n"
                    +"    public ObjectOutputStream oos;\n"
                    +"\n";

        // constructeur du Stub : il initialise les flux entrée-sortie
        stub = stub +"    public "+prefix+"Stub() {\n"
                    +"        System.out.println(\"constructeur\");\n"
                    +"        try {\n"
                    +"            s = new java.net.Socket(\"localhost\", 1234);\n"
                    +"            oos = new ObjectOutputStream(s.getOutputStream());\n"
                    +"            ois = new ObjectInputStream(s.getInputStream());\n"
                    +"        } catch (Exception e) {\n"
                    +"            e.printStackTrace();\n"
                    +"            throw new ExceptionInInitializerError(e);\n"
                    +"        }\n"
                    +"    }\n"
                    +"\n";


        // Pour chaque méthode de l'interface :
        //      1 - le Stub redéfinit la méthode
        //      2 - le Stub annonce la méthode au Skeleton
        //      3 - le Stub envoie les paramètres un par un avec des writeObject
        //      4 - si le Stub attend un retour du Skeleton, il fait un readObject + cast

        String returnType = "";
        int i = 1;

        for (Method m : arrayMethods) {
            returnType = m.getReturnType().getName();

            // 1 - le Stub redéfinit la méthode
            stub += "    @Override\n";
            stub += "    public "+returnType+" ";
            stub += m.getName()+"(";
            i = 1;
            for (Class param : m.getParameterTypes()) {
                stub += param.getName()+" param"+i+", ";
                i++;
            }
            if (m.getParameterTypes().length > 0) stub = stub.substring(0, stub.length()-2);
            stub += ") throws Exception {\n";
            stub +="        System.out.println(\""+m.getName()+"\");\n";

            // 2 - le Stub annonce la méthode au Skeleton
            stub +="        oos.writeUTF(\""+m.getName()+"\");\n";

            // 3 - le Stub envoie les paramètres un par un avec des writeObject
            i=1;
            for (Class param : m.getParameterTypes()) {
                stub += "        oos.writeObject(param"+i+");\n";
                i++;
            }
            stub += "        oos.flush();\n";

            // 4 - si le Stub attend un retour du Skeleton, il fait un readObject + cast
            if  (!returnType.equals("void")) {
                stub += "        "+returnType+" result = ("+returnType+") ois.readObject();\n";
                stub += "        return result;\n";
            }
            stub += "    }\n\n";   
        }

        stub += "}";
        w.write(stub);
        w.close();
    }
    //----------------------------------------------------------------------------------------



    /**
     * genereSkeleton : génère le fichier CalculSkeleton.java qui sera lancé côté serveur
     *                  il instanciera un object Matlab() et attendra un message du Stub
     *                  chaque fois qu'il recevra un message du Stub :
     *                      - il recevra les paramètres s'il y en a
     *                      - il fera appel à la méthode correspondante de l'objet Matlab
     *                      - il retournera le résultat au Stub s'il y en a un
     *
     * @param prefix       = Calcul
     * @param path         = ./rpc/Calcul
     * @param arrayMethods = [ init, calcul, ...]
     */
    public static void genereSkeleton(String prefix, String path, ArrayList<Method> arrayMethods) throws IOException {
        // on ouvre le Skeleton
        Writer w = new FileWriter(new File(path+"Skeleton.java"));

        // template du fichier
        String skel ="// CECI EST UN FICHIER GENERE AUTOMATIQUEMENT PAR rpc.Compilateur\n"
                    +"package rpc;\n"
                    +"\n"
                    +"import java.net.*;\n"
                    +"import java.io.*;\n"
                    +"\n"
                    +"public class "+prefix+"Skeleton {\n"
                    +"\n"
                    +"    public static void main(String [] arg) throws Exception {\n"
                    +"        ServerSocket server = new ServerSocket(1234);\n"
                    +"        Socket s = server.accept();\n"
                    +"        System.out.println(\"Stub connected\");\n"
                    +"\n"
                    +"        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());\n"
                    +"        ObjectInputStream ois  = new ObjectInputStream(s.getInputStream());\n"
                    +"\n"
                    +"        Matlab m = new Matlab();\n"
                    +"        String fonction = ois.readUTF();\n"
                    +"\n"
                    +"        try {\n"
                    +"            while(true) {\n"
                    +"\n"
                    +"                switch (fonction) {\n"
                    +"                    default:\n"
                    +"                        System.out.println(\"ERREUR : fonction non reconnue\");\n"
                    +"                        break;\n"
                    +"\n";


        // Pour chaque méthode de l'interface :
        //      1 - on ajoute un case dans le Skeleton
        //      2 - le Skeleton reçoit chaque paramètre avec readObject()
        //      3 - si le Skeleton doit retourner quelque chose, il appelle writeObject()
        //          dans tous les cas le Skeleton appelle Matlab.methode() avec les paramètres reçus
        String paramRecus = "";
        String nomMethode = "";
        int i = 1;

        for (Method m : arrayMethods) {
            nomMethode = m.getName();

            // 1 - on ajoute un case dans le Skeleton
            skel +="                    case \""+nomMethode+"\":\n";
            skel +="                        System.out.println(\""+nomMethode+"\");\n";

            // 2 - le Skeleton reçoit chaque paramètre avec un readObject
            paramRecus = "";
            for (Class param : m.getParameterTypes()) {
                skel += "                        "+param.getName()+" param"+i+" = ("+param.getName()+")ois.readObject();\n";
                paramRecus += "param"+i+", ";
                i++;
            }
            if (m.getParameterTypes().length > 0) paramRecus = paramRecus.substring(0, paramRecus.length()-2);

            // 3 - si le Skeleton doit retourner quelque chose, il appelle writeObject()
            if  (!m.getReturnType().getName().equals("void")) {
                skel += "                        oos.writeObject(m."+nomMethode+"("+paramRecus+"));\n";
                skel += "                        oos.flush();\n";
            } else {
                // dans tous les cas le Skeleton appelle Matlab.methode() avec les paramètres reçus
                skel += "                        m."+nomMethode+"("+paramRecus+");\n";
            }
            skel += "                        break;\n\n";   
        }

        skel +="                }\n"
             +"                fonction = ois.readUTF();\n"
             +"            }\n"
             +"        } catch (EOFException e) {\n"
             +"            System.out.println(\"Connection closed.\");\n"
             +"        }\n"
             +"    }\n"
             +"}";

        w.write(skel);
        w.close();
    }
}
