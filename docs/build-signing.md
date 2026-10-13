# 云端构建与签名

仅在用户调试 gtocore 构建产物，或任务需要触发、排查、下载 Build and Sign 产物时读取本文。

## 触发条件

| 触发 | 条件 | 所需标记或命令 |
|---|---|---|
| `push` | commit message 含构建标记，且标记单独成词，如 `release --build` | 需要 |
| PR `opened` | 提起人为组织成员，按 PR head 自动构建一次 | 不需要 |
| PR `synchronize` | head commit 含构建标记，且推送者为组织成员 | 需要 |
| PR 评论 | 组织成员评论的第一行精确为 `/build` | `/build` |

只有组织成员或本仓 write 级 collaborator 能触发签名构建；bot 与外部人员会被拒绝。只构建本仓分支，fork 来源的 PR 一律跳过。

## 产物与 PR 反馈

- 工作流产出签名版 jar，只有该版本可以放入整合包。本地构建只用于开发调试。
- 组织成员可在 PR 下以 `/build` 作为第一行评论，触发对 PR head 的签名构建。
- 另有 `gtolib-mapping-encrypted`：GTOLib 发行混淆映射，用 `GTO_KEY` 加密，解密方法见 [GTOLib 子模块与构建](gtolib.md)。
- 构建开始时，bot 会在 PR 下发送“正在构建”评论与 Actions 链接；完成、失败或取消后原地更新同一条评论。
- `issue_comment` 触发的 run 不会出现在 PR Checks 页，应查看 bot 评论或 Actions 页面。

## 安全与排查

`pull_request_target` 会执行 PR 侧的 `build.gradle.kts` 等代码并注入签名 secrets 与 GTOLib 只读凭据，因此只允许本仓分支触发；成员评论 `/build` 即表示已审阅并愿意签名。

云端用 secret `GTOLIB_DEPLOY_KEY`（GTOLib 仓库只读 deploy key 的私钥）按主仓 gitlink 检出 GTOLib 源码，现场 ProGuard + Seal 加密后嵌入发行 jar。不检出 GTOSeal，Runtime 沿用 `libs/gto-seal-runtime-1.0.jar`。不得上传 `build/gtolib/` 下任何文件。

当用户正在调试构建产物，或某次 push 已触发云端构建时，用 `gh` CLI 找到对应 workflow run 并直接打开页面：

```powershell
gh run list --repo GregTech-Odyssey/GTOCore-Main
gh run view <run-id> --web
```

随后引导用户在页面的 Artifacts 区下载签名产物。
