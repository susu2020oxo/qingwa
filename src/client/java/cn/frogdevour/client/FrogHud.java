package cn.frogdevour.client;

import cn.frogdevour.FrogData;
import cn.frogdevour.GrowthRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.Locale;

public final class FrogHud {
    private FrogHud() { }
    public static void render(DrawContext draw, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.isSpectator() || client.options.hudHidden) return;
        int food = FrogData.food(client.player);
        GrowthRules.Stage stage = GrowthRules.stage(food);
        int center = draw.getScaledWindowWidth() / 2;
        // Leave health, armor, food, air, experience level and actionbar unobscured.
        int y = draw.getScaledWindowHeight() - (client.player.isSubmergedInWater() ? 112 : 102);
        int width = 182;
        draw.fill(center - width / 2 - 4, y - 3, center + width / 2 + 4, y + 30, 0xB016241D);
        Text title = Text.translatable("hud.frogdevour.size", Text.translatable("stage.frogdevour." + stage.name().toLowerCase(Locale.ROOT)),
                String.format(Locale.ROOT, "%.1f", stage.modelScale));
        draw.drawCenteredTextWithShadow(client.textRenderer, title, center, y, 0xFFB9F88A);
        int required = GrowthRules.required(food);
        if (required > 0) {
            int progress = GrowthRules.progress(food);
            draw.fill(center - 91, y + 12, center + 91, y + 16, 0xFF344438);
            draw.fill(center - 91, y + 12, center - 91 + (int) (182L * progress / required), y + 16, 0xFF8ED95A);
            Text hint = Text.translatable("hud.frogdevour.progress", progress, required,
                    FrogDevourClient.devourKey.getBoundKeyLocalizedText());
            draw.drawCenteredTextWithShadow(client.textRenderer, hint, center, y + 19, 0xFFE4F1DD);
        } else {
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("hud.frogdevour.max",
                    FrogDevourClient.devourKey.getBoundKeyLocalizedText()), center, y + 13, 0xFFE4F1DD);
        }
    }
}
