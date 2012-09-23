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

//�`���b�g���[�U�[�I�u�W�F�N�g
class Client implements Runnable, MessageListener {
	// �\�P�b�g
	private Socket socket;

	// ���[�U�[�̖��O
	private String name;

	// �`���b�g�T�[�o�[
	private RHSystem server = RHSystem.getInstance();

	// ���b�Z�[�W���X�i�̓��I�z��
	// ���b�Z�[�W���X�i�͂��̃��[�U�[�����������Ƃ��ɌĂяo�����C�x���g
	private ArrayList<MessageListener> messageListeners;

	private BufferedReader reader;
	private BufferedWriter writer;

	public Client(Socket socket) {
		messageListeners = new ArrayList<MessageListener>();
		this.socket = socket;

		addMessageListener(this);
		// ���[���Ȃǂ���́AMessageListener������������AClient.(�ʒm�𓊂����̃N���X�A���[���Ȃ�);�Ń��X�i�[��ǉ��ł���B

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
			// ���[�U�[�̏����擾����
			InputStream input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));

			OutputStream output = socket.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(output));

			int cp;
			
			while ((cp = reader.read()) !=-1) {//cp�ɓǂݍ��񂾃o�C�g�̂͂��߂̕�����-1�łȂ����ǂ����-1�͐ؒf
				//-1�łȂ���Ύ��ɐi�ݤ\0(null����)���ł�܂�StringBuffer�ɒǉ����Ă����
				
				StringBuilder sb = new StringBuilder();
				sb.append((char) cp);
				//System.out.println(cp);
				
				while ((cp = reader.read()) > 0) {//0�܂�null�����̂Ƃ��Ƥ-1�܂�ؒf���͏���0����65535�܂łȂ̂š
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
			System.out.println("�ʐM�G���[" + socket.getInetAddress());
		} catch (JSONException e) {// json�̃G���[���͂��͂�����̏ꍇ���S�O�Ȃ��ؒf���ׂ��
			System.out.println("JSON�G���[" + socket.getInetAddress());
		} catch (Exception e) {
			System.out.println("�G���[" + socket.getInetAddress());
		}
		try {
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ���̃��[�U�[�����M���Ă������b�Z�[�W�C�x���g���󂯤���͂��s���
	public void messageThrow(MessageEvent e) {// �N���C�A���g�֑��M����ꍇ����M����ꍇ�������ʂ��
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
			switch (type) {// �N���C�A���g���g����������R�}���h����[���R�}���h�̓��[��������������
			case "message":
				// ���b�Z�[�W����܂̂Ƃ���S���ɔz�M
				// System.out.println(data.getString("message"));
				// break;
			case "event":// �e���b�v��摜�̍X�V //���݂͂��̂܂ܑS�N���C�A���g�ɕԂ��ݒ�
				for (Client user : server.getUsers()) {
					user.sendMessage(json);
				}
				break;
			case "close": // ����
				close();
				break;
			case "msg": // �S���Ƀ��b�Z�[�W�𑗐M //������
				for (Client user : server.getUsers()) {
					// user.sendMessage(e.getName() + " " + source.getName() +
					// ">"+ e.getValue());
				}
				break;
			case "setName": // ���O��ύX����
				String name = data.getString("name");
				String before = getName();
				setName(name);
				sendStringMessage("successful setName");
				// �S���Ƀ��b�Z�[�W��n�����ߤ���L�ɂȂ顂���ŏ�ɍċA����
				reachedMessage(before + " ���� " + name + " �ɖ��O��ύX���܂���");

				break;
			case "getUsers": // ���O���擾���� //������
				/*
				 * String result = ""; for (Client user : server.getUsers())
				 * {//���[�U�̐������J��Ԃ� result += user.getName() + " ";
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

	// ���̃��[�U�[�Ɏw�肳�ꂽ���b�Z�[�W�𑗐M����
	public void sendMessage(JSONObject json) {
		try {
			// String message=json.toString();

			// ���b�Z�[�W�̑��M
			json.write(writer);
			System.out.println("out");

			writer.flush();
		} catch (Exception err) {
		}
	}

	// JSON��
	public void sendMessageEvent(JSONObject json) {
		MessageEvent event = new MessageEvent(this, json);
		for (MessageListener l : messageListeners) { // messageListeners����eMessageListener���擾�A���ꂼ��ɑ΂��ď���
			l.messageThrow(event); // ���[�U���������Ă��郋�[���Ȃǂ̋�؂�֑S�Ĕz�M�@�����̐�=Listener�̐�
			// ���[���Ȃǂ̊T�O��ݒu���Ă��Ȃ��ꍇ�̓N���C�A���g����̂��݂̂̂Ȃ̂Ť���̃N���C�A���g�݂̂Ƀ��b�Z�[�W���͂�
		}
	}

	// ���̃��[�U�[���󂯎�������b�Z�[�W���������� //���g�p������JSON�ɕϊ������LanalyzeCommand�ɓn��
	public void reachedMessage(String value) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", "message");
		json.put("data", new JSONObject());
		json.getJSONObject("data").put("message", value);
		sendMessageEvent(json);
		/*
		 * MessageEvent event = new MessageEvent(this, name, value); for
		 * (MessageListener l : messageListeners) { //
		 * messageListeners����eMessageListener���擾�A���ꂼ��ɑ΂��ď���
		 * l.messageThrow(event); // ���[�U���������Ă��郋�[���Ȃǂ̋�؂�֑S�Ĕz�M�@�����̐�=Listener�̐�
		 * //���[���Ȃǂ̊T�O��ݒu���Ă��Ȃ��ꍇ�̓N���C�A���g����̂��݂̂̂Ȃ̂Ť���̃N���C�A���g�݂̂Ƀ��b�Z�[�W���͂� }
		 */
	}

	// ���̃I�u�W�F�N�g�Ƀ��b�Z�[�W���X�i��o�^����
	public void addMessageListener(MessageListener l) {
		messageListeners.add(l);
	}

	// �w�肵�����b�Z�[�W���X�i�����̃I�u�W�F�N�g�����������
	public void removeMessageListener(MessageListener l) {
		messageListeners.remove(l);
	}

	// ���̃I�u�W�F�N�g�ɓo�^����Ă��郁�b�Z�[�W���X�i�̔z���Ԃ�
	public MessageListener[] getMessageListeners() {
		MessageListener[] listeners = new MessageListener[messageListeners
				.size()];
		messageListeners.toArray(listeners);
		return listeners;
	}
}
