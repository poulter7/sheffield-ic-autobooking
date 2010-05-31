/*
 * Session.java
 * 
 * Copyright (c) 2010 Jonathan Poulter and Ian Salmons
 * 
 * Date: 			31 May 2010
 * Last edited: 	
 * Written by:		Jonathan Poulter
 * 
 * Simulates one logged on session for a given user on the system
 * Can perform all of the tasks that user would want to do within it,
 * logon, book, logout.
 * Could be easily extended to do more, find booking, edit bookings, delete bookings
 */

package com.autobooking.data;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;



public class Session {
	private static final String PAGE = "/MyPC3/Front.aspx";
	private static final String URL = "mypc.shef.ac.uk";
	private static final String PROTOCOL = "https";
	private static final String LOGIN_URL = "https://mypc.shef.ac.uk/MyPC3/Front.aspx";
	private HttpContext localContext;
	private CookieStore cookieStore;
	private HttpClient client;
	private List<NameValuePair> params = null;
	
	protected Session(){
		client = new DefaultHttpClient();
		localContext = new BasicHttpContext();
		// make the cookies store
		cookieStore = new BasicCookieStore();
		// attach the cookie store
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	/**
	 * Log into the system
	 * 
	 * @param username
	 * @param password
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	protected void login(String username, String password) throws ClientProtocolException, IOException{
		// Setup NVPs for login
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("loginButton", "Login"));
		params.add(new BasicNameValuePair("page", "validateLogin"));
		
		// Put the parameters into a form
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		
		// Setup the post
		HttpPost httppost = new HttpPost(Session.LOGIN_URL);
		httppost.setEntity(entity);
		
		// Execute the post
		HttpResponse response = client.execute(httppost, localContext);
		response.getEntity().consumeContent();
		
		// Check what was returned
		if(printCookies() == 2){
			System.out.println("\tSuccessful logon");
		}else{
			System.out.println("\tLogon failed");
		}
	}
	
	/**
	 * Make a booking of the passed job.
	 * 
	 * 	This method uses {@link #book(String, String, String, int)} to perform the booking
	 * @param j
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	protected void doJob(Job j) throws ClientProtocolException, URISyntaxException, IOException{
		book(j.startTime,j.endTime,j.date,j.room.getRoomID());
	}
	
	/**
	 * Make a booking with the current login
	 * 
	 * @param startTime
	 * @param finishTime
	 * @param date
	 * @param resource
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws ClientProtocolException 
	 */
	private void book(String startTime, String finishTime, String date, int resource) throws URISyntaxException, ClientProtocolException, IOException{
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("startTime", startTime));
		params.add(new BasicNameValuePair("endTime", finishTime));
		params.add(new BasicNameValuePair("resourceId", resource+""));
		params.add(new BasicNameValuePair("date", date));
		params.add(new BasicNameValuePair("page", "booking"));
		params.add(new BasicNameValuePair("command", "create"));
		params.add(new BasicNameValuePair("submitButton", "Save"));
		params.add(new BasicNameValuePair("submitted", "1"));
		URI uri = URIUtils.createURI(Session.PROTOCOL, Session.URL, -1, Session.PAGE,
				URLEncodedUtils.format(params, "UTF-8"), null);
		// TODO add some confirmation for the user
		System.out.println(uri.toString());
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request, localContext);
		response.getEntity().consumeContent();
	}
	
	/**
	 * Returns a Calendar object which represents the server time
	 * Should be accurate and unchanging as lag is introduced through javascript
	 * 
	 * @return current time
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Calendar queryTime() throws ClientProtocolException, IOException, ParseException {
		// Setup a client
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(Session.LOGIN_URL);
//		HttpGet request = new HttpGet("https://mypc.shef.ac.uk/MyPC3/Front.aspx?page=login");

		// Get the HTML of the login page
		HttpResponse response = client.execute(request);
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		response.getEntity().writeTo(b);
		
		// parse the page
		String serverTimeString = b.toString().split("var nd = new Date")[1]; 
		String[] elements= serverTimeString.substring(serverTimeString.indexOf('(')+1, serverTimeString.indexOf(')')).split(",");
		
		// convert the date string from the page into a calendar object
		DateFormat d = new SimpleDateFormat();
		Date serverTime = d.parse(elements[2]+"/"+elements[1]+"/"+elements[0]+" "+elements[3]+":"+elements[4]);
		Calendar returnDate = Calendar.getInstance();
		returnDate.setTime(serverTime);
		// TODO SimpleDateFormat doesn't parse seconds, is there a better way?
		returnDate.set(Calendar.SECOND, Integer.parseInt(elements[5]));
		return returnDate;
	}
	
	/**
	 * Clears the CookieStore, effectively logging out of the system
	 */
	protected void logout(){
		//TODO release any resources still attached to the session
		((CookieStore)localContext.getAttribute(ClientContext.COOKIE_STORE)).clear();
	}
	
	/**
	 * Shows all the session cookies stored so far
	 */
	protected int printCookies(){
		for(Cookie c: cookieStore.getCookies()){
			System.out.println("[" + c.getName() +", " + c.getValue() +"]");
		}
		return cookieStore.getCookies().size();
	}
}
