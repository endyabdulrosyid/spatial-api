package id.go.big.spatial.dao;

import java.util.Collection;
import java.util.Map;

public interface KugiDao
{
	public void saveFeature(String schema, String entity, Map<String, Object> feature);
	public void deleteFeatureByIds(String schema, String entity, Collection<Long> ids);
}