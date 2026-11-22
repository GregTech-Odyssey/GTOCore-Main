# ParaTranz API

在 [ParaTranz 个人中心](https://paratranz.cn/users/my) 获取具有目标项目访问权限的账号 Token。把它写入本仓根目录的 `.env`，每行一个变量，值后不加注释。

```dotenv
PARATRANZ_SECRET_KEY=<你的 ParaTranz Token>
```

根目录的 `.env` 已被 Git 忽略。密钥只用于向 `https://paratranz.cn/api` 发送 `Authorization: Bearer <Token>` 请求头。不要把密钥写进命令行参数、提交、日志或聊天消息。

## 验证读取权限

在本仓根目录运行以下 PowerShell 命令。示例读取日语项目 `18185` 下的 `GTOCore/ja_jp.json`（文件 `2810105`），只输出词条数。

```powershell
& {
    $setting = Select-String -LiteralPath .env -Pattern '^\s*(?:export\s+)?PARATRANZ_SECRET_KEY\s*=\s*(.*?)\s*$' |
        Select-Object -First 1
    if (-not $setting) { throw '.env 缺少 PARATRANZ_SECRET_KEY' }

    $paraTranzToken = $setting.Matches[0].Groups[1].Value.Trim('"', "'")
    if (-not $paraTranzToken) { throw 'PARATRANZ_SECRET_KEY 为空' }

    $entries = Invoke-RestMethod -Method Get `
        -Uri 'https://paratranz.cn/api/projects/18185/files/2810105/translation' `
        -Headers @{ Authorization = "Bearer $paraTranzToken" } `
        -MaximumRedirection 0 -TimeoutSec 30 -ErrorAction Stop
    "ParaTranz 词条数：$($entries.Count)"
}
```

收到 HTTP 401 时，检查 Token 和请求头。收到 HTTP 403 时，检查 Token 所属账号在项目中的权限。读取成功只验证读取权限，上传原文和导入译文还需要对应的编辑权限。

## GTO-Translations 同步

本仓使用 `PARATRANZ_SECRET_KEY` 保存本地密钥。[GTO-Translations 的同步脚本](https://github.com/GregTech-Odyssey/GTO-Translations/blob/main/scripts/sync_paratranz_to_mc.py) 从进程环境变量 `PARATRANZ_TOKEN` 取值。本地运行该脚本时，把从 `.env` 读出的 Token 赋给脚本进程的 `PARATRANZ_TOKEN`。GitHub Actions 使用 [工作流配置](https://github.com/GregTech-Odyssey/GTO-Translations/blob/main/.github/workflows/paratranz-sync.yml) 中同名的 Actions secret。
