package net.dragonloot.client;

import java.util.Objects;
import java.util.Optional;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.dragonloot.config.DragonLootConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

final class DragonLootConfigScreen {
    private static final String TRANSLATION_PREFIX = "text.autoconfig.dragonloot.";

    private DragonLootConfigScreen() {
    }

    static Screen create(Screen parent) {
        ForgeConfigSpec config = DragonLootConfig.SPEC;
        ConfigBuilder screen = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslationTextComponent(TRANSLATION_PREFIX + "title"))
                .setDefaultBackgroundTexture(new ResourceLocation("minecraft", "textures/block/stone.png"))
                .setSavingRunnable(config::save);
        ConfigCategory category = screen.getOrCreateCategory(new TranslationTextComponent(TRANSLATION_PREFIX + "category.general"));
        ConfigEntryBuilder entries = screen.entryBuilder();

        for (String key : config.getSpec().valueMap().keySet()) {
            ForgeConfigSpec.ValueSpec spec = config.getSpec().get(key);
            Object value = config.getValues().get(key);
            ITextComponent label = new TranslationTextComponent(TRANSLATION_PREFIX + "option." + key);

            // Forge 1.16 keeps Range package-private. Clamping the extremes gives its endpoints.
            if (value instanceof ForgeConfigSpec.IntValue) {
                ForgeConfigSpec.IntValue number = (ForgeConfigSpec.IntValue) value;
                int current = number.get();
                int min = (Integer) spec.correct(Integer.MIN_VALUE);
                int max = (Integer) spec.correct(Integer.MAX_VALUE);
                var field = key.startsWith("dragon_armor_protection_")
                        ? entries.startIntSlider(label, current, min, max)
                        : entries.startIntField(label, current).setMin(min).setMax(max);
                field.setDefaultValue((Integer) spec.getDefault());
                addEntry(category, field, number, current, spec);
            } else if (value instanceof ForgeConfigSpec.DoubleValue) {
                ForgeConfigSpec.DoubleValue number = (ForgeConfigSpec.DoubleValue) value;
                double current = number.get();
                double min = (Double) spec.correct(-Double.MAX_VALUE);
                double max = (Double) spec.correct(Double.MAX_VALUE);
                var field = entries.startDoubleField(label, current)
                        .setDefaultValue((Double) spec.getDefault())
                        .setMin(min).setMax(max)
                        .setErrorSupplier(input -> Double.isFinite(input) ? Optional.empty()
                                : Optional.of(new TranslationTextComponent("text.cloth-config.error.not_valid_number_double")));
                addEntry(category, field, number, current, spec);
            } else if (value instanceof ForgeConfigSpec.BooleanValue) {
                ForgeConfigSpec.BooleanValue toggle = (ForgeConfigSpec.BooleanValue) value;
                boolean current = toggle.get();
                var field = entries.startBooleanToggle(label, current).setDefaultValue((Boolean) spec.getDefault());
                addEntry(category, field, toggle, current, spec);
            }
        }

        return screen.build();
    }

    private static <T> void addEntry(ConfigCategory category, AbstractFieldBuilder<T, ?, ?> field,
            ForgeConfigSpec.ConfigValue<T> value, T initialValue, ForgeConfigSpec.ValueSpec spec) {
        field.setSaveConsumer(newValue -> {
            if (!Objects.equals(initialValue, newValue)) {
                value.set(newValue);
            }
        });
        field.requireRestart(spec.needsWorldRestart());
        category.addEntry(field.build());
    }
}
