# SimpleMinecraftLauncher

🇹🇷:

SimpleMinecraftLauncher Java ile yazılmış, oyun kaynaklarını indirme, jre(java runtime environment) indirme, sürüm seçme ve oyunun çalışmasını sağlayan dosyaları indirme özelliklerine sahiptir. Komut istemcisinde kullanılabilir. En yakın zamanda GUI ekleyeceğiz.

Kullanım:  
  java -jar SimpleMinecraftLauncher.jar -username Oyuncu123 -version 1.8  

Args:   
  -usejre (isteğe bağlı): Bir jre indirir(eğer bulunmuyorsa) ve onu kullanır.  
  -username <kullanıcıadı> (zorunlu): Oyuncunun kullanıcı adı  
  -version <sürüm> (zorunlu): Oynamak istediğiniz sürüm  
  -skipupdate (isteğe bağlı): Güncellemeleri kontrol etmez(SmlCore'u çalıştırmaz)  

Katkıda Bulunanlar:  
  Oyun Kaynaklarını İndirme -> Ahmetflix  
  Oyun Kütüphane Dosyalarını İndirme -> Ahmetflix  
  Oyunun Çalışmasını Sağlayan Dosyaları İndirme -> Fatih Ulu  
  VM Args ve Program Args : Fatih Ulu  
  JRE İndirme : Fatih Ulu and Ahmetflix  
  SmlCore(Güncelleyici) : Ahmetflix  

🇺🇸: 

SimpleMinecraftLauncher written by Java has assets downloader, library downloader, jre download, version select, native unpack. Can be used in command line. As soon as we add the gui.

Usage:  
  java -jar SimpleMinecraftLauncher.jar -username Player123 -version 1.8

Args:  
  -usejre (optional): Downloads a jre(if not exists) and uses it.  
  -username <username> (required): Username of the player  
  -version <version> (required): Version of the you want to play  
  -skipupdate (optional): Dont checks updates(Not starts SmlCore)  

Credits:  
  Downloading Assets by Ahmetflix  
  Downloading Libraries by Ahmetflix  
  Unpack Natives by Fatih Ulu  
  VM Args and Program Args by Fatih Ulu  
  JRE Downloading by Fatih Ulu and Ahmetflix  
  SmlCore(Updater) by Ahmetflix  
