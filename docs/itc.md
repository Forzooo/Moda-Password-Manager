# Inter Thread Communication
## Brief Description
The ITC (Inter Thread Communication) is used to let threads share data with each other.

In the Moda Password Manager, it is inside the **InterThreadCommunication** package, and it allows the Frontend and the
Backend to exchange data using asynchronous and synchronous requests, where both sides act as client and server. 
Moreover, they use **Event** objects, which are in the same package, to communicate telling the operation request and 
the data required.\
Lastly, the server side is implemented by inheriting the abstract class **EventListener** that provides the methods to
handle any operation.

## Event objects
Event objects have the following properties:
- _String_ operation: indicates the operation requested
- _ArrayList\<Object\> data: the data required for the operation
- _int_ communicationID: a unique integer that identifies the communication
- _int_ sequenceNumber: an auto incremental integer that identifies the current state of the communication

\[[List of events supported by the Password Manager](events.md)\]

## Methods
### void send(Event request)
Send an event asynchronously.

### Event receive()
Set the thread to wait until an event is sent, and returns it.

### Event request(Event request)
Send an event synchronously, waiting for the response and returns it.
