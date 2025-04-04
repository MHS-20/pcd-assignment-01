## Report Assingment 1 - PCD

Il problema principale da affrontare era la gestione delle letture e scritture di ogni boid della lista condivisa. Infatti ogni boid per aggiornare la propria velocità deve fare riferimento ai boid vicini, di conseguenza deve leggere il loro stato. Senza nessun tipo di sincronizzazione, c'è il rischio che un thread/task vada a leggere la velocità di un boid vicino mentre un altro thread/task la sta modificando. 

Ogni versione era propensa a tecniche di parallelizzazione diverse, ma la sincronizzazione era simile. Quello che rimane uguale nelle varie versioni è l'aggiornamento della GUI: questa viene aggiornata solo in una fase in cui i boid non vengono modificati.

Gestione dei bottoni:
* Suspend/Resume: bloccano il main, i thread si fermano su una barriera in attesa del main per ripartire. 
* Reset: quando il main nota che è stato premuto il tasto, ferma i thread/task e ne crea di nuovi, con una lista di boid aggiornata alla nuova dimensione, e crea anche delle nuove barriere.


### Versione 1: Platform Threads
In questa versione sono stati creati N thread (N = numero di core disponibili), ognuno dei quali si occupa di un sottoinsieme di boid.
Sono state usate diverse barriere cicliche per mantenere i thread sincronizzati nelle varie fasi di calcolo/lettura e scrittura dei boid.
La GUI viene modificata dopo che la velocità e la posizione di tutti i boid sono state aggiornate al nuovo istante di tempo. Finché l'aggiornamento della GUI non è terminato, i thread/task non possono procedere a scrivere i nuovi valori di posizione/velocità.

Le barriere utilizzato sono:
1. Barriera per il calcolo della velocità
2. Barriera per l'aggiornamento della velocità
3. Barriere per l'aggiornamento della posizione
4. Barriera per l'aggiornamento della GUI

Implementazione CyclicBarrier: utilizzando un lock è stata costruita una sezione critica nel metodo await, che ferma il thread in attesa quando il counter di thread in attesa non è sufficiente a rompere la barriera. Il thread che rompe la barriera prima la resetta e poi sveglia tutti i thread in attesa. 
La condizione sul while utilizza una variabile generation e non il counter, altrimenti ci sarebbe il rischio che un thread controlli la condizione sul counter quando è già stato resettato.


### Versione 2: Executor
In questa versione sono stati creati delle classi Task diverse per le fasi di lettura/calcolo ed aggiornamento dei boid.
In ogni fase, viene creato un task per ogni boid, e poi invocati in parallelo tramite un ExecutorService di tipo FixedThreadPool. 
Si attende il completamento di ogni task aspettando sull'oggetto Future corrispondente. 
Quando tutti i task di una fase sono terminati, si può passare alla fase successiva. Terminate le fasi, si aggiorna la GUI e si ripete il ciclo. 

Tipologie di Task create: 
1. Task per il calcolo della velocità
2. Task per l'aggiornamento della velocità
3. Task per l'aggiornamento della posizione


### Versione 3: Virtual Threads
Dato che i virtual thread sono molto leggeri e ci permettono di superare il limite di thread fisici del sistema, in questa versione è stato creato un virtual thread per ogni boid, che si occupa dei calconi e degli aggiornamenti corrispondenti. 
Anche in questo caso vengono utilizzate delle barriere cicliche per sincronizzare i thread e separare le fasi di calcolo da quelle di aggiornamento. 

Le barriere utilizzato sono:
1. Barriera per il calcolo della velocità
2. Barriera per l'aggiornamento della velocità
3. Barriere per l'aggiornamento della posizione
4. Barriera per l'aggiornamento della GUI