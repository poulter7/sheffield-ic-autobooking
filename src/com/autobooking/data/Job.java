package com.autobooking.data;

public class Job {

	Room room;
	String startTime;
	String endTime;
	String date;
	public Job(Room room, String startTime, String endTime,
			String date) {
		this.room = room;
		this.startTime = startTime;
		this.endTime = endTime;
		this.date = date;
	}
	
}
