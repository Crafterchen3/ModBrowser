package com.deckerpw.modbrowser.api;

import com.deckerpw.modbrowser.ModBrowser;
import com.deckerpw.modrinth.Facets;
import com.deckerpw.modrinth.data.ProjectType;

public enum ModType {
    MOD(
            ProjectType.MOD,
            "mods",
            Facets.empty()
                .projectType(ProjectType.MOD)
                .category("neoforge")
                .version(ModBrowser.MC_VERSION)),
    RESOURCE_PACK(
            ProjectType.RESOURCEPACK,
            "resourcepacks",
            Facets.empty()
                .projectType(ProjectType.RESOURCEPACK)
                .version(ModBrowser.MC_VERSION)),
    SHADERS(
            ProjectType.SHADER,
            "shaderpacks",
            Facets.empty()
                    .projectType(ProjectType.SHADER)
                    .version(ModBrowser.MC_VERSION));

    public final ProjectType projectType;
    public final String folder;
    public final Facets facets;

    ModType(ProjectType projectType, String folder, Facets facets) {
        this.projectType = projectType;
        this.folder = folder;
        this.facets = facets;
    }
}
