package id.go.big.spatial.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;

@Repository
public class KugiDaoImpl implements KugiDao
{
	@Resource(name = "igd.sessionFactories")
	private Map<String, SessionFactory> sessionFactories;

	@Override
	public void saveFeature(String schema, String entity, Map<String, Object> feature)
	{
		SessionFactory sessionFactory = sessionFactories.get(schema);
		
		// Convert type
		ClassMetadata metadata  = sessionFactory.getClassMetadata(entity);
		if(metadata!=null)
		{
			for(Entry<String, Object> entry : feature.entrySet())
			{
				String prop = entry.getKey();
				Object val = entry.getValue();
				if(val!=null&&ArrayUtils.contains(metadata.getPropertyNames(), prop))
				{
					Type type = metadata.getPropertyType(prop);
					
					if(type instanceof IntegerType 
							&&!(val instanceof Integer))
					{
						Integer intVal = null;
						if(val instanceof Number)
							intVal = ((Number) val).intValue();
						else
							intVal = Integer.valueOf(val.toString());
						feature.put(prop, intVal);
					}
					else if(type instanceof LongType 
							&&!(val instanceof Long))
					{
						Long longVal = null;
						if(val instanceof Number)
							longVal = ((Number) val).longValue();
						else
							longVal = Long.valueOf(val.toString());
						feature.put(prop, longVal);
					}
					else if(type instanceof DoubleType 
							&&!(val instanceof Double))
					{
						Double doubleVal = null;
						if(val instanceof Number)
							doubleVal = ((Number) val).doubleValue();
						else
							doubleVal = Double.valueOf(val.toString());
						feature.put(prop, doubleVal);
					}
					else if(type instanceof StringType 
							&&!(val instanceof String))
						feature.put(prop, val.toString());
				}
			}
		}
		
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(entity, feature);
	}
	
	@Override
	public void deleteFeatureByIds(String schema, String entity, Collection<Long> ids)
	{
		SessionFactory sessionFactory = sessionFactories.get(schema);
		Session session = sessionFactory.getCurrentSession();
		Set<Long> subIds = new HashSet<Long>();
		for(Long id : ids)
		{
			subIds.add(id);
			if(subIds.size()>=100)
			{
				doDeleteByIds(session, entity, subIds);
				subIds.clear();
			}
		}
		doDeleteByIds(session, entity, subIds);
	}
	
	private void doDeleteByIds(Session session, String entity, Collection<Long> ids)
	{
		if(!ids.isEmpty())
		{
			session.createSQLQuery(
						"DELETE FROM "+entity+" " +
						"WHERE OBJECTID IN (:ids)")
					.setParameterList("ids", ids)
					.executeUpdate();
		}
	}
}