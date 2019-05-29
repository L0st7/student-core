package com.fis.lab.common;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fis.lab.anotation.Col;
import com.fis.lab.anotation.Id;
import com.fis.lab.anotation.Table;
import com.fis.lab.datasource.DataSourceFactory;

/**
 * @createdDate May 22, 2019
 * @author <a href="mailto:hiepnv14@fpt.com.vn">hiepnv14</a>
 * @version 0.0.1
 */
public abstract class AbstractJDBCRepository<T> {
	
	private Connection connection;
	private Statement statement;
	
	public AbstractJDBCRepository() {
		DataSourceFactory dataSourceFactory = new DataSourceFactory();
		try {
			connection = dataSourceFactory.getMySqlDataSource().getConnection();
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Select all abstract
	 * 
	 * @param <T>
	 * @param sqlSelect
	 * @param type
	 * @return list
	 */
	@SuppressWarnings("hiding")
	public<T extends Object> List<T> findAll(String sqlSelect, Class<T> type) {
		List<T> list = new ArrayList<T>();
		ResultSet rs = null;
		try {
			rs = statement.executeQuery(sqlSelect);
			while(rs.next()) {
				T t = type.newInstance();
				loadResultSetMapObject(rs, t);
				list.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Select one abstract
	 * 
	 * @param <T>
	 * @param sqlSelectOne
	 * @param type
	 * @return Object
	 */
	@SuppressWarnings("hiding")
	public<T extends Object> T findOne(String sqlSelectOne, Class<T> type) {
		ResultSet rs = null;
		T t = null;
		try {
			rs = statement.executeQuery(sqlSelectOne);
			while (rs.next()) {
				t = type.newInstance();
				loadResultSetMapObject(rs, t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
	
	/**
	 * Insert abstract
	 * 
	 * @param object
	 */
	public void insert(Object object) {
		PreparedStatement preparedStatement = null;
		Class<?> clazz = object.getClass();
		Set<Field> fieldsId = RefectionUtil.findFields(clazz, Id.class);
		Set<Field> fieldsCol = RefectionUtil.findFields(clazz, Col.class);
		String query = createSqlInsert(fieldsCol, fieldsId, object);
		try {
			preparedStatement = connection.prepareStatement(query.toString());
			mapParamInsert(preparedStatement, fieldsCol, fieldsId, object);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update abstract
	 * 
	 * @param sqlUpdate
	 */
	public void update(Object object) {
		PreparedStatement preparedStatement = null;
		Class<?> clazz = object.getClass();
		Set<Field> fieldsId = RefectionUtil.findFields(clazz, Id.class);
		Set<Field> fieldsCol = RefectionUtil.findFields(clazz, Col.class);
		Table table = clazz.getAnnotation(Table.class);
		try {
			String query = createSqlUpdate(object, fieldsCol, fieldsId, table);
			preparedStatement = connection.prepareStatement(query.toString());
			mapParamUpdate(preparedStatement, fieldsCol, fieldsId, object);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete abstract
	 * 
	 * @param sqlDelete
	 */
	public void delete(Object object) {
		PreparedStatement preparedStatement = null;
		Class<?> clazz = object.getClass();
		String query = createSqlDelete(object);
		Set<Field> fieldsId = RefectionUtil.findFields(clazz, Id.class);
		for(Field field : fieldsId) {
			Id id = field.getAnnotation(Id.class);
			if(id == null || id.name().equals("")) {
				continue;
			}else {
				try {
					field.setAccessible(true);
					preparedStatement = connection.prepareStatement(query);
					preparedStatement.setObject(1, field.get(object));
					preparedStatement.execute();
				} catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Map value result set to object abstract
	 * 
	 * @param rs
	 * @param object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void loadResultSetMapObject(ResultSet rs, Object object) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		Set<Field> fieldsCol = RefectionUtil.findFields(clazz, Col.class);
		Set<Field> fieldsId = RefectionUtil.findFields(clazz, Id.class);
		for(Field fieldId : fieldsId) {
			Id id = fieldId.getAnnotation(Id.class);
			if(id == null || id.name().equals("")) {
				continue;
			}else {
				try {
					fieldId.setAccessible(true);
					Object value = rs.getObject(id.name());
					Class<?> type = fieldId.getType();
					value = type.cast(value);
					fieldId.set(object, value);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		for(Field fieldCol: fieldsCol) {
			Col col = fieldCol.getAnnotation(Col.class);
			if(col == null || col.name().equals("")) {
				continue;
			}else {
				try {
					fieldCol.setAccessible(true);
					Object value = rs.getObject(col.name());
					Class<?> type = fieldCol.getType();
					value = type.cast(value);
					fieldCol.set(object, value);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** 
	 * Create query sql insert
	 * 
	 * @param fieldsCol
	 * @param fieldsId
	 * @param object
	 * @return sqlQuery
	 */
	private String createSqlInsert(Set<Field> fieldsCol,Set<Field> fieldsId,Object object) {
		Class<?> clazz = object.getClass();
		int lengthField = fieldsCol.size() + fieldsId.size();
		Table table = clazz.getAnnotation(Table.class);
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT INTO ");
		insertQuery.append(table.name() + " VALUES(");
		for(int i=0;i<lengthField;i++) {
			if(i==(lengthField-1)) {
				insertQuery.append("?)");
			}else {
				insertQuery.append("?,");
			}
		}
		return insertQuery.toString();
	}
	
	/**
	 * Create query sql update
	 * 
	 * @param object
	 * @param fieldsCol
	 * @param fieldsId
	 * @param table
	 * @return sqlQuery
	 */
	private String createSqlUpdate(Object object,Set<Field> fieldsCol,Set<Field> fieldsId,Table table) {
		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("UPDATE "+table.name()+" SET ");
		Iterator<Field> iteratorField = fieldsCol.iterator();
		while(iteratorField.hasNext()) {
			Field field = iteratorField.next();
			field.setAccessible(true);
			Col col = field.getAnnotation(Col.class);
			if(col == null || col.name().equals("")) {
				continue;
			}
			if(!iteratorField.hasNext()) {
				updateQuery.append(col.name() + "=? WHERE ");
			}else {
				updateQuery.append(col.name() + "=?,");
			}
		}
		for(Field field: fieldsId) {
			Id id = field.getAnnotation(Id.class);
			if(id == null || id.name().equals("")) {
				continue;
			}else {
				field.setAccessible(true);
				updateQuery.append(id.name() + "=?");
			}
		}
		return updateQuery.toString();
	}
	
	private PreparedStatement mapParamInsert(PreparedStatement preparedStatement,Set<Field> fieldsCol,Set<Field> fieldsId,Object object) {
		int n =1;
		try {
			for(Field field : fieldsId) {
				Id id = field.getAnnotation(Id.class);
				if(id == null || id.name().equals("")) {
					continue;
				}else {
					field.setAccessible(true);
					if(!id.identity()) {
						preparedStatement.setObject(n,field.get(object));
						n++;
					}else {
						preparedStatement.setObject(n,null);
						n++;
					}
				}
			}
			for(Field field : fieldsCol) {
				Col col = field.getAnnotation(Col.class);
				if(col == null || col.name().equals("")) {
					continue;
				}else {
					field.setAccessible(true);
					preparedStatement.setObject(n,field.get(object));
					n++;
				}
			}
		}catch (IllegalArgumentException| IllegalAccessException | SQLException e1) {
			e1.printStackTrace();
		}
		return preparedStatement;
	}
	
	private PreparedStatement mapParamUpdate(PreparedStatement preparedStatement,Set<Field> fieldsCol,Set<Field> fieldsId,Object object) {
		int n =1;
		try {
			for(Field field : fieldsCol) {
				Col col = field.getAnnotation(Col.class);
				if(col == null || col.name().equals("")) {
					continue;
				}else {
					field.setAccessible(true);
					preparedStatement.setObject(n,field.get(object));
					n++;
				}
			}
			for(Field field : fieldsId) {
				Id id = field.getAnnotation(Id.class);
				if(id == null || id.name().equals("")) {
					continue;
				}else {
					field.setAccessible(true);
					preparedStatement.setObject(n,field.get(object));
					n++;
				}
			}
		}catch (IllegalArgumentException| IllegalAccessException | SQLException e1) {
			e1.printStackTrace();
		}
		return preparedStatement;
	}
	
	private String createSqlDelete(Object object) {
		Class<?> clazz = object.getClass();
		Set<Field> fieldsId = RefectionUtil.findFields(clazz, Id.class);
		Table table = clazz.getAnnotation(Table.class);
		StringBuilder sqlDelete = new StringBuilder();
		sqlDelete.append("DELETE FROM "+table.name()+ " WHERE ");
		for(Field field: fieldsId) {
			Id id = field.getAnnotation(Id.class);
			if(id == null || id.name().equals("")) {
				continue;
			}else {
				field.setAccessible(true);
				sqlDelete.append(id.name() + "=?");
			}
		}
		return sqlDelete.toString();
	}
}
