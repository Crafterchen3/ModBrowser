package com.deckerpw.modbrowser.api;

import com.deckerpw.modbrowser.ModBrowser;
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

public final class Modrinth {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "modbrowser-modrinth");
        thread.setDaemon(true);
        return thread;
    });

    public record Page(List<Mod> entries, Integer nextOffset) {
    }

    public static CompletableFuture<Page> searchModsPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.MOD), EXECUTOR);
    }

    public static CompletableFuture<Page> searchResourcePacksPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.RESOURCEPACK), EXECUTOR);
    }

    private static ModrinthAPI client = ModrinthAPI.rateLimited(null, "");

    private static FacetCollection facets(ProjectType projectType){
        FacetCollection collection = null;
        switch (projectType){
            case MOD ->
                collection = FacetCollection.builder()
                        .facets(
                                List.of(
                                        List.of(
                                                Facet.projectType(ProjectType.MOD)
                                        ),
                                        List.of(
                                                Facet.category("neoforge")
                                        ),
                                        List.of(
                                                Facet.version(ModBrowser.MC_VERSION)
                                        )
                                )
                        )
                        .build();
            case RESOURCEPACK ->
                    collection = FacetCollection.builder()
                            .facets(
                                    List.of(
                                            List.of(
                                                    Facet.projectType(ProjectType.RESOURCEPACK)
                                            ),
                                            List.of(
                                                    Facet.version(ModBrowser.MC_VERSION)
                                            )
                                    )
                            )
                            .build();
        }
        assert collection != null;
        ModBrowser.LOGGER.debug(collection.toString());
        return collection;
    }

    private static Page page(String query, int offset, int limit, ProjectType projectType) {
        SearchEndpoint.SearchResponse response = client.search(SearchEndpoint.SearchRequest.builder().query(query).offset(offset).limit(limit)
                        .facets(facets(projectType))
                        .index(SearchEndpoint.IndexType.RELEVANCE)
                .build()).join();
        ArrayList<Mod> list = new ArrayList<>();
        for (SearchEndpoint.SearchResult hit : response.getHits()) {
            list.add(new Mod(
                    hit.getProjectId(), hit.getIconUrl(), hit.getAuthor(), Component.literal(hit.getTitle()), Component.literal(hit.getDescription())
            ));
        }
        if (list.isEmpty())
            return new Page(list,null);
        return new Page(list,offset+list.size());
    }
}




