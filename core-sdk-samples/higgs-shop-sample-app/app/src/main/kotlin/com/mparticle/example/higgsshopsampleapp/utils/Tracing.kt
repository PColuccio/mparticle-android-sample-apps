package com.mparticle.example.higgsshopsampleapp.utils

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.contrib.awsxray.AwsXrayIdGenerator
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter
import io.opentelemetry.extension.aws.AwsXrayPropagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer


class Tracing {
    companion object {
        lateinit var openTelemetry : OpenTelemetrySdk
        private var initialized = false


        fun InitializeTracing(otelCollectorHost : String) {

            if (initialized) return

            val logExport: LoggingSpanExporter =
                LoggingSpanExporter
                    .create()

            val spanExporter: ZipkinSpanExporter =
                ZipkinSpanExporter
                    .builder()
                    .setEndpoint(otelCollectorHost)
                    .build()

            openTelemetry = OpenTelemetrySdk.builder()
                // This will enable your downstream requests to include the X-Ray trace header
                .setPropagators(
                    ContextPropagators.create(
                        TextMapPropagator.composite(
                            W3CTraceContextPropagator.getInstance(), AwsXrayPropagator.getInstance())))

                // This provides basic configuration of a TracerProvider which generates X-Ray compliant IDs
                .setTracerProvider(
                    SdkTracerProvider.builder()
                        .addSpanProcessor(
                            BatchSpanProcessor.builder(spanExporter).build())
                        .addSpanProcessor(
                            BatchSpanProcessor.builder(logExport).build())
                        .setIdGenerator(AwsXrayIdGenerator.getInstance())
                        .build())
                .buildAndRegisterGlobal();

            this.initialized = true
        }

        fun GetTracer() : Tracer {
            val tracer = this.openTelemetry.getTracer("MP-Android-Sample-App")
            return tracer
        }

        fun StartSpan(segmentName: String): Span {
            val tracer = GetTracer()
            return tracer.spanBuilder(segmentName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan()
                .setAttribute("service.name", "MP-Sample-Android-App")
        }
    }
}