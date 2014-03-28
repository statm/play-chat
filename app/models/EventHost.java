package models;

import java.util.List;

import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

public class EventHost {
	
	private ArchivedEventStream<Event> stream = new ArchivedEventStream<Event>(100);
	
	public void say() {
		Event event = new Event();
		this.stream.publish(event);
	}
	
	public Promise<List<IndexedEvent<Event>>> nextMessages(long lastID) {
		return this.stream.nextEvents(lastID);
	}
	
	
	public static class Event {
		public Long time;

		public Event() {
			this.time = System.currentTimeMillis();
		}
	}

	private static EventHost instance = null;
	public static EventHost get() {
		if (instance == null) {
			instance = new EventHost();
		}
		return instance;
	}
}
