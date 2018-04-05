package com.fracturedskies.render.events

import com.fracturedskies.engine.jeact.Component
import com.fracturedskies.engine.jeact.Point
import com.fracturedskies.engine.jeact.event.Event

class Unfocus(target: Component<*>): Event(target)
class Focus(target: Component<*>): Event(target)
class Unhover(target: Component<*>): Event(target)
class Hover(target: Component<*>): Event(target)
class Scroll(target: Component<*>, val xOffset: Double, val yOffset: Double): Event(target)

class Click(target: Component<*>, val mousePos: Point, val button: Int, val action: Int, val mods: Int): Event(target)

class Key(target: Component<*>, val key: Int, val scancode: Int, val action: Int, val mods: Int) : Event(target)
class CharMods(target: Component<*>, val codepoint: Int, val mods: Int) : Event(target)