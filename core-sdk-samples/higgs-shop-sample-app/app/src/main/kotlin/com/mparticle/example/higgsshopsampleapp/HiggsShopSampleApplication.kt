package com.mparticle.example.higgsshopsampleapp;

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import com.mparticle.MParticle
import com.mparticle.MParticleOptions
import com.mparticle.example.higgsshopsampleapp.utils.Tracing
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind


class HiggsShopSampleApplication: Application() {
    val TAG = "HiggsShopSampleApplication"
    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        java.util.logging.Level.FINE

        Tracing.InitializeTracing(BuildConfig.OTEL_COLLECTOR_HOST)


        val span: Span = Tracing.StartSpan("Application-Start-Up")
            //.setAttribute("Application-Start-Up", "name")
            //.setAttribute("user", "pc")
            .setAttribute("android-id", Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID))


        val options: MParticleOptions = MParticleOptions.builder(this)
            .credentials(BuildConfig.HIGGS_SHOP_SAMPLE_APP_KEY, BuildConfig.HIGGS_SHOP_SAMPLE_APP_SECRET)
            .environment(MParticle.Environment.Development)
            // logLevel can be 'NONE', 'ERROR', 'WARNING', 'DEBUG', 'VERBOSE', or 'INFO
            // (the default is 'DEBUG').
            // This logLevel provides context into the inner workings of mParticle.
            // It can be updated after MP has been initialized using mParticle.setLogLevel().
            // and passing.  Logs will be available in the inspector.
            // More can be found at https://docs.mparticle.com/developers/sdk/android/logger/
            .logLevel(MParticle.LogLevel.VERBOSE)
            .build()

        MParticle.start(options)

        span.end()

        println("Trace ID: ${span.spanContext.traceId}")
        println("Span ID: ${span.spanContext.spanId}")
    }
}