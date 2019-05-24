package org.jetbrains.kotlin.konan.library

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.propertyList

const val KLIB_PROPERTY_ABI_VERSION = "abi_version"
const val KLIB_PROPERTY_COMPILER_VERSION = "compiler_version"
const val KLIB_PROPERTY_DEPENDENCY_VERSION = "dependency_version"
const val KLIB_PROPERTY_LIBRARY_VERSION = "library_version"
const val KLIB_PROPERTY_UNIQUE_NAME = "unique_name"
const val KLIB_PROPERTY_DEPENDS = "depends"
const val KLIB_PROPERTY_PACKAGE = "package"
/**
 * Abstractions for getting access to the information stored inside of Kotlin/Native library.
 */

interface BaseKotlinLibrary {
    val libraryName: String
    val libraryFile: File
    val versions: KonanLibraryVersioning
    // Whether this library is default (provided by distribution)?
    val isDefault: Boolean
    val manifestProperties: Properties
}

interface MetadataLibrary {
    val moduleHeaderData: ByteArray
    fun packageMetadataParts(fqName: String): Set<String>
    fun packageMetadata(fqName: String, partName: String): ByteArray
}

interface IrLibrary {
    val dataFlowGraph: ByteArray?
    val irHeader: ByteArray?
    fun irDeclaration(index: Long, isLocal: Boolean): ByteArray
}

val BaseKotlinLibrary.uniqueName: String
    get() = manifestProperties.getProperty(KLIB_PROPERTY_UNIQUE_NAME)!!

val BaseKotlinLibrary.unresolvedDependencies: List<UnresolvedLibrary>
    get() = manifestProperties.propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true)
            .map {
                UnresolvedLibrary(it, manifestProperties.getProperty("dependency_version_$it"))
            }
// KONAN

const val KLIB_PROPERTY_LINKED_OPTS = "linkerOpts"
const val KLIB_PROPERTY_INTEROP = "interop"
const val KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS = "exportForwardDeclarations"
const val KLIB_PROPERTY_INCLUDED_HEADERS = "includedHeaders"


interface TargetedLibrary {
    val targetList: List<String>
    val includedPaths: List<String>
}

interface BitcodeLibrary : TargetedLibrary {
    val bitcodePaths: List<String>
}

interface KonanLibrary : BaseKotlinLibrary, MetadataLibrary, IrLibrary, BitcodeLibrary {
    val linkerOpts: List<String>
}

val KonanLibrary.isInterop
    get() = manifestProperties.getProperty(KLIB_PROPERTY_INTEROP) == "true"

val KonanLibrary.packageFqName: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_PACKAGE)

val KonanLibrary.exportForwardDeclarations
    get() = manifestProperties.propertyList(KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS, escapeInQuotes = true)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

val KonanLibrary.includedHeaders
    get() = manifestProperties.propertyList(KLIB_PROPERTY_INCLUDED_HEADERS, escapeInQuotes = true)
