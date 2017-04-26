import ClientAndServer.*;
import ClientAndServer.RegionalOfficePackage.connectError;

import org.omg.CORBA.*;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.*;
import javax.swing.UIManager.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


class RegionalOfficeServant extends RegionalOfficePOA {
	private RegionalOffice parent;
	public ClientAndServer.HomeHub homeHub;
	public ClientAndServer.HomeView homeView;
	public String hubName;
	public ArrayList<ClientAndServer.HomeHub> hubConn = new ArrayList<ClientAndServer.HomeHub>();
	public ArrayList<String> detailList = new ArrayList<String>();
	public ArrayList<String> connected = new ArrayList<String>();
	public ArrayList<String> currentAlarms = new ArrayList<String>();
	public HashMap<String, String> multiMap = new HashMap<String,String>();





	public RegionalOfficeServant(RegionalOffice parentGUI, String hubNameNew) {
		// store reference to parent GUI
		parent = parentGUI;
		hubName = hubNameNew;

	}

	//Method called by the homehub when a confirmed alarm has been raised
	public String alarmRaised(String deviceID, String hubName, String roomID) {
		//add the message
		parent.addMessage("Alarm Raised by:" + " " + deviceID + " " +  " " + "in" + " "+ roomID + " " + "Connected to hub:" + hubName +"\n");
		//for loop for adding to a map
		for(Entry<String,String> result : multiMap.entrySet()){
			//get key and add to string
			String k = result.getKey().toString();
			//if k equals the hubname
			if(k.equals(hubName)){
				//get value as val from result
				String val = result.getValue();
				// if val euqals yes
				if(val.equals("yes")){
					// for loop to check the hubConn array
					for(int s =0; s<hubConn.size(); s++){
						//if index in hubConn array euals hubName
						if(hubConn.get(s).getName().equals(hubName)){
							//call send text method to correct homeHub
							hubConn.get(s).sendText(hubName, roomID);
						}
					}
				}
			}
		}

		currentAlarms.add(hubName + ":" +deviceID);
		//return the string
		return "Cops are on their way" + "\n";
	}

	//Method called by the regional office when a camera has been switched off
	public void cameraOff(String deviceID) {
		parent.addMessage(deviceID + "switched off" + "\n");

	}

	//Method called by the regional office when a camera has been switched on
	public void cameraOn(String deviceID) {
		parent.addMessage(deviceID + "switched on" + "\n");
	}

	//Method called by the regional office when a camera has connected
	public void cameraAdded(String deviceID, String hubName, String roomID){
		//add to connected array
		connected.add(deviceID + ":"+ hubName + ":"+ roomID);
	}

	//Method called by the regional office when a sensor has connected
	public void sensorAdded(String deviceID, String hubName,String roomID){
		connected.add(deviceID + ":"+ hubName + ":"+ roomID);

	}

	//Method called by the regional office when a homeHub has connected
	public void homeHubAdded(String hubName){
		parent.addHomeHub(hubName);

	}

	//Method called when the resetAlarm button has been clicked
	public void resetAlarm(String deviceID, String hubName){
		parent.addMessage(deviceID+ " " + "has been reset" + "\n");
		//for loop to iterate currentAlarms array
		for(int i=0; i <currentAlarms.size(); i++){
			//get the index to string
			String tempAlarm = currentAlarms.get(i).toString();
			//split the array lenth by the :
			String [] total = tempAlarm.split(":",2);
			//set hubTemp to first index
			String hubTemp = total[0];
			//set tempCamera to second index
			String tempCamera = total[1];
			//if hubname equals hubtemp and deviceID equals tempcamera
			if(hubName.equals(hubTemp) && deviceID.equals(tempCamera)){
				//remove index from array
				currentAlarms.remove(i);
			}
		}

	}


	@Override
	//method called when the homehub has entered the details
	public void details(String hubName, String address, String mobile) {
		detailList.add(hubName + ":" + address + ":" + mobile);
	}



	//Method called by the homehub when it starts up. used to connect to the regional office.
	public void homeHubConnect(String hubName) throws connectError{
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
			String name = hubName;
			homeHub = HomeHubHelper.narrow(nameService.resolve_str(name));
			hubConn.add(homeHub);

			multiMap.put(hubName, "no");

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}



	@Override
	//Method called when the homeview registered for notification
	public void register(String name, String number,String hubName) {
		// for loop for map
		for(Entry<String,String> result : multiMap.entrySet()){
			//get key from map to string
			String k = result.getKey().toString();
			//if k equals hubname
			if(k.equals(hubName)){
				//put hubname and yes into map
				multiMap.put(hubName, "yes");
				//call addNotify method 
				parent.addNotify(name, hubName,number);
			}
		}
	}


	@Override
	//method called when the homeview unregisters
	public void unRegister(String hubName) {
		// for loop for multimap
		for(Entry<String,String> result : multiMap.entrySet()){
			//get key from map to string
			String k = result.getKey().toString();
			//if k equals hubname
			if(k.equals(hubName)){
				//put hubname and yes into no
				multiMap.put(hubName, "no");
			}
		}
	}

	//method used to return the homehub log
	public String[] getLog(int i) {
		//get log from correct homeHub
		String[] logArray =	hubConn.get(i).getLog();
		//return log
		return logArray;
	}

	@Override
	//method called when the camera is being reset
	public void resetC(String camName, int value) {
		hubConn.get(value).resetCam(camName);
	}


	@Override
	//method called when the sensor is being reset
	public void resetS(String sensorName, int value) {

		hubConn.get(value).resetSensor(sensorName);
	}

	//method called when checking the status of the camera
	public String camStatus(String camName,int value){
		return hubConn.get(value).returnCamStatus(camName);
	}

	@Override
	//method called when checking the status of the sensor
	public String sensorStatus(String sensorName, int value) {
		return hubConn.get(value).returnSensorStatus(sensorName);
	}
}



public class RegionalOffice extends JFrame {
	private JPanel panel;
	private JScrollPane scrollpane;
	private ClientAndServer.RegionalOffice office;
	private JTextArea textarea;
	private JLabel lblHomehubs, lblCamerasensor,lblDevices,lblDetails;
	private JList homeHubList, camList,list,deviceList,notifyList;
	private DefaultListModel camModel, homeHubModel,listModel,listModelArea,deviceListModel,notifyModel;
	private JButton btnResetSensor, btnCamReset,btnGetLogs,btnCurrentAlarms,btnGetCamStatus,btnGetSensorStatus;
	public RegionalOffice(String[] args){

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


		try {

			// Initialize the OR
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			RegionalOfficeServant officeRef = new RegionalOfficeServant(this, getTitle());

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(officeRef);
			office = RegionalOfficeHelper.narrow(ref);

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
			System.out.println("here 2");
			// bind the Count object in the Naming service
			String name = "server";
			NameComponent[] countName = nameService.to_name(name);
			nameService.rebind(countName, office);
			System.out.println("here 3");



			// set up the GUI
			textarea = new JTextArea(20,25);
			scrollpane = new JScrollPane(textarea);
			scrollpane.setBounds(10, 5, 410, 285);
			panel = new JPanel();
			panel.setBackground(new Color(255, 51, 51));
			panel.setForeground(new Color(255, 99, 71));
			panel.setLayout(null);

			panel.add(scrollpane);
			getContentPane().add(panel, "Center");

			lblHomehubs = new JLabel("HomeHubs");
			lblHomehubs.setBounds(29, 331, 71, 14);
			panel.add(lblHomehubs);

			lblCamerasensor = new JLabel("Camera/Sensor");
			lblCamerasensor.setBounds(188, 331, 96, 14);
			panel.add(lblCamerasensor);

			homeHubModel = new DefaultListModel();
			homeHubList = new JList();
			homeHubList.addListSelectionListener(new ListSelectionListener(){

				public void valueChanged(ListSelectionEvent event) {
					if (!event.getValueIsAdjusting()){
						//get models
						DefaultListModel camModel = (DefaultListModel) 
								camList.getModel();
						camModel.removeAllElements();
						DefaultListModel listModel = (DefaultListModel) 
								list.getModel();
						listModel.removeAllElements();
						DefaultListModel deviceListModel = (DefaultListModel) 
								deviceList.getModel();
						deviceListModel.removeAllElements();

						//get selected value from list and add to string
						String hList = homeHubList.getSelectedValue().toString();
						//for loop for checking connected array
						for(int i = 0; i < officeRef.connected.size(); i++){
							//split the connected index by :
							String[] connected = officeRef.connected.get(i).split(":",3);
							//set first index to deviceID
							String deviceID = connected[0];
							//set second index to hubName
							String hubName = connected[1];
							//set third index to roomID
							String roomID = connected[2];
							//if hubName equals the selected value in the list
							if(hubName.equals(hList)){
								//add to camModel
								camModel.addElement("Device:" + " " + deviceID + " "+ "in" + " " + roomID + " " + "Connected to Hub" + ":" + hubName + "\n");
								deviceListModel.addElement(deviceID);

							}
						}
						camList.setModel(camModel);
						deviceList.setModel(deviceListModel);
						//get the selected value and add to string
						String detailList = homeHubList.getSelectedValue().toString();
						//for loop over the detailList array
						for(int i = 0; i < officeRef.detailList.size(); i++){
							//split the index in the array with :
							String[] details = officeRef.detailList.get(i).split(":",3);
							//set hubname as first index
							String hubName = details[0];
							//set address to second index
							String address = details[1];
							//set mobile to third index
							String mobile = details[2];
							//if hubName equals the selected list
							if(hubName.equals(hList)){
								//call show details method
								showDetails(detailList,address,mobile);
							}
						}
					}
				}
			});

			homeHubList.setBounds(10, 356, 96, 86);
			panel.add(homeHubList);
			homeHubList.setModel(homeHubModel);

			camModel = new DefaultListModel();
			camList = new JList();


			camList.setBounds(116, 356, 223, 86);
			panel.add(camList);
			camList.setModel(camModel);

			btnCamReset = new JButton("Reset Camera");
			btnCamReset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//get selected value to string
					String camName = deviceList.getSelectedValue().toString();
					//get selected value to string
					String hub = homeHubList.getSelectedValue().toString();
					//for loop over hubConn array
					for(int i = 0; i < officeRef.hubConn.size(); i++){
						//if the index in array equals the hub var
						if(officeRef.hubConn.get(i).getName().equals(hub)){
							//call method above
							officeRef.resetC(camName,i);

						}
					}
					// for loop over current alarms
					for(int i=0; i <officeRef.currentAlarms.size(); i++){
						//set index of current alarms to temp alarm
						String tempAlarm = officeRef.currentAlarms.get(i).toString();
						//split index by :
						String [] total = tempAlarm.split(":",2);
						//set hubtemp to first index
						String hubTemp = total[0];
						//set tempcamera to second index
						String tempCamera = total[1];
						if(hub.equals(hubTemp) && camName.equals(tempCamera)){

							officeRef.currentAlarms.remove(i);
						}
					}

					textarea.append(camName + " " + "alarm has been reset" + "\n");
				}

			});
			btnCamReset.setBounds(428, 44, 144, 36);
			panel.add(btnCamReset);


			btnResetSensor = new JButton("Reset Sensor");
			btnResetSensor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//get selected value to string
					String sensorName = deviceList.getSelectedValue().toString();
					//get selected value to string
					String hub = homeHubList.getSelectedValue().toString();
					//for loop over hubConn array
					for(int i = 0; i < officeRef.hubConn.size(); i++){
						//if index equals hub var
						if(officeRef.hubConn.get(i).getName().equals(hub)){
							//call method above
							officeRef.resetS(sensorName,i);
						}
					}
					// for loop over current alarms
					for(int i=0; i <officeRef.currentAlarms.size(); i++){
						//set index of current alarms to temp alarm
						String tempAlarm = officeRef.currentAlarms.get(i).toString();
						//split index by :
						String [] total = tempAlarm.split(":",2);
						//set hubtemp to first index
						String hubTemp = total[0];
						//set tempsensor to second index
						String tempSensor = total[1];
						if(hub.equals(hubTemp) && sensorName.equals(tempSensor)){

							officeRef.currentAlarms.remove(i);
						}
					}
					textarea.append(sensorName + " " + "alarm has been reset" + "\n");
				}
			});
			btnResetSensor.setBounds(582, 44, 144, 36);
			panel.add(btnResetSensor);

			btnGetLogs = new JButton("Get Log");
			btnGetLogs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String hList = homeHubList.getSelectedValue().toString();
					for(int i = 0; i < officeRef.hubConn.size(); i++){
						if(officeRef.hubConn.get(i).getName().equals(hList)){
							String[] logArray =	officeRef.getLog(i);
							HistoryLog hist = new HistoryLog(logArray, hList);
							hist.setVisible(true);
						}
					}

				}
			});
			btnGetLogs.setBounds(428, 5, 144, 36);
			panel.add(btnGetLogs);

			listModel = new DefaultListModel();
			list = new JList();
			list.setBounds(349, 356, 404, 86);
			panel.add(list);
			list.setModel(listModel);

			lblDetails = new JLabel("Details");
			lblDetails.setBounds(546, 331, 58, 14);
			panel.add(lblDetails);

			lblDevices = new JLabel("Devices");
			lblDevices.setBounds(472, 244, 46, 14);
			panel.add(lblDevices);

			deviceListModel = new DefaultListModel();

			btnGetCamStatus = new JButton("Get Camera Status");
			btnGetCamStatus.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//get selected value and set to string
					String hub = homeHubList.getSelectedValue().toString();
					//get selected value and set to string
					String camName = deviceList.getSelectedValue().toString();
					//for loop over hubconn array
					for(int i = 0; i < officeRef.hubConn.size(); i++){
						if(officeRef.hubConn.get(i).getName().equals(hub)){
							String status = officeRef.camStatus(camName, i);
							JFrame frame = new JFrame("Status");
							JOptionPane.showMessageDialog(frame, camName + ":"+ " "  + status);						
						}
					}
				}
			});
			btnGetCamStatus.setBounds(430, 84, 143, 36);
			panel.add(btnGetCamStatus);


			btnGetSensorStatus = new JButton("Get Sensor Status");
			btnGetSensorStatus.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//get selected value and set to string
					String hub = homeHubList.getSelectedValue().toString();
					//get selected value and set to string
					String sensorName = deviceList.getSelectedValue().toString();
					//for loop over hubconn array
					for(int i = 0; i < officeRef.hubConn.size(); i++){
						if(officeRef.hubConn.get(i).getName().equals(hub)){
							String status = officeRef.sensorStatus(sensorName, i);
							JFrame frame = new JFrame("Status");
							JOptionPane.showMessageDialog(frame, sensorName + ":"+ " "  + status);						
						}
					}
				}
			});
			btnGetSensorStatus.setBounds(582, 84, 142, 36);
			panel.add(btnGetSensorStatus);


			btnCurrentAlarms = new JButton("Current Alarms");
			btnCurrentAlarms.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//new jframe
					JFrame frame2 = new JFrame();
					JPanel panelArea = new JPanel(new GridLayout(0, 1));
					JLabel labArea = new JLabel();
					labArea.setText("current alarms");
					panelArea.add(labArea);
					listModelArea = new DefaultListModel();
					JList listArea = new JList(listModelArea);
					panelArea.add(listArea);
					//for loop over current alarms
					for(int i=0; i <officeRef.currentAlarms.size(); i++){
						listModelArea.addElement(officeRef.currentAlarms.get(i));
					}
					//joptionpane
					JOptionPane.showConfirmDialog(frame2, panelArea, "Current alarms : ", JOptionPane.OK_CANCEL_OPTION);

				}
			});
			btnCurrentAlarms.setBounds(582, 5, 144, 36);
			panel.add(btnCurrentAlarms);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(430, 258, 129, 70);
			panel.add(scrollPane);
			deviceList= new JList();
			scrollPane.setViewportView(deviceList);
			deviceList.setModel(deviceListModel);

			notifyModel = new DefaultListModel();
			notifyList = new JList();
			notifyList.setBounds(430, 163, 296, 70);
			panel.add(notifyList);
			notifyList.setModel(notifyModel);

			JLabel lblRegisteredForNotification = new JLabel("Registered for Notification");
			lblRegisteredForNotification.setBounds(501, 146, 163, 14);
			panel.add(lblRegisteredForNotification);





			setSize(779, 491);
			setTitle("Regional Office");

			addWindowListener (new java.awt.event.WindowAdapter () {
				public void windowClosing (java.awt.event.WindowEvent evt) {
					System.exit(0);;
				}
			} );

			textarea.append("Regional Office is Online.  Listening for alarms to be raised...\n\n");

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

	}

	//method called to add message to textarea
	public void addMessage(String message){
		textarea.append(message);
	}
	
	//method called to add camera to the device list
	@SuppressWarnings("unchecked")
	public void addCamera(String deviceID){
		camModel.addElement(deviceID);
	}

	@SuppressWarnings("unchecked")
	//method called to add homehub to list area
	public void addHomeHub(String deviceID){
		homeHubModel.addElement(deviceID);
	}

	//method called to show details for homehub
	public void showDetails(String hubName,String address, String mobile){

		listModel.addElement("Homehub:"+ " " +hubName+" " + "\n"+
				"Address:" + " " + address + " " + "\n"
				+ "Mobile:" + " " + mobile);
	}

	//method called when the user registers the homeview for notification
	public void addNotify(String name, String hubName, String number){

		notifyModel.addElement("Person" + " " + name + " " + "Hub" + " " + hubName + " " + "Mobile:" + " " + number);
	}


	public static void main(String args[]) {
		final String[] arguments = args;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new  RegionalOffice(arguments).setVisible(true);
			}
		});
	}   
}


