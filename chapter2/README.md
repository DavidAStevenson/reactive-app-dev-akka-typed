# reactive-app-dev-typed - chapter2

## running

1) Run Guidebook:

```
sbt "runMain GuidebookMain"
```

2) Run Tourist:

```
sbt "runMain TouristMain"
```

To run with akka cluster logging turned off:
(in different terminals)
```
sbt "-Dakka.cluster.log-info=off" "runMain TouristMain"
sbt "-Dakka.cluster.log-info=off" "runMain GuidebookMain"
```

To run with more than one Tourist:
(in different terminals)
```
sbt "runMain GuidebookMain"
sbt "runMain TouristMain"
sbt "runMain TouristMain 25253"
```
