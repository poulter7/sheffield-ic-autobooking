/*
 * JobManager.java
 * 
 * Copyright (c) 2010 Jonathan Poulter and Ian Salmons
 * 
 * Date: 			31 May 2010
 * Last edited: 	
 * Written by:		Jonathan Poulter
 * 
 * Manages the job of splitting up bookings
 * and scheduling them to be performed at the
 * correct time
 */

package com.autobooking.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;

import com.autobooking.ui.UI;

/**
 * JobManager
 * 
 * From a single broadly defined job, will figure out what order best to do things in and setup
 * the necessary tasks to happen at the right time, or perform them immediately if possible
 * 
 * @author Jonathan Poulter
 */
public class JobManager {
	
	public final static int SUCCESSFUL_SCHEDULE = 1;
	public final static int START_TIME_ALREADY_GONE = 2;
	public final static int START_TIME_AFTER_END_TIME = 3;
	public final static int NEED_MIN_OF_TWO_USERS = 4;
	public final static int NEED_MIN_OF_ONE_USER = 5;
	public static final int BOOKING_MADE = 0;
	
	private final Calendar startDate =  Calendar.getInstance();
	private final Calendar endDate =  Calendar.getInstance();
	private final UI ui;

	private static final int ADVANCED_BOOKING_DAYS = 2;



	public JobManager(UI console) {
		this.ui = console;
	}

	/**
	 * Asks the JobManager to book this room out and state how many users you have to play with
	 * 
	 * @param job
	 * @param users
	 * @return true if the job is successfully scheduled, else false
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws URISyntaxException 
	 */
	public int performTask(Job job, final List<User> users) throws ClientProtocolException, IOException, URISyntaxException{
		
		extractTimes(job);
		// if the user has missed the start time, it is unbookable
		if(startDate.before(Calendar.getInstance())){
			return START_TIME_ALREADY_GONE;
		}
		// if the start time is after the end time, it is unbookable
		if(startDate.after(endDate)){
			return START_TIME_AFTER_END_TIME;
		}
		long hoursBetween = hoursBetween(startDate, endDate);
		
		// Don't have the two users you need
		if( hoursBetween > 4 && users.size() < 2 ) {
			return NEED_MIN_OF_TWO_USERS;
		} else if(users.isEmpty()){
			return NEED_MIN_OF_ONE_USER;
		}
		// divide the job up into tasks for each user
		assignUserTasks(job, users, hoursBetween);
		
		// if you can do it all now, go for it!
		if(canStartNow(job)){
			performLoginAndBooking(users);
			return BOOKING_MADE;
		}else{
			scheduleLoginAndBooking(users);
			return SUCCESSFUL_SCHEDULE;
		}
		
	}
	
	/**
	 * Perform the logging in and booking all in one, straight away
	 * 
	 * @param users
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void performLoginAndBooking(List<User> users) throws ClientProtocolException, IOException, URISyntaxException {
		ui.printToUiConsole("Can book right now!"); 
		for(User u: users){
			u.personalSession.login(u.name, u.password);
		}
		for(User u: users){
			for(Job j: u.taskList){
				u.personalSession.doJob(j);
			}
		}
	}

	/**
	 * Schedule the login to take place at a later date.
	 * The login will queue the booking requests
	 * @param users
	 */
	private void scheduleLoginAndBooking(final List<User> users) {
		ui.printToUiConsole("Scheduling logon procedure to five minutes before booking");
		final Timer t = new Timer();
		
		// define the booking task
		final TimerTask book = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for(User u: users){
					for(Job j: u.taskList){
						try {
							u.personalSession.doJob(j);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		
		// define the login task
		final TimerTask login = new TimerTask() {
			
			@Override
			public void run() {
				ui.printToUiConsole("Executing logon");
				for(User u: users){
					try {
						u.personalSession.login(u.name, u.password);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// schedule booking tasks
				int serverTimeOffset= getServerTimeOffset();
				Calendar canBookNow = (Calendar) startDate.clone();
				canBookNow.add(Calendar.DAY_OF_WEEK, -ADVANCED_BOOKING_DAYS);
				setToMidnight(canBookNow);
				// will book one second after
				canBookNow.add(Calendar.SECOND, 1 - serverTimeOffset);
				System.out.println("Will make bookings at: " + canBookNow.getTime().toString() + " which is " + serverTimeOffset + " seconds behind the server's time and a second later for grace");
				t.schedule(book, canBookNow.getTime());
			}
		};
		// make the date 5 minutes before the login time
		Calendar canLogInNow = (Calendar) startDate.clone();
		canLogInNow.add(Calendar.DAY_OF_WEEK, -ADVANCED_BOOKING_DAYS);
		setToMidnight(canLogInNow);
		// go a few minutes before
		int loginPrior = 2;
		canLogInNow.add(Calendar.MINUTE, -loginPrior);
		// schedule everyone to be logged in
		ui.printToUiConsole("Will log everyone in at: " + canLogInNow.getTime().toString());
		t.schedule(login, canLogInNow.getTime());
		
	}

	/**
	 * Returns true if the currently requested job can be performed now
	 * 
	 * @param job
	 * @return
	 */
	private boolean canStartNow(Job job) {
		Calendar canBookOnCal = Calendar.getInstance();
		canBookOnCal.add(Calendar.DAY_OF_WEEK, ADVANCED_BOOKING_DAYS);
		return startDate.before(canBookOnCal);
	}
	
	/**
	 * Reset a Calendar date to midnight
	 * @param c
	 * @return
	 */
	private void setToMidnight(Calendar c){
		c.set(Calendar.AM_PM, 0);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND,0);
	}

	/**
	 * Divide up the major job into smaller more manageable jobs, and divide them up, giving them to the users
	 * @param job
	 * @param users
	 * @param hoursBetween
	 */
	private void assignUserTasks(Job job, List<User> users, long hoursBetween) {
		// if you only have one job, add it
		if(hoursBetween <= 4){
			users.get(0).addTask(new Job(job.room, job.startTime, job.endTime, job.date));
		}else{  
			int blocks = (int) Math.ceil(hoursBetween/4.0);
			System.out.println(blocks);
			
			/*
			 * figure who will book which rooms
			 * will only ever REALLY need to book using two users, anything more is over complicated
			 * the people are bothered solely about obtaining the room, not who books it
			 * if that was an issue they would book separately not use this system. For now at least
			 */
			// calculate start hour
			int startHour = startDate.get(Calendar.HOUR_OF_DAY);
			// do the full blocks if only one this won't get called
			int i;
			for(i = 0; i< blocks -1; i++){
				// add the chosen user a task
				int blockHour = startHour + i*4;
				users.get(i%2).addTask(new Job(job.room, blockHour+":00", (blockHour+4)+":00", job.date));
			}
			// this section will start at a four hour block and finish at some other time  
			users.get(i%2).addTask(new Job(job.room, (startHour + (i*4)) +":00", job.endTime, job.date));
		}
	}
	
	/**
	 * Calculates the number of hours between two Calendar instances
	 * 
	 * Relies on startDate is before endDate
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long hoursBetween(Calendar startDate, Calendar endDate) {  
		// TODO could be int this can be at maximum 24
		// TODO use millis between
		Calendar date = (Calendar) startDate.clone();  
		long hoursBetween = 0;  
		while (date.before(endDate)) {  
			date.add(Calendar.HOUR_OF_DAY, 1);  
			hoursBetween++;  
		}  
		return hoursBetween;  
	}  

	/**
	 * Set calendars that the JobHandler needs to worry about
	 * @param job
	 */
	private void extractTimes(Job job) {
		System.out.println("Extracting Times");
		Date startTime;
		Date endTime;
		try {
			// setup date formatter
			DateFormat d = new SimpleDateFormat();
			// parse start and end times
			startTime = d.parse(job.date + " " + job.startTime);
			endTime = d.parse(job.date + " " + job.endTime);
			// programming error if there is a failure here
			if(startTime == null){
				throw new RuntimeException("Reached the end of the startTime string and didn't find a date?!");
			}
			if(endTime == null){
				throw new RuntimeException("Reached the end of the endTime string and didn't find a date?!");
			}
			
		} catch (ParseException e) {
			throw new RuntimeException("Parsing an incorrectly formatted date" + e);
		}
		startDate.setTime(startTime);
		endDate.setTime(endTime);
	}
	
	/**
	 * get the number of seconds by which the server is ahead of the client
	 * @return
	 */
	public static int getServerTimeOffset() {
		int secondsServerIsAhead = 0;
		try {
			// server should take longer to return so get the first
			Calendar serverNow = Session.queryTime();
			Calendar clientNow = Calendar.getInstance();
			secondsServerIsAhead = (int) ((serverNow.getTimeInMillis() - clientNow.getTimeInMillis())/1000.0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return secondsServerIsAhead;
	}
	
}
