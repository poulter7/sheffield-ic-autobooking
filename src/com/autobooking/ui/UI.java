package com.autobooking.ui;

import java.awt.GridBagLayout;

import javax.swing.GroupLayout;
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
		JPanel panel = new JPanel(new GridBagLayout());
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		String[] timeList = timeList();

		String[] roomNames = { "Room 1", "Room 2" };
		JComboBox roomList = new JComboBox(roomNames);

		JXDatePicker dateField = new JXDatePicker();
		

		JComboBox startTime = new JComboBox(timeList);

		JComboBox endTime = new JComboBox(timeList);
		endTime.setSelectedIndex(startTime.getSelectedIndex() + 4);

		JButton bookButton = new JButton("Book Now!");

		
		//layout.linkSize(SwingConstants.HORIZONTAL, roomList, dateField, startTime, endTime);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(roomList)
					.addComponent(startTime)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(dateField)
					.addComponent(endTime)
				)
		);

		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup( layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(roomList)
					.addComponent(dateField)
				)
				.addGroup( layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(startTime)
					.addComponent(endTime)
				)
			
		);


		// return the freshly created panel
		return panel;
	}
}
