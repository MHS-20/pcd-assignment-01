Volatile nei campi del Boid? In tutte le versioni?
Non so se la gestione con Interrupt vada bene.

### Version 1
Usare N thread per spezzare la lista di Boid, ed ognuno si occupa di un sottogruppo.
Velocity/Position: necessarie due barriere su cui aspettano tutti prima di passare alla fase successiva.
Velocity in realtà ha due barriere interne, prima per leggere le velocità, poi per scriverle.

Ho inserito anche una barriera per disegnare la gui perché altrimenti i thread andrebbero avanti a modificare i boid mentre la view li legge per disegnalri. 
L'alternativa sarebbe stata quella di farli procedere in lettura e fermarsi finché non arrivava anche il main. 

Per fermare i thread viene fermato il main settando una flag nella view, e quindi tutti i thread si fermano su una barriera in attesa del main. 
Per il reset vengono interrotti i thread e creati di nuovi, insieme a nuove barriere e nuovi boid. 
Non serve fare join su i thread vecchi perché si perderebbe tempo, tanto creiamo una nuova lista e nuove barriere, i thread vecchi non possono interferire. 



Listeners JPF:
[https://github.com/javapathfinder/jpf-core/tree/master/src/main/gov/nasa/jpf/listener](https://github.com/javapathfinder/jpf-core/tree/master/src/main/gov/nasa/jpf/listener)


### Version 2
Avevo un problema di deadlock perché mancava synch sul running della view.
Ogni fase di lettura e calcolo è un task separato. Viene creato un pool e poi invocati tutti i task insieme. 
Si attende che tutti i task abbiano finito prima di poter passare alla fase successiva.
Per il reset viene prima fermato l'Executor e poi creato uno nuovo.

### Version 3
Sembra più lenta rispetto ai thread fisici.
Non so se la gestione con Interrupt vada bene.

Barriera: quando si fa await su una Condition, il lock associato viene in automatico rilasciato. 
Quando riceve l'interrupt però, acquisisce il lock prima di gestire l'eccezione. 

Alcuni Vthread non sembrano fermarsi, oppure la tua gestione è sbagliata solo che con i thread fisici non emerge perché sono pochi?
Oppure i vthread hanno un ciclo di vita diverso?

Forse il deadlock è dovuto al fatto che usi interrupt sui thread ma non sul main, se il main è fermo ad aspettare su una barriera non c'è nessuno che lo sveglia quando premi reset. 
Anche se il disegno lo fa, quindi vuol dire che il main ci arriva. Forse dopo si blocca su una barriera vecchia?
E' come se aspettassero su barriere diverse e si bloccano, ma le stampe non funzionano, le saltano.
Ma non ha senso che vada in deadlock se sta facendo solo degli interrupt. 

E' come se il main saltasse la barrier della posizione ed andasse subito su quella della gui. Anche cambiando la barriera con il +1, ne salta sempre una.
Ma cosa c'entra il tasto reset poi? Perché proprio quando premo reset?