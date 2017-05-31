package id.go.big.spatial.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

@Service
public class ZipService
{
	public void extract(File dir, File zip) throws IOException
	{
		ZipFile zipFile = null; 
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			zipFile = new ZipFile(zip);
			
			Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
			while(zipEnum.hasMoreElements())
			{
				ZipEntry entry = zipEnum.nextElement();
				File entryFile = new File(dir, entry.getName());
				if(entry.isDirectory())
					entryFile.mkdirs();
				else
				{
					entryFile.getParentFile().mkdirs();
					in = zipFile.getInputStream(entry);
					out = new FileOutputStream(entryFile);
					
					IOUtils.copy(in, out);
					
					in.close();
					out.close();
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(in, out, zipFile);
		}	
	}
}
