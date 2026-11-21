package net.dragonloot.client;

import java.util.Objects;
import java.util.Optional;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.dragonloot.config.DragonLootConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

final class DragonLootConfigScreen {
    private static final String TRANSLATION_PREFIX = "text.autoconfig.dragonloot.";

    private DragonLootConfigScreen() {
    }

    static Screen create(Screen parent) {
        ForgeConfigSpec config = DragonLootConfig.SPEC;
        ConfigBuilder screen = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(TRANSLATION_PREFIX + "title"))
                .setDefaultBackgroundTexture(new ResourceLocation("minecraft", "textures/block/stone.png"))
                .setSavingRunnable(config::save);
        ConfigCategory category = screen.getOrCreateCategory(Component.translatable(TRANSLATION_PREFIX + "category.general"));
        ConfigEntryBuilder entries = screen.entryBuilder();

        for (String key : config.getSpec().valueMap().keySet()) {
            ForgeConfigSpec.ValueSpec spec = config.getSpec().get(key);
            Object value = config.getValues().get(key);
            Component label = Component.translatable(TRANSLATION_PREFIX + "option." + key);

            if (value instanceof ForgeConfigSpec.IntValue number) {
                int current = number.get();
                ForgeConfigSpec.Range<Integer> range = spec.getRange();
                var field = key.startsWith("dragon_armor_protection_")
                        ? entries.startIntSlider(label, current, range.getMin(), range.getMax())
                        : entries.startIntField(label, current).setMin(range.getMin()).setMax(range.getMax());
                addEntry(category, field, number, current, spec);
            } else if (value instanceof ForgeConfigSpec.DoubleValue number) {
                double current = number.get();
                ForgeConfigSpec.Range<Double> range = spec.getRange();
                var field = entries.startDoubleField(label, current)
                        .setMin(range.getMin()).setMax(range.getMax())
                        .setErrorSupplier(input -> Double.isFinite(input) ? Optional.empty()
                                : Optional.of(Component.translatable("text.cloth-config.error.not_valid_number_double")));
                addEntry(category, field, number, current, spec);
            } else if (value instanceof ForgeConfigSpec.BooleanValue toggle) {
                boolean current = toggle.get();
                addEntry(category, entries.startBooleanToggle(label, current), toggle, current, spec);
            }
        }

        return screen.build();
    }

    private static <T> void addEntry(ConfigCategory category, AbstractFieldBuilder<T, ?, ?> field,
            ForgeConfigSpec.ConfigValue<T> value, T initialValue, ForgeConfigSpec.ValueSpec spec) {
        field.setDefaultValue(value::getDefault);
        field.setSaveConsumer(newValue -> {
            if (!Objects.equals(initialValue, newValue)) {
                value.set(newValue);
            }
        });
        field.requireRestart(spec.needsWorldRestart());
        category.addEntry(field.build());
    }
}
