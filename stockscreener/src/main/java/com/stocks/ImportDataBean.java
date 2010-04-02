package com.stocks;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.springframework.beans.factory.annotation.Required;

import com.stocks.service.ImportService;
import com.stocks.service.StockService;

public class ImportDataBean{    
    // Services
    private ImportService bseImportService;
    private ImportService nyseImportService;
    private StockService stockService;

    private Date lastBseImport;
    private Date lastNyseImport;
    private Date lastNasdaqImport;
    private Date lastAmexImport;
    
    // Getter / Setters
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public ImportService getBseImportService() {
		return bseImportService;
	}

	@Required
	public void setBseImportService(ImportService bseImportService) {
		this.bseImportService = bseImportService;
	}

	public ImportService getNyseImportService() {
		return nyseImportService;
	}

	@Required
	public void setNyseImportService(ImportService nyseImportService) {
		this.nyseImportService = nyseImportService;
	}

	public Date getLastBseImport() throws Exception {
		final String lastBseImport = getStockService().getKeyValue(ImportService.BSE_IMPORT_KEY).getV();

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		
		return sdf.parse(lastBseImport);
	}

	public void setLastBseImport(Date lastBseImport) {
		this.lastBseImport = lastBseImport;
	}

	public Date getLastNyseImport() throws Exception {
		final String lastNyseImport = getStockService().getKeyValue(ImportService.NYSE_IMPORT_KEY).getV();
	
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		
		return sdf.parse(lastNyseImport);
	}

	public void setLastNyseImport(Date lastNyseImport) {
		this.lastNyseImport = lastNyseImport;
	}
	
	public Date getLastNasdaqImport() throws Exception{
		final String lastNasdaqImport = getStockService().getKeyValue(ImportService.NASDAQ_IMPORT_KEY).getV();
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		
		return sdf.parse(lastNasdaqImport);
	}

	public void setLastNasdaqImport(Date lastNasdaqImport) {
		this.lastNasdaqImport = lastNasdaqImport;
	}

	public Date getLastAmexImport() throws Exception{
		final String lastAmexImport = getStockService().getKeyValue(ImportService.AMEX_IMPORT_KEY).getV();
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		
		return sdf.parse(lastAmexImport);
	}

	public void setLastAmexImport(Date lastAmexImport) {
		this.lastAmexImport = lastAmexImport;
	}

	// Bean Actions
	public void importBse(final ActionEvent ae){
		try {
			this.getBseImportService().importData();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Bse Data Imported", "Bse Data Imported"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}

	public void importNyse(final ActionEvent ae){
		try {
			this.getNyseImportService().importData();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Nyse Data Imported", "Nyse Data Imported"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}
	
}
