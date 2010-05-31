/*
 * Job.java
 * 
 * Copyright (c) 2010 Jonathan Poulter and Ian Salmons
 * 
 * Date: 			31 May 2010
 * Last edited: 	
 * Written by:		Jonathan Poulter
 * 
 * Encapsulates a single booking task
 */

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
