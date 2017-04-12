package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

 


/***********************************************************************************************************************************
 * Class Name : Peer - This is the fundamental main class for peer which takes care for initiating server ,which can be used by other peer to 
 * 					Download file from this peer.
 * 				It has the main execution block which will invoke three threads.
 * 			Thread 1 : To start Peer as a server for letting other Peer to download file.
 * 			Thread 2 : To run the execution part for peer to take action according to the user input.
 * 						 To run a async process to update server periodically file values to server.so that server is constantly updated with files 
 * 						which are there with the peer.
 * 			Thread 3 and 4 : this will only run if the configuration is set as pull. Which is to set a file if its out of date when TTR is expired, and 
 * 						thread 5 will take care to check with the server if the files has been modified or not.
 * 			Thread 5 : This Thread will only run when configuration is set as push. Where if there is any modification requested by the user,then a 
 * 						Peer will broadcast invalid message.
 * @author Piyush and Priyanka
 *
 */
 
public class Peer{
	protected static String intPort;
	private static String fileName = "../peerNeighbour.txt";
	public static volatile Queue<MsgDetails> queueBroadcast;
	public static volatile Queue<MsgDetails> queryHitBroadcast;
	public static volatile Queue<MsgDetails> invalidFileBroadcast;
	public static volatile Queue<String> queue;
	//Keep a track of history as well
	/***************start change**************************/
	public static volatile HashMap<String,FileDetails> DownloadedFileDetails;
	public static volatile HashMap<String,FileDetails> OriginalFileDetails;
	public static ArrayList<Integer> neighbour;
	public static int localMsgID; 
	public static volatile ArrayList<String> reqtdMsgID;
	static final int maxTTL = 10;
	public static volatile boolean blnSearching;
	static long startTime;//INITIALIZED TIME FOR COMPUTING PURPOSE
	static long endTime;
	static int GlobalTTR;
	protected static void initlaizePeer() {
		//RequestorDetail =  new HashMap<String,String>();
		queueBroadcast= new LinkedList<MsgDetails>();
		queryHitBroadcast = new LinkedList<MsgDetails>();
		queue = new LinkedList<String>();
		neighbour =  new ArrayList<Integer>();
		localMsgID=0;
		reqtdMsgID=new ArrayList<String>();
		blnSearching=false;
		/***************start change**************************/
		OriginalFileDetails =  new HashMap<String,FileDetails>();
		DownloadedFileDetails = new HashMap<String,FileDetails>();
		GlobalTTR=1;
		invalidFileBroadcast =  new LinkedList<MsgDetails>(); 
		/***************end change**************************/
	}
	public static void main(String[] args) {
        int opt=0;
        String strApproach="";
        intPort=args[0];
    	/***************start change**************************/
        initlaizePeer();
        initActions();
    	
        //Code to check config
        BufferedReader bfconfig;
		try {
			bfconfig = new BufferedReader(new FileReader("../config.txt"));

		String lineConfig = "";
		
		while((lineConfig = bfconfig.readLine())!=null){
			String partsconf[] = lineConfig.split(":");

			if(partsconf.length==2)
			if ("approach".equals(partsconf[0])){
				strApproach=partsconf[1];
	    	}
			
		}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/***************end change**************************/
        //THREAD TO MAKE PEER AS SERVER FOR DOWNLOAD FUNCTIONALITY
      	Thread tServer = new Thread(new PeerServer());//THREAD 1
      	tServer.start();
      	Thread bServer = new Thread(new PeerBroadCastReq());//THREAD 2
      	bServer.start();
      	//THIS APPORACH HAS TWO THREADS BECAUSE WE ARE ALSO HAVING TTR EXPIRED STATE
      	/***************start change**************************/
      	if (strApproach.equals("pull")){
	      	Thread pullThread = new Thread(new PullFile());
	      	pullThread.start();
	      	Thread checkThread= new Thread(new CheckTTRExpired());
	      	checkThread.start();
      	}
      	else{
      		System.out.println("Push approach");
      		Thread broadCastInvalidFile= new Thread(new BroadCastInvalidFile());
      		broadCastInvalidFile.start();
      	}
      	
        Scanner sc=new Scanner(System.in);
		do{
			System.out.println("\nWhat do you want to do?");
	        System.out.println("1.Show both File list details.");
	        System.out.println("2.Lookup for a file in N/W.");
	        System.out.println("3.Show connected peer.");
	        System.out.println("4.Modify File.");
	        System.out.println("5.Get Invalid File & refresh.");
	        System.out.println("0.Exit.");
			System.out.println("Enter Some value.");
			opt=sc.nextInt();
			switch(opt){
			case 1:
				//System.out.println("Enter the Port number (and IP of neighbour)");//Currently taking only the port number
				//Integer ab =sc.nextInt();
				//System.out.println(ab);
				//neighbour.add(ab);
				System.out.println(DownloadedFileDetails.values());
				System.out.println(OriginalFileDetails.values());
		        System.out.println("Conneted to you neighbours!!!");
				break;
			case 2:
				System.out.println("Please enter the file which you want to search and download.");
				String strFileName = sc.next();
				localMsgID++;
				String MsgID = intPort+"-"+localMsgID;//MSGID format is portnumber with; msgid to make it unique in the network
				String name = "peerImp";
				reqtdMsgID.add(MsgID);
				blnSearching=true;
				startTime=System.currentTimeMillis();//SET START TIME AS A TIME
	            for (int a : neighbour){
	            	//Registry registry;
					try {
						//System.out.println(a+"trying to connect to....");
						String registry = "rmi://localhost:"+a+"/peerImp";
						//registry = LocateRegistry.getRegistry("localhost",a);
						//InClientIF comp = (InClientIF) registry.lookup(name);
						InClientIF comp = (InClientIF) Naming.lookup(registry);
						if (comp.query(MsgID, maxTTL, strFileName))
							System.out.println("Your request is initiated..");
						else
							System.out.println("Some problem with the system, please get in touch with admin..");
						TimeUnit.SECONDS.sleep(10);//SET 10 seconds as time.
						if (blnSearching){
							System.out.println("Unable to find the files, which is requested by you.");
							blnSearching=false;//blnSearching if true means that search is still going and we didnt found the outputf
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
	            }
	            break;
			case 3:
				for (int b : neighbour){
					System.out.println("Connected to -->"+b);
				}
				break;
			case 4:
				System.out.println("Please enter the File which is updated.");
				String tempFile = sc.next();
				if (OriginalFileDetails.containsKey(tempFile)){
					FileDetails tempFD= new FileDetails();
					tempFD=OriginalFileDetails.get(tempFile);
					tempFD.setversion(tempFD.getversion()+1);
					OriginalFileDetails.put(tempFile,tempFD);
					localMsgID++;
					String MsgID4 = intPort+"-"+localMsgID;
					MsgDetails tempMsgDetails = new MsgDetails(MsgID4,"localhost",Integer.valueOf(intPort),tempFile,maxTTL);
					reqtdMsgID.add(MsgID4);
					invalidFileBroadcast.add(tempMsgDetails);
				}
				else
					System.out.println("File not found.");
				break;
			case 5:
				int counter=0;
				for (FileDetails obj : DownloadedFileDetails.values()){
					ArrayList<String> tempAlFile=new ArrayList<String>();
					if (obj.getstatus()=="invalid"){
						counter++;
						tempAlFile.add(obj.getFileName());
						System.out.println(counter +"\t:"+obj.getFileName()+"\t Server:\t"+obj.getOriginPort());
					}
					if (counter>0){
						System.out.println("Enter Some option which you want to refresh.");
						opt=sc.nextInt();
						if (opt<=counter){
							System.out.println(tempAlFile.get(opt-1));
							FileDetails temp= DownloadedFileDetails.get(tempAlFile.get(opt-1));
							try {
								fetchFile(Integer.parseInt(temp.getOriginPort()),temp.getFileName());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
							
					}
				}
			default:
				System.out.println("Closing Program.");
				break;
			}
			/***************end change**************************/
		}while(opt!=0);
        
	}
	/*******************************************************************************************************************************
	 * Method :FetchFile : This method is used by the peer to connect with other peer from which files need to be fetched. In this 
	 * 						method we have to do the connection with the other peer.
	 * @param peer : This is the peer details which is selected by the user from whome the files need to be pulled.
	 * @throws IOException
	 */
	private synchronized static void fetchFile(int intPortNumber,String fileName) throws IOException{
		//iNFO TO USER AS THE FILES ARE BEING FETCHED
		//System.out.println("\t Fetching file from Peer \t:"+peer.getPeerId());
		//INITIALIZED THE PORT NUMBER OF THE SOURCE PEER
		String clientName = "rmi://localhost:"+intPortNumber+"/peerImp";
    	InClientIF compLocal; 
		try {
			compLocal = (InClientIF) Naming.lookup(clientName);//THIS WILL STABLISH THE CONNECTION WITH THE REMOTE PEER TO FETCH THE FILE
			//CREATED A LOCAL FOLDER WHERE WE WANT TO CREATE THE SAME FILE WHICH WE WANT TO COPY AND GIVE THE FILE NAME AS SAME FILE NAME WHICH IS REQUIRED.
			FileOutputStream fos=new FileOutputStream(System.getProperty("user.dir")+"/Download/"+fileName);
			//BELOW WE WILL GET BITWISE OUTPUT FROM THE PEER WITH THE FILE NAME
			byte[] btFile=compLocal.downloadFile(fileName);
			fos.write(btFile);	//WRITE THE CONTENT TO NEWLY CREATED FILE
	        fos.close();
        	System.out.println("successfully downloaded");
        	//System.out.println(compLocal.fetchVersion(fileName));
        	//Add logic to add files to downloaded hash table
        	/***************start change**************************/
        	 FileDetails obj =  new FileDetails(fileName,"valid",compLocal.fetchVersion(fileName),new Date(),GlobalTTR,Integer.toString(intPortNumber));
        	 DownloadedFileDetails.put(fileName, obj);//ADDING DOWNLOADED FILE TO THE LIST
        	 /***************end change**************************/
        	//CATCH BLOCK
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	
	}
	/*******************************************************************************************************************************
	 * Method :initActions : This method is used to do intialization and set the hash table for original list.//ADDED NEW METHOD
	 * @throws IOException
	 */
	private static void initActions(){
		try {
			BufferedReader bf = new BufferedReader(new FileReader(fileName));
			String line = "";
			
			while((line = bf.readLine())!=null){
				String parts[] = line.split("\t");
				
				if(parts.length==2)
				if (intPort.equals(parts[0]) ){
					neighbour.add(Integer.parseInt(parts[1]));
				
		    	}
				
			}
			/*--------------Start Changes ------------------*/
			//CODE CHANGE TO ADD FILES TO ORIGIN LIST WHEN ON REGISTER NEIGHBOURS
			File folder = new File(System.getProperty("user.dir")+"/Original");
			File[] listOfFiles = folder.listFiles();
			
			for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			        System.out.println("File " + listOfFiles[i].getName());
			        FileDetails obj =  new FileDetails(listOfFiles[i].getName(),"valid",1,null,0,intPort);
			        OriginalFileDetails.put(listOfFiles[i].getName(), obj);
			      }
			    }
			/***************end change**************************/
        }catch ( Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	/*********************************************************************************************************************************
	 * Class name : PeerServer : This class takes care for the part where client acts as a server.
	 * @author Piyush And Priyanka
	 *
	 */
	private static class PeerServer implements Runnable  {
			
	        
	        // Services this thread's peer client by sending the requested file.
			public void run() {
				try {
					InClientIF peerImp = new PeerImp();
					Naming.rebind("rmi://localhost:"+intPort+"/peerImp", peerImp);//REBIND THE CLIENT SO THAT IT CAN ACT AS A SERVER.
					System.out.println("Client is ready to accept request.....");
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			}
		}
	/*********************************************************************************************************************************
	 * Class name : PeerBroadCastReq : This class takes care for picking up the value from queue and broadcast to its neighbor
	 * @author Piyush And Priyanka
	 *
	 */
	private static class PeerBroadCastReq implements Runnable  {
			

	        // Services this thread's peer client by sending the requested file.
			public void run() {
				MsgDetails msgDetail=null;
				while(true){
				try {
					while(!queueBroadcast.isEmpty()){
						/*for (MsgDetails c : queueBroadcast){
							System.out.println(c.getPortNo()+"TTL is "+c.getTTL());
						}*/
						msgDetail=queueBroadcast.poll();//msg id to broadcast for query
						//System.out.println("got new request to push."+msgDetail.getFileName());
						break;
					}
					if (msgDetail != null){
						//System.out.println(msgDetail.getTTL());
						if (msgDetail.getTTL()>0){
							String name = "peerImp";
				            //Registry registry = LocateRegistry.getRegistry(msgDetail.getPeerIP(),msgDetail.getPortNo());
				            //InClientIF comp = (InClientIF) registry.lookup(name);
				            for (int a : neighbour){
				            	Registry registry2;
								try {
									//if (a!=Integer.parseInt(RequestorDetail.get(msgDetail).split("-")[0])){
									if (a!=msgDetail.portNo){
										//System.out.println("Port number --> "+a+"---->"+msgDetail.portNo);
										registry2 = LocateRegistry.getRegistry("localhost",a);
										InClientIF comp2 = (InClientIF) registry2.lookup(name);
										comp2.query(msgDetail.getmsgID(), msgDetail.getTTL()-1, msgDetail.getFileName());
										
									}
									
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					            
				            }
						}
					}
					msgDetail=null;
					while(!queryHitBroadcast.isEmpty()){
						msgDetail=queryHitBroadcast.poll();//msg id to send to requester with a hit
						break;
					}
					if (msgDetail != null){
						if (reqtdMsgID.contains(msgDetail.getmsgID())){
							if (blnSearching){
								blnSearching=false;
								System.out.println("Your requested item is here,we are proceeding with the download.");
								fetchFile(msgDetail.getPortNo(),msgDetail.getFileName());
								endTime=System.currentTimeMillis();//SET START TIME AS A TIME
								System.out.println("Total time taken by system to fetch is : "+Long.toString(endTime-startTime) +" ms.");
							}
						}
						else{
							if (msgDetail.getTTL()>0){
								String name = "peerImp";
					            //Registry registry = LocateRegistry.getRegistry(msgDetail.getPeerIP(),msgDetail.getPortNo());
					            //InClientIF comp = (InClientIF) registry.lookup(name);
					            for (int a : neighbour){
					            	Registry registry2;
									try {
										//if (a!=Integer.parseInt(RequestorDetail.get(msgDetail).split("-")[0])){
										if (a!=msgDetail.portNo){
											//System.out.println("Port number --> "+a+"---->"+msgDetail.portNo);
											registry2 = LocateRegistry.getRegistry("localhost",a);
											InClientIF comp2 = (InClientIF) registry2.lookup(name);
											comp2.queryhit(msgDetail.getmsgID(), msgDetail.getTTL()-1, msgDetail.getFileName(), "localhost",msgDetail.getPortNo());
											
										}
										
										
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						            
					            }
							}
						}
						msgDetail=null;
					}
					
					//Code change to remove old msgid from the queue so that its not filled with too many junk data
					if (queue.size()>30)
					{
						String tempMsgId = queue.poll();
						//RequestorDetail.remove(tempMsgId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				}
			}
		}
	/*********************************************************************************************************************************
	 * Class name : PeerServer : This class takes care for the part where client acts as a server.
	 * @author Piyush And Priyanka
	 *
	 */
	private static class PullFile implements Runnable  {
			
		static final long ONE_MINUTE_IN_MILLIS=60000;
	        // Services this thread's peer client by sending the requested file.
			public void run() {
				
				while(true){
					
					try {
						Thread.sleep(ONE_MINUTE_IN_MILLIS);
						//TimeUnit.SECONDS.sleep(60);
						for (FileDetails obj : DownloadedFileDetails.values()){
							//LOGIC TO CHECK IF TTR EXPIRED
							if (obj.getTimeStamp().before(new Date(System.currentTimeMillis()-obj.getTTR()*ONE_MINUTE_IN_MILLIS)) && obj.getstatus()=="valid"){
								obj.setstatus("TTR Expired");
							}
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	/***************ALL BELOW IS NEW MODIFICATION**************************/
	/*********************************************************************************************************************************
	 * Class name : CheckTTRExpired : This class checks if TTR expired then check with server if file has been modified
	 * @author Piyush And Priyanka
	 *
	 */
	private static class CheckTTRExpired implements Runnable  {
			
		
	        // Services this thread's peer client by sending the requested file.
			public void run() {
				String clientName ;
		    	InClientIF compLocal; 
		    	int tempVer=0;
				while(true){
					
					try {
					
						//TimeUnit.SECONDS.sleep(60);
						for (FileDetails obj : DownloadedFileDetails.values()){
							
							if (obj.getstatus()=="TTR Expired"){
								clientName = "rmi://localhost:"+obj.getOriginPort()+"/peerImp";
								compLocal = (InClientIF) Naming.lookup(clientName);
								tempVer=compLocal.fetchVersion(obj.getFileName());//FETCH THE VERSION FROM SERVER
								if (tempVer!=obj.getversion()){
									obj.setstatus("invalid");//UPDATE THE STATUS ACCORDING TO VERSION 
									System.out.println("updated file as invalid \t:\t"+obj.getFileName());}
								else{
									obj.setstatus("valid");
									obj.setTimeStamp(new Date());
								}
								DownloadedFileDetails.put(obj.getFileName(), obj);//UPDATE THE LIST
								
							}
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	/*********************************************************************************************************************************
	 * Class name : BroadCastInvalidFile : This class takes care to broadcast invalid file msg and update if peer has that file.
	 * @author Piyush And Priyanka
	 *
	 */
	private static class BroadCastInvalidFile implements Runnable  {

		public void run() {
			
			while(true){
				MsgDetails tempmsgDetails =  null;
				try {
					while(!invalidFileBroadcast.isEmpty()){//CHECK THE QUEUE IF THERE IS ANY INVALID FILE REQUEST
						tempmsgDetails=invalidFileBroadcast.poll();//msg id to broadcast for query
						break;
					}
					if (tempmsgDetails != null){
						if (tempmsgDetails.getTTL()>0){
							String name = "peerImp";
				            for (int a : neighbour){
				            	Registry registry2;
								try {
									//if (a!=Integer.parseInt(RequestorDetail.get(msgDetail).split("-")[0])){
									if (a!=tempmsgDetails.portNo){
										//System.out.println("Port number --> "+a+"---->"+msgDetail.portNo);
										registry2 = LocateRegistry.getRegistry("localhost",a);
										InClientIF comp2 = (InClientIF) registry2.lookup(name);
										comp2.INVALIDATION(tempmsgDetails.getmsgID(), tempmsgDetails.getTTL()-1, tempmsgDetails.getFileName());
										
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					            
				            }
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
