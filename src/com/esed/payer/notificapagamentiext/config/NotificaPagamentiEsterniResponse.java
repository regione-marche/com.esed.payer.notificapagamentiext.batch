/**
 * 
 */
package com.esed.payer.notificapagamentiext.config;

/**
 * @author ggiacani
 *
 */
public class NotificaPagamentiEsterniResponse {
	
	private String code;
	private String message;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public NotificaPagamentiEsterniResponse() {}
	public NotificaPagamentiEsterniResponse(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	
	public String toString() {
		return "EstrazionePagamentiInAcquisizioneResponse [code="+code+
		" ,message="+message+"]";
	}

}
