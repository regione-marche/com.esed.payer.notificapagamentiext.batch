package com.esed.payer.notificapagamentiext.components;


import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniResponse;
import com.seda.bap.components.core.BapException;
import com.seda.bap.components.core.spi.ClassRunnableHandler;
import com.seda.commons.management.ManagementException;

public class NotificaPagamentiEsterniBap extends ClassRunnableHandler {
	public void run(String[] args) throws BapException {
		
		//Stampa parametri di input - inizio
		for (int i=0; i< args.length;i++) {
			System.out.println( "argomento[" + i +"] " + args[i] );   
		} 
		//In caso di esecuzione da BAP:
		String[] parameters = getParameters();
		for (int i=0; i< parameters.length;i++) {
			System.out.println( "param[" + i +"] [" + parameters[i] + "]" );   
		}
		//Stampa parametri di input - fine
		
		NotificaPagamentiEsterniCore notificaPagamentiEsterniCore= new NotificaPagamentiEsterniCore();
		NotificaPagamentiEsterniResponse res = null;
 		
//		try {
			res =  notificaPagamentiEsterniCore.run(this.getParameters(), 
															 this.getDataSource(), 
															 this.getSchema(),
//															 this.lookupDataSource("HOST"), 
//															 this.lookupDataSourceMetaData("HOST")!=null?this.lookupDataSourceMetaData("HOST").getSchema():null, 
															 printer(),
															 logger(),
															 this.getJobId());
//		} catch (ManagementException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		this.setCode(res!=null?res.getCode():"");
		this.setMessage(res!=null?res.getMessage():"");
	}
}
