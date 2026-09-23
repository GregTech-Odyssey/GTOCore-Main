# 云端构建与签名

仅在用户调试 gtocore 构建产物，或任务需要触发、排查、下载 Build and Sign 产物时读取本文。

## 触发条件

| 触发 | 条件 | 所需标记或命令 |
|---|---|---|
| `push` | commit message 含构建标记，且标记单独成词，如 `release --build` | 需要 |
| PR `opened` | 提起人为组织成员，按 PR head 自动构建一次 | 不需要 |
| PR `synchronize` | head commit 含构建标记，且推送者为组织成员 | 需要 |
| PR 评论 | 组织成员评论的第一行精确为 `/build` | `/build` |

只有组织成员或本仓 write 级 collaborator 能触发签名构建；bot 与外部人员会被拒绝。

## 产物与 PR 反馈

- 工作流产出签名版 jar，只有该版本可以放入整合包。本地构建只用于开发调试。
- 外部 fork PR 使用 `pull_request_target` 在 base 仓上下文运行，因此可以使用组织签名 secrets；外部贡献者开 PR 不会自动构建。
- 组织成员审阅后，可在 PR 下以 `/build` 作为第一行评论，触发对 PR head（包括 fork）的签名构建。
- 构建开始时，bot 会在 PR 下发送“正在构建”评论与 Actions 链接；完成、失败或取消后原地更新同一条评论。
- `issue_comment` 触发的 run 不会出现在 PR Checks 页，应查看 bot 评论或 Actions 页面。

## 安全与排查

`pull_request_target` 会执行 PR 侧的 `build.gradle` 等代码并注入签名 secrets。成员评论 `/build` 即表示已审阅并愿意签名；不得对未审代码轻易触发。

`GTOLib` 与 `GTOSeal` 是私有仓库，不得向执行 PR 侧代码的 `pull_request_target` 工作流注入私有子模块凭据。云端构建应保持 `submodules: false`，并使用主仓提交的预构建产物及 gitlink 一致性校验。

当用户正在调试构建产物，或某次 push 已触发云端构建时，用 `gh` CLI 找到对应 workflow run 并直接打开页面：

```powershell
gh run list --repo GregTech-Odyssey/GTOCore-Main
gh run view <run-id> --web
```

随后引导用户在页面的 Artifacts 区下载签名产物。
