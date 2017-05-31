package id.go.big.spatial.bean;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class MigrationReport
{
	private static ThreadLocal<MigrationReport> threadLocal = new ThreadLocal<MigrationReport>();	
	
	public static void init()
	{
		threadLocal.set(new MigrationReport());
	}
	
	public static MigrationReport get()
	{
		return threadLocal.get();
	}
	
	public static void destroy()
	{
		threadLocal.set(null);
	}
	
	private String filename;
	private String fCode;
	private String alias;
	private int records;
	private int processed;
	private Set<String> errors = new LinkedHashSet<String>();

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getfCode() {
		return fCode;
	}

	public void setfCode(String fCode) {
		this.fCode = fCode;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getRecords() {
		return records;
	}

	public void setRecords(int records) {
		this.records = records;
	}

	public int getProcessed() {
		return processed;
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	public Set<String> getErrors() {
		return errors;
	}

	public void setErrors(Set<String> errors) {
		this.errors = errors;
	}

	public void clear()
	{
		filename = null;
		fCode = null;
		alias = null;;
		records = 0;
		processed = 0;
		errors.clear();
	}

	public String print()
	{
		return 
				filename 
				+"|"+StringUtils.trimToEmpty(alias)
				+"|"+StringUtils.trimToEmpty(fCode)
				+"|"+records
				+"|"+processed
				+"|"+StringUtils.join(errors, ", ");
	}
}
