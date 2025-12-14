package com.mitu.appium.template

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import java.util.Properties

class MiTuScreenAction : AnAction() {

    private val testPackagePath = "com/dt/tests"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dir = resolveTargetDirectory(e) ?: run {
            Messages.showErrorDialog(project, "Please select a target directory in Project view.", "MiTu Appium Templates")
            return
        }

        // Provider để dialog có thể validate test root existence trước
        val testRootProvider = { getOrCreateTestJavaRoot(project) }

        val dialog = MiTuScreenDialog(
            project = project,
            screenTargetDir = dir,
            testTargetDirProvider = testRootProvider,
            testPackagePath = testPackagePath
        )

        if (!dialog.showAndGet()) return

        val raw = dialog.getRawName()
        val createTest = dialog.shouldCreateTest()

        val baseName = dialog.normalizeToPascalCase(raw)
        val screenClassName = "${baseName}Screen"
        val screenFileName = "$screenClassName.java"

        val testClassName = "${baseName}Test"
        val testFileName = "$testClassName.java"

        // Double-check existence (defensive) before write action
        if (dir.findFile(screenFileName) != null) {
            showExists(project, "File already exists:\n$screenFileName")
            return
        }

        if (createTest) {
            val testRoot = testRootProvider.invoke()
            if (testRoot == null) {
                showExists(project, "Cannot find/create test source root (src/test/java).")
                return
            }
            val testPackageDir = ensurePackageDir(testRoot, testPackagePath)
            if (testPackageDir.findFile(testFileName) != null) {
                showExists(project, "Test file already exists:\n$testPackagePath/$testFileName")
                return
            }
        }

        // Create files
        try {
            WriteCommandAction.runWriteCommandAction(project, "Create Appium Screen", null, Runnable {

                // 1) Create Screen
                val screenTemplate = FileTemplateManager.getInstance(project).getInternalTemplate("MiTuScreen")

                val screenProps = Properties().apply {
                    put("CLASS_NAME", screenClassName)
                    put("BASE_NAME", baseName)
                    put("BASE_NAME_LOWER", lowerFirstChar(baseName))
                    put("VARIABLE_NAME", lowerFirstChar(screenClassName))
                }

                FileTemplateUtil.createFromTemplate(
                    screenTemplate,
                    screenFileName,
                    screenProps,
                    dir
                )

                // 2) Create Test (optional)
                if (createTest) {
                    val testTemplate = FileTemplateManager.getInstance(project).getInternalTemplate("MiTuTest")

                    val testProps = Properties().apply {
                        put("CLASS_NAME", testClassName)
                        put("SCREEN_CLASS", screenClassName)
                        put("VARIABLE_NAME", lowerFirstChar(screenClassName))
                        put("BASE_NAME", baseName)
                        put("BASE_NAME_LOWER", lowerFirstChar(baseName))
                    }

                    val testRoot = getOrCreateTestJavaRoot(project)
                        ?: throw IncorrectOperationException("Cannot find/create test source root (src/test/java).")

                    val testPackageDir = ensurePackageDir(testRoot, testPackagePath)

                    FileTemplateUtil.createFromTemplate(
                        testTemplate,
                        testFileName,
                        testProps,
                        testPackageDir
                    )
                }
            })
        } catch (ex: IncorrectOperationException) {
            // Friendly message for common "already exists" or create failures
            val msg = ex.message ?: "Failed to create file(s)."
            if (msg.contains("already exists", ignoreCase = true) || msg.contains("exists", ignoreCase = true)) {
                showExists(project, msg)
            } else {
                Messages.showErrorDialog(project, msg, "MiTu Appium Templates")
            }
        } catch (t: Throwable) {
            Messages.showErrorDialog(project, t.message ?: t.toString(), "MiTu Appium Templates")
        }
    }

    private fun resolveTargetDirectory(e: AnActionEvent): PsiDirectory? {
        // Prefer selected directory
        val selected = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory
        if (selected != null) return selected

        // If user selected a file, use its containing directory
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        if (psiFile != null) return psiFile.containingDirectory

        // As a fallback, try IDE selected virtual file
        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (vFile != null && vFile.isDirectory) {
            return PsiManager.getInstance(e.project ?: return null).findDirectory(vFile)
        }

        return null
    }

    private fun showExists(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "MiTu Appium Templates")
    }

    private fun lowerFirstChar(name: String): String {
        if (name.isBlank()) return name
        return name.replaceFirstChar { it.lowercase() }
    }

    private fun getOrCreateTestJavaRoot(project: Project): PsiDirectory? {
        val rootManager = ProjectRootManager.getInstance(project)
        val fileIndex = rootManager.fileIndex
        val psiManager = PsiManager.getInstance(project)

        // 1) Prefer existing test source roots
        rootManager.contentSourceRoots.firstOrNull { fileIndex.isInTestSourceContent(it) }?.let { root ->
            psiManager.findDirectory(root)?.let { return it }
        }

        // 2) Create default structure: <contentRoot>/src/test/java
        val contentRoot: VirtualFile = rootManager.contentRoots.firstOrNull() ?: return null
        val contentDir = psiManager.findDirectory(contentRoot) ?: return null

        val srcDir = contentDir.findSubdirectory("src") ?: contentDir.createSubdirectory("src")
        val testDir = srcDir.findSubdirectory("test") ?: srcDir.createSubdirectory("test")
        val javaDir = testDir.findSubdirectory("java") ?: testDir.createSubdirectory("java")

        return javaDir
    }

    private fun ensurePackageDir(base: PsiDirectory, pkg: String): PsiDirectory {
        var current = base
        pkg.split("/").filter { it.isNotBlank() }.forEach { name ->
            current = current.findSubdirectory(name) ?: current.createSubdirectory(name)
        }
        return current
    }
}
