package com.fracturedskies.render.components.layout

import com.fracturedskies.engine.jeact.Component

enum class Direction {
  ROW {
    override fun main(x: Int, y: Int) = x
    override fun cross(x: Int, y: Int) = y
    override fun x(main: Int, cross: Int) = main
    override fun y(main: Int, cross: Int) = cross
    override fun iterator(componentRow: List<Component<*>>) = componentRow.iterator()
  },
  ROW_REVERSE {
    override fun main(x: Int, y: Int) = x
    override fun cross(x: Int, y: Int) = y
    override fun x(main: Int, cross: Int) = main
    override fun y(main: Int, cross: Int) = cross
    override fun iterator(componentRow: List<Component<*>>) = componentRow.reversed().iterator()
  },
  COLUMN {
    override fun main(x: Int, y: Int) = y
    override fun cross(x: Int, y: Int) = x
    override fun x(main: Int, cross: Int) = cross
    override fun y(main: Int, cross: Int) = main
    override fun iterator(componentRow: List<Component<*>>) = componentRow.iterator()
  },
  COLUMN_REVERSE {
    override fun main(x: Int, y: Int) = y
    override fun cross(x: Int, y: Int) = x
    override fun x(main: Int, cross: Int) = cross
    override fun y(main: Int, cross: Int) = main
    override fun iterator(componentRow: List<Component<*>>) = componentRow.reversed().iterator()
  };

  abstract fun main(x: Int, y: Int): Int
  abstract fun cross(x: Int, y: Int): Int
  abstract fun x(main: Int, cross: Int): Int
  abstract fun y(main: Int, cross: Int): Int
  abstract fun iterator(componentRow: List<Component<*>>): Iterator<Component<*>>
}