package com.stocks;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.stocks.model.BseIciciMapping;
import com.stocks.service.StockService;

public class BseIciciMappingBean {
    private String stockCode;
    private Integer scCode;
    
    // Services
    private StockService stockService;

    // Getter / Setters
    public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public Integer getScCode() {
		return scCode;
	}
	public void setScCode(Integer scCode) {
		this.scCode = scCode;
	}
	
	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public void getBseScCode(final ActionEvent ae){
		try {
			this.setScCode( this.getStockService().getBseScCode(this.getStockCode()) );
		} catch (Exception e) {
			e.printStackTrace();
			this.setScCode(0);
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}

	// Bean Actions
	public void save(){
		BseIciciMapping bseIciciMapping = new BseIciciMapping();
		bseIciciMapping.setStockCode(getStockCode().toUpperCase());
		bseIciciMapping.setScCode(getScCode());
		try {
			getStockService().saveBseIciciMapping(bseIciciMapping);
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Bse Icici Mapping Saved", "Bse Icici Mapping Saved"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}
}
