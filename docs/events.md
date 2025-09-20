# Documentazione degli eventi di CommunicationHandler
### set-master-password
* Nome: `set-master-password`
* Mittente: Frontend
* Descrizione: Invia al backend la master password inserita dall'utente.
* Dati: _char[]_ masterPassword
* Evento di risposta: `set-master-password-completed`

### set-master-password-completed
* Nome: `set-master-password-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend se la master password inserita sia corretta
* Dati: _boolean_ Flag che indica il risultato del test
* Evento di risposta: -

### get-service-fields
* Nome: `get-service-fields`
* Mittente: Frontend
* Descrizione: Richiedi dal backend una lista contenente tutti i _serviceData_ con i relativi _ID_ del database
               selezionato.
* Dati: -
* Evento di risposta: `get-service-fields-completed`

### get-service-fields-completed
* Nome: `get-service-fields-completed`
* Mittente: Backend
* Descrizione: Invia al frontend una lista contenente tutti i _service_ con i relativi _ID_ del database.
* Dati: _ArrayList\<Data\>_
* Evento di risposta: -

### save-data
* Nome: `save-data`
* Mittente: Frontend
* Descrizione: Invia tutti i dati inseriti dall'utente affinché vengano salvati nel database
* Dati: _Data_ object
* Evento di risposta: `save-data-completed`

### save-data-completed
* Nome: `save-data-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend indicando che i dati sono stati salvati correttamente
* Dati: -
* Evento di risposta: -

### get-data
* Nome: `get-data`
* Mittente: Frontend
* Descrizione: Invia un ID al Backend per richiedere tutti i dati decrittati relativi a quel specifico ID
* Dati: _int_ ID
* Evento di risposta: `get-data-completed`

### get-data-completed
* Nome: `get-data-completed`
* Mittente: Backend
* Descrizione: Invia tutti i dati decrittati relativi a un ID
* Dati: _Data_ object
* Evento di risposta: -

### delete-data
* Nome: `delete-data`
* Mittente: Frontend
* Descrizione: Invia un ID al backend per richiedere di rimuovere il record che ha quel ID
* Dati: _int_ ID
* Evento di risposta: `delete-data-completed`

### delete-data-completed
* Nome: `delete-data-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend indicando che quel record è stato cancellato
* Dati: -
* Evento di risposta: -

### change-data
* Nome: `change-data`
* Mittente: Frontend
* Descrizione: Invia l'oggetto di tipo Data contenente l'ID con i relativi dati aggiornati
* Dati: _Data_ object
* Evento di risposta: `change-data-completed`

### change-data-completed
* Nome: `change-data-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend indicando che quel record è stato modificato
* Dati: -
* Evento di risposta: -

### generate-string
* Nome: `generate-string`
* Mittente: Frontend
* Descrizione: Richiedi dal backend una stringa generata casualmente utilizzando i parametri configurati
* Dati: -
* Evento di risposta: `generate-string-completed`

### generate-string-completed
* Nome: `generate-string-completed`
* Mittente: Backend
* Descrizione: Invia la stringa generata casualmente utilizzando i parametri configurati
* Dati: _String_ stringa generata casualmente
* Evento di risposta: -

### configure-string-generation
* Nome: `configure-string-generation`
* Mittente: Frontend
* Descrizione: Invia i parametri per la generazione delle stringhe da salvare nelle impostazioni
* Dati: _int_ numero di caratteri, _bool_ se le lettere sono abilitate, _bool_ se i numeri sono abilitati, _bool_ se i
caratteri speciali sono abilitati
* Evento di risposta: `configure-string-generation-completed`

### configure-string-generation-completed
* Nome: `configure-string-generation`
* Mittente: Backend
* Descrizione: Salva nelle impostazioni i parametri ricevuti
* Dati: -
* Evento di risposta: `-`

### set-database
* Nome: `set-database`
* Mittente: Frontend
* Descrizione: Invia il database da utilizzare
* Dati: _String_ path del database
* Evento di risposta: `set-database-path-completed`

### set-database-completed
* Nome: `set-database-completed`
* Mittente: Backend
* Descrizione: Imposta il database da utilizzare e salvare il path del database nelle impostazioni
* Dati: -
* Evento di risposta: -

### get-database-path
* Nome: `get-database-path`
* Mittente: Frontend
* Descrizione: Richiedi il path del database in uso
* Dati: -
* Evento di risposta: `get-database-path-completed`

### get-database-path-completed
* Nome: `get-database-path-completed`
* Mittente: Frontend
* Descrizione: Invia il path del database in uso
* Dati: _String_ path del database
* Evento di risposta: -

### get-string-generation-configuration
* Nome: `get-database-path`
* Mittente: Frontend
* Descrizione: Richiedi i parametri della generazione delle stringhe
* Dati: -
* Evento di risposta: `get-string-generation-configuration-completed`

### get-string-generation-configuration-completed
* Nome: `get-string-generation-configuration-completed`
* Mittente: Frontend
* Descrizione: Invia i parametri letti dal file settings, della generazione delle stringhe
* Dati: _int_ numero di caratteri, _bool_ se le lettere sono abilitate, _bool_ se i numeri sono abilitati, _bool_ se i
  caratteri speciali sono abilitati
* Evento di risposta: -

### exception-raised
* Nome: `exception-raised`
* Mittente: Backend
* Descrizione: Invia al frontend l'eccezione che è accaduta sul backend
* Dati: _String_ messaggio dell'eccezione
* Evento di risposta: `close-connection`

### close-connection
* Nome: `close-connection`
* Mittente: Frontend
* Descrizione: Il frontend indica al backend che la comunicazione viene chiusa e che quindi si può interrompere
l'esecuzione
* Dati: -
* Evento di risposta: `close-connection-confirm`

### close-connection-confirm
* Nome: `close-connection-confirm`
* Mittente: Backend
* Descrizione: Conferma la chiusura della comunicazione e interrompe l'esecuzione del thread del backend
* Dati: -
* Evento di risposta: -

### change-master-password
* Nome: `change-master-password`
* Mittente: Frontend
* Descrizione: Il frontend richiede di cambiare la master password in uso
  l'esecuzione
* Dati: _char[]_ nuova master password
* Evento di risposta: `change-master-password-confirm`

### change-master-password-confirm
* Nome: `change-master-password-confirm`
* Mittente: Backend
* Descrizione: Il backend rigenera tutti i dati con la nuova master password
* Dati: -
* Evento di risposta: -

### get-google-drive
* Nome: `get-google-drive`
* Mittente: Frontend
* Descrizione: Richiede al backend la configurazione attuale di google drive
* Dati: -
* Evento di risposta: `get-google-drive-confirm`

### get-google-drive-confirm
* Nome: `get-google-drive-confirm`
* Mittente: Backend
* Descrizione: Il backend invia al frontend la configurazione di google drive letta dal file di settings
* Dati: _boolean_ Stato di google drive
* Evento di risposta: -

### get-google-drive-synchronization
* Nome: `get-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Richiede al backend se la sincronizzazione automatica è abilitata
* Dati: -
* Evento di risposta: `get-google-drive-synchronization-confirm`

### get-google-drive-synchronization-confirm
* Nome: `get-google-drive-synchronization-confirm`
* Mittente: Backend
* Descrizione: Il backend invia al frontend lo stato della sincronizzazione automatica letto dal file di settings
* Dati: _boolean_ Stato della sincronizzazione automatica di google drive
* Evento di risposta: -

### google-drive-authenticate
* Nome: `google-drive-authenticate`
* Mittente: Frontend
* Descrizione: Invia al Backend il path del file richiesto per l'autenticazione
* Dati: _String_ path del file "credentials.json"
* Evento di risposta: `google-drive-authenticate-confirm`

### google-drive-authenticate-confirm
* Nome: `google-drive-authenticate-confirm`
* Mittente: Backend
* Descrizione: Salva il path del file nel file di settings e fa autenticare l'utente
* Dati: -
* Evento di risposta: -

### google-drive-unauthenticate
* Nome: `google-drive-unauthenticate`
* Mittente: Frontend
* Descrizione: Indica al backend che google drive è da disabilitare
* Dati: -
* Evento di risposta: `google-drive-unauthenticate-confirm`

### google-drive-unauthenticate-confirm
* Nome: `google-drive-unauthenticate-confirm`
* Mittente: Backend
* Descrizione: Imposta nel file di settings che google drive è disabilitato
* Dati: -
* Evento di risposta: -

### google-drive-synchronize
* Nome: `google-drive-synchronize`
* Mittente: Frontend
* Descrizione: Indica al backend che deve effettuare un sincronizzazione con Google Drive
* Dati: -
* Evento di risposta: `google-drive-synchronize-confirm`

### google-drive-synchronize-confirm
* Nome: `google-drive-synchronize-confirm`
* Mittente: Backend
* Descrizione: Viene effettuata una sincronizzazione con Google Drive
* Dati: -
* Evento di risposta: -

### enable-google-drive-synchronization
* Nome: `enable-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Indica al backend che viene abilitata la sincronizzazione automatica ogni 60 secondi
* Dati: -
* Evento di risposta: `enable-google-drive-synchronization-confirm`

### enable-google-drive-synchronization-confirm
* Nome: `enable-google-drive-synchronization-confirm`
* Mittente: Backend
* Descrizione: Il backend salva nel file di settings che la sincronizzazione automatica è abilitata
* Dati: -
* Evento di risposta: -

### disable-google-drive-synchronization
* Nome: `disable-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Indica al backend che viene disabilitata la sincronizzazione automatica
* Dati: -
* Evento di risposta: `disable-google-drive-synchronization-confirm`

### disable-google-drive-synchronization-confirm
* Nome: `disable-google-drive-synchronization-confirm`
* Mittente: Backend
* Descrizione: Il backend salva nel file di settings che la sincronizzazione automatica è disabilitata
* Dati: -
* Evento di risposta: -