# Database
## Brief Description
In the Password Manager the user data is stored under the "data" table inside an SQLite 3 database. All the databases end in
".modb", and they have a file association such that when opening them, the Password Manager will be started with that
database selected. Lastly, there is also the "sensitive_settings" table that contains all the settings of the application
that require to be encrypted such as the Google Drive secrets file.

## Fields of the Data table
Each record has the following fields:
* `ID`: the auto-incremental primary key of the table
* `username`: the username of the user
* `email_address`: the email address of the user
* `password`: the password of the user
* `service`: the service of the data
* `additional_data`: additional data that is not covered by the other fields

Lastly all the fields are already encrypted by the cryptography class when they're added or modified.

## Fields of the Sensitive Settings table
Each record has the following fields:
* `propertyPath`: the name of the setting (ex. **google_drive/credentials**)
* `value`: the value of the setting

## Versions
* **0**: The first version of the database supported up to 0.5.2. It featured the `moda` table
* **1**: The current version of the database, in use from 0.6.0. It features the `data` table, which is the `moda` table 
         but renamed, and the `sensitive_settings` table 

The database is updated automatically from a previous version to the next one.