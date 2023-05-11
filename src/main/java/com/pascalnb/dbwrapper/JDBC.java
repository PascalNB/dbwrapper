package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The JDBC implementation of [Database].
 */
class JDBC extends Database {

    private static final int FETCH_SIZE = 500;

    private Connection connection = null;
    private Statement statement = null;

    @Contract("-> this")
    @Override
    public Database connect() {
        if (getUrl() == null) {
            throw new DatabaseException("URL for database connection not set.");
        }
        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return this;
    }

    @Override
    protected void checkConnection() throws DatabaseException {
        if (connection == null || statement == null) {
            throw new DatabaseException("No connection to the database exists or it has already been closed.");
        }
    }

    @Contract("_ -> this")
    @Override
    public Database execute(Query query) {
        checkConnection();
        try {
            statement.execute(query.toString());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public Database query(Consumer<Table> callback, Query query) {
        checkConnection();
        try {
            statement.setFetchSize(FETCH_SIZE);
            callback.accept(parseResult(statement.executeQuery(query.toString())));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return this;
    }

    @Contract("_, _ -> param1")
    private PreparedStatement setVariables(PreparedStatement statement, Object... variables) {
        try {
            statement.setFetchSize(FETCH_SIZE);

            for (int i = 0; i < variables.length; i++) {
                int index = i + 1;
                Object variable = variables[i];
                if (variable == null) {
                    statement.setObject(index, null);
                    continue;
                }

                if (variable instanceof Integer) {
                    statement.setInt(index, (int) variable);
                } else if (variable instanceof Double) {
                    statement.setDouble(index, (double) variable);
                } else if (variable instanceof Boolean) {
                    statement.setBoolean(index, (boolean) variable);
                } else if (variable instanceof Long) {
                    statement.setLong(index, (long) variable);
                } else if (variable instanceof Float) {
                    statement.setFloat(index, (float) variable);
                } else if (variable instanceof Byte) {
                    statement.setByte(index, (byte) variable);
                } else if (variable instanceof Short) {
                    statement.setShort(index, (short) variable);
                } else {
                    statement.setString(index, (String) variable);
                }

            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return statement;
    }

    @Override
    public Database queryStatement(Consumer<Table> callback, Query query) {
        checkConnection();
        try {
            callback.accept(
                parseResult(
                    setVariables(
                        connection.prepareStatement(query.toString()),
                        query.getArgs()
                    ).executeQuery()
                )
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return this;
    }

    @Override
    public Database executeStatement(Query query) {
        checkConnection();
        try {
            setVariables(connection.prepareStatement(query.toString()), query.getArgs()).execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return this;
    }

    @Override
    public void close() {
        checkConnection();
        try {
            connection.commit();
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
        statement = null;
    }

    // specific implementation to parse a ResultSet to a Table
    @Contract(value = "_ -> new")
    private Table parseResult(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] attributes = new String[columnCount];

            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = metaData.getColumnName(i + 1);
            }

            List<String[]> tuples = new ArrayList<>();

            while (resultSet.next()) {
                String[] tuple = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    tuple[i] = resultSet.getString(i + 1);
                }

                tuples.add(tuple);
            }

            return new Table(attributes, tuples);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}