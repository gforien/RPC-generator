# RPC-genenerator :pencil::twisted_rightwards_arrows::package:
![SYD TD1](https://img.shields.io/static/v1.svg?label=SYD&message=TD1&color=2aaee6&style=flat)
![License](https://img.shields.io/static/v1.svg?label=License&message=None&color=aaaaaa&style=flat)<br>
Un "compilateur" prend une interface quelconque et génère une architecture réseau **Stub <=> Skeleton** transparente pour le développeur.

## :construction_worker: Tester le projet
```sh
    ~/rpcgen/ $ make
    ~/rpcgen/ $ java rpc.CalculSkeleton
    ~/rpcgen/ $ java rpc.Client
```

## :rocket: Créer sa propre interface
:warning: L'interface n'est pas lue à la volée : elle doit être recompilée à chaque fois
```sh
    ~/rpcgen/ $ javac rpc/HelloIfc.java
    ~/rpcgen/ $ java  rpc.Compilateur rpc/HelloIfc.java
    ~/rpcgen/ $ make
```

## Gérer les exceptions
*Uniquement pour le Stub (le Skeleton n'a pas besoin d'implémenter l'interface).*
Soit une méthode qui définit `throws IOException` :
 - si le Stub définit la même exception, tout va bien
 - si le Stub définit une exception parente (ex: `throws Exception`), il ne compilera pas
 - si le Stub définit une exception qui n'a rien à voir, tout va bien
 - si le Stub ne définit pas cette exception, il doit la catcher (ex: `catch IOException`)

#### NB
On peut déclarer une exception même si aucune méthode n'a déclaré cette exception throwable.<br>
Mais on ne peut pas catcher une exception si elle n'est jamais throw dans le bloc try{}<br>
On peut catcher une `ClassNotFoundException` même si on cast vers un type primitif.<br>

#### De plus
Toutes les méthodes de l'interface peuvent provoquer des `IOException` (à cause des appels à `readObject`, `writeObject`, `writeUTF`, `flush`)<br>
Toutes les méthodes qui reçoivent un résultat doivent le caster et peuvent provoquer une `ClassNotFoundException`<br>

#### Solution trouvée
Pour chaque méthode, le Stub définit les mêmes exceptions que l'interface, et dans chaque méthode on catch `IOException` et `ClassNotFoundException` qui ne dépendent pas du développeur.<br>
=> On ne laisse pas le développeur gérer lui même ses IOExceptions ou ClassNotFoundException, mais on le laisse gérer tout le reste.

## Gérer les modifiers
Uniquement pour le Stub (le Skeleton n'a pas besoin d'implémenter l'interface).
On n'a pas besoin de vérifier la pertinence ou la logique : si l'interface est compilée, Java a déjà tout vérifié pour nous.

| Modifier      | Conséquence |
|---------------|-------------|
| PUBLIC        | toutes les méthodes d'une interface sont implicitement publiques |
| PROTECTED     | interdit dans une interface |
| PRIVATE       | interdit dans une interface |
| ABSTRACT      | interdit dans une interface |
| STATIC        | on ignore la méthode |
| FINAL         | interdit dans une interface |
| SYNCHRONIZED  | interdit dans une interface |
| NATIVE        | interdit dans une interface |
| STRICT        | interdit dans une interface |
| INTERFACE     | x |
| TRANSIENT     | uniquement pour variables |
| VOLATILE      | uniquement pour variables |
