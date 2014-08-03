
package org.smslib.groups;

import java.util.HashMap;

public class GroupManager
{
	HashMap<String, Group> groupList = null;

	public GroupManager()
	{
		this.groupList = new HashMap<>();
	}

	public void addGroup(Group g)
	{
		this.groupList.put(g.getName(), g);
	}

	public void removeGroup(Group g)
	{
		removeGroup(g.getName());
	}

	public void removeGroup(String groupName)
	{
		this.groupList.remove(groupName);
	}

	public Group getGroup(String groupName)
	{
		return this.groupList.get(groupName);
	}

	public boolean exist(String groupName)
	{
		return (this.groupList.containsKey(groupName));
	}
}
