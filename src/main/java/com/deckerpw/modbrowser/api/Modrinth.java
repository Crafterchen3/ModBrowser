package com.deckerpw.modbrowser.api;

import com.deckerpw.modbrowser.ModBrowser;
import com.deckerpw.modbrowser.data.Mod;
import masecla.modrinth4j.endpoints.SearchEndpoint;
import masecla.modrinth4j.endpoints.version.GetProjectVersions;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.project.Project;
import masecla.modrinth4j.model.project.ProjectType;
import masecla.modrinth4j.model.search.Facet;
import masecla.modrinth4j.model.search.FacetCollection;
import masecla.modrinth4j.model.version.ProjectVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Modrinth {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "modbrowser-modrinth");
        thread.setDaemon(true);
        return thread;
    });
    private static final ModrinthAPI client = ModrinthAPI.rateLimited(null, "");

    public static CompletableFuture<Page> searchModsPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.MOD), EXECUTOR);
    }

    public static CompletableFuture<Page> searchResourcePacksPageAsync(String query, int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> page(query, offset, limit, ProjectType.RESOURCEPACK), EXECUTOR);
    }

    public static CompletableFuture<List<Mod>> resolveDependenciesAsync(List<Mod> mods) {
        return CompletableFuture.supplyAsync(() -> resolveDependencies(mods), EXECUTOR);
    }

    public static CompletableFuture<Void> downloadModAsync(Mod mod) {
        return CompletableFuture.runAsync(() -> downloadMod(mod), EXECUTOR);
    }

    private static FacetCollection facets(ProjectType projectType) {
        FacetCollection collection = null;
        switch (projectType) {
            case MOD -> collection = FacetCollection.builder()
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
            case RESOURCEPACK -> collection = FacetCollection.builder()
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
                    hit.getProjectId(), hit.getSlug(), hit.getIconUrl(), hit.getAuthor(), Component.literal(hit.getTitle()), Component.literal(hit.getDescription()),projectType
            ));
        }
        if (list.isEmpty())
            return new Page(list, null);
        return new Page(list, offset + list.size());
    }

    private static List<Mod> resolveDependencies(List<Mod> mods) {
        ArrayList<Mod> resolved = new ArrayList<>(mods.stream().filter(mod -> mod.version != null).toList());
        Deque<Mod> toVisit = new ArrayDeque<>(mods.stream().filter(mod -> mod.version == null).toList());
        while (!toVisit.isEmpty()) {
            Mod mod = toVisit.pop();
            if (resolved.contains(mod))
                continue;
            List<ProjectVersion> versions;
            if (mod.type == ProjectType.RESOURCEPACK){
                 versions = client.versions().getProjectVersions(mod.slug, GetProjectVersions.GetProjectVersionsRequest.builder()
                        .gameVersions(List.of(ModBrowser.MC_VERSION))
                        .build()).join();
            }else {
                versions = client.versions().getProjectVersions(mod.slug, GetProjectVersions.GetProjectVersionsRequest.builder()
                        .loaders(List.of("neoforge"))
                        .gameVersions(List.of(ModBrowser.MC_VERSION))
                        .build()).join();
            }
            if (versions.isEmpty())
                continue;
            ProjectVersion version = versions.getFirst();
            mod.version = version;
            resolved.add(mod);
            for (ProjectVersion.ProjectDependency dependency : version.getDependencies()) {
                if (dependency.getDependencyType() == ProjectVersion.ProjectDependencyType.REQUIRED) {
                    if (resolved.stream().anyMatch(m -> m.id.equals(dependency.getProjectId())))
                        continue;
                    Project project = client.projects().get(dependency.getProjectId()).join();
                    toVisit.add(new Mod(project.getId(), project.getSlug(), project.getIconUrl(), project.getTeam(), Component.literal(project.getTitle()), Component.literal(project.getDescription()),project.getProjectType()));
                }
            }
        }
        return resolved;
    }

    private static void downloadMod(Mod mod) {
        if (mod.version == null)
            return;
        mod.version.getFiles().stream().filter(ProjectVersion.ProjectFile::isPrimary).findFirst().ifPresent(file -> {
            String url = file.getUrl();
            Path destination = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve(mod.type == ProjectType.MOD ? "mods" : "resourcepacks")
                    .resolve(file.getFilename());
            try {
                Files.createDirectories(destination.getParent());
                try (InputStream inputStream = URI.create(url).toURL().openStream()) {
                    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception exception) {
                throw new RuntimeException("Failed to download " + url + " to " + destination, exception);
            }
        });
    }

    public record Page(List<Mod> entries, Integer nextOffset) {
    }

}




