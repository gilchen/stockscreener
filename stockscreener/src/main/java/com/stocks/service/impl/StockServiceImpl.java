package com.stocks.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.dao.AlertDao;
import com.stocks.model.Alert;
import com.stocks.service.StockService;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class StockServiceImpl implements StockService {
    @Autowired(required = true)
    private PlatformTransactionManager transactionManager;

    private AlertDao alertDao;
    
	public AlertDao getAlertDao() {
		return alertDao;
	}

	@Required
	public void setAlertDao(AlertDao alertDao) {
		this.alertDao = alertDao;
	}

	public List<Alert> getAllAlerts() {
		return getAlertDao().findAll();
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveAlert(Alert alert) {
		getAlertDao().save(alert);
	}

}
