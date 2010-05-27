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
public class Main {
	protected static final String loginPageURL = "https://mypc.shef.ac.uk/MyPC3/Front.aspx";
	public static int SUCCESSFUL_SCHEDULE = 1;
	public static int START_TIME_ALREADY_GONE = 2;
	public static int START_TIME_AFTER_END_TIME = 3;
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
		
		// if the booking could be made now, go!
		// execute the booking tasks
		
		// else schedule the booking tasks
		// schedule a task to find the server's time, 2 minutes before the time at which they can be performed
		// shimmy the tasks around and login using each client
		
		return SUCCESSFUL_SCHEDULE;
	}

	/**
	 * Set all of the times the JobHandler needs to worry about, now, start time, end time.
	 * @param job
	 */
	private void extractTimes(Job job) {
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
