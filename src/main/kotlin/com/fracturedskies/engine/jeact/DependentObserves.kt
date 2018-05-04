package com.fracturedskies.engine.jeact

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class DependentObserves