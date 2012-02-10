package examples;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.builtin.IRubyObject;

public aspect JRubyOrphanedCode {

    before(DynamicMethod method) : execution(* call*(..)) && target(method) {
        RubyModule definingModule = method.getImplementationClass();
        Ruby runtime = definingModule.getRuntime();
        RubyModule rootNamespace = runtime.getObject();
        String moduleName = definingModule.getName();
        IRubyObject moduleNameConstantValue = rootNamespace.getConstant(moduleName);
        if (moduleNameConstantValue != definingModule) {
            System.out.println(
                    "Called method " + method.getName() + 
                    " on orphaned copy of class/module " + moduleName);
        }
    }
}
