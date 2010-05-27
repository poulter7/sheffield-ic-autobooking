package com.autobooking.data;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

/**
 * Session
 * 
 * Simulates one logged on session for a given user on the system
 * Can perform all of the tasks that user would want to do within it,
 * logon, book, logout.
 * Could be easily extended to do more, find booking, edit bookings, delete bookings
 * 
 * @author Jonathan Poulter
 *
 */

//TODO add feedback to the user during booking and logging in
public class Session {
	
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
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("loginButton", "Login"));
		params.add(new BasicNameValuePair("page", "validateLogin"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		// setup the post
		HttpPost httppost = new HttpPost(JobManager.loginPageURL);
		httppost.setEntity(entity);
		// execute the post
		HttpResponse response = client.execute(httppost, localContext);
		response.getEntity().consumeContent();
	}
	
	/**
	 * Make a booking with the current login
	 * 
	 * @param startTime
	 * @param finishTime
	 * @param date
	 * @param resource
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	protected void book(String startTime, String finishTime, String date, String resource) throws URISyntaxException, ClientProtocolException, IOException{
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("startTime", startTime));
		params.add(new BasicNameValuePair("endTime", finishTime));
		params.add(new BasicNameValuePair("resourceId", resource));
		params.add(new BasicNameValuePair("date", date));
		params.add(new BasicNameValuePair("page", "booking"));
		params.add(new BasicNameValuePair("command", "create"));
		params.add(new BasicNameValuePair("submitButton", "Save"));
		params.add(new BasicNameValuePair("submitted", "1"));
		// TODO use URLEncodedUtils.format concatenated to loginPageURL to produce get
		URI uri = URIUtils.createURI("https", "mypc.shef.ac.uk", -1, "/MyPC3/Front.aspx",
				URLEncodedUtils.format(params, "UTF-8"), null);
		// TODO add some confirmation for the user
		System.out.println(uri.toString());
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request, localContext);
		response.getEntity().consumeContent();
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
	protected void printCookies(){
		System.out.println("Printing Cookie store");
		for(Cookie c: cookieStore.getCookies()){
			System.out.println("[" + c.getName() +", " + c.getValue() +"]");
		}
	}
}
