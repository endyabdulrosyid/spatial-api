package id.go.big.spatial.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {
		"id.go.big.spatial.service",
		"id.go.big.spatial.dao",
		"id.go.big.metadata.model.ig.dao"})
@PropertySource({
		"classpath:app.properties",
		"classpath:jdbc.properties"})
@EnableTransactionManagement
public class Context
{
	@Autowired
	protected Environment environment;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public List<String> igdSchemas()
	{
		String raw = environment.getProperty("igd.schemas");
		StrTokenizer tokenizer = new StrTokenizer(raw).setDelimiterChar(',').setTrimmerMatcher(StrMatcher.trimMatcher());
		return tokenizer.getTokenList();
	}
	
	@Bean
	public RestTemplate restTemplate()
	{
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		
		String proxyAddr = environment.getProperty("connection.proxy.address");
		if(StringUtils.isNotBlank(proxyAddr))
			requestFactory.setProxy(new Proxy(Type.HTTP, new InetSocketAddress(proxyAddr, Integer.valueOf(environment.getProperty("connection.proxy.port")))));
		
		String connectTimeout = environment.getProperty("connection.connectTimeout");
		if(StringUtils.isNotBlank(connectTimeout))
			requestFactory.setConnectTimeout(Integer.valueOf(connectTimeout));
		
		String readTimeout = environment.getProperty("connection.readTimeout");
		if(StringUtils.isNotBlank(readTimeout))
			requestFactory.setReadTimeout(Integer.valueOf(readTimeout));
		
		return new RestTemplate(requestFactory);
	}
	
	@Bean(name = "igd.dataSources")
	public Map<String, DataSource> dataSources() throws Exception
	{
		Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
		String jndiPrefix = environment.getProperty("igd.jndiPrefix");
		JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
		dataSourceLookup.setResourceRef(true);
		
		for(String igdSchema : igdSchemas())
		{
			dataSources.put(igdSchema, dataSourceLookup.getDataSource(jndiPrefix+igdSchema));
		}
		
		return dataSources;
	}
	
	@Bean(name = "igd.sessionFactories")
	public Map<String, SessionFactory> sessionFactories() throws Exception
	{
		Map<String, SessionFactory> sessionFactories = new HashMap<String, SessionFactory>();
		
		for(String igdSchema : igdSchemas())
		{
			LocalSessionFactoryBuilder sessionFactoryBuilder = new LocalSessionFactoryBuilder(dataSources().get(igdSchema));
			
			sessionFactoryBuilder.setProperty("hibernate.dialect", "org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect");
			sessionFactoryBuilder.setProperty("hibernate.show_sql", "false");
			sessionFactoryBuilder.setProperty("hibernate.hbm2ddl.auto", "update");
			/*sessionFactoryBuilder.setProperty("hibernate.jdbc.batch_size", "500");
			sessionFactoryBuilder.setProperty("hibernate.order_inserts", "true");
			sessionFactoryBuilder.setProperty("hibernate.order_updates", "true");*/
			sessionFactoryBuilder.setProperty("hibernate.spatial.connection_finder", getConnectionFinder());
			
			sessionFactoryBuilder.addResource("id/go/big/spatial/entity/Kugi.hbm.xml");
			
			sessionFactories.put(igdSchema, sessionFactoryBuilder.buildSessionFactory());
		}
		
		return sessionFactories;
	}
	
	protected String getConnectionFinder()
	{
		return "id.go.big.spatial.connectionfinder.WeblogicConnectionFinder";
	}
	
	@Bean(name = "igd.transactionManagers")
	public Map<String, PlatformTransactionManager> transactionManagers() throws Exception
	{
		Map<String, PlatformTransactionManager> transactionManagers = new HashMap<>();
		
		for(String igdSchema : igdSchemas())
		{
			HibernateTransactionManager transactionManager = new HibernateTransactionManager(sessionFactories().get(igdSchema));
			
			transactionManagers.put(igdSchema, transactionManager);
		}
		
		return transactionManagers;
	}
	
	@Bean(name = "metadata.dataSource", destroyMethod = "")
	public DataSource metadataDataSource() throws Exception
	{
		JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
		dataSourceLookup.setResourceRef(true);
			
		return dataSourceLookup.getDataSource(environment.getProperty("metadata.jndi"));
	}
	
	@Bean(name = "metadata.sessionFactory")
	public SessionFactory metadataSessionFactory() throws Exception
	{
		LocalSessionFactoryBuilder sessionFactoryBuilder = new LocalSessionFactoryBuilder(metadataDataSource());
		
		sessionFactoryBuilder.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
		sessionFactoryBuilder.setProperty("hibernate.show_sql", "false");
		
		sessionFactoryBuilder.scanPackages("id.go.big.metadata.model.ig.entity");
		
		return sessionFactoryBuilder.buildSessionFactory();
	}
	
	@Bean(name = "metadata.transactionManager")
	public PlatformTransactionManager metadataTransactionManager() throws Exception
	{
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(metadataSessionFactory());
			
		return transactionManager;
	}
}
