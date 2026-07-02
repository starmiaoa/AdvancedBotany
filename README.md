# Advanced Botany（移植版）

把 Pulxes / Liapel 的 Botania 附属模组 **Advanced Botany** 从 1.7.10 搬到现代版本。原版一直停在 1.7.10，这个仓库把它移植到了 1.20.1 Forge 和 1.21.1 NeoForge。

移植尽量忠实原版——五种特殊花、各类机器、遗物、星云全套装备、命运棋盘这些该有的都在，数值和逻辑是对着 1.7.10 反编译源一条条核过来的，不是凭感觉重写。

## 版本

| 分支 | 目标 |
| --- | --- |
| `main` | 1.20.1 Forge |
| `1.21.1-neoforge` | 1.21.1 NeoForge |

## 依赖

- **Botania** —— 硬依赖
- **Curios** —— 戒指、饰品走它（原版用的 Baubles 已经没了，换成 Curios）
- **Patchouli** —— 手册（原版塞在植物魔法辞典里的条目用它重建）

## 移植时做的取舍

- Thaumcraft、NEI、MineTweaker 这些当年的集成砍掉了，物品本体的功能保留
- 手册改用 Patchouli 重做
- 配方展示接了 JEI
- 全物品补了简体中文，术语跟着 Botania 官方汉化走

## 说明

个人移植，纯粹为了在新版本里还能玩上这个模组。原模组版权归原作者 **Pulxes / Liapel**，这里只做移植。碰到问题欢迎提 issue。
