# Documentazione degli eventi di CommunicationHandler
### set-master-password
* Nome: `set-master-password`
* Mittente: Frontend
* Descrizione: Invia al backend la master password inserita dall'utente.
* Dati: _String_ masterPassword
* Evento di risposta: `set-master-password-completed`

### set-master-password-completed
* Nome: `set-master-password-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend indicando che la master password è stata salvata correttamente.
* Dati: -
* Evento di risposta: -

### close-connection
* Nome: `close-connection`
* Mittente: Frontend
* Descrizione: Comunica al backend che la comunicazione viene chiusa e che quindi si può interrompere l'esecuzione
* Dati: -
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
* Dati: _String_
* Evento di risposta: -

### configure-string-generation
* Nome: `configure-string-generation`
* Mittente: Frontend
* Descrizione: Salva nelle impostazioni i parametri per la generazione delle stringhe
* Dati: _int_ numero di caratteri, _bool_ se le lettere sono abilitate, _bool_ se i numeri sono abilitati, _bool_ se i
caratteri speciali sono abilitati
* Evento di risposta: `configure-string-generation-completed`

### configure-string-generation-completed
* Nome: `configure-string-generation`
* Mittente: Backend
* Descrizione: Salva nelle impostazioni i parametri ricevuti
* Dati: -
* Evento di risposta: `-`
