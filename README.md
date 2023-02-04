# dbwrapper

A wrapper to quickly make database queries with asynchronous callbacks.
1. [Setup](#setup)
2. [Querying](#querying)
3. [Executing](#executing)
4. [Value Mapping](#value-mapping)
5. [Custom Executor](#custom-executor)
6. [Combining Database Actions](#combining-database-actions)

## Setup

Add jitpack repository:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Add dbwrapper dependency to project:

```xml
<dependency>
    <groupId>com.github.PascalNB</groupId>
    <artifactId>dbwrapper</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

Add ``config.cfg`` containing the database credentials, connection URL and JDBC driver as resource or the in the folder the jar is contained in:

```properties
username=<username>
password=<password>
# e.g. jdbc:mysql://localhost:3306/database
url=jdbc:<driver>://<host>:<port>/<database>
# e.g. com.mysql.cj.jdbc.Driver
driver=<driver class>
```

Or set a custom way to authenticate the database:

```java
DatabaseAuthenticator.setImplementation(/* custom authenticator */);
```

## Querying

A database query can be made as follows:

```java
String version = DatabaseAction.of("SELECT version();")
    .query(Mapper.stringValue())
    .join();

System.out.println(version);
```

This can also be done asynchronously:

```java
DatabaseAction.of("SELECT version();")
    .query(Mapper.stringValue())
    .thenAccept(System.out::println);
```

Query arguments can be added as follows:

```java
DatabaseAction.of("SELECT * FROM users WHERE id=?;", 154)
    .query(Mapper.firstRow())
    .thenAccept(row -> {
        if (row == null) {
            return;
        }
        String username = row.get("username");
        // do stuff
    });
```

Reusing existing queries with different arguments:

```java
Query query = new Query("SELECT * FROM users WHERE id=?;");
DatabaseAction.of(query.withArgs(154))
    .query(Mapper.firstRow())
    .thenAccept(row -> {
        // do stuff
    });
DatabaseAction.of(query.withArgs(451))
    .query(Mapper.firstRow())
    .thenAccept(row -> {
        // do stuff
    });
```

## Executing

Executing without response:

```java
DatabaseAction.of("DELETE FROM users WHERE id=?;", 154)
    .execute();
```

## Value Mapping

Returned values can be mapped to primitives:

```java
int count = DatabaseAction.of("SELECT count(*) FROM users")
    .query(Mapper.toPrimitive(Integer.class)) // or Integer.TYPE
    .join();
```

They can also be mapped to objects with the `ParseField` annotation:

```java
User user = DatabaseAction.of("SELECT id, username FROM users WHERE id=?", 154)
    .query(Mapper.toObject(User.class))
    .join();

List<User> users = DatabaseAction.of("SELECT id, username FROM users WHERE username=?", "username")
    .query(Mapper.toObjects(User.class))
    .join();
```

Given the following class:

```java
public class User {

    @ParseField // annotate with ParseField
    private int id;

    @ParseField("username") // specifiy different name
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
```

## Custom executor

Select what executor to use the database actions on:

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
DatabaseAction.of("DELETE FROM users WHERE id=?;", 154)
    .withExecutor(executorService)
    .execute();

// run on current thread
DatabaseAction.of("DELETE FROM users WHERE id=?;", 154)
    .withExecutor(Runnable::run)
    .execute();
```

## Combining database actions

Multiple database actions can be combined as follows:

```java
Query query = new Query("SELECT * FROM users WHERE id=?")
DatabaseAction.allOf(
        Mapper.toObject(User.class),
        DatabaseAction.of(query.withArgs(154)),
        DatabaseAction.of(query.withArgs(541)),
        DatabaseAction.of("SELECT * FROM users WHERE username=?;", "username")
    )
    .query()
    .thenAccept(list -> {
        for (User user : list) {
            // do stuff
        }
    });
```