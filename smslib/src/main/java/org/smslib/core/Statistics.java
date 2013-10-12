
package org.smslib.core;

import java.util.Date;

public class Statistics
{
	int totalSent = 0;

	int totalFailed = 0;

	int totalReceived = 0;

	int totalFailures = 0;

	Date startTime = new Date();

	public int getTotalSent()
	{
		return this.totalSent;
	}

	public int getTotalFailed()
	{
		return this.totalFailed;
	}

	public int getTotalReceived()
	{
		return this.totalReceived;
	}

	public int getTotalFailures()
	{
		return this.totalFailures;
	}

	public Date getStartTime()
	{
		return new Date(this.startTime.getTime());
	}

	public synchronized void increaseTotalSent()
	{
		this.totalSent++;
	}

	public synchronized void increaseTotalFailed()
	{
		this.totalFailed++;
	}

	public synchronized void increaseTotalReceived()
	{
		this.totalReceived++;
	}

	public synchronized void increaseTotalFailures()
	{
		this.totalFailures++;
	}
}
