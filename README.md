# RPC-genenerator :pencil::twisted_rightwards_arrows::package:
![SYD TD1](https://img.shields.io/static/v1.svg?label=SYD&message=TD1&color=2aaee6&style=flat)
![License](https://img.shields.io/static/v1.svg?label=License&message=None&color=aaaaaa&style=flat)

Un "compilateur" prend une interface quelconque et génère une architecture réseau Stub <-> Skeleton transparente pour le développeur.

## :construction_worker: Tester le projet
```sh
    ~/rpcgen/ $ make compilo
    ~/rpcgen/ $ make all
    ~/rpcgen/ $ java rpc.CalculSkeleton
    ~/rpcgen/ $ java rpc.Client
```

## :rocket: Créer sa propre interface
:warning: Pour être correctement compilée, une nouvelle interface doit suivre ces règles :
- faire partie du package **rpc**

*Le fichier source se trouve dans rpc/ et sa première ligne est* `package rpc;`
- les méthodes de l'interface sont forcément `public`
- les méthodes de l'interface ne doivent ni recevoir ni renvoyer de types primitifs

*Toutefois cela ne devrait pas poser de problème grâce à l'autoboxing et l'unboxing en Java*
```java
    public void maFonction(int param1, boolean param2, double param3);              // faux
    public void maFonction(Integer param1, Boolean param2, Double param3);          // OK
```
- enfin il faut modifier les lignes 17 et 18 de **rpc/Compilateur.java**<br>
*Par défaut ces lignes font référence à l'interface* **rpc.CalculIfc**
```java
17        String prefix = "Calcul";                                                 // à modifier
18        String path   = "./rpc/Calcul";                                           // à modifier
```

## Problèmes RPC
:warning: Le constructeur ne fait pas partie de l'interface<br>
"Le constructeur qui prend des parametres c'est une erreur"<br>
"Le constructeur devrait juste servir à faire l'allocation mémoire, et pas à initialiser"<br>
"Sur des systèmes distribués, comme on ne maitrise ni la mémoire ni le temps, la responsabilité du cycle de vie de l'objet ne devrait pas revenir au Client"<br>

Pourquoi le Skeleton devrait implémenter l'interface ??<br>
Le Stub implémente l'interface uniquement pour avoir la garantie que le Client peut appeler stub.methode (c'est une condition nécessaire pour compiler le Client)<br>
Mais le Skeleton n'est pas concerné puisque c'est lui qui appelle Matlab ! Le Skeleton compilera toujours sans problème<br>
À la limite si l'on veut suivre la même logique c'est Matlab qui devrait implémenter l'interface.

## Problèmes relatifs à Java
- penser à ajouter le nom de package
- ObjectStream vs. DataStream : on doit avoir un seul flux de données pour transmettre des données du Stub au Skeleton, mais on a des types primitifs et des objets<br>
Comment tous les envoyer ? L'interface ne doit pas utiliser de types primitifs.
- l'interface n'est pas lue à la volée : elle doit être recompilée à chaque fois

- la connection reste ouverte ou pas ? Différentes solutions<br>
1 - on crée une fonction close() que l'utilisateur doit appeler<br>
2 - chaque méthode ouvre et ferme ses flux<br>
3 - on silence les erreurs générées dans le Skeleton<br>

- Erreur assez bizarre :
Si le Stub et le Skeleton créent tous les deux un inputStream d'abord et un outputStream ensuite :
```java
    ObjectInputStream ois  = new ObjectInputStream(s.getInputStream());
    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
```
Le fil d'exécution est bloqué ! (sans lever d'Exception ni rien, juste bloqué au moment de la création de l'inputStream)
