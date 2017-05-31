package id.go.big.spatial.connectionfinder;

import java.sql.Connection;

import org.springframework.jdbc.support.nativejdbc.WebLogicNativeJdbcExtractor;

import oracle.jdbc.driver.OracleConnection;

public class WeblogicConnectionFinder implements org.geolatte.geom.codec.db.oracle.ConnectionFinder
{
	private static final long serialVersionUID = -6354235995762712027L;

	@Override
	public Connection find(Connection connection)
	{
		try
		{
			if(connection.isWrapperFor(OracleConnection.class))
			{
				WebLogicNativeJdbcExtractor extractor = new WebLogicNativeJdbcExtractor();
				
				return extractor.getNativeConnection(connection);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return (OracleConnection) connection;
	}
}
