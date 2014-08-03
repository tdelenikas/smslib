
package org.smslib.groups;

import java.util.LinkedList;
import java.util.List;
import org.smslib.message.MsIsdn;

public class Group
{
	String name = null;

	String description = null;

	List<MsIsdn> addressList = null;

	public Group(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.addressList = new LinkedList<>();
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean addAddress(String msisdn)
	{
		return addAddress(new MsIsdn(msisdn));
	}

	public boolean addAddress(MsIsdn msisdn)
	{
		return this.addressList.add(msisdn);
	}

	public boolean removeAddress(String msisdn)
	{
		return removeAddress(new MsIsdn(msisdn));
	}

	public boolean removeAddress(MsIsdn msisdn)
	{
		return this.addressList.remove(msisdn);
	}

	public List<MsIsdn> getRecipients()
	{
		return this.addressList;
	}
}
