package com.stocks.dao;

import java.util.List;

import com.stocks.model.NyseTx;

public interface NyseTxDao extends Dao<NyseTx, Long> {
	List<NyseTx> findNyseTransactionsBySymbol(final String symbol);
}
