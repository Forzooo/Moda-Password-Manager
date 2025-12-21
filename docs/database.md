# Database
## Brief Description
In the Password Manager the data is stored under the "moda" table inside an SQLite 3 database. All the databases end in
".modb", and they have a file association such that when opening them, the Password Manager will be started with that
database selected.

## Fields of the table
Each record has the following fields:
* `ID`: the auto-incremental primary key of the table
* `username`: the username of the user
* `email_address`: the email address of the user
* `password`: the password of the user
* `service`: the service of the data
* `additional_data`: additional data that is not covered by the other fields

Lastly all the fields are already encrypted by the cryptography class when they're added or modified.