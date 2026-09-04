package com.verityje;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VerityJeMod implements ModInitializer {
    public static final String MOD_ID = "verityje";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private final DeepSeekClient deepSeek = DeepSeekClient.fromEnvironment();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("verity")
                .then(CommandManager.literal("ask")
                    .then(CommandManager.argument("question", StringArgumentType.greedyString())
                        .executes(context -> {
                            var source = context.getSource();
                            var question = StringArgumentType.getString(context, "question");

                            if (!deepSeek.isConfigured()) {
                                source.sendError(Text.literal(
                                    "Verity JE 未配置：请设置 DEEPSEEK_API_KEY 后重启游戏。"));
                                return 0;
                            }

                            source.sendFeedback(() -> Text.literal("§7[Verity] 正在思考…"), false);
                            deepSeek.ask(question).whenComplete((answer, error) ->
                                source.getServer().execute(() -> {
                                    if (error != null) {
                                        LOGGER.error("DeepSeek request failed", error);
                                        source.sendError(Text.literal("DeepSeek 请求失败：" + rootMessage(error)));
                                    } else {
                                        source.sendFeedback(
                                            () -> Text.literal("§b[Verity] §f" + answer), false);
                                    }
                                }));
                            return 1;
                        })))));

        LOGGER.info("Verity JE 已加载；DeepSeek 配置状态：{}", deepSeek.isConfigured() ? "就绪" : "缺少密钥");
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
