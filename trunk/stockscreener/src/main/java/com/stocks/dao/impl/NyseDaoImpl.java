package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.NyseDao;
import com.stocks.model.Nyse;
import com.stocks.model.NysePK;

public class NyseDaoImpl extends AbstractDao implements NyseDao {
	public void delete(Nyse nyse) {
		entityManager.remove(nyse);
	}

	public List<Nyse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public Nyse read(NysePK nysePK) {
		return get(Nyse.class, nysePK);
	}

	public void save(Nyse nyse) {
		try{
			entityManager.persist(nyse);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
