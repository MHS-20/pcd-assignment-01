Volatile nei campi del Boid? In tutte le versioni?

### Version 1
Usare N thread per spezzare la lista di Boid, ed ognuno si occupa di un sottogruppo.
Velocity/Position: necessarie due barriere su cui aspettano tutti prima di passare alla fase successiva.
Velocity in realtà ha due barriere interne, prima per leggere le velocità, poi per scriverle.
Ha detto che la GUI ha un suo thread in cui va a leggere i boid mentre i thread li stanno modificando?

Per ora viene usato un Monitor per far partire o fermare i thread, ma basterebbe aggiungere +1 alle barriere e fermare solo il main,
alla fine il monitor serve solo a rispecchiare lo stato dell view.
La upddatePositionBarrier non è ciclica, il reset è manuale fatto dal main dopo che ha aggiornato la view.

Listeners JPF:
[https://github.com/javapathfinder/jpf-core/tree/master/src/main/gov/nasa/jpf/listener](https://github.com/javapathfinder/jpf-core/tree/master/src/main/gov/nasa/jpf/listener)


### Version 2
Perché se metto while(true) invece di while(loop) non va?
Se metto una stampa nel while(true) funziona, se la tolgo no.
Magari è si incrociano i flussi con la GUI?

### Version 3
Sembra più lenta rispetto ai thread fisici. 
C'è un problema con il tasto reset se lo spammo. 
Non so se la gestione con Interrupt vada bene.