package com.autobooking.data;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;


public class Test {

	public List<User> users = new ArrayList<User>();
	public List<Room> rooms = new ArrayList<Room>();
	
	public static void main(String[] args) {
		new Test();
	}
	public Test() {
		rooms.add(new Room(330, "Cilass 1"));
		users.add(new User("aca08is", ""));
		users.add(new User("jon", ""));
		//rooms.add(new Room("235"));
		System.out.println("Testing");
		try {
			System.out.println(new JobManager().performTask(new Job(rooms.get(0), "23:00","23:59","28/05/2010"), users));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
