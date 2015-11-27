Building RUPS
-------------

Install a recent JDK, [Maven](http://maven.apache.org/), and type:

```
mvn install
```

Running a maven build without a profile will just build the jar.

Running the build with profiles (`-P profile`):

profile name | build actions
------------ | -------------
all          | generate jar, jar with dependencies in it, sources jar, javadoc jar
exe          | create a windows exe to run RUPS
