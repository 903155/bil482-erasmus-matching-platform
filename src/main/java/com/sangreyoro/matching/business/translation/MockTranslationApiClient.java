package com.sangreyoro.matching.business.translation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ADAPTER pattern – role: Adaptee. Simulates the third-party translation API (scope: the real API is out of
 * scope, SRS 1.2). Its interface is deliberately different from ours: other method name, parameter order,
 * upper-case language codes and an HTTP-like status code instead of exceptions.
 * <p>
 * It knows the translations of the demo profile descriptions and can be switched off to test UC2-E4.
 */
public class MockTranslationApiClient {
    private final Map<String, Map<String, String>> dictionary = new HashMap<>();
    private volatile boolean online = true;
    private final AtomicInteger calls = new AtomicInteger();

    public MockTranslationApiClient() {
        add("Me encanta la fotografía y cocinar comida española.",
                "en", "I love photography and cooking Spanish food.",
                "tr", "Fotoğrafçılığı ve İspanyol yemekleri pişirmeyi çok seviyorum.");
        add("I like board games and watching European films.",
                "es", "Me gustan los juegos de mesa y ver películas europeas.",
                "tr", "Kutu oyunlarını ve Avrupa filmleri izlemeyi seviyorum.");
        add("Architecture student who loves drawing old buildings.",
                "es", "Estudiante de arquitectura a quien le encanta dibujar edificios antiguos.",
                "tr", "Eski binaları çizmeyi seven bir mimarlık öğrencisi.");
        add("I play guitar and I want to visit every city in Turkey.",
                "es", "Toco la guitarra y quiero visitar todas las ciudades de Turquía.",
                "tr", "Gitar çalıyorum ve Türkiye'deki her şehri gezmek istiyorum.");
        add("Hackathons, hiking and board games are my favourite things.",
                "es", "Los hackathons, el senderismo y los juegos de mesa son mis cosas favoritas.",
                "tr", "Hackathonlar, doğa yürüyüşü ve kutu oyunları en sevdiğim şeyler.");
        add("Quiero practicar turco y enseñar a cocinar paella.",
                "en", "I want to practise Turkish and teach how to cook paella.",
                "tr", "Türkçe pratik yapmak ve paella yapmayı öğretmek istiyorum.");
        add("Fotoğraf çekmeyi ve doğa yürüyüşlerini seviyorum.",
                "en", "I love taking photos and going hiking.",
                "es", "Me encanta hacer fotos y hacer senderismo.");
        add("Mimarlık öğrencisiyim, müzeleri ve resim yapmayı seviyorum.",
                "en", "I am an architecture student and I love museums and painting.",
                "es", "Soy estudiante de arquitectura y me encantan los museos y pintar.");
        add("Kutu oyunları ve basketbol oynamayı seviyorum.",
                "en", "I love playing board games and basketball.",
                "es", "Me encanta jugar a juegos de mesa y al baloncesto.");
        add("I want to practise French and Spanish before my Erasmus.",
                "es", "Quiero practicar francés y español antes de mi Erasmus.",
                "tr", "Erasmus'umdan önce Fransızca ve İspanyolca pratik yapmak istiyorum.");
        add("Yazılım ve voleybol hakkında konuşmayı severim.",
                "en", "I like talking about software and volleyball.",
                "es", "Me gusta hablar de software y de voleibol.");
        add("Futbol ve yemek fotoğrafçılığı benim tutkum.",
                "en", "Football and food photography are my passion.",
                "es", "El fútbol y la fotografía de comida son mi pasión.");
    }

    private void add(String original, String l1, String t1, String l2, String t2) {
        Map<String, String> m = new HashMap<>();
        m.put(l1.toUpperCase(), t1);
        m.put(l2.toUpperCase(), t2);
        dictionary.put(original, m);
    }

    /** Third-party signature: (target, source, payload) with upper-case ISO codes. */
    public TranslationApiResponse translateText(String targetIso, String sourceIso, String payload) {
        calls.incrementAndGet();
        if (!online) return new TranslationApiResponse(503, null, "Service Unavailable");
        if (payload == null || payload.isBlank()) return new TranslationApiResponse(400, null, "Empty payload");
        String t = dictionary.getOrDefault(payload, Map.of()).get(targetIso);
        if (t == null) return new TranslationApiResponse(404, null, "No translation " + sourceIso + "->" + targetIso);
        return new TranslationApiResponse(200, t, null);
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int calls() {
        return calls.get();
    }
}
