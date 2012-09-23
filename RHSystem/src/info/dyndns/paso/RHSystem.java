package info.dyndns.paso;

import java.net.*;
import java.util.*;
import java.io.*;



/**
 * RHSystem �T�[�o
 * @author wizard_paso
 * @author http://codezine.jp/
 */

public class RHSystem {
	public static void main(String[] args) {
		RHSystem application = RHSystem.getInstance();
		
		application.start();
	}

	// �T�[�o�[�̓V���O���g���݌v�B
	private static RHSystem instance;

	public static RHSystem getInstance() {
		if (instance == null) {
			instance = new RHSystem();
		}
		return instance;
	}

	// �T�[�o�[�\�P�b�g
	private ServerSocket server;

	// ���݃`���b�g�ɎQ�����Ă���S���[�U�[�̓��I�z��
	private ArrayList<Client> userList;

	private RHSystem() {
		userList = new ArrayList<Client>();
		Admin admin=Admin.getInstance(this);
	}

	// main ���\�b�h����Ăяo�����
	public void start() {
		try {
			server = new ServerSocket(8080);

			while (!server.isClosed()) {
				// �V�����N���C�A���g�̐ڑ���҂�
				Socket client = server.accept();

				// ���[�U�[�I�u�W�F�N�g�𐶐�����
				Client user = new Client(client);
				addUser(user);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	// ���[�U�[��ǉ�����
	public void addUser(Client user) {
		if (userList.contains(user))
			return;

		userList.add(user);
		System.out.println("addUser:" + user);
	}

	// �w�肵�����O�̃��[�U�[���擾����
	public Client getUser(String name) {
		for (int i = 0; i < userList.size(); i++) {
			Client user = userList.get(i);
			if (user.getName().equals(name))
				return user;
		}
		return null;
	}

	// ���ׂẴ��[�U�[��Ԃ�
	public Client[] getUsers() {
		Client[] users = new Client[userList.size()];
		userList.toArray(users);
		return users;
	}

	// ���[�U�[���폜����
	public void removeUser(Client user) {
		userList.remove(user);
		System.out.println("removeUser:" + user);
	}

	// ���ׂẴ��[�U�[���폜����
	public void clearUser() {
		userList.clear();
	}

	// �T�[�o�[����Đؒf����
	public void close() throws IOException {
		server.close();
	}
}






