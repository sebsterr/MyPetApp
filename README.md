PetCareSmart - Aplicatie Android pentru Gestionarea Animalelor de Companie
Acest proiect reprezinta o aplicatie mobila nativa Android, dezvoltata in limbajul Kotlin utilizand paradigma declarativa Jetpack Compose si arhitectura MVVM. Aplicatia integreaza servicii Backend-as-a-Service (Google Firebase) si Inteligenta Artificiala Generativa (Gemini 2.5 Flash) pentru asistenta veterinara virtuala.

1. Adresa Repository-ului
Intregul cod sursa al aplicatiei este gazduit cu vizibilitate Publica si poate fi accesat la urmatorul link:

Adresa Repository: https://github.com/sebsterr/PetCareSmart

2. Pasii de compilare ai aplicatiei
Pentru a compila aplicatia din codul sursa, este necesar un mediu de dezvoltare configurat pentru Android.

Cerinte preliminare:
IDE: Android Studio (versiunea recomandata: minim Iguana sau Jellyfish).

SDK: Android SDK configurat (Target API minim: 34).

Pasi de urmat:
Clonarea proiectului:
Deschideti terminalul sau Git Bash si rulati comanda:
git clone [https://github.com/sebsterr/PetCareSmart.git](https://github.com/sebsterr/PetCareSmart.git)

Deschiderea in Android Studio:
Lansati Android Studio, selectati File -> Open si navigati catre folderul unde ati clonat proiectul (asigurati-va ca selectati folderul care contine fisierul build.gradle.kts principal).

Sincronizarea dependentelor:
Faceti click pe butonul "Sync Project with Gradle Files" (pictograma cu elefant) din bara superioara a Android Studio pentru a descarca toate librariile necesare (Jetpack Compose, Firebase, WorkManager, etc.).

Compilarea codului (Build):
Dupa finalizarea sincronizarii, din meniul superior selectati Build -> Make Project (sau folositi scurtatura Ctrl + F9). Verificatorul de erori va confirma ca aplicatia s-a compilat cu succes.

3. Pasii de instalare si lansare a aplicatiei
Lansarea aplicatiei se poate face fie pe un dispozitiv fizic, fie pe un emulator Android direct din mediul de dezvoltare.

Rularea prin Android Studio 
Configurarea dispozitivului:

Emulator: Porniti un dispozitiv virtual din Device Manager (recomandat un emulator cu Google Play Services activat).

Dispozitiv fizic: Conectati telefonul prin cablu USB si asigurati-va ca modul USB Debugging (Depanare USB) este activat din optiunile dezvoltatorului (Developer Options).

Lansarea:

Asigurati-va ca dispozitivul apare in lista de Running Devices din meniul de sus al Android Studio.

Apasati butonul verde de tip Play (Run 'app') sau folositi scurtatura Shift + F10.

Android Studio va genera automat fisierul APK, il va instala pe dispozitiv si va lansa aplicatia pe ecranul principal (ecranul de Login).
