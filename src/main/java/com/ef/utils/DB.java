package com.ef.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kudoji
 */
public class DB {
    private Connection dbConnection = null;
    private String url, login, password, db;
    private String dbUrl = null;
    private boolean debug = false;
    
    public DB(String url, String login, String password, String db) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.db = db;

        this.dbUrl = "jdbc:mysql://" + this.url + "/" + this.db + "?useSSL=false";
    }
    
    public Connection getConnection(){
        return this.dbConnection;
    }
    
    public void setDebugMode(boolean _debug){
        this.debug = _debug;
    }
    
    public boolean connect(){
        boolean result = false;
        try{
//            Class.forName("com.mysql.jdbc.Driver");

            dbConnection = DriverManager.getConnection(this.dbUrl, this.login, this.password);
            if (debug){
                System.out.println("mysql connected");
            }
            result = true;
        }catch (Exception e){
            if (debug){
                System.out.println(e.getMessage());
            }
            result = false;
        }
        
        return result;
    }

    public boolean close(){
        boolean result = true;
        
        try{
            if (dbConnection != null){
                dbConnection.close();
            }
        }catch (SQLException ex){
            result = false;
            if (debug){
                System.out.println(ex.getMessage());
            }
        }
        
        return result;
    }

    @Override
    protected void finalize() throws Throwable{
        if (this.dbConnection != null){
            this.dbConnection.close();
        }
        this.dbConnection = null;
        
        super.finalize();
    }
    
    /**
     * converts ResultSet to Dictionary
     * 
     * @param _rs 
     */
    private HashMap<String, String> convertRS2Dic(ResultSet _rs) throws SQLException{
        HashMap<String, String> result = new HashMap<>();
        
        ResultSetMetaData rsmd = _rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        
        for (int i = 1; i <= numColumns; i++){ //strange to face indexes start from 1
            String col_name = rsmd.getColumnName(i);
            
            switch (rsmd.getColumnType(i)) {
                case java.sql.Types.INTEGER:
                    result.put(col_name, Integer.toString(_rs.getInt(i)));
                    break;
                case java.sql.Types.VARCHAR:
                    if (_rs.getString(i) == null){ //can return null if value in DB is null
                        result.put(col_name, "");
                    }else{
                        result.put(col_name, _rs.getString(i));
                    }
                    break;
                case java.sql.Types.REAL:
                    result.put(col_name, Float.toString(_rs.getFloat(i)));
                    break;
                case java.sql.Types.BLOB:
                    result.put(col_name, new String(_rs.getBlob(i).getBytes(1l, (int) _rs.getBlob(i).length())) );
                    break;
                case java.sql.Types.NUMERIC:
                    if (_rs.getBigDecimal(i) == null){
                        result.put(col_name, "");
                    }else{
                        result.put(col_name, _rs.getBigDecimal(i).toString());
                    }
                    break;
                default:
                    System.out.println(rsmd.getColumnType(i));
                    break;
            }
        }
        
        return result;
    }
    
    /**
     * Inserts or updates data into DB
     *
     * @param _insert
     * @param _parameters
     * - "table" - table name working with
     * - "id" -
     *      if !_insert (update) than update this id only;
     *      if _insert than insert this id as well.
     * - "set" - used with update only
     * - "where" - used with update only
     * - "order" - used with update only
     * 
     * @return 0 in case of error; if update, returns 1, if insert, returns generated id
     */
    public int updateData(boolean _insert, HashMap<String, String> _parameters){
        int insertedId = 0;
        String sqlText = "";
        Object param = _parameters.get("table");
        if (param == null){ //table is not set
            System.err.println(this.getClass().getName() + ": " + "table is not set");
            return 0;
        }

        if (_insert){ //insert into statement
            sqlText = "insert into " + param.toString() + " ";
        }else{ //update statement
            sqlText = "update " + param.toString() + " set ";
        }
        _parameters.remove("table");
        
        Object paramID = _parameters.get("id");
        Object paramWhere = _parameters.get("where");
        //  id or where condition must be set for update statement
        if ( (paramWhere == null) && (paramID == null) && (!_insert) ){
            System.err.println(this.getClass().getName() + ": " + "id field or where condition is not set");
            return 0;
        }
        if (!_insert){
            //  keep id field for insert statement
            _parameters.remove("id");
        }
        
        _parameters.remove("where");
        
        Object paramOrder = _parameters.get("order");
        if ( (paramOrder != null) && (_insert) ){
            System.err.println(this.getClass().getName() + ": " + "order by cannot be used with insert");
            return 0;
        }
        _parameters.remove("order");

        if (_parameters.isEmpty()){ //no fields to update/insert
            System.err.println(this.getClass().getName() + ": " + "no fields are set for update/insert");
            return 0;
        }
        
        Object paramSet = _parameters.get("set");
        if ( (paramSet != null) && (!_insert) ){
            sqlText = sqlText + "" + paramSet.toString(); //keyword set is already injected earlier
        }else{
            String sqlValues = " values (";
            if (_insert){
                sqlText = sqlText + " (";
            }

            for (Map.Entry<String, String> parameter : _parameters.entrySet()) {
                String value = parameter.getValue();
                if (!_insert){
                    sqlText = sqlText + parameter.getKey() + " = ";
                    if (value == null){ //null value must be null but not "null"
                        sqlText += value;
                    }else{
                        sqlText += "'" + value + "'";
                    }
                    sqlText += ", ";
                }else{
                    sqlText = sqlText + parameter.getKey() + ", ";
                    if (value == null){ //null value must be null but not "null"
                        sqlValues += value;
                    }else{
                        sqlValues += "'" + value + "'";
                    }
                    sqlValues += ", ";
                }
            }
            //remove last comma with space
            sqlText = sqlText.substring(0, sqlText.length() - 2 );
            if (_insert){
                sqlValues = sqlValues.substring(0, sqlValues.length() - 2 );

                sqlText = sqlText + ")" + sqlValues + ")";
            }
        }
        
        if (!_insert){ //must specify which row to update
            if (paramID != null){
                sqlText = sqlText + " where id = " + paramID.toString();
            }else if (paramWhere != null){
                sqlText = sqlText + " where " + paramWhere.toString();
            }
            
            if (paramOrder != null){
                sqlText += "order by " + paramOrder.toString();
            }
        }
        
        sqlText = sqlText + ";";

        try{
            try (PreparedStatement st = this.dbConnection.prepareStatement(sqlText, Statement.RETURN_GENERATED_KEYS)) {
                st.executeUpdate();
                
                if (_insert){ //get the inserted id
                    ResultSet rs = st.getGeneratedKeys();

                    if (rs.next()){
                        insertedId = rs.getInt(1);
                    }
                }
            }
        }catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 0;
        }
        
        if (!_insert){ //in case of update return 1 otherwise - inserted id
            insertedId = 1;
        }
        
        return insertedId;
    }
    
    /**
     * Executes raw sql sentence
     * @param _sqlText sql sentence
     * @return 
     */
    public boolean execSQL(String _sqlText){
        try
        (
            Statement statement = this.dbConnection.createStatement();
        ){
            statement.execute(_sqlText);
        }catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    public boolean deleteData(HashMap<String, String> _params){
        String sqlText = "delete from ";
        Object param = _params.get("table");
        if (param == null){ //table is not set
            System.err.println(this.getClass().getName() + ": " + "table is not set");
            return false;
        }
        sqlText += param.toString();
        
        param = _params.get("where");
        if (param == null){ //table is not set
            System.err.println(this.getClass().getName() + ": " + "where condition is not set");
            return false;
        }
        
        sqlText += " where " + param.toString() + ";";
        
        try
        (
            Statement statement = this.dbConnection.createStatement();
        ){
            statement.execute(sqlText);
        }catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Selects data from a table
     * 
     * @param _parameters can have these keys:
     * - "table" - table name to select data from;
     * - "id"(optional) - in case of getting one row only;
     * - "where" (optional) - in case of using where condition;
     * - "order"(optional) - in case of sorting data.
     * - "limit"(optional) - in case of using limit
     * 
     * "id" and "where" cannot be used together
     * 
     * @return Array of HashMap where each HashMap is a table row 
     */
    public ArrayList<HashMap<String, String>> selectData(HashMap<String, String> _parameters){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        
        String sqlText = "select * from ";
        Object param = _parameters.get("table");
        if (param == null){
            System.err.println(this.getClass().getName() + ": " + " table is not set");
        }else{
            sqlText = sqlText + param.toString();
        }
        
        param = _parameters.get("id");
        if (param != null){ //find a particular row
            sqlText = sqlText + " where id = " + param.toString();
        }
        
        param = _parameters.get("where");
        if (param != null){
            sqlText = sqlText + " where " + param.toString();
        }
        
        param = _parameters.get("order");
        if (param != null){ //add order by
            sqlText = sqlText + " order by " + param.toString();
        }
        
        param = _parameters.get("limit");
        if (param != null){ //add order by
            sqlText = sqlText + " limit " + param.toString();
        }
        
        sqlText = sqlText + ";";

        try
        (
            Statement st = this.dbConnection.createStatement();
            ResultSet rs = st.executeQuery(sqlText);
        )
        {
            while (rs.next()){
                HashMap<String, String> row = convertRS2Dic(rs);

                result.add(row);
            }
        }catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        
        return result;
    }
    
    public boolean startTransaction(){
        boolean result = true;
        try{
            this.dbConnection.setAutoCommit(false);
        }catch (SQLException e){
            result = false;
            System.err.println(e.getClass() + ": " + e.getMessage() + " (couldn't start transaction)");
        }
        
        return result;
    }
    
    public boolean rollbackTransaction(){
        boolean result = true;
        try{
            this.dbConnection.rollback();
        }catch (SQLException e){
            result = false;
            System.err.println(e.getClass() + ": " + e.getMessage() + " (couldn't start transaction)");
        }
        
        return result;
    }
    
    public boolean commitTransaction(){
        boolean result = true;
        try{
            this.dbConnection.commit();
        }catch (SQLException e){
            result = false;
            System.err.println(e.getClass() + ": " + e.getMessage() + " (couldn't start transaction)");
        }
        
        return result;
    }
}
