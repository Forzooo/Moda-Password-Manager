# ITC Events implemented by the Moda Password Manager
Events sections:
* [Default Operations](#default-operations)
* [Data Operations](#data-operations)
* [Database Operations](#database-operations)
* [Google Drive](#google-drive)
* [Utilities](#utilities)

***

## Default Operations
The default operations are added by the EventListener itself, thus they cannot be added by using the `addOperation`
method.

### Exception Raised
* Operation: `exception-raised`
* Sender: Backend
* Description: Sends to the frontend the exception raised
* Data sent: _String_ the thread name, _Throwable_ the exception raised
* Data received: -
* Synchronous: not required

### Close the connection
* Operation: `close-connection`
* Sender: Frontend
* Description: The connection will be closed, thus the execution will be terminated
* Data sent: -
* Data received: -
* Synchronous: required

***

## Data Operations
### Set the Master Password
* Operation: `set-master-password`
* Sender: Frontend
* Description: Sends the master password entered by the user
* Dati: _char[]_ masterPassword
* Data received: _boolean_ flag that is set to true only if the master password entered is the right one
* Synchronous: required

### Update the Master Password
* Operation: `update-master-password`
* Sender: Frontend
* Description: Updates the master password with a new one, reencrypting the data of the database
* Data sent: _char[]_ new master password
* Data received: -
* Synchronous: required

### Update the service fields
* Operation: `update-service-fields`
* Sender: Backend
* Description: The updated service fields are sent to the Frontend, to be shown in the "Show Data" section
* Data sent: _ArrayList<Data>_ the updated data
* Data received: -
* Synchronous: not required

### Reset the service fields
* Operation: `reset-service-fields`
* Sender: Backend
* Description: The service fields stored in the "Show Data" section must be reset
* Data sent: -
* Data received: -
* Synchronous: not required

### Get Data
* Operation: `get-data`
* Sender: Frontend
* Description: Request the decrypted data associated with a specific ID
* Data sent: _int_ ID
* Data received: _Data_ decrypted data
* Synchronous: required

### Save Data
* Operation: `save-data`
* Sender: Frontend
* Description: Send the data to the backend to save it inside the database
* Data sent: _Data_ data entered by the user
* Data received: -
* Synchronous: not required

### Delete Data
* Operation: `delete-data`
* Sender: Frontend
* Data sent: _int_ ID
* Data received: -
* Evento di risposta: -
* Synchronous: not required

### Update Data
* Operation: `update-data`
* Sender: Frontend
* Description: Send the updated data to update its record
* Data sent: _Data_ the data updated
* Data received: -
* Synchronous: required

***

## Database Operations
### Set the database
* Operation: `set-database`
* Sender: Frontend
* Description: Send the path of the database to use
* Data sent: _String_ path
* Data received: -
* Synchronous: required

### Get the database
* Operation: `get-database`
* Sender: Frontend
* Description: Get the path of the database in use
* Data sent: -
* Data received: _String_ path
* Synchronous: required

### get-recent-databases
* Operation: `get-recent-databases`
* Sender: Frontend
* Description: Get the paths of the last databases used
* Dati sent: -
* Dati received: _ArrayList<String>_ paths
* Synchronous: required

***

## Google Drive
### Get Google Drive Status
* Operation: `get-google-drive`
* Sender: Frontend
* Description: Requests whether Google Drive synchronization is enabled
* Data sent: -
* Data received:  _boolean_ google drive synchronizations status
* Synchronous: required

### Set Google Drive Status
* Operation: `set-google-drive`
* Sender: Frontend
* Description: Enable or disable the Google Drive service, which also requires the JSON credentials file used to 
authenticate with Google Drive (only if it has to be enabled)
* Data sent: _boolean_ status of Google Drive, _String_ path of "credentials.json" (only if the status is set to true)
* Data received: -
* Synchronous: not required

### Google Drive synchronization
* Operation: `google-drive-synchronize`
* Sender: Frontend
* Description: Perform a synchronization with Google Drive
* Data sent: -
* Data received: -
* Synchronous: not required

### Google Drive synchronization conflicts
* Operation: `google-drive-synchronization-conflicts`
* Sender: Backend
* Description: During the synchronization some conflicts between the local database and the remote one have been found.
  The user has to solve them by updating the local database with the changes he wants.
* Data sent: _ArrayList<Data>_ the conflict data
* Data received: -
* Synchronous: not required

### Google Drive synchronization conflicts solved
* Operation: `google-drive-synchronization-conflicts-solved`
* Sender: Frontend
* Description: The synchronization conflicts found have been solved, thus the local database has been updated, and the
  synchronization can be performed.
* Data sent: -
* Data received: -
* Synchronous: not required

### Get Google Drive automatic synchronization
* Operation: `get-google-drive-automatic-synchronization`
* Sender: Frontend
* Description: Request whether the automatic synchronization is enabled
* Data sent: -
* Data received: _boolean_ automatic synchronization status
* Synchronous: required

### Enable Google Drive automatic synchronization
* Operation: `set-google-drive-automatic-synchronization`
* Sender: Frontend
* Description: Enable or disable the automatic synchronization
* Data sent: _boolean_ status of the automatic synchronization
* Data received: -
* Synchronous: not required

***

## Utilities
### Generate a string
* Operation: `generate-string`
* Sender: Frontend
* Description: Request a randomly generated string, where the parameters of the generation are stored in the settings 
file
* Data sent: -
* Data received: _String_ the generated string
* Synchronous: required

### Configure String Generation
* Operation: `configure-string-generation`
* Sender: Frontend
* Description: Set the parameters of the string generation
* Data sent: _int_ length of the string, _bool_ whether letters are enabled, _bool_ whether numbers are enabled, _bool_ 
whether special characters are enabled
* Data received: -
* Synchronous: not required

### Get String Generation Configuration
* Operation: `get-string-generation-configuration`
* Sender: Frontend
* Description: Request the parameters of the string generation
* Data sent: -
* Data received: _int_ length of the string, _bool_ whether letters are enabled, _bool_ whether numbers are enabled, _bool_
  whether special characters are enabled
* Synchronous: required

### Get the application theme
* Operation: `get-application-theme`
* Sender: Frontend
* Description: Request the theme used by the application
* Data sent: -
* Data received: _Themes_ theme
* Synchronous: required

### Set the application theme
* Operation: `set-application-theme`
* Sender: Frontend
* Description: Set the new theme of the application
* Data sent: _Themes_ the new theme
* Data received: -
* Synchronous: not required

### Get the application language
* Operation: `get-application-language`
* Sender: Frontend
* Description: Request the language used by the application
* Data sent: -
* Data received: _Languages_ language
* Synchronous: required

### Set the application theme
* Operation: `set-application-theme`
* Sender: Frontend
* Description: Set the new language of the application
* Data sent: _Languages_ the new language
* Data received: -
* Synchronous: not required