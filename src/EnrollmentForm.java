//import com.digitalpersona.uareu.Reader;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//
//import static java.awt.Component.LEFT_ALIGNMENT;
//
//public class EnrollmentForm  extends JPanel{
//
//    private  JLabel infoUsername_text;
//    private  JTextField username_text;
//    private  JTextField othernames_text;
//    private  JTextArea address_text;
//    public  JComboBox gender_text;
//    private  JComboBox lga_text;
//    private  JComboBox state_text;
//    private  JComboBox nationality_text;
//    private  JButton photo;
//    private  JTextField bvn_text;
//    private  JLabel lblState;
//    private  JLabel lblLga;
//    private  JButton m_save;
//    private  JButton m_save2DB;
//    private JLabel photoPreview;
//
//
//    private static final long serialVersionUID = 6;
//    private static final String ACT_BACK = "back";
//    private static final String ACT_SAVE = "save";
//    private static final String ACT_SAVE_DB = "save2db";
//
//    public com.digitalpersona.uareu.Fmd enrollmentFMD;
//    public com.digitalpersona.uareu.Fmd enrollmentFMD2;
//
//    ComboUtilities comboUtilities = new ComboUtilities();
//    //private final EnrollmentThread m_enrollment2;
//
//    private boolean m_bJustStarted;
//
//    public EnrollmentForm(){}
//
//    public JPanel createMainForm(String operation, String uniquId){
//        System.out.println("Unique id: " + uniquId + ", operation= " + operation);
//        JPanel panelForm = new JPanel();
//        GridBagLayout layout3 = new GridBagLayout();
//        panelForm.setLayout(layout3);
//        GridBagConstraints gridBagCons =  new GridBagConstraints();
//        gridBagCons.insets = new Insets(20,0,0,10);
//        int row = 0;
//        String title="";
//        if(operation=="add"){
//            title = "Capture Personal Data";
//        }else if(operation =="edit"){
//            title = "Edit Personal Data";
//        }else if(operation=="view"){
//            title = "View Personal Data";
//        }
//        JLabel headerLabel = new JLabel("<html><font color='red'><b>" + title + "</b></font></html>");
//        headerLabel.setFont(new Font("Verdana", Font.PLAIN, 24));
//
//        gridBagCons.gridwidth = 2;
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row++;
//        panelForm.add(headerLabel, gridBagCons);
//
//
//        gridBagCons.gridwidth=1;
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//
//        //gridBagCons.fill = GridBagConstraints.HORIZONTAL;
//        infoUsername_text = new JLabel("Surname:");
//        panelForm.add(infoUsername_text, gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        username_text = new JTextField(40);
//        panelForm.add(username_text, gridBagCons);
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        //infoUsername_text = new JLabel("Enter Username:");
//        panelForm.add(new JLabel("Other Names"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        othernames_text = new JTextField(40);
//        panelForm.add(othernames_text, gridBagCons);
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        //infoUsername_text = new JLabel("Enter Username:");
//        panelForm.add(new JLabel("Gender"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        gender_text = new JComboBox();
//        gender_text.setPreferredSize(new Dimension(300, 20));
//        panelForm.add(gender_text, gridBagCons);
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        //infoUsername_text = new JLabel("Enter Username:");
//        panelForm.add(new JLabel("Address"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        address_text = new JTextArea(5,30);
//        panelForm.add(address_text, gridBagCons);
//
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        //infoUsername_text = new JLabel("Enter Username:");
//        panelForm.add(new JLabel("Nationality"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        nationality_text = new JComboBox();
//        nationality_text.setPreferredSize(new Dimension(300, 20));
//        panelForm.add(nationality_text, gridBagCons);
//
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        lblState = new JLabel("State");
//        panelForm.add(lblState, gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        state_text = new JComboBox();
//        state_text.setPreferredSize(new Dimension(300, 20));
//        panelForm.add(state_text, gridBagCons);
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        lblLga = new JLabel("LGA");
//        panelForm.add(lblLga, gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        lga_text = new JComboBox();
//        lga_text.setPreferredSize(new Dimension(300, 20));
//        panelForm.add(lga_text, gridBagCons);
//
//
//        row++;
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        panelForm.add(new JLabel("BVN"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        bvn_text = new JTextField(11);
//        panelForm.add(bvn_text, gridBagCons);
//
//
//        gridBagCons.gridx = 0;
//        gridBagCons.gridy = row;
//        gridBagCons.anchor = GridBagConstraints.EAST;
//        panelForm.add(new JLabel("Photo"), gridBagCons);
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        photo = new JButton("Select Picture");
//        panelForm.add(photo, gridBagCons);
//
//
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        //gridBagCons.gridwidth = 2;
//        gridBagCons.anchor = GridBagConstraints.WEST;
//        JLabel photoFileName = new JLabel("no file selected");
//        //photoFileName.setVisible(false);
//        panelForm.add(photoFileName, gridBagCons);
//
//        JPanel pn = new JPanel();
//        BoxLayout buttonLayout = new BoxLayout(pn, BoxLayout.X_AXIS);
//        pn.setLayout(buttonLayout);
//        JButton btnBack = new JButton("Back");
//        btnBack.setActionCommand(ACT_BACK);
//        btnBack.addActionListener(this);
//
//        m_save = new JButton("Save to File");
//        m_save.setActionCommand(ACT_SAVE);
//        m_save.addActionListener(this);
//        m_save.setEnabled(false);
//        //add(m_save);
//
//        m_save2DB = new JButton("Save to DB");
//        m_save2DB.setActionCommand(ACT_SAVE_DB);
//        m_save2DB.addActionListener(this);
//        m_save2DB.setEnabled(false);
//        //add(m_save2DB);
//
//        gridBagCons.gridx = 1;
//        gridBagCons.gridy = row++;
//        pn.setAlignmentX(LEFT_ALIGNMENT);
//
//        if(operation=="add"){
//            pn.add(btnBack);
//            pn.add(m_save);
//            pn.add(m_save2DB);
//
//            panelForm.add(pn, gridBagCons);
//        } else if(operation == "edit"){
//            JButton btnSave = new JButton("Save");
//            JButton btnCancel = new JButton("Cancel");
//            pn.add(btnSave);
//            pn.add(btnCancel);
//            panelForm.add(pn, gridBagCons);
//        } else if(operation =="view"){
//            JButton btnFirst = new JButton("First");
//            JButton btnPrev = new JButton("Previous");
//            JButton btnNext = new JButton("Next");
//            JButton btnLast = new JButton("Last");
//            pn.add(btnFirst);
//            pn.add(btnPrev);
//            pn.add(btnNext);
//            pn.add(btnLast);
//            panelForm.add(pn, gridBagCons);
//        }
//
//
//        comboUtilities.loadGenderCombo(this.gender_text);
//        comboUtilities.loadStates(this.state_text);
//        nationality_text.addItem("Select One");
//        nationality_text.addItem("Nigeria");
//        nationality_text.addItem("Non-Nigeria");
//
//        nationality_text.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e)
//            {
//                String country = nationality_text.getSelectedItem().toString();
//                System.out.println("Event listener fired.." + country);
//                state_text.setVisible(country == "Nigeria");
//                lga_text.setVisible(country == "Nigeria");
//                lblLga.setVisible(country == "Nigeria");
//                lblState.setVisible(country == "Nigeria");
//            }
//        });
//
//        state_text.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e)
//            {
//                lga_text.removeAllItems();
//                comboUtilities.loadLgaCombo(lga_text, (long) state_text.getSelectedIndex());
//            }
//        });
//
//        photo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e)
//            {
//                JFileChooser fc = new JFileChooser();
//                int result = fc.showOpenDialog(null);
//                if (result == JFileChooser.APPROVE_OPTION) {
//                    File file = fc.getSelectedFile();
//                    try {
//                        photoPreview.setIcon(new ImageIcon(ImageIO.read(file)));
//                        photoPreview.setVisible(true);
//                        photoFileName.setText(file.getName());
////						photoFileName.setVisible(true);
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        FingerDB fingerDB = new FingerDB("localhost","uareu","root","");
//
//        FingerDB.Record record = null;
//        try {
//            fingerDB.Open();
//            record = fingerDB.GetSingleFPData(uniquId);
////			record = fingerDB.GetSingleFPData("Success");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        this.username_text.setText(record.surname);
//        this.othernames_text.setText(record.othernames);
//        this.gender_text.removeAllItems();
//        this.gender_text.addItem(record.gender);
//        this.address_text.setText(record.address);
//        this.nationality_text.addItem(record.nationality_id);
//        this.state_text.addItem(record.state_id);
//        this.lga_text.addItem(record.lga_id);
//        this.bvn_text.setText(record.bvn);
//        this.photo.setText(record.photo);
//
//        return panelForm;
//    }
//
//}
