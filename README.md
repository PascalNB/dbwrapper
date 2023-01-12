# dbwrapper

A wrapper to quickly make database queries with asynchronous callbacks.
1. [Setup](#setup)
2. [Querying](#querying)
3. [Executing](#executing)
4. [Value Mapping](#value-mapping)
5. [Custom Executor](#custom-executor)
6. [Combining Database Actions](#combining-database-actions)

## Setup

Add jitpack dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add dbwrapper to project:

```xml
<dependency>
    <groupId>com.github.PascalNB</groupId>
    <artifactId>dbwrapper</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

Add ``config.cfg`` containing database credentials, url and JDBC driver to resources folder or the same folder as jar:

```properties
username="<username>"
password="<password>"
url="jdbc:<driver>://<host>:<port>/<database>"
driver="<driver class>"
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
```

Given the following class:

```java
public class User {

    @ParseField // annotate with ParseField
    private int id;

    @ParseField("username") // specifiy different name
    private String name;

    public User() {} // constructor without arguments required

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
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
```

## Combining database actions

Database actions can be combined as follows:

```java
DatabaseAction.allOf(
        Mapper.to(Long.class),
        DatabaseAction.of("SELECT count(*) FROM users WHERE id=?;", 154),
        DatabaseAction.of("SELECT count(*) FROM users WHERE username=?;", "username")
    )
    .query()
    .thenAccept(counts -> {
        for (long count : counts){
            // do stuff
        }
    });
```