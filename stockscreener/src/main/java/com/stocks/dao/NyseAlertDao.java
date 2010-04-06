package com.stocks.dao;

import java.util.List;

import com.stocks.model.NyseAlert;

public interface NyseAlertDao extends Dao<NyseAlert, Long> {
	List<NyseAlert> findAlertsByTrxType(final String trxType);
}
