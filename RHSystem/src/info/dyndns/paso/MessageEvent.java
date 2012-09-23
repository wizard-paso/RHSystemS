package info.dyndns.paso;

import java.util.EventObject;

import org.json.JSONException;
import org.json.JSONObject;

class MessageEvent extends EventObject {//�C�x���g�ԂŒl�����Ƃ肷�邽�߂̃N���X
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
			this.type=json.getString("type");//�Ȃ����null�̂܂܂ɂȂ�
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		try {
			this.data=json.getJSONObject("data");//��L���l�Ȃ����null�̂܂ܡ
		} catch (JSONException e) {
			//e.printStackTrace();
		}
	}

	// �C�x���g�𔭐����������[�U�[
	public Client getUser() {
		return source;
	}
	// json��Ԃ�
	public JSONObject getJSON() {
		return this.json;
	}
	public String getType(){
		return this.type;
	}
	public JSONObject getData(){
		return this.data;
	}
/*	���g�p	json����͂��Ė��O���o�����\�b�h�͎�������?
	// ���̃C�x���g�̃R�}���h����Ԃ�
	public String getName() {
		return this.name;
	}

	// ���̃C�x���g��
	public String getValue() {
		return this.value;
	}
*/
}