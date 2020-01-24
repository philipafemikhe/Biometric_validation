import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JFileChooser;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;


public class Enrollment extends JPanel implements ActionListener {

	public class EnrollmentThread extends Thread implements
			Engine.EnrollmentCallback {
		public static final String ACT_PROMPT = "enrollment_prompt";
		public static final String ACT_CAPTURE = "enrollment_capture";
		public static final String ACT_FEATURES = "enrollment_features";
		public static final String ACT_DONE = "enrollment_done";
		public static final String ACT_CANCELED = "enrollment_canceled";
		public static final String ACT_SAVE = "save";

		//public static final String ACT_UPDATE = "update";

		public class EnrollmentEvent extends ActionEvent {
			private static final long serialVersionUID = 102;

			public Reader.CaptureResult capture_result;
			public Reader.Status reader_status;
			public UareUException exception;
			public Fmd enrollment_fmd;

			public EnrollmentEvent(Object source, String action, Fmd fmd,
					Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
				super(source, ActionEvent.ACTION_PERFORMED, action);
				capture_result = cr;
				reader_status = st;
				exception = ex;
				enrollment_fmd = fmd;
			}
		}

		private final Reader m_reader;
		private CaptureThread m_capture;
		private final ActionListener m_listener;
		private boolean m_bCancel;

		protected EnrollmentThread(Reader reader, ActionListener listener) {
			m_reader = reader;
			m_listener = listener;
		}

		@Override
		public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
			Engine.PreEnrollmentFmd prefmd = null;

			while (null == prefmd && !m_bCancel) {
				// start capture thread
				m_capture = new CaptureThread(m_reader, false,
						Fid.Format.ISO_19794_4_2005,
						Reader.ImageProcessing.IMG_PROC_DEFAULT);
				m_capture.start(null);

				// prompt for finger
				SendToListener(ACT_PROMPT, null, null, null, null);

				// wait till done
				m_capture.join(0);

				// check result
				CaptureThread.CaptureEvent evt = m_capture
						.getLastCaptureEvent();
				if (null != evt.capture_result) {
					if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
						// capture canceled, return null
						break;
					} else if (null != evt.capture_result.image
							&& Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
						// Send image
						SendToListener(ACT_CAPTURE, null, evt.capture_result,
								null, null);

						// acquire engine
						Engine engine = UareUGlobal.GetEngine();

						try {
							// extract features

							Fmd fmd = engine.CreateFmd(
									evt.capture_result.image,
									Fmd.Format.DP_PRE_REG_FEATURES);

							// return prefmd
							prefmd = new Engine.PreEnrollmentFmd();
							prefmd.fmd = fmd;
							prefmd.view_index = 0;

							// send success
							SendToListener(ACT_FEATURES, null, null, null, null);
						} catch (UareUException e) {
							// send extraction error
							SendToListener(ACT_FEATURES, null, null, null, e);
						}
					} else {
						// send quality result
						SendToListener(ACT_CAPTURE, null, evt.capture_result,
								evt.reader_status, evt.exception);
					}
				} else {
					// send capture error
					SendToListener(ACT_CAPTURE, null, evt.capture_result,
							evt.reader_status, evt.exception);
				}
			}

			return prefmd;
		}

		public void cancel() {
			m_bCancel = true;
			if (null != m_capture)
				m_capture.cancel();
		}

		private void SendToListener(String action, Fmd fmd,
				Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
			if (null == m_listener || null == action || action.equals(""))
				return;

			final EnrollmentEvent evt = new EnrollmentEvent(this, action, fmd,
					cr, st, ex);

			// invoke listener on EDT thread
			try {
				javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						m_listener.actionPerformed(evt);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// acquire engine
			Engine engine = UareUGlobal.GetEngine();

			try {
				m_bCancel = false;
				while (!m_bCancel) {
					// run enrollment
					Fmd fmd = engine.CreateEnrollmentFmd(
							Fmd.Format.DP_REG_FEATURES, this);

					// send result
					if (null != fmd) {

						SendToListener(ACT_DONE, fmd, null, null, null);
					} else {
						SendToListener(ACT_CANCELED, null, null, null, null);
						break;
					}
				}
			} catch (UareUException e) {
				JOptionPane.showMessageDialog(null,
						"Exception during creation of data and import");
				SendToListener(ACT_DONE, null, null, null, e);
			}
		}
	}

	private static final long serialVersionUID = 6;
	private static final String ACT_BACK = "back";
	private static final String ACT_SAVE = "save";
	private static final String ACT_SAVE_DB = "save2db";

	public com.digitalpersona.uareu.Fmd enrollmentFMD;
	public com.digitalpersona.uareu.Fmd enrollmentFMD2;
	private final EnrollmentThread m_enrollment;
	//private final EnrollmentThread m_enrollment2;
	private final Reader m_reader;
	private JDialog m_dlgParent;
	private final JTextArea m_text;
	private boolean m_bJustStarted;

//	private final JLabel infosurname_text;
//	private final JTextField surname_text;
//	private final JTextField othernames_text;
//	private final JTextArea address_text;
//	public final JComboBox gender_text;
//	private final JComboBox lga_text;
//	private final JComboBox state_text;
//	private final JComboBox nationality_text;
//	private final JButton photo;
//	private final JTextField bvn_text;
//	private final JLabel lblState;
//	private final JLabel lblLga;
//	private final JButton m_save;
//	private final JButton m_save2DB;


	private  JLabel infosurname_text;
	private  JTextField surname_text;
	private  JTextField othernames_text;
	private  JTextArea address_text;
	public  JComboBox gender_text;
	private  JComboBox lga_text;
	private  JComboBox state_text;
	private  JComboBox nationality_text;
	private  JButton photo;
	private  JTextField bvn_text;
	private  JLabel lblState;
	private  JLabel lblLga;
	private  JButton m_save;
	private  JButton m_save2DB;
	private JLabel photoPreview;

	private final ImagePanel m_imagePanel;

	//private final GridBagConstraints gridBagCons;

	private ComboUtilities comboUtilities = new ComboUtilities();

	public Enrollment(Reader reader) {
		m_reader = reader;
		m_bJustStarted = true;
		m_enrollment = new EnrollmentThread(m_reader, this);

		final int vgap = 5;
		final int width = 380;

		// Define the panel to hold the buttons
		JPanel panelScan = new JPanel();
		JPanel panelText = new JPanel();
		JPanel panelForm = new JPanel();

		panelScan.setBorder(BorderFactory.createTitledBorder("Preview"));
		panelText.setBorder(BorderFactory.createTitledBorder("Monitor"));
		panelForm.setBorder(BorderFactory.createTitledBorder("Personal Date"));

		BoxLayout layout1 = new BoxLayout(panelScan, BoxLayout.Y_AXIS);
		BoxLayout layout2 = new BoxLayout(panelText, BoxLayout.Y_AXIS);
		GridBagLayout layout3 = new GridBagLayout();

		panelScan.setLayout(layout1);
		panelText.setLayout(layout2);
		panelForm.setLayout(layout3);


		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);

		setLayout(layout);

		m_imagePanel = new ImagePanel();
		m_imagePanel.setPreferredSize(new Dimension(300, 300));
		panelScan.add(m_imagePanel);
		add(panelScan);


		photoPreview = new JLabel();
		//photoPreview.setVisible(false);
		panelText.add(photoPreview);


		m_text = new JTextArea(22, 1);
		m_text.setEditable(false);
		JScrollPane paneReader = new JScrollPane(m_text);
		Dimension dm = paneReader.getPreferredSize();
		dm.width = width;
		paneReader.setPreferredSize(dm);
		panelText.add(paneReader);
		add(panelText);

		add(createMainForm("add", null));

		setOpaque(true);


	}

	public JPanel createMainForm(String operation, Long uniquId){
		System.out.println("Unique id: " + uniquId + ", operation= " + operation);
		JPanel panelForm = new JPanel();
		GridBagLayout layout3 = new GridBagLayout();
		panelForm.setLayout(layout3);
		GridBagConstraints gridBagCons =  new GridBagConstraints();
		gridBagCons.insets = new Insets(20,0,0,10);
		int row = 0;
		String title="";
		if(operation=="add"){
			title = "Capture Personal Data";
		}else if(operation =="edit"){
			title = "Edit Personal Data";
		}else if(operation=="view"){
			title = "View Personal Data";
		}
		JLabel headerLabel = new JLabel("<html><font color='black'><b>" + title + "</b></font></html>");
		headerLabel.setFont(new Font("Verdana", Font.PLAIN, 24));

		gridBagCons.gridwidth = 2;
		gridBagCons.gridx = 0;
		gridBagCons.gridy = row++;
		panelForm.add(headerLabel, gridBagCons);

		gridBagCons.gridwidth=1;
		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;

		//gridBagCons.fill = GridBagConstraints.HORIZONTAL;
		infosurname_text = new JLabel("Surname:");
		panelForm.add(infosurname_text, gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		surname_text = new JTextField(40);
		panelForm.add(surname_text, gridBagCons);

		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		//infosurname_text = new JLabel("Enter Username:");
		panelForm.add(new JLabel("Other Names"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		othernames_text = new JTextField(40);
		panelForm.add(othernames_text, gridBagCons);

		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		//infosurname_text = new JLabel("Enter Username:");
		panelForm.add(new JLabel("Gender"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		gender_text = new JComboBox();
		gender_text.setPreferredSize(new Dimension(300, 20));
		panelForm.add(gender_text, gridBagCons);

		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		//infosurname_text = new JLabel("Enter Username:");
		panelForm.add(new JLabel("Address"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		address_text = new JTextArea(5,30);
		panelForm.add(address_text, gridBagCons);


		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		//infosurname_text = new JLabel("Enter Username:");
		panelForm.add(new JLabel("Nationality"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		nationality_text = new JComboBox();
		nationality_text.setPreferredSize(new Dimension(300, 20));
		panelForm.add(nationality_text, gridBagCons);


		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		lblState = new JLabel("State");
		panelForm.add(lblState, gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		state_text = new JComboBox();
		state_text.setPreferredSize(new Dimension(300, 20));
		panelForm.add(state_text, gridBagCons);

		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		lblLga = new JLabel("LGA");
		panelForm.add(lblLga, gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		lga_text = new JComboBox();
		lga_text.setPreferredSize(new Dimension(300, 20));
		panelForm.add(lga_text, gridBagCons);


		row++;

		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("BVN"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		bvn_text = new JTextField(11);
		panelForm.add(bvn_text, gridBagCons);


		gridBagCons.gridx = 0;
		gridBagCons.gridy = row;
		gridBagCons.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Photo"), gridBagCons);
		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		gridBagCons.anchor = GridBagConstraints.WEST;
		photo = new JButton("Select Picture");
		panelForm.add(photo, gridBagCons);


		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		//gridBagCons.gridwidth = 2;
		gridBagCons.anchor = GridBagConstraints.WEST;
		JLabel photoFileName = new JLabel("no file selected");
		//photoFileName.setVisible(false);
		panelForm.add(photoFileName, gridBagCons);

		JPanel pn = new JPanel();
		BoxLayout buttonLayout = new BoxLayout(pn, BoxLayout.X_AXIS);
		pn.setLayout(buttonLayout);
		JButton btnBack = new JButton("Back");
		btnBack.setActionCommand(ACT_BACK);
		btnBack.addActionListener(this);

		m_save = new JButton("Save to File");
		m_save.setActionCommand(ACT_SAVE);
		m_save.addActionListener(this);
		m_save.setEnabled(false);
		//add(m_save);

		m_save2DB = new JButton("Save to DB");
		m_save2DB.setActionCommand(ACT_SAVE_DB);
		m_save2DB.addActionListener(this);
		m_save2DB.setEnabled(false);
		//add(m_save2DB);

		gridBagCons.gridx = 1;
		gridBagCons.gridy = row++;
		pn.setAlignmentX(LEFT_ALIGNMENT);

		if(operation=="add"){
			pn.add(btnBack);
			pn.add(m_save);
			pn.add(m_save2DB);

			panelForm.add(pn, gridBagCons);
		} else if(operation == "edit"){
			JButton btnUpdate = new JButton("Update");
			JButton btnCancel = new JButton("Cancel");

			btnUpdate.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					//update record
					FingerDB db = new FingerDB("localhost", "uareu", "root", "");
					try {
						db.Open();
					}catch (SQLException ex){
						ex.printStackTrace();
					}
					Object item = nationality_text.getSelectedItem();
					String nationalityValue = ((ComboItem) item).getValue();

					item = state_text.getSelectedItem();
					String stateValue = ((ComboItem) item).getValue();

					item = lga_text.getSelectedItem();
					String lgaValue = ((ComboItem) item).getValue();

					try {
						db.Update(Math.toIntExact(uniquId), surname_text.getText(), othernames_text.getText(),
                                gender_text.getSelectedItem().toString(), address_text.getText(), nationalityValue,stateValue,lgaValue,bvn_text.getText(),
								photoFileName.getText());
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
			pn.add(btnUpdate);
			pn.add(btnCancel);
			panelForm.add(pn, gridBagCons);
		} else if(operation =="view"){
			surname_text.setEditable(false);
			othernames_text.setEditable(false);
			bvn_text.setEditable(false);
			gender_text.setEditable(false);
			address_text.setEditable(false);
			JButton btnFirst = new JButton("First");
			JButton btnPrev = new JButton("Previous");
			JButton btnNext = new JButton("Next");
			JButton btnLast = new JButton("Last");
			pn.add(btnFirst);
			pn.add(btnPrev);
			pn.add(btnNext);
			pn.add(btnLast);
			panelForm.add(pn, gridBagCons);
		}


		comboUtilities.loadGenderCombo(this.gender_text);
		comboUtilities.loadStates(this.state_text);
		nationality_text.addItem(new ComboItem("Select One", "-1"));
		nationality_text.addItem(new ComboItem("Nigeria", String.valueOf(160)));
		nationality_text.addItem(new ComboItem("Non-Nigeria", String.valueOf(0)));

		nationality_text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//String country = nationality_text.getSelectedItem().toString();
				Object item = nationality_text.getSelectedItem();
				if(item != null){
					String value = ((ComboItem)item).getKey();
					System.out.println("Event listener fired.." + value + " " + ((ComboItem)item).getKey());
					state_text.setVisible(value == "Nigeria");
					lga_text.setVisible(value == "Nigeria");
					lblLga.setVisible(value == "Nigeria");
					lblState.setVisible(value == "Nigeria");
				}

			}
		});

		state_text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Object item = state_text.getSelectedItem();
				if(item != null){
					System.out.println("State selected: " + ((ComboItem)item).getValue() + " - " + ((ComboItem)item).getKey());
				}

				lga_text.removeAllItems();
				comboUtilities.loadLgaCombo(lga_text, (long) state_text.getSelectedIndex());
			}
		});

		photo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fc = new JFileChooser();
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();

					try {
						if((new ImageIcon(ImageIO.read(file)).getIconWidth() > 75) || (new ImageIcon(ImageIO.read(file)).getIconHeight() > 75)){
						    Miscellanouse.msgbox("Image lenght and width must be 75","Image size Error");
							return;
						}
						photoPreview.setIcon(new ImageIcon(ImageIO.read(file)));
						photoPreview.setVisible(true);
						photoFileName.setText(file.getName());
						photo.setIcon(new ImageIcon(ImageIO.read(file)));
//						photoFileName.setVisible(true);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		FingerDB fingerDB = new FingerDB("localhost","uareu","root","");

		FingerDB.Record record = null;
		try {
			fingerDB.Open();
			record = fingerDB.GetSingleFPData(uniquId);
//			record = fingerDB.GetSingleFPData("Success");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (record != null){
			this.surname_text.setText(record.surname);
			this.othernames_text.setText(record.othernames);

			if((operation=="view") || (operation =="edit")){
				System.out.println("Selected action: " + operation);
				state_text.setVisible(true);
				lga_text.setVisible(true);
				gender_text.removeAllItems();
				nationality_text.removeAllItems();
				this.state_text.removeAllItems();
				this.gender_text.addItem(record.gender);
				this.address_text.setText(record.address);
				FingerDB fdb = new FingerDB("localhost","uareu","root", "");
				try {
					fdb.Open();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				String nation;
				if(record.nationality_id == 160){
					this.nationality_text.addItem(new ComboItem("Nigeria", String.valueOf(160)));
				}else{
					this.nationality_text.addItem(new ComboItem("Non-Nigeria", String.valueOf(0)));
				}
				try {
					this.state_text.addItem(new ComboItem(fdb.GetSingleState(Long.valueOf(record.state_id)).name, fdb.GetSingleState(Long.valueOf(record.state_id)).id.toString()));
					this.lga_text.removeAllItems();
					this.lga_text.addItem(new ComboItem(fdb.GetSingleLga(Long.valueOf(record.lga_id)).name, fdb.GetSingleLga(Long.valueOf(record.lga_id)).id.toString()));
					state_text.setVisible(true);
					lga_text.setVisible(true);
					if(operation=="edit"){

						List<FingerDB.State> m_listOfState = new ArrayList<>();
						m_listOfState = fdb.GetAllStates();
						for (FingerDB.State state : m_listOfState) {
							this.state_text.addItem(new ComboItem(state.name, state.id.toString()) );
						}

						this.gender_text.addItem(new ComboItem("Male", "Male"));
						this.gender_text.addItem(new ComboItem("Female", "Female"));

						this.nationality_text.addItem(new ComboItem("Nigeria", String.valueOf(160)));
						this.nationality_text.addItem(new ComboItem("Non-Nigeria", String.valueOf(0)));

						List<FingerDB.Lga> m_listOfLga = new ArrayList<>();
						m_listOfLga = fdb.GetStateLgas((long) record.state_id);
							for (FingerDB.Lga lga : m_listOfLga) {
								this.lga_text.addItem(new ComboItem(lga.name, lga.id.toString()));
							}

					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				gender_text.setSelectedItem(record.gender);
				this.state_text.setSelectedItem(record.state_id);
				this.lga_text.setSelectedItem(record.lga_id);
			} else{
				if(record.nationality_id ==160){
					this.nationality_text.addItem(new ComboItem("Nigeria", String.valueOf(record.nationality_id)));
				}else{
					this.nationality_text.addItem(new ComboItem("Non-Nigeria", String.valueOf(0)));
				}

//				this.state_text.addItem(new ComboItem() record.state_id);
				this.lga_text.removeAllItems();
				FingerDB fdb = new FingerDB("localhost","uareu","root", "");
				try {
					fdb.Open();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					this.state_text.addItem(new ComboItem(fdb.GetSingleState(Long.valueOf(record.state_id)).name, fdb.GetSingleState(Long.valueOf(record.state_id)).id.toString()));
					this.lga_text.removeAllItems();
					this.lga_text.addItem(new ComboItem(fdb.GetSingleLga(Long.valueOf(record.lga_id)).name, fdb.GetSingleLga(Long.valueOf(record.lga_id)).id.toString()));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			this.bvn_text.setText(record.bvn);
			BufferedImage image=null;
			try {
				image = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\pictures\\" + record.photo));
			} catch (IOException ex) {
				// handle exception...
			}
			if(image != null)
			this.photo.setIcon(new ImageIcon(image));
		}


		return panelForm;

	}

	private Image getScaledImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACT_BACK)) {
			// destroy dialog to cancel enrollment
			m_dlgParent.setVisible(false);

			return;
		} else if (e.getActionCommand().equals(ACT_SAVE_DB)) {
			FingerDB db = new FingerDB("localhost", "uareu", "root", "");
			try {
				db.Open();
				if (this.surname_text.getText().isEmpty() != true) {
					// Check if user already exists
					if (db.UserExists(this.surname_text.getText()) == false) {
						Object item = nationality_text.getSelectedItem();
						String nationalityValue = ((ComboItem)item).getKey();
						System.out.println("Nat value:" + nationalityValue);
						String stateValue = ((ComboItem)state_text.getSelectedItem()).getKey();
						System.out.println("State value:" + stateValue);
						String lgaValue = ((ComboItem)lga_text.getSelectedItem()).getKey();
						System.out.println("LGA value:" + lgaValue);
						db.Insert(this.surname_text.getText(), this.othernames_text.getText(),
								this.gender_text.getSelectedItem().toString(), this.address_text.getText(), nationalityValue,stateValue,lgaValue,this.bvn_text.getText(),
								this.photo.getName(),this.enrollmentFMD.getData(),
								this.enrollmentFMD.getData());
						m_dlgParent.setVisible(false);
					} else {
						JOptionPane.showMessageDialog(null,
								"Username already taken.");
						this.surname_text.requestFocusInWindow();
					}

				} else {
					JOptionPane.showMessageDialog(null,
							"Please enter a unique username");
					this.surname_text.requestFocusInWindow();
				}

			} catch (SQLException e3) {
				e3.printStackTrace();
				JOptionPane.showMessageDialog(null, e3.getMessage());
			}
			try {
				db.Close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		else if (e.getActionCommand().equals(ACT_SAVE)) {
			saveDataToFile(enrollmentFMD.getData());
			this.m_save.setEnabled(false);
			return;
		} else {
			this.captureAndPrepare(e, enrollmentFMD);
			this.captureAndPrepare(e, enrollmentFMD2);
		}
	}

	private void captureAndPrepare(ActionEvent e, Fmd enrolFmd){

		EnrollmentThread.EnrollmentEvent evt = (EnrollmentThread.EnrollmentEvent) e;

		if (e.getActionCommand().equals(EnrollmentThread.ACT_PROMPT)) {
			if (m_bJustStarted) {
				m_text.append("Enrollment started\n");
				m_text.append("    Put your any finger on the reader\n");
			} else {
				m_text.append("    Put the same finger on the reader\n");
			}
			m_bJustStarted = false;
		} else if (e.getActionCommand()
				.equals(EnrollmentThread.ACT_CAPTURE)) {

			if (null != evt.capture_result)
				if (evt.capture_result.image != null)
					m_imagePanel.showImage(evt.capture_result.image);
			System.out.println("Score is " + evt.capture_result.score);
			System.out.println("Qualityis " + evt.capture_result.quality);

			if (null != evt.capture_result) {
				// MessageBox.BadQuality(evt.capture_result.quality);
			} else if (null != evt.exception) {

				// MessageBox.DpError("Capture", evt.exception);
			} else if (null != evt.reader_status) {
				// MessageBox.BadStatus(evt.reader_status);
			}

			m_bJustStarted = false;
		} else if (e.getActionCommand().equals(
				EnrollmentThread.ACT_FEATURES)) {
			if (null == evt.exception) {
				m_text.append("    fingerprint captured, features extracted\n\n");
			} else {
				MessageBox.DpError("Feature extraction", evt.exception);
			}
			m_bJustStarted = false;
		}

		else if (e.getActionCommand().equals(EnrollmentThread.ACT_DONE)) {
			if (null == evt.exception) {
				String str = String
						.format("    Enrollment template created, size: %d\n\n\nPlease save to file or verify.",
								evt.enrollment_fmd.getData().length);
				enrolFmd = evt.enrollment_fmd;
				m_enrollment.cancel();
				this.m_save.setEnabled(true);
				this.m_save2DB.setEnabled(true);
				m_text.append(str);
			} else {
				MessageBox.DpError("Enrollment template creation",
						evt.exception);
			}
			m_bJustStarted = true;
		} else if (e.getActionCommand().equals(
				EnrollmentThread.ACT_CANCELED)) {
			// canceled, destroy dialog
			m_dlgParent.setVisible(false);
		}

		// cancel enrollment if any exception or bad reader status
		if (null != evt.exception) {
			m_dlgParent.setVisible(false);
		} else if (null != evt.reader_status
				&& Reader.ReaderStatus.READY != evt.reader_status.status
				&& Reader.ReaderStatus.NEED_CALIBRATION != evt.reader_status.status) {
			m_dlgParent.setVisible(false);
		}
	}

	private void saveDataToFile(byte[] data) {

		System.out.println(new String(data));

		// TODO Auto-generated method stub
		JFileChooser fc = new JFileChooser(new File("test"));

		fc.showSaveDialog(this);
		if (fc.getSelectedFile() != null) {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						fc.getSelectedFile()));
				output.write(data);
				output.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error saving file.");
			}
		}
	}

	private void doModal(JDialog dlgParent) {
		// open reader
		try {
			m_reader.Open(Reader.Priority.EXCLUSIVE);
		} catch (UareUException e) {
			MessageBox.DpError("Reader.Open()", e);
		}

		// start enrollment thread
		m_enrollment.start();

		// bring up modal dialog
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();

		// stop enrollment thread
		m_enrollment.cancel();

		// close reader
		try {
			m_reader.Close();
		} catch (UareUException e) {
			MessageBox.DpError("Reader.Close()", e);
		}
	}

	public static Fmd Run(Reader reader) {
		JDialog dlg = new JDialog((JDialog) null, "Enrollment", true);
		Enrollment enrollment = new Enrollment(reader);
		enrollment.doModal(dlg);

		return enrollment.enrollmentFMD;
	}

//	private void msgbox(String s, String title){
//		JOptionPane.showMessageDialog(null, s, title,0);
//	}
}
