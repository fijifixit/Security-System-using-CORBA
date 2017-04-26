import ClientAndServer.*;


import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.*;
import java.awt.event.*;


class SensorServant extends SensorPOA {
	private Sensor parent;
	private String deviceID,roomID,hubName;
	public ClientAndServer.HomeHub homeHub;


	public SensorServant(ClientAndServer.HomeHub homeHubNew,Sensor parentGUI, String deviceIDNew, String roomIDNew, ORB orb_val, String args[]) {
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

	@Override
	// Method is called by the Homehub to reset the alarm
	public void remoteReset() {
		parent.alarmReset();
	}

	@Override
	//Method to return status of the sensor
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

	@Override
	//Method is to add a new sensor. 
	//Calls the newSensor method in the homehub
	public void newSensor(String deviceID) {
		homeHub.newSensor(deviceID);


	}

	@Override
	//Method is called to add a sensor 
	public void addSensor(ClientAndServer.Sensor newSensor) {
		homeHub.addSens(newSensor);
	}
	
	
}

/**
 * @author Ibrahim
 *This is the Sensor class which extends JFrame.
 *Contains all the GUI interface
 */
public class Sensor extends JFrame{
	private JPanel textpanel;
	public ClientAndServer.HomeHub homeHub;
	private ClientAndServer.Sensor sensor;
	private JScrollPane scrollpane;
	private JTextArea textarea;
	private String deviceID,roomID,hubName, myName;
	private JLabel statuslbl,lblRoom,roomLbl,lblAlarmStatus;
	private JButton raiseAlarmButton;
	private JButton btnResetSensor;

	public Sensor(String[] args) {

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
		// set layout 
		JFrame newFrame = new JFrame("Sensor Details");
		JPanel p = new JPanel(new GridLayout(0,1));
		JTextField hubTextField = new JTextField(20);
		JLabel homeHubNameLbl = new JLabel("Home Hub Name");
		p.add(homeHubNameLbl);
		p.add(hubTextField);

		JTextField roomNameTextfield = new JTextField(20);
		JLabel rNameLbl = new JLabel("Room Name");
		p.add(rNameLbl);
		p.add(roomNameTextfield);

		JTextField sensorNameTextfield = new JTextField(20);
		JLabel sensorNameLbl = new JLabel("Sensor Name");
		p.add(sensorNameLbl);
		p.add(sensorNameTextfield);

		
		//Set JOptionPane 
		int result = JOptionPane.showConfirmDialog(newFrame, p,"Sensor Details",JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION){
			// set hubaname from textfield
			hubName = hubTextField.getText();
			//set roomname from textfield
			roomID = roomNameTextfield.getText();
			//set deviceID from the textfield
			deviceID = sensorNameTextfield.getText(); 
		}else{
			//system exit
			System.exit(0);
		}


		try {

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

			SensorServant sensorRef = new SensorServant(homeHub,this, deviceID,roomID, orb, newArgs);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(sensorRef);
			sensor = SensorHelper.narrow(ref);


			// bind the Count object in the Naming service
			String name2 = sensorRef.getName() ;
			NameComponent[] countName = nameService.to_name(name2);
			nameService.rebind(countName, sensor);




			sensorRef.newSensor(sensorRef.getName());
			sensorRef.addSensor(sensor);


			// set up the GUI	

			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(10, 11, 364, 199);
			textpanel = new JPanel();
			textpanel.setBackground(new Color(147, 112, 219));
			textpanel.setLayout(null);


			textpanel.add(scrollpane);

			getContentPane().add(textpanel, "Center");

			lblAlarmStatus = new JLabel("Alarm Status");
			lblAlarmStatus.setBounds(10, 228, 89, 14);
			textpanel.add(lblAlarmStatus);

			statuslbl = new JLabel("----");
			statuslbl.setBounds(112, 228, 68, 14);
			textpanel.add(statuslbl);

			raiseAlarmButton = new JButton("Panic");
			raiseAlarmButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					textarea.append("Raising Alarm...\n");
					//return  boolean result from the raiseAlarm method
					boolean isTrue = sensorRef.raiseAlarm(deviceID, hubName,roomID);	
					//if boolean is true
					if(isTrue){

						statuslbl.setBackground(Color.RED);
						statuslbl.setOpaque(true);
						statuslbl.setText("ALARM ON");
						raiseAlarmButton.setEnabled(false);
					}
				}
			});
			raiseAlarmButton.setBounds(10, 263, 181, 33);
			textpanel.add(raiseAlarmButton);

			lblRoom = new JLabel("Room");
			lblRoom.setBounds(190, 228, 46, 14);
			textpanel.add(lblRoom);

			roomLbl = new JLabel("");
			roomLbl.setBounds(246, 228, 46, 14);
			textpanel.add(roomLbl);

			setSize(399, 341);
			//set title
			setTitle("Sensor Name =" + " " + deviceID + " " +"connected to" + " " +hubName);

			roomLbl.setText(roomID);
			statuslbl.setBackground(Color.GREEN);
			statuslbl.setOpaque(true);
			statuslbl.setText("ALARM OFF");

			btnResetSensor = new JButton("Reset Sensor");
			btnResetSensor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					statuslbl.setBackground(Color.GREEN);
					statuslbl.setOpaque(true);
					statuslbl.setText("ALARM OFF");
					raiseAlarmButton.setEnabled(true);
					homeHub.resetSensorAlarm(deviceID);
				}
			});
			btnResetSensor.setBounds(200, 263, 173, 33);
			textpanel.add(btnResetSensor);

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

			textarea.append("Sensor has started.\n\n");
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

	//Method called onchas been resete the alarm 
	public void alarmReset(){
		statuslbl.setBackground(Color.GREEN);
		statuslbl.setOpaque(true);
		statuslbl.setText("ALARM OFF");
		raiseAlarmButton.setEnabled(true);
	}

	//Method for returning the label text
	public String mess(){

		return statuslbl.getText();
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Sensor(arguments).setVisible(true);
			}
		});
	}
}

