package info.dyndns.paso;

import java.net.*;
import java.util.*;
import java.io.*;



/**
 * RHSystem サーバ
 * @author wizard_paso
 * @author http://codezine.jp/
 */

public class RHSystem {
	public static void main(String[] args) {
		RHSystem application = RHSystem.getInstance();
		
		application.start();
	}

	// サーバーはシングルトン設計。
	private static RHSystem instance;

	public static RHSystem getInstance() {
		if (instance == null) {
			instance = new RHSystem();
		}
		return instance;
	}

	// サーバーソケット
	private ServerSocket server;

	// 現在チャットに参加している全ユーザーの動的配列
	private ArrayList<Client> userList;

	private RHSystem() {
		userList = new ArrayList<Client>();
		Admin admin=Admin.getInstance(this);
	}

	// main メソッドから呼び出される
	public void start() {
		try {
			server = new ServerSocket(8080);

			while (!server.isClosed()) {
				// 新しいクライアントの接続を待つ
				Socket client = server.accept();

				// ユーザーオブジェクトを生成する
				Client user = new Client(client);
				addUser(user);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	// ユーザーを追加する
	public void addUser(Client user) {
		if (userList.contains(user))
			return;

		userList.add(user);
		System.out.println("addUser:" + user);
	}

	// 指定した名前のユーザーを取得する
	public Client getUser(String name) {
		for (int i = 0; i < userList.size(); i++) {
			Client user = userList.get(i);
			if (user.getName().equals(name))
				return user;
		}
		return null;
	}

	// すべてのユーザーを返す
	public Client[] getUsers() {
		Client[] users = new Client[userList.size()];
		userList.toArray(users);
		return users;
	}

	// ユーザーを削除する
	public void removeUser(Client user) {
		userList.remove(user);
		System.out.println("removeUser:" + user);
	}

	// すべてのユーザーを削除する
	public void clearUser() {
		userList.clear();
	}

	// サーバーを閉じて切断する
	public void close() throws IOException {
		server.close();
	}
}






