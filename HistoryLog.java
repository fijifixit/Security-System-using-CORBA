import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Color;

@SuppressWarnings("serial")
public class HistoryLog extends JFrame{


	private JScrollPane scrollPane;
	private JList list;
	private DefaultListModel listModel;
	private JButton saveBtn,closeBtn;
	private JLabel downloadLbl,homeHubNameLbl,hubLabel;
	private String [] lines = null;

	public HistoryLog(String [] logs, String hubName){
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
		
		getContentPane().setBackground(Color.PINK);

		setSize(557, 338);
		setLocation(250, 250);
		getContentPane().setLayout(null);

		closeBtn = new JButton("Close");
		closeBtn.setBounds(305, 55, 231, 29);

		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		} );

		listModel = new DefaultListModel();
		list = new JList(listModel);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 49, 289, 240);
		scrollPane.setViewportView(list);
		getContentPane().add(scrollPane);

		getContentPane().add(closeBtn);

		homeHubNameLbl = new JLabel("HomeHub:");
		homeHubNameLbl.setBounds(28, 16, 117, 16);
		getContentPane().add(homeHubNameLbl);

		lines = logs;
		for (String s: logs) {
			listModel.addElement(s);
		}
		int size = list.getModel().getSize();
		list.ensureIndexIsVisible(size - 1);

		saveBtn = new JButton("Save Log");
		saveBtn.setBounds(305, 107, 231, 29);
		getContentPane().add(saveBtn);
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					//new bufferedwriter with filename
					BufferedWriter out = new BufferedWriter(new FileWriter(hubName+".txt"));
					//for loop 
					for (int q = 0; q < lines.length; q++) {
						//write the lines to q
						out.write(lines[q]);
						out.newLine();
					}
					//close bufferedwriter
					out.close();
					//show joption pane with message
					JFrame message = new JFrame("Saved Logs");
					JOptionPane.showMessageDialog(message, "The logs have been saved. Please check your source directory");
				} catch (IOException ex) {}

			}
		} );

		downloadLbl = new JLabel("");
		downloadLbl.setBounds(325, 158, 211, 16);
		downloadLbl.setText("Download as "+ hubName+".txt");
		getContentPane().add(downloadLbl);
		
		hubLabel = new JLabel("---");
		hubLabel.setBounds(93, 17, 84, 14);
		getContentPane().add(hubLabel);
		
		hubLabel.setText(hubName);


		addWindowListener (new java.awt.event.WindowAdapter () {
			@Override
			public void windowClosing (java.awt.event.WindowEvent evt) {
				setVisible(false);
			}
		} );
	}
}