package com.stocks.dao;

import java.util.List;

/**
 * Common interface for all the Dao interfaces. Mainly used to hold common constants between all interfaces.
 */
public interface Dao<T, E> {
    /**
     * The maximum number of records that a query should return.
     */
    static final int MAX_RESULTS = 100;

    List<T> findAll();

    void save(T entity);

    T read(E id);

    void delete(T source);
}
