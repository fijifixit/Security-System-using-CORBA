import ClientAndServer.*;
import ClientAndServer.RegionalOfficePackage.connectError;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



class HomeHubServant extends HomeHubPOA {

	private ORB orb;
	public ClientAndServer.RegionalOffice server;
	public ClientAndServer.Camera camera;
	public ClientAndServer.Sensor sensor;
	public ClientAndServer.HomeView homeView;
	private HomeHub parent;
	private Timer timer;
	private String hubName;
	private boolean isHit;
	private static String hitName;
	private static String roomName;
	public ArrayList<ClientAndServer.Camera> camConn = new ArrayList<ClientAndServer.Camera>();
	public ArrayList<ClientAndServer.Sensor> sensorConn = new ArrayList<ClientAndServer.Sensor>();
	public ArrayList<String> logs = new ArrayList<String>();



	public HomeHubServant(HomeHub parentGUI, String hubNameNew, ORB orb_val, String args[]) {
		// store reference to parent GUI
		parent = parentGUI;
		hubName = hubNameNew;
		// store reference to ORB
		orb = orb_val;
		// look up the server
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
			String name = "server";
			server = RegionalOfficeHelper.narrow(nameService.resolve_str(name));

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
	}

	//Method for getting the time for the logs
	public String logTime(){
		//create new simple date format
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//create new date
		Date date = new Date();
		//format the date to a string
		String time = (sdf.format(date));
		//return the time and date
		return time;
	}

	//Method called by the camera or sensor to raise an alarm
	public boolean call_alarm(String deviceID, String hubName, String roomID) {
		//create new timer
		timer = new Timer();
		// set i to 5 seconds
		long i = 5000;
		//call the cancel timer class and pass in variable i
		timer.schedule(new CancelTimer(this),i);
		//if boolean  is equal to false
		if (isHit == false){
			hitName = deviceID;
			roomName = roomID;
			isHit = true;
			//add to the array logs
			logs.add("false alarm by"+ " " + deviceID+ " "+ " "+ " " + logTime());
		}
		//if the device id is different but in the same room
		else if (!deviceID.equals(hitName) & roomID.equals(roomName)){
			//call alarm method
			alarm(deviceID, hubName, roomID);
			//return true
			return true;
		}
		return false;
	}


	//Method called by the call_alarm methhod
	public String alarm(String deviceID, String hubName, String roomID) {
		//set boolean to false
		setIsHit(false);
		//call cancel timer 
		CancelTimer();
		//create new timer
		timer = new Timer();
		//set i to 5 seconds
		long i = 5000;
		//schedule timer and call cancel timer. pass through i
		timer.schedule(new CancelTimer(this),i);
		//return the messae from the regional office alarm raised method
		String messageFromOffice = server.alarmRaised(deviceID, hubName, roomID);
		//add to logs array
		logs.add(deviceID + " " + "has raised an alarm"+ " "+ " "+ " " + logTime());
		// if the homeView is not empty
		if(homeView != null){
			//call return message method in homeview
			homeView.returnMessage(messageFromOffice);
		}
		//return the message
		return messageFromOffice;
	}

	//method for setting the isHit boolean
	public void setIsHit(boolean newIsHit){

		isHit = newIsHit;
	}

	//Method for returning isHit
	public boolean getIsHit(){
		return isHit;

	}

	//method for canceling the timer
	public void CancelTimer(){

		timer.cancel();
	}

	//Method for returning the hubName
	public String getName() {

		return hubName;
	}

	//Method called when the cameras has been switched off
	public void toggleOff(String deviceID){
		server.cameraOff(deviceID);
		logs.add(deviceID+ " " + "has been switched off" + " "+ " "+ " " + logTime());

	}

	//Method called when the camera has been switched on
	public void toggleOn(String deviceID){

		server.cameraOn(deviceID);
		logs.add(deviceID+ " " + "has been switched on" + " "+ " "+ " " + logTime());
	}

	//Method called when a new camera has been added
	public void addCam(ClientAndServer.Camera newCam){
		//get cameras name
		String cameraName = newCam.getName();
		//get room name
		String roomName = newCam.getRoomName();
		//call the cameraAdded method in regional office
		server.cameraAdded(cameraName, hubName, roomName);
		//add to logs
		logs.add(cameraName+" " + "has been added"+ " "+ " "+ " " + logTime());

	}

	//Method called when a new sensor has been added
	public void addSens(ClientAndServer.Sensor newSensor){
		//get sensor name
		String cameraName = newSensor.getName();
		//get room name
		String roomName = newSensor.getRoomName();
		//call the sensorAdded method in the regional office
		server.sensorAdded(cameraName, hubName, roomName);
		//add to logs
		logs.add(cameraName+" " + "has been added"+ " "+ " "+ " " + logTime());

	}

	@Override
	//method called to add a new cam to the array
	public void newCam(String deviceID) {
		addCamArray(deviceID);

	}

	@Override
	//Method called to add a new sensor to the array
	public void newSensor(String deviceID) {
		addSensorArray(deviceID);
	}

	//Method called when the regional office resets a camera
	public void resetCam(String camName){
		//for loop to check the camConn array for the correct camera
		for(int i = 0; i < camConn.size(); i++){
			if(camConn.get(i).getName().equals(camName)){
				//call remote reset method in the correct camera.
				camConn.get(i).remoteReset();
			}
		}
		//add to logs
		logs.add(camName+ " " + "alarm has been reset"+ " "+ " "+ " " + logTime());
	}

	//Method called when the regional office resets a sensor
	public void resetSensor(String camName){
		//for loop to check the sensorConn array for the correct sensor
		for(int i = 0; i < sensorConn.size(); i++){
			if(sensorConn.get(i).getName().equals(camName)){
				//call remote reset method in the correct camera.
				sensorConn.get(i).remoteReset();
			}
		}
		//add to logs
		logs.add(camName+ " " + "alarm has been reset"+ " "+ " "+ " " + logTime());
	}

	//method called when the camera hits reset cam alarm
	public void resetCamAlarm(String deviceID){
		server.resetAlarm(deviceID, hubName);
	}

	//method called when the sensor hits reset sensor alarm

	public void resetSensorAlarm(String deviceID){
		server.resetAlarm(deviceID, hubName);
	}

	//Method used to store the logs into a new array
	public String[] getLog() {
		//get size of logs array and add to logArray
		String[] logArray = new String[logs.size()];
		//add to logArray
		logArray = logs.toArray(logArray);
		// return logArray
		return logArray;
	}


	@Override
	//Method is called when the homeview has registered for notification. 
	public void sendText(String deviceID, String roomID) {
		//if homeview is not empty
		if(homeView != null){
			//call getMessage in homeview
			homeView.getMessage(deviceID, roomID);
		}
	}

	@Override
	//Method used by the regional office to get the status of the camera.
	public String returnCamStatus(String camName) {
		//for loop to find the right camera
		for(int i = 0; i < camConn.size(); i++){
			//if the camera name equals the camera name passed through
			if(camConn.get(i).getName().equals(camName)){
				//return the status of the camera
				return camConn.get(i).currentStatus();
			}
		}
		return null;
	}

	@Override
	//Method used by the regional office to get the status of the sensor.
	public String returnSensorStatus(String sensorName) {
		//for loop to find the right sensor
		for(int i = 0; i < sensorConn.size(); i++){
			//if the sensor name equals the sensor name passed through
			if(sensorConn.get(i).getName().equals(sensorName)){
				//return the status of the sensor
				return sensorConn.get(i).currentStatus();
			}
		}
		return null;
	}


	//Method called by the HomView once it has connected to the homehub
	public void connectHomeView(String homeViewName){
		try {

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
			String name = homeViewName;
			homeView = HomeViewHelper.narrow(nameService.resolve_str(name));


		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}


	@Override
	//Method called by the camera once it has connected to the homehub
	public void addCamArray(String deviceID) {
		// TODO Auto-generated method stub

		try {
			String[] newArgs = {"-ORBInitialPort", "1050"}; 
			ORB orb = ORB.init(newArgs, null);



			// Initialize the ORB
			System.out.println("Initializing the ORB");


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
			String name = deviceID;
			camera = CameraHelper.narrow(nameService.resolve_str(name));
			camConn.add(camera);

			System.out.println(camConn);

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}

	}

	@Override
	//Method called by the sensor once it has connected to the homeHub
	public void addSensorArray(String deviceID) {
		// TODO Auto-generated method stub

		try {
			String[] newArgs = {"-ORBInitialPort", "1050"}; 
			ORB orb = ORB.init(newArgs, null);

			// Initialize the ORB
			System.out.println("Initializing the ORB");


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
			String name = deviceID;
			sensor = SensorHelper.narrow(nameService.resolve_str(name));
			sensorConn.add(sensor);

		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}

	}

	@Override
	//method called by the homeview once it has registered for notification
	public void register(String name, String number) {
		server.register(name, number, hubName);
	}

	@Override
	//method called by the homeview once it has unregistered
	public void Unregister() {
		server.unRegister(hubName);
	}

	@Override
	//Method called once the homeHub has connected. 
	//passes the hubname to the regional office.
	public void homeHubConnect(String hubName) {
		try {
			server.homeHubConnect(hubName);
		} catch (connectError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void homeHubAdded(String hubName) {
		server.homeHubAdded(hubName);

	}

	@Override
	//Method called to pass the details of the homehub to the regional office
	public void details(String hubName, String address, String mobile) {
		server.details(hubName,address,mobile);
	}
}




public class HomeHub extends JFrame {
	private JPanel panel;
	private ClientAndServer.RegionalOffice server;
	private ClientAndServer.HomeHub homeHub;
	private JList list;
	private Long dateN;
	//private List<String> a = new ArrayList<String>();
	private String hubName = ""+((int)(Math.random()*9000)+1000);
	private String address, mobile;
	private JLabel nameLabel;
	public HomeHub(String[] args) {

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

		JFrame newFrame = new JFrame("HomeHub Name");
		JPanel p = new JPanel(new GridLayout(0,1));

		JTextField addressField = new JTextField(20);
		JLabel addressLbl = new JLabel("Address");
		p.add(addressLbl);
		p.add(addressField);

		JTextField mobileTextfield = new JTextField(20);
		JLabel mobileLbl = new JLabel("Mobile Number");
		p.add(mobileLbl);
		p.add(mobileTextfield);


		int result = JOptionPane.showConfirmDialog(newFrame, p,"HomeHub Detail",JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION){
			address = addressField.getText();
			mobile = mobileTextfield.getText();
		}else{

			System.exit(0);
		}

		try {
			
			// Initialize the OR
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			HomeHubServant homeHubRef = new HomeHubServant(this,hubName, orb, args);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(homeHubRef);
			homeHub = HomeHubHelper.narrow(ref);

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) {
				System.out.println("nameServiceObj = null");
				return;
			}
			System.out.println("here 1");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null) {
				System.out.println("nameService = null");
				return;
			}
			// bind the Count object in the Naming service
			String name = hubName ;
			System.out.println(hubName);
			NameComponent[] countName = nameService.to_name(name);
			nameService.rebind(countName, homeHub);

			homeHubRef.homeHubConnect(hubName);
			homeHubRef.homeHubAdded(hubName);
			homeHubRef.details(hubName,address,mobile);



			panel = new JPanel();
			getContentPane().add(panel, "Center");
			panel.setLayout(null);

			nameLabel = new JLabel("------");
			nameLabel.setBounds(46, 24, 121, 14);
			panel.add(nameLabel);

			setSize(225, 98);
			setTitle("HomeHub");
			nameLabel.setText("HomeHub Id: " + " " + hubName);

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowOpened (java.awt.event.WindowEvent evt) {

				}
			} );



		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}



	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new  HomeHub(arguments).setVisible(true);
			}
		});
	}

}
