package com.fracturedskies.game.render.components.layout

import com.fracturedskies.engine.jeact.Component


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