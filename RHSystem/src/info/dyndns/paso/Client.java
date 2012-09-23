package info.dyndns.paso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

//チャットユーザーオブジェクト
class Client implements Runnable, MessageListener {
	// ソケット
	private Socket socket;

	// ユーザーの名前
	private String name;

	// チャットサーバー
	private RHSystem server = RHSystem.getInstance();

	// メッセージリスナの動的配列
	// メッセージリスナはこのユーザーが発言したときに呼び出されるイベント
	private ArrayList<MessageListener> messageListeners;

	private BufferedReader reader;
	private BufferedWriter writer;

	public Client(Socket socket) {
		messageListeners = new ArrayList<MessageListener>();
		this.socket = socket;

		addMessageListener(this);
		// ルームなどからは、MessageListenerを実装した後、Client.(通知を投げる先のクラス、ルームなど);でリスナーを追加できる。

		Thread thread = new Thread(this);
		thread.start();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void run() {
		try {
			// ユーザーの情報を取得する
			InputStream input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));

			OutputStream output = socket.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(output));

			int cp;
			
			while ((cp = reader.read()) !=-1) {//cpに読み込んだバイトのはじめの部分が-1でないかどうか｡-1は切断
				//-1でなければ次に進み､\0(null文字)がでるまでStringBufferに追加していく｡
				
				StringBuilder sb = new StringBuilder();
				sb.append((char) cp);
				//System.out.println(cp);
				
				while ((cp = reader.read()) > 0) {//0つまりnull文字のときと､-1つまり切断時は除く0から65535までなので｡
					/*
					 * The character read, as an integer in the range 0 to 65535 (0x00-0xffff), 
					 * or -1 if the end of the stream has been reached 
					 * */
					sb.append((char) cp);
				}

				System.out.println(new JSONObject(sb.toString()));
				sendMessageEvent(new JSONObject(sb.toString()));

			}

		} catch (IOException e) {
			// err.printStackTrace();
			System.out.println("通信エラー" + socket.getInetAddress());
		} catch (JSONException e) {// jsonのエラーもはくはず｡その場合は躊躇なく切断すべし｡
			System.out.println("JSONエラー" + socket.getInetAddress());
		} catch (Exception e) {
			System.out.println("エラー" + socket.getInetAddress());
		}
		try {
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// このユーザーが送信してきたメッセージイベントを受け､分析を行う｡
	public void messageThrow(MessageEvent e) {// クライアントへ送信する場合も受信する場合もこれを通す｡
		Client source = e.getUser();
		JSONObject json = e.getJSON();
		String type = e.getType();
		JSONObject data = e.getData();

		// String msgType = e.getName();
		// String msgValue = e.getValue();
		if (type == null) {
			return;
		}
		try {
			switch (type) {// クライアント自身が処理するコマンド｡ルームコマンドはルーム側が処理する
			case "message":
				// メッセージ､いまのところ全部に配信
				// System.out.println(data.getString("message"));
				// break;
			case "event":// テロップや画像の更新 //現在はそのまま全クライアントに返す設定
				for (Client user : server.getUsers()) {
					user.sendMessage(json);
				}
				break;
			case "close": // 閉じる
				close();
				break;
			case "msg": // 全員にメッセージを送信 //未実装
				for (Client user : server.getUsers()) {
					// user.sendMessage(e.getName() + " " + source.getName() +
					// ">"+ e.getValue());
				}
				break;
			case "setName": // 名前を変更する
				String name = data.getString("name");
				String before = getName();
				setName(name);
				sendStringMessage("successful setName");
				// 全員にメッセージを渡すため､下記になる｡これで上に再帰する
				reachedMessage(before + " から " + name + " に名前を変更しました");

				break;
			case "getUsers": // 名前を取得する //未実装
				/*
				 * String result = ""; for (Client user : server.getUsers())
				 * {//ユーザの数だけ繰り返す result += user.getName() + " ";
				 * sendMessage("users " + result); }
				 */
				break;
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public String toString() {
		return "NAME=" + getName();
	}

	public void close() throws IOException {
		server.removeUser(this);
		messageListeners.clear();
		socket.close();
	}

	public void sendStringMessage(String value) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", "message");
		json.put("data", new JSONObject());
		json.getJSONObject("data").put("message", value);
		sendMessage(json);
	}

	// このユーザーに指定されたメッセージを送信する
	public void sendMessage(JSONObject json) {
		try {
			// String message=json.toString();

			// メッセージの送信
			json.write(writer);
			System.out.println("out");

			writer.flush();
		} catch (Exception err) {
		}
	}

	// JSONを
	public void sendMessageEvent(JSONObject json) {
		MessageEvent event = new MessageEvent(this, json);
		for (MessageListener l : messageListeners) { // messageListenersから各MessageListenerを取得、それぞれに対して処理
			l.messageThrow(event); // ユーザが所属しているルームなどの区切りへ全て配信　所属の数=Listenerの数
			// ルームなどの概念を設置していない場合はクライアント､そのもののみなので､このクライアントのみにメッセージが届く
		}
	}

	// このユーザーが受け取ったメッセージを処理する //未使用､代わりにJSONに変換し､上記analyzeCommandに渡す
	public void reachedMessage(String value) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", "message");
		json.put("data", new JSONObject());
		json.getJSONObject("data").put("message", value);
		sendMessageEvent(json);
		/*
		 * MessageEvent event = new MessageEvent(this, name, value); for
		 * (MessageListener l : messageListeners) { //
		 * messageListenersから各MessageListenerを取得、それぞれに対して処理
		 * l.messageThrow(event); // ユーザが所属しているルームなどの区切りへ全て配信　所属の数=Listenerの数
		 * //ルームなどの概念を設置していない場合はクライアント､そのもののみなので､このクライアントのみにメッセージが届く }
		 */
	}

	// このオブジェクトにメッセージリスナを登録する
	public void addMessageListener(MessageListener l) {
		messageListeners.add(l);
	}

	// 指定したメッセージリスナをこのオブジェクトから解除する
	public void removeMessageListener(MessageListener l) {
		messageListeners.remove(l);
	}

	// このオブジェクトに登録されているメッセージリスナの配列を返す
	public MessageListener[] getMessageListeners() {
		MessageListener[] listeners = new MessageListener[messageListeners
				.size()];
		messageListeners.toArray(listeners);
		return listeners;
	}
}
