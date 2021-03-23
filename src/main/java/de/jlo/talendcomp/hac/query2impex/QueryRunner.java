package de.jlo.talendcomp.hac.query2impex;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryRunner {
	
	private TypeUtil typeUtil = new TypeUtil();
	private Connection conn = null;
	private String query = null;
	private ResultSet resultset = null;
	private Statement stat = null;
	private ResultSetMetaData rsmd = null;
	private List<CSVColumnConfig> listCSVColumns = new ArrayList<>();
	private List<SeparatedColumnConfig> listConfiguredColumns = new ArrayList<>();
	private Map<String, Object> oneResult = new HashMap<>();
	private StringBuilder csvLine = new StringBuilder();
	private String datePattern = "yyyy-MM-dd";
	private SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
	private int fetchSize = 10000;
	
	public void addSeparatedColumn(String name, Boolean excluded) {
		SeparatedColumnConfig sc = new SeparatedColumnConfig();
		sc.setColumnName(name);
		sc.setExcludeFromCSV(excluded);
		listConfiguredColumns.add(sc);
	}
	
	private boolean excludeColumnFromCSV(String column) {
		for (SeparatedColumnConfig sc : listConfiguredColumns) {
			if (sc.isExcludeFromCSV()) {
				if (column.equalsIgnoreCase(sc.getColumnName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void addCSVColumnConfig(String column, int position, String type) {
		CSVColumnConfig c = new CSVColumnConfig();
		c.setColumnName(column);
		c.setPosition(position);
		c.setDBType(type);
		listCSVColumns.add(c);
	}
	
	public void executeQuery(String query) throws Exception {
		this.query = query;
		if (query == null || query.trim().isEmpty()) {
			throw new IllegalStateException("No  given!");
		}
		if (conn == null) {
			throw new IllegalStateException("No connection given!");
		}
		if (conn.isClosed()) {
			throw new IllegalStateException("Connection is closed already");
		}
		conn.setAutoCommit(true);
		stat = conn.createStatement();
		stat.setFetchSize(fetchSize);
		resultset = null;
		try {
			resultset = stat.executeQuery(query);
			resultset.setFetchSize(fetchSize);
		} catch (Exception e) {
			throw new Exception("Execute query failed: " + e.getMessage() + " query: \n" + query, e);
		}
		rsmd = resultset.getMetaData();
		for (int i = 1, n = rsmd.getColumnCount(); i <= n; i++) {
			String rsColumn = rsmd.getColumnLabel(i);
			if (rsColumn == null) {
				rsColumn = rsmd.getColumnName(i);
			}
			String typeName = rsmd.getColumnTypeName(i);
			if (excludeColumnFromCSV(rsColumn) == false) {
				addCSVColumnConfig(rsColumn, i, typeName);
			}
		}
	}
	
	private void extractValuesForCSV(ResultSet rs) throws Exception {
		csvLine.setLength(0); // reset CSV line
		// the list of CSV columns is already sorted
		for (CSVColumnConfig conf : listCSVColumns) {
			try {
				csvLine.append(";");
				Object test = rs.getObject(conf.getPosition());
				if (test != null) {
					if (conf.isBoolean()) {
						csvLine.append(rs.getBoolean(conf.getPosition()));
					} else if (conf.isDate()) {
						csvLine.append(sdf.format(rs.getDate(conf.getPosition())));
					} else if (conf.isDouble()) {
						csvLine.append(rs.getDouble(conf.getPosition()));
					} else if (conf.isInteger()) {
						csvLine.append(rs.getInt(conf.getPosition()));
					} else if (conf.isLong()) {
						csvLine.append(rs.getLong(conf.getPosition()));
					} else if (conf.isString()) {
						String value = rs.getString(conf.getPosition());
						if (TypeUtil.isEmpty(value) == false) {
							csvLine.append("\"");
							csvLine.append(value.replace("\"", "\"\""));
							csvLine.append("\"");
						}
					}
				}
			} catch (Exception e) {
				throw new Exception("Fail to extract value from resultset for " + conf + " message: " + e.getMessage(), e);
			}
		}
	}
	
	private void extractSparatedValues(ResultSet rs) throws Exception {
		oneResult.clear(); // clear previous results
		for (SeparatedColumnConfig conf : listConfiguredColumns) {
			oneResult.put(conf.getColumnName(), rs.getObject(conf.getColumnName()));
		}
	}
	
	public boolean next() throws Exception {
		if (resultset == null) {
			throw new IllegalStateException("No resultset available");
		}
		boolean hasNext = resultset.next();
		if (hasNext) {
			extractValuesForCSV(resultset);
			extractSparatedValues(resultset);
		}
		return hasNext;
	}
	
	public String getCSVLine() {
		return csvLine.toString();
	}

	/**
	 * Returns a output value for the current result record
	 * @param outgoingSchemaColumn
	 * @return the value
	 */
	public Object getOutputValue(String outgoingSchemaColumn, boolean nullable) throws Exception {
		if (oneResult == null || oneResult.isEmpty()) {
			throw new IllegalStateException("No resut values available. We expect to have one result record but there is no one.");
		} else {
			Object value = oneResult.get(outgoingSchemaColumn);
			if (value == null && nullable == false) {
				throw new Exception("For column: " + outgoingSchemaColumn + " null value detected but column is configured as not nullable");
			}
			return value;
		}
	}
	
	public String getOutputValueAsString(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToString(value, null);
	}
	
	public Integer getOutputValueAsInteger(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToInteger(value, null);
	}
	
	public Long getOutputValueAsLong(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToLong(value, null);
	}
	
	public Double getOutputValueAsDouble(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDouble(value, null);
	}
	
	public Float getOutputValueAsFloat(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToFloat(value, null);
	}
	
	public BigDecimal getOutputValueAsBigDecimal(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBigDecimal(value, null);
	}
	
	public Boolean getOutputValueAsBoolean(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBoolean(value, null);
	}
	
	public Date getOutputValueAsDate(String outgoingSchemaColumn, boolean nullable, String pattern) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDate(value, pattern, null);
	}

	public Short getOutputValueAsShort(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToShort(value, null);
	}

	public void close() {
		if (resultset != null) {
			try {
				resultset.close();
			} catch (Exception e) {}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (Exception e) {}
		}
	}

	public String getQuery() {
		return query;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(Integer fetchSize) {
		if (fetchSize != null) {
			this.fetchSize = fetchSize;
		}
	}
}
