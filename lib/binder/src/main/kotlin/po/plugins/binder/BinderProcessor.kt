package po.plugins.binder

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import po.plugins.binder.annotations.ClassBinder
import po.plugins.binder.annotations.PropertyBinder
import po.plugins.binder.auxiliary.PropertyInfo
import java.io.OutputStream

object Constants {
    const val BIND_PROPERTY_INTERFACE = "BindPropertyInterface"
    const val ROOT_PACKAGE =  "package po.exposify"
}

class BinderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private fun KSClassDeclaration.getClassBinderKey(): String {
        val annotation = annotations.first { it.shortName.asString() == "ClassBinder" }
        val keyArg = annotation.arguments.first { it.name?.asString() == "key" }
        return keyArg.value as String
    }

    private fun KSPropertyDeclaration.getPropertyBinderKey(): String {
        val annotation = annotations.first { it.shortName.asString() == "PropertyBinder" }
        val keyArg = annotation.arguments.first { it.name?.asString() == "key" }
        return keyArg.value as String
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val classBinders = resolver.getSymbolsWithAnnotation(ClassBinder::class.qualifiedName!!,)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()

        val propertyBinders = resolver.getSymbolsWithAnnotation(PropertyBinder::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .filter { it.validate() }
            .toList()

        processBindings(classBinders, propertyBinders)

        // Return empty list indicating all annotations are handled
        return emptyList()
    }

    inner class Visitor(private val file: OutputStream) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {}
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {}
        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {}
    }

    private fun processBindings(
        classBinders: List<KSClassDeclaration>,
        propertyBinders: List<KSPropertyDeclaration>
    ) {
        // Map of key to classes annotated with @ClassBinder
        val classBinderMap = classBinders.groupBy { it.getClassBinderKey() }

        // Map of class to its annotated properties


        val propertyBinderMap = propertyBinders.groupBy { it.parentDeclaration as? KSClassDeclaration }
        logger.info(propertyBinderMap.size.toString())
        for ((key, classes) in classBinderMap) {
            if (classes.size != 2) {
                logger.error("Expected exactly 2 classes annotated with @ClassBinder(\"$key\"), but found ${classes.size}")
                continue
            }

            val (classA, classB) = classes
            val propertiesA = propertyBinderMap[classA] ?: emptyList()
            val propertiesB = propertyBinderMap[classB] ?: emptyList()

            val propertiesAMap = propertiesA.associateBy { it.getPropertyBinderKey() }
            val propertiesBMap = propertiesB.associateBy { it.getPropertyBinderKey() }

            val commonKeys = propertiesAMap.keys.intersect(propertiesBMap.keys)

            if (commonKeys.isEmpty()) {
                logger.warn("No matching properties for key \"$key\" between ${classA.simpleName.asString()} and ${classB.simpleName.asString()}")
                continue
            }

            // generateSynchronizationCode(classA, classB, commonKeys, propertiesAMap, propertiesBMap)
            generateBinderClass(classA, classB, commonKeys, propertiesAMap, propertiesBMap)
        }
    }


    private fun generateBindingPropertiesPart( property: PropertyInfo): String{

   val propText =  when(property.typeStr) {
        "LocalDateTime::class" -> {
            """DatePropertyClass(
                name = "${property.name}",
                   dtoProperty = dto.${property.name},
                   entityProperty = entity.${property.name}
               )"""
        }

        "String::class" -> {
            """StringPropertyClass(
                   name = "${property.name}",
                   dtoProperty = dto.${property.name},
                   entityProperty = entity.${property.name}
               )"""
        }
       "Int::class" -> {
           """StringPropertyClass(
                   name = "${property.name}",
                   dtoProperty = dto.${property.name},
                   entityProperty = entity.${property.name}
               )"""
       }
        else -> {
                """DatePropertyClass(
                name = "${property.name}",
                   dtoProperty = dto.${property.name},
                   entityProperty = entity.${property.name}
               )"""
        }
    }
   return  propText.trimIndent()
}

    private fun generateBinderClass(
        classA: KSClassDeclaration,
        classB: KSClassDeclaration,
        commonKeys: Set<String>,
        propertiesAMap: Map<String, KSPropertyDeclaration>,
        propertiesBMap: Map<String, KSPropertyDeclaration>
    ) {

        val packageName = "po.exposify"
        val className = "${classA.simpleName.asString()}BinderClass"

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, classA.containingFile!!, classB.containingFile!!),
            packageName = packageName,
            fileName = className
        )

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.newLine()

            writer.appendLine("import po.exposify.binder.PartnerDTO")
            writer.appendLine("import po.exposify.binder.PartnerEntity")
            writer.appendLine("import po.exposify.binder.BindPropertyInterface")
            writer.appendLine("import po.exposify.binder.StringPropertyClass")
            writer.appendLine("import po.exposify.binder.DatePropertyClass")
            writer.appendLine("import kotlinx.datetime.LocalDateTime")
            writer.newLine()

            writer.appendLine("object $className {")

            writer.appendLine("lateinit var dto : ${classA.simpleName.asString()}")
            writer.appendLine("lateinit var entity : ${classB.simpleName.asString()}")

            writer.appendLine("var properties = mutableListOf<BindPropertyInterface>(")
            for (key in commonKeys) {
                val propA = propertiesAMap[key]!!
                val propB = propertiesBMap[key]!!
                val type = propA.type.resolve()

                writer.appendLine(generateBindingPropertiesPart(PropertyInfo(type,propA.simpleName.asString(),type.toString())))

            }
            writer.appendLine(")")

            val closingText = """    fun updateProperties(){
                            properties.forEach{
                                it.update()
                            }
                        }
                    }"""

            writer.appendLine(closingText)
        }
    }

    private fun generateSynchronizationCode(
        classA: KSClassDeclaration,
        classB: KSClassDeclaration,
        commonKeys: Set<String>,
        propertiesAMap: Map<String, KSPropertyDeclaration>,
        propertiesBMap: Map<String, KSPropertyDeclaration>
    ) {
        val packageName = "generated.bindings"
        val className = "${classA.simpleName.asString()}_${classB.simpleName.asString()}_Binder"

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, classA.containingFile!!, classB.containingFile!!),
            packageName = packageName,
            fileName = className
        )

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.newLine()

            val importA = classA.qualifiedName?.asString()
            val importB = classB.qualifiedName?.asString()
            writer.appendLine("import $importA")
            writer.appendLine("import $importB")
            writer.newLine()

            writer.appendLine("object $className {")

            // Generate copyAtoB function
            writer.appendLine(" fun copyAtoB(a: ${classA.simpleName.asString()}, b: ${classB.simpleName.asString()}) {")
            for (key in commonKeys) {
                val propA = propertiesAMap[key]!!
                val propB = propertiesBMap[key]!!

                val propAName = propA.simpleName.asString()
                val propBName = propB.simpleName.asString()

                writer.appendLine("        b.$propBName = a.$propAName")
            }
            writer.appendLine("    }")
            writer.newLine()

            // Generate copyBtoA function
            writer.appendLine("    fun copyBtoA(b: ${classB.simpleName.asString()}, a: ${classA.simpleName.asString()}) {")
            for (key in commonKeys) {
                val propA = propertiesAMap[key]!!
                val propB = propertiesBMap[key]!!

                propA.type.resolve()

                val propAName = propA.simpleName.asString()
                val propBName = propB.simpleName.asString()

                writer.appendLine("        a.$propAName = b.$propBName")
            }
            writer.appendLine("    }")
            writer.appendLine("}")
        }
    }

}