import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.*;
import java.rmi.server.*;
/**
 * Provides a method that can be used to count the number of words in a String
 * @author hrqiu
 *
 */
public class WordCounter extends UnicastRemoteObject implements WordCount{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		try {
			WordCounter app = new WordCounter();
			// Register the service
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("WordCounter", app);
			
			// delete relative previous data in the files
			try {
				FileWriter f = new FileWriter(new File("UserInfo.txt"));
				f.write("");
				f.close();
				f = new FileWriter(new File("OnlineUser.txt"));
				f.write("");
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			System.err.println("Exception thrown: "+e);
		}
		
	}
	
	public WordCounter() throws RemoteException {}
	public int count(String message) throws RemoteException {
		if (message.isEmpty())
			return 0;
		else
			return message.split(" +").length;
	}

	@Override
	/**
	 * @return integer: state
	 * 1 stands for valid (update online user list)
	 * 2 stands for no such user (need to register first)
	 * 3 stands for password wrong (need to enter password again)
	 * 4 stands for already login
	 * 
	 */
	public int Login(String username, String password) throws RemoteException {
		int state = 0;
		if (!userList.contains(username))
			state = 2;
		else if (!password.equals(this.password.get(userList.indexOf(username))))
			state = 3;
		else if (onlineUserList.contains(username))
			state = 4;
		else {
			// update online user list and file
			onlineUserList.add(username);
			writeToFile(username, "", "OnlineUser.txt");
			state = 1;
		}
		return state;
	}

	@Override
	/**
	 * @return integer: state
	 * 1 stands for valid (register successfully -> update user list -> login)
	 * 5 stands for user name already exist (enter user name again)
	 * 
	 */
	public int Register(String username, String password) throws RemoteException {
		int state = 0;
		if (userList.contains(username))
			state = 5;
		else {
			// update user list
			userList.add(username);
			this.password.add(password);
			// update file
			writeToFile(username, password, "UserInfo.txt");
			
			// login
			Login(username, password);
			state = 1; // succeed
		}
		return state;
	}

	@Override
	public void Logout(String username) throws RemoteException {
		// update online user list
		removeUser(username);
	}

	@Override
	public void writeToFile(String username, String password, String fileName) throws RemoteException {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true)); // append
		    out.write(username + "#" + password);
		    out.newLine();
		    out.close();
		} catch (IOException e){
			System.out.println("Fail to write to file!");
			e.printStackTrace();
		}
		
	}

	@Override
	public void removeUser(String username) throws RemoteException {
		// remove from the array list
		onlineUserList.remove(username);
		
		// update the local file OnlineUser.txt
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("OnlineUser.txt"));
			for (int i = 0; i < onlineUserList.size(); i++) {
				out.write("" + onlineUserList.get(i));
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("Fail to write to file!");
			e.printStackTrace();
		}
		
	}

}