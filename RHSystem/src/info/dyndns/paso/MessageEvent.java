package info.dyndns.paso;

import java.util.EventObject;

import org.json.JSONException;
import org.json.JSONObject;

class MessageEvent extends EventObject {//イベント間で値をやりとりするためのクラス
	private Client source;
	private JSONObject json;
	private String type;
	private JSONObject data;
	
	//private String name;
	//private String value;

	public MessageEvent(Client source, JSONObject json) {
		super(source);
		this.source = source;
		this.json=json;
		try {
			this.type=json.getString("type");//なければnullのままになる｡
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		try {
			this.data=json.getJSONObject("data");//上記同様なければnullのまま｡
		} catch (JSONException e) {
			//e.printStackTrace();
		}
	}

	// イベントを発生させたユーザー
	public Client getUser() {
		return source;
	}
	// jsonを返す
	public JSONObject getJSON() {
		return this.json;
	}
	public String getType(){
		return this.type;
	}
	public JSONObject getData(){
		return this.data;
	}
/*	未使用	jsonを解析して名前を出すメソッドは実装する?
	// このイベントのコマンド名を返す
	public String getName() {
		return this.name;
	}

	// このイベントの
	public String getValue() {
		return this.value;
	}
*/
}