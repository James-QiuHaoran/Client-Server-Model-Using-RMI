import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * RMI methods definition.
 * All methods on RMI must throw RemoteException.
 * @author HAORAN
 *
 */
public interface WordCount extends Remote {
	// We are making the count() in WordCounter class a service
	int count (String message) throws RemoteException;
	
	// For Assignment 1
	/*
	 * Validate user from UserInfo.txt
	 * Avoid Repeated login using OnlineUser.txt
	 * Update OnlineUser.txt
	 */
	int Login(String username, String password) throws RemoteException;
	
	/*
	 * Avoid duplicating user name with UserInfo.txt
	 * login user and update OnlineUser.txt
	 */
	int Register(String username, String password) throws RemoteException;
	
	/*
	 * Update OnlineUser.txt
	 */
	void Logout(String username) throws RemoteException;
	
	ArrayList<String> userList = new ArrayList<String>();
	ArrayList<String> password = new ArrayList<String>();
	ArrayList<String> onlineUserList = new ArrayList<String>();
	
	// write the username and password to the local file
	void writeToFile(String username, String password, String fileName) throws RemoteException;
	
	// remove the user from online user list file
	void removeUser(String username) throws RemoteException;
}