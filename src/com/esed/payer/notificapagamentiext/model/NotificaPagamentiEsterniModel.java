package com.esed.payer.notificapagamentiext.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

public class NotificaPagamentiEsterniModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private String chiaveTransazione;
    private String codiceSocieta;
    private String codiceUtente;
    private String chiaveEnte;
    private int numeroTentativoNotifica;
    private String urlPortale;
    private String numeroDocumento;
    private String numeroAvviso;
    private String codiceFiscale;
    private Calendar dataInvioNotifica;
    private Calendar dataRispostaNotifica;
    private String ultimoEsitoNotifica;
    private BigDecimal importoPagato;
    private Calendar dataPagamento;
    private String xmlRicevuta;
    private String chiaveDettaglioTransazione;	//PG1800XX_014
    //inizio LP PG190220    	
    private int numeroTentativoNotificaAnnullo = -1;
    private Calendar dataInvioNotificaAnnullo = null;
    private Calendar dataRispostaNotificaAnnullo = null;
    private String ultimoEsitoNotificaAnnullo = "00";
    private String xmlRichiestaRevoca = "";
    //fine LP PG190220    	

    public NotificaPagamentiEsterniModel() {}
    

	public String getChiaveTransazione() {
		return chiaveTransazione;
	}

	public void setChiaveTransazione(String chiaveTransazione) {
		this.chiaveTransazione = chiaveTransazione;
	}

	public String getCodiceSocieta() {
		return codiceSocieta;
	}

	public void setCodiceSocieta(String codiceSocieta) {
		this.codiceSocieta = codiceSocieta;
	}

	public String getCodiceUtente() {
		return codiceUtente;
	}

	public void setCodiceUtente(String codiceUtente) {
		this.codiceUtente = codiceUtente;
	}

	public String getChiaveEnte() {
		return chiaveEnte;
	}

	public void setChiaveEnte(String chiaveEnte) {
		this.chiaveEnte = chiaveEnte;
	}

	public int getNumeroTentativoNotifica() {
		return numeroTentativoNotifica;
	}

	public void setNumeroTentativoNotifica(int numeroTentativoNotifica) {
		this.numeroTentativoNotifica = numeroTentativoNotifica;
	}

	public String getUrlPortale() {
		return urlPortale;
	}

	public void setUrlPortale(String urlPortale) {
		this.urlPortale = urlPortale;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getNumeroAvviso() {
		return numeroAvviso;
	}

	public void setNumeroAvviso(String numeroAvviso) {
		this.numeroAvviso = numeroAvviso;
	}

	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}

	public Calendar getDataInvioNotifica() {
		return dataInvioNotifica;
	}

	public void setDataInvioNotifica(Calendar dataInvioNotifica) {
		this.dataInvioNotifica = dataInvioNotifica;
	}

	public Calendar getDataRispostaNotifica() {
		return dataRispostaNotifica;
	}

	public void setDataRispostaNotifica(Calendar dataRispostaNotifica) {
		this.dataRispostaNotifica = dataRispostaNotifica;
	}

	public String getUltimoEsitoNotifica() {
		return ultimoEsitoNotifica;
	}

	public void setUltimoEsitoNotifica(String ultimoEsitoNotifica) {
		this.ultimoEsitoNotifica = ultimoEsitoNotifica;
	}

	public BigDecimal getImportoPagato() {
		return importoPagato;
	}

	public void setImportoPagato(BigDecimal importoPagato) {
		this.importoPagato = importoPagato;
	}

	public Calendar getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Calendar dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public String getXmlRicevuta() {
		return xmlRicevuta;
	}

	public void setXmlRicevuta(String xmlRicevuta) {
		this.xmlRicevuta = xmlRicevuta;
	}
	
	//PG1800XX_014 - inizio
	public String getChiaveDettaglioTransazione() {
		return chiaveDettaglioTransazione;
	}

	public void setChiaveDettaglioTransazione(String chiaveDettaglioTransazione) {
		this.chiaveDettaglioTransazione = chiaveDettaglioTransazione;
	}
	//PG1800XX_014 - fine

	//inizio LP PG190220    	
	public int getNumeroTentativoNotificaAnnullo() {
		return numeroTentativoNotificaAnnullo;
	}

	public void setNumeroTentativoNotificaAnnullo(int numeroTentativoNotificaAnnullo) {
		this.numeroTentativoNotificaAnnullo = numeroTentativoNotificaAnnullo;
	}

	public Calendar getDataInvioNotificaAnnullo() {
		return dataInvioNotificaAnnullo;
	}

	public void setDataInvioNotificaAnnullo(Calendar dataInvioNotificaAnnullo) {
		this.dataInvioNotificaAnnullo = dataInvioNotificaAnnullo;
	}

	public Calendar getDataRispostaNotificaAnnullo() {
		return dataRispostaNotificaAnnullo;
	}

	public void setDataRispostaNotificaAnnullo(Calendar dataRispostaNotificaAnnullo) {
		this.dataRispostaNotificaAnnullo = dataRispostaNotificaAnnullo;
	}

	public String getUltimoEsitoNotificaAnnullo() {
		return ultimoEsitoNotificaAnnullo;
	}

	public void setUltimoEsitoNotificaAnnullo(String ultimoEsitoNotificaAnnullo) {
		this.ultimoEsitoNotificaAnnullo = ultimoEsitoNotificaAnnullo;
	}

	public String getXmlRichiestaRevoca() {
		return xmlRichiestaRevoca;
	}

	public void setXmlRichiestaRevoca(String xmlRichiestaRevoca) {
		this.xmlRichiestaRevoca = xmlRichiestaRevoca;
	}

	public boolean DaEseguireNotifica() {
		return !(this.getUltimoEsitoNotifica().equals("00")); 
	}
	//fine LP PG190220    	

	@Override
	public String toString() {
		return "NotificaPagamentiEsterniModel [chiaveDettaglioTransazione="
				+ chiaveDettaglioTransazione + ", chiaveEnte=" + chiaveEnte
				+ ", chiaveTransazione=" + chiaveTransazione
				+ ", codiceFiscale=" + codiceFiscale + ", codiceSocieta="
				+ codiceSocieta + ", codiceUtente=" + codiceUtente
				+ ", dataInvioNotifica=" + dataInvioNotifica
				+ ", dataPagamento=" + dataPagamento
				+ ", dataRispostaNotifica=" + dataRispostaNotifica
				+ ", importoPagato=" + importoPagato + ", numeroAvviso="
				+ numeroAvviso + ", numeroDocumento=" + numeroDocumento
				+ ", numeroTentativoNotifica=" + numeroTentativoNotifica
				+ ", ultimoEsitoNotifica=" + ultimoEsitoNotifica
				+ ", urlPortale=" + urlPortale + ", xmlRicevuta=" + xmlRicevuta
				+ "]";
	}


}
