package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.NyseTxDao;
import com.stocks.model.NyseTx;

public class NyseTxDaoImpl extends AbstractDao implements NyseTxDao {
	public void delete(NyseTx nyseTx) {
		entityManager.remove(nyseTx);
	}

	public List<NyseTx> findAll() {
		return entityManager.createNamedQuery("allNyseTransactions").getResultList();
	}
	
	public List<NyseTx> findNyseTransactionsBySymbol(final String symbol) {
		return entityManager.createNamedQuery("nyseTransactionsBySymbol").setParameter("symbol", symbol).getResultList();
	}
	
	public NyseTx read(Long id) {
		return get(NyseTx.class, id);
	}

	public void save(NyseTx nyseTx) {
		try{
			if( nyseTx.getNyseTxId() == null ){
				entityManager.persist(nyseTx);
			}else{
				entityManager.merge(nyseTx);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
