package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.KeyValueDao;
import com.stocks.model.KeyValue;

public class KeyValueDaoImpl extends AbstractDao implements KeyValueDao {
	public List<KeyValue> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public void delete(KeyValue keyValue) {
		entityManager.remove(keyValue);
	}

	public KeyValue read(String k) {
		return get(KeyValue.class, k);
	}

	public void save(KeyValue keyValue) {
		try{
			entityManager.merge(keyValue);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
