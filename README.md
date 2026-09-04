# Verity JE

Verity JE 是适用于 Minecraft Java Edition 1.21.1 的 Fabric 模组，可在游戏中调用 DeepSeek。

## 环境

- JDK 21
- Minecraft 1.21.1
- Fabric Loader 0.16+
- Fabric API

## 配置 DeepSeek

不要把 API Key 写入源码。启动 Minecraft 之前设置环境变量：

```bash
export DEEPSEEK_API_KEY="你的密钥"
export DEEPSEEK_MODEL="deepseek-v4-flash" # 可选
```

如果从启动器启动且启动器无法继承环境变量，可在 JVM 参数中设置：

```text
-Ddeepseek.apiKey=你的密钥 -Ddeepseek.model=deepseek-v4-flash
```

也可用 `DEEPSEEK_API_URL` 或 `-Ddeepseek.apiUrl=...` 指向兼容的代理接口。

## 使用

进入世界或服务器后执行：

```text
/verity ask 怎么找到钻石？
```

## 构建

```bash
./gradlew build
```

生成的 JAR 位于 `build/libs/`。服务端使用时也需要安装 Fabric API。
