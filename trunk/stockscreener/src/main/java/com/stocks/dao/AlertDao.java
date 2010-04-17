package com.stocks.dao;

import java.util.List;

import com.stocks.model.Alert;
import com.stocks.search.AlertResult;

public interface AlertDao extends Dao<Alert, Long> {
	List<AlertResult> findAlertResultsByTrxType(String trxType, String isActive);
}
