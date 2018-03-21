package com.fracturedskies.render.components.layout

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*

/**
 * Layout takes the `bounds` provided and divides the space with to child components
 */
class Layout(attributes: MultiTypeMap) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    // Flex Attributes
    val DIRECTION = TypedKey<Direction>("direction")
    val JUSTIFY_CONTENT = TypedKey<JustifyContent>("justifyContent")
    val ALIGN_ITEMS = TypedKey<ItemAlign>("itemAlign")
    val ALIGN_CONTENT = TypedKey<ContentAlign>("contentAlign")
    val WRAP = TypedKey<Wrap>("wrap")

    // Item Attributes
    val GROW = TypedKey<Double>("grow")
    val SHRINK = TypedKey<Double>("shrink")
    val ALIGN_SELF = TypedKey<ItemAlign>("alignSelf")

    fun Node.Builder<*>.layout(
        direction: Direction = Direction.ROW,
        justifyContent: JustifyContent = JustifyContent.LEFT,
        alignItems: ItemAlign = ItemAlign.START,
        alignContent: ContentAlign = ContentAlign.START,
        wrap: Wrap = Wrap.WRAP,
        block: Node.Builder<Unit>.() -> (Unit) = {}
    ) {
      nodes.add(Node(::Layout, MultiTypeMap(
              DIRECTION to direction,
              JUSTIFY_CONTENT to justifyContent,
              ALIGN_ITEMS to alignItems,
              ALIGN_CONTENT to alignContent,
              WRAP to wrap
      ), block))
    }

    /* Helper Functions */
    private fun Component<*>.cross(direction: Direction, width: Int, height: Int): Int {
      val preferredWidth = this.preferredWidth(width, height)
      val preferredHeight = this.preferredHeight(width, height)
      return direction.cross(preferredWidth, preferredHeight)
    }
    private fun Component<*>.main(direction: Direction, width: Int, height: Int): Int {
      val preferredWidth = this.preferredWidth(width, height)
      val preferredHeight = this.preferredHeight(width, height)
      return direction.main(preferredWidth, preferredHeight)
    }
  }

  /* Attributes */
  private val direction get() = requireNotNull(attributes[DIRECTION])
  private val justifyContent get() = requireNotNull(attributes[JUSTIFY_CONTENT])
  private val alignItems get() = requireNotNull(attributes[ALIGN_ITEMS])
  private val alignContent get() = requireNotNull(attributes[ALIGN_CONTENT])
  private val wrap get() = requireNotNull(attributes[WRAP])

  override fun preferredWidth(parentWidth: Int, parentHeight: Int): Int {
    val componentRows = wrap.split(children, direction, parentWidth, parentHeight)
    val maxMainSpace = maxMainSpace(componentRows, parentWidth, parentHeight)
    val maxCrossSpace = maxCrossSpace(componentRows, parentWidth, parentHeight)
    return direction.x(maxMainSpace, maxCrossSpace)
  }
  override fun preferredHeight(parentWidth: Int, parentHeight: Int): Int {
    val componentRows = wrap.split(children, direction, parentWidth, parentHeight)
    val maxMainSpace = maxMainSpace(componentRows, parentWidth, parentHeight)
    val maxCrossSpace = maxCrossSpace(componentRows, parentWidth, parentHeight)
    return direction.y(maxMainSpace, maxCrossSpace)
  }
  private fun maxCrossSpace(componentRows: List<List<Component<*>>>, parentWidth: Int, parentHeight: Int): Int {
    return componentRows.map({ componentRow->
      componentRow
              .map({ it.cross(direction, parentWidth, parentHeight) })
              .max() ?: 0
    }).sum()
  }
  private fun maxMainSpace(componentRows: List<List<Component<*>>>, parentWidth: Int, parentHeight: Int): Int {
    return componentRows.map({ componentRow->
      componentRow
              .map({ it.main(direction, parentWidth, parentHeight) })
              .sum()
    }).max() ?: 0
  }
  override fun componentFromPoint(point: Point): Component<*>? {
    return children.reversed().mapNotNull({ it.componentFromPoint(point) }).firstOrNull()
  }

  override fun render(bounds: Bounds) {
    this.bounds = bounds

    val mainAxisSize = direction.main(bounds.width, bounds.height)
    val crossAxisSpace = direction.cross(bounds.width, bounds.height)

    val componentRows = wrap.split(children, direction, bounds.width, bounds.height)
    val minCrossSpace = componentRows.sumBy({row ->
      row.map({it.cross(direction, bounds.width, bounds.height)}).max() ?: 0
    })

    val extraCrossSpace = crossAxisSpace - minCrossSpace
    var componentCrossOffset = alignContent.componentCrossOffset(componentRows, extraCrossSpace)
    val betweenCrossOffset = alignContent.betweenCrossOffset(componentRows, extraCrossSpace)
    val additionalCrossSpace = alignContent.additionalCrossSpace(componentRows, extraCrossSpace)
    for (componentRow in componentRows) {
      val rowMainSpace = componentRow
              .map({ it.main(direction, bounds.width, bounds.height) })
              .sum()
      var rowCrossSpace = componentRow
              .map({ it.cross(direction, bounds.width, bounds.height) })
              .max() ?: 0
      rowCrossSpace += additionalCrossSpace

      val extraMainSpace = if (mainAxisSize > rowMainSpace) mainAxisSize - rowMainSpace else 0
      val totalGrow = componentRow.sumByDouble({it.attributes[GROW] ?: 0.0 })
      val growCoefficient = if (totalGrow > 0.0) extraMainSpace / totalGrow else 0.0

      val missingMainSpace = if (mainAxisSize < rowMainSpace) rowMainSpace - mainAxisSize else 0
      val totalShrink = componentRow.sumByDouble({ it.attributes[SHRINK] ?: 0.0 })
      val shrinkCoefficient = if (totalShrink > 0.0) missingMainSpace / totalShrink else 0.0

      val initialMainOffset = justifyContent.initialOffset(componentRow, if (growCoefficient > 0.0) 0 else extraMainSpace)
      val betweenMainOffset = justifyContent.betweenOffset(componentRow, if (growCoefficient > 0.0) 0 else extraMainSpace)

      var componentMainOffset = initialMainOffset
      val componentIterator = direction.iterator(componentRow)
      while (componentIterator.hasNext()) {
        val component = componentIterator.next()

        val grow = growCoefficient * (component.attributes[GROW] ?: 0.0)
        val shrink = shrinkCoefficient * (component.attributes[SHRINK] ?: 0.0)
        val componentMainSpace = (component.main(direction, bounds.width, bounds.height) + grow - shrink).toInt()
        val componentCrossSpace = component.cross(direction, bounds.width, bounds.height)

        val alignSelf = component.attributes[ALIGN_SELF] ?: alignItems
        val crossSpace = alignSelf.cross(componentCrossSpace, rowCrossSpace)
        val crossOffset = alignSelf.offset(componentCrossSpace, rowCrossSpace)
        component.render(Bounds(
                bounds.x + direction.x(componentMainOffset, componentCrossOffset + crossOffset),
                bounds.y + direction.y(componentMainOffset, componentCrossOffset + crossOffset),
                direction.x(componentMainSpace, crossSpace),
                direction.y(componentMainSpace, crossSpace)
        ))
        componentMainOffset += componentMainSpace + betweenMainOffset
      }
      componentCrossOffset += rowCrossSpace + betweenCrossOffset
    }
  }
}