package com.stocks.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.dao.BseDao;
import com.stocks.model.Bse;
import com.stocks.service.StockImportService;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class StockImportServiceImpl implements StockImportService {
	@Autowired(required = true)
    private PlatformTransactionManager transactionManager;

    private BseDao bseDao;

	public BseDao getBseDao() {
		return bseDao;
	}

	@Required
	public void setBseDao(BseDao bseDao) {
		this.bseDao = bseDao;
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveBse(Bse bse) throws Exception {
		getBseDao().save(bse);
	}
    
    
}
