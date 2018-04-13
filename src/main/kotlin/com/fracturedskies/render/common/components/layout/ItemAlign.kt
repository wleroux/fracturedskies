package com.fracturedskies.render.common.components.layout

enum class ItemAlign {
  START {
    override fun cross(componentCrossSpace: Int, rowCrossSpace: Int) = componentCrossSpace
    override fun offset(componentCrossSpace: Int, rowCrossSpace: Int) = 0
  },
  END{
    override fun cross(componentCrossSpace: Int, rowCrossSpace: Int) = componentCrossSpace
    override fun offset(componentCrossSpace: Int, rowCrossSpace: Int) = rowCrossSpace - componentCrossSpace
  },
  CENTER{
    override fun cross(componentCrossSpace: Int, rowCrossSpace: Int) = componentCrossSpace
    override fun offset(componentCrossSpace: Int, rowCrossSpace: Int) = (rowCrossSpace - componentCrossSpace) / 2
  },
  STRETCH{
    override fun cross(componentCrossSpace: Int, rowCrossSpace: Int) = rowCrossSpace
    override fun offset(componentCrossSpace: Int, rowCrossSpace: Int) = 0
  };

  abstract fun cross(componentCrossSpace: Int, rowCrossSpace: Int): Int
  abstract fun offset(componentCrossSpace: Int, rowCrossSpace: Int): Int
}