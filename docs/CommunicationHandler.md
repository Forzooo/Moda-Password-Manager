# Documentazione degli eventi di CommunicationHandler
### set-master-password
* Nome: `set-master-password`
* Mittente: Frontend
* Descrizione: Invia al backend la master password inserita dall'utente.
* Dati: String masterPassword
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
* Dati: ArrayList\<Data\>
* Evento di risposta: -

### save-data
* Nome: `save-data`
* Mittente: Frontend
* Descrizione: Invia tutti i dati inseriti dall'utente affinché vengano salvati nel database
* Dati: Data object
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
* Dati: int ID
* Evento di risposta: `get-data-completed`

### get-data-completed
* Nome: `get-data-completed`
* Mittente: Backend
* Descrizione: Invia tutti i dati decrittati relativi a un ID
* Dati: Data object
* Evento di risposta: -

### delete-data
* Nome: `delete-data`
* Mittente: Frontend
* Descrizione: Invia un ID al backend per richiedere di rimuovere il record che ha quel ID
* Dati: int ID
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
* Dati: Data object
* Evento di risposta: `change-data-completed`

### change-data-completed
* Nome: `change-data-completed`
* Mittente: Backend
* Descrizione: Notifica il frontend indicando che quel record è stato modificato
* Dati: -
* Evento di risposta: -
