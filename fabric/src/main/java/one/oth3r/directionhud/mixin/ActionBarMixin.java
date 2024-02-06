package one.oth3r.directionhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import one.oth3r.directionhud.DirectionHUDClient;
import one.oth3r.directionhud.common.HUD;
import one.oth3r.directionhud.common.files.PlayerData;
import one.oth3r.directionhud.utils.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ActionBarMixin {
    @Inject(at = @At("HEAD"), method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V")
    private void sendMessage(Text message, boolean tinted, CallbackInfo info) {
        // no point in doing anything if the message is empty
        if (message.getString().equals("")) return;
        MinecraftClient client = MinecraftClient.getInstance();
        // get the actionbar's click event
        ClickEvent click = message.getStyle().getClickEvent();
        // if the click event has the ModRinth link, it's a directionhud actionbar
        if (click == null || !click.getValue().equals("https://modrinth.com/mod/directionhud")) {
            if (client.player == null) return;
            Player player = DirectionHUDClient.getClientPlayer(client);
            // if on supported server and hud is on AND hud type is actionbar
            if (DirectionHUDClient.onSupportedServer && (boolean) PlayerData.get.hud.setting(player, HUD.Setting.state) &&
                    PlayerData.get.hud.setting(player, HUD.Setting.type).equals(HUD.Setting.DisplayType.actionbar.toString())) {
                DirectionHUDClient.override = message;
                DirectionHUDClient.overrideCd = 40;
            }
        }
    }
}