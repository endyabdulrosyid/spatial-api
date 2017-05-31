package id.go.big.spatial.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import id.go.big.metadata.model.ig.dao.FeatureCodeDao;
import id.go.big.metadata.model.ig.entity.FeatureCode;
import id.go.big.spatial.bean.MigrationReport;

@Service
public class MigrationService
{
	@Autowired
	private FeatureCodeDao featureCodeDao;
	@Autowired
	private IGDService igdService;
	
	private Pattern filenamePatern = Pattern.compile(".*(^|[^A-Z])+([A-Z]+_[A-Z]+_[0-9]+K)\\..*");
	
	@Transactional(value = "metadata.transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
	public void migrateShp(File file, File reportFile) throws IOException
	{
		long start = System.currentTimeMillis();
		
		MigrationReport.init();
		
		if(file.isDirectory())
		{
			for(File shpFile : FileUtils.listFiles(file, new String[]{"shp"}, true))
			{
				migrateShpFile(shpFile, reportFile);
			}
		}
		else
		{
			migrateShpFile(file, reportFile);
		}
		
		MigrationReport.destroy();
		
		System.out.println("Done in "+(System.currentTimeMillis()-start)+" ms");
	}
	
	private void migrateShpFile(File shpFile, File reportFile) throws IOException
	{
		MigrationReport report = MigrationReport.get();
		report.clear();
		
		String fCode = null;
		
		Matcher matcher = filenamePatern.matcher(shpFile.getPath());
		if(matcher.matches())
		{
			String alias = matcher.group(2).toUpperCase();
			report.setAlias(alias);
			FeatureCode featureCode = featureCodeDao.findByAlias(alias);
			if(featureCode!=null)
				fCode = featureCode.getCode();
		}
		
		try
		{
			igdService.saveIGDFromShapefile(shpFile, fCode);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			report.getErrors().add(e.getMessage());
		}
		
		System.out.println(report.print());
		FileUtils.writeLines(reportFile, Arrays.asList(report.print()), true);
	}
}