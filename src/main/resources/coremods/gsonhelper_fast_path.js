function initializeCoreMod() {
    const ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    const Opcodes = Java.type('org.objectweb.asm.Opcodes');
    const Type = Java.type('org.objectweb.asm.Type');
    const InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    const VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    const MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    const InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

    const HELPER = 'com/gtocore/utils/GsonHelperPatches';
    const NAMES = [
        'isStringValue', 'isNumberValue', 'isBooleanValue', 'isArrayNode', 'isObjectNode', 'isValidPrimitive',
        'getAsString', 'getAsItem', 'getAsBoolean', 'getAsDouble', 'getAsFloat', 'getAsLong', 'getAsInt',
        'getAsByte', 'getAsShort', 'getAsCharacter', 'getAsBigDecimal', 'getAsBigInteger',
        'getAsJsonObject', 'getAsJsonArray', 'getAsObject'
    ];

    function patch(classNode) {
        let stubbed = 0;
        for (let i = 0; i < classNode.methods.size(); i++) {
            const method = classNode.methods.get(i);
            if (NAMES.indexOf(method.name) < 0) {
                continue;
            }
            const args = Type.getArgumentTypes(method.desc);
            if (args.length < 2 || args[0].getSort() !== Type.OBJECT
                    || args[0].getInternalName() !== 'com/google/gson/JsonObject'
                    || args[1].getDescriptor() !== 'Ljava/lang/String;') {
                continue;
            }

            const body = new InsnList();
            let slot = 0;
            for (let a = 0; a < args.length; a++) {
                body.add(new VarInsnNode(args[a].getOpcode(Opcodes.ILOAD), slot));
                slot += args[a].getSize();
            }
            body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, method.name, method.desc, false));
            body.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));

            method.instructions.clear();
            method.instructions.add(body);
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) {
                method.localVariables.clear();
            }
            stubbed++;
        }
        ASMAPI.log('INFO', 'GTO: GsonHelper fast path stubbed ' + stubbed + ' methods');
        return classNode;
    }

    return {
        'gtocore_gsonhelper_fast_path': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.GsonHelper'
            },
            'transformer': patch
        }
    };
}
