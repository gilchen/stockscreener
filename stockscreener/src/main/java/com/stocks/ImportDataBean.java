package com.stocks;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.stocks.model.BseIciciMapping;
import com.stocks.service.StockService;

public class ImportDataBean {    
    // Services
    private StockService stockService;

    // Getter / Setters
	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	// Bean Actions
	public void importBse(final ActionEvent ae){
		try {
			//this.getStockService().getBseScCode(this.getStockCode());
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}
}
