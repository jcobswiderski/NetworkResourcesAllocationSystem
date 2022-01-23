Projekt zaliczeniowy SKJ 2021/22

Spis treści:
 1. Szczegółowy opis implementacji
  1.1. Struktura przechowywania informacji w Mapach
  1.2. Modyfikacja procesu klienta
 2. Jak skompilować i zainstalować?
  2.1. Uruchomienie węzła
  2.2. Uruchomienie klienta
  2.3. UWAGA! Problem z automatycznymi skryptami testującymi
 3. Co zostało zaimplementowane?
 4. Co nie działa?

1. Szczegółowy opis implementacji

 Rozwiązanie opiera się na dwóch podprogramach: NetworkNode oraz NetworkClient. NetworkNode jest niezależną klasą opartą na mechanizmie wątków. Każdy utworzony węzeł stanowi odrębny wątek. Węzły komunikują się z klientami przy użyciu   protokołu TCP. Każdy węzeł w trakcie tworzenia staje się serwerem TCP (ServerSocket) i oczekuje na połączenia od klientów. Do każdego węzła możemy podłączyć nieograniczoną liczbę innych węzłów oraz klientów (jeżeli liczba zasobów na to   pozwala). Klient jest prostym procesem, który ma za zadanie wysłać do sieci żądanie rezerwacji zasobów w odpowiednim formacie i poczekać na odpowiedź. Implementacja nie zakłada ściśle określonego limitu podłączonych węzłów oraz  klientów. Każdy uruchomiony proces (węzeł) może dysponować określonymi zasobami. Klient łącząc się z węzłem alokuje określone zasoby wraz z ich  ilością. Alokacja zasobów poprzedzona jest sprawdzeniem czy węzeł dysponuje odpowiednią  liczbą zasobów. Jeżeli węzeł nie dysponuje określonymi węzłami w podanej ilości to komunikacja kończy się przesłaniem do klienta flagi FAILED. Zaalokowane zasoby nie mogą być zajęte przez innych klientów do momentu aż klient, który je  zajął  nie dokona zwrotu. Zwrot zasobów możliwy jest poprzez wywołanie polecenia NetworkClient wraz z flagą terminate oraz parametrami -ident i -gateway. Stan zasobów danego węzła przechowywany jest w dwóch strukturach HashMap. Pierwsza  - availableResources przechowuje kolekcję zasobów, które są dostępne i oczekują na alokację. Druga mapa - occupiedResources przechowuje aktualny stan zajętych zasobów ich liczebność oraz identyfikatory klientów, którzy je zajmują.    Ewidencja zasobów odbywa się w sposób dynamiczny, wiele klientów może zajmować wiele różnych zasobów w tym samym czasie u jednego węzła. Klienci mogą alokować jednocześnie zasoby od wielu różnych węzłów, gdzie każda alokacja poprzedzona  jest wykonaniem odpowiedniego polecenia klienta. Dla lepszej czytelności działania programu stworzone zostały statyczne metody pozwalające na wyświetlanie aktualnego stanu zasobów po każdej operacji podjętej przez węzeł bądź klienta. Po  nawiązaniu połączenia między danymi instancjami do bezpośredniej komunikacji wykorzystane zostały klasy BufferedReader oraz PrintWriter. Od strony technicznej większość operacji takich jak przetwarzanie przesyłanych danych odbywa się za  pomocą prostych mechanizmów takich jak metody klasy String (split) czy metody wbudowanej HashMapy (get, put...). Stan zasobów przechowywany jest w węźle, nie w kliencie. Klient po uzyskaniu połączenia z węzłem przesyła linię tekstu   postaci: <identyfikator> <zasób>:<liczność> [<zasób>:liczność]; Węzeł sprawdza czy jest w stanie spełnić oczekiwania klienta po czym odsyła mu n-wierszy postaci: <zasób>:<liczność>:<ip węzła>:<port węzła> - w przypadku pozytywnej   alokacji lub FAILED w przypadku niedoboru któregoś z zasobów. Niezależnie od rezultatu alokacji połączenie z klientem jest zamykane. Aby zwrócić zaalokowane zasoby, klient musi ponownie na chwilę połączyć się z węzłem aby przesłać mu   informację o chęci zwolnienia zasobów (flaga terminate).

 UWAGA: Projekt wdrażany i testowany wyłącznie na systemie Windows, nie gwarantuję poprawności działania w przypadku uruchomienia na innych systemach operacyjnych.

1.1 Struktura przechowywania informacji w Mapach

 Wszelkie dane związane z przechowywaniem stanu zasobów danego węzła przechowywane są w strukturach zwanych Mapami (HashMap). Implementacja zakłada istnienie dwóch oddzielnych map, jedna dla zasobów dostępnych, oczekujących na alokację;  druga zaś dla zasobów zajętych oraz identyfikatorów klientów, którzy aktualnie je zajmują. Zajęcie bądź zwolnienie zasobów wiąże się z automatyczną aktualizacją obu map.

 availableResources<String, Integer>	<- <nazwa zasobu, licznosc zasobu>
 occupiedResources<String, String>	<- <id klienta, seria zasobow postaci "A:3 B:1...">

 Przykładowa zawartość mapy availableResources:
	{ ["A", 3], ["F", 14], ["X", 0] }

 Przykładowa zawartość mapy occupiedResources:
	{ ["201", "A:1 B:7"], ["202", "A:4"] }

2. Jak skompilować i zainstalować?

 Do obsługi projektu stworzony został jeden skrypt znajdujący się w katalogu S22773_PROJEKT pod nazwą uruchomienie.bat; Jest to prosty program wsadowy pozwalający na kompilację plików źródłowych projektu tj. NetworkClient.java oraz    NetworkNode.java; Po wykonaniu tego pliku (dwukrotne kliknięcie) skrypt dokona kompilacji i uruchomi CMD. Jeżeli kompilacja przebiegnie pomyślnie w pierwszej linii okna poleceń powinna wyświetlić się informacja "Kompilacja przebiegla    pomyslnie, mozesz uruchomic wezel lub klienta;". W uruchomionym oknie poleceń możemy przystąpić do testowania programu - za jego pomocą odpowiednimi komendami opisanymi poniżej możemy tworzyć węzły (NetworkNode) oraz klientów    (NetworkClient). Jeżeli nie chcemy korzystać z otwartej konsoli, ponieważ potrzebujemy wyłącznie kompilacji wystarczy zamknąć CMD. Pliki zostały skompilowane, można na nich operować bezpośrednio z systemu.

2.1 Uruchomienie węzła:

 java NetworkNode -ident <identyfikator> -tcpport <port> [-gateway <ip:port>] [<zasób:liczebność> ...]

 Parametry:
  -ident (...)		<- pozwala na nadanie unikalnego identyfikatora węzła
  -tcpport (...)		<- pozwala na określenie portu dostępowego dla nowych węzłów oraz klientów
  [-gateway (...)]	<- parametr opcjonalny; nie jest wymagany jeżeli tworzymy pierwszy węzeł - bez rodziców; pozwala na określenie ip oraz portu węzła źródłowego, do którego podłączamy nowo tworzony węzeł
  [a:x c:y e:z]		<- opcjonalny ciąg zasobów jakimi ma dysponować węzeł np. A:3 B:7 H:1

 Przykładowe wywołania nowych węzłów:
  java NetworkNode -ident 101 -tcpport 9991 G:1 F:12 I:1
  java NetworkNode -ident 102 -tcpport 9992 -gateway localhost:9991 A:3 C:2
  java NetworkNode -ident 103 -tcpport 9993 -gateway localhost:9991 D:3 C:1
  java NetworkNode -ident 103 -tcpport 9994 -gateway localhost:9992 A:7 B:3 C:9

2.2. Uruchomienie klienta:

 java NetworkClient -ident <identyfikator> -gateway <ip:port> [<zasób:liczebność> ...] [terminate]

 Parametry:
  -ident (...)		<- pozwala na nadanie unikalnego identyfikatora klienta
  -gateway (...)	<- pozwala na określenie ip oraz portu węzła docelowego, do którego podłączamy naszego klienta
  [A:1 F:5 G:14]	<- ciąg zasobów oddzielonych spacją, które chcemy zaalokować od powyższego węzła; nie musimy podawać jeżeli korzystamy z flagi terminate
  [terminate]		<- opcjonalny parametr podawany NA KOŃCU wywołania, pozwala na zwrot zaalokowanych zasobów do węzła źródłowego; wywołanie WYMAGA parametru -gateway oraz -ident

 Przykładowe wywołania nowych klientów:
  java NetworkClient -ident 201 -gateway localhost:9991 F:24 A:13
  java NetworkClient -ident 201 -gateway localhost:9991 F:2
  java NetworkClient -ident 33 -gateway localhost:9994 terminate

2.3. Problem z automatycznymi skryptami testującymi

 UWAGA! W skryptach testujących dostarczonych przez wykładowcę na Gakko, występuje błąd logiczny. W przypadku wywołania polecenia NetworkNode bez podawania parametru -ident, jednoznacznie identyfikującego danego klienta nie ma możliwości  zwrócenia zaalokowanych zasobów. Wynika to z faktu, że próbujemy jako klient wysłać do węzła polecenie zwrócenia zasobów ale nie podajemy informacji kim jesteśmy. Węzeł nie wie, który klient zwraca zasoby. Nie może zatem przywrócić ich   wszystkich, bo inne węzły być może aktualnie z nich korzystają. Rozwiązaniem tego problemu jest implementacja parametru -ident w każdym poleceniu, w którym wykorzystujemy flagę terminate. Próba zakończenia połączenia za pomocą terminate  bez podawania parametru ident skutkuje zamknięciem procesu klienta, natomiast zasoby które zajął pozostają zarezerwowane w procesie węzła. 

 Celem rozwiązania tej nieścisłości w implementacji uwzględniono drobną zmianę w procesie klienta związaną z przesyłaniem id klienta do procesu w przypadku chęci zwrotu zasobów poleceniem terminate.

3. Co zostało zaimplementowane?

 Zgodnie ze specyfikacją zaimplementowane zostały wszystkie podpunkty 3.1, 3.2, 3.3 oraz częściowo 3.4;

 - Model węzła oraz mechanizmy do komunikacji
 - Model klienta oraz mechanizmy do komunikacji
 - Mechanizm alokowania zasobów
 - Zabezpieczenie przed zaalokowaniem tych samych zasobów przez wielu klientów jednocześnie
 - Obsługa poleceń wraz z parametrami przez konsolę
 - Mechanizm alokacji zasobów tylko w sytuacji, kiedy węzeł jest w stanie zaalokować wszystkie żądane zasoby. Nie ma możliwości alokacji części zasobów.
 - Kolekcja zapisująca aktualną listę dostępnych zasobów
 - Kolekcja przechowująca informację o zajętych zasobach oraz klientach, którzy je zaalokowali
 - Funkcje wyświetlania stanu zasobów po każdej operacji

4. Co nie działa?

 W implementacji nie został uwzględniony rekurencyjny mechanizm udostępniania zasobów z węzłów nadrzędnych w sytuacji gdy węzeł bazowy, od którego klient domaga się określonych zasobów nie będzie posiadał odpowiedniej ich liczby. To   znaczy, że klient zarządający alokacji zasobu przykładowo C w ilości 3 nie będzie szukał w węzłach nadrzędnych poza bezpośrednim węzłem, do którego jest podłączony.

 W implementacji założone zostało, że wszystkie węzły pracują na localhoście.
