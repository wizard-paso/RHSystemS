package info.dyndns.paso;

import java.util.EventListener;

interface MessageListener extends EventListener {
	void messageThrow(MessageEvent e);
}