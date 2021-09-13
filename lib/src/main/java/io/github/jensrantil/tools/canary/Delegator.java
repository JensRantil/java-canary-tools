package io.github.jensrantil.tools.canary;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Delegator a helper class to simplify implementing {@link Proxy}s that delegates to downstream
 * implementations.
 *
 * @param <T> the Java interface that the Delegator wraps.
 */
class Delegator<T> implements InvocationHandler {

    private final DelegateSelector<T> selector;

    interface DelegateSelector<T> {
        T select(Method method, Object[] args);
    }

    public Delegator(DelegateSelector<T> selector) {
        this.selector = selector;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final T delegate = selector.select(method, args);

        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            final Throwable actualUnwrappedException = e.getTargetException();
            throw actualUnwrappedException;
        }
    }

    public static <T> T build(Class<T> type, DelegateSelector selector) {
        Preconditions.checkArgument(type.isInterface(), "T must be an interface");
        return (T)
                Proxy.newProxyInstance(
                        type.getClassLoader(), new Class[] {type}, new Delegator<T>(selector));
    }
}
