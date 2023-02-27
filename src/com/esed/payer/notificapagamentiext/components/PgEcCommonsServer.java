package com.esed.payer.notificapagamentiext.components;

import java.rmi.RemoteException;


import javax.xml.rpc.ServiceException;

import com.seda.payer.pgec.webservice.commons.dati.ConfigPagamentoSingleRequest;
import com.seda.payer.pgec.webservice.commons.dati.ConfigPagamentoSingleResponse;
import com.seda.payer.pgec.webservice.commons.dati.RecuperaTransazioneRequestType;
import com.seda.payer.pgec.webservice.commons.dati.RecuperaTransazioneResponseType;
import com.seda.payer.pgec.webservice.commons.source.CommonsSOAPBindingStub;
import com.seda.payer.pgec.webservice.commons.source.CommonsServiceLocator;
import com.seda.payer.pgec.webservice.commons.srv.FaultType;


public class PgEcCommonsServer extends BaseServer {
	private CommonsSOAPBindingStub commonsCaller = null;

	private void setCodSocietaHeader(CommonsSOAPBindingStub stub, String dbSchemaCodSocieta) {
		stub.clearHeaders();
		stub.setHeader("",DBSCHEMACODSOCIETA,dbSchemaCodSocieta);		
	}
	
	public PgEcCommonsServer(String endPoint) throws ServiceException
	{
		CommonsServiceLocator lsService = new CommonsServiceLocator();
		lsService.setCommonsPortEndpointAddress(endPoint);
		//instanzio l'interfaccia del webservice
		commonsCaller = (CommonsSOAPBindingStub)lsService.getCommonsPort();
	}
	
	
	
	public RecuperaTransazioneResponseType recuperaTransazione(String dbSchemaCodSocieta, RecuperaTransazioneRequestType in) throws FaultType, RemoteException
    {	
    	setCodSocietaHeader(commonsCaller, dbSchemaCodSocieta);
    	//stored PYTRASP_SEL
    	return this.commonsCaller.recuperaTransazione(in);
    }
	
	public ConfigPagamentoSingleResponse recuperaFunzioneEnte(String dbSchemaCodSocieta, ConfigPagamentoSingleRequest in) throws FaultType, RemoteException
	{
		setCodSocietaHeader(commonsCaller, dbSchemaCodSocieta);
		//stored PYCESSP_SEL_CONFIG
		return commonsCaller.recuperaFunzioneEnte(in);
	}
	
	
}
