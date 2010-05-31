/*
 * User.java
 * 
 * Copyright (c) 2010 Jonathan Poulter and Ian Salmons
 * 
 * Date: 			31 May 2010
 * Last edited: 	
 * Written by:		Jonathan Poulter
 * 					Ian Salmons
 * 
 * Encapsulates a single user
 * 
 */
package com.autobooking.data;

import java.util.ArrayList;
import java.util.List;

public class User {

	String name;
	String password;
	Session personalSession = new Session();
	List<Job> taskList = new ArrayList<Job>();
	
	public User(String name, String password) {
		this.name = name;
		this.password = password;
	}
	
	protected void addTask(Job j) {
		this.taskList.add(j);
		System.out.println("User:" + name + " has new job: " + j.startTime + " - " + j.endTime +" on "+j.date);
	}
	
	
}
