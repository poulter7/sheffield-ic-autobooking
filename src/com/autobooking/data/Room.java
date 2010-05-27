package com.autobooking.data;

public class Room {
	private int roomID;
	private String roomName;
	
	public int getRoomID() {
		return roomID;
	}
	
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}
	
	public String getRoomName() {
		return roomName;
	}
	
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	@Override
	public String toString() {
		return roomName;
	}

	public Room(int roomID, String roomName) {
		this.roomID = roomID;
		this.roomName = roomName;
	}
	
}
