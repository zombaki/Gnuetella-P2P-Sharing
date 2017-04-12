package Client;

import java.util.Date;

public class FileDetails implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	String fileName;//file name
	String status;//Status Invalid or valid
	int version;//Version of file
	Date timeStamp; 
	int TTR;//Time to refresh
	String OriginPort;//OriginPort for file
	/******************************************************************************************************************
	 * Constructor assigning value on creation.
	 * @param fileName
	 * @param status
	 * @param version
	 * @param timeStamp
	 * @param TTR
	 * @param OriginPort
	 */
	public FileDetails(String fileName,String status, int version, Date timeStamp,Integer TTR,String OriginPort){
		this.fileName=fileName;
		this.status = status;
		this.version = version;
		this.timeStamp = timeStamp;
		this.TTR=TTR;
		this.OriginPort=OriginPort;
	}
	 public FileDetails() {
		// TODO Auto-generated constructor stub
	}
	public String toString() {
		 return "Values for files are "+this.fileName + " vers :"+ version+" status :"+status+" Origin Port :"+OriginPort+"\n";
	 }
	public String getOriginPort() {
		return OriginPort;
	}
	
	public int getTTR() {
		return TTR;
	}

	public void setTTR(int TTR) {
		this.TTR = TTR;
	}


	public String getstatus() {
		return status;
	}

	public void setstatus(String status) {
		this.status = status;
	}

	public int getversion() {
		return version;
	}

	public void setversion(int version) {
		this.version = version;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
