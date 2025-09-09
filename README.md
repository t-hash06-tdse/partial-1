# Como clonar

```bash
git clone https://github.com/t-hash06-tdse/partial-1
cd partial-1
```

## Ejecutar servidor

```bash
mvn exec:java -D"exec.mainClass"="parcial.AppBackend"
```

## Ejecutar facade

```bash
mvn exec:java -D"exec.mainClass"="parcial.AppFacade"
```

## Probar

Para probarlo es suficiente con ir a [http://localhost:8080/](http://localhost:8080/) y utilizar la pagina web