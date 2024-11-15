package po.plugins.binder

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration


interface KSVisitor<D, R> {
    fun visitNode(node: KSNode, data: D): R

    fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: D): R

    fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: D): R
}