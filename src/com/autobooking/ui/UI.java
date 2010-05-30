package com.autobooking.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.http.client.ClientProtocolException;
import org.jdesktop.swingx.JXDatePicker;

import com.autobooking.data.Job;
import com.autobooking.data.JobManager;
import com.autobooking.data.Room;
import com.autobooking.data.User;

public class UI extends JFrame {
	
	final JTextArea console= new JTextArea();
	private JButton bookButton;

	public static void main(String[] args) {
		new UI();
	}

	public UI() {
		Dimension size = new Dimension(298, 137);
		
		this.setMinimumSize(size);
		
		this.setTitle("IC auto booking system");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().add(setupPanel());
		this.pack();
		this.setVisible(true);
		//System.out.println(this.getSize());
	}

	private String[] timeList() {
		String[] list = new String[25];
		for (int i = 0; i < 24; i++) {
			list[i] = i + ":00";
		}
		list[24] = "23:59";

		return list;
	}

	private JPanel setupPanel() {
		JPanel panel = new JPanel();
		
		double border = 6;
		
		double size[][] =
	        {{border, 0.5, border, 0.5, border},
	         {border, 25, border, 25, border, 25, border, 25, border, 25, border, 25, border, 25, border, 25, border}};

		panel.setLayout (new TableLayout(size));


		String[] timeList = timeList();

		String[] roomNames = { "231", "330", "331" };
		final JComboBox roomList = new JComboBox(roomNames);

		final JXDatePicker dateField = new JXDatePicker(Calendar.getInstance().getTime());
		

		final JComboBox startTime = new JComboBox(timeList);

		final JComboBox endTime = new JComboBox(timeList);	
		//endTime.setSelectedIndex(startTime.getSelectedIndex() + 4);

		bookButton = new JButton("Book Now!");
		final JTextField username1 = new JTextField("username");
		final JPasswordField password1 = new JPasswordField();
		final JTextField username2 = new JTextField("username");
		final JPasswordField password2 = new JPasswordField();
		JScrollPane scrollPane = new JScrollPane(console);
		console.setEditable(false);
				
		panel.add(roomList, "1,1");
		panel.add(dateField, "3,1");
		panel.add(startTime, "1,3");
		panel.add(endTime, "3,3");
		panel.add(bookButton, "1,5,3,5");
		panel.add(username1, "1,7");
		panel.add(password1, "3,7");
		panel.add(username2, "1,9");
		panel.add(password2, "3,9");
		panel.add(scrollPane, "1,11,3,15");
		FocusListener clearField = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				JTextField f = (JTextField) e.getComponent();
				if(f.getText().equals("")){
					f.setText("username");
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				JTextField f = (JTextField) e.getComponent();
				if(f.getText().equals("username")){
					f.setText("");
				}
				
			}
		};
		username1.addFocusListener(clearField);
		username2.addFocusListener(clearField);
		
		bookButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UI.this.console.setText("");
				UI.this.bookButton.setEnabled(false);
				UI.this.bookButton.setText("Working...");
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// init lists for further development
						List<User> users = new ArrayList<User>();
						
						// make the room
						Room room = new Room(Integer.parseInt(roomList.getSelectedItem().toString()),"");				

						// adding users
						String username1String = username1.getText();
						String username2String = username2.getText();
						String password1String = String.copyValueOf(password1.getPassword());
						String password2String = String.copyValueOf(password2.getPassword());
						if(!username1String.isEmpty() && !password1String.isEmpty()){
							printToUiConsole("User " +username1String + " accepted");
							users.add(new User(username1.getText(), String.copyValueOf(password1.getPassword())));	
						}
						if(!username2String.isEmpty() && !password2String.isEmpty()){
							printToUiConsole("User " +username2String + " accepted");
							users.add(new User(username2.getText(), String.copyValueOf(password2.getPassword())));
						}
						// make a Calendar Object of the UI calendar selected date 
						Calendar c = Calendar.getInstance();
						c.setTime(dateField.getDate());
						 
						
						SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
						Job job = new Job(room,
										  startTime.getSelectedItem().toString(), 
										  endTime.getSelectedItem().toString(),
										  f.format(c.getTime()));
						
						try {
							System.out.println("Sending job");
							int i = new JobManager(UI.this).performTask(job, users);
							switch (i) {
							case JobManager.NEED_MIN_OF_TWO_USERS:
								printToUiConsole("ERROR: You need at least two users to perform this booking");
								break;
							
							case JobManager.START_TIME_AFTER_END_TIME:
								printToUiConsole("ERROR: The start time you entered is incorrect");
								break;
								
							case JobManager.START_TIME_ALREADY_GONE:
								printToUiConsole("ERROR: You can't book for a time in the past");
								break;
								
							case JobManager.NEED_MIN_OF_ONE_USER:
								printToUiConsole("ERROR: You need at least one user for this job");
								break;
								
							case JobManager.SUCCESSFUL_SCHEDULE:
								printToUiConsole("SUCCESS: Job accepted and scheduled");
								break;
							case JobManager.BOOKING_MADE:
								printToUiConsole("SUCCESS: Job executed!");
								break;
							default:
								printToUiConsole("ERROR: Unknown Error");
								break;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Runnable r = new Runnable() {
							
							@Override
							public void run() {
								bookButton.setText("Book Now!");
								bookButton.setEnabled(true);
								
							}
						};
						SwingUtilities.invokeLater(r);
					}
				});
				t.start();
				
			}
		});
		
		// return the freshly created panel
		return panel;
	}
	
	public void printToUiConsole(String s){
		System.out.println(s);
		console.append(s+"\n");
	}
}
