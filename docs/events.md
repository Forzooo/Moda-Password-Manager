# Documentazione degli eventi dell'Inter Thread Communication
Sezioni:
* [Generali](#generali)
* [Gestione dei dati](#gestione-dei-dati)
* [Gestione del database](#gestione-del-database)
* [Generazione delle stringhe](#generazione-delle-stringhe)
* [Google Drive](#google-drive)

**Per l'implementazione attuale, se dei dati di risposta, dello stesso tipo di evento, sono inclusi, allora non è
possibile che esista un evento di risposta, e viceversa.**

## Generali
### exception-raised
* Nome: `exception-raised`
* Mittente: Backend
* Descrizione: Invia al frontend l'eccezione che è accaduta sul backend
* Dati: _String_ nome del thread dove è avvenuta l'eccezione, _Throwable_ l'eccezione provocata
* Dati risposta: -
* Evento di risposta: `close-connection`
* Altà priorità: false

### close-connection
* Nome: `close-connection`
* Mittente: Frontend
* Descrizione: Il frontend indica al backend che la comunicazione viene chiusa e che quindi si può interrompere
  l'esecuzione
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

### unknown-event
* Nome: `unknown-event`
* Mittente: Backend
* Descrizione: Il backend indica al frontend che l'evento inviato non è conosciuto, e quindi è scartato
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

***

## Gestione dei dati
### set-master-password
* Nome: `set-master-password`
* Mittente: Frontend
* Descrizione: Invia al backend la master password inserita dall'utente.
* Dati: _char[]_ masterPassword
* Dati risposta: _boolean_ Flag che indica se la master password è corretta
* Evento di risposta: -
* Altà priorità: true

### change-master-password
* Nome: `change-master-password`
* Mittente: Frontend
* Descrizione: Il frontend richiede di cambiare la master password per il database in uso
* Dati inviati: _char[]_ nuova master password
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

### update-service-fields
* Nome: `update-service-fields`
* Mittente: Backend
* Descrizione: Il backend invia al Frontend i dati aggiornati da mostrare nella sezione "Show Data"
* Dati inviati: _ArrayList<Data>_ i dati aggiornati
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### get-data
* Nome: `get-data`
* Mittente: Frontend
* Descrizione: Invia un ID al Backend per richiedere tutti i dati decrittati relativi a quel specifico ID
* Dati inviati: _int_ ID associato ai dati richiesti
* Dati risposta: _Data_ i dati decrittati relativi a quel ID
* Evento di risposta: -
* Altà priorità: true

### save-data
* Nome: `save-data`
* Mittente: Frontend
* Descrizione: Invia tutti i dati inseriti dall'utente affinché vengano salvati nel database
* Dati inviati: _Data_ contenente i dati inseriti dell'utente
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### delete-data
* Nome: `delete-data`
* Mittente: Frontend
* Descrizione: Invia un ID al backend per richiedere di rimuovere il record che ha quel ID
* Dati inviati: _int_ ID associato al record da eliminare
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### change-data
* Nome: `change-data`
* Mittente: Frontend
* Descrizione: Invia l'oggetto di tipo Data contenente l'ID con i relativi dati aggiornati
* Dati inviati: _Data_ contenente i dati aggiornati
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

***

## Gestione del database
### set-database
* Nome: `set-database`
* Mittente: Frontend
* Descrizione: Invia il path del database da utilizzare al backend
* Dati inviati: _String_ path del database da utilizzare
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

### get-database
* Nome: `get-database`
* Mittente: Frontend
* Descrizione: Richiedi il path del database in uso
* Dati inviati: -
* Dati risposta: _String_ path del database in uso
* Evento di risposta: -
* Altà priorità: true

***

## Generazione delle stringhe
### generate-string
* Nome: `generate-string`
* Mittente: Frontend
* Descrizione: Richiedi dal backend una stringa generata casualmente utilizzando i parametri configurati
* Dati inviati: -
* Dati ricevuti: _String_ stringa generata casualmente
* Evento di risposta: -
* Altà priorità: true

### configure-string-generation
* Nome: `configure-string-generation`
* Mittente: Frontend
* Descrizione: Invia i parametri per la generazione delle stringhe da salvare nelle impostazioni
* Dati inviati: _int_ numero di caratteri, _bool_ se le lettere sono abilitate, _bool_ se i numeri sono abilitati, _bool_ se i
caratteri speciali sono abilitati
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### get-string-generation-configuration
* Nome: `get-string-generation-configuration`
* Mittente: Frontend
* Descrizione: Richiedi i parametri della generazione delle stringhe
* Dati inviati: -
* Dati ricevuti: _int_ numero di caratteri, _bool_ se le lettere sono abilitate, _bool_ se i numeri sono abilitati, _bool_ se i
  caratteri speciali sono abilitati
* Evento di risposta: -
* Altà priorità: true

***

## Google Drive
### get-google-drive
* Nome: `get-google-drive`
* Mittente: Frontend
* Descrizione: Richiede al backend la configurazione attuale di google drive
* Dati inviati: -
* Dati risposta:  _boolean_ Stato di google drive
* Evento di risposta: -
* Altà priorità: true

### google-drive-authenticate
* Nome: `google-drive-authenticate`
* Mittente: Frontend
* Descrizione: Invia al Backend il path del file richiesto per l'autenticazione
* Dati inviati: _String_ path del file "credentials.json"
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### google-drive-unauthenticate
* Nome: `google-drive-unauthenticate`
* Mittente: Frontend
* Descrizione: Indica al backend che google drive è da disabilitare
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### google-drive-synchronize
* Nome: `google-drive-synchronize`
* Mittente: Frontend
* Descrizione: Indica al backend che deve effettuare una sincronizzazione con Google Drive
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: true

### enable-google-drive-synchronization
* Nome: `enable-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Indica al backend che viene abilitata la sincronizzazione automatica ogni 60 secondi
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### disable-google-drive-synchronization
* Nome: `disable-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Indica al backend che viene disabilitata la sincronizzazione automatica
* Dati inviati: -
* Dati risposta: -
* Evento di risposta: -
* Altà priorità: false

### get-google-drive-synchronization
* Nome: `get-google-drive-synchronization`
* Mittente: Frontend
* Descrizione: Richiede al backend se la sincronizzazione automatica è abilitata
* Dati inviati: -
* Dati risposta: _boolean_ Stato della sincronizzazione automatica di google drive
* Evento di risposta: -
* Altà priorità: true
