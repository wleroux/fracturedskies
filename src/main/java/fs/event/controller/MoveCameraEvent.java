package fs.event.controller;

import lombok.Value;

public @Value class MoveCameraEvent {
  float pitch;
  float yaw;
}
