package net.dragonloot.item;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DragonUpgradeTemplateItem extends Item {
    public DragonUpgradeTemplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("upgrade.dragonloot.dragon_upgrade").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("item.dragonloot.smithing_template.applies_to").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" ").append(new TranslationTextComponent(
                "item.dragonloot.smithing_template.dragon_upgrade.applies_to")).withStyle(TextFormatting.BLUE));
        tooltip.add(new TranslationTextComponent("item.dragonloot.smithing_template.ingredients").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" ").append(new TranslationTextComponent(
                "item.dragonloot.smithing_template.dragon_upgrade.ingredients")).withStyle(TextFormatting.BLUE));
    }
}
