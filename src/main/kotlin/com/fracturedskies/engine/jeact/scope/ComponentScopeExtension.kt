package com.fracturedskies.engine.jeact.scope

import com.fracturedskies.engine.jeact.Component
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedMethodImpl
import org.jboss.weld.bean.builtin.BeanManagerProxy
import org.jboss.weld.injection.*
import org.jboss.weld.injection.MethodInjectionPoint.MethodInjectionPointType
import org.jboss.weld.manager.BeanManagerImpl
import org.jboss.weld.resources.ClassTransformer
import org.jboss.weld.util.collections.ImmutableSet
import javax.enterprise.event.*
import javax.enterprise.inject.spi.*

class ComponentScopeExtension : Extension {

  private val context = ComponentScopeContext()

  internal fun addComponentScope(@Observes beforeBeanDiscovery: BeforeBeanDiscovery) {
    beforeBeanDiscovery.addScope(ComponentScope::class.java, false, false)
  }

  internal fun addComponentScopeContext(@Observes afterBeanDiscovery: AfterBeanDiscovery) {
    afterBeanDiscovery.addContext(context)
  }

  internal fun <T> configureComponents(@Observes processAnnotatedType: ProcessAnnotatedType<T>) {
    if (Component::class.java.isAssignableFrom(processAnnotatedType.annotatedType.javaClass)) {
      processAnnotatedType.configureAnnotatedType().add(ComponentScope.Companion.Literal.INSTANCE)
    }
  }

  internal fun <T, X> configureObserverMethodOnComponentScope(@Observes processObserverMethod: ProcessObserverMethod<T, X>, managerProxy: BeanManager) {
    if (processObserverMethod.annotatedMethod.declaringType.isAnnotationPresent(ComponentScope::class.java)) {
      val manager = (managerProxy as BeanManagerProxy).delegate()
      val beanClass = processObserverMethod.annotatedMethod.declaringType
      val declaringBean = manager.resolve(manager.getBeans(beanClass.javaClass))
      val methodInjectionPoint = initMethodInjectionPoint<T, X>(manager, declaringBean, processObserverMethod.annotatedMethod)
      val creationalContext = managerProxy.createCreationalContext(declaringBean)
      val methodInvocationStrategy = MethodInvocationStrategy.forObserver(methodInjectionPoint, manager)
      processObserverMethod.configureObserverMethod().notifyWith { eventContext ->
        context.getAll(declaringBean).forEach { receiver ->
          methodInvocationStrategy.invoke(receiver, methodInjectionPoint, eventContext.event, manager, creationalContext)
        }
      }
    }
  }

  private fun <T, X> initMethodInjectionPoint(manager: BeanManagerImpl, declaringBean: Bean<out Any>, annotatedMethod: AnnotatedMethod<X>): MethodInjectionPoint<T, X> {
    val classTransformer = manager.services[ClassTransformer::class.java]
    @Suppress("UNCHECKED_CAST")
    val enhancedAnnotatedType = classTransformer.getEnhancedAnnotatedType(declaringBean.beanClass as Class<X>, manager.id)
    val observer = EnhancedAnnotatedMethodImpl.of<T, X, X>(annotatedMethod, enhancedAnnotatedType, classTransformer)
    return InjectionPointFactory.instance().createMethodInjectionPoint(
        MethodInjectionPointType.OBSERVER,
        observer,
        declaringBean,
        declaringBean.beanClass,
        ImmutableSet.of(Observes::class.java, ObservesAsync::class.java),
        manager
    )
  }
}