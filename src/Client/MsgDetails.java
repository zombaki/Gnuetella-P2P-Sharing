package Client;

public class MsgDetails  implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	String peerIP;//PEER IP
	int portNo;//PORT NUMBER
	String fileName;//FILE NAME
	int TTL;//TTL of the message
	String msgID;//PEER IP
	/*****************************************************************************************************************************
	 * Constructor assigning value on creation.
	 * @param peerIP //PEER IP
	 * @param portNo //PORT NUMBER OF PEER
	 * @param fileName //FILENAME
	 * @param TTL //TTL of the message
	 */
	public MsgDetails(String msgID,String peerIP, int portNo, String fileName,Integer TTL){
		this.msgID=msgID;
		this.peerIP = peerIP;
		this.portNo = portNo;
		this.fileName = fileName;
		this.TTL=TTL;
		
	}
	public String getmsgID() {
		return msgID;
	}

	
	public int getTTL() {
		return TTL;
	}

	public void setTTL(int TTL) {
		this.TTL = TTL;
	}


	public String getPeerIP() {
		return peerIP;
	}

	public void setPeerIP(String peerIP) {
		this.peerIP = peerIP;
	}

	public int getPortNo() {
		return portNo;
	}
 
	public void setPortNo(int portNo) {
		this.portNo = portNo;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
