package id.go.big.spatial.connectionfinder;

import java.sql.Connection;

import oracle.jdbc.driver.OracleConnection;

public class ConnectionFinder implements org.geolatte.geom.codec.db.oracle.ConnectionFinder
{
	private static final long serialVersionUID = -6354235995762712027L;

	@Override
	public Connection find(Connection connection)
	{
		try
		{
			if(connection.isWrapperFor(OracleConnection.class))
				return connection.unwrap(OracleConnection.class);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return (OracleConnection) connection;
	}
}
