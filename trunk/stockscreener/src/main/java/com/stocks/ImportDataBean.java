package com.stocks;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.stocks.service.impl.BseImport;

public class ImportDataBean {    
    // Services
    private BseImport bseImport;

    // Getter / Setters
	public BseImport getBseImport() {
		return bseImport;
	}

	public void setBseImport(BseImport bseImport) {
		this.bseImport = bseImport;
	}

	// Bean Actions
	public void importBse(final ActionEvent ae){
		try {
			this.getBseImport().importData();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Bse Data Imported", "Bse Data Imported"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}
}
