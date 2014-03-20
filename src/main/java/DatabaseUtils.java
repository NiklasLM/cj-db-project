package main.java;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUtils {
	
	public static Connection getConnection(String subprotocol, String subname, String user, String password) {
		Connection conn = null;
		String conDetails = "jdbc:" + subprotocol + ":" + subname + "?user=" + user + "&password=" + password;
		try {
		    conn =
		       DriverManager.getConnection(conDetails);
		} catch(Exception e) {
			
		}
		return conn;
	}
	
	public static String getExport(String tableName, String subprotocol, String subname, String user, String password) {
		Connection con = getConnection(subprotocol, subname, user, password	);
		
		String export = null;
		export = exportDatabaseTable(tableName, con);
		export += exportTableData(tableName, con);
		
		return export;
	}

	public static String exportDatabaseTable(String tableName, Connection con){
		String sqlString = null;
		Map<String, ArrayList<String>> primaryKeys = new HashMap<String, ArrayList<String>>();
		
		try {
			
			DatabaseMetaData dbm = con.getMetaData();
			ResultSet columns = dbm.getColumns(con.getCatalog(), null, tableName, null);
			ResultSet pks = dbm.getPrimaryKeys(con.getCatalog(), null, tableName);
			
			while (pks.next()){
				ArrayList<String> keys;
				if(primaryKeys.get(tableName) !=null) {
					keys = primaryKeys.get(tableName);
				}
			    else  {
			    	keys = new ArrayList<String>();
			    }
				keys.add(pks.getString("COLUMN_NAME"));
				primaryKeys.put(tableName, keys);
			}
			
			// DROP TABLE
			sqlString = "/* Tabelle loeschen */\n";
			sqlString += "DROP TABLE " + tableName +";\n";
			sqlString += "\n";
			
			// CREATE TABLE
			sqlString += "/* Tabelle " + tableName + " neu erzeugen */\n";
			sqlString += "CREATE TABLE " + tableName + "\n";
			sqlString += "( \n";
			
			while(columns.next()) {
				sqlString += "   " + columns.getString("COLUMN_NAME") + "\t" + columns.getString("TYPE_NAME") + "(" + columns.getString("COLUMN_SIZE");
					
				//Precision angeben wenn bestimmter Datentyp.
					switch(columns.getInt("DATA_TYPE")) {
						case java.sql.Types.DECIMAL:
						case java.sql.Types.DOUBLE:
						case java.sql.Types.FLOAT:
						case java.sql.Types.NUMERIC:
						case java.sql.Types.REAL:
							sqlString += "." + columns.getString("DECIMAL_DIGITS");
							break;
					}
				sqlString += ") ";
					
					//Prüfen ob Wer ein Primary Key ist und "primary key" anängen wenn nur ein pk existiert.
					if(primaryKeys.get(tableName) != null) {
						if(primaryKeys.get(tableName).size() < 2) {
							String pk = primaryKeys.get(tableName).get(0);
							if(pk.equals(columns.getString("COLUMN_NAME"))) {
								sqlString += " primary key";
							}
						}
					}
					if(!columns.isLast()) {
						sqlString += ",";
					} else if(primaryKeys.get(tableName) != null) {
						if(primaryKeys.get(tableName).size() >= 2) sqlString += ", ";
					}
				sqlString += "\n";
			}
			
			if(primaryKeys.get(tableName) != null)
				
			if(primaryKeys.get(tableName).size() >= 2) {
				ArrayList<String> pks1 = primaryKeys.get(tableName);
				sqlString += "   primary key("; 
				for(int i = 0; i < pks1.size(); i++) {
					sqlString += pks1.get(i);
					if(i < pks1.size()-1) sqlString += ", ";
				}
				sqlString += ")" + "\n";
			}
			sqlString += ");\n\n";
			
		} catch (SQLException e) {
			sqlString = e.getMessage();
		}
		
		return sqlString;
	}
	
	public static String exportTableData(String tableName, Connection con){
		
		String sqlString = null;
		
		try {
			DatabaseMetaData dbm = con.getMetaData();
			String[] types = {"TABLE"};
			ResultSet tables = dbm.getTables(null, null, null, types);
			
			while(tables.next()){
				
				Statement stmt = con.createStatement();
				ResultSet rs = null;
	            ResultSetMetaData metaData = null;
				int count;
				
				sqlString = "/* Tabelle " + tableName + " loeschen */\n";
				sqlString += "DELETE FROM " + tableName + ";\n\n";
				
				rs = stmt.executeQuery("SELECT * FROM " + tableName);
				metaData = rs.getMetaData();
				count = metaData.getColumnCount();
				
				sqlString += "/* Tabelle " + tableName + " fuellen */\n";
				
				int z = 0;
				while(rs.next())
				{
					sqlString += "INSERT INTO " + tableName + "\n";
					sqlString += "\tVALUES( ";
					for(int i = 1; i <= count; i++)
					{
						sqlString += "'" + rs.getString(i) + "'";
						if(i != count) sqlString += ",  ";
					}
					sqlString += ");\n";
					z++;
				}
				
				if(z == 0){
					sqlString += "/* Tabelle " + tableName + " enthaelt keine Daten! */\n";
				}
				sqlString += "\n";
			}
			
			sqlString += "\n\n";
			
		} catch (SQLException e) {
			sqlString = e.getMessage();
		}
		
		return sqlString;
	}
}
