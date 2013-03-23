
package org.smslib;

public class Helper
{
	public static void sleep(int seconds)
	{
		for (int i = 0; i < seconds; i++)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				//
			}
		}
	}
}
