package com.stocks.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "report")
public class Report implements Serializable{
    private final static long serialVersionUID = 2l;
    
    public enum ReportName {BseAlertReportCommand, BseReportCommand, NyseReportCommand};

    @Id
    @Column(name="REPORT_NAME", length=50)
    private String reportName;
    
    @Column(name="CONTENT")
    private String content;
    
	public Report() {
	}

	public Report(String reportName, String content) {
		this.reportName = reportName;
		this.content = content;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
    

}
