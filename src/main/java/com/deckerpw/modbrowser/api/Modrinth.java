package com.deckerpw.modbrowser.api;

import com.deckerpw.modbrowser.ModBrowser;
import com.deckerpw.modbrowser.data.Mod;
import com.deckerpw.modrinth.Facets;
import com.deckerpw.modrinth.ModrinthAPI;
import com.deckerpw.modrinth.data.Project;
import com.deckerpw.modrinth.data.ProjectType;
import com.deckerpw.modrinth.data.Version;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Modrinth {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "modbrowser-modrinth");
        thread.setDaemon(true);
        return thread;
    });
    private static final ModrinthAPI client = new ModrinthAPI();

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

    private static Facets facets(ProjectType projectType) {
        Facets collection = null;
        switch (projectType) {
            case MOD -> collection = Facets.empty()
                    .projectType(ProjectType.MOD)
                    .category("neoforge")
                    .version(ModBrowser.MC_VERSION);
            case RESOURCEPACK -> collection = Facets.empty()
                    .projectType(ProjectType.RESOURCEPACK)
                    .version(ModBrowser.MC_VERSION);
        }
        return collection;
    }

    private static Page page(String query, int offset, int limit, ProjectType projectType) {
        List<Project> projects = client.search(query, facets(projectType), offset, limit).join();
        ArrayList<Mod> list = new ArrayList<>();
        for (Project hit : projects) {
            list.add(new Mod(
                    hit.id, hit.slug, hit.getIcon(), hit.author, Component.literal(hit.name), Component.literal(hit.summary), projectType
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
            Version version;
            if (mod.type == ProjectType.RESOURCEPACK) {
                version = client.getProjectVersion(mod.slug, null, ModBrowser.MC_VERSION).join();
            } else {
                version = client.getProjectVersion(mod.slug, "neoforge", ModBrowser.MC_VERSION).join();
            }
            mod.version = version;
            resolved.add(mod);
            for (Version.VersionDependency dependency : version.dependencies) {
                if (dependency.type == Version.VersionDependency.DependencyType.REQUIRED) {
                    if (resolved.stream().anyMatch(m -> m.id.equals(dependency.projectId)))
                        continue;
                    Project project = client.getProject(dependency.projectId).join();
                    toVisit.add(new Mod(project.id, project.slug, project.getIcon(), project.author, Component.literal(project.name), Component.literal(project.summary), project.type));
                }
            }
        }
        return resolved;
    }

    private static void downloadMod(Mod mod) {
        if (mod.version == null)
            return;
        Version.VersionFile file = mod.version.primaryFile;
        String url = file.url;
        Path destination = Minecraft.getInstance().gameDirectory.toPath()
                .resolve(mod.type == ProjectType.MOD ? "mods" : "resourcepacks")
                .resolve(file.filename);
        try {
            Files.createDirectories(destination.getParent());
            try (InputStream inputStream = URI.create(url).toURL().openStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to download " + url + " to " + destination, exception);
        }
    }

    public record Page(List<Mod> entries, Integer nextOffset) {
    }

}




