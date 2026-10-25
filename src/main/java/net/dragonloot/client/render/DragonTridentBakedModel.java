package net.dragonloot.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class DragonTridentBakedModel implements BakedModel {
    private final BakedModel baseModel;
    private final BakedModel inHandModel;
    private final BakedModel throwingModel;
    private final boolean throwing;
    private final ItemOverrides overrides;

    public DragonTridentBakedModel(BakedModel baseModel, BakedModel inHandModel, BakedModel throwingModel) {
        this(baseModel, inHandModel, throwingModel, false);
    }

    private DragonTridentBakedModel(BakedModel baseModel, BakedModel inHandModel, BakedModel throwingModel, boolean throwing) {
        this.baseModel = baseModel;
        this.inHandModel = inHandModel;
        this.throwingModel = throwingModel;
        this.throwing = throwing;
        this.overrides = new ItemOverrides() {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                boolean isThrowing = entity != null && entity.isUsingItem() && entity.getUseItem() == stack;
                return new DragonTridentBakedModel(baseModel, inHandModel, throwingModel, isThrowing);
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random) {
        return baseModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return baseModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType displayContext, PoseStack poseStack) {
        if (isHandContext(displayContext)) {
            BakedModel model = throwing ? throwingModel : inHandModel;
            return model.handlePerspective(displayContext, poseStack);
        }

        return baseModel.handlePerspective(displayContext, poseStack);
    }

    private static boolean isHandContext(ItemTransforms.TransformType displayContext) {
        return displayContext == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }
}
