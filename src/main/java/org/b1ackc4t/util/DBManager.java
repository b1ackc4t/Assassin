package org.b1ackc4t.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    public static DBManager db;

    static {
        try {
            db = new DBManager("webshelldb.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String dbFilePath;
    private final String[] payloadNames = new String[]{"Print", "Download", "Rce", "Upload"};

    private DBManager(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        connection = getConnection();
        this.creatTable();
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void addWebshell(String url, String pass, String type, String method,String reqEncode, String resEncode, String note) throws ClassNotFoundException, SQLException {
        Integer ID = getEndID();
        connection = getConnection();
        String sql = "INSERT INTO ShellData (ID,URL,pass,type,method,reqencode,resencode,status,note) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,ID+1);
        preparedStatement.setString(2,url);
        preparedStatement.setString(3,pass);
        preparedStatement.setString(4,type);
        preparedStatement.setString(5,method);
        preparedStatement.setString(6,reqEncode);
        preparedStatement.setString(7,resEncode);
        preparedStatement.setInt(8,0);
        preparedStatement.setString(9,note);
        preparedStatement.addBatch();
        preparedStatement.executeBatch();
        preparedStatement.close();
        for (String payloadName : payloadNames) {
            String sql2 = "INSERT INTO ClassLoadInfo (ID, URL, payloadname, classname, status) " +
                    "VALUES (?, ?, ?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql2);
            preparedStatement.setInt(1, ID+1);
            preparedStatement.setString(2, url);
            preparedStatement.setString(3, payloadName);
            preparedStatement.setString(4, "");
            preparedStatement.setInt(5, 0);
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
        destroyed();
    }


    public int getEndID() {
        connection = getConnection();
        int rowCount = 0;
        try {
            statement = connection.createStatement();
            String sql = "SELECT Count(*) FROM ShellData";
            resultSet = statement.executeQuery(sql);
            rowCount = 0;
            while(resultSet.next()) {
                rowCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            destroyed();
        }
        return rowCount;
    }

    public void showAll()throws ClassNotFoundException, SQLException {
        connection = getConnection();
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select * from ShellData");//查询此表的数据
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        List<List<String>> result = new ArrayList<>();
        int[] len = new int[count - 1];
        List<String> head = new ArrayList<>();
        for (int i = 1; i <= count - 1; ++i) {
            head.add(meta.getColumnName(i));
        }
        result.add(head);
        while (resultSet.next()) {//获取此表数据
            List<String> row = new ArrayList<>();
            for (int i = 1; i <= count - 1; ++i) {
                row.add(resultSet.getString(i));
                if (resultSet.getString(i).length() > len[i - 1]) len[i - 1] = resultSet.getString(i).length();
            }
            result.add(row);
        }
        String[] formats = new String[]{"|%-"+Math.max(2, len[0])+"s", "|%-"+Math.max(25, len[1]+1)+"s", "|%-"+Math.max(10, len[2]+1)+"s", "|%-"+Math.max(4, len[3]+1)+"s", "|%-"+Math.max(8, len[4]+1)+"s", "|%-"+Math.max(9, len[5]+1)+"s", "|%-"+Math.max(9, len[6]+1)+"s", "|%-"+Math.max(6, len[7]+1)+"s|"};
        for (List<String> row : result) {
            for (int i = 0; i < formats.length; ++i) {
                System.out.printf(formats[i], row.get(i));
            }
            System.out.println();
        }
        destroyed();
    }

    public boolean updateWebshell(Integer ID)throws SQLException{
        connection = getConnection();
        String sql = "update ShellData set ID = ID-1 where ID >?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,ID);
        preparedStatement.executeUpdate();
        destroyed();
        return false;
    }

    public void deleteWebShell(Integer ID)throws ClassNotFoundException, SQLException{
        connection = getConnection();
        String delsql = "DELETE from ShellData where ID=?";
        String sql = "update ShellData set ID = ID-1 where ID >?";
        preparedStatement = connection.prepareStatement(delsql);
        preparedStatement.setInt(1,ID);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,ID);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        String delsql2 = "DELETE from ClassLoadInfo where ID=?";
        String sql2 = "update ClassLoadInfo set ID = ID-1 where ID >?";
        preparedStatement = connection.prepareStatement(delsql2);
        preparedStatement.setInt(1,ID);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        preparedStatement = connection.prepareStatement(sql2);
        preparedStatement.setInt(1,ID);
        preparedStatement.executeUpdate();
        destroyed();
    }


    public void setClassLoadStatus(String URL, String payloadName, String className) throws SQLException {
        connection = getConnection();
        String sql = "update ClassLoadInfo set classname=?, status=1 where URL=? and payloadname=?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, className);
        preparedStatement.setString(2, URL);
        preparedStatement.setString(3, payloadName);
        preparedStatement.executeUpdate();
        destroyed();

    }

    public int getClassLoadStatus(String URL, String payloadName) {
        connection = getConnection();
        preparedStatement = null;
        String sql = "select status from ClassLoadInfo where URL=? and payloadname=?";
        try {
            int t = 0;
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,URL);
            preparedStatement.setString(2,payloadName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                t = resultSet.getInt("status");
            }
            return t;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            destroyed();
        }
        return 0;
    }

    public String getClassLoadName(String URL, String payloadName) throws SQLException {
        connection = getConnection();
        String sql = "select classname from ClassLoadInfo where URL=? and payloadname=?";
        String clsName = null;
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,URL);
        preparedStatement.setString(2,payloadName);
        resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            clsName = resultSet.getString("classname");
        }
        destroyed();
        return clsName;
    }

    private void creatTable() throws SQLException {
        connection = getConnection();
        statement = connection.createStatement();
        String sql1 = "CREATE TABLE IF NOT EXISTS ShellData" +
                "(ID INT NOT NULL," +
                " URL            TEXT   PRIMARY KEY        NOT NULL, " +
                " pass           TEXT     NOT NULL, " +
                " type           TEXT     NOT NULL, " +
                " method       TEXT     NOT NULL, " +
                " reqencode      TEXT     NOT NULL, " +
                " resencode      TEXT     NOT NULL, " +
                " status         TEXT  NOT NULL, " +
                " note           TEXT);";

        String sql2 = "CREATE TABLE IF NOT EXISTS ClassLoadInfo" +
                "(ID INT NOT NULL," +
                " URL            TEXT     NOT NULL, " +
                " payloadname    TEXT     NOT NULL, " +
                " classname      TEXT     NOT NULL, " +
                " status         INT     NOT NULL" +
                ");";
        statement.executeUpdate(sql1);
        statement.executeUpdate(sql2);
        destroyed();
    }

    public void resetClassInfo(int ID) {
        connection = getConnection();
        String sql = "update ClassLoadInfo set status=0 where ID=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, ID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            destroyed();
        }

    }

    public String[] getShellInfo(int ID){
        connection = getConnection();
        preparedStatement = null;
        resultSet = null;
        try{
            String sql = "select * from ShellData where id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,ID);
            resultSet = preparedStatement.executeQuery();
            String[] info = new String[6];
            while (resultSet.next()) {
                info[0] = resultSet.getString("url");
                info[1] = resultSet.getString("pass");
                info[2] = resultSet.getString("type");
                info[3] = resultSet.getString("method");
                info[4] = resultSet.getString("reqencode");
                info[5] = resultSet.getString("resencode");
            }
            return info;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
                destroyed();
        }
        return null;
    }

    public boolean changeShell(Integer ID, String key, String value){
        connection = getConnection();
        try {
            String sql = "update ShellData set " + key+ " = ? where id = ?";
            preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1,key);
            preparedStatement.setString(1,value);
            preparedStatement.setInt(2,ID);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            destroyed();
        }

    }

    public boolean isExist(String url) {
        connection = getConnection();
        try {
            String sql = "select ID from ShellData where URL = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, url);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            destroyed();
        }
        return false;
    }


    /**
     * 数据库资源关闭和释放
     */
    public void destroyed() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != preparedStatement) {
                preparedStatement.close();
                preparedStatement = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
