package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Component
import com.fracturedskies.engine.jeact.Node
import java.util.*


class Flex(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val DIRECTION = Key<Direction>("direction")
    val JUSTIFY_CONTENT = Key<JustifyContent>("justifyContent")
    val ALIGN_ITEMS = Key<ItemAlign>("itemAlign")
    val ALIGN_CONTENT = Key<ContentAlign>("contentAlign")
    val WRAP = Key<Wrap>("wrap")
    fun Node.Builder<*>.flex(
            direction: Direction = Direction.ROW,
            justifyContent: JustifyContent = JustifyContent.LEFT,
            alignItems: ItemAlign = ItemAlign.START,
            alignContent: ContentAlign = ContentAlign.START,
            wrap: Wrap = Wrap.WRAP,
            block: Node.Builder<Unit>.() -> (Unit) = {}
    ) {
      nodes.add(Node(::Flex, Context(
              DIRECTION to direction,
              JUSTIFY_CONTENT to justifyContent,
              ALIGN_ITEMS to alignItems,
              ALIGN_CONTENT to alignContent,
              WRAP to wrap
      ), block))
    }
  }

  private val direction get() = requireNotNull(attributes[DIRECTION])
  private val justifyContent get() = requireNotNull(attributes[JUSTIFY_CONTENT])
  private val alignItems get() = requireNotNull(attributes[ALIGN_ITEMS])
  private val alignContent get() = requireNotNull(attributes[ALIGN_CONTENT])
  private val wrap get() = requireNotNull(attributes[WRAP])

  override fun preferredWidth(): Int {
    return children.sumBy({ it.preferredWidth() })
  }

  override fun preferredHeight(): Int {
    return children.sumBy({ it.preferredHeight() })
  }

  override fun render(bounds: Bounds) {
    this.bounds = bounds

    val mainAxisSize = direction.main(bounds.width, bounds.height)
    val componentRows = wrap.split(children, direction, bounds.width, bounds.height)

    var usedCrossSpace = 0
    for (componentRow in componentRows) {
      val rowCrossSpace = componentRow
              .map({ direction.cross(it.preferredWidth(), it.preferredHeight()) })
              .max()
              ?: 0
      usedCrossSpace += rowCrossSpace
    }
    val crossAxisSpace = direction.cross(bounds.width, bounds.height)
    val extraCrossSpace = crossAxisSpace - usedCrossSpace

    var componentCrossOffset = alignContent.componentCrossOffset(componentRows, extraCrossSpace)
    val betweenCrossOffset = alignContent.betweenCrossOffset(componentRows, extraCrossSpace)
    val additionalCrossSpace = alignContent.additionalCrossSpace(componentRows, extraCrossSpace)
    for (componentRow in componentRows) {
      var rowMainSpace = 0
      var rowCrossSpace = 0
      for (component in componentRow) {
        rowMainSpace += direction.main(component.preferredWidth(), component.preferredHeight())
        val componentCrossSpace = direction.cross(component.preferredWidth(), component.preferredHeight())
        if (rowCrossSpace < componentCrossSpace) {
          rowCrossSpace = componentCrossSpace
        }
      }
      rowCrossSpace += additionalCrossSpace

      val extraMainSpace = if (mainAxisSize < rowMainSpace) 0 else mainAxisSize - rowMainSpace
      val mainAxisCoefficient = if (rowMainSpace < mainAxisSize) 1f else mainAxisSize.toFloat() / rowMainSpace.toFloat()
      val initialMainOffset = justifyContent.initialOffset(componentRow, extraMainSpace)
      val betweenMainOffset = justifyContent.betweenOffset(componentRow, extraMainSpace)

      var componentMainOffset = initialMainOffset
      var maxCrossSpace = 0
      val componentIterator = direction.iterator(componentRow)
      while (componentIterator.hasNext()) {
        val component = componentIterator.next()
        val componentMainSpace = (direction.main(component.preferredWidth(), component.preferredHeight()) * mainAxisCoefficient).toInt()
        val componentCrossSpace = direction.cross(component.preferredWidth(), component.preferredHeight())

        val initialCrossOffset = alignItems.offset(componentCrossSpace, rowCrossSpace)
        val cross = alignItems.cross(componentCrossSpace, rowCrossSpace)

        component.render(Bounds(
                bounds.x + direction.x(componentMainOffset, componentCrossOffset + initialCrossOffset),
                bounds.y + direction.y(componentMainOffset, componentCrossOffset + initialCrossOffset),
                direction.x(componentMainSpace, cross),
                direction.y(componentMainSpace, cross)
        ))

        componentMainOffset += componentMainSpace + betweenMainOffset
        if (maxCrossSpace < cross) {
          maxCrossSpace = cross
        }
      }

      componentCrossOffset += maxCrossSpace + betweenCrossOffset
    }
  }
}

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
  COLUMN_RESVERSE {
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
          val componentMainSize = direction.main(component.preferredWidth(), component.preferredHeight())
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

enum class JustifyContent {
  LEFT {
    override fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = 0
    override fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = 0
  },
  CENTER {
    override fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = extraMainSpace / 2
    override fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = 0
  },
  RIGHT {
    override fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = extraMainSpace
    override fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = 0
  },
  SPACE_BETWEEN {
    override fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = 0
    override fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int): Int {
      return if (componentRow.size <= 1) 0 else extraMainSpace / (componentRow.size - 1)
    }
  },
  SPACE_AROUND {
    override fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = betweenOffset(componentRow, extraMainSpace) / 2
    override fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int) = extraMainSpace / componentRow.size
  };

  abstract fun initialOffset(componentRow: List<Component<*>>, extraMainSpace: Int): Int
  abstract fun betweenOffset(componentRow: List<Component<*>>, extraMainSpace: Int): Int
}

enum class ItemAlign {
  START {
    override fun cross(columnCrossSpace: Int, rowCrossSpace: Int) = columnCrossSpace
    override fun offset(columnCrossSpace: Int, rowCrossSpace: Int) = 0
  },
  END{
    override fun cross(columnCrossSpace: Int, rowCrossSpace: Int) = columnCrossSpace
    override fun offset(columnCrossSpace: Int, rowCrossSpace: Int) = rowCrossSpace - columnCrossSpace
  },
  CENTER{
    override fun cross(columnCrossSpace: Int, rowCrossSpace: Int) = columnCrossSpace
    override fun offset(columnCrossSpace: Int, rowCrossSpace: Int) = (rowCrossSpace - columnCrossSpace) / 2
  },
  STRETCH{
    override fun cross(columnCrossSpace: Int, rowCrossSpace: Int) = rowCrossSpace
    override fun offset(columnCrossSpace: Int, rowCrossSpace: Int) = 0
  };

  abstract fun cross(columnCrossSpace: Int, rowCrossSpace: Int): Int
  abstract fun offset(columnCrossSpace: Int, rowCrossSpace: Int): Int
}

enum class ContentAlign {
  START {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
  },
  END {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = extraCrossSpace
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
  },
  CENTER {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = extraCrossSpace / 2
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
  },
  STRETCH {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) =
            extraCrossSpace / componentRows.size
  },
  SPACE_BETWEEN {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) =
            if (componentRows.size <= 1) 0 else extraCrossSpace / (componentRows.size - 1)
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
  },
  SPACE_AROUND {
    override fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) =
            betweenCrossOffset(componentRows, extraCrossSpace) / 2
    override fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) =
            extraCrossSpace / componentRows.size
    override fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int) = 0
  };

  abstract fun componentCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int): Int
  abstract fun betweenCrossOffset(componentRows: List<List<Component<*>>>, extraCrossSpace: Int): Int
  abstract fun additionalCrossSpace(componentRows: List<List<Component<*>>>, extraCrossSpace: Int): Int
}