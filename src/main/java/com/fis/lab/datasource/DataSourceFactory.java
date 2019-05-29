package com.fis.lab.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

/**
 * @CreatedDate May 23, 2019	
 * @author <a href="mailto:hiepnv14@fpt.com.vn">hiepnv14</a>
 * @version 0.0.1
 */
public class DataSourceFactory {
	private Properties properties;
	public DataSourceFactory() {
		properties = new Properties();
		loadProperties();
	}
	
	public DataSource getMySqlDataSource() {
		MysqlDataSource mysqlDataSource = new MysqlDataSource();
		mysqlDataSource.setUrl(properties.getProperty("URL"));
		mysqlDataSource.setUser(properties.getProperty("USER_NAME"));
		mysqlDataSource.setPassword(properties.getProperty("PASSWORD"));
		return mysqlDataSource;
	}
	
	private Properties loadProperties() {
		try {
			FileInputStream fileInputStream = new FileInputStream("resources/configdb.properties");
			properties.load(fileInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}
