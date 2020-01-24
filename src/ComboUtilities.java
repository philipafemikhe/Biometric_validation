
import com.sun.media.ui.MessageBox;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ComboUtilities {
    //public FingerDB db = new FingerDB("localhost", "uareu", "root", "");
    public ComboUtilities(){

    }

    public void loadGenderCombo(JComboBox combo){
        combo.addItem(new ComboItem("Select One", "Select One"));
        combo.addItem(new ComboItem("Male", "Male"));
        combo.addItem(new ComboItem("Female", "Female"));
    }

    public void loadStates(JComboBox combo){
            FingerDB db = new FingerDB("localhost", "uareu", "root", "","states","nameColumn");
            List<FingerDB.State> m_listOfStates = new ArrayList<FingerDB.State>();


            try {
                db.Open();
                m_listOfStates = db.GetAllStates();
                Object[][] data = new Object[m_listOfStates.size()][3];
                combo.addItem(new ComboItem("Select One", "-1") );
                for (FingerDB.State state : m_listOfStates) {
                    combo.addItem(new ComboItem(state.name, state.id.toString()));
                   //combo.addItem(state.name);
                }
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
//                MessageBox.getOwnerlessWindows (
//                                "Failed to load FMDs from database.  Please check connection string in code.");
                return;
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                JOptionPane
                        .showMessageDialog(null, e1);
                return;
            }
            return;


    }

    public void loadLgaCombo(JComboBox combo, Long state){

        combo.addItem(new ComboItem("Select One", "-1"));

        FingerDB db = new FingerDB("localhost", "uareu", "root", "","states","nameColumn");
        List<FingerDB.Lga> m_listOfLga = new ArrayList<FingerDB.Lga>();


        try {
            db.Open();
            m_listOfLga = db.GetStateLgas(state);
            for (FingerDB.Lga lga : m_listOfLga) {
                combo.addItem(new ComboItem(lga.name, lga.id.toString()) );
            }
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
//                MessageBox.getOwnerlessWindows (
//                                "Failed to load FMDs from database.  Please check connection string in code.");
            return;
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            JOptionPane
                    .showMessageDialog(null, e1);
            return;
        }
    }
}
