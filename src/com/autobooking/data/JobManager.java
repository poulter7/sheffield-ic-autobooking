package com.autobooking.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	private Date startTime = null;
	private Date endTime = null;
	private Date now = null;

	/**
	 * Asks the JobManager to book this room out and state how many users you have to play with
	 * 
	 * @param job
	 * @param users
	 * @return true if the job is successfully scheduled, else false
	 */
	public int performTask(Job job, List<User> users){
		/*
		 * If you have a job to do (book a time)
		 * and so many users to do it, why not let the program sort it out for you, what to do
		 */
		extractTimes(job);
		
		// if the user has missed the start time, it is unbookable
		if(startTime.before(now)){
			return START_TIME_ALREADY_GONE;
		}
		// if the start time is after the end time, it is unbookable
		if(startTime.after(endTime)){
			return START_TIME_AFTER_END_TIME;
		}
		// devise the ordering of booking tasks
		Calendar startDate =  Calendar.getInstance();
		Calendar endDate =  Calendar.getInstance();
		
		startDate.setTime(startTime);
		endDate.setTime(endTime);
		
		long hoursBetween = hoursBetween(startDate, endDate);
		System.out.println("Job Length: " + hoursBetween);
		
		// Don't have the two users you need
		if( hoursBetween > 4 && users.size() < 2 ) {
			return NEED_MIN_OF_TWO_USERS;
		}
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
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			int startHour = c.get(Calendar.HOUR_OF_DAY);
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
		
		// assign user one task of booking last (possibly the only) booking
		
			
		
		// if the booking could be made now, go!
		// execute the booking tasks
		
		// else schedule the booking tasks
		// schedule a task to find the server's time, 2 minutes before the time at which they can be performed
		// shimmy the tasks around and login using each client
		
		return SUCCESSFUL_SCHEDULE;
	}
	
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
	 * Set all of the times the JobHandler needs to worry about, now, start time, end time.
	 * @param job
	 */
	private void extractTimes(Job job) {
		System.out.println("Extracting Times");
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
		now = Calendar.getInstance().getTime();
	}
}
