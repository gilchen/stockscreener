package com.stocks.dao;

import java.util.List;

import com.stocks.model.Alert;

public interface AlertDao extends Dao<Alert, Long> {
	List<Alert> findAlertsByTrxType(final String trxType);
}
