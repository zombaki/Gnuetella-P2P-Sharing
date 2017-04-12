package Client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class PeerImp extends UnicastRemoteObject  implements InClientIF {

	protected PeerImp() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized boolean query(String msgID, int intTTL, String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		//First check if the process has the file or not.
		MsgDetails msgDetail = new MsgDetails(msgID,null,0,fileName,intTTL);
		//Peer.RequestorDetail.put(msgID,parentPeerIP +";"+ ParentPortNum);
		if (Peer.queue.contains(msgID))
			return true;
		Peer.queue.add(msgID);
		File f = new File("./Original/"+fileName);
		//System.out.println(msgID+fileName+ParentPortNum);
		if(f.exists()){
		    //send query hit
			msgDetail.setPortNo(Integer.parseInt(Peer.intPort));//We will add the ip address as well here
			msgDetail.setTTL(Peer.maxTTL-intTTL+1);//to optimize number of hops required to reach back
			Peer.queryHitBroadcast.add(msgDetail);//To add the msg details that need to be broadcast for query  hit
			//System.out.println(Peer.queryHitBroadcast);
		}
		else{
			//System.out.println(Peer.queueBroadcast.isEmpty());
			Peer.queueBroadcast.add(msgDetail);//To add the msg details if its not found in the Peer
			//System.out.println(Peer.queueBroadcast.isEmpty());
		}
		
		return true;
	}

	@Override
	public synchronized boolean queryhit(String msgID, int intTTL, String fileName, String peerIP, int portNum)
			throws RemoteException {
		//System.out.println("Found the file baba");
		MsgDetails msgDetail = new MsgDetails(msgID,peerIP,portNum,fileName,intTTL);
		Peer.queryHitBroadcast.add(msgDetail);
		// TODO Auto-generated method stub
		return false;
	}
	/*********************************************************************************************************
	 * Method name :downloadFile this takes care for downloading files from server.
	 */
	public synchronized byte[] downloadFile(String fileName) throws RemoteException {
		System.out.println("Printing for download requested for file - " +fileName);
		File file = new File("Original\\"+fileName);//Path to download file
		byte[] buffer= new byte[(int)file.length()];
		BufferedInputStream input;
		try { 
			input= new BufferedInputStream(new FileInputStream(file));//READ THE FILE FROM CLIENT AS SERVER AND SEND THE BYTE TO THE REQUESTED PEER
			input.read(buffer, 0, buffer.length);
			return buffer;
		} catch (IOException e) { 
			e.printStackTrace();
			return null;
		}

	}
	public synchronized int fetchVersion(String fileName) throws RemoteException{
		if (Peer.OriginalFileDetails.containsKey(fileName)){
			return Peer.OriginalFileDetails.get(fileName).getversion();
		}
		return 0;
		
	}
	public synchronized boolean INVALIDATION(String msgID, int intTTL, String fileName) throws RemoteException {
		MsgDetails msgDetail = new MsgDetails(msgID,null,0,fileName,intTTL);
		if (Peer.queue.contains(msgID))
			return true;
		Peer.queue.add(msgID);
		if (Peer.DownloadedFileDetails.containsKey(fileName)){
			FileDetails obj = Peer.DownloadedFileDetails.get(fileName);
			obj.setstatus("invalid");
			Peer.DownloadedFileDetails.put(fileName, obj);
			System.out.println(fileName +"\t is set to invalidate,Please refresh to fetch the latest file.");
		}
		Peer.invalidFileBroadcast.add(msgDetail);
		return true;
	}
}
