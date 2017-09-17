package fs.event.controller;

import fs.controller.Controller;
import lombok.Value;

public @Value class ControllerKeyReleasedEvent {
  Controller.Key key;
}
