package net.dragonloot.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonUpgradeTemplateItem extends Item {
    public DragonUpgradeTemplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("upgrade.dragonloot.dragon_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(""));
        tooltip.add(new TranslatableComponent("item.dragonloot.smithing_template.applies_to").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(" ").append(new TranslatableComponent(
                "item.dragonloot.smithing_template.dragon_upgrade.applies_to")).withStyle(ChatFormatting.BLUE));
        tooltip.add(new TranslatableComponent("item.dragonloot.smithing_template.ingredients").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(" ").append(new TranslatableComponent(
                "item.dragonloot.smithing_template.dragon_upgrade.ingredients")).withStyle(ChatFormatting.BLUE));
    }
}
