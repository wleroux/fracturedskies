package fs.client.system;

import fs.client.event.TickEvent;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class FpsSystem {

  private long lastFps = System.nanoTime();
  private int ticks = 0;
  private static final long ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

  public void onTick(@Observes TickEvent event) {
    long now = System.nanoTime();
    if (lastFps + ONE_SECOND_IN_NANOSECONDS < now) {
      System.out.println("FPS: " + ticks);
      ticks = 0;
      lastFps = now;
    } else {
      ticks++;
    }
  }
}
