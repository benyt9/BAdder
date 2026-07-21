package b.bplugins.badder.commands;

import b.bplugins.badder.Main;
import b.bplugins.badder.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.io.File;

public class BadderCommand {

    public static LiteralCommandNode<CommandSourceStack> createCommand(Main plugin) {
        return Commands.literal("badder")
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    sender.sendMessage(MessageUtils.getMessage("usage"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("badder.command.reload"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            plugin.reloadConfigs();
                            sender.sendMessage(MessageUtils.getMessage("reload.success"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("zip")
                        .requires(source -> source.getSender().hasPermission("badder.command.zip"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();

                            // Nutzt jetzt den bereits in Main initialisierten PackManager
                            // statt einer zweiten, inkonsistenten Zip-Implementierung (PackBuilder)
                            File zipFile = new File(plugin.getDataFolder(), "output/badder-resourcepack.zip");
                            boolean success = plugin.getPackManager().buildResourcePack(zipFile);

                            if (success) {
                                sender.sendMessage(MessageUtils.getMessage("zip.success"));
                            } else {
                                sender.sendMessage(MessageUtils.getMessage("zip.failed"));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
    }
}