package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/******************************************************************************************************************
 * Interface : InClientIF This interface is created for client, when we interact with other client to broadcast message
 * 				and we aresending two message script, query and query hit
 * @author Piyush and Priyanka
 *
 */
public interface InClientIF  extends Remote {

	public boolean query(String msgID,int intTTL,String fileName) throws RemoteException;
	public boolean queryhit(String msgID,int intTTL,String fileName,String peerIP,int portNum) throws RemoteException;
	public byte[] downloadFile(String fileName) throws RemoteException;
	public int fetchVersion(String fileName) throws RemoteException; //ADDED NEW FUNCTION TO FETCH VERSION
	public boolean INVALIDATION(String msgID,int intTTL,String fileName) throws RemoteException;//TO BROADCAST INVALID FILE
}
