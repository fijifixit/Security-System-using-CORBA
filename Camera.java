import ClientAndServer.*;
import ClientAndServer.Image;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;



import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.*;
import java.awt.event.*;

class CameraClientServant extends CameraPOA {
	
	private Camera parent;
	private String deviceID,hubName,roomID;
	public ClientAndServer.HomeHub homeHub;

	public CameraClientServant(ClientAndServer.HomeHub homeHubNew, Camera parentGUI, String deviceIDNew, String roomIDNew, ORB orb_val, String args[]) {
		deviceID = deviceIDNew;
		roomID = roomIDNew;
		parent = parentGUI;
		homeHub = homeHubNew;
	}


	
	// Method for returning device name
	public String getName() {

		return deviceID;
	}
	
	// Method for returning room name
	public String getRoomName() {

		return roomID;
	}

	// Method is called by the Homehub to reset the alarm
	public void remoteReset() {
		parent.alarmReset();

	}

	@Override
	// Method used to get the Image of the camera
	// Gets the date and time and the label status
	public Image CurrentImage() {
		Image img = new Image();
		//get calender time
		Date date = Calendar.getInstance().getTime();
		//set simple date format
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		// set format for time
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmss");
		
		//parse date to an integer
		int dd = Integer.parseInt(sdf.format(date));
		// parse time to integer
		int time2 = Integer.parseInt(sdf2.format(date));

		img.date = dd;
		img.time = time2;
		img.status = parent.mess();
		
		
		String currentImg = ("Date:" + " "+ Integer.toString(img.date).replaceAll("..(?!$)","$0-") 
				+ " " + "Time:" + " " + Integer.toString(img.time).replaceAll("..(?!$)","$0-"));

		JFrame frame = new JFrame("Current Time");
		JOptionPane.showMessageDialog(frame,currentImg + " " + " " + "\n" + "Status:" + " " + img.status);
		return img;
	}


	@Override
	// Method returns the current status as a string
	public String currentStatus() {
		String status = parent.mess();
		return status;
	}


	@Override
	//This method is a boolean method called by the camera and sensor
	public boolean raiseAlarm(String deviceID, String hubName, String roomID) {
		// returns the call alarm as a boolean
		boolean isTrue = homeHub.call_alarm(deviceID, hubName, roomID);
		//return boolean
		return isTrue;
	}

	//Method is to add a new camera. 
	//Calls the newcam method in the homehub
	public void newCam(String devID){
		homeHub.newCam(devID);
	}
	
	//Method is called to add a camera 
	public void addCam(ClientAndServer.Camera newDevice){
		homeHub.addCam(newDevice);
	}
}

/**
 * @author Ibrahim
 *This is the Cameras class which extends JFrame.
 *Contains all the GUI interface
 */
public class Camera extends JFrame{

	private JPanel textpanel;
	public ClientAndServer.HomeHub homeHub;
	private ClientAndServer.Camera camera;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private String deviceID,roomID,hubName;
	private JButton raiseAlarmButton,btnSwitchOn,btnSwitchOff,btnImageStatus,btnResetAlarm;
	private JLabel statuslbl,lblRoom,roomNameLbl,lblAlarmStatus;



	public Camera(String[] args) {
		
		//set nimbus look and feel
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

		// Create new JFrame 
		//set layout 
		JFrame newFrame = new JFrame("Camera Name");
		JPanel p = new JPanel(new GridLayout(0,1));
		JTextField hubTextField = new JTextField(20);
		JLabel homeHubNameLbl = new JLabel("Home Hub Name");
		p.add(homeHubNameLbl);
		p.add(hubTextField);

		JTextField roomNameTextfield = new JTextField(20);
		JLabel rNameLbl = new JLabel("Room Name");
		p.add(rNameLbl);
		p.add(roomNameTextfield);

		JTextField camNameTextfield = new JTextField(20);
		JLabel camNameLbl = new JLabel("Camera Name");
		p.add(camNameLbl);
		p.add(camNameTextfield);

		//Set JOptionPane 
		int result = JOptionPane.showConfirmDialog(newFrame, p,"Camera Details",JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION){
			//set the hubName from the textfield
			hubName = hubTextField.getText();
			//set the roomID from the textfield
			roomID = roomNameTextfield.getText();
			//set the cam name from the textfield
			deviceID = camNameTextfield.getText(); 
		}else{
			//system exit if window is closed.
			System.exit(0);
		}
		

		try {
			
			
			//ORB
			String[] newArgs = {"-ORBInitialPort", "1050"}; 
			//initialise the ORB
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

			CameraClientServant cameraRef = new CameraClientServant(homeHub, this, deviceID,roomID, orb, newArgs);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(cameraRef);
			camera = CameraHelper.narrow(ref);

			
			// bind the Count object in the Naming service
			String name2 = cameraRef.getName();
			NameComponent[] countName = nameService.to_name(name2);
			nameService.rebind(countName, camera);
			
			//call method above
			cameraRef.newCam(cameraRef.getName());
			cameraRef.addCam(camera);

			// set up the GUI	

			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(10, 5, 364, 141);
			textpanel = new JPanel();
			textpanel.setBackground(Color.CYAN);
			textpanel.setLayout(null);
			textpanel.add(scrollpane);
			getContentPane().add(textpanel, "Center");

			btnSwitchOff = new JButton("Switch Off");
			btnSwitchOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					raiseAlarmButton.setEnabled(false);
					btnSwitchOff.setEnabled(false);
					textarea.append("Camera has been switched off" + "\n");
					homeHub.toggleOff(deviceID);
				}
			});
			btnSwitchOff.setBounds(35, 175, 148, 23);
			textpanel.add(btnSwitchOff);

			btnSwitchOn = new JButton("Switch On");
			btnSwitchOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					raiseAlarmButton.setEnabled(true);
					btnSwitchOff.setEnabled(true);
					textarea.append("Camera has been switched on" + "\n");
					homeHub.toggleOn(deviceID);
				}
			});
			btnSwitchOn.setBounds(35, 214, 148, 23);
			textpanel.add(btnSwitchOn);

			lblAlarmStatus = new JLabel("Alarm Status");
			lblAlarmStatus.setBounds(35, 256, 80, 14);
			textpanel.add(lblAlarmStatus);

			statuslbl = new JLabel("----");
			statuslbl.setBounds(125, 256, 80, 14);
			textpanel.add(statuslbl);

			btnResetAlarm = new JButton("Reset Alarm");
			btnResetAlarm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					statuslbl.setBackground(Color.GREEN);
					statuslbl.setOpaque(true);
					statuslbl.setText("ALARM OFF");
					raiseAlarmButton.setEnabled(true);
					homeHub.resetCamAlarm(deviceID);
					btnSwitchOff.setEnabled(true);
					btnSwitchOn.setEnabled(true);
				}
			});
			btnResetAlarm.setBounds(193, 175, 148, 23);
			textpanel.add(btnResetAlarm);

			btnImageStatus = new JButton("Image Status");
			btnImageStatus.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//call method above
					cameraRef.CurrentImage();			
				}
			});
			btnImageStatus.setBounds(193, 214, 148, 23);
			textpanel.add(btnImageStatus);

			lblRoom = new JLabel("Room");
			lblRoom.setBounds(235, 256, 46, 14);
			textpanel.add(lblRoom);

			roomNameLbl = new JLabel("");
			roomNameLbl.setBounds(295, 256, 46, 14);
			textpanel.add(roomNameLbl);

			setSize(400, 368);
			//set title
			setTitle("Camera Name =" + " " + deviceID + " " +"connected to" + " " +hubName);

			roomNameLbl.setText(roomID);
			btnSwitchOn.setEnabled(false);
			if(btnSwitchOff.isEnabled() == true){

				btnSwitchOn.setEnabled(true);
			}
			statuslbl.setBackground(Color.GREEN);
			statuslbl.setOpaque(true);
			statuslbl.setText("ALARM OFF");
			raiseAlarmButton = new JButton("Raise Alarm");
			raiseAlarmButton.setBounds(10, 300, 364, 23);
			textpanel.add(raiseAlarmButton);
			raiseAlarmButton.addActionListener (new ActionListener() {
				public void actionPerformed (ActionEvent evt) {
					textarea.append("Raising Alarm...\n");
					//boolean return
					boolean isTrue = cameraRef.raiseAlarm(deviceID, hubName,roomID);
					if(isTrue){
						statuslbl.setBackground(Color.RED);
						statuslbl.setOpaque(true);
						statuslbl.setText("ALARM ON");
						raiseAlarmButton.setEnabled(false);
						btnSwitchOff.setEnabled(false);
						btnSwitchOn.setEnabled(false);

					}
				}
			});

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowOpened (java.awt.event.WindowEvent evt) {


				}
			} );

			textarea.append("Camera has started.\n\n");
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}



	}

	
	// Method is used when the alarm has been reset
	//Changes label status
	public void alarmReset(){
		statuslbl.setBackground(Color.GREEN);
		statuslbl.setOpaque(true);
		statuslbl.setText("ALARM OFF");
		btnSwitchOff.setEnabled(true);
		btnSwitchOn.setEnabled(true);
		raiseAlarmButton.setEnabled(true);


	}
	
	//Method is used to return the label 
	public String mess(){

		return statuslbl.getText();
	}

	//Main method
	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Camera(arguments).setVisible(true);
			}
		});
	}
}


