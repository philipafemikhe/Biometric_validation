import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.html.ImageView;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import javafx.scene.layout.FlowPane;

import static java.lang.System.exit;

public class UareUSampleJava extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1;

	private static final String ACT_SELECTION = "selection";
	private static final String ACT_CAPTURE = "capture";
	private static final String ACT_STREAMING = "streaming";
	private static final String ACT_VERIFICATION = "verification";
	private static final String ACT_IDENTIFICATION = "identification";
	private static final String ACT_ENROLLMENT = "enrollment";
	private static final String ACT_EXIT = "exit";

	private static final String ACT_REFRESH = "refresh";

	private JDialog m_dlgParent;
	private JTextArea m_textReader;

	private static ReaderCollection m_collection;
	private static Reader m_reader;
	private static Fmd enrollmentFMD;
	private static Fmd enrollmentFMD2;

	private BufferedImage image, enrolImage, captureImage, verifyImage, dashboardImage, settingsImage;

	private JTable table;
	private JPanel mainPanel = new JPanel(); // the panel is not visible in output
	private JFrame frame = new JFrame("Jofem Biometric Register");

	private UareUSampleJava() {
//		JFrame frame = new JFrame("Jofem Biometric Register");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setSize(400, 400);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setBackground(Color.pink);


		//Creating the MenuBar and adding components
		JMenuBar mb = new JMenuBar();
		JMenu mFile = new JMenu("FILE");
		JMenu mHelp = new JMenu("Help");
		JMenu mBiometrics = new JMenu("Biometrics");
		JMenu mView = new JMenu("View");
		JMenu mFind = new JMenu("Find");
		mb.add(mFile);
		mb.add(mBiometrics);
		mb.add(mView);
		mb.add(mFind);
		mb.add(mHelp);

		JMenuItem mItemOpen = new JMenuItem("Open");
		JMenuItem mItemSaveAs = new JMenuItem("Save as");
		JMenuItem mItemExit  =  new JMenuItem("Exit");
		mFile.add(mItemOpen);
		mFile.add(mItemSaveAs);
		mFile.add(mItemExit);

		JMenuItem mItemEnrol = new JMenuItem("New Enrollment");
		JMenuItem mItemPhoto = new JMenuItem("Photo Capture");
		JMenuItem mItemVerify = new JMenuItem("Verification");
		JMenuItem mItemViewList = new JMenuItem("View List");
		mBiometrics.add(mItemEnrol);
		mBiometrics.add(mItemPhoto);
		mBiometrics.add(mItemVerify);
		mBiometrics.add(mItemViewList);

		mItemEnrol.setActionCommand(ACT_ENROLLMENT);
		mItemEnrol.addActionListener(this);
		mItemPhoto.setActionCommand(ACT_CAPTURE);
		mItemPhoto.addActionListener(this);
		mItemVerify.setActionCommand(ACT_VERIFICATION);
		mItemVerify.addActionListener(this);
		mItemExit.setActionCommand(ACT_EXIT);
		mItemExit.addActionListener(this);

		JMenuItem mItemViewByName = new JMenuItem("By Name");
		JMenuItem mItemViewByLga = new JMenuItem("By LGA");
		JMenuItem mItemViewByDate = new JMenuItem("By RegDate");
		JMenuItem mItemViewByState = new JMenuItem("By State");
		mView.add(mItemViewByName);
		mView.add(mItemViewByLga);
		mView.add(mItemViewByDate);
		mView.add(mItemViewByState);

		JMenuItem mItemFind = new JMenuItem("Find Record");
		JMenuItem mItemAdmin = new JMenuItem("Moderators");
		mFind.add(mItemFind);
		mFind.add(mItemAdmin);

		JMenuItem mItemAbout = new JMenuItem("About");
		JMenuItem mItemDoc = new JMenuItem("Documentation");
		mHelp.add(mItemAbout);
		mHelp.add(mItemDoc);


		frame.getContentPane().add(BorderLayout.NORTH, mb);

//		JPanel mainPanel = new JPanel(); // the panel is not visible in output
		mainPanel.setLayout(new GridBagLayout());
//		//Adding Components to the frame.
//
		Integer rowIndex = 0;
		Integer colIndex = 0;

		try {
			image = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\logo.png"));
		} catch (IOException ex) {
			// handle exception...
		}
		JLabel logoLabel = new JLabel(new ImageIcon(image));
		GridBagConstraints logoGridBagCons = new GridBagConstraints();
		//logoGridBagCons.weightx = 1;
		logoGridBagCons.gridx = colIndex;
		logoGridBagCons.gridy = rowIndex++;
		mainPanel.add(logoLabel, logoGridBagCons);

		rowIndex++;

		JLabel headRow0 = new JLabel("Jofem Biometric Enrollment");
		headRow0.setFont(new Font("Verdana", Font.PLAIN, 36));

		GridBagConstraints headRow0GridBagCons = new GridBagConstraints();

		headRow0GridBagCons.weightx = 1;
		headRow0GridBagCons.gridx = colIndex;
		headRow0GridBagCons.gridy = rowIndex++;
		mainPanel.add(headRow0, headRow0GridBagCons);


		JButton btnEnrollment = new JButton("Run enrollment");
		btnEnrollment.setActionCommand(ACT_ENROLLMENT);
		btnEnrollment.addActionListener(this);

		JButton btnVerification = new JButton("Run verification");
		btnVerification.setActionCommand(ACT_VERIFICATION);
		btnVerification.addActionListener(this);

		JButton btnCapture = new JButton("Take Picture");
		btnCapture.setActionCommand(ACT_CAPTURE);
		btnCapture.addActionListener(this);

		JButton btnExit = new JButton("Exit");
		btnExit.setActionCommand(ACT_EXIT);
		btnExit.addActionListener(this);

		JPanel btnPanel = new JPanel();
		GridBagLayout btnGrid = new GridBagLayout();
		btnPanel.setLayout(btnGrid);
		GridBagConstraints btnGridBagCons = new GridBagConstraints();


		JLabel label = new JLabel("Enter search Text");
		JTextField tf = new JTextField(10); // accepts upto 10 characters
		JButton send = new JButton("Find");
		JButton reset = new JButton("Reset");
		JButton refresh = new JButton();
		btnPanel.add(label); // Components Added using Flow Layout
		btnPanel.add(tf);
		btnPanel.add(send);
		btnPanel.add(reset);
		refresh.setActionCommand(ACT_REFRESH);
		refresh.addActionListener(this);
		try {
			enrolImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\refresh.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		refresh.setIcon(new ImageIcon(enrolImage));
		btnPanel.add(refresh);
		//frame.getContentPane().add(BorderLayout.SOUTH, panel);

		btnGridBagCons.weightx = 1;
		btnGridBagCons.gridx = colIndex;
		btnGridBagCons.gridy = rowIndex;
		btnPanel.setBackground(Color.lightGray);
		btnPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		mainPanel.add(new JScrollPane(btnPanel), btnGridBagCons);


		JLabel headRow = new JLabel("List of Registerd Members");
		headRow.setFont(new Font("Verdana", Font.PLAIN, 18));


		rowIndex = 3;
		btnGridBagCons.weightx = 1;
		btnGridBagCons.gridx = 0;
		btnGridBagCons.gridy = rowIndex;
		mainPanel.add(headRow, btnGridBagCons);


		JPanel jpane =  new JPanel();
		jpane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		jpane.setLayout(new GridBagLayout());
		GridBagConstraints gridBagCons = new GridBagConstraints();
		gridBagCons.insets = new Insets(10,0,0,10);

		gridBagCons.gridx = 0;
		gridBagCons.gridy = 0;
		jpane.add(new JLabel("State"), gridBagCons);

		gridBagCons.gridx = 1;
		gridBagCons.gridy = 0;
		JComboBox stateCombo = new JComboBox();
		stateCombo.setPreferredSize(new Dimension(300, 20));
		jpane.add(stateCombo, gridBagCons);

		gridBagCons.gridx = 2;
		gridBagCons.gridy = 0;
		jpane.add(new JLabel("LGA"), gridBagCons);

		gridBagCons.gridx = 3;
		gridBagCons.gridy = 0;
		JComboBox lgaCombo = new JComboBox();
		lgaCombo.setPreferredSize(new Dimension(300,20));
		jpane.add(lgaCombo, gridBagCons);

		gridBagCons.gridx = 4;
		gridBagCons.gridy = 0;
		JButton btnFilter = new JButton();
		btnFilter.setPreferredSize(new Dimension(300,20));
		jpane.add(btnFilter, gridBagCons);


		rowIndex = 5;
		gridBagCons.weightx = 1;
		gridBagCons.gridx = 0;
		gridBagCons.gridy = rowIndex;
		gridBagCons.insets = new Insets(10,10,10,10);
		//jpane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		jpane.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(0)));
		mainPanel.add(new JScrollPane(jpane), gridBagCons);

		this.table = this.loadData();
		table.setRowHeight(30);

		gridBagCons.weightx = 1;
		gridBagCons.gridx = 0;
		gridBagCons.gridy = 7;
		gridBagCons.fill = GridBagConstraints.HORIZONTAL;

		mainPanel.add(new JScrollPane(table), gridBagCons);


		//mainPanel.add(table, gridBagCons);
		mainPanel.setBackground(Color.WHITE);

		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);

		try {
			enrolImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\enrol.png"));
			verifyImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\verify.png"));
			captureImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\capture.png"));
			dashboardImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\dashboard.png"));
			settingsImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\images\\settings.png"));

		} catch (IOException ex) {
			// handle exception...
		}

		JPanel panel2 = new JPanel(); // the panel is not visible in output
		panel2.setLayout(new GridBagLayout());
//		panel2.setBackground(SystemColor.white);
		panel2.setBackground(Color.lightGray);

		GridBagConstraints constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.VERTICAL;

		JButton picEnrolButton = new JButton(new ImageIcon(enrolImage));
		constr.weightx = 1;
		constr.gridx = 0;
		constr.gridy = 0;
		constr.insets = new Insets(20,10,10,10);
		constr.anchor = GridBagConstraints.NORTH;
		panel2.add(picEnrolButton, constr);

		JButton picVerifyButton = new JButton(new ImageIcon(verifyImage));
		constr.weightx = 1;
		constr.gridx = 0;
		constr.gridy = 1;
		constr.insets = new Insets(20,10,10,10);
		constr.anchor = GridBagConstraints.NORTH;
		panel2.add(picVerifyButton, constr);

		JButton picCaptureButton = new JButton(new ImageIcon(captureImage));
		constr.weightx = 1;
		constr.gridx = 0;
		constr.gridy = 2;
		constr.insets = new Insets(20,10,10,10);
		constr.anchor = GridBagConstraints.NORTH;
		panel2.add(picCaptureButton, constr);

		JButton picSummariesButton = new JButton(new ImageIcon(dashboardImage));
		constr.weightx = 1;
		constr.gridx = 0;
		constr.gridy = 3;
		constr.insets = new Insets(20,10,10,10);
		constr.anchor = GridBagConstraints.NORTH;
		panel2.add(picSummariesButton, constr);

		JButton picSettingsButton = new JButton(new ImageIcon(settingsImage));
		constr.weightx = 1;
		constr.gridx = 0;
		constr.gridy = 4;
		constr.insets = new Insets(20,10,10,10);
		constr.anchor = GridBagConstraints.NORTH;
		panel2.add(picSettingsButton, constr);

		picEnrolButton.setActionCommand(ACT_ENROLLMENT);
		picEnrolButton.addActionListener(this);

		picCaptureButton.setActionCommand(ACT_CAPTURE);
		picCaptureButton.addActionListener(this);

		picVerifyButton.setActionCommand(ACT_VERIFICATION);
		picVerifyButton.addActionListener(this);

		frame.getContentPane().add(BorderLayout.WEST, panel2);


		frame.setVisible(true);

	}

	private JTable loadData_a() {
		FingerDB db = new FingerDB("localhost", "uareu", "root", "");
		List<FingerDB.Record> m_listOfRecords = new ArrayList<FingerDB.Record>();

		JTable table;
		// Initialize column headings.
		String[] colHeads = { "Photo", "Surname", "Other Names", "BVN", "Address#" };

		try {
			db.Open();
			m_listOfRecords = db.GetAllFPData();
			Object[][] data = new Object[m_listOfRecords.size()][6];
			int i=0;
			for (FingerDB.Record record : m_listOfRecords) {
				try {
					image = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\pictures\\" + record.photo));
				} catch (IOException ex) {
					// handle exception...
				}
				JLabel picLabel = new JLabel(new ImageIcon(image));

				data[i][0] = image;;
				data[i][1] = record.surname;
				data[i][2] = record.othernames;
				data[i][3] = record.bvn;
				data[i][4] = record.address;
//				data[i][2] = record.fmdBinary;
//				data[i][3] = "nil";
				System.out.println(record.surname);
				i++;
			}
			table = new JTable(data, colHeads);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			MessageBox
					.DpError(
							"Failed to load FMDs from database.  Please check connection string in code.",
							null);
			return new JTable();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			JOptionPane
					.showMessageDialog(null, e1);
			return new JTable();
		}
		return table;
	}


	private JTable loadData() {
		FingerDB db = new FingerDB("localhost", "uareu", "root", "");
		List<FingerDB.Record> m_listOfRecords = new ArrayList<FingerDB.Record>();

		//JTable table;
		// Initialize column headings.
		String[] colHeads = {"RecNo", "Photo", "Surname", "Other Names", "BVN", "Address" };
		DefaultTableModel model = new DefaultTableModel(colHeads, 0);// <-- 0 is number of rows
		table = new JTable(model);

		try {
			db.Open();
			m_listOfRecords = db.GetAllFPData();
			//Object[][] data = new Object[m_listOfRecords.size()][6];
			int i=0;
			for (FingerDB.Record record : m_listOfRecords) {
				try {
					image = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\pictures\\" + record.photo));
				} catch (IOException ex) {
					// handle exception...
				}

				ImageIcon picIcon = new ImageIcon(image);
				//ImageIcon picIcon = new ImageIcon(image);
				Object[] rowData = {record.id, picIcon, record.surname, record.othernames,record.bvn,record.address};
				model.addRow(rowData);


				System.out.println(record.id);
				i++;
			}
			table = new JTable(model);
			table.setFont(new Font("Verdana", Font.PLAIN, 12));

			table.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
				    JPopupMenu popupMenu = new JPopupMenu();
				    JMenuItem viewPopUp = new JMenuItem("View");
				    popupMenu.add(viewPopUp);
                    JMenuItem editPopUp = new JMenuItem("Edit");
				    popupMenu.add(editPopUp);
                    JMenuItem deletePopUp = new JMenuItem("Delete");
				    popupMenu.add(deletePopUp);

                    viewPopUp.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e)
                        {
                            int col = 0;
                            int row = table.getSelectedRow();
                            if(row >=0){
								Long userId = (Long) table.getModel().getValueAt(row,col);
								System.out.println("View popup selected on user with id  " + userId);
								JPanel editPanel = new JPanel();

								Enrollment enrol = new Enrollment(null);
								System.out.println("View popup selected on user with id  " + userId);
								editPanel = enrol.createMainForm("view", userId);

								JScrollPane  pane = new JScrollPane(editPanel);//JPanel is wrapped with JScrollPane
								JOptionPane.showOptionDialog(null, pane,"View Record", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);

								//JOptionPane.showMessageDialog(null, pane, "Edit Record", JOptionPane.PLAIN_MESSAGE,null);//JScrollPane is added to the JOptionPane

								//frame.getContentPane().add(BorderLayout.EAST, editPanel);
							}else{
                            	System.out.println("No selection detected..");
							}
                        }
                    });

                    editPopUp.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e)
                        {
                            System.out.println("edit popup selected");
							int col = 0;
							int row = table.getSelectedRow();
							if(row >=0){
								Long userId = (Long) table.getModel().getValueAt(row,col);
								System.out.println("Edit popup selected on " + userId);
								JPanel editPanel = new JPanel();

								Enrollment enrol = new Enrollment(null);
								editPanel = enrol.createMainForm("edit", userId);

								JScrollPane  pane = new JScrollPane(editPanel);//JPanel is wrapped with JScrollPane
								JOptionPane.showOptionDialog(null, pane,"Edit Record", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);

								//JOptionPane.showMessageDialog(null, pane, "Edit Record", JOptionPane.PLAIN_MESSAGE,null);//JScrollPane is added to the JOptionPane

								//frame.getContentPane().add(BorderLayout.EAST, editPanel);
							}else{
								System.out.println("No selection detected..");
							}
                        }
                    });

                    deletePopUp.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e)
                        {
							Long userId;
							System.out.println("delete popup selected");
							int col = 0;
							int row = table.getSelectedRow();
							if(row >=0) {
								userId = (Long) table.getModel().getValueAt(row, col);


								FingerDB fingerDB = new FingerDB("localhost", "uareu", "root", "");
								FingerDB.Record record = null;
								try {
									fingerDB.Open();
									fingerDB.deleteUser(Math.toIntExact(userId));
								} catch (SQLException err) {
									err.printStackTrace();
								}
							}


                        }
                    });

				    System.out.println("Table click event fired");
                    if(e.getClickCount() ==2){
                        System.out.println("Double click");
                    }else if(e.getButton() == MouseEvent.BUTTON3){
                        System.out.println("Right click");
                        popupMenu.show(e.getComponent(),e.getX(),e.getY());
                    }else{
                        System.out.println("Left click");
                        //popupMenu.show(e.getComponent(),e.getX(),e.getY());
                    }

				}
			});
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
//			MessageBox
//					.DpError(
//							"Failed to load FMDs from database.  Please check connection string in code.",
//							null);
			return new JTable();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			JOptionPane
					.showMessageDialog(null, e1);
			return new JTable();
		}


		return table;
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(ACT_VERIFICATION)) {

			try {
				this.m_collection = UareUGlobal.GetReaderCollection();
				m_collection.GetReaders();
			} catch (UareUException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error getting collection");
				return;
			}

			if (m_collection.size() == 0) {
				MessageBox.Warning("Reader is not selected");
				return;
			}

			m_reader = m_collection.get(0);

			if (null == m_reader) {
				MessageBox.Warning("Reader is not selected");
			} else {
//				Verification.Run(m_reader, this.enrollmentFMD);
				Verification.Run(m_collection.get(0), this.enrollmentFMD);
			}
		}

		else if (e.getActionCommand().equals(ACT_ENROLLMENT)) {

			try {
				this.m_collection = UareUGlobal.GetReaderCollection();
				m_collection.GetReaders();
			} catch (UareUException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error getting collection");
				return;
			}

			if (m_collection.size() == 0) {
				MessageBox.Warning("Reader is not selected");
				return;
			}

			m_reader = m_collection.get(0);

			if (null == m_reader) {
				MessageBox.Warning("Reader is not selected");
			} else {

				this.enrollmentFMD = Enrollment.Run(m_reader);
			}
		}else if (e.getActionCommand().equals(ACT_CAPTURE)) {
			TakePicture.Run();
		}else if(e.getActionCommand().equals(ACT_REFRESH)){
			System.out.println("Refreshing table list");

			FingerDB fingerDB = new FingerDB("localhost","uareu","root","");
			List<FingerDB.Record>	userList = new ArrayList<>();
			FingerDB.Record record = null;
			try {
				fingerDB.Open();
				userList = fingerDB.GetAllFPData();
			} catch (SQLException er) {
				er.printStackTrace();
			}
			if (userList != null) {

				DefaultTableModel model = new DefaultTableModel(userList.size(), 6);
				table.setModel(model);

				JTableHeader th = table.getTableHeader();
				TableColumnModel tcm = th.getColumnModel();
				TableColumn tc = tcm.getColumn(0);
				tc.setHeaderValue( "ID" );
				tc = tcm.getColumn(1);
				tc.setHeaderValue( "Photo" );
				tc = tcm.getColumn(2);
				tc.setHeaderValue( "Surname" );
				tc = tcm.getColumn(3);
				tc.setHeaderValue( "Other Names" );
				tc = tcm.getColumn(4);
				tc.setHeaderValue( "BVN" );
				tc = tcm.getColumn(5);
				tc.setHeaderValue( "Address" );

				th.repaint();
				int rowIndex = 0;
				for (FingerDB.Record usr: userList){
					if(usr.id >= 1){
						table.getModel().setValueAt(usr.id, rowIndex, 0);
						table.getModel().setValueAt(usr.photo, rowIndex, 1);
						table.getModel().setValueAt(usr.surname, rowIndex, 2);
						table.getModel().setValueAt(usr.othernames, rowIndex, 3);
						table.getModel().setValueAt(usr.bvn, rowIndex, 4);
						table.getModel().setValueAt(usr.address, rowIndex, 5);
						rowIndex++;
					}
				}

			}

			//table.getModel().setValueAt("54553533544353", 1, 1);

//			GridBagConstraints gridBagCons = new GridBagConstraints();
//			gridBagCons.weightx = 1;
//			gridBagCons.gridx = 0;
//			gridBagCons.gridy = 7;
//			gridBagCons.fill = GridBagConstraints.HORIZONTAL;
//
//			mainPanel.add(null, gridBagCons);
//			revalidate();
//			repaint();
//
//
//
//			this.table = loadData();
//
//			GridBagConstraints gridBagCons = new GridBagConstraints();
//			gridBagCons.weightx = 1;
//			gridBagCons.gridx = 0;
//			gridBagCons.gridy = 7;
//
//			mainPanel.add(new JScrollPane(table), gridBagCons);
//			mainPanel.setBackground(Color.WHITE);
//			mainPanel.revalidate();
//			mainPanel.repaint();
//
//			frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		}else if (e.getActionCommand().equals(ACT_EXIT)) {
			//m_dlgParent.setVisible(false);
			exit(0);
		}
	}

	private void doModal(JDialog dlgParent) {
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		//m_dlgParent.setSize(1000,800);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		m_dlgParent.setSize(screenSize);
		//m_dlgParent.setBounds(20,20,1000,600);
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();
	}

	private static void createAndShowGUI() {
		UareUSampleJava paneContent = new UareUSampleJava();

		// initialize capture library by acquiring reader collection
		try {
			paneContent.m_collection = UareUGlobal.GetReaderCollection();
		} catch (UareUException e) {
			MessageBox.DpError("UareUGlobal.getReaderCollection()", e);
			return;
		}

		// run dialog
//		JDialog dlg = new JDialog((JDialog) null,
//				"Finger Print Scanning Demo", true );
//		paneContent.doModal(dlg);
//		dlg.setVisible(false);

		// release capture library by destroying reader collection
		try {
			UareUGlobal.DestroyReaderCollection();
		} catch (UareUException e) {
			MessageBox.DpError("UareUGlobal.destroyReaderCollection()", e);
		}
	}

	public static void main(String[] args) throws IOException {
		 //SwingUtilities.invokeLater(new WebcamViewerExample());
		 createAndShowGUI();
		try {
			m_collection = UareUGlobal.GetReaderCollection();
			m_collection.GetReaders();
		} catch (UareUException e1) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error getting collection");
			return;
		}

		m_reader = m_collection.get(0);

		enrollmentFMD = Enrollment.Run(m_reader);

	}

}
