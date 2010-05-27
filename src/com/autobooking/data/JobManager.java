package com.autobooking.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;

/**
 * JobManager
 * 
 * From a single broadly defined job, will figure out what order best to do things in and setup
 * the necessary tasks to happen at the right time, or perform them immediately if possible
 * 
 * @author Jonathan Poulter
 */
public class JobManager {
	protected static final String loginPageURL = "https://mypc.shef.ac.uk/MyPC3/Front.aspx";
	
	public static int SUCCESSFUL_SCHEDULE = 1;
	public static int START_TIME_ALREADY_GONE = 2;
	public static int START_TIME_AFTER_END_TIME = 3;
	public static int NEED_MIN_OF_TWO_USERS = 4;
	final Calendar startDate =  Calendar.getInstance();
	final Calendar endDate =  Calendar.getInstance();

	private static final int ADVANCED_BOOKING_DAYS = 3;

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
		System.out.println("Job Length: " + hoursBetween);
		
		// Don't have the two users you need
		if( hoursBetween > 4 && users.size() < 2 ) {
			return NEED_MIN_OF_TWO_USERS;
		}
		// divide the job up into tasks for each user
		assignUserTasks(job, users, hoursBetween);
		
		// if you can do it all now, go for it!
		if(canStartNow(job)){
			System.out.println("Can book right now!");
			for(User u: users){
				u.personalSession.login(u.name, u.password);
			}
			for(User u: users){
				for(Job j: u.taskList){
					u.personalSession.doJob(j);
				}
			}
		}else{
			System.out.println("Scheduling logon procedure to five minutes before booking");
			Timer t = new Timer();
			TimerTask login = new TimerTask() {
				
				@Override
				public void run() {
					for(User u: users){
						try {
							u.personalSession.login(u.name, u.password);
							int serverTimeOffset= getServerTimeOffset();
							// schedule booking tasks
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}

				private int getServerTimeOffset() {
					// TODO Auto-generated method stub
					return 0;
				}
			};
			// make the date 5 minutes before the login time
			Calendar canLogInNow = (Calendar) startDate.clone();
			canLogInNow.add(Calendar.DAY_OF_WEEK, -ADVANCED_BOOKING_DAYS+1);
			setToMidnight(canLogInNow);
			// go five minutes before
			canLogInNow.add(Calendar.MINUTE, -5);
			// schedule everyone to be logged in
			System.out.println("Will log everyone in at: " + canLogInNow.getTime().toString());
			t.schedule(login, canLogInNow.getTime());
			
			
		}
		
		return SUCCESSFUL_SCHEDULE;
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
}
