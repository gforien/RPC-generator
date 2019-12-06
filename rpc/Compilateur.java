package rpc;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Compilateur {


    /**
     * main : récupère les méthodes de CalculIfc par réflexion, et génère les fichiers CalculStub et CalculSkeleton correspondants
     *
     * @param args = arguments de la ligne de commande
     */
    public static void main(String[] args) throws Exception {

        // on veut le chemin en paramètre
        if (args.length!=1) {
            System.out.println("Usage: java rpc.Compilateur <fichier _____Ifc.java>");
            return;
        }
        String path = args[0];

        // le fichier doit exister
        if (!(new File(path)).exists()) {
            System.out.println("Fichier source "+path+" introuvable...");
            System.out.println("Usage: java rpc.Compilateur <fichier _____Ifc.java>");
            return;
        }

        // on récupère le path et le prefix
        Pattern pattern = Pattern.compile("^(./)?(([a-zA-Z0-9]+/)*[a-zA-Z0-9]+Ifc).java");
        Matcher matcher = pattern.matcher(path);
        matcher.find();
        try {
            path = matcher.group(2);
            System.out.println("Fichier source trouvé: "+path);
        } catch (IllegalStateException e) {
            System.out.println("Le fichier doit s'appeler _____Ifc.java (ex: HelloIfc.java)");
            System.out.println("Usage: java rpc.Compilateur <fichier _____Ifc.java>");
            return;
        }

        // on récupère les méthodes de l'interface par réflexion
        Class ifc = null;
        try {
            String classname = matcher.group(2).replaceAll("/", ".");
            ifc = Class.forName(classname);
            System.out.println("Fichier .class trouvé: "+classname);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.out.println("Fichier compilé de la classe "+path.replaceAll("/", ".")+" introuvable !");
            System.out.println("Usage: java rpc.Compilateur <fichier _____Ifc.java>");
            return;
        }
        ArrayList<Method> arrayMethods = new ArrayList<Method>(Arrays.asList(ifc.getMethods()));

        // on génère les fichiers
        genereStub(path, arrayMethods);
        genereSkeleton(path, arrayMethods);
    }



    /**
     * genereStub : génère le fichier CalculStub.java qui sera appelé par le Client
     *              il doit contenir une socket, des flux entrée-sortie avec le Skeleton
     *              et une méthode publique pour chaque méthode de l'interface CalculIfc
     *
     * @param path         = rpc/Calcul
     * @param arrayMethods = [ init, calcul, ...]
     */
    public static void genereStub(String path, ArrayList<Method> arrayMethods) throws IOException {
        // on récupère ce qui nous intéresse
        boolean avecPackage = (path.lastIndexOf("/")>=0);
        String nomPackage = (avecPackage)? path.substring(0, path.lastIndexOf("/")).replaceAll("/", "."): "";
        String base = path.substring(0, path.lastIndexOf("Ifc"));
        String prefix = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("Ifc"));

        // on ouvre le Stub
        Writer w = new FileWriter(new File(base+"Stub.java"));

        // template du fichier
        String stub = "// CECI EST UN FICHIER GENERE AUTOMATIQUEMENT PAR rpc.Compilateur\n";
        if (avecPackage) stub +="package "+nomPackage+";\n";

        stub +="\n"
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
        stub +="    public "+prefix+"Stub() {\n"
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

            if (Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            // 1 - le Stub redéfinit la méthode
            stub += "    @Override\n";
            stub += "    "+ prototypeMethode(m) +" {\n";

            returnType = m.getReturnType().getName();
            if  (!returnType.equals("void")) stub += "        "+returnType+" result = "+valeurDefaut(returnType)+";\n";
            stub += "        try {\n";
            stub += "            System.out.println(\""+m.getName()+"\");\n";

            // 2 - le Stub annonce la méthode au Skeleton
            stub += "            oos.writeUTF(\""+m.getName()+"\");\n";

            // 3 - le Stub envoie les paramètres un par un avec des writeObject : si on a un type primitif, on fait un "autoboxing" à la main
            i=1;
            for (Class paramType : m.getParameterTypes()) {
                if (estPrimitif(paramType.getName())) {
                    stub += "            oos.writeObject(new "+typeObjet(paramType.getName())+"(param"+i+"));\n";
                } else {
                    stub += "            oos.writeObject(param"+i+");\n";
                }
                i++;
            }
            stub += "            oos.flush();\n";

            // 4 - si le Stub attend un retour du Skeleton, il fait un readObject + cast
            if  (!returnType.equals("void")) {
                stub += "            result = ("+returnType+") ois.readObject();\n";
                stub += "        } catch (IOException | ClassNotFoundException e) {\n";
            } else {
                stub += "        } catch (IOException e) {\n";
            }
            stub += "            e.printStackTrace();\n";
            stub += "        }\n";
            if (!returnType.equals("void")) stub += "        return result;\n";
            stub += "    }\n\n";
        }

        stub += "}";
        w.write(stub);
        w.close();
    }
    //----------------------------------------------------------------------------------------

    public static String prototypeMethode(Method m) {
        String prototype = "";

        // la méthode sera toujours publique
        prototype += "public "+m.getReturnType().getName()+" "+m.getName()+"(";

        // s'il y a des paramètres, on génère
        // int param1, String param2, boolean param3
        int i = 1;
        for (Class paramType : m.getParameterTypes()) {
            prototype += paramType.getName()+" param"+i+", ";
            i++;
        }
        if (m.getParameterTypes().length > 0) prototype = prototype.substring(0, prototype.length()-2);
        prototype += ")";

        // s'il y a des exceptions, on génère
        // ) throws Exception1, Exception 2
        ArrayList<String> arrExceptions = new ArrayList<String>();
        for (Class exceptionType : m.getExceptionTypes()) {
            arrExceptions.add(exceptionType.getName());
        }
        if (arrExceptions.size()>0) prototype += " throws "+String.join(", ", arrExceptions);

        return prototype;
    }

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
    public static void genereSkeleton(String path, ArrayList<Method> arrayMethods) throws IOException {
        // on récupère ce qui nous intéresse
        boolean avecPackage = (path.lastIndexOf("/")>=0);
        String nomPackage = (avecPackage)? path.substring(0, path.lastIndexOf("/")).replaceAll("/", "."): "";
        String base = path.substring(0, path.lastIndexOf("Ifc"));
        String prefix = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("Ifc"));

        // on ouvre le Skeleton
        Writer w = new FileWriter(new File(base+"Skeleton.java"));

        // template du fichier
        String skel ="// CECI EST UN FICHIER GENERE AUTOMATIQUEMENT PAR rpc.Compilateur\n";

        if (avecPackage) skel += "package "+nomPackage+";\n";

        skel +="\n"
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
        String returnType = "";
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
            returnType = m.getReturnType().getName();
            if  (!returnType.equals("void")) {
                if (estPrimitif(returnType)) {
                    skel += "                        oos.writeObject(new "+typeObjet(returnType)+"(m."+nomMethode+"("+paramRecus+")));\n";
                } else {
                    skel += "                        oos.writeObject(m."+nomMethode+"("+paramRecus+"));\n";
                }
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

    public static boolean estPrimitif(String type) {

        switch (type) {
            case "int":
            case "char":
            case "byte":
            case "short":
            case "long":
            case "float":
            case "double":
            case "boolean":
                return true;
        }

        return false;
    }

    public static String typeObjet(String type) {
        String res = new String(type);

        switch (type) {
            case "int":
                res = "Integer";
                break;

            case "char":
                res = "Character";
                break;

            case "byte":
            case "short":
            case "long":
            case "float":
            case "double":
            case "boolean":
                // on met la première lettre en majuscule
                res = type.substring(0, 1).toUpperCase() + type.substring(1);
                break;
        }

        return res;
    }

    public static String valeurDefaut(String type) {
        String res = "null";

        switch (type) {
            case "int":
            case "byte":
            case "short":
            case "long":
            case "double":
            case "float":
                res = "0";
                break;

            case "boolean":
                res = "false";
                break;

            case "char":
                res = "' '";
                break;
        }

        return res;
    }

}
