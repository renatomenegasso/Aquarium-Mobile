package com.aquarariumMobile.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.aquariumMobile.web.WebService;

public class ArduinoServices {
	
	public enum DeviceStatus {
		ON,OFF,UNKNOW
	}
	
	private static final String ON_RESULT = "1";
	private static final String POSITIVE_RESULT = "ok";

	private WebService service;
	private Properties properties;
	private String serviceUrl;
	private boolean serviceOnline;
	
	public ArduinoServices(){
		initialize(new WebService());
	}
	
	public ArduinoServices(WebService service){
		initialize(service);
	}
	
	public void setServiceUrl(String url){
		serviceUrl = url;
		properties.setProperty("domain", url);
	}
	
	private void initialize(WebService service){
		properties = new Properties();
		
		try{
			InputStream stream = getClass().getResourceAsStream("/service.properties");
			properties.load(stream);
		} catch(IOException e){
			throw new RuntimeException("Erro ao carregar o arquivo service.properties");
		}
		
		this.serviceUrl = properties.getProperty("domain");
		this.service = service;
	}
	
	private String requestService(String operation){
		serviceOnline = true;
		String result = "";
		try{
			result = service.webGet(serviceUrl + operation);
		}catch(Exception ex){
			ex.printStackTrace();
			result = null;
			serviceOnline = false;
		}
		
		return result;
		
	}

	
	public boolean ascenderLuz(){
		String result = requestService(properties.getProperty("light.on"));
		return (POSITIVE_RESULT.equals(result));
	}
	
	public boolean apagarLuz() {
		String result = requestService(properties.getProperty("light.off"));
		return (POSITIVE_RESULT.equals(result));
	}

	public DeviceStatus statusLuz() {
		String result = requestService(properties.getProperty("light.status"));
		return (ON_RESULT.equals(result)) ? DeviceStatus.ON : DeviceStatus.OFF; 
	}

	public boolean ligarBomba() {
		String result = requestService(properties.getProperty("pump.on"));
		return (POSITIVE_RESULT.equals(result));
	}
	
	public boolean desligarBomba() {
		String result = requestService(properties.getProperty("pump.off"));
		return (POSITIVE_RESULT.equals(result));
	}
	
	public DeviceStatus statusBomba() {
		String result = requestService(properties.getProperty("pump.status"));
		return (ON_RESULT.equals(result)) ? DeviceStatus.ON : DeviceStatus.OFF;
	}

	public float recuperarTemperatura() {
		String temperature = requestService(properties.getProperty("temperature"));
		float result = -1;
		try {  
			result = Float.parseFloat(temperature);  
		} catch( Exception e ) {  
		}
		
		return result;
	}

	public DeviceStatus statusAlimentador() {
		String result = requestService(properties.getProperty("alimentator.status"));
		return (ON_RESULT.equals(result)) ? DeviceStatus.ON : DeviceStatus.OFF;
	}

	public boolean dispararAlimentador() {
		String result = requestService(properties.getProperty("alimentator.trigger"));
		return (POSITIVE_RESULT.equals(result));
	}

	public boolean isServiceOnline() {
		return serviceOnline;
	}

	public boolean ligarAquecedor() {
		String result = requestService(properties.getProperty("aquecedor.on"));
		return (POSITIVE_RESULT.equals(result));
	}
	
	public boolean desligarAquecedor() {
		String result = requestService(properties.getProperty("aquecedor.off"));
		return (POSITIVE_RESULT.equals(result));
	}
	
	public DeviceStatus statusAquecedor() {
		String result = requestService(properties.getProperty("aquecedor.status"));
		return (ON_RESULT.equals(result)) ? DeviceStatus.ON : DeviceStatus.OFF;
	}
}
