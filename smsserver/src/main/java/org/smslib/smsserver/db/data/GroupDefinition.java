package org.smslib.smsserver.db.data;

import java.util.Collection;

public class GroupDefinition
{
	String name;
	String description;
	Collection<GroupRecipientDefinition> recipients;

	public GroupDefinition(String name, String description, Collection<GroupRecipientDefinition> recipients)
	{
		this.name = name;
		this.description = description;
		this.recipients = recipients;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public Collection<GroupRecipientDefinition> getRecipients()
	{
		return recipients;
	}
}
