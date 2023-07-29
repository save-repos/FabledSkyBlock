package com.songoda.skyblock.localization.type.impl;

import com.craftaro.core.compatibility.CompatibleMaterial;

public class MaterialsToMaterialLocalization extends EnumLocalization<CompatibleMaterial> {
    public MaterialsToMaterialLocalization(String keysPath) {
        super(keysPath, CompatibleMaterial.class);
    }

    @Override
    public CompatibleMaterial parseEnum(String input) {
        return CompatibleMaterial.getMaterial(input);
    }
}
