/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.scalableui.designcompose

import android.util.Log
import com.android.designcompose.definition.DesignComposeDefinition
import com.android.designcompose.definition.DesignComposeDefinitionHeader
import com.android.designcompose.definition.element.Bounds
import com.android.designcompose.definition.element.ScalableDimension
import com.android.designcompose.definition.element.ScalableUIComponentSet
import com.android.designcompose.definition.element.ScalableUiVariant
import com.android.designcompose.definition.view.View
import java.io.File
import java.io.InputStream

private const val TAG = "DC_ScalableUiDoc"

private fun removeFileExtension(filename: String): String {
    val file = File(filename)
    return file.nameWithoutExtension
}

fun loadScalableUiDoc(fileStream: InputStream, docId: String): ScalableUiDoc? {
    try {
        return ScalableUiDoc(fileStream)
    } catch (error: Throwable) {
        throw DocLoadException("Failed to decode file: $error")
        return null
    }
}

private sealed class NodeQuery {
    data class NodeId(val id: String) : NodeQuery()

    data class NodeName(val name: String) : NodeQuery()

    data class NodeVariant(val name: String, val parent: String) : NodeQuery()

    data class NodeComponentSet(val name: String) : NodeQuery()

    companion object {
        const val QUERY_TYPE_ID = "id"
        const val QUERY_TYPE_NAME = "name"
        const val QUERY_TYPE_VARIANT = "variant"
        const val QUERY_TYPE_COMPONENT_SET = "component_set"

        fun id(id: String) = NodeId(id)

        fun name(name: String) = NodeName(name)

        fun variant(name: String, parent: String) = NodeVariant(name, parent)

        fun componentSet(name: String) = NodeComponentSet(name)

        fun decode(s: String): NodeQuery {
            val parts = s.split(":", limit = 2)
            val queryType = parts[0]
            val queryValue = parts[1]

            return when (queryType) {
                QUERY_TYPE_ID -> NodeId(queryValue)
                QUERY_TYPE_NAME -> NodeName(queryValue)
                QUERY_TYPE_VARIANT -> {
                    val variantParts = queryValue.split("\u001F")
                    if (variantParts.size != 2) {
                        throw IllegalArgumentException("Invalid variant query string: $s")
                    }
                    NodeVariant(variantParts[0], variantParts[1])
                }
                QUERY_TYPE_COMPONENT_SET -> NodeComponentSet(queryValue)
                else -> throw IllegalArgumentException("Invalid query type: $queryType")
            }
        }
    }
}

class ScalableUiDoc(docStream: InputStream) {
    // variant name -> scalable ui data
    val variantMap: HashMap<String, ScalableUiVariant> = HashMap()

    // variant id -> scalable ui data
    val variantIdMap: HashMap<String, ScalableUiVariant> = HashMap()

    // component set name -> { event name -> variant name }
    val componentSetMap: HashMap<String, ScalableUIComponentSet> = HashMap()

    companion object {
        const val CHILD_NAME_MAIN = "main"
    }

    init {
        val designDefinition = parseDcfStream(docStream)
        val variantViewMap = createVariantViewMap(designDefinition.views())

        val allViews = designDefinition.views()
        variantViewMap.forEach { setMap ->
            // Create a mapping of component set names to the scalable ui data for that set
            val componentSetQuery = NodeQuery.NodeComponentSet(setMap.key)
            val setView = allViews[componentSetQuery]
            setView?.style?.nodeStyle?.scalableData?.set?.let { setData ->
                val setName = setData.name
                componentSetMap[setName] = setData
            }

            // Iterate through the components of each component set
            setMap.value.forEach { variantMap ->
                val variant = variantMap.value
                if (variant.data.hasContainer()) {
                    if (variant.data.container.childrenCount > 0) {
                        // If the first child of this component is a child named "main", copy its
                        // visibility, alpha, and bounds to create a ScalableUiVariant.
                        val child = variant.data.container.getChildren(0)
                        if (child.name == CHILD_NAME_MAIN) {
                            val layout = child.style.layoutStyle
                            val scalableUiVariant = ScalableUiVariant.newBuilder(
                                variant.style?.nodeStyle?.scalableData?.variant
                            )
                                    .setIsVisible(
                                        when (child.style.nodeStyle.displayType) {
                                            com.android.designcompose.definition.view.Display
                                                .DISPLAY_NONE -> false
                                            else -> true
                                        }
                                    )
                                    .setAlpha(
                                        if (child.style.nodeStyle.hasOpacity()) {
                                            child.style.nodeStyle.opacity
                                        } else {
                                            1f
                                        }
                                    )
                                    .setBounds(
                                        Bounds.newBuilder()
                                            .setLeft(dimPoints(layout.margin.start.points))
                                            .setTop(dimPoints(layout.margin.top.points))
                                            .setRight(dimPoints(layout.margin.end.points))
                                            .setBottom(dimPoints(layout.margin.bottom.points))
                                            .setWidth(dimPoints(layout.width.points))
                                            .setHeight(dimPoints(layout.height.points))
                                            .build()
                                    )
                                    .build()
                            // Populate the variant maps by name and id
                            scalableUiVariant?.let {
                                this.variantMap[variantMap.key] = it
                                this.variantIdMap[variant.id] = it
                            }
                        }
                    }
                }
            }

            // Print out debugging data of what we parsed
            setView?.style?.nodeStyle?.scalableData?.set?.let { setData ->
                val setName = setData.name
                componentSetMap[setName] = setData
                Log.i(TAG, "Set ${setData.name}, ${setData.id}")
                setData.variantIdsList.forEach {
                    Log.i(
                        TAG,
                        "  Variant $it: Default ${variantIdMap[it]?.isDefault} " +
                        "Visible ${variantIdMap[it]?.isVisible}"
                    )
                }
            }
        }
    }

    private fun dimPoints(points: Float): ScalableDimension {
        return ScalableDimension.newBuilder()
            .setPoints(points)
            .build()
    }

    private fun DesignComposeDefinition.views(): Map<NodeQuery, View> {
        val views = mutableMapOf<NodeQuery, View>()
        for ((key, value) in this.viewsMap) {
            views[NodeQuery.decode(key)] = value
        }
        return views
    }

    private fun parseDcfStream(docStream: InputStream): DesignComposeDefinition {
        val header = DesignComposeDefinitionHeader.parseDelimitedFrom(docStream)
        val content = DesignComposeDefinition.parseDelimitedFrom(docStream)
        return content
    }

    // Given all the nodes, create a mapping of all components with variants. The HashMap maps the
    // component set name to a second map of the component set's child node name, with the
    // properties rearranged to be sorted, to their corresponding Views
    private fun createVariantViewMap(
        nodes: Map<NodeQuery, View>?
    ): HashMap<String, HashMap<String, View>> {
        val variantMap: HashMap<String, HashMap<String, View>> = HashMap()
        nodes?.forEach {
            val nodeQuery = it.key
            val view = it.value
            if (nodeQuery is NodeQuery.NodeVariant) {
                val nodeName = nodeQuery.name
                val parentNodeName = nodeQuery.parent

                val nodeNameToView = variantMap[parentNodeName] ?: HashMap()
                val sortedNodeName = createSortedVariantName(nodeName)
                nodeNameToView[sortedNodeName] = view
                variantMap[parentNodeName] = nodeNameToView
            }
        }
        return variantMap
    }

    // Given a variant name with comma separated properties in the form of property=variant, return
    // the same node name with the properties sorted.
    private fun createSortedVariantName(nodeName: String): String {
        val props = nodeName.split(',')
        val sortedProperties: ArrayList<String> = ArrayList()
        for (p in props) {
            sortedProperties.add(p.trim())
        }
        sortedProperties.sort()
        return sortedProperties.joinToString(",")
    }

    fun getBounds(variantName: String): Bounds? {
        return variantMap[variantName]?.bounds
    }

    fun getPanels(): List<ScalableUIComponentSet> {
        return componentSetMap.values.toList()
    }

    fun getVariantById(id: String): ScalableUiVariant? {
        return variantIdMap[id]
    }
}
