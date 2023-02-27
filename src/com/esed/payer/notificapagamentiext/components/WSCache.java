package com.esed.payer.notificapagamentiext.components;

public class WSCache
{
	public static PgEcCommonsServer pgEcCommonsServer = null;
	
	
	
	public static boolean initiatePgEcCommonsServer(String address) throws Exception
	{
		try {
			pgEcCommonsServer = new PgEcCommonsServer(address);
		} catch (Exception e) {
			throw e;
		}
		return pgEcCommonsServer != null ? true : false;
	}
	
	
}