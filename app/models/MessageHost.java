package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import play.libs.F.Promise;

public class MessageHost {

	// TODO: message interval
	public static final int NOTIFICTION_INTERVAL = 5000;
	public static final int CHAT_INTERVAL = 500;

	private MessageStream<Message> stream = new MessageStream<Message>();

	// ====================
	// public methods
	// ====================
	public void addMessage() {
		Message msg = new Notification();
		this.stream.publish(msg);
	}

	public Promise<List<Message>> getMessages(long lastID) {
		return this.stream.nextMessages(lastID);
	}


	// ====================
	// singleton
	// ====================
	private static MessageHost instance = null;

	public static MessageHost get() {
		if (instance == null) {
			instance = new MessageHost();
		}
		return instance;
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


	// ========================================
	// Class: MessageStream
	// ========================================

	public static class MessageStream<T extends Message> {

		final ConcurrentLinkedQueue<T> messages = new ConcurrentLinkedQueue<T>();
		final List<FilterTask<T>> waiting = Collections.synchronizedList(new ArrayList<FilterTask<T>>());

		public synchronized Promise<List<T>> nextMessages(long lastEventSeen) {
			FilterTask<T> filter = new FilterTask<T>(lastEventSeen);
			waiting.add(filter);
			notifyNewMessages();
			return filter;
		}

		// ?
		public synchronized List<T> availableMessages(long lastEventSeen) {
			List<T> result = new ArrayList<T>();
			for (T message : messages) {
				if (message.id > lastEventSeen) {
					result.add(message);
				}
			}
			return result;
		}

		// ?
		public List<T> archive() {
			List<T> result = new ArrayList<T>();
			for (T event : messages) {
				result.add(event);
			}
			return result;
		}

		public synchronized void publish(T event) {
			messages.offer(event);
			notifyNewMessages();
		}

		void notifyNewMessages() {
			for (ListIterator<FilterTask<T>> it = waiting.listIterator(); it.hasNext();) {
				FilterTask<T> filter = it.next();
				for (T message : messages) {
					filter.propose(message);
				}
				if (filter.trigger()) {
					it.remove();
				}
			}
		}

		static class FilterTask<K extends Message> extends Promise<List<K>> {

			final Long lastEventSeen;
			final List<K> newEvents = new ArrayList<K>();

			public FilterTask(Long lastEventSeen) {
				this.lastEventSeen = lastEventSeen;
			}

			public void propose(K event) {
				if (event.id > lastEventSeen) {
					newEvents.add(event);
				}
			}

			public boolean trigger() {
				if (newEvents.isEmpty()) {
					return false;
				}
				invoke(newEvents);
				return true;
			}
		}
	}
}
