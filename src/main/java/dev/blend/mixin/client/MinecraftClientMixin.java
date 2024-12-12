package dev.blend.mixin.client;

import dev.blend.handler.impl.ThemeHandler;
import dev.blend.util.render.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/item/HeldItemRenderer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/render/BufferBuilderStorage;)Lnet/minecraft/client/render/GameRenderer;"
            )
    )
    private void createNanoVGContext(RunArgs args, CallbackInfo ci) {
        DrawUtil.initialize();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V"
            )
    )
    private void preRender(CallbackInfo ci) {
        ThemeHandler.INSTANCE.update();
    }

}
