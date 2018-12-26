package com.fracturedskies.engine.jeact.scope

import javax.enterprise.util.AnnotationLiteral
import javax.inject.Scope

@Scope
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
annotation class ComponentScope {
    companion object {
        class Literal : AnnotationLiteral<ComponentScope>() {
            companion object {
                val INSTANCE = Literal()
            }
        }
    }
}
