package org.smslib.groups;

import java.util.LinkedList;
import java.util.List;

import org.smslib.message.MsIsdn;

public class Group
{
	String name = null;
	String description = null;
	List<MsIsdn> recipientList = null;

	public Group(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.recipientList = new LinkedList<MsIsdn>();
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

	public boolean addRecipient(String msisdn)
	{
		return addRecipient(new MsIsdn(msisdn));
	}

	public boolean addRecipient(MsIsdn msisdn)
	{
		return this.recipientList.add(msisdn);
	}

	public boolean removeRecipient(String msisdn)
	{
		return removeRecipient(new MsIsdn(msisdn));
	}

	public boolean removeRecipient(MsIsdn msisdn)
	{
		return this.recipientList.remove(msisdn);
	}

	public List<MsIsdn> getRecipients()
	{
		return this.recipientList;
	}
}
