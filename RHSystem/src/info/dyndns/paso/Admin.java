package info.dyndns.paso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author wizard_paso
 */

class Admin extends Thread {
	private RHSystem server;
	
	private static Admin instance;

	public static Admin getInstance(RHSystem server) {
		if (instance == null) {
			instance = new Admin(server);
		}
		return instance;
	}

	public Admin(RHSystem server) {
		this.server=server;
		start();
	}

	public void run() {
		
			BufferedReader input = new BufferedReader(new InputStreamReader(
					System.in));
			while(true){
				try {
				String str = input.readLine();
				System.out.println(str);
				for (Client user : server.getUsers()) {
					user.sendMessageEvent(new JSONObject(str));//管理用の入力はイベント処理へ回す｡JSONの型でないといけない｡
				}
				
				} catch (IOException e) {
					System.err.println(e);
				}catch(JSONException e){
					System.err.println(e);
				}
			}
		

	}
}
