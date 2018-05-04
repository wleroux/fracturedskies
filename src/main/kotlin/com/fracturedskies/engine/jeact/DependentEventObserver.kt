package com.fracturedskies.engine.jeact

import java.lang.reflect.*
import javax.enterprise.event.Observes
import javax.enterprise.inject.Default
import javax.enterprise.inject.spi.EventMetadata
import javax.inject.*


@Singleton
class DependentEventObserver {
  private val observers = mutableMapOf<Type, MutableMap<Any, List<Pair<Method, List<Annotation>>>>>()

  operator fun plusAssign(observer: Any) {
    observer::class.java.methods
        .filter { it.parameters.size == 1 }
        .filter { it.parameters[0].isAnnotationPresent(DependentObserves::class.java) }
        .groupBy { it.parameters[0].parameterizedType }
        .forEach { type, methods ->
          val observables = observers.computeIfAbsent(type, { mutableMapOf() } )
          observables[observer] = methods.map { method ->
            method to qualifiers(method.parameters[0])
          }
        }
  }

  private fun qualifiers(element: AnnotatedElement): List<Annotation> {
    val qualifiers = element.annotations.filter { annotation ->
      annotation.annotationClass.java.isAnnotationPresent(Qualifier::class.java)
    }
    return if (qualifiers.isEmpty()) {
      listOf(Default.Literal.INSTANCE)
    } else {
      qualifiers
    }
  }

  operator fun minusAssign(observer: Any) {
    observers.forEach { _, value -> value.remove(observer) }
  }

  fun onEvent(@Observes event: Any, eventMetadata: EventMetadata) {
    observers[event::class.java]?.forEach { observer, methods ->
      methods
          .filter { (_, qualifiers) -> qualifiers.all(eventMetadata.qualifiers::contains) }
          .forEach { (method, _) ->
            method.invoke(observer, event)
          }
    }
  }
}