package com.stocks.service;

import java.util.List;

import com.stocks.model.Alert;
import com.stocks.model.BseIciciMapping;
import com.stocks.model.KeyValue;

public interface StockService {
	void saveAlert(Alert alert) throws Exception;
	List<Alert> getAllAlerts();
	
	void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception;
	Integer getBseScCode(String stockCode) throws Exception;

	KeyValue getKeyValue(String key);
	void saveKeyValue(KeyValue keyValue) throws Exception;
}
