package id.go.big.spatial.test;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import id.go.big.spatial.config.TestContext;
import id.go.big.spatial.service.MigrationService;

public class Migration
{
	public static void main(String[] args)
	{
		ApplicationContext context = null;
		
		try
		{
			context = new AnnotationConfigApplicationContext(TestContext.class);
			
			//File shpDir = new File("I:/RBI/SHP/25K/BALI");
			//File shpDir = new File("I:/SHP RBI/KUGI_RBI50K_SUMATERA");
			//File reportDir = new File("D:/Works/Fusi/2016/BIG/docs/migrate-report"+DateFormatUtils.format(new Date(), "yyMMddHHmm")+".csv");
			File shpDir = new File("E:/SHP RBI/KUGI_RBI50K_SUMATERA");
			File reportDir = new File("F:/FUSISOLUTION/Project/BIG/Ina-Geoportal/migrate-report"+DateFormatUtils.format(new Date(), "yyMMddHHmm")+".csv");
			
			context.getBean(MigrationService.class).migrateShp(shpDir, reportDir);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}