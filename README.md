
# Chat Call Centre (CCC)

The Chat Call Centre (CCC) gives a salesman (or a support operator) the means to conduct several simultaneous chats with the clients, who may use any messenger types supported by MiniApps. The system allows to connect as many operators as needed to handle the incoming flow of requests.

## Connection of CCC

To connect the CCC you need to register a phone number with the MiniApps administrator account (https://dev.miniapps.run). Log onto the administrator account and choose your bot. As shown in the picture below, here the CCC is not yet connected:
![](https://i.imgur.com/Q1LrLXL.png)

Click on **Connect Chat Call Centre** and enter the operator's phone number in the popped up window:
![](https://i.imgur.com/sr1vDUd.png)

Click **Connect**. The CCC is now connected:
![](https://i.imgur.com/LHpxGda.png)

## Using ССС

To activate CCC enter **Connect CCC** command in the messenger. To deactivate enter **Disconnect CCC**.

If the operator's phone number is not yet verified by the system, it will ask you for a phone number verification. The number should match the number that was previously registered when connecting the CCC using the MiniApps administrator's account.

Now you enter the administrator interface. To switch to the operator mode push the **Operator mode** button. To make changes to CCC parameters push the Parameters button.

## Operator Mode

To switch to the operator mode push the **Operator mode** button. To get back push the **Administrator mode** button.

The message No users means that there are no active chats, or all active chats are handled by other operators.

If there is a user's request in the queue, it is shown, like this.

A button with a user's name marked with an asterisk (an active user) is added automatically. There are as many buttons as there are active users. When choosing another user (pressing the button with an appropriate name) the chat window shows the last three messages of the chat with that user and the **More** button that shows three earlier messages, and so on.

The chat closes after pressing the End dialog button, or after the timeout set in the parameters expires.

## Changing CCC Parameters

To make changes to CCC parameters push the **Parameters** button while in the administrator mode.

Here you can change the number of simultaneous chats per operator (5 is the default number), set the message waiting timeout, after expiration of which the chat with an unresponsive user will close automatically (20 minutes is the default timeout), or go the operator management part.

Here you can add an operator by clicking on the **Add operator** button and entering the operator's phone number, or get the list of connected operators with the possibility to disconnect any of them.

## Authorising Operators

Operators connect to CCC entering the command **Login operator** and disconnect entering the **Logout operator** command. The same as the administrator, an operator should pass the phone number verification procedure. But, prior to that the operator should be added to the system by the administrator (see above). Then the operator works the same way as the administrator in the operator mode.
