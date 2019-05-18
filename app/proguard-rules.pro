-dontobfuscate
-dontoptimize
#Dagger
-dontwarn com.google.errorprone.annotations.**

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule

-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

#Okio
-dontwarn okio.**
-dontwarn okhttp3.internal.platform.*

-dontnote org.apache.http.**
-dontnote android.net.http.**
-dontnote okhttp3.internal.platform.**
-dontnote com.google.android.gms.internal.**
-dontnote com.squareup.okhttp.internal.**

-dontnote com.crashlytics.android.core.CrashlyticsController
-dontnote com.google.common.util.concurrent.MoreExecutors
-dontnote com.google.gson.internal.UnsafeAllocator
-dontnote com.subgraph.orchid.crypto.PRNGFixes
-dontnote org.bitcoinj.crypto.DRMWorkaround
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote org.chalup.microorm.ClassFactory
-dontnote io.fabric.sdk.android.FabricKitsFinder
-dontnote go.LoadJNI

-dontnote org.bitcoinj.**
-dontnote com.facebook.**
-dontnote com.google.android.gms.**
-dontnote com.journeyapps.barcodescanner.**
-dontnote com.google.firebase.iid.FirebaseInstanceId
-dontnote com.teambrella.android.ui.widget.VoterBar
-dontnote com.ortiz.touch.TouchImageView
-dontnote com.android.vending.billing.IInAppBillingService
-dontnote com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-dontnote com.bumptech.glide.integration.okhttp.OkHttpGlideModule



#bitcoinj
-dontwarn org.bitcoinj.store.LevelDBBlockStore
-dontwarn org.bitcoinj.store.LevelDBFullPrunedBlockStore
-dontwarn org.bitcoinj.store.LevelDBFullPrunedBlockStore$BloomFilter
-dontwarn org.bitcoinj.store.WindowsMMapHack
-dontwarn org.slf4j.LoggerFactory
-dontwarn org.slf4j.MarkerFactory
-dontwarn org.slf4j.MDC

-keep class org.ethereum.** {
   *;
}

-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}


#Retrofit
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
