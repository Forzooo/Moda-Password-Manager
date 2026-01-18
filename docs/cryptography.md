# Cryptography
To encrypt the data, the user has to provide a password, which is referred to as the "Master Password", that
allows to securely store the encrypted version of it. Moreover, each data is encoded into base 64 before being saved
inside the database.

### Encryption Process
1. Compute the salt: a 16 bytes random array
2. Compute the hash using the generated salt and the password provided by the user with the Argon2id algorithm where the
configuration is the following:
   * Memory: 64 MB
   * Iterations: 3
   * Parallelism: 2
3. Compute the IV: a 12 bytes random array
4. Encrypt the plaintext using AES-GCM where the encryption key is the hash generated
5. Append to the ciphertext, which it is a bytes array, the salt and the IV as headers

### Decrypt Process
1. Extract the salt from the headers of the ciphertext
2. Compute the hash using the extracted salt and the password provided by the user with the Argon2id where the
   configuration is the following:
    * Memory: 64 MB
    * Iterations: 3
    * Parallelism: 2
3. Extract the IV from the headers of the ciphertext
4. Decrypt the ciphertext using AES-GCM where the encryption key is the hash generated