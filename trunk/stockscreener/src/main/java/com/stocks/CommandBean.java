package com.stocks;

import javax.faces.event.ActionEvent;

import org.apache.commons.chain.impl.ContextBase;
import com.stocks.command.StockBoxCommand;

public class CommandBean {
    private StockBoxCommand stockBoxCommand;

	public StockBoxCommand getStockBoxCommand() {
		return stockBoxCommand;
	}

	public void setStockBoxCommand(StockBoxCommand stockBoxCommand) {
		this.stockBoxCommand = stockBoxCommand;
	}

	public void executeStockBoxCommand(ActionEvent ae){
		try{
			boolean returnCode = getStockBoxCommand().execute(new ContextBase());
			System.out.println( "returnCode: " +returnCode );
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
