package com.kezong.fataar

import com.android.build.api.variant.Variant
import com.android.build.gradle.api.LibraryVariant

final class VariantInfo {

    final String name
    final String buildTypeName
    final String flavorName
    final boolean minifyEnabled

    VariantInfo(String name, String buildTypeName, String flavorName, boolean minifyEnabled) {
        this.name = name
        this.buildTypeName = buildTypeName
        this.flavorName = flavorName
        this.minifyEnabled = minifyEnabled
    }

    static VariantInfo fromLegacy(LibraryVariant variant) {
        return new VariantInfo(
                variant.name,
                variant.buildType?.name,
                variant.flavorName,
                variant.buildType?.isMinifyEnabled() ?: false
        )
    }

    static VariantInfo fromNew(Variant variant) {
        boolean minifyEnabled = false
        try {
            minifyEnabled = variant.isMinifyEnabled()
        } catch (Exception ignore) {
            // Property not available on some variant types.
        }
        return new VariantInfo(
                variant.name,
                variant.buildType,
                variant.flavorName,
                minifyEnabled
        )
    }
}
