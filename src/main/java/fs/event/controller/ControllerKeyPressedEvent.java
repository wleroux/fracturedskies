package fs.event.controller;

import fs.controller.Controller;
import lombok.Value;

public @Value class ControllerKeyPressedEvent {
  Controller.Key key;
}
