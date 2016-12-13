
public class pktError extends pktGeneric {

	int errorCode;	
	
	//constructor w/ header
	public pktError(pktHeader headerIn) {
		super(headerIn);
	}
	
	//constructor w/ errorCode
	public pktError(int errorCodeIn) {
		super(new pktHeader(0x10000001));
		errorCode = errorCodeIn;
	}

	//getter: errorCode
	public int getErrorCode() {
		return errorCode;
	}
}
