package com.dnocturne.basalt.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * Interface for modular subcommands.
 *
 * <p>Implement this interface to create subcommands that can be registered
 * through {@link SubCommandRegistry}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class HelpCommand implements SubCommand {
 *     private final MyPlugin plugin;
 *
 *     public HelpCommand(MyPlugin plugin) {
 *         this.plugin = plugin;
 *     }
 *
 *     @Override
 *     public void register(PaperCommandManager<CommandSourceStack> manager) {
 *         manager.command(
 *             manager.commandBuilder("myplugin")
 *                 .literal("help")
 *                 .handler(ctx -> {
 *                     // Handle help command
 *                 })
 *         );
 *     }
 * }
 * }</pre>
 */
@SuppressWarnings("UnstableApiUsage")
public interface SubCommand {

    /**
     * Register this subcommand with the command manager.
     *
     * @param manager The Cloud command manager
     */
    void register(PaperCommandManager<CommandSourceStack> manager);
}
