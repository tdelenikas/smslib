
package org.smslib.queue;

import java.util.concurrent.TimeUnit;

public interface IOutboundQueue<T>
{
	public boolean start() throws Exception;

	public boolean stop() throws Exception;

	public boolean add(T o) throws Exception;

	public T get() throws Exception;

	public T get(int count, TimeUnit timeUnit) throws Exception;

	public int size() throws Exception;
}
