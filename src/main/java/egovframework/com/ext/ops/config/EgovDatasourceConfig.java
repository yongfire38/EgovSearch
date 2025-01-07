package egovframework.com.ext.ops.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EgovDatasourceConfig {
	
	@Autowired
	Environment env;

	private String className;

	private String url;

	private String userName;

	private String password;

	@PostConstruct
	void init() {
		className = env.getProperty("spring.datasource.driver-class-name");
		url = env.getProperty("spring.datasource.url");
		userName = env.getProperty("spring.datasource.username");
		password = env.getProperty("spring.datasource.password");
	}

	private DataSource basicDataSource() {
		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setDriverClassName(className);
		basicDataSource.setUrl(url);
		basicDataSource.setUsername(userName);
		basicDataSource.setPassword(password);
		return basicDataSource;
	}

	@Bean(name = { "dataSource" })
	public DataSource dataSource() {
		return basicDataSource();

	}

}
