package com.deckerpw.modbrowser.api;

import com.deckerpw.modbrowser.data.Mod;
import masecla.modrinth4j.endpoints.SearchEndpoint;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.project.ProjectType;
import masecla.modrinth4j.model.search.Facet;
import masecla.modrinth4j.model.search.FacetCollection;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MockModrinthApi {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "modbrowser-mock-modrinth");
        thread.setDaemon(true);
        return thread;
    });

    private static final List<Mod> MODS = List.of(
        new Mod("sodium", "", Component.literal("CaffeineMC"), Component.literal("Sodium"), Component.literal("A modern rendering engine for Minecraft.")),
        new Mod("lithium", "", Component.literal("CaffeineMC"), Component.literal("Lithium"), Component.literal("A general-purpose optimization mod for Minecraft.")),
        new Mod("iris", "", Component.literal("Iris Shaders"), Component.literal("Iris"), Component.literal("Adds shader support for compatible rendering mods.")),
        new Mod("jade", "", Component.literal("Snownee"), Component.literal("Jade"), Component.literal("Shows useful information about blocks and entities.")),
        new Mod("appleskin", "", Component.literal("Squeek502"), Component.literal("AppleSkin"), Component.literal("Adds food value information to the HUD."))
    );

    private static final List<Mod> RESOURCE_PACKS = List.of(
        new Mod("faithful-32x", "", Component.literal("Faithful Team"), Component.literal("Faithful 32x"), Component.literal("A faithful-style resource pack with clean textures.")),
        new Mod("vanilla-tweaks", "", Component.literal("Vanilla Tweaks"), Component.literal("Vanilla Tweaks"), Component.literal("A collection of lightweight resource pack tweaks.")),
        new Mod("stay-true", "", Component.literal("Hixis"), Component.literal("Stay True"), Component.literal("A vanilla-inspired resource pack with softer textures."))
    );

    private MockModrinthApi() {
    }

    public record Page(List<Mod> entries, Integer nextOffset) {
    }

    public static CompletableFuture<Page> searchModsPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.MOD), EXECUTOR);
    }

    public static CompletableFuture<Page> searchResourcePacksPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.RESOURCEPACK), EXECUTOR);
    }

    private static List<Mod> filter(List<Mod> entries, String query) {
        String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (filter.isEmpty()) {
            return entries;
        }

        return entries.stream()
            .filter(entry -> matches(entry, filter))
            .toList();
    }

    private static boolean matches(Mod entry, String filter) {
        return entry.id().toLowerCase(Locale.ROOT).contains(filter)
            || entry.name().getString().toLowerCase(Locale.ROOT).contains(filter)
            || entry.summary().getString().toLowerCase(Locale.ROOT).contains(filter)
            || entry.author().getString().toLowerCase(Locale.ROOT).contains(filter);
    }


    private static ModrinthAPI client = ModrinthAPI.rateLimited(null, "");

    private static Page page(String query, int offset, int limit, ProjectType projectType) {
        SearchEndpoint.SearchResponse response = client.search(SearchEndpoint.SearchRequest.builder().query(query).offset(offset).limit(limit)
                        .index(SearchEndpoint.IndexType.RELEVANCE)
                .build()).join();
        ArrayList<Mod> list = new ArrayList<>();
        for (SearchEndpoint.SearchResult hit : response.getHits()) {
            list.add(new Mod(
                    hit.getProjectId(), hit.getIconUrl(), Component.literal(hit.getAuthor()), Component.literal(hit.getTitle()), Component.literal(hit.getDescription())
            ));
        }
        if (list.isEmpty())
            return new Page(list,null);
        return new Page(list,offset+list.size());
    }
}




