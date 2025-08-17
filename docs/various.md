# Moda-Password-Manager
## Cryptography
Using a _Master Password_ given in input in each execution of the software, and not stored anywhere,
with a _salt_ safely generated, generate an _hash_ using **Argon2id** with the following settings:
* Memory: 64 MB
* Iterations: 3
* Parallelism: 2

Then use the _hash_ with a safely generated _IV_ (Initialization Vector) to compute a ciphertext of
the data, given by the user, using **AES-GCM**.

## Database
The database used for the Password Manager is **SQLite** for it's compatibility with any device.
Moreover it does not require any other third party software.

The table has 6 elements, which are the following:
* ID: allows easy access to all the rows of the table
* username_data
* email_data
* password_data
* service_data
* additional_data

## Synchronization
To allow the user to use the same database on more devices, the **Google Drive** can be used
as the server to store your database.
The only requirement is having a Google Cloud Project which implements the Google
Drive API.