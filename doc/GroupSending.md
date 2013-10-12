# Sending To Groups (multiple recipients)

## Introduction

SMSLib has a feature which you can use to define groups of recipients. Once defined, you can send sms messages to all recipients of a group with a single request.

## Group definition

A *Group* defines a collection of destination numbers. Each group has a name (which you use to refer to), a description and a list of recipients. When you send a message to a group, SMSLib creates and sends as many messages as the count of group's recipients define.

## Defining a group

It's very simple to define a group. First define the main attributes:

```
Group group = new Group("family", "My family members");
```

Then, add some recipients:

```
group.addRecipient("+306900123456");
group.addRecipient("+306900234567");
```

or

```
group.addRecipient(new MsIsdn("+306900123456"));
group.addRecipient(new MsIsdn("+306900234567"));
```

Finally, register your group definition to SMSLib:

```
Service.getInstance().getGroupManager().addGroup(group);
```

You can add as many groups and/or recipients as your available memory permits.

## Sending a message to a group

If you want to send a message to a predefined group, do

```
Service.getInstance().queue(new OutboundMessage("family", "Greetings, family!");
```

Note that a message sent to a group automatically creates many similar messages (with different recipients). So prepare to receive many callback calls - as many as your group's recipients.
