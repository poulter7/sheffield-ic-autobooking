package com.autobooking.data;
import java.util.ArrayList;
import java.util.List;


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
		System.out.println(new Main().performTask(new Job(rooms.get(0), "12:00","24:00","29/05/2010"), users));
	}
}
