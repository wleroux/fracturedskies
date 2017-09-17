package fs.event.controller;

import fs.util.math.Vector3;
import lombok.Value;

public @Value class MouseClickEvent {
  int button;
  Vector3 rayStart;
  Vector3 rayEnd;
}
