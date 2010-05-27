package com.autobooking.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

public class UI extends JFrame {

	public static void main(String[] args) {
		new UI();
	}

	public UI() {
		Dimension size = new Dimension(300, 150);
		
		this.setSize(size);
		this.setMinimumSize(size);
		
		this.setTitle("IC auto booking system");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().add(setupPanel());
		this.pack();
		this.setVisible(true);
	}

	private String[] timeList() {
		String[] list = new String[25];
		for (int i = 0; i < 25; i++) {
			list[i] = i + ":00";
		}

		return list;
	}

	private JPanel setupPanel() {
		JPanel panel = new JPanel();
		
		double border = 10;
		double gap = border/2;
		
		double size[][] =
	        {{border, 0.5, gap, 0.5, border},
	         {border, TableLayout.FILL, gap, TableLayout.FILL,gap,  TableLayout.FILL, border}};

		panel.setLayout (new TableLayout(size));


		String[] timeList = timeList();

		String[] roomNames = { "Room 1", "Room 2" };
		JComboBox roomList = new JComboBox(roomNames);

		JXDatePicker dateField = new JXDatePicker();
		

		JComboBox startTime = new JComboBox(timeList);

		JComboBox endTime = new JComboBox(timeList);
		endTime.setSelectedIndex(startTime.getSelectedIndex() + 4);

		JButton bookButton = new JButton("Book Now!");
		
		panel.add(roomList, "1,1,f,c");
		panel.add(dateField, "3,1,f,c");
		panel.add(startTime, "1,3,f,c");
		panel.add(endTime, "3,3,f,c");
		panel.add(bookButton, "1,5,3,5");
		
		// return the freshly created panel
		return panel;
	}
}
