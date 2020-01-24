import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FingerDB {

	private static final String usersTable = "users";
	private static final String userColumn = "userID";
	private static final String surnameColumn = "surname";
	private static final String othernamesColumn = "othernames";
	private static final String bvnColumn = "bvn";
	private static final String genderColumn = "gender";
	private static final String addressColumn = "address";
	private static final String print1Column = "print1";
	private static final String idColumn = "id";

	private static final String nameColumn = "name";
	private static final String nationalityColumn = "nationality_id";
	private static final String stateColumn = "state_id";
	private static final String lgaColumn = "lga_id";
	private static final String photoColumn = "photo";

	private static final String stateIdColumn = "state_id";


	private static final String print2Column = "print2";

	private String URL = "jdbc:mysql://localhost:3306/";
	private String host;
	private String database;
	private String userName;
	private String pwd;
	private java.sql.Connection connection = null;
	private String preppedStmtInsert = null;
	private String preppedStmtUpdate = null;

	public class Record {
		Long id;
		String surname;
		String othernames;
		String gender;
		String address;
		int nationality_id;
		int state_id;
		int lga_id;
		String bvn;
		String photo;
		byte[] fmdBinary;
		byte[] fmdBinary2;

		Record(Long recId, String sname, String onames, String sex, String addr, Integer nationalityId, Integer stateId, Integer lgaId, String bvno, String pic, byte[] fmd, byte[] fmd2) {
			id = recId;
			surname = sname;
			othernames = onames;
			gender = sex;
			address = addr;
			nationality_id=nationalityId;
			state_id = stateId;
			lga_id = lgaId;
			bvn = bvno;
			photo = pic;
			fmdBinary = fmd;
			fmdBinary2 = fmd2;
		}
	}

	public class State{
		Long id;
		String name;
		Timestamp created_at;
		Timestamp updated_at;
		State(String state){
			name = state;
		}
		State(Long recId, String state){
			id = recId;
			name = state;
		}
	}

	public class Lga{
		Long id;
		Long state_id;
		String name;
		Timestamp created_at;
		Timestamp updated_at;
		Lga(Long stateNo, String lgaName){
			state_id = stateNo;
			name = lgaName;
		}

		Lga(Long recId, Long stateNo, String lgaName){
			id = recId;
			state_id = stateNo;
			name = lgaName;
		}
	}

	public FingerDB(String _host, String db, String user, String password) {
		database = db;
		userName = user;
		pwd = password;
		host = _host;

		URL = "jdbc:mysql://" + host + ":3306/";
		preppedStmtInsert = "INSERT INTO " + usersTable + "(" + userColumn + "," + surnameColumn + "," + othernamesColumn + "," + genderColumn + ","
				+ addressColumn + "," + nationalityColumn + "," + stateColumn + "," + lgaColumn + "," + bvnColumn + "," + photoColumn + ","
				+ print1Column + ","	+ print2Column + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	}

	public FingerDB(String _host, String db, String user, String password, String table, String fld) {
		database = db;
		userName = user;
		pwd = password;
		host = _host;

		URL = "jdbc:mysql://" + host + ":3306/";
		preppedStmtInsert = "INSERT INTO " + table + "(" + fld + ") VALUES(?,?)";
	}

	@Override
	public void finalize() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Open() throws SQLException {
		connection = DriverManager.getConnection(URL + database, userName, pwd);
	}

	public void Close() throws SQLException {
		connection.close();
	}

	public boolean UserExists(String userID) throws SQLException {
		String sqlStmt = "Select " + idColumn + " from " + usersTable
				+ " WHERE " + userColumn + "='" + userID + "'";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		return rs.next();
	}
//
//	public void Insert(String userID, byte[] print1) throws SQLException {
//		java.sql.PreparedStatement pst = connection
//				.prepareStatement(preppedStmtInsert);
//		pst.setString(1, userID);
//		pst.setBytes(2, print1);
//		pst.execute();
//	}

	public void Insert(String surname, String othernames, String gender,
					   String address, String nationality_id, String state_id, String lga_id, String bvn,
					   String photo, byte[] print1, byte[] print2) throws SQLException {
		java.sql.PreparedStatement pst = connection
				.prepareStatement(preppedStmtInsert);
		//pst.setString(1, userID);
		pst.setString(2,surname);
		pst.setString(3,othernames);
		pst.setString(4,gender);
		pst.setString(5,address);
		pst.setInt(6, Integer.parseInt(nationality_id));
		pst.setInt(7, Integer.parseInt(state_id));
		pst.setInt(8, Integer.parseInt(lga_id));
		pst.setString(9,bvn);
		pst.setString(10,photo);
		pst.setBytes(11, print1);
		pst.setBytes(12, print2);
		pst.execute();
	}

	public void Update(int id, String surname, String othernames, String gender,
					   String address, String nationality_id, String state_id, String lga_id, String bvn,
					   String photo) throws SQLException {
		preppedStmtUpdate = "UPDATE " + usersTable + " SET " + surnameColumn + "='" + surname + "', " + othernamesColumn + "='" + othernames + "', " + genderColumn + "='"
				+ gender + "', " + addressColumn + "='" + address + "', " + nationalityColumn + "=" + Integer.valueOf(nationality_id)  + ", " + stateColumn + "=" + Integer.valueOf(state_id)  + " , " + lgaColumn +
				"=" + Integer.valueOf(lga_id)  + ", " + bvnColumn + "='" + bvn + "', " + photoColumn + "='" + photo + "' WHERE id=" + id;

		java.sql.PreparedStatement pst = connection
				.prepareStatement(preppedStmtUpdate);

		pst.execute();
		Miscellanouse.msgbox("Record updated","Successful");
	}

	public List<Record> GetAllFPData() throws SQLException {
		List<Record> listUsers = new ArrayList<Record>();
		String sqlStmt = "Select * from " + usersTable;
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		while (rs.next()) {
			if (rs.getBytes(print1Column) != null)
				listUsers.add(new Record(rs.getLong(idColumn), rs.getString(surnameColumn),
						rs.getString(othernamesColumn), rs.getString(genderColumn), rs.getString(addressColumn), rs.getInt(nationalityColumn), rs.getInt(stateColumn),
						rs.getInt(lgaColumn), rs.getString(bvnColumn), rs.getString(photoColumn), rs.getBytes(print1Column), rs.getBytes(print2Column)));
		}
		return listUsers;
	}

	public Record GetSingleFPData(Long id) throws SQLException {
		Record singleUser = null;
		String sqlStmt = "Select * from " + usersTable + " WHERE " + idColumn + "='" + id + "'";
		System.out.println("Sql: " + "Select * from " + usersTable + " WHERE " + idColumn + "='" + id + "'");
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		//System.out.println(rs.);
		rs.first();
		if(rs.first()){
			singleUser = new Record(rs.getLong(idColumn), rs.getString(surnameColumn),
					rs.getString(othernamesColumn), rs.getString(genderColumn), rs.getString(addressColumn), rs.getInt(nationalityColumn), rs.getInt(stateColumn),
					rs.getInt(lgaColumn), rs.getString(bvnColumn), rs.getString(photoColumn), rs.getBytes(print1Column), rs.getBytes(print2Column));
		}

		return singleUser;
	}

	public List<State> GetAllStates() throws SQLException {
		List<State> listStates = new ArrayList<State>();
		String sqlStmt = "Select * from states";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		while (rs.next()) {
			if (rs.getBytes(nameColumn) != null)
				listStates.add(new State(rs.getLong(idColumn), rs.getString(nameColumn)));
		}
		return listStates;
	}

	public State GetSingleState(Long searchId) throws SQLException {
		State aState = null;
		String sqlStmt = "Select * from states WHERE id=" + searchId;
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		if(rs.first()){
			rs.first();
			aState = new State(rs.getLong(idColumn),rs.getString(nameColumn));
		}
		return aState;
	}

	public List<Lga> GetStateLgas(Long state) throws SQLException {
		List<Lga> listLgas = new ArrayList<Lga>();
		String sqlStmt = "Select * from lgas WHERE state_id=" + state;
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		while (rs.next()) {
			if (rs.getBytes(nameColumn) != null)
				listLgas.add(new Lga(rs.getLong(idColumn),rs.getLong(stateIdColumn), rs.getString(nameColumn)));
		}
		return listLgas;
	}

	public Lga GetSingleLga(Long searchId) throws SQLException {
		Lga aLga = null;
		String sqlStmt = "Select * from lgas WHERE id=" + searchId;
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sqlStmt);
		if(rs.first()){
			rs.first();
			aLga = new Lga(rs.getLong(idColumn), rs.getLong(stateIdColumn),rs.getString(nameColumn));
		}
		return aLga;
	}

	public String GetConnectionString() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return URL + " User: " + this.userName;
	}

	public String GetExpectedTableSchema() {
		return "Table: " + usersTable + " LONG(20): " + idColumn + " PK(VARCHAR(50)): " + surnameColumn + " PK(VARCHAR(50)): "
				+ othernamesColumn + " PK(VARCHAR(6)): " + genderColumn  + " PK(VARCHAR(255)): " + addressColumn + " INT(11): " + nationalityColumn
				+ " INT(11): " + stateColumn + " INT(11): " + lgaColumn + " PK(VARCHAR(11)): " + bvnColumn + " PK(VARCHAR(200)): " + photoColumn
				+ " VARBINARY(4000): " + print1Column + "VARBINARY(4000): " + print2Column;
	}

	public void deleteUser(int userId) throws SQLException {
		String sqlStmt = "DELETE from " + usersTable + " WHERE id=" + userId;

		java.sql.PreparedStatement pst = connection
				.prepareStatement(sqlStmt);
		pst.execute();

//		connection.prepareStatement(sqlStmt);
//		Statement st = connection.createStatement();
//		ResultSet rs = st.executeQuery(sqlStmt);
		Miscellanouse.msgbox("Record Deleted", "Successful");
		return;
	}
}
