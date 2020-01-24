import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Engine.Candidate;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Fmd.Format;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

public class Verification extends JPanel implements ActionListener {
	private static final long serialVersionUID = 6;
	private static final String ACT_BACK = "back";
	private static final String ACT_LOAD = "load";
	private static final String ACT_LOAD_FROM_DB = "load_from_db";

	private CaptureThread m_capture;
	private Reader m_reader;
	private Fmd[] m_fmds;
	private JDialog m_dlgParent;
	public FingerDB db = new FingerDB("localhost", "uareu", "root", "");
	public List<FingerDB.Record> m_listOfRecords = new ArrayList<FingerDB.Record>();
	public List<Fmd> m_fmdList = new ArrayList<Fmd>();
	public Fmd[] m_fmdArray = null; // Will hold final array of FMDs to identify
									// against
	private JTextArea m_text;
	public Fmd m_enrollmentFmd;
	private ImagePanel m_imagePanel;
	private JButton m_loadFromDB;
	private JButton m_load;

	private final String m_strPrompt1 = "Verification started\n    put any finger on the reader\n\n";
	private final String m_strPrompt2 = "    put the same or any other finger on the reader\n\n";

	private Verification(Reader reader) {
		m_reader = reader;
		m_fmds = new Fmd[2]; // two FMDs to perform comparison

		final int vgap = 5;
		final int width = 380;

		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);

		m_imagePanel = new ImagePanel();
		m_imagePanel.setPreferredSize(new Dimension(100, 300));
		add(m_imagePanel);

		m_text = new JTextArea(22, 1);
		m_text.setEditable(false);
		JScrollPane paneReader = new JScrollPane(m_text);
		add(paneReader);
		Dimension dm = paneReader.getPreferredSize();
		dm.width = width;
		paneReader.setPreferredSize(dm);

		add(Box.createVerticalStrut(vgap));

		m_load = new JButton("Load FMD");
		m_load.setActionCommand(ACT_LOAD);
		m_load.addActionListener(this);
		add(m_load);

		m_loadFromDB = new JButton("Load FMD from Database");
		m_loadFromDB.setActionCommand(ACT_LOAD_FROM_DB);
		m_loadFromDB.addActionListener(this);
		add(m_loadFromDB);

		JButton btnBack = new JButton("Back");
		btnBack.setActionCommand(ACT_BACK);
		btnBack.addActionListener(this);
		add(btnBack);
		add(Box.createVerticalStrut(vgap));

		setOpaque(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACT_BACK)) {
			// cancel capture
			StopCaptureThread();
			m_dlgParent.setVisible(false);
		}

		else if (e.getActionCommand().equals(ACT_LOAD)) {
			byte[] fmdFromFile = loadDataFile();
			try {
				this.m_enrollmentFmd = UareUGlobal.GetImporter().ImportFmd(
						fmdFromFile, Format.DP_REG_FEATURES,
						Format.DP_REG_FEATURES);
				this.m_loadFromDB.setEnabled(false);
				this.m_listOfRecords.clear();
			} catch (UareUException e1) {
				// TODO Auto-generated catch block
				MessageBox.DpError("ImportFmd", e1);
			}
		} else if (e.getActionCommand().equals(ACT_LOAD_FROM_DB)) {
			System.out.println("Fingerprints loaded from database");
			try {
				db.Open();
				this.m_listOfRecords = db.GetAllFPData();
				System.out.println("Records fetched");
				for (FingerDB.Record record : this.m_listOfRecords) {
					Fmd fmd = UareUGlobal
							.GetImporter()
							.ImportFmd(
									record.fmdBinary,
									com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES,
									com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES);
					this.m_fmdList.add(fmd);
					System.out.println("Looping through fingerprints");
				}
				m_fmdArray = new Fmd[this.m_fmdList.size()];
				this.m_fmdList.toArray(m_fmdArray);
				System.out.println("Fingerprints loaded completely");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				MessageBox
						.DpError(
								"Failed to load FMDs from database.  Please check connection string in code.",
								null);
				return;
			} catch (UareUException e1) {
				// TODO Auto-generated catch block
				JOptionPane
						.showMessageDialog(null, "Error importing fmd data.");
				return;
			}

			this.m_load.setEnabled(false); // Dont allow user to load fmd from
											// file (confusing).
			this.m_enrollmentFmd = null;

		} else if (e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
			System.out.println("Capture triggered");
			// process result
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent) e;
			System.out.println("Capture 2.....");
			if (evt.capture_result.image != null)
				System.out.println("Capture 3.....");
				if (ProcessCaptureResult(evt)) {
					System.out.println("Capture ..gggg...");
					// restart capture thread
					WaitForCaptureThread();
					StartCaptureThread();
				} else {
					System.out.println("Capture ..77.");
					// destroy dialog
					m_dlgParent.setVisible(false);
				}
		}
	}

	private void StartCaptureThread() {
		m_capture = new CaptureThread(m_reader, false,
				Fid.Format.ANSI_381_2004,
				Reader.ImageProcessing.IMG_PROC_DEFAULT);
		m_capture.start(this);
	}

	private void StopCaptureThread() {
		if (null != m_capture)
			m_capture.cancel();
	}

	private void WaitForCaptureThread() {
		if (null != m_capture)
			m_capture.join(1000);
	}

	private boolean ProcessCaptureResult(CaptureThread.CaptureEvent evt) {
		boolean bCanceled = false;

		if (this.m_enrollmentFmd == null && this.m_listOfRecords.size() == 0) {
			MessageBox
					.Warning("You cannot verify until you register or load a template.");
//			Enrollment enrollment =  new Enrollment(null);
//			JPanel panelForm = enrollment.createMainForm("view", (long) 2);
//			JScrollPane  pane = new JScrollPane(panelForm);//JPanel is wrapped with JScrollPane
//			JOptionPane.showOptionDialog(null, pane,"View Record", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
			return !bCanceled;
		}

		if (null != evt.capture_result) {
			if (null != evt.capture_result.image
					&& Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
				// extract features
				Engine engine = UareUGlobal.GetEngine();

				try {
					m_imagePanel.showImage(evt.capture_result.image);
					Fmd fmd = engine.CreateFmd(evt.capture_result.image,
							Fmd.Format.DP_VER_FEATURES);
					m_fmds[0] = fmd;

					// Lets perform 1:1 comparison
					if (this.m_enrollmentFmd != null) {
						m_fmds[1] = this.m_enrollmentFmd;

						try {
							int falsematch_rate = engine.Compare(m_fmds[0], 0,
									m_fmds[1], 0);

							int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; // target
																							// rate
																							// is
																							// 0.00001
							if (falsematch_rate < target_falsematch_rate) {
								m_text.append("MATCHED !!!\n");
								String str = String.format(
										"    dissimilarity score: 0x%x.\n",
										falsematch_rate);
								m_text.append(str);
								str = String
										.format("    false match rate: %e.\n\n\n",
												(double) (falsematch_rate / Engine.PROBABILITY_ONE));
								m_text.append(str);
							} else {
								m_text.append("NO MATCH!!!\n\n\n");
							}
						} catch (UareUException e) {
							MessageBox.DpError("Engine.Compare exception()", e);
						}

						// discard FMDs
						m_fmds[0] = null;
						m_fmds[1] = null;

					} else // Perform identification
					{
						int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; // target
																						// rate
																						// is
																						// 0.00001
						Candidate[] matches = engine.Identify(m_fmds[0], 0,
								m_fmdArray, target_falsematch_rate, 1);
						if (matches.length == 1){

							JOptionPane
									.showMessageDialog(
											null,
											"Match found:"
													+ this.m_listOfRecords
															.get(matches[0].fmd_index).surname
									+ " " + this.m_listOfRecords.get(matches[0].fmd_index).othernames);


							Enrollment enrollment =  new Enrollment(null);
							JPanel panelForm = enrollment.createMainForm("view",this.m_listOfRecords.get(matches[0].fmd_index).id);
							JScrollPane  pane = new JScrollPane(panelForm);//JPanel is wrapped with JScrollPane
							JOptionPane.showOptionDialog(null, pane,"View Record", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);


						}else
							JOptionPane.showMessageDialog(null,
									"Not Identified!!!");
					}
				} catch (UareUException e) {
					MessageBox.DpError("Engine.CreateFmd()", e);
				}
			}

			else {
				// the loop continues
				m_text.append(m_strPrompt2);
			}
		}

		else if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
			// capture or streaming was canceled, just quit
			bCanceled = true;
		}

		else if (null != evt.exception) {
			// exception during capture
			MessageBox.DpError("Capture", evt.exception);
			bCanceled = true;
		} else if (null != evt.reader_status) {
			// reader failure
			MessageBox.BadStatus(evt.reader_status);
			bCanceled = true;
		}

		else {
			// bad quality
			MessageBox.BadQuality(evt.capture_result.quality);
		}

		return !bCanceled;
	}

	private void doModal(JDialog dlgParent) {
		// open reader
		try {
			m_reader.Open(Reader.Priority.COOPERATIVE);
		} catch (UareUException e) {
			MessageBox.DpError("Reader.Open()", e);
		}

		// start capture thread
		System.out.println("Capture thread started for verification");
		StartCaptureThread();

		// put initial prompt on the screen
		m_text.append(m_strPrompt1);

		// bring up modal dialog
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.toFront();
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();

		// cancel capture
		StopCaptureThread();

		// wait for capture thread to finish
		WaitForCaptureThread();

		// close reader
		try {
			m_reader.Close();
		} catch (UareUException e) {
			MessageBox.DpError("Reader.Close()", e);
		}
	}

	private byte[] loadDataFile() {
		// TODO Auto-generated method stub
		System.out.println("Loading fingerprint from file");
		JFileChooser fc = new JFileChooser(new File("test"));

		fc.showOpenDialog(this);
		if (fc.getSelectedFile() != null) {
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(
						fc.getSelectedFile()));
				byte[] data = new byte[input.available()];
				input.read(data, 0, input.available());
				input.close();
				return data;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error saving file.");
			}
		}
		return null;
	}

	public static void Run(Reader reader, Fmd _enrolledFmd) {
		JDialog dlg = new JDialog((JDialog) null, "Verification", true);
		Verification verification = new Verification(reader);
		verification.m_enrollmentFmd = _enrolledFmd;
		verification.doModal(dlg);
	}

//	public JPanel showVerifiedUser( FingerDB.Record record){
//		Enrollment enrollment =  new Enrollment(m_reader);
//		JPanel panelForm = enrollment.createMainForm("view",record.id);
//		JScrollPane  pane = new JScrollPane(panelForm);//JPanel is wrapped with JScrollPane
//		JOptionPane.showOptionDialog(null, pane,"View Record", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
//
//		return panelForm;
//	}
}
