package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import play.libs.F.*;

public class NotificationHost {

	public static final int EVENT_POLL_INTERVAL = 10000;
	public static final int CHAT_POLL_INTERVAL = 500;

	public EventStream stream;

	private NotificationHost() {
		stream = new EventStream();
	}

	// stream
	public static class EventStream {
		private static Timer pollIntervalTimer = new Timer("EventStream.Timer", true);
		private static TimerTask pollIntervalTimerTask;
		private Long lastPublishTime = 0L;

		private ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<Event>();
		private List<Promise<Event[]>> waiting = Collections.synchronizedList(new ArrayList<Promise<Event[]>>());

		public void addEvent(Event event) {
			events.offer(event);
			
			Long currentTime = System.currentTimeMillis();
			
			if (currentTime - lastPublishTime > EVENT_POLL_INTERVAL
				|| event.type == EventType.Chat) {
				publishEvents();
			} else {
				if (pollIntervalTimerTask != null) {
					pollIntervalTimerTask.cancel();
				}
				
				pollIntervalTimerTask = new TimerTask() {

					@Override
					public void run() {
						publishEvents();
					}
				};
				
				// pollIntervalTimer.schedule(pollIntervalTimerTask, lastPublishTime + EVENT_POLL_INTERVAL - currentTime);
			}
		}

		public synchronized Promise<Event[]> getEvents() {
			Promise<Event[]> promise = new Promise<Event[]>();
			waiting.add(promise);
			return promise;
		}

		private void publishEvents() {
			if (waiting.size() == 0) {
				return;
			}

			Event[] array = events.toArray(new Event[events.size()]);
			for (Promise<Event[]> promise : waiting) {
				promise.invoke(array.clone());
			}
			events.clear();
			waiting.clear();
			lastPublishTime = System.currentTimeMillis();
			if (pollIntervalTimerTask != null) {
				pollIntervalTimerTask.cancel();
				pollIntervalTimerTask = null;
			}
		}
	}

	// events
	private static enum EventType {
		Notification, Chat
	}

	public static abstract class Event {
		public String text;
		public Long time;
		public EventType type;

		public Event(String text) {
			this.text = text;
			this.time = System.currentTimeMillis();
		}
	}

	public static class Notification extends Event {
		public Notification(String text) {
			super(text);
			this.type = EventType.Notification;
		}
	}

	public static class Chat extends Event {
		public Chat(String text) {
			super(text);
			this.type = EventType.Chat;
		}
	}

	// singleton
	private static NotificationHost instance = null;

	public static NotificationHost get() {
		if (instance == null) {
			instance = new NotificationHost();
		}
		return instance;
	}
}
