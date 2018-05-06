package com.fracturedskies.engine.jeact.scope

import javax.enterprise.context.spi.*


class ComponentScopeContext : Context {
  data class Instance<T>(val value: T, val creationalContext: CreationalContext<T>)

  private val map: MutableMap<Contextual<*>, MutableList<Instance<*>>> = mutableMapOf()

  override fun isActive(): Boolean = true
  override fun getScope(): Class<out Annotation> = ComponentScope::class.java
  override fun <T> get(contextual: Contextual<T>, creationalContext: CreationalContext<T>): T? {
      val instance = Instance(contextual.create(creationalContext), creationalContext)
      map
          .computeIfAbsent(contextual, { mutableListOf()})
          .add(instance)
    return instance.value
  }

  override fun <T> get(contextual: Contextual<T>): T? {
    return null
  }

  fun <T> destroy(contextual: Contextual<T>, instance: T) {
    map[contextual]?.let { instances ->
      @Suppress("UNCHECKED_CAST")
      val deleteInstances = instances.filter { it.value == instance } as List<Instance<T>>
      deleteInstances.forEach { contextual.destroy(it.value, it.creationalContext) }
      instances -= deleteInstances
    }
  }

  fun <T> getAll(contextual: Contextual<T>): List<T> {
    @Suppress("UNCHECKED_CAST")
    return map.computeIfAbsent(contextual, { mutableListOf()}).map { it.value as T }
  }
}