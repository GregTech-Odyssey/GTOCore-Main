# 发电机与燃料数据表更新

从当前工作区重新读取燃料配方、直接制造材料和中文名称，生成包含公式的六工作表Excel。默认普通难度，可以在表内调整难度、涡轮参数与输出配置。

## 运行

需要Windows、PowerShell 7、Codex自带的Python与Node文档运行时，以及本仓同级的GTM fork源码检出。使用已有依赖，不联网、不安装软件，不运行Gradle，不修改游戏源码或世界规则。

在仓库根目录执行：

```powershell
.\tools\generator-report\Update-GeneratorData.ps1
```

指定其他路径或难度：

```powershell
.\tools\generator-report\Update-GeneratorData.ps1 `
    -RepoPath '.' `
    -GtmPath '..\GregTech-Modern' `
    -OutputDirectory '.\outputs\generator-report' `
    -Difficulty Expert
```

解压脚本包后从其他位置运行时，传入`-RepoPath`即可；`-GtmPath`默认取该仓库同级的`GregTech-Modern`。

输出为`outputs/generator-report/发电机与燃料数据表_中文版.xlsx`及`校验结果.json`。重复运行会替换同名结果；文件已在Excel中打开时请先关闭。添加`-RenderPreviews`会在输出目录生成六张校验预览图。

运行时无法自动定位时，使用`-RuntimeRoot`指定Codex的`codex-primary-runtime/dependencies`目录。运行时应包含`python/python.exe`、`node/bin/node.exe`和`node/node_modules/@oai/artifact-tool`。

## 更新范围

- 自动提取已支持燃料配方类别的新增、删除、燃料量、助剂量、发电EU/t和燃烧时长；单方块时长取整、超频、扩展和供氧按模型重新计算。
- 自动更新能静态识别的直接制造配方及材料量；成本只展开最近一道配方，副产物价值与全流程加工电耗不自动抵扣。
- 表中包含固体燃料的运行时注册公式；不启动游戏时无法枚举其他模组实际注册的全部固体燃料。
- 机器注册、难度规则、生体发电等机制由模板建模。脚本首先核对`mechanism_baseline.json`中的38个机制源文件；任一文件变更都会停止，列出需要复核的文件。新机型、新机制或新燃料类别需要修改`build_report.mjs`或`extract_data.py`后再更新基线，不能只重写哈希以跳过检查。
- 配方中的Java循环、条件分支、运行时数据包和自定义世界规则不由静态提取器执行。无法静态识别的来源不能据此视为不存在。

## 文件

| 文件 | 用途 |
| --- | --- |
| `Update-GeneratorData.ps1` | 检查机制与依赖，运行提取和表格生成 |
| `extract_data.py` | 从源码提取配方、制造材料、中文名与版本信息 |
| `build_report.mjs` | 生成六张中文工作表，校验代表性计算及公式错误 |
| `mechanism_baseline.json` | 当前计算模型所依据的机制源码SHA-256 |

校验覆盖代表性单位EU成本、难度切换重算、生物发电量和公式错误。预览核对与Excel原生程序行为应按使用环境另行检查。
