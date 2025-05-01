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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.android.designcompose.DocContent
import com.android.designcompose.common.DesignDocId
import com.android.designcompose.common.FeedbackImpl
import com.android.designcompose.common.FeedbackLevel
import com.android.designcompose.common.GenericDocContent
import com.android.designcompose.common.NodeQuery
import com.android.designcompose.common.decodeDiskBaseDoc
import com.android.designcompose.common.decodeServerBaseDoc
import com.android.designcompose.common.views
import com.android.designcompose.definition.DesignComposeDefinition
import com.android.designcompose.definition.DesignComposeDefinitionHeader
import com.android.designcompose.definition.element.Bounds
import com.android.designcompose.definition.element.KeyframeVariant
import com.android.designcompose.definition.element.ScalableUIComponentSet
import com.android.designcompose.definition.element.ScalableUiVariant
import com.android.designcompose.definition.element.bounds
import com.android.designcompose.definition.element.copy
import com.android.designcompose.definition.element.scalableDimension
import com.android.designcompose.definition.element.setOrNull
import com.android.designcompose.definition.element.variantOrNull
import com.android.designcompose.definition.view.nodeStyleOrNull
import com.android.designcompose.definition.view.scalableDataOrNull
import com.android.designcompose.definition.view.styleOrNull
import com.android.designcompose.definition.view.View
import com.android.designcompose.live_update.ConvertResponse
import com.android.designcompose.decodeDiskDoc
import java.io.File
import java.io.InputStream

private const val TAG = "DC_ScalableUiDoc"

object FeedbackLogger : FeedbackImpl() {
    override fun logMessage(str: String, level: FeedbackLevel) {
        when (level) {
            FeedbackLevel.Debug -> Log.d(TAG, str)
            FeedbackLevel.Info -> Log.i(TAG, str)
            FeedbackLevel.Warn -> Log.w(TAG, str)
            FeedbackLevel.Error -> Log.e(TAG, str)
        }
    }
}

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

class ScalableUiDoc(docStream: InputStream)  {
    // variant name -> scalable ui data
    val variantMap: HashMap<String, ScalableUiVariant> = HashMap()
    // variant id -> scalable ui data
    val variantIdMap: HashMap<String, ScalableUiVariant> = HashMap()
    // component set name -> { event name -> variant name }
    val componentSetMap: HashMap<String, ScalableUIComponentSet> = HashMap()

    init {
        val designDefinition = parseDcfStream(docStream)
        val variantViewMap = createVariantViewMap(designDefinition.views())

        val allViews = designDefinition.views()
        variantViewMap.forEach { setMap ->
            // Create a mapping of component set names to the scalable ui data for that set
            val componentSetQuery = NodeQuery.NodeComponentSet(setMap.key)
            val setView = allViews[componentSetQuery]
            setView?.styleOrNull?.nodeStyleOrNull?.scalableDataOrNull?.setOrNull?.let { setData ->
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
                        if (child.name == "main") {
                            val layout = child.style.layoutStyle
                            val scalableUiVariant =
                                variant.styleOrNull
                                    ?.nodeStyleOrNull
                                    ?.scalableDataOrNull
                                    ?.variantOrNull
                                    ?.copy {
                                        isVisible =
                                            when (child.style.nodeStyle.displayType) {
                                                com.android.designcompose.definition.view.Display
                                                    .DISPLAY_NONE -> false
                                                else -> true
                                            }
                                        alpha =
                                            if (child.style.nodeStyle.hasOpacity())
                                                child.style.nodeStyle.opacity
                                            else 1f
                                        bounds = bounds {
                                            left = scalableDimension {
                                                points = layout.margin.start.points
                                            }
                                            top = scalableDimension {
                                                points = layout.margin.top.points
                                            }
                                            right = scalableDimension {
                                                points = layout.margin.end.points
                                            }
                                            bottom = scalableDimension {
                                                points = layout.margin.bottom.points
                                            }
                                            width = scalableDimension {
                                                points = layout.width.points
                                            }
                                            height = scalableDimension {
                                                points = layout.height.points
                                            }
                                        }
                                    }
                            // Opulate the variant maps by name and id
                            scalableUiVariant?.let {
                                this.variantMap[variantMap.key] = it
                                this.variantIdMap[variant.id] = it
                            }
                        }
                    }
                }
            }

            // Print out debugging data of what we parsed
            setView?.styleOrNull?.nodeStyleOrNull?.scalableDataOrNull?.setOrNull?.let { setData ->
                val setName = setData.name
                componentSetMap[setName] = setData
                Log.i(TAG, "Set ${setData.name}, ${setData.id}")
                setData.variantIdsList.forEach {
                    Log.i(TAG,
                        "  Variant $it: Default ${variantIdMap[it]?.isDefault} " +
                        "Visible ${variantIdMap[it]?.isVisible}"
                    )
                }
            }
        }
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

    fun hasVariant(variantName: String): Boolean {
        return variantMap.containsKey(variantName)
    }

    fun getVisible(variantName: String): Boolean {
        return variantMap[variantName]?.isVisible == true
    }

    fun getBounds(variantName: String): Bounds? {
        return variantMap[variantName]?.bounds
    }

    fun getKeyframeVariantList(componentSetName: String): List<KeyframeVariant>? {
        return componentSetMap[componentSetName]?.keyframeVariantsList
    }

    fun getPanels(): List<ScalableUIComponentSet> {
        return componentSetMap.values.toList()
    }

    fun getVariantById(id: String): ScalableUiVariant? {
        return variantIdMap[id]
    }
}
