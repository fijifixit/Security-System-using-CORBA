import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


class HomeViewServant extends HomeViewPOA {
	private HomeView parent;
	private ORB orb;
	public ClientAndServer.HomeView homeView;
	public ClientAndServer.RegionalOffice server;
	public ClientAndServer.HomeHub homeHub;
	public ArrayList<ClientAndServer.HomeHub> hubConn = new ArrayList<ClientAndServer.HomeHub>();
	public ArrayList<String> detailList = new ArrayList<String>();
	public ArrayList<String> connected = new ArrayList<String>();

	public HomeViewServant(ClientAndServer.HomeHub homeHubNew, HomeView parentGUI, ORB orb_val, String args[]) {
		homeHub = homeHubNew;
		// store reference to parent GUI
		parent = parentGUI;
		orb = orb_val;

		try {

			// Initialize the ORB
			System.out.println("Initializing the ORB");
			ORB orb = ORB.init(args, null);

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				return;
			}

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}

			// resolve the Count object reference in the Naming service
			//regional office
			String name = "server";
			server = RegionalOfficeHelper.narrow(nameService.resolve_str(name));

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}


	}

	@Override
	//method called to show details to array
	public void details(String hubName, String address, String mobile) {
		detailList.add(hubName + ":" + address + ":" + mobile);

	}

	@Override
	//method called when an alarm has been raised and the user has registered for notification
	public void getMessage(String deviceID,String roomID) {
		//call method below
		parent.addAlarm(deviceID,roomID);
	}

	@Override
	//emthod called to return message from regional office
	public void returnMessage(String messageFromOffice) {
		//call method below
		parent.officeMessage(messageFromOffice);
	}

	@Override
	//methd called to register homeview for notification
	public void register(String name, String number) {
		// call method in homehub
		homeHub.register(name, number);

	}

	@Override
	//method called when homeview uregisters
	public void unRegister() {
		homeHub.Unregister();

	}
	
	//method called to return the logs from the homehub
	public String[] getLog() {
		String[] logArray =	homeHub.getLog();
		return logArray;
	}

}


public class HomeView extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private ClientAndServer.RegionalOffice office;
	private ClientAndServer.HomeView homeView;
	public ClientAndServer.HomeHub homeHub;
	private static String homeViewName = "homeView";
	private JTextArea textArea;
	private String hubName,personName,mobileNum;
	private DefaultListModel camModel, homeHubModel,listModel;
	private JCheckBox chckbxNotification;
	private JButton btnGetLogs,btnUnreisterForNotification,btnNewButton;
	private JLabel lblCurrentEvents,lblIfregistered,lblRegistered;
	private JLabel lblCheckTheBox;
	public HomeView(String[] args){

		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}

		JFrame newFrame = new JFrame("HomeHubName");
		JPanel p = new JPanel(new GridLayout(0,1));
		JTextField hubTextField = new JTextField(20);
		JLabel homeHubNameLbl = new JLabel("HomeHub to connect to");
		p.add(homeHubNameLbl);
		p.add(hubTextField);
		
		int result = JOptionPane.showConfirmDialog(newFrame, p,"HomeView Details",JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION){

			hubName = hubTextField.getText();
		}

		try{

			String[] newArgs = {"-ORBInitialPort", "1050"}; 
			ORB orb = ORB.init(newArgs, null);

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				return;
			}

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}

			// resolve the Count object reference in the Naming service
			String name = hubName;
			homeHub = HomeHubHelper.narrow(nameService.resolve_str(name));

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			HomeViewServant homeViewRef = new HomeViewServant(homeHub, this,orb, newArgs);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(homeViewRef);
			homeView = HomeViewHelper.narrow(ref);


			// bind the Count object in the Naming service
			String name2 = homeViewName;
			NameComponent[] countName = nameService.to_name(name2);
			nameService.rebind(countName, homeView);

			homeHub.connectHomeView(homeViewName);


			// set up the GUI
			textArea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textArea);
			scrollpane.setBounds(10, 27, 375, 133);
			panel = new JPanel();
			panel.setBackground(new Color(255, 255, 51));
			panel.setForeground(new Color(255, 99, 71));
			panel.setLayout(null);

			panel.add(scrollpane);
			getContentPane().add(panel, "Center");

			chckbxNotification = new JCheckBox("Notification");
			chckbxNotification.setBounds(10, 172, 97, 23);
			panel.add(chckbxNotification);

			btnGetLogs = new JButton("Get Logs");
			btnGetLogs.setBounds(37, 215, 333, 23);
			btnGetLogs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String hList = hubName;
					String[] logArray = homeViewRef.getLog();
					
					HistoryLog hist = new HistoryLog(logArray, hList);
					hist.setVisible(true);
				}
			});

			panel.add(btnGetLogs);

			lblCurrentEvents = new JLabel("Current Events");
			lblCurrentEvents.setBounds(159, -141, 112, 14);
			panel.add(lblCurrentEvents);

			btnNewButton = new JButton("Register for Notification");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					JFrame newFrame = new JFrame("Notification Register");
					JPanel p = new JPanel(new GridLayout(0,1));

					JTextField personNameTextfield = new JTextField(20);
					JLabel personNameLbl = new JLabel("Person Name");
					p.add(personNameLbl);
					p.add(personNameTextfield);

					JTextField mobileNumberTextfield = new JTextField(20);
					JLabel mobileNumberLbl = new JLabel("Mobile Number");
					p.add(mobileNumberLbl);
					p.add(mobileNumberTextfield);


					int result = JOptionPane.showConfirmDialog(newFrame, p,"Notification Register",JOptionPane.OK_CANCEL_OPTION);
					if(result == JOptionPane.OK_OPTION){

						personName = personNameTextfield.getText();
						mobileNum = mobileNumberTextfield.getText();
						lblIfregistered.setText("You have registered for Notifications");
						lblIfregistered.setBackground(Color.GREEN);
						lblIfregistered.setOpaque(true);
					}

					homeViewRef.register(personName, mobileNum);

				}
			});
			btnNewButton.setBounds(37, 249, 333, 23);
			panel.add(btnNewButton);

			btnUnreisterForNotification = new JButton("Unreister for Notification");
			btnUnreisterForNotification.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					homeViewRef.unRegister();
					lblIfregistered.setText("You are no longer registered for notifications");
					lblIfregistered.setBackground(Color.CYAN);
					lblIfregistered.setOpaque(true);

				}
			});
			btnUnreisterForNotification.setBounds(37, 284, 333, 23);
			panel.add(btnUnreisterForNotification);

			lblRegistered = new JLabel("Registered");
			lblRegistered.setBounds(37, 331, 97, 14);
			panel.add(lblRegistered);

			lblIfregistered = new JLabel("");
			lblIfregistered.setBounds(115, 331, 255, 14);
			panel.add(lblIfregistered);

			homeHubModel = new DefaultListModel();

			camModel = new DefaultListModel();

			listModel = new DefaultListModel();



			setSize(411, 397);
			setTitle("HomeView" + " " + "connected to" + " " + hubName);
			lblIfregistered.setText("You are NOT registered for notifications");
			
			lblCheckTheBox = new JLabel("Check the box to recieve a notifcation popup");
			lblCheckTheBox.setBounds(128, 176, 257, 14);
			panel.add(lblCheckTheBox);
			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

	
	
	@SuppressWarnings("unchecked")
	//method called to add camera to list
	public void addCamera(String deviceID){
		camModel.addElement(deviceID);
	}

	@SuppressWarnings("unchecked")
	//method called to add homehub to list
	public void addHomeHub(String deviceID){
		homeHubModel.addElement(deviceID);
		System.out.println("homeHubModel in server");
	}
	
	//method called to show details
	public void showDetails(String hubName,String address, String mobile){

		listModel.addElement("Homehub:"+ " " +hubName+" " + "\n"+
				"Address:" + " " + address + " " + "\n"
				+ "Mobile:" + " " + mobile);
	}
	
	//methd called when an alarm is raised
	public void addAlarm(String deviceID,String roomID){
		//if the notification box is selected
		if (chckbxNotification.isSelected()){
			//jframe with messae
			JFrame frame = new JFrame("Notification");
			JOptionPane.showMessageDialog(frame, "THERE IS A CONFIRMED ALARM BY" + deviceID + " " + "in" + "" + roomID, "Inane warning", JOptionPane.WARNING_MESSAGE);
		}
		//append to textarea
		textArea.append("Just to let you know.." +" " + deviceID + " " + " has a confirmed alarm." + " " + "in " + roomID + "\n");
	}

	//append return from office to textarea
	public void officeMessage(String messageFromOffice){
		textArea.append(messageFromOffice);
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new  HomeView(arguments).setVisible(true);
			}
		});
	}   
}


