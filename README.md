# BookingMicroservices
## Praca inżynierska

### Wymagania
Aplikacja Booking Microservices została przystosowana do uruchamiania w kontenerach programu Docker, na 
komputerach z systemem operacyjnym Linux.
Aplikacja wymaga zainstalowanego programu Docker i Docker-compose.

Instrukcja instalacji programu Docker dostępna jest na stronie: 
https://docs.docker.com/engine/installation/
 
Instrukcja instalacji programu Docker-compose dostępna jest na stronie: 
https://docs.docker.com/compose/install/

### Instrukcje zbudowania i uruchomienia aplikacji Booking Microservices:
Po zainstalowaniu potrzebnych programów aplikację można uruchomić wpisując w konsoli:

> mvn clean package

Polecenie to zbuduje aplikację i stworzy obrazy programu Docker

> docker-compose up

Polecenie to uruchomi i odpowiednio połączy ze sobą kontenery programu Docker

Po wykonaniu powyższych poleceń, aplikacja Booking Microservices będzie uruchomiona, w konsoli będą 
wypisywane logi, a dostęp do aplikacji będzie możliwy poprzez otworzenie w przeglądarce internetowej 
adresu: 
localhost:8097

Istnieje także możliwość uruchomienia aplikacji za pomocą stworzonych do tego celu skryptów: 

> build-and-run.sh

Który buduje aplikacje, tworzy obrazy i kontenery Docker'a i uruchamia program z podglądem logów w konsoli.

> logs-build-and-run.sh

Który buduje aplikacje, tworzy obrazy i kontenery Docker'a i uruchamia program z zapisem logów do plików,
logi budowania aplikacji zapisane zostaną do pliku build-logs-2.txt, a logi działania aplikacji do pliku:
run-logs-2.txt

### Instrukcje zbudowania pojedynczych mikroserwisów:
Aplikacja składa się z mikroserwisów i mogą one być budowane i uruchamiane niezależnie. Aby zbudować i 
stworzyć obraz Docker'a dla każdego z mikroserwisów, należy przejść do katalogu zawierającego wybrany 
mikroserwis, a następnie wpisać w konsoli polecenie:

> mvn clean package

Dzięki temu uzyskamy zbudowany program wraz z odpowiednim obrazem Docker'a gotowym do uruchomienia.

### Instrukcja uruchomienia testów
Aby uruchomić testy aplikacji wymagane jest zainstalowanie narzędzie Postman, który z kolei wymaga 
zainstalowania przeglądarki Google Chrome. 

Przeglądarkę Google Chrome można pobrać ze strony: https://www.google.com/chrome/browser/desktop/index.html

Narzędzie Postman można pobrać ze strony: https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop

Po uruchomianiu narzędzia Postman należy zaimportować stworzone testy, znajdujące się w pliku 
MicroservicesApi.postman_collection.json. Po zaimportowaniu testów należy zaimportować także zmienne 
środowiskowe z pliku address.postman_environment.json. Oba te pliki znajdują się w katalogu 
ApiPostmanTests.
Następnie uruchamiamy 'Collection Runner' (przycisk Runner), wybieramy stworzoną kolekcję testów 
(MicroservicesApi), zaznaczamy zaimportowane środowisko (address) i klikamy na przycisk Start Tests.
Wyniki testów będą wyświetlane na bierząco, po prawej stronie okna. Możliwe jest uruchomienie testów
wielokrotnie, z wybranym opóźnieniem między nimi.
