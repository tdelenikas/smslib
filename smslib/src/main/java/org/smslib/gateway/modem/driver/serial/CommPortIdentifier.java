
package org.smslib.gateway.modem.driver.serial;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communications port management.
 * <p>
 * <b>Please note: </b>This is a wrapper around
 * <code>javax.comm.CommPortIdentifier</code> (and so
 * <code>gnu.io.CommPortIdentifier</code>). The API definition is taken from
 * Sun. So honor them!
 * </p>
 * <p><code>CommPortIdentifier</code> is the central class for controlling access
 * to communications ports. It includes methods for: </p>
 * <ul>
 * <li>Determining the communications ports made available by the driver.</li>
 * <li>Opening communications ports for I/O operations.</li>
 * <li>Determining port ownership.</li>
 * <li>Resolving port ownership contention.</li>
 * <li>Managing events that indicate changes in port ownership status.</li>
 * </ul>
 * <p>
 * An application first uses methods in <code>CommPortIdentifier</code> to
 * negotiate with the driver to discover which communication ports are available
 * and then select a port for opening. It then uses methods in other classes
 * like <code>CommPort</code>, <code>ParallelPort</code> and
 * <code>SerialPort</code> to communicate through the port.
 * </p>
 * *
 * 
 * @author gwellisch
 */
public class CommPortIdentifier
{
	static Logger logger = LoggerFactory.getLogger(CommPortIdentifier.class);

	static private Class<?> classCommPortIdentifier;

	public static final int PORT_SERIAL;
	static
	{
		try
		{
			classCommPortIdentifier = Class.forName("javax.comm.CommPortIdentifier");
			logger.info("Using 'javax.comm' serial library.");
		}
		catch (ClassNotFoundException e1)
		{
			try
			{
				classCommPortIdentifier = Class.forName("gnu.io.CommPortIdentifier");
				logger.info("Using 'RxTx' serial library.");
			}
			catch (ClassNotFoundException e2)
			{
				throw new RuntimeException("Neither Java Comm nor RXTX library found. Please check http://smslib.org/doc/installation/#Java_Communications_Library");
			}
		}
		try
		{
			Field f;
			f = classCommPortIdentifier.getField("PORT_SERIAL");
			PORT_SERIAL = f.getInt(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private Object realObject;

	protected CommPortIdentifier(Object myRealObject)
	{
		this.realObject = myRealObject;
	}

	/**
	 * Returns the port type.
	 * 
	 * @return portType - PORT_SERIAL or PORT_PARALLEL
	 */
	public int getPortType()
	{
		try
		{
			Method method = classCommPortIdentifier.getMethod("getPortType", (java.lang.Class[]) null);
			return (Integer) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the name of the port.
	 * 
	 * @return the name of the port
	 */
	public String getName()
	{
		try
		{
			Method method = classCommPortIdentifier.getMethod("getName", (java.lang.Class[]) null);
			return (String) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	// Note: using SerialPort instead of CommPort
	public SerialPort open(String appname, int timeout)
	{
		Class<?>[] paramTypes = new Class<?>[] { String.class, int.class };
		try
		{
			Method method = classCommPortIdentifier.getMethod("open", paramTypes);
			return new SerialPort(method.invoke(this.realObject, appname, timeout));
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtains an enumeration object that contains a
	 * <code>CommPortIdentifier</code> object for each port in the system.
	 * 
	 * @return <code>Enumeration</code> that can be used to enumerate all the
	 *         ports known to the system
	 */
	public static Enumeration<CommPortIdentifier> getPortIdentifiers()
	{
		if (classCommPortIdentifier == null) { throw new RuntimeException("CommPortIdentifier class not found"); }
		Enumeration<CommPortIdentifier> list;
		try
		{
			// get the enumeration of real objects
			Method method = classCommPortIdentifier.getMethod("getPortIdentifiers", (java.lang.Class[]) null);
			CommPortIdentifier type = null;
			list = ReflectionHelper.invokeAndCastEnumeration(type, method, null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		// wrap the real objects
		Vector<CommPortIdentifier> vec = new Vector<>();
		while (list.hasMoreElements())
			vec.add(new CommPortIdentifier(list.nextElement()));
		return vec.elements();
	}

	/**
	 * Obtains a CommPortIdentifier object by using a port name. The port name
	 * may have been stored in persistent storage by the application.
	 * 
	 * @param portName
	 *            name of the port to open
	 * @return <code>CommPortIdentifier</code> object
	 * @throws RuntimeException
	 *             (wrapping a NoSuchPortException) if the port does not exist
	 */
	public static CommPortIdentifier getPortIdentifier(String portName)
	{
		if (classCommPortIdentifier == null) { throw new RuntimeException("CommPortIdentifier class not found"); }
		CommPortIdentifier port;
		try
		{
			//get the string of real objects
			Method method = classCommPortIdentifier.getMethod("getPortIdentifier", String.class);
			port = new CommPortIdentifier(method.invoke(null, portName));
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return port;
	}
}
