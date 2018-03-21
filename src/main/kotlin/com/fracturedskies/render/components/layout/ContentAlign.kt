package com.fracturedskies.render.components.layout

import com.fracturedskies.engine.jeact.Component

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