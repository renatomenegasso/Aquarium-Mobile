package com.aquariumMobile.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;
 
public class WebService{
 
    DefaultHttpClient httpClient;
    HttpContext localContext;
    private static final int TIMEOUT = 4000;
    
    public WebService(){
        HttpParams myParams = new BasicHttpParams();
 
        HttpConnectionParams.setConnectionTimeout(myParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(myParams, TIMEOUT);
        httpClient = new DefaultHttpClient(myParams);
        localContext = new BasicHttpContext();
    }
    
    
    //Use this method to do a HttpGet/WebGet on the web service
    public String webGet(String getUrl) {
    	String responseString = "";
    	
    	HttpGet httpGet = new HttpGet(getUrl);
        Log.i("WebGetURL: ",getUrl);

        try {
        	HttpEntity entity  = httpClient.execute(httpGet).getEntity();
            BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
            String inputLine = "";

            while ((inputLine = in.readLine()) != null){
            	responseString += inputLine;
            }
            in.close();
            
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        } finally{
        }
        
        Log.i("Response: ", responseString);
        
        return responseString;
    }
}