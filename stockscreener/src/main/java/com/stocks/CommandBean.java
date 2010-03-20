package com.stocks;

import javax.faces.event.ActionEvent;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.impl.ContextBase;

public class CommandBean {
    private Chain reportChain;

	public Chain getReportChain() {
		return reportChain;
	}

	public void setReportChain(Chain reportChain) {
		this.reportChain = reportChain;
	}

	public void executeReportChain(ActionEvent ae){
		try{
			System.out.println( "executeReportChain." );
			reportChain.execute(new ContextBase());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
