package io.github.codeutilities.mod.commands.impl.text;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.codeutilities.mod.config.Config;
import io.github.codeutilities.mod.commands.Command;
import io.github.codeutilities.mod.commands.arguments.ArgBuilder;
import io.github.codeutilities.mod.features.commands.ColorsMenu;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;

public class ColorsCommand extends Command {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void register(MinecraftClient mc, CommandDispatcher<FabricClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("colors").executes((context) -> {
            if (Config.getBoolean("colorReplacePicker")) {
                showColorPalette(1);
            } else {
                ColorsMenu colorsGui = new ColorsMenu();
                colorsGui.scheduleOpenGui(colorsGui, "");
                //showColorPalette(1);
            }
            return 1;
        })
                .then(ArgBuilder.argument("Saturation(%)", IntegerArgumentType.integer(0, 100)).executes((context) -> {
                    float saturation = (float) IntegerArgumentType.getInteger(context, "Saturation(%)");
                    showColorPalette(saturation / 100f);
                    return 1;
                })));
    }

    private void showColorPalette(float saturation) {
        int maxColors = Config.getInteger("colorMaxRender");
        int lines = Config.getInteger("colorLines");

        for (int j = 0; j < lines; j++) {
            Text base = new LiteralText("");
            float b = 1f - ((1f / lines) * j);
            for (int i = 0; i < maxColors; i++) {
                float index = (360 / maxColors) * i;

                java.awt.Color color = java.awt.Color.getHSBColor(index / 360, saturation, b);
                String colorName = "#" + Integer.toHexString(color.getRGB()).substring(2);

                Style colorStyle = Style.EMPTY.withColor(TextColor.fromRgb(color.getRGB()));
                LiteralText extra = new LiteralText("|");
                LiteralText hover = new LiteralText(colorName);
                hover.append("\n§7Click to copy!");
                extra.setStyle(colorStyle);
                hover.setStyle(colorStyle);
                extra.styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color hex " + colorName)));
                extra.styled((style) -> style.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(hover)));
                base.getSiblings().add(extra);
            }
            mc.player.sendMessage(base, false);
        }
    }
}