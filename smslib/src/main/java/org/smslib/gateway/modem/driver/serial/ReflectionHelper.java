
package org.smslib.gateway.modem.driver.serial;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Some methods to use generics with reflection.
 */
public class ReflectionHelper
{
	/**
	 * Searches in the given class for the given method name. The argument list
	 * is ignored. Overload methods shouldn't used with it - You can't be sure
	 * which method you will get!
	 * 
	 * @param c
	 *            Class to search
	 * @param methodName
	 *            Methodname to search
	 * @return The found method
	 * @throws NoSuchMethodException
	 *             If the search method isn't shown
	 */
	public static Method getMethodOnlyByName(Class<?> c, String methodName) throws NoSuchMethodException
	{
		Method method = null;
		for (Method m : c.getMethods())
		{
			if (m.getName().equals(methodName))
			{
				method = m;
				break;
			}
		}
		if (method == null) { throw new NoSuchMethodException(methodName); }
		return method;
	}

	/**
	 * Invokes the given method on the given object with the given arguments.
	 * The result is cast to T and every kind of exception is wrapped as
	 * RuntimeException
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T> T invokeAndCast(T returnType, Method m, Object obj, Object... args)
	{
		try
		{
			return (T) m.invoke(obj, args);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Same as
	 * {@link ReflectionHelper#invokeAndCast(Object, Method, Object, Object...)
	 * but with a cast to Enumeration-of-T }
	 * 
	 * @see ReflectionHelper#invokeAndCast(Object, Method, Object, Object...)
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T> Enumeration<T> invokeAndCastEnumeration(T returnType, Method m, Object obj, Object... args)
	{
		try
		{
			return (Enumeration<T>) m.invoke(obj, args);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Same as
	 * {@link ReflectionHelper#invokeAndCast(Object, Method, Object, Object...)
	 * but with a cast to Collection-of-T }
	 * 
	 * @see ReflectionHelper#invokeAndCast(Object, Method, Object, Object...)
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T> Collection<T> invokeAndCastCollection(T returnType, Method m, Object obj, Object... args)
	{
		try
		{
			return (Collection<T>) m.invoke(obj, args);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
