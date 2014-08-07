
package org.smslib.callback.events;

import java.util.Date;

public class BaseCallbackEvent
{
	Date date = new Date();

	public Date getDate()
	{
		return new Date(this.date.getTime());
	}
}
