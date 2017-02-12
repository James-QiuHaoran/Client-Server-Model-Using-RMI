import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

/**
 * User-side application (Clients) of JPoker 24-game.
 * Based on tutorial 3, without changing the name of classes and several methods.
 * 
 * @author HAORAN
 * @version 1.0
 * @since 2017.2.9
 *
 */
public class MessageBox implements Runnable {

	public static void main(String[] args) {
		//SwingUtilities.invokeLater(new MessageBox());
		SwingUtilities.invokeLater(new MessageBox(args[0]));
	}
	
	public MessageBox(String host) {
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			wordCounter = (WordCount) registry.lookup("WordCounter");
		} catch (Exception e) {
			System.err.println("Failed accessing RMI: " + e);
		}
	}
	
	private WordCount wordCounter;
	public MessageBox() {
		try {
			wordCounter = (WordCount) Naming.lookup("WordCounter"); // find a service from the RMI registry on the same machine
		} catch (Exception e) {
			System.err.println("Failed accessing RMI: " + e);
		}
	}
	
	private JFrame frame; // login page
	private JFrame frame1; // register page
	private JFrame frame2; // game page
	private MyPanel panel; // panel for game window (change in action_listener)
	private MyTable table; // the table of leader board
	private int wordCount = 0; // for name
	private int wordCount2 = 0; // for password
	private int wordCount3 = 0; // for register name
	private int wordCount4 = 0; // for register password
	private int wordCount5 = 0; // for confirm password
	private JLabel wordCountLabel; // show # of words in name text field
	private JLabel wordCountLabel2; // show # of words in password text field
	private JTextField name;
	private JPasswordField password;
	private JTextField rname;
	private JPasswordField rpassword;
	private JPasswordField cpassword;
	private int index = -1;
	private int preindex = -1;
	private String _username = "Default"; // store the user_name and password of the local player
	private String _password = "********";
	
	private class MyDocumentListener implements DocumentListener {
		private String name;
		public MyDocumentListener(String name) {
			this.name = name;
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			WordCountUpdater updater = new WordCountUpdater();
			updater.setSource(name);
			updater.execute();
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			WordCountUpdater updater = new WordCountUpdater();
			updater.setSource(name);
			updater.execute();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			WordCountUpdater updater = new WordCountUpdater();
			updater.setSource(name);
			updater.execute();
		}
	}
	
	private class MyActionListener implements ActionListener {
		private String source;
		public void setSource(String name) {
			source = name;
		}
		
		public MyActionListener (String name) {
			source = name;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (source == "login") {
				if (wordCount == 0)
					JOptionPane.showMessageDialog(null, "Login name cannot be empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
				else if (wordCount2 == 0)
					JOptionPane.showMessageDialog(null, "Password cannot be empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
				else {
					if (wordCounter != null) {
						try {
							int state = wordCounter.Login(name.getText(), new String(password.getPassword()));
							if (state == 1) {
								// login successfully
								if (index == -1) {
									// first time to open the application
									preindex = -1;
									index = 0;
								} else {
									preindex = index;
									index = 0;
								}
								_username = name.getText();
								_password = new String(password.getPassword());
								panel = new MyPanel();
								panel.repaint();
								if (preindex == 2) {
									frame2.remove(table);
									frame2.add(panel);
								}
								frame2.pack();
								frame2.repaint();
								frame.setVisible(false);
								frame1.setVisible(false);
								frame2.setVisible(true); // jump to game page
							} else if (state == 2)
								JOptionPane.showMessageDialog(null, "No such user! Please Register first!", "ERROR", JOptionPane.ERROR_MESSAGE);
							else if (state == 3)
								JOptionPane.showMessageDialog(null, "Wrong Password!", "ERROR", JOptionPane.ERROR_MESSAGE);
							else if (state == 4)
								JOptionPane.showMessageDialog(null, "User Already Login!", "ERROR", JOptionPane.ERROR_MESSAGE);
						} catch (RemoteException e) {
							System.err.println("Failed invoking RMI: ");
						}
					}
				}
			} else if (source == "register") {
				// jump to the register window
				frame.setVisible(false);
				frame2.setVisible(false);
				frame1.setVisible(true);
				
			} else if (source == "rregister") {
				// judge whether the register is successful
				if (wordCount3 == 0)
					JOptionPane.showMessageDialog(null, "User name cannot be empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
				else if (wordCount4 == 0)
					JOptionPane.showMessageDialog(null, "Password cannot be empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
				else if (wordCount5 == 0)
					JOptionPane.showMessageDialog(null, "Confirm-password cannot be empty", "ERROR", JOptionPane.ERROR_MESSAGE);
				else if (!Arrays.equals(rpassword.getPassword(), cpassword.getPassword()))
					JOptionPane.showMessageDialog(null, "Password not consistent!", "ERROR", JOptionPane.ERROR_MESSAGE);
				else if (wordCounter != null) {
					try {
						int state = wordCounter.Register(rname.getText(), new String(rpassword.getPassword()));
						if (state == 1) {
							// login successfully
							_username = rname.getText();
							_password = new String(rpassword.getPassword());
							if (index == -1) {
								// first time to open the application
								preindex = -1;
								index = 0;
								panel = new MyPanel();
								
							} else {
								preindex = index;
								index = 0;
								if (preindex == 2) {
									frame2.remove(table);
								}
							}
							frame2.add(panel);
							frame2.pack();
							panel.repaint();
							frame2.repaint();
							frame.setVisible(false);
							frame1.setVisible(false);
							frame2.setVisible(true); // jump to game page
						} else if (state == 5)
							JOptionPane.showMessageDialog(null, "Username already exists, choose another username!", "ERROR", JOptionPane.ERROR_MESSAGE);
					} catch (RemoteException e) {
						System.err.println("Failed invoking RMI: ");
					}
				}
				
			} else if (source == "cancel") {
				// cancel register
				frame1.setVisible(false);
				frame2.setVisible(false);
				frame.setVisible(true);
			} else if (source == "userProfile") {
				// show the user profile, i.e. draw UserProfilePanel
				preindex = index;
				index = 0;
				panel.repaint();
				if (table != null) {
					frame2.remove(table);
					frame2.add(panel);
				}
				frame2.pack();
				frame2.setVisible(true);
				frame2.repaint();
			} else if (source == "game") {
				// jump to the game page
				preindex = index;
				index = 1;
				panel.repaint();
				if (table != null) {
					frame2.remove(table);
					frame2.add(panel);
				}
				frame2.pack();
				frame2.setVisible(true);
				frame2.repaint();
			} else if (source == "leaderBoard") {
				// show the leader board
				preindex = index;
				index = 2;
				if (table == null && preindex != 2) {
					table = new MyTable();
				}
				frame2.remove(panel);
				frame2.add(table);
				frame2.pack();
				frame2.setVisible(true);
				frame2.repaint();
			} else if (source == "logout") {
				// logout
				if (wordCounter != null) {
					try {
						wordCounter.Logout(_username);
						frame2.setVisible(false);
						frame1.setVisible(false);
						frame.setVisible(true);
					} catch (RemoteException e) {
						System.err.println("Failed invoking RMI: ");
					}
				}
			}
		}
	}
	
	public void updateCount(String source) {
		// use the word counting service
		if (wordCounter != null) {
			try {
				if (source == "name")
					wordCount = wordCounter.count(name.getText());
				else if (source == "password")
					wordCount2 = wordCounter.count(new String(password.getPassword()));
				else if (source == "rname")
					wordCount3 = wordCounter.count(rname.getText());
				else if (source == "rpassword")
					wordCount4 = wordCounter.count(new String(rpassword.getPassword()));
				else if (source == "cpassword")
					wordCount5 = wordCounter.count(new String(cpassword.getPassword()));
			} catch (RemoteException e) {
				System.err.println("Failed invoking RMI: ");
			}
		}
	}
	
	class MyTable extends JPanel {
		final JTable table;

		public MyTable() {
			super(new GridLayout(1,0));
			// Random data is being used for now.
			String[] columnNames = {"Rank", "Player", "Games Won", "Games Played", "Average Time"};
			Object[][] data = {
					{new Integer(1), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(2), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(3), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(4), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(5), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(6), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(7), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(8), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(9), "James", new Integer(10), new Integer(20), "12.5s"},
					{new Integer(10), "James", new Integer(10), new Integer(20), "12.5s"}
			};
			
			table = new JTable(data, columnNames);
	        table.setPreferredScrollableViewportSize(new Dimension(150, 300));
	        table.setFillsViewportHeight(true);
	        table.setRowHeight(27);
	        //Create the scroll pane and add the table to it.
	        JScrollPane scrollPane = new JScrollPane(table);

	        //Add the scroll pane to this panel.
	        add(scrollPane);
		}
	}
	
	/**
	 * User defined JPanel to draw the user profile window
	 * State: 0 - user profile
	 * 		  1 - game
	 * 		  2 - leader board
	 * @author HAORAN
	 *
	 */
	class MyPanel extends JPanel {
		public MyPanel() {
			this.setPreferredSize(new Dimension(150, 340));
		}
		
		public void paintComponent(Graphics g) {
			if (index == 0) {
				Graphics2D g2 = (Graphics2D) g;
				int fontSize = 34;
		        Font f = new Font("Arial", Font.BOLD, fontSize);
		        g2.setFont(f);
		        g2.setStroke(new BasicStroke(2));
		        g2.drawString(_username, 30, 50);
		        
		        fontSize = 20;
				f = new Font("Arial", Font.PLAIN, fontSize);
				g2.setFont(f);
				g2.drawString("Number of wins: " + "10", 30, 90);
				g2.drawString("Number of games: " + "20", 30, 118);
				g2.drawString("Average time to win: " + "12.5s", 30, 146);
				
				fontSize = 29;
				f = new Font("Arial", Font.BOLD, fontSize);
				g2.setFont(f);
				g2.drawString("Rank: #" + "10", 30, 184);
				
				frame2.pack();
				frame2.setVisible(true);
			} else if (index == 1) {
				frame2.pack();
				frame2.setVisible(true);
				// ......
				// More codes to be implemented in Assignment 2
			} 
		}
	}
	
	public void run() {
		frame = new JFrame("Login");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame1 = new JFrame("Register");
		frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame2 = new JFrame("JPoker 24-Game");
		frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// GUI Design for Login window
		JPanel title = new JPanel();
		title.setPreferredSize(new Dimension(260,150));
		title.setBorder(BorderFactory.createTitledBorder("LOGIN"));
		
		JPanel login = new JPanel();
		login.setLayout(new BoxLayout(login, BoxLayout.Y_AXIS));
		JPanel field1 = new JPanel();
		field1.setLayout(new BorderLayout());
		JLabel title1 = new JLabel("Login Name");
		name = new JTextField(); // login name
		name.getDocument().addDocumentListener(new MyDocumentListener("name"));
		name.setPreferredSize(new Dimension(250, 25));
		field1.add(title1, BorderLayout.CENTER);
		field1.add(name, BorderLayout.PAGE_END);
		login.add(field1);
		
		JPanel field2 = new JPanel();
		field2.setLayout(new BorderLayout());
		JLabel title2 = new JLabel("Password");
		password = new JPasswordField(); // login password
		password.getDocument().addDocumentListener(new MyDocumentListener("password"));
		password.setPreferredSize(new Dimension(250, 25));
		field2.add(title2, BorderLayout.CENTER);
		field2.add(password, BorderLayout.PAGE_END);
		login.add(field2);
		
		JPanel buttons = new JPanel();
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new MyActionListener("login"));
		JButton registerButton = new JButton("Register");
		registerButton.addActionListener(new MyActionListener("register"));
		buttons.add(loginButton);
		buttons.add(registerButton);
		login.add(buttons);
		title.add(login, new Integer(0));
		frame.add(title, BorderLayout.CENTER);
		
		JPanel wordCountPane = new JPanel();
		wordCountPane.add(new JLabel("Word count, name:"));
		
		wordCount = 0;
		wordCount2 = 0;
		wordCountLabel = new JLabel("" + wordCount);
		wordCountPane.add(wordCountLabel);
		wordCountPane.add(new JLabel(" password:"));
		wordCountLabel2 = new JLabel("" + wordCount2);
		wordCountPane.add(wordCountLabel2);
		frame.add(wordCountPane, BorderLayout.PAGE_END);
		
		frame.pack();
		frame.setVisible(true);
		
		// GUI Design for Register window
		JPanel register = new JPanel();
		register.setBorder(BorderFactory.createTitledBorder("Register"));
		register.setPreferredSize(new Dimension(260, 170));
		
		JPanel rfield1 = new JPanel();
		rfield1.setLayout(new BorderLayout());
		JLabel rtitle1 = new JLabel("Login Name");
		rname = new JTextField(); // login name
		rname.getDocument().addDocumentListener(new MyDocumentListener("rname"));
		rname.setPreferredSize(new Dimension(250, 25));
		rfield1.add(rtitle1, BorderLayout.CENTER);
		rfield1.add(rname, BorderLayout.PAGE_END);
		register.add(rfield1);
		
		JPanel rfield2 = new JPanel();
		rfield2.setLayout(new BorderLayout());
		JLabel rtitle2 = new JLabel("Password");
		rpassword = new JPasswordField(); // password
		rpassword.getDocument().addDocumentListener(new MyDocumentListener("rpassword"));
		rpassword.setPreferredSize(new Dimension(250, 25));
		rfield2.add(rtitle2, BorderLayout.CENTER);
		rfield2.add(rpassword, BorderLayout.PAGE_END);
		register.add(rfield2);
		
		JPanel rfield3 = new JPanel();
		rfield3.setLayout(new BorderLayout());
		JLabel rtitle3 = new JLabel("Confirm Password");
		cpassword = new JPasswordField(); // confirm password
		cpassword.getDocument().addDocumentListener(new MyDocumentListener("cpassword"));
		cpassword.setPreferredSize(new Dimension(250, 25));
		rfield3.add(rtitle3, BorderLayout.CENTER);
		rfield3.add(cpassword, BorderLayout.PAGE_END);
		register.add(rfield3);
		
		JPanel buttonPanel = new JPanel();
		JButton registerB = new JButton("Register");
		registerB.addActionListener(new MyActionListener("rregister"));
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new MyActionListener("cancel"));
		buttonPanel.add(registerB);
		buttonPanel.add(cancelB);
		
		frame1.add(register, BorderLayout.CENTER);
		frame1.add(buttonPanel, BorderLayout.SOUTH);
		frame1.pack();
		//frame1.setVisible(true);
		
		// GUI Design for the Game Window
		JPanel menu =new JPanel(new FlowLayout());
		JButton profile = new JButton("User Profile");
		profile.setPreferredSize(new Dimension(149, 30));
		profile.addActionListener(new MyActionListener("userProfile"));
		menu.add(profile);
		JButton game = new JButton("Play Game");
		game.setPreferredSize(new Dimension(149, 30));
		game.addActionListener(new MyActionListener("game"));
		menu.add(game);
		JButton board = new JButton("Leader Board");
		board.setPreferredSize(new Dimension(149, 30));
		board.addActionListener(new MyActionListener("leaderBoard"));
		menu.add(board);
		JButton logout = new JButton("Logout");
		logout.setPreferredSize(new Dimension(149, 30));
		logout.addActionListener(new MyActionListener("logout"));
		menu.add(logout);
		frame2.add(menu, BorderLayout.NORTH);
		
		panel = new MyPanel();
		frame2.add(panel, BorderLayout.CENTER);
		
		frame2.pack();
		//frame2.setVisible(true);
	}
	

	/* Document Listener */
//	public void insertUpdate(DocumentEvent e) {
//		new WordCountUpdater().execute();
//	}
//	public void removeUpdate(DocumentEvent e) {
//		new WordCountUpdater().execute();
//	}
//	public void changedUpdate(DocumentEvent e) {
//		new WordCountUpdater().execute();
//	}

	/* Word count updater */
	private class WordCountUpdater extends SwingWorker<Void, Void> {
		private String source;
		
		protected void setSource(String name) {
			source = name;
		}
		
		protected Void doInBackground() {
			String _source = source;
			updateCount(_source);
			return null;
		}
		protected void done() {
			if (source == "name") {
				wordCountLabel.setText("" + wordCount);
				wordCountLabel.invalidate();
			} else if (source == "password") {
				wordCountLabel2.setText("" + wordCount2);
				wordCountLabel2.invalidate();
			}
		}
	}
}