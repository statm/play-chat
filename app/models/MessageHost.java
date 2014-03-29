package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import play.libs.F.Promise;

public class MessageHost {

	// TODO: message interval
	public static final int NOTIFICTION_INTERVAL = 5000;
	public static final int CHAT_INTERVAL = 500;

	private static final HashMap<String, MessageStream> streamMap = new HashMap<String, MessageStream>();

	public static MessageStream getMessageStream(String userID) {
		MessageStream stream = streamMap.get(userID);
		if (stream == null) {
			stream = new MessageStream();
			streamMap.put(userID, stream);
		}
		return stream;
	}


	// ========================================
	// Class: MessageStream
	// ========================================

	public static class MessageStream {

		final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();
		final List<MessageFilter> waiting = Collections.synchronizedList(new ArrayList<MessageFilter>());

		public synchronized Promise<List<Message>> getMessages(long lastMessageID) {
			MessageFilter filter = new MessageFilter(lastMessageID);
			waiting.add(filter);
			notifyNewMessages();
			return filter;
		}

		// ?
		public synchronized List<Message> availableMessages(long lastMessageID) {
			List<Message> result = new ArrayList<Message>();
			for (Message message : messages) {
				if (message.id > lastMessageID) {
					result.add(message);
				}
			}
			return result;
		}

		// ?
		public List<Message> archive() {
			List<Message> result = new ArrayList<Message>();
			for (Message message : messages) {
				result.add(message);
			}
			return result;
		}

		public synchronized void addMessage(Message message) {
			messages.offer(message);
			notifyNewMessages();
		}

		void notifyNewMessages() {
			for (ListIterator<MessageFilter> it = waiting.listIterator(); it.hasNext();) {
				MessageFilter filter = it.next();
				for (Message message : messages) {
					filter.propose(message);
				}
				if (filter.trigger()) {
					it.remove();
				}
			}
		}

		static class MessageFilter extends Promise<List<Message>> {

			final Long lastMessageID;
			final List<Message> newMessages = new ArrayList<Message>();

			public MessageFilter(Long lastMessageID) {
				this.lastMessageID = lastMessageID;
			}

			public void propose(Message message) {
				if (message.id > lastMessageID) {
					newMessages.add(message);
				}
			}

			public boolean trigger() {
				if (newMessages.isEmpty()) {
					return false;
				}
				invoke(newMessages);
				return true;
			}
		}
	}


	// ========================================
	// Message Classes
	// ========================================

	public static abstract class Message {
		private static final AtomicLong idGenerator = new AtomicLong(1);

		public Date time;
		public Long id;
		public MessageType type;
		// =
		public String content;

		public Message() {
			this.time = new Date();
			this.id = idGenerator.getAndIncrement();
		}
	}

	public static enum MessageType {
		NOTIFICATION, CHAT
	}

	public static class Notification extends Message {
		public Notification() {
			super();
			this.type = MessageType.NOTIFICATION;
			this.content = "Notification";
		}
	}

	public static class Chat extends Message {
		public Chat() {
			super();
			this.type = MessageType.CHAT;
			this.content = "Chat";
		}
	}
}
