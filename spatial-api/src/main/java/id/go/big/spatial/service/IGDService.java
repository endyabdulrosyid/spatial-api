package id.go.big.spatial.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import id.go.big.metadata.model.ig.dao.FeatureCodeDao;
import id.go.big.metadata.model.ig.entity.FeatureCode;
import id.go.big.spatial.bean.MigrationReport;
import id.go.big.spatial.dao.KugiDao;
import id.go.big.spatial.exception.MdwException;

@Service
public class IGDService
{
	@Resource(name = "igd.transactionManagers")
	private Map<String, PlatformTransactionManager> transactionManagers;	
	private Map<String, TransactionTemplate> transactionTemplates = new HashMap<String, TransactionTemplate>();
	
	@Autowired
	private DownloadService downloadService;
	@Autowired
	private ZipService zipService;
	
	@Autowired
	private FeatureCodeDao featureCodeDao;
	
	private final KugiDao kugiDao;
	
	@Value("${igd.shapefile.zipDir}")
	private String zipDir;
	@Value("${igd.shapefile.tempDir}")
	private String tempDirRoot;
	
	private int batchSize = 500;
	
	@Autowired
	public IGDService(FeatureCodeDao featureCodeDao, KugiDao kugiDao)
	{
		this.kugiDao = kugiDao;
	}
	
	@Transactional(value = "metadata.transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
	public void saveIGDFromShpZip(String shapefileUrl, String fCode, String orgCode) throws MdwException
	{
		String zipDirname = this.zipDir;
		if(StringUtils.isNotBlank(orgCode))
			zipDirname += "/"+orgCode;
		File zipDir = new File(zipDirname);
		zipDir.mkdirs();
		
		File shapefileZip = null;
		try {shapefileZip = downloadService.downloadFile(shapefileUrl, zipDir);}
		catch (IOException e) {throw new MdwException("39", "Error occured when downloading shapefile", e);}
		
		File tempDir = new File(tempDirRoot, String.valueOf(System.currentTimeMillis())+RandomStringUtils.randomNumeric(4));
		tempDir.mkdirs();
		
		try {zipService.extract(tempDir, shapefileZip);}
		catch (IOException e) {throw new MdwException("99", "Error occured when extracting shapefile", e);}

		try
		{
			for(File shpFile : FileUtils.listFiles(tempDir, new String[]{"shp"}, true))
			{
				saveIGDFromShapefile(shpFile, fCode);
			}
		}
		catch (IOException e) {throw new MdwException("99", "Error when reading shapefile", e);}
		
		try {FileUtils.deleteDirectory(tempDir);}
		catch (IOException e) {e.printStackTrace();}
	}
	
	@Transactional(value = "metadata.transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
	public void saveIGDFromShapefile(File shpFile, String fCode) throws IOException
	{
		MigrationReport report = MigrationReport.get();
		if(report!=null)
			report.setFilename(shpFile.getPath());		
		
		List<Map<String, Object>> featureMaps = new ArrayList<Map<String, Object>>();
		
		FileDataStore dataStore = null;
		FeatureIterator<SimpleFeature> iterator = null;
		
		int records = 0;
		int processed = 0;
		
		try
		{
			shpFile.setReadOnly();
			dataStore = FileDataStoreFinder.getDataStore(shpFile);
			
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource();
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(Filter.INCLUDE);

			iterator = collection.features();
			records = collection.size();
			
			Set<Long> savedIds = new HashSet<Long>();
			
			while(iterator.hasNext())
			{
				SimpleFeature feature = iterator.next();
				
				if(fCode==null)
					fCode = StringUtils.trimToNull((String) feature.getAttribute("FCODE"));
				
				if(fCode!=null)
				{
					processed++;
					
					if(report!=null&&report.getfCode()==null)
						report.setfCode(fCode);
					
					Map<String, Object> featureMap = new HashMap<String, Object>();
				
					for(AttributeDescriptor desc : feature.getFeatureType().getAttributeDescriptors())
					{
						Object attr = feature.getAttribute(desc.getLocalName());
						if(attr!=null)
						{
							if(attr instanceof String)
								attr = StringUtils.trimToNull((String) attr);
						}
						featureMap.put(desc.getLocalName().toUpperCase(), attr);
					}
				
					if(!featureMap.containsKey("SHAPE"))
						featureMap.put("SHAPE", feature.getDefaultGeometry());
					featureMap.put("FCODE", fCode);
					
					featureMaps.add(featureMap);
					
					// Check for batch saving
					if(featureMaps.size()>=batchSize)
					{
						System.out.println("Save "+fCode+" "+processed+"/"+records);
						boolean success = saveFeatures(fCode, featureMaps, savedIds);
						featureMaps.clear();
						
						if(!success)
						{
							processed = 0;
							break;
						}
					}					
				}				
			}
			
			if(fCode!=null&&!featureMaps.isEmpty())
			{
				System.out.println("Save "+fCode+" "+processed+"/"+records);
				saveFeatures(fCode, featureMaps, savedIds);
			}
		}
		catch(Exception e)
		{
			if(report!=null)
				report.getErrors().add(e.getMessage());
			throw e;
		}
		finally
		{
			if(iterator!=null)
				iterator.close();
			if(dataStore!=null)
				dataStore.dispose();
			if(report!=null)
			{
				report.setRecords(records);
				report.setProcessed(processed);
			}
		}
	}
	
	private boolean saveFeatures(String fCode, final List<Map<String, Object>> features, final Collection<Long> savedIds)
	{
		final MigrationReport report = MigrationReport.get();
		
		FeatureCode featureCode = featureCodeDao.findByCode(fCode);
		if(featureCode!=null)
		{
			if(report!=null)
				report.setAlias(featureCode.getAlias());
			
			final String entity = StringUtils.substringBeforeLast(featureCode.getAlias(), "_").toUpperCase();
			final String schema = "IGD"+StringUtils.substringAfterLast(featureCode.getAlias(), "_").toUpperCase();
			
			if(!transactionTemplates.containsKey(schema))
			{
				TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManagers.get(schema));
				transactionTemplates.put(schema, transactionTemplate);
			}
			TransactionTemplate transactionTemplate = transactionTemplates.get(schema);
			try
			{
				transactionTemplate.execute(new TransactionCallback<Void>()
				{
					@Override
					public Void doInTransaction(TransactionStatus status)
					{
						Set<Long> curSavedIds = new HashSet<Long>();
						
						for(Map<String, Object> feature : features)
						{
							kugiDao.saveFeature(schema, entity, feature);
							curSavedIds.add((Long) feature.get("OBJECTID"));
						}
						
						savedIds.addAll(curSavedIds);
						return null;
					}
				});
			}
			catch(Exception e)
			{
				e.printStackTrace();
				if(report!=null)
					report.getErrors().add(e.getMessage());
				
				// Manual rollback
				transactionTemplate.execute(new TransactionCallback<Void>()
				{
					@Override
					public Void doInTransaction(TransactionStatus status)
					{	
						System.out.println("Manual Rollback");
						kugiDao.deleteFeatureByIds(schema, entity, savedIds);
						return null;
					}
				});
				
				return false;
			}
			
			return true;
		}
		else
		{
			if(report!=null)
				report.getErrors().add("Unknown FCode: "+fCode);
		
			return false;
		}
	}
}