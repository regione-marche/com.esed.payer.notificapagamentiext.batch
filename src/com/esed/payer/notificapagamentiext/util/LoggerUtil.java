package com.esed.payer.notificapagamentiext.util;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.esed.log.req.dati.LogPap;
import com.esed.log.req.dati.LogWin;
import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniContext;
import com.esed.payer.notificapagamentiext.config.PropertiesPath;
import com.seda.commons.properties.tree.PropertiesTree;

public class LoggerUtil {

	private NotificaPagamentiEsterniContext notificaPagamentiEsterniContext;
	
	public void saveWinLog(LogWin logWin , PropertiesTree configTree) {

			String uri = "";

			notificaPagamentiEsterniContext =  new NotificaPagamentiEsterniContext();
			notificaPagamentiEsterniContext.setConfig(configTree);
			
			if ( notificaPagamentiEsterniContext.geturlLogWs() != null 
					&& !notificaPagamentiEsterniContext.geturlLogWs().equals("")){
				 uri = notificaPagamentiEsterniContext.geturlLogWs() + "/saveWinLogger";
				 System.out.println("uri ws LOG = " + uri);
			}
			
			
			if(logWin != null && uri != "") {
				
				Entity<LogWin> entity =  Entity.entity(logWin, MediaType.APPLICATION_JSON);

				WsLogRequestThread wsLogRequestThread = new WsLogRequestThread(uri, entity);
				Thread thread = new Thread(wsLogRequestThread);
				thread.start();		
				
			}
		
		
	}


}
