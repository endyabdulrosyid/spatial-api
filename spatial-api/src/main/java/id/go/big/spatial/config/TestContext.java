package id.go.big.spatial.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
public class TestContext extends Context
{
	@Bean(name = "igd.dataSources")
	public Map<String, DataSource> dataSources() throws Exception
	{
		Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
		
		for(String igdSchema : igdSchemas())
		{
			ComboPooledDataSource dataSource = new ComboPooledDataSource();
			dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
			dataSource.setJdbcUrl(environment.getProperty("igd.jdbc.url"));
			dataSource.setUser(igdSchema);
			dataSource.setPassword(environment.getProperty("igd.jdbc.password"));
			dataSource.setAcquireIncrement(1);
			dataSource.setMinPoolSize(1);
			dataSource.setMaxPoolSize(50);
			dataSource.setPreferredTestQuery("SELECT 1 FROM DUAL");
			dataSource.setTestConnectionOnCheckin(true);
			dataSource.setIdleConnectionTestPeriod(300);
			
			dataSources.put(igdSchema, dataSource);
		}
		
		return dataSources;
	}
	
	@Override
	protected String getConnectionFinder()
	{
		return "id.go.big.spatial.connectionfinder.C3P0ConnectionFinder";
	}
	
	@Override
	@Bean(name = "metadata.dataSource")
	public DataSource metadataDataSource() throws Exception
	{
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
		dataSource.setJdbcUrl(environment.getProperty("metadata.jdbc.url"));
		dataSource.setUser(environment.getProperty("metadata.jdbc.username"));
		dataSource.setPassword(environment.getProperty("metadata.jdbc.password"));
		dataSource.setAcquireIncrement(1);
		dataSource.setMinPoolSize(1);
		dataSource.setMaxPoolSize(50);
		dataSource.setPreferredTestQuery("SELECT 1 FROM DUAL");
		dataSource.setTestConnectionOnCheckin(true);
		dataSource.setIdleConnectionTestPeriod(300);
			
		return dataSource;
	}
}
