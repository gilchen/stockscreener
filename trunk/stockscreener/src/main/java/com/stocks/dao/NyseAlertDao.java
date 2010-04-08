package com.stocks.dao;

import java.util.List;

import com.stocks.model.NyseAlert;
import com.stocks.search.NyseAlertResult;

public interface NyseAlertDao extends Dao<NyseAlert, Long> {
	List<NyseAlert> findAlertsByTrxType(final String trxType);
	List<NyseAlertResult> findAlertResultsByTrxType(final String trxType);
}
