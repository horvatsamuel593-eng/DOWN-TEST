DOWN TEST — Android Studio projekt (zdrojový kód)

Toto je skutečný nativní Android projekt, nikoli APK. APK se musí sestavit v Android Studiu.

Jak z něj vytvořit APK:
1. Na počítači nainstaluj Android Studio z oficiální stránky developer.android.com/studio.
2. Rozbal tento ZIP.
3. V Android Studiu zvol Open a vyber složku DOWN_TEST_Android_Project.
4. Počkej na Gradle Sync a případně potvrď instalaci Android SDK Platform 35.
5. V menu Build zvol Build Bundle(s) / APK(s) > Build APK(s).
6. Android Studio vytvoří APK a zobrazí odkaz k jeho umístění.
7. Přenes APK do telefonu a otevři ho. Instalaci povol jen pokud víš, že APK pochází z tvého vlastního projektu.

Co aplikace obsahuje:
- nativní Android obrazovku s tmavým/fialovým vzhledem;
- živý náhled přední kamery (vyžádá oprávnění);
- animovaný průběh testu;
- náhodný výsledek „Down: 0–100 %“;
- historii posledních výsledků během běhu aplikace;
- sdílení výsledku přes nabídku Androidu.

Poznámky:
- Aplikace nevyhodnocuje obličej. Číslo je náhodné, čistě pro zábavu.
- Historie je zatím pouze v paměti aplikace a po jejím ukončení se smaže.
- Toto ještě není podepsaný release build ani publikace v Google Play.
- Pro zveřejnění je potřeba dokončit ikonu, screenshoty, zásady ochrany soukromí, testování a Play Console účet. Google Play vyžaduje AAB pro nové aplikace.
