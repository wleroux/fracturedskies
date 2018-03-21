package com.fracturedskies.render.components.layout

import com.fracturedskies.engine.jeact.Component
import java.util.*


enum class Wrap {
  WRAP {
    override fun split(components: List<Component<*>>, direction: Direction, width: Int, height: Int): List<List<Component<*>>> {
      val mainAxisSize = direction.main(width, height)
      val componentRows = ArrayList<List<Component<*>>>()
      var currentComponentIndex = 0
      while (currentComponentIndex < components.size) {
        val componentRow = mutableListOf<Component<*>>()

        var cumulativeMainAxisSize = 0
        while (currentComponentIndex < components.size) {
          val component = components[currentComponentIndex]
          val componentMainSize = direction.main(component.preferredWidth(width, height), component.preferredHeight(width, height))
          if (cumulativeMainAxisSize + componentMainSize > mainAxisSize && componentRow.size > 0) {
            break
          } else {
            cumulativeMainAxisSize += componentMainSize
            componentRow.add(component)
            currentComponentIndex++
          }
        }
        componentRows.add(componentRow)
      }

      return componentRows
    }
  },
  NO_WRAP {
    override fun split(components: List<Component<*>>, direction: Direction, width: Int, height: Int): List<List<Component<*>>> {
      val componentRows = ArrayList<List<Component<*>>>()
      componentRows.add(components)
      return componentRows
    }
  };

  abstract fun split(components: List<Component<*>>, direction: Direction, width: Int, height: Int): List<List<Component<*>>>
}
