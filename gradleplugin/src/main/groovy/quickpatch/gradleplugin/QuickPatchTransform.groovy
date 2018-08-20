package quickpatch.gradleplugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

import javassist.ClassPool
import org.gradle.api.Plugin
import org.gradle.api.logging.Logger

class QuickPatchTransform extends Transform implements Plugin<Project> {

    private project
    static Logger logger

    void apply(Project project) {
        this.project = project
        logger = project.logger
        project.android.registerTransform(this)

        System.out.println("=====================")
        System.out.println(" QuickPatchTransform ")
        System.out.println("=====================")
    }

    @Override
    String getName() {
        return "QuickPatchPlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) {
        super.transform(transformInvocation)
        def inputs = transformInvocation.inputs
        System.out.println("=============== QuickPatchTransform start ===============")
        def startTime = System.currentTimeMillis()

        transformInvocation.getOutputProvider().deleteAll()
        File jarFile = transformInvocation.getOutputProvider().getContentLocation("main", getOutputTypes(), getScopes(),
                Format.JAR)
        if (jarFile.exists()) {
            jarFile.delete()
        }

        ClassPool classPool = new ClassPool()

        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        def box = ConvertUtils.toCtClasses(inputs, classPool)
        CodePatcher.insertCode(box, jarFile)
        // println(box)
        def cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "transform cost $cost seconds"
        System.out.println("=============== QuickPatchTransform end ===============")
    }
}