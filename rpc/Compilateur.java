package rpc;

import java.util.regex.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

// piege : le constructeur ne fait pas partie de l'interface
// le constructeur qui prend des parametres c'est une erreur
// le constructeur devrait juste servicr à faire l'allocation mémoire, et pas à initialiser

// sur des SYD, comme on ne maitrise ni la mémoire ni le temps, la responsabilité du cycle de vie
// de l'objet ne devrait pas être au client

public class Compilateur {

    public static void main(String[] args) throws IOException {
        String fichier = new String(Files.readAllBytes(Paths.get("Interface.java")));

        ArrayList<ArrayList<String>> res = trouveMethode(fichier);
        System.out.println(res);

    }

    public static ArrayList<ArrayList<String>> trouveMethode(String chaine) {
        ArrayList<ArrayList<String>> tab = new ArrayList<ArrayList<String>>();
        Pattern pattern;
        Matcher matcher;

        String[]  tabChaine = chaine.split("\n");

        ArrayList<String> ligne;
        for (String s : tabChaine) {
            ligne = new ArrayList<String>();
            //  selections                             (2)              (3)        (4)
            pattern = Pattern.compile("^( )*public ([a-zA-Z0-9]+) ([a-zA-Z0-9]+)\\((.*)\\)(.*);$");

            matcher = pattern.matcher(s);
            while(matcher.find()) {
                ligne.add(matcher.group(2));
                ligne.add(matcher.group(3));
                ligne.add(matcher.group(4));
            }
            if (ligne.size() != 0) {
                tab.add(ligne);
            }
        }
        return tab;
    }

}