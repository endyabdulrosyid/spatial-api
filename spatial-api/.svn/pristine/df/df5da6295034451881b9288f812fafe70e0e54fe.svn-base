package id.go.big.spatial.exception;

public class MdwException extends Exception
{
	private static final long serialVersionUID = -9067583638994652526L;
	private String code;
	private String desc;
	
	public MdwException(String code, String desc)
	{
		super();
		this.code = code;
		this.desc = desc;
	}

	public MdwException(String code, String desc, Throwable cause)
	{
		super(cause);
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String getMessage()
	{
		return code+":"+desc;
	}
}
