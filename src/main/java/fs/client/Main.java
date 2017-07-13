package fs.client;

import fs.client.event.*;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Main implements Runnable {

  private boolean isTerminated = false;

  @Inject
  private Event<Object> events;

  public Main() {
  }

  public static void main(String[] args) {
    Weld weld = new Weld();
    try (WeldContainer container = weld.initialize()) {
      container.select(Main.class).get().run();
    }
  }

  @Override
  public void run() {
    events.fire(new GamePreInitializationEvent());
    events.fire(new GameInitializationEvent());

    while (!isTerminated) {
      events.fire(new TickEvent());
      events.fire(new RenderEvent());
    }
  }

  public void onTerminatedEvent(@Observes TerminatedEvent event) {
    isTerminated = true;
  }

  @Override
  public String toString() {
    return "main";
  }
}
