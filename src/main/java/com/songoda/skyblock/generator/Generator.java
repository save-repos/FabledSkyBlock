package com.songoda.skyblock.generator;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.island.IslandWorld;

import java.util.List;

public class Generator {
    private final String name;
    private final IslandWorld isWorld;
    private final CompatibleMaterial materials;
    private final List<GeneratorMaterial> generatorMaterials;
    private final long level;
    private boolean permission;

    public Generator(String name, IslandWorld isWorld, CompatibleMaterial materials, List<GeneratorMaterial> generatorMaterials, long level, boolean permission) {
        this.name = name;
        this.isWorld = isWorld;
        this.materials = materials;
        this.generatorMaterials = generatorMaterials;
        this.level = level;
        this.permission = permission;
    }

    public String getName() {
        return this.name;
    }

    public IslandWorld getIsWorld() {
        return this.isWorld;
    }

    public CompatibleMaterial getMaterials() {
        return this.materials;
    }

    public List<GeneratorMaterial> getGeneratorMaterials() {
        return this.generatorMaterials;
    }

    public boolean isPermission() {
        return this.permission;
    }

    public String getPermission() {
        return "fabledskyblock.generator." + this.name.toLowerCase().replace(" ", "_");
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public long getLevel() {
        return this.level;
    }
}
