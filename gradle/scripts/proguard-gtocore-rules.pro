# ============================================================
# gtocore 发行代码保护规则（流水线任务 obfuscateGtocoreRelease，只作用于 reobf 后的 gtocore 自身类）
#
# 输入是 reobfJar 产物里 com/gtolib 与 META-INF 以外的类（SRG 命名空间）；gtolib 发行包、资源原样保留，
# 只当 library 参与解析。gtocore 引用 gtolib 的名字在 jar 阶段已按 gtolib 发行 mapping 改写过。
#
# 不做优化、不删类；去掉局部变量表与参数名。
#
# 流水线另外拼接 build/gtocore-obf/keep-generated.pro（扫描生成）：
#   * GTOLib 发行轨（加密前明文）引用到的 gtocore 类及其 gtocore 祖先：整类保名
#     —— gtolib 方法体在发行包里是加密的，打包时无法改写它对 gtocore 的引用；
#     含字符串拼类名前缀（如 MultiBlock + 字母）命中的类
#   * mixin 类继承/实现的 gtocore 类型及其祖先：整类保名（接口实现可能来自 mixin 目标类）
#   * 带 DataSyncLib / 配置库 / LDLib 注解的成员（存盘键、同步键、配置键是成员名）
#   * 注解字符串值里写的本类（或祖先）成员名（listener / skipWhen / defaultValueGetter / @ConfigSetter 等）
# 新增按名访问（反射、Class.forName、资源或脚本里写类名/成员名）而以上都覆盖不到时，在本文件补 keep。
# ============================================================

-dontshrink
-dontoptimize
-dontwarn
-dontnote
-ignorewarnings
-verbose

-repackageclasses com.gtocore
-allowaccessmodification
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses,EnclosingMethod,Record,PermittedSubclasses,NestHost,NestMembers,LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

-adaptclassstrings com.gtocore.**,com.hepdd.**

# Mixin：gtocore.mixins.json / refmap / MixinConfigPlugin 按类名加载与比较，@Shadow/@Accessor 等成员名对应目标
-keep class com.gtocore.mixin.** { *; }
-keep class com.gtocore.config.MixinConfigPlugin { *; }

# 配置：dev.toma 配置库与 GTOConfig.set/getField 按字段名读写
-keep class com.gtocore.config.GTOConfig { *; }
-keep class com.gtocore.config.GTOConfig$* { *; }

# coremods/gsonhelper_fast_path.js 写死类名与 21 个方法名
-keep class com.gtocore.utils.GsonHelperPatches { *; }

# JNI：native 方法名与所属类名由原生库按名绑定
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# 枚举：values()/valueOf 反射调用；常量字段保名
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# Java 序列化
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
