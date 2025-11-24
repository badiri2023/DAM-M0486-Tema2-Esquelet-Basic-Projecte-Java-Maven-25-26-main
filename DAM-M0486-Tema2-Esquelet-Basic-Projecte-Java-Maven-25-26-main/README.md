# Plantilla projecte Java amb Maven DAM2-MP06 #

## Arrencada ràpida ##
Execució ràpida dels diferents exemples i resolusions de problemes

## Windows ##
```bash
.\run.ps1 cat.iesesteveterradas.Main
```

## Linux ##
```bash
run.sh  cat.iesesteveterradas.Main
```

## Execució i funcionament ##

### Amb els scripts propis inclosos ###

#### Windows ####
```bash
.\run.ps1 <cat.iesesteveterradas.Main> <param1> <param2> <param3>
run.sh <cat.iesesteveterradas.Main> <param1> <param2> <param3>
```

#### Linux ####
```bash
.\run.ps1 <cat.iesesteveterradas.Main> <param1> <param2> <param3>
run.sh <cat.iesesteveterradas.Main> <param1> <param2> <param3>
```

On:
* <cat.iesesteveterradas.Main>: és la classe principal que vols executar.
* \<param1>, \<param2>, \<param3>: són els paràmetres que necessites passar a la teva aplicació.


### Usant mvn ###

Cal el 'Maven' per compilar el projecte
```bash
mvn clean
mvn compile
```

Per executar sense usar script propi, directament amb maven:
```bash
mvn exec:java "-Dexec.mainClass=cat.iesesteveterradas.Main" <param1> <param2> <param3>
```

Per executar, un cop generat l'artefacte .jar
```bash
java -cp ./target/<nom de l'artefacte>.jar cat.iesesteveterradas.Main <param1> <param2> <param3>
```

### Execució de tests ###
Per executar, un cop generat l'artefacte .jar
```bash
# Executar TOTS els tests
mvn test
# Executar un test individual especificant package o només nom del test
mvn test "-Dtest=cat.iesesteveterradas.MainTest"
mvn test -Dtest=MainTest
# Executar múltiples tests específics (separats per comes)
mvn test -Dtest="MainTest,..."
# Tots els tests que comencin amb "Lectura"
mvn test -Dtest="Lectura*"
# Tots els tests que continguin "Arxiu"
mvn test -Dtest="*Arxiu*"
# Tests específics de List i Scanner
mvn test -Dtest="*List*,*Scanner*"
```

## Ajuda Visual Studio Code: resseteig de l'entorn de programació Java ##

Si Visual Studio code no es comporta com esperem i hem provat a solucionar-ho sense èxit podem provar aquestes dues solucions:

* Recarregar la Finestra: Obre la Paleta de Comandes (**Ctrl+Maj+P**), escriu Developer: "**Reload Window**" i prem Enter.

* Netejar l'Espai de Treball: Si recarregar no funciona, obre de nou la Paleta de Comandes (**Ctrl+Maj+P**), escriu "**Java: Clean Java Language Server Workspace**" i prem Enter. Aquesta és una solució molt eficaç per a molts problemes relacionats amb Java a VS Code. Se't demanarà que recarreguis i tornis a escanejar el projecte.