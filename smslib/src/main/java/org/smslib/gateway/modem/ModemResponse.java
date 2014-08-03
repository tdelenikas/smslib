
package org.smslib.gateway.modem;

public class ModemResponse
{
	String responseData;

	boolean responseOk;

	public ModemResponse(String responseData, boolean responseOk)
	{
		this.responseData = responseData;
		this.responseOk = responseOk;
	}

	public String getResponseData()
	{
		return this.responseData;
	}

	public boolean isResponseOk()
	{
		return this.responseOk;
	}
}
