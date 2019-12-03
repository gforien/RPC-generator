# tp-rpc

## Problèmes relatifs au langage Java
- nom de package : tous les types sont de la forme rpc.foo.bar
- ObjectStream vs. DataStream : on doit avoir un seul flux de données pour transmettre des données Stub - Skeleton, mais on a des types primitifs et des objets, comment tous les envoyer ? OptionalDataException
Solution la plus simple -> écrire une interface uniquement avec des objets.
- l'interface n'est pas lue à la volée : elle doit être recompilée à chaque fois
- la connection reste ouverte ou pas ?
1 - on crée une fonction close() que l'utilisateur doit appeler
2 - chaque méthode ouvre et ferme ses flux
3 - on silence les erreurs générées dans le Skeleton


// piege : le constructeur ne fait pas partie de l'interface
// le constructeur qui prend des parametres c'est une erreur
// le constructeur devrait juste servir à faire l'allocation mémoire, et pas à initialiser

// sur des SYD, comme on ne maitrise ni la mémoire ni le temps, la responsabilité du cycle de vie
// de l'objet ne devrait pas être au client

Erreur assez bizarre :
Si le Stub et le Skeleton créent tous les deux un inputStream d'abord et un outputStream ensuite :

    ObjectInputStream ois  = new ObjectInputStream(s.getInputStream());
    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
Le fil d'exécution est bloqué !
(sans lever d'Exception ni rien, juste bloqué au moment de la création de l'inputStream)