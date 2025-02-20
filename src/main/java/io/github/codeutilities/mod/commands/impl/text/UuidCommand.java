package io.github.codeutilities.mod.commands.impl.text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import io.github.codeutilities.CodeUtilities;
import io.github.codeutilities.mod.commands.Command;
import io.github.codeutilities.mod.commands.arguments.ArgBuilder;
import io.github.codeutilities.mod.commands.arguments.types.PlayerArgumentType;
import io.github.codeutilities.sys.player.chat.ChatType;
import io.github.codeutilities.sys.player.chat.ChatUtil;
import io.github.codeutilities.sys.util.StringUtil;
import io.github.codeutilities.sys.player.DFInfo;
import io.github.codeutilities.sys.networking.State;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UuidCommand extends Command {

    @Override
    public void register(MinecraftClient mc, CommandDispatcher<FabricClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("uuid")
                .then(ArgBuilder.argument("username", PlayerArgumentType.player())
                        .executes(ctx -> {
                            CodeUtilities.EXECUTOR.submit(() -> {
                                String username = ctx.getArgument("username", String.class);
                                String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
                                try {
                                    String UUIDJson = IOUtils
                                            .toString(new URL(url), StandardCharsets.UTF_8);
                                    if (UUIDJson.isEmpty()) {
                                        ChatUtil.sendMessage("Player was not found!", ChatType.FAIL);
                                        return;
                                    }
                                    JsonObject json = new JsonParser().parse(UUIDJson).getAsJsonObject();
                                    String uuid = json.get("id").getAsString();
                                    String fullUUID = StringUtil.fromTrimmed(uuid);

                                    Text text = new LiteralText("§eUUID of §6" + username + " §eis §b" + fullUUID + "§e!")
                                            .styled(s -> s.withHoverEvent(
                                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("§eClick to copy to clipboard."))
                                            ).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fullUUID)));
                                    this.sendMessage(mc, text);

                                    if (this.isCreative(mc) && DFInfo.isOnDF() && DFInfo.currentState.getMode() == State.Mode.DEV) {
                                        this.sendChatMessage(mc, "/txt " + fullUUID);
                                    }
                                } catch (IOException e) {
                                    ChatUtil.sendMessage("§cUser §6" + username + "§c was not found.");
                                    e.printStackTrace();
                                }
                            });
                            return 1;
                        })
                )
        );
    }
}
