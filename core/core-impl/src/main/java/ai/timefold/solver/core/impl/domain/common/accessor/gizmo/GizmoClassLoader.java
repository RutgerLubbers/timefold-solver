package ai.timefold.solver.core.impl.domain.common.accessor.gizmo;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads a class if we have the Gizmo-generated bytecode for it,
 * otherwise uses the current {@link Thread}'s context {@link ClassLoader}.
 * This implementation is thread-safe.
 */
public final class GizmoClassLoader extends ClassLoader {

    private final Map<String, byte[]> classNameToBytecodeMap = new HashMap<>();

    public GizmoClassLoader() {
        /*
         * As parent, Gizmo needs to use the same ClassLoader that loaded its own class.
         * Otherwise, issues will arise in Quarkus with MemberAccessors which were first loaded by Quarkus
         * and then loaded again by Gizmo, which uses the default parent ClassLoader.
         */
        super(GizmoClassLoader.class.getClassLoader());
    }

    @Override
    public String getName() {
        return "Timefold Solver Gizmo ClassLoader";
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] byteCode = getBytecodeFor(name);
        if (byteCode == null) { // Not a Gizmo generated class; load from context class loader.
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } else { // Gizmo generated class.
            return defineClass(name, byteCode, 0, byteCode.length);
        }
    }

    public synchronized byte[] getBytecodeFor(String className) {
        return classNameToBytecodeMap.get(className);
    }

    public boolean hasBytecodeFor(String className) {
        return getBytecodeFor(className) != null;
    }

    public synchronized void storeBytecode(String className, byte[] bytecode) {
        classNameToBytecodeMap.put(className, bytecode);
    }

}
