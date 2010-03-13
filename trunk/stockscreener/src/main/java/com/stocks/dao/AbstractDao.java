package com.stocks.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractDao {
    @PersistenceContext
    protected EntityManager entityManager;

    final Log log = LogFactory.getLog(this.getClass());

    final List executeQuery(javax.persistence.Query query) {
        return query.getResultList();
    }

    final List findByNamedQueryAndNamedParam(final String queryName, final String paramName, final Object paramValue) {
        return executeQuery(entityManager.createNamedQuery(queryName).setParameter(paramName, paramValue));
    }

    final List findByNamedQueryAndNamedParam(final String queryName, final String[] params, final Object[] values) {
        final javax.persistence.Query q = entityManager.createNamedQuery(queryName);

        for (int x = 0; x < params.length; x++) {
            q.setParameter(params[x], values[x]);
        }

        return executeQuery(q);
    }

    @SuppressWarnings({"unchecked"})
    public final <T> T get(Class<T> clazz, Serializable id) {
        return entityManager.find(clazz, id);
    }

    final Object findOne(final Query query) {
        List list = query.getResultList();

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
