# Proiect pentru PAOJ - Platforma E-ticketing

## Etapa 1

### Definirea sistemului:

#### 1 Drept obiecte in cadrul temei de platforma e-ticketing, putem avea:
* Eveniment 
* Bilet
* Utilizator
* Locatie
* Tip Bilet (VIP, Standard, etc)
* Comanda
* Plata
* Tip eveniment (concert, meci de sport, teatru, etc)
* Recenzie
* Coduri Discount (inca nu-s sigur de asta)

#### 2 Drept actiuni putem avea:
* CRUD evenimente
* Cumparare bilete
* Refund bilet
* Vizionare istoric comenzi
* Search/Select cu sau fara filtrare evenimente
* Raport vanzari pe locatie sau eveniment
* Aplicare cod prom (?)
* Transfer bilet/comanda
* CR recenzii
* Exportare bilete in format PDF/TXT

### Implementare Java:
```
# pentru rula:
javac -d bin src/models/*.java src/main.java
```
```
java -cp bin main
```

# Etapa 2: 

### am folosit mysql

### audit
* se scrie in `audit.csv` la fiecare actiune (format: nume_actiune, timestamp)

### pentru rulare
```
docker compose up -d db [&& sleep 10] 
```

```
docker compose run [--build] --rm app 
```