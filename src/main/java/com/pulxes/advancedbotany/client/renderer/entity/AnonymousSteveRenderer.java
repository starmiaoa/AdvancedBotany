package com.pulxes.advancedbotany.client.renderer.entity;

import com.pulxes.advancedbotany.common.entity.EntityAnonymousSteve;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class AnonymousSteveRenderer extends LivingEntityRenderer<EntityAnonymousSteve, PlayerModel<EntityAnonymousSteve>> {
    public AnonymousSteveRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAnonymousSteve entity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }

    @Override
    protected int getBlockLightLevel(EntityAnonymousSteve entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(EntityAnonymousSteve entity, BlockPos pos) {
        return 15;
    }
}
