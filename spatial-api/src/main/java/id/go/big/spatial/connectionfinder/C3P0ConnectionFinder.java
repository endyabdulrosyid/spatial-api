package id.go.big.spatial.connectionfinder;

import java.sql.Connection;

import org.geolatte.geom.codec.db.oracle.ConnectionFinder;
import org.springframework.jdbc.support.nativejdbc.C3P0NativeJdbcExtractor;

public class C3P0ConnectionFinder extends C3P0NativeJdbcExtractor implements ConnectionFinder
{
	private static final long serialVersionUID = -6354235995762712027L;

	@Override
	public Connection find(Connection connection)
	{
		try
		{
			return getNativeConnection(connection);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return connection;
		}
	}
}
