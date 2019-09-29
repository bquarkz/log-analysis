#LOG ANALYSIS

Log analysis uses 3 processors:

> 1. EventProcessor: receive a batch of jsons from file, and parse every json into an entry and merge it into an EventManager. 
>When EventManager was bind for two entries we have an event and it will be send to Storage Complete Processor. When some 
>EventManager just have one entry in the end of the batch it will be send to Merge Remainder Processor.    
> 
> 1. Merge Remainder Processor: it will merge all remainder EventManager (if it is possible) or follow back the number 
>of remainders (when finished all operations) as entry problems.
>
> 1. Storage Complete Processor: store into the database all events using batch insertions.

##Stack
* We will use spring boot and spring-jdbc
* Flyway was added just to show something else but it was not necessary to be honest
* JDBCPool from HSQLDB (it is better than nothing)
* JCommander to easily parser command lines
* JUnit and Mockito for tests, and EnhancedRandomBeans (currently Easy-Random)

##Build and Run
First of all, run `gradle bootjar` to build the project and the java executable will stay on `launcher-app/bin/libs/`, after that you could run teh application with 
```
java -jar launcher-app/build/libs/launcher-app-1.0-SNAPSHOT.jar -f PATH-LOG-FILE.txt
```

If you want to send all log output to stdout as well do:
```
java -Dspring.profiles.active=stdout -jar launcher-app/build/libs/launcher-app-1.0-SNAPSHOT.jar -f PATH-LOG-FILE.txt
```
and if you want to "hide" all debug messages just add `no-debug` on active profiles:
```
java -Dspring.profiles.active=stdout,no-debug -jar launcher-app/build/libs/launcher-app-1.0-SNAPSHOT.jar -f PATH-LOG-FILE.txt
```

##Thoughts
* we could use Guice or Dagger2 as dependency injection frameworks as well but Spring sounds more relevant 
* no sure if the entry state (STARTED or FINISHED) should follow the timestamp
* the biggest problem for large files still when we have to insert on database, probably the best way should use batch insertions
    * we could easily see it on visualVM for large files, when thread-03 (storage thread) still for long time running even when other threads are stopped 
* for the biggest file that I have tested: ~1.3G (~12 millions of entries or ~6 millions of events) took ~148s to run
* log-analysis accepts `#` as a line skipper
* empty lines will be skipped as well
* bad entries will be discarded:
    * id is empty or null
    * timestamp is null, zero or negative
    * state doesn't fit with STARTED and FINISHED
* tests should be improved
 
##Job Diary
1. start project with git init and gradle init
1. creation of `.gitignore` (i just copy from another project)
1. build directories: `mkdir {core-domain,core-service,dbc-adapter,dbc-persistence,port-controller,port-persistence}/src/{main,test}/{java,resources}`
1. spend some time planning how to achieve huge files
1. at some stage I wrote a script to create files with random content
1. start to code core-domain and core-service with tests
1. running some tests against really small files (from tests environment)
1. build the controller (command line interface)
1. build the persistence layer:
    * I got some problems to use HSQLDB with HikariCP and replaced it for JDBCPool instead