package id.go.big.spatial.test;

import java.io.File;

import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.cga.NumericalMethods;
import org.geolatte.geom.codec.db.oracle.SdoMultiPolygonEncoder;
import org.geolatte.geom.jts.JTS;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class IGDTest
{
	public static void main(String[] args)
	{
		try
		{
			File shpFile = new File("I:/SHP RBI/KUGI_RBI25K_BALI_NT/AGRIKEBUN_AR_25K.shp");
			
			shpFile.setReadOnly();
			
			FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);		
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource();
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(Filter.INCLUDE);
			FeatureIterator<SimpleFeature> iterator = collection.features();
			
			while(iterator.hasNext())
			{
				SimpleFeature feature = iterator.next();
				/*for(AttributeDescriptor attr : feature.getFeatureType().getAttributeDescriptors())
				System.out.println(attr.getLocalName()+":"+feature.getAttribute(attr.getLocalName()));*/
				//if(feature.getID().equals("SUNGAI_AR_25K.158"))
				{
					System.out.println(feature.getDefaultGeometry());
					MultiPolygon geom = (MultiPolygon) JTS.from((com.vividsolutions.jts.geom.Geometry) feature.getDefaultGeometry());
					System.out.println(geom);
					for(int i=0;i<geom.getNumGeometries();i++)
					{
						System.out.println(i);
						Polygon pg = (Polygon) geom.getGeometryN(i);
						PositionSequence positionSequence = pg.getExteriorRing().getPositions();
						for(int j=0;j<positionSequence.size();j++)
						{
							Position pos = positionSequence.getPositionN(j);
							double[] da = pos.toArray(null);
							System.out.println(da[0]+":"+da[1]);
						}
						System.out.println(NumericalMethods.determinant(1, 1, 1, 118.65497699600007, 118.65497691600001, 118.65497697500007, -8.547922309999933, -8.547922119999953, -8.547922017999952));
					}
					SdoMultiPolygonEncoder encoder = new SdoMultiPolygonEncoder();
					encoder.encode(geom);
				}
			}
			
			iterator.close();
			dataStore.dispose();
			
			/*ApplicationContext context = new AnnotationConfigApplicationContext(TestContext.class);
			
			//File shpDir = new File("I:/RBI/SHP/25K/BALI");
			File shpDir = new File("I:/SHP RBI/KUGI_RBI25K_BALI_NT/SUNGAI_AR_25K.shp");
			File reportDir = new File("D:/Works/Fusi/2016/BIG/docs/test-report.csv");
			
			context.getBean(MigrationService.class).migrateShp(shpDir, reportDir);*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}