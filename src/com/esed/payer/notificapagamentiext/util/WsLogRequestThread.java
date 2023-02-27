package com.esed.payer.notificapagamentiext.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WsLogRequestThread implements Runnable {
	
	private String uri = null;
	
	private Entity<?> entity = null;
	
	public WsLogRequestThread(String uri, Entity<?> entity) {
		super();
		this.uri = uri;
		this.entity = entity;
	}	

	public void run() {

		Client client = ClientBuilder.newClient();
		Response responseWS = null;
		try {
			
			WebTarget target = client.target(uri);
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			responseWS = builder.post(entity);
			if(responseWS != null) {
				responseWS.close();
				responseWS = null;
			}
		} catch (Exception e) {
			System.out.println("WsLogRequestThread::run errore: " + e.getMessage());
		} finally {
			client.close();
			if(responseWS != null)
				responseWS.close();
			
		}
	}		
	
}
