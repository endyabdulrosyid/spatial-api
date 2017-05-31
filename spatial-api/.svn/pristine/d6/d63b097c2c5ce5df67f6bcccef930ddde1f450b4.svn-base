package id.go.big.spatial.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

@Service
public class DownloadService
{
	@Autowired
	private RestTemplate restTemplate;
	
	public File downloadFile(final String url, final File dir) throws IOException
	{
		if(url.startsWith("file:"))
		{
			String filename = FilenameUtils.getBaseName(url);
			File resultFile = new File(dir, filename);
			FileUtils.copyURLToFile(new URL(url), resultFile);
			
			return resultFile;
		}
		else
		{
			RequestCallback requestCallback = new RequestCallback()
			{				
				@Override
				public void doWithRequest(ClientHttpRequest request) throws IOException
				{
					request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
				}
			};
			
			ResponseExtractor<File> responseExtractor = new ResponseExtractor<File>()
			{				
				@Override
				public File extractData(ClientHttpResponse response) throws IOException
				{
					String filename = null;
					List<String> disposition = response.getHeaders().get("Content-Disposition");
					if(disposition!=null&&!disposition.isEmpty())
						filename = StringUtils.substringAfterLast(disposition.get(0), "filename=");
					else
						filename = FilenameUtils.getBaseName(StringUtils.substringBeforeLast(url, "?"));
					
					File resultFile = new File(dir, filename);
					FileOutputStream output = new FileOutputStream(resultFile);
					IOUtils.copy(response.getBody(), output);
					output.close();
					
					return resultFile;
				}
			};
			
			return restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
		}
	}
}
