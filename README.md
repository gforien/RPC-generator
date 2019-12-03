# rpcgen

## :wrench: Tester le projet
    ~/rpcgen/ $ make compilo
    ~/rpcgen/ $ make all
    ~/rpcgen/ $ java rpc.CalculSkeleton
    ~/rpcgen/ $ java rpc.Client

## Problèmes RPC
:warning: Le constructeur ne fait pas partie de l'interface<br>
le constructeur qui prend des parametres c'est une erreur<br>
le constructeur devrait juste servir à faire l'allocation mémoire, et pas à initialiser<br>

Sur des systèmes distribués, comme on ne maitrise ni la mémoire ni le temps, la responsabilité du cycle de vie de l'objet ne devrait pas revenir au Client

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

    ObjectInputStream ois  = new ObjectInputStream(s.getInputStream());
    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
Le fil d'exécution est bloqué ! (sans lever d'Exception ni rien, juste bloqué au moment de la création de l'inputStream)

