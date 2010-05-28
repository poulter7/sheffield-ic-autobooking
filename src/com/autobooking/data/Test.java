package com.autobooking.data;
import java.util.ArrayList;
import java.util.List;


public class Test {

	public List<User> users = new ArrayList<User>();
	public List<Room> rooms = new ArrayList<Room>();
	
	public static void main(String[] args) {
		try {
			new Test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Test() throws Exception {
		System.out.println(JobManager.getServerTimeOffset());
		//System.out.println(Session.queryTime().getTime().toString());
		//rooms.add(new Room(330, "Cilass 1"));
		users.add(new User("pha07jrp", ""));
		users.add(new User("pcb07bs", ""));
		rooms.add(new Room(330, "CILass1"));
		System.out.println("Testing");
		try {
			System.out.println(new JobManager().performTask(new Job(rooms.get(0), "12:00","23:59","31/05/2010"), users));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
