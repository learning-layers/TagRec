package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import file.preprocessing.BibBookmark;

public class DBManager {
	private String dbName;
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	public DBManager(String dbname) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			dbName = dbname;
			connect = DriverManager.getConnection("jdbc:mysql://localhost/"
					+ dbname + "?user=root");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<BibBookmark> getBibBookmarks(String tableName, String idFieldName, String urlFieldName) {
		List<BibBookmark> bookmarks = new ArrayList<BibBookmark>();

		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery("select * from " + dbName + "." + tableName);
			while (resultSet.next()) {
				BibBookmark b = new BibBookmark();
				b.id = resultSet.getString(idFieldName);
				b.urlHash = resultSet.getString(urlFieldName);
				bookmarks.add(b);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return bookmarks;
	}

	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}
}
